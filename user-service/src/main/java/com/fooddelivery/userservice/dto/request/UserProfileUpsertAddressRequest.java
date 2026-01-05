package com.fooddelivery.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileUpsertAddressRequest {

    private String country;
    private String city;
    private String area;
    private String street;
    private String buildingNumber;
    private String apartmentNumber;

}
