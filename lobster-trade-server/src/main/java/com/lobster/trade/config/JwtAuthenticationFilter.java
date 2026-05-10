package com.lobster.trade.config;

import com.lobster.trade.service.JwtAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthService jwtAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // SSE subscribe endpoints use token=queryParam instead of Authorization header
        if (path.contains("/subscribe") && request.getParameter("token") != null) {
            String token = request.getParameter("token");
            try {
                Long userId = jwtAuthService.getUserIdFromToken(token);
                if (userId != null) {
                    request.setAttribute("userId", userId);
                }
            } catch (Exception e) {
                // Token invalid - let endpoint handle auth error
            }
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                Long userId = jwtAuthService.getUserIdFromToken(token);
                if (userId != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.emptyList()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    request.setAttribute("userId", userId);
                }
            } catch (Exception e) {
                // Token invalid - proceed without authentication
            }
        }

        filterChain.doFilter(request, response);
    }
}