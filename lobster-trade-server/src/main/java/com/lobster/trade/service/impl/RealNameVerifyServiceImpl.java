package com.lobster.trade.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.mapper.UserRealNameMapper;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.UserRealName;
import com.lobster.trade.service.RealNameVerifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealNameVerifyServiceImpl implements RealNameVerifyService {

    private final UserRealNameMapper realNameMapper;
    private final UserMapper userMapper;

    @Value("${aliyun.face-verify.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.face-verify.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.face-verify.scene:RPBasic}")
    private String scene;

    private static final String VERIFY_URL = "https://faceverify.cn-shanghai.aliyuncs.com";

    @Override
    @Transactional
    public Map<String, String> initCertification(Long userId) {
        // 生成认证token（实际场景调用阿里云API获取certifyId）
        String certifyToken = UUID.randomUUID().toString().replace("-", "");

        // 查询是否有进行中的认证记录
        UserRealName existing = getPendingCert(userId);

        if (existing == null) {
            UserRealName record = new UserRealName();
            record.setUserId(userId);
            record.setAliyunVerifyToken(certifyToken);
            record.setStatus(0); // 审核中
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            realNameMapper.insert(record);
        } else {
            existing.setAliyunVerifyToken(certifyToken);
            existing.setUpdateTime(LocalDateTime.now());
            realNameMapper.updateById(existing);
        }

        // 构建认证页面URL（阿里云实人认证H5跳转URL，含回调地址）
        String callbackUrl = "https://your-domain.com/api/real-name/callback";
        Map<String, String> result = new HashMap<>();
        result.put("certifyToken", certifyToken);

        // 如果配置了阿里云凭证，使用真实接口
        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret)) {
            result.put("verifyUrl", buildAliyunVerifyUrl(certifyToken, callbackUrl));
        } else {
            // DEMO模式：返回本地演示URL
            result.put("verifyUrl", "/api/real-name/demo-page?token=" + certifyToken);
        }
        result.put("mode", StringUtils.hasText(accessKeyId) ? "production" : "demo");

        return result;
    }

    @Override
    @Transactional
    public void handleAliyunCallback(Map<String, Object> body) {
        log.info("阿里云实人认证回调: {}", JSON.toJSONString(body));

        String certifyId = (String) body.get("certifyId");
        String verifyToken = (String) body.get("verifyToken");
        Integer statusCode = (Integer) body.get("statusCode"); // 2000=通过

        UserRealName record = realNameMapper.selectOne(
                new LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getAliyunVerifyToken, verifyToken)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );

        if (record == null) {
            log.warn("认证回调找不到记录: verifyToken={}", verifyToken);
            return;
        }

        record.setAliyunVerifyResult(JSON.toJSONString(body));
        record.setVerifyTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        if (statusCode != null && statusCode == 2000) {
            // 认证通过
            record.setStatus(1); // 已通过
            // 同步更新用户表的实名状态
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                user.setRealNameStatus(1);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
            log.info("用户 {} 实名认证通过", record.getUserId());
        } else {
            // 认证未通过
            record.setStatus(2);
            record.setRejectReason("认证失败，statusCode=" + statusCode);
            log.info("用户 {} 实名认证未通过, statusCode={}", record.getUserId(), statusCode);
        }

        realNameMapper.updateById(record);
    }

    /**
     * 简单表单申请实名认证（不经过阿里云，用户手动填姓名+身份证）
     */
    @Override
    @Transactional
    public void applyRealName(Long userId, String realName, String idCard) {
        if (userId == null) throw new BusinessException(ErrorCode.PARAM_INVALID, "用户ID不能为空");
        if (!StringUtils.hasText(realName)) throw new BusinessException(ErrorCode.PARAM_INVALID, "真实姓名不能为空");
        if (!StringUtils.hasText(idCard)) throw new BusinessException(ErrorCode.PARAM_INVALID, "身份证号不能为空");

        // 检查是否已有记录
        UserRealName existing = realNameMapper.selectOne(
                new LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getUserId, userId)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );

        if (existing != null && existing.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "您已有认证申请正在审核中，请耐心等待");
        }

        // 保存或更新记录
        UserRealName record = new UserRealName();
        record.setUserId(userId);
        record.setRealName(realName);
        record.setIdCard(idCard);
        record.setStatus(0); // 审核中
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        realNameMapper.insert(record);
    }

    @Override
    public Map<String, Object> getCertificationStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();

        UserRealName record = realNameMapper.selectOne(
                new LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getUserId, userId)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );

        if (record == null) {
            result.put("status", 0); // 从未提交
            result.put("statusText", "未认证");
            return result;
        }

        result.put("status", record.getStatus());
        result.put("statusText", statusText(record.getStatus()));
        result.put("createTime", record.getCreateTime());
        result.put("verifyTime", record.getVerifyTime());
        result.put("rejectReason", record.getRejectReason());

        // 返回姓名和身份证（脱敏）供前端展示
        if (StringUtils.hasText(record.getRealName())) {
            result.put("realName", record.getRealName());
        }
        if (StringUtils.hasText(record.getIdCard())) {
            result.put("idCard", maskIdCard(record.getIdCard()));
        }

        return result;
    }

    @Override
    public Map<String, Object> getVerifyResult(Long userId) {
        UserRealName record = realNameMapper.selectOne(
                new LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getUserId, userId)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );

        if (record == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "无认证记录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", record.getStatus());
        result.put("statusText", statusText(record.getStatus()));
        result.put("realName", record.getRealName());
        result.put("idCard", maskIdCard(record.getIdCard()));
        result.put("verifyTime", record.getVerifyTime());

        if (!StringUtils.isEmpty(record.getAliyunVerifyResult())) {
            try {
                JSONObject json = JSON.parseObject(record.getAliyunVerifyResult());
                result.put("aliyunResult", json);
            } catch (Exception ignored) {}
        }

        return result;
    }

    private UserRealName getPendingCert(Long userId) {
        return realNameMapper.selectOne(
                new LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getUserId, userId)
                        .in(UserRealName::getStatus, 0, 2)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );
    }

    private String buildAliyunVerifyUrl(String certifyToken, String callbackUrl) {
        // 阿里云实人认证接入URL格式
        // 实际使用时通过阿里云OpenAPI获取认证页面URL
        return VERIFY_URL + "/certify?token=" + certifyToken + "&callback=" + callbackUrl;
    }

    private String statusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "审核中";
            case 1 -> "已认证";
            case 2 -> "未通过";
            case 3 -> "已撤回";
            default -> "未知";
        };
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return idCard;
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }
}