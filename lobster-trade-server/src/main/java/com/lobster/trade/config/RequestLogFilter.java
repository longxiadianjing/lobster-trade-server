package com.lobster.trade.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@Slf4j
@Component
@WebFilter(urlPatterns = "/*")
public class RequestLogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();
        
        if (path.contains("/subscribe")) {
            System.out.println("[RequestLogFilter] BEFORE chain for: " + path + " token=" + req.getParameter("token"));
        }
        
        chain.doFilter(request, response);
        
        if (path.contains("/subscribe")) {
            System.out.println("[RequestLogFilter] AFTER chain for: " + path);
        }
    }
}