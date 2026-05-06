package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.mapper.ServiceProviderCertificationMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.ServiceProviderCertification;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.service.CertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

    private final ServiceProviderCertificationMapper certMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyCertification(Long userId, String certType, Long gameId,
                                    String description, String regions,
                                    BigDecimal hourlyRate, Integer providerLevel, String credentials) {
        // 检查是否已有认证申请（待审或通过状态）
        ServiceProviderCertification existing = getUserCertification(userId);
        if (existing != null) {
            if (existing.getStatus() == 0) {
                throw new BusinessException("您已有待审核的认证申请，请耐心等待");
            }
            if (existing.getStatus() == 1) {
                throw new BusinessException("您已是认证服务商，无需重复申请");
            }
            // 拒绝/冻结状态：允许重新申请
        }

        // 检查用户是否已实名
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getRealNameStatus() == null || user.getRealNameStatus() != 1) {
            throw new BusinessException("请先完成实名认证后再申请服务商认证");
        }

        ServiceProviderCertification cert = new ServiceProviderCertification();
        cert.setUserId(userId);
        cert.setCertificationType(certType);
        cert.setGameId(gameId);
        cert.setServiceDescription(description);
        cert.setServiceRegions(regions);
        cert.setHourlyRate(hourlyRate);
        cert.setProviderLevel(providerLevel != null ? providerLevel : 1);
        cert.setCredentials(credentials);
        cert.setStatus(0); // 待审核
        cert.setSubmitTime(LocalDateTime.now());
        certMapper.insert(cert);
    }

    @Override
    public ServiceProviderCertification getUserCertification(Long userId) {
        LambdaQueryWrapper<ServiceProviderCertification> q = new LambdaQueryWrapper<>();
        q.eq(ServiceProviderCertification::getUserId, userId)
         .ne(ServiceProviderCertification::getStatus, 3) // 排除已冻结的
         .orderByDesc(ServiceProviderCertification::getCreateTime)
         .last("LIMIT 1");
        return certMapper.selectOne(q);
    }

    @Override
    public List<ServiceProviderCertification> getPendingCertifications() {
        LambdaQueryWrapper<ServiceProviderCertification> q = new LambdaQueryWrapper<>();
        q.eq(ServiceProviderCertification::getStatus, 0)
         .orderByAsc(ServiceProviderCertification::getSubmitTime);
        return certMapper.selectList(q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewCertification(Long certId, Integer status, String rejectReason, Integer providerLevel) {
        ServiceProviderCertification cert = certMapper.selectById(certId);
        if (cert == null) throw new BusinessException("认证申请不存在");
        // 待审核才能操作通过/拒绝，已通过才能操作撤销
        if (cert.getStatus() == 0 && status != 1 && status != 2) {
            throw new BusinessException("该申请待审核，请先通过或拒绝");
        }
        if (cert.getStatus() == 1 && status != 3) {
            throw new BusinessException("该申请已处理");
        }
        if (cert.getStatus() == 1 && status == 3) {
            // 撤销认证：将状态改为冻结(3)
            LambdaUpdateWrapper<ServiceProviderCertification> u = new LambdaUpdateWrapper<>();
            u.eq(ServiceProviderCertification::getId, certId)
             .set(ServiceProviderCertification::getStatus, 3)
             .set(ServiceProviderCertification::getReviewTime, LocalDateTime.now())
             .set(ServiceProviderCertification::getRejectReason, "管理员撤销认证");
            certMapper.update(null, u);
            // 清除用户的userLevel
            User user = userMapper.selectById(cert.getUserId());
            if (user != null) {
                user.setUserLevel(null);
                userMapper.updateById(user);
            }
            return;
        }
        if (status == 2 && (rejectReason == null || rejectReason.isBlank())) {
            throw new BusinessException("请填写拒绝原因");
        }

        LambdaUpdateWrapper<ServiceProviderCertification> u = new LambdaUpdateWrapper<>();
        u.eq(ServiceProviderCertification::getId, certId)
         .set(ServiceProviderCertification::getStatus, status)
         .set(ServiceProviderCertification::getReviewTime, LocalDateTime.now())
         .set(ServiceProviderCertification::getRejectReason, rejectReason);

        if (status == 1) {
            // 通过：设置1年有效期
            u.set(ServiceProviderCertification::getExpireTime, LocalDateTime.now().plusYears(1));
            u.set(ServiceProviderCertification::getProviderLevel, providerLevel != null ? providerLevel : cert.getProviderLevel());
        }

        certMapper.update(null, u);

        // 如果通过，同步更新用户的服务商标识
        if (status == 1) {
            User user = userMapper.selectById(cert.getUserId());
            if (user != null) {
                Integer level = providerLevel != null ? providerLevel : cert.getProviderLevel();
                user.setUserLevel(level); // 同步服务商等级
                userMapper.updateById(user);
            }
        }
    }

    @Override
    public List<ServiceProviderCertification> getCertifiedProviders(String certType, Long gameId) {
        LambdaQueryWrapper<ServiceProviderCertification> q = new LambdaQueryWrapper<>();
        q.eq(ServiceProviderCertification::getStatus, 1); // 已认证
        if (certType != null && !certType.isBlank()) {
            q.eq(ServiceProviderCertification::getCertificationType, certType);
        }
        if (gameId != null) {
            q.eq(ServiceProviderCertification::getGameId, gameId);
        }
        q.orderByDesc(ServiceProviderCertification::getCreateTime);
        return certMapper.selectList(q);
    }
}
