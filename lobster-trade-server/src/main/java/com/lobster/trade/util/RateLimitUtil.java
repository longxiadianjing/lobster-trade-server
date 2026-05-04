package com.lobster.trade.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的滑动窗口限流工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitUtil {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "rate:";

    /**
     * 尝试获取令牌
     * @param key       限流key（如手机号、IP、userId）
     * @param limit     窗口内最大请求数
     * @param windowSec 窗口秒数
     * @return true = 通过，false = 被限流
     */
    public boolean tryAcquire(String key, int limit, long windowSec) {
        String redisKey = PREFIX + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) return true;
            if (count == 1) {
                redisTemplate.expire(redisKey, windowSec, TimeUnit.SECONDS);
            }
            if (count > limit) {
                log.warn("[RATE_LIMIT] key={} count={} limit={} blocked", redisKey, count, limit);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[RATE_LIMIT] Redis error, allow request. key={}", redisKey, e);
            return true; // Redis 挂了不阻止业务
        }
    }

    /**
     * 检查手机号短信限流（每分钟1次）
     */
    public boolean isSmsLimited(String phone) {
        return !tryAcquire("sms:" + phone, 1, 60);
    }

    /**
     * 检查IP登录限流（每分钟10次）
     */
    public boolean isLoginLimited(String ip) {
        return !tryAcquire("login:" + ip, 10, 60);
    }

    /**
     * 检查用户支付限流（每分钟5次）
     */
    public boolean isPaymentLimited(Long userId) {
        return !tryAcquire("pay:" + userId, 5, 60);
    }
}