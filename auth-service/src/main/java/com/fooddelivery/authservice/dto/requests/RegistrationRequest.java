package com.fooddelivery.authservice.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationRequest {

    private String email;
    private String name;
    private String role;
    private String password;
}
