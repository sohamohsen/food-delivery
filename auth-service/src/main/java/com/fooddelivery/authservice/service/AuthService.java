package com.fooddelivery.authservice.service;

import com.fooddelivery.authservice.dto.requests.LoginRequest;
import com.fooddelivery.authservice.dto.requests.RegistrationRequest;
import com.fooddelivery.authservice.dto.response.AuthResponse;

public interface AuthService {
    void registration(RegistrationRequest request);
    AuthResponse login(LoginRequest request);
}
