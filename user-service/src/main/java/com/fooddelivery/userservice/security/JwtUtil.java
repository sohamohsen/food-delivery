package com.fooddelivery.userservice.security;

import com.fooddelivery.userservice.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secret}")
    private String secretKey;

    /* ========= Validation ========= */
    public void validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(token);

        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException("JWT token expired");

        } catch (UnsupportedJwtException ex) {
            throw new JwtAuthenticationException("Unsupported JWT token");

        } catch (MalformedJwtException ex) {
            throw new JwtAuthenticationException("Malformed JWT token");

        } catch (SecurityException | IllegalArgumentException ex) {
            throw new JwtAuthenticationException("Invalid JWT token");
        }
    }

    /* ========= Claims ========= */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);

        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            throw new JwtAuthenticationException("JWT does not contain userId");
        }

        return userId;
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);

        String email = claims.get("email", String.class);
        if (email == null) {
            throw new JwtAuthenticationException("JWT does not contain email");
        }

        return email;
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);

        String role = claims.get("role", String.class);
        if (role == null) {
            throw new JwtAuthenticationException("JWT does not contain role");
        }

        return role;
    }
}
