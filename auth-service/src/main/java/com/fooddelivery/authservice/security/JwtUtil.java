package com.fooddelivery.authservice.security;

import com.fooddelivery.authservice.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secret}")
    private String secretKey;
    @Value("${spring.security.jwt.expiration}")
    private long expirationDate;

    public String generateToken (User user){
        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(addClaims(user))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationDate))
                .signWith(getSignedKey() ,SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, Object> addClaims(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        return claims;
    }

    public Claims extractAllClaims (String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail (String token){
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }


    public void validateToken(String token) {
        try {
            extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid token");
        }
    }


    private Key getSignedKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
