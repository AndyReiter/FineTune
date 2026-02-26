package com.finetune.app.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    // Hardcoded secret for development/demo purposes. Replace with secure random value in production.
    private static final String JWT_SECRET = "hardcoded-demo-secret-which-is-at-least-32-bytes!";

    private SecretKey signingKey;

    private static final long EXPIRATION_MS = 60 * 60 * 1000L; // 1 hour

    @PostConstruct
    public void init() {
        // Use the hardcoded secret bytes for HS256 signing
        byte[] keyBytes = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a signed JWT using HS256 with 1 hour expiry. Subject is the email.
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate token including shop IDs as a claim.
     */
    public String generateToken(String email, java.util.List<Long> shopIds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256);

        if (shopIds != null && !shopIds.isEmpty()) {
            builder.claim("shopIds", shopIds);
            // include a convenience single shopId claim (first shop)
            builder.claim("shopId", shopIds.get(0));
        }

        return builder.compact();
    }

    /**
     * Generate token including staffUserId and shop IDs.
     */
    public String generateToken(Long staffUserId, String email, java.util.List<Long> shopIds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .claim("staffUserId", staffUserId);

        if (shopIds != null && !shopIds.isEmpty()) {
            builder.claim("shopIds", shopIds);
            builder.claim("shopId", shopIds.get(0));
        }

        return builder.compact();
    }

    /**
     * Extract shop IDs claim from token (returns empty list if missing/invalid).
     */
    public java.util.List<Long> getShopIdsFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
            Object raw = claims.get("shopIds");
            if (raw instanceof java.util.List) {
                java.util.List<?> rawList = (java.util.List<?>) raw;
                java.util.List<Long> out = new java.util.ArrayList<>();
                for (Object o : rawList) {
                    if (o instanceof Number) out.add(((Number)o).longValue());
                    else if (o instanceof String) {
                        try { out.add(Long.parseLong((String)o)); } catch (Exception ignore) {}
                    }
                }
                return out;
            }
        } catch (Exception e) {
            // ignore
        }
        return java.util.List.of();
    }

    /**
     * Validate token signature and expiration. Returns true if valid.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract email (subject) from token. Returns null if token invalid.
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse and return the token claims, or null on error.
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
