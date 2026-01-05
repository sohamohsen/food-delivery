package com.fooddelivery.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileBasicResponse {

    private Long userId;

    private String fullName;
    private String phone;
    private String email;

    private LocalDate dateOfBirth;
    private String gender;

    private String role;
    private String status;

    private LocalDateTime createdAt;
}
