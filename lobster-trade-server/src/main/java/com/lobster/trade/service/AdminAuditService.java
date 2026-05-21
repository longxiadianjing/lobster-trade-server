package com.lobster.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.mapper.AdminAuditLogMapper;
import com.lobster.trade.model.entity.AdminAuditLog;
import com.lobster.trade.model.request.AuditLogRequest;
import com.lobster.trade.model.response.AuditLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogMapper auditLogMapper;

    @Async
    public void logAsync(Long adminId, String adminUsername, String action,
                         String entityType, Long entityId, String detail,
                         HttpServletRequest request) {
        try {
            AdminAuditLog log = new AdminAuditLog();
            log.setAdminId(adminId);
            log.setAdminUsername(adminUsername);
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setDetail(detail);
            log.setIp(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
        }
    }

    public IPage<AuditLogVO> list(AuditLogRequest req) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminAuditLog>();
        if (req.getAdminId() != null) q.eq(AdminAuditLog::getAdminId, req.getAdminId());
        if (req.getAction() != null && !req.getAction().isEmpty()) q.like(AdminAuditLog::getAction, req.getAction());
        if (req.getEntityType() != null && !req.getEntityType().isEmpty()) q.like(AdminAuditLog::getEntityType, req.getEntityType());
        if (req.getStartTime() != null) q.ge(AdminAuditLog::getCreateTime, req.getStartTime());
        if (req.getEndTime() != null) q.le(AdminAuditLog::getCreateTime, req.getEndTime());
        q.orderByDesc(AdminAuditLog::getCreateTime);
        Page<AdminAuditLog> page = new Page<>(req.getPage(), req.getSize());
        IPage<AdminAuditLog> result = auditLogMapper.selectPage(page, q);
        return result.convert(this::toVO);
    }

    public AuditLogVO toVO(AdminAuditLog l) {
        AuditLogVO vo = new AuditLogVO();
        vo.setId(l.getId());
        vo.setAdminId(l.getAdminId());
        vo.setAdminUsername(l.getAdminUsername());
        vo.setAction(l.getAction());
        vo.setEntityType(l.getEntityType());
        vo.setEntityId(l.getEntityId());
        vo.setDetail(l.getDetail());
        vo.setIp(l.getIp());
        vo.setCreateTime(l.getCreateTime());
        return vo;
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) return xf.split(",")[0];
        return request.getRemoteAddr();
    }
}