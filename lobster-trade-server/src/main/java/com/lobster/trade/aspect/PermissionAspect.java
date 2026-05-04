package com.lobster.trade.aspect;

import com.lobster.trade.annotation.RequirePermission;
import com.lobster.trade.config.AdminContext;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.model.entity.Admin;
import com.lobster.trade.model.entity.AdminPermission;
import com.lobster.trade.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionAspect {

    private final AdminService adminService;

    @Before("@annotation(com.lobster.trade.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        Long adminId = AdminContext.get();
        if (adminId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) return;
        String[] required = annotation.value();
        boolean hasAccess;
        if (annotation.all()) {
            // 全部权限都需要
            hasAccess = adminService.hasAllPermissions(required);
        } else {
            // 任一权限即可
            hasAccess = adminService.hasAnyPermission(required);
        }
        if (!hasAccess) {
            log.warn("Permission denied: adminId={}, required={}", adminId, String.join(",", required));
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有操作权限");
        }
    }
}