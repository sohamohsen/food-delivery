package com.fooddelivery.userservice.configration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.userservice.exception.ApiResponse;
import com.fooddelivery.userservice.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint((request, response, authException) -> {
                            ApiResponse error = ApiResponse.builder()
                                    .status(HttpStatus.UNAUTHORIZED.value())
                                    .message("Unauthorized: Invalid or missing token")
                                    .timeStamp(LocalDateTime.now())
                                    .build();

                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            new ObjectMapper().writeValue(
                                    response.getOutputStream(), error
                            );
                        })

                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ApiResponse error = ApiResponse.builder()
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message("Forbidden: insufficient permissions")
                                    .timeStamp(LocalDateTime.now())
                                    .build();

                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            new ObjectMapper().writeValue(
                                    response.getOutputStream(), error
                            );
                        })
                );

        return http.build();
    }
}
