package com.fooddelivery.authservice.service;

import com.fooddelivery.authservice.dto.requests.LoginRequest;
import com.fooddelivery.authservice.dto.requests.RegistrationRequest;
import com.fooddelivery.authservice.dto.response.AuthResponse;
import com.fooddelivery.authservice.entity.Role;
import com.fooddelivery.authservice.entity.User;
import com.fooddelivery.authservice.exception.InvalidCredentialsException;
import com.fooddelivery.authservice.exception.InvalidRoleException;
import com.fooddelivery.authservice.repository.UserRepository;
import com.fooddelivery.authservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;


    @Test
    void shouldRegisterUserSuccessfully() {
        // given
        RegistrationRequest request = new RegistrationRequest(
                "Soha",
                "soha@test.com",
                "ADMIN",
                "123456"
                );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        // when
        authService.registration(request);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRoleIsInvalid() {
        // given
        RegistrationRequest request = new RegistrationRequest(
                "Soha",
                "soha@test.com",
                "123456",
                "INVALID_ROLE"
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        // then
        assertThrows(
                InvalidRoleException.class,
                () -> authService.registration(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // =========================
    // Login Tests - Success
    // =========================

    @Test
    void shouldLoginSuccessfully() {
        // given
        LoginRequest request = new LoginRequest(
                "soha@test.com",
                "123456"
        );

        User user = User.builder()
                .id(1L)
                .email("soha@test.com")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtUtil.generateToken(user))
                .thenReturn("fake-jwt-token");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals(1L, response.getId());
    }

    // =========================
    // Login Tests - Wrong Password
    // =========================

    @Test
    void shouldThrowExceptionWhenPasswordIsWrong() {
        // given
        LoginRequest request = new LoginRequest(
                "soha@test.com",
                "wrong-password"
        );

        User user = User.builder()
                .email("soha@test.com")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(false);

        // then
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }

    // =========================
    // Login Tests - User Not Found
    // =========================

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        LoginRequest request = new LoginRequest(
                "notfound@test.com",
                "123456"
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        // then
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }
}
