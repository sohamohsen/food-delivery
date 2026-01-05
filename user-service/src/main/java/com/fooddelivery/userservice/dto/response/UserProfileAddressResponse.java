package com.fooddelivery.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileAddressResponse {

    private String country;
    private String city;
    private String area;
    private String street;
    private String buildingNumber;
    private String apartmentNumber;

    private Double latitude;
    private Double longitude;
}
