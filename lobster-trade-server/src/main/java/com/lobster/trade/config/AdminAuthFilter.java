package com.lobster.trade.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();

        // 仅拦截 /api/admin/**（登录接口和录单识别接口除外）
        if (path.startsWith("/api/admin")) {
            // 放行登录
            if (path.equals("/api/admin/login")
                    || path.equals("/api/admin/order/recognize")) {
                chain.doFilter(request, response);
                return;
            }

            // Allow ?token=xxx for SSE subscribe endpoints
            if (path.contains("/subscribe") && req.getParameter("token") != null) {
                chain.doFilter(request, response);
                return;
            }

            String auth = req.getHeader("Authorization");
            if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
                HttpServletResponse resp = (HttpServletResponse) response;
                resp.setStatus(401);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
                return;
            }

            String token = auth.substring(7);
            try {
                // 解析 token，获取 adminId（使用 JwtUtil 静态方法）
                java.util.Map<String, Object> payload = com.lobster.trade.util.JwtUtil.verifyToken(token);
                if (payload == null) {
                    sendUnauthorized((HttpServletResponse) response, "无效Token");
                    return;
                }
                Object adminIdObj = payload.get("adminId");
                Long adminId = null;
                if (adminIdObj instanceof Integer) {
                    adminId = ((Integer) adminIdObj).longValue();
                } else if (adminIdObj instanceof Long) {
                    adminId = (Long) adminIdObj;
                }
                if (adminId != null) {
                    AdminContext.set(adminId);
                    chain.doFilter(request, response);
                } else {
                    sendUnauthorized((HttpServletResponse) response, "无效Token");
                }
            } catch (Exception e) {
                log.warn("Admin JWT verify failed: {}", e.getMessage());
                sendUnauthorized((HttpServletResponse) response, "Token验证失败");
            } finally {
                AdminContext.remove();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private void sendUnauthorized(HttpServletResponse resp, String msg) throws IOException {
        resp.setStatus(401);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"code\":401,\"message\":\"" + msg + "\"}");
    }
}