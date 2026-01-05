package com.fooddelivery.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileUpsertBasicDataRequest {

    private String fullName;

    @Pattern(
            regexp = "^01[0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phone;

    private LocalDate dateOfBirth;

    @Pattern(
            regexp = "MALE|FEMALE",
            message = "Gender must be MALE, FEMALE"
    )
    private String gender;
}
