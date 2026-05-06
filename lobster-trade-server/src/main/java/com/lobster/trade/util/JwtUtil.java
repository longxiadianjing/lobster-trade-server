package com.lobster.trade.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expire:86400000}")
    private Long expire;

    // Static reference to the secret, initialized from the injected value
    private static String STATIC_SECRET;
    private static Long STATIC_EXPIRE;

    @PostConstruct
    public void init() {
        STATIC_SECRET = secret;
        STATIC_EXPIRE = expire;
    }

    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, String.valueOf(userId));
    }

    public String generateToken(Map<String, Object> claims) {
        return generateTokenStatic(claims);
    }

    // 静态方法，供 Admin 等模块使用
    public static String generateTokenStatic(Map<String, Object> claims) {
        Object id = claims.get("adminId");
        String subject = id != null ? String.valueOf(id) : "admin";
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + (STATIC_EXPIRE != null ? STATIC_EXPIRE : 86400000L));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, STATIC_SECRET)
                .compact();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Static method used by services (consistent with instance secret)
    public static Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(STATIC_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    // 验证 token 并返回完整 payload（供 Admin 等模块使用）
    public static Map<String, Object> verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(STATIC_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
            Map<String, Object> map = new HashMap<>();
            map.put("userId", claims.get("userId"));
            map.put("adminId", claims.get("adminId"));
            map.put("username", claims.get("username"));
            map.put("role", claims.get("role"));
            return map;
        } catch (Exception e) {
            return null;
        }
    }
}
