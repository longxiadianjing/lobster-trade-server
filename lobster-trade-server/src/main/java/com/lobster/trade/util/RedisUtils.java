package com.lobster.trade.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取分布式锁
     * @param lockKey  锁的key
     * @param expireMs 锁的过期时间（毫秒）
     * @param waitMs   等待锁的最大时间（毫秒）
     * @return 锁的value（用于解锁），null 表示未获取到锁
     */
    public String tryLock(String lockKey, long expireMs, long waitMs) {
        long start = System.currentTimeMillis();
        long deadline = start + waitMs;
        String lockValue = String.valueOf(System.currentTimeMillis());

        while (System.currentTimeMillis() < deadline) {
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, expireMs, TimeUnit.MILLISECONDS);
            if (Boolean.TRUE.equals(acquired)) {
                return lockValue;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    /**
     * 释放分布式锁（仅释放自己持有的锁）
     * @param lockKey 锁的key
     * @param lockValue 加锁时返回的value
     */
    public void unlock(String lockKey, String lockValue) {
        String current = stringRedisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(current)) {
            stringRedisTemplate.delete(lockKey);
        }
    }
}