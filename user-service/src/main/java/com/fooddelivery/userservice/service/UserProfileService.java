package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.request.*;
import com.fooddelivery.userservice.dto.response.UserProfileAddressResponse;
import com.fooddelivery.userservice.dto.response.UserProfileBasicResponse;
import com.fooddelivery.userservice.dto.response.UserProfilePictureResponse;
import jakarta.transaction.Transactional;

public interface UserProfileService {

    /* ===== Basic Data ===== */
    @Transactional
    UserProfileBasicResponse upsertBasicData( AuthenticatedUser user, UserProfileUpsertBasicDataRequest request);

    /* ===== Address ===== */
    void updateAddress(UserProfileUpsertAddressRequest request, Long userId);

    /* ===== Location (Map / Delivery) ===== */
    void updateLocation(UserProfileUpdateLocationRequest request, Long userId);

    /* ===== Profile Image ===== */
    void updateProfileImage(UserProfileImageRequest request, Long userId);

    /* ===== Read ===== */
    UserProfileBasicResponse getUserProfile(Long userId);
    UserProfileAddressResponse getUserAddress(Long userId);
    UserProfilePictureResponse getProfilePicture(Long userId);

}
