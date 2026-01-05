package com.fooddelivery.authservice.service;

import com.fooddelivery.authservice.dto.requests.LoginRequest;
import com.fooddelivery.authservice.dto.requests.RegistrationRequest;
import com.fooddelivery.authservice.dto.response.AuthResponse;
import com.fooddelivery.authservice.entity.Role;
import com.fooddelivery.authservice.entity.User;
import com.fooddelivery.authservice.exception.InvalidCredentialsException;
import com.fooddelivery.authservice.exception.InvalidRoleException;
import com.fooddelivery.authservice.exception.UserAlreadyExistsException;
import com.fooddelivery.authservice.repository.UserRepository;
import com.fooddelivery.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void registration(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("This email already have account");
        }
        validateRole(request.getRole());

        userRepository.save(convertUser(request));
    }

    private User convertUser (RegistrationRequest request){
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .build();

    }

    private void validateRole(String roleFromRequest) {
        try {
            Role.valueOf(roleFromRequest.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRoleException("Invalid role");
        }
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Wrong email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Wrong email or password");
        }

        String token = jwtUtil.generateToken(user);

        return convertResponse(user, token);
    }

    private AuthResponse convertResponse (User user, String token){
        return AuthResponse.builder()
                .id(user.getId())
                .role(String.valueOf(user.getRole()))
                .token(token)
                .build();
    }
}
