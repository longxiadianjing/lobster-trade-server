package com.lobster.trade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后端 API 服务专用，不再充当 SPA 宿主。
 * 用户前端（lobster-trade-web）由 Vite 开发服务器（localhost:5174）托管。
 */
@RestController
@Configuration
public class SpaFallbackController {

    /**
     * 当浏览器直接访问后端根路径时，返回简单的 API 信息页。
     * 不再尝试返回任何 HTML，避免与前端路由冲突。
     */
    @GetMapping("/")
    public String root() {
        return "龙虾道具交易平台 - API 服务 (端口 8080)\n前端请访问: http://localhost:5174";
    }
}