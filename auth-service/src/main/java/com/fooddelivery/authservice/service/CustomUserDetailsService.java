package com.fooddelivery.authservice.service;

import com.fooddelivery.authservice.entity.User;
import com.fooddelivery.authservice.exception.InvalidCredentialsException;
import com.fooddelivery.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws InvalidCredentialsException {
        User user =  userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("Wrong Email or password"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
