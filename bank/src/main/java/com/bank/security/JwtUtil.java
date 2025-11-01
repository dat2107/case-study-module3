package com.bank.security;

import com.bank.model.Account;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JwtUtil {

    private String jwtSecret;
    private long jwtExpirationMs;

    public JwtUtil() {
        init();
    }

    public void init() {
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("application.properties")) {

            Properties props = new Properties();
            props.load(input);

            jwtSecret = props.getProperty("jwt.secret", "defaultSecretKey");
            jwtExpirationMs = Long.parseLong(props.getProperty("jwt.expiration", "86400000"));

            System.out.println("Loaded JWT config from application.properties");
        } catch (Exception e) {
            throw new RuntimeException("Cannot load JWT config", e);
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String role, Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("accountId", account.getAccountId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public Long extractAccountId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("accountId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            System.err.println("JWT validation failed: " + e.getMessage());
            return false;
        }
    }

}