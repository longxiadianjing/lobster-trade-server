package com.lobster.trade.service;

import java.util.Map;

public interface RealNameVerifyService {

    // User - 初始化阿里云实人认证
    Map<String, String> initCertification(Long userId);

    // 阿里云回调（认证结果通知）
    void handleAliyunCallback(Map<String, Object> body);

    // 查询认证状态
    Map<String, Object> getCertificationStatus(Long userId);

    // 简单表单申请（用户填姓名+身份证，后台审核）
    void applyRealName(Long userId, String realName, String idCard);

    // 查询认证结果明细
    Map<String, Object> getVerifyResult(Long userId);
}