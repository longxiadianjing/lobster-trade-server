package com.lobster.trade.service;

import com.lobster.trade.model.entity.ServiceProviderCertification;
import java.math.BigDecimal;
import java.util.List;

public interface CertificationService {

    /**
     * 用户申请服务商认证
     */
    void applyCertification(Long userId, String certType, Long gameId,
                            String description, String regions,
                            BigDecimal hourlyRate, Integer providerLevel, String credentials);

    /**
     * 获取用户的认证状态
     */
    ServiceProviderCertification getUserCertification(Long userId);

    /**
     * 获取所有待审核的认证申请（管理员）
     */
    List<ServiceProviderCertification> getPendingCertifications();

    /**
     * 审核认证申请（管理员）
     * @param certId 认证记录ID
     * @param status 1通过 2拒绝
     * @param rejectReason 拒绝原因（拒绝时必填）
     */
    void reviewCertification(Long certId, Integer status, String rejectReason, Integer providerLevel);

    /**
     * 获取认证服务商列表（前端展示）
     */
    List<ServiceProviderCertification> getCertifiedProviders(String certType, Long gameId);
}
