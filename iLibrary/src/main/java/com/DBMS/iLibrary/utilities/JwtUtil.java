package com.DBMS.iLibrary.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${app.jwtSecret}")
    private String secretKey; // ensure this is provided in application.properties (see notes)

    private final long EXPIRATION_MS = 1000L * 60 * 60 * 10; // 10 hours

    // Convert String secret to a Key for signing/verifying (HS256 requires >= 32 bytes)
    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT that includes username as subject and roles as a claim.
     */
    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles == null ? Collections.emptyList() : roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Optional convenience overload: generate token with no roles.
     */
    public String generateToken(String username) {
        return generateToken(username, Collections.emptyList());
    }

    /**
     * Extract claims (centralized parsing)
     */
    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract username (subject)
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract roles claim as a List<String>. Handles different representations safely.
     */
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");

        if (rolesObj == null) return Collections.emptyList();

        // If it's already a List, map to strings
        if (rolesObj instanceof Collection<?>) {
            return ((Collection<?>) rolesObj).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        // If it's a single String (comma separated), split it
        if (rolesObj instanceof String) {
            String s = (String) rolesObj;
            if (s.isBlank()) return Collections.emptyList();
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
        }

        // Unknown type -> return empty list
        return Collections.emptyList();
    }

    /**
     * Check token expiration and structure; returns true when token is well-formed and not expired.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // token invalid / malformed / signature invalid / expired
            return false;
        }
    }
}
