package com.campus.campussharebackend.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 保留原方法（兼容角色存储，可选）
    public String generateToken(Long userId, String roleName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleName", roleName);
        return createToken(claims);
    }

    // 新增：同时包含角色和权限的Token生成方法
    public String generateToken(Long userId, String roleName, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleName", roleName); // 包含角色名
        claims.put("permissions", permissions); // 包含权限列表
        return createToken(claims);
    }

    // 创建Token的底层方法（不变）
    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 解析Token（不变）
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // 通用提取方法（不变）
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    // 获取用户ID（不变）
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    // 新增：从Token中获取权限列表
    public List<String> getPermissionsFromToken(String token) {
        return getClaimFromToken(token, claims -> {
            // 强制转换为List<String>（确保JWT中存储的是List类型）
            return (List<String>) claims.get("permissions");
        });
    }

    // 校验Token是否过期（不变）
    public boolean isTokenExpired(String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    // 保留原角色提取方法（如果需要）
    public String getRoleNameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("roleName", String.class));
    }
}