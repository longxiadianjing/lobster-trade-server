package com.lobster.trade.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class SpaFallbackController {

    /** 单段路径的 SPA 路由，如 /home, /login 等（排除 assets） */
    @GetMapping(value = "/{pathSegment}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> fallback(@PathVariable String pathSegment) {
        if ("assets".equals(pathSegment) || "favicon.svg".equals(pathSegment)) {
            return ResponseEntity.notFound().build();
        }
        // 排除 /admin 下的所有路径（由 WebMvcConfig 处理）
        if ("admin".equals(pathSegment)) {
            return ResponseEntity.notFound().build();
        }
        return serveIndex();
    }

    private ResponseEntity<String> serveIndex() {
        try {
            ClassPathResource resource = new ClassPathResource("static/index.html");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }
}