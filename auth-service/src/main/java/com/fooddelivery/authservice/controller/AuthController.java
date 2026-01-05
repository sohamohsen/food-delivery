package com.fooddelivery.authservice.controller;

import com.fooddelivery.authservice.dto.requests.LoginRequest;
import com.fooddelivery.authservice.dto.requests.RegistrationRequest;
import com.fooddelivery.authservice.dto.response.AuthResponse;
import com.fooddelivery.authservice.exception.ApiResponse;
import com.fooddelivery.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<?>> registration(
            @Valid @RequestBody RegistrationRequest request) {

        authService.registration(request);

        ApiResponse<?> response = ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("User registered successfully")
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Login successful")
                .data(authResponse)
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

}
