package com.fooddelivery.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.userservice.dto.request.AuthenticatedUser;
import com.fooddelivery.userservice.exception.ApiResponse;
import com.fooddelivery.userservice.exception.JwtAuthenticationException;
import com.fooddelivery.userservice.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            // 1️⃣ No token → continue
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            // 2️⃣ Validate token
            jwtUtil.validateToken(token);

            // 3️⃣ Extract claims
            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            AuthenticatedUser principal =
                    new AuthenticatedUser(userId, email, role);

            // 4️⃣ Authority
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role);

            // 5️⃣ Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(authority)
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 6️⃣ Set context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException ex) {

            // ✅ HANDLE JWT ERRORS HERE
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            ApiResponse error = ApiResponse.builder()
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .message(ex.getMessage())
                    .timeStamp(LocalDateTime.now())
                    .build();

            response.getWriter()
                    .write(objectMapper.writeValueAsString(error));
        }
    }
}
