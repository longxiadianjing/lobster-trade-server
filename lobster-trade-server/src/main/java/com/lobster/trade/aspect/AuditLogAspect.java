package com.lobster.trade.aspect;

import com.lobster.trade.annotation.Audit;
import com.lobster.trade.service.AdminAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AdminAuditService adminAuditService;

    @Around("@annotation(com.lobster.trade.annotation.Audit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Audit audit = signature.getMethod().getAnnotation(Audit.class);

        Long adminId = null;
        String username = "unknown";
        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                request = attrs.getRequest();
                Object aid = request.getAttribute("adminId");
                if (aid != null) adminId = Long.valueOf(aid.toString());
                username = request.getAttribute("adminUsername") != null
                        ? request.getAttribute("adminUsername").toString()
                        : "admin";
            }
        } catch (Exception e) {
            log.warn("[AUDIT] 获取管理员上下文失败: {}", e.getMessage());
        }

        // 提取目标ID
        Long targetId = extractTargetId(point.getArgs(), signature.getParameterNames());

        Object result = point.proceed();

        // 异步记录日志
        try {
            adminAuditService.logAsync(
                    adminId,
                    username,
                    audit.value(),
                    audit.targetType(),
                    targetId,
                    buildDetail(point, result),
                    request
            );
        } catch (Exception e) {
            log.warn("[AUDIT] 记录审计日志失败: {}", e.getMessage());
        }

        return result;
    }

    private Long extractTargetId(Object[] args, String[] paramNames) {
        if (args == null || paramNames == null) return null;
        for (int i = 0; i < paramNames.length; i++) {
            String name = paramNames[i].toLowerCase();
            if (args[i] instanceof Long && (name.equals("id") || name.equals("orderid") || name.equals("userid"))) {
                return (Long) args[i];
            }
            if (args[i] instanceof Integer && name.equals("id")) {
                return ((Integer) args[i]).longValue();
            }
        }
        return null;
    }

    private String buildDetail(ProceedingJoinPoint point, Object result) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            StringBuilder sb = new StringBuilder();
            sb.append("method=").append(point.getTarget().getClass().getSimpleName())
              .append(".").append(signature.getName());
            if (result != null) {
                String r = result.toString();
                sb.append("|result=").append(r.substring(0, Math.min(200, r.length())));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
