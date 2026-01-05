package com.fooddelivery.userservice.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticatedUser {

    private Long userId;
    private String email;
    private String role;
}
