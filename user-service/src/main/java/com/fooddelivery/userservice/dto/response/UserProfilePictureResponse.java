package com.fooddelivery.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfilePictureResponse {

    private String profileImageUrl;
}
