package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.request.*;
import com.fooddelivery.userservice.dto.response.UserProfileAddressResponse;
import com.fooddelivery.userservice.dto.response.UserProfileBasicResponse;
import com.fooddelivery.userservice.dto.response.UserProfilePictureResponse;
import com.fooddelivery.userservice.exception.ApiResponse;
import com.fooddelivery.userservice.service.UserProfileService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping("/basic")
    public ResponseEntity<ApiResponse<UserProfileBasicResponse>> upsertBasicData(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UserProfileUpsertBasicDataRequest request
    ) {
        UserProfileBasicResponse userProfileBasic = userProfileService.upsertBasicData(user, request);

        ApiResponse<UserProfileBasicResponse> responseBody = ApiResponse.<UserProfileBasicResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Your Profile data added successfully")
                .data(userProfileBasic)
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping("/basic")
    public ResponseEntity<ApiResponse<UserProfileBasicResponse>> getBasicProfile(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        UserProfileBasicResponse basicResponse = userProfileService.getUserProfile(user.getUserId());
        ApiResponse<UserProfileBasicResponse> response = ApiResponse.<UserProfileBasicResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Your profile data")
                .data(basicResponse)
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }


    @PutMapping("/address")
    public ResponseEntity<ApiResponse<Void>> updateAddress(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UserProfileUpsertAddressRequest request
    ) {
        userProfileService.updateAddress(request, user.getUserId());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Address updated successfully")
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/address")
    public ResponseEntity<ApiResponse<UserProfileAddressResponse>> getAddress(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        ApiResponse<UserProfileAddressResponse> response = ApiResponse.<UserProfileAddressResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Address data get successfully")
                .data(userProfileService.getUserAddress(user.getUserId()))
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    // ================= LOCATION =================

    @PutMapping("/location")
    @RolesAllowed("RESTAURANT")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UserProfileUpdateLocationRequest request
    ) {
        userProfileService.updateLocation(request, user.getUserId());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Address updated successfully")
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    // ================= PROFILE IMAGE =================

    @PutMapping("/image")
    public ResponseEntity<ApiResponse<Void>> updateProfileImage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam("image") MultipartFile image
    ) {
        UserProfileImageRequest request = new UserProfileImageRequest();
        request.setImage(image);
        userProfileService.updateProfileImage(request, user.getUserId());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Image updated successfully")
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);

    }

    @GetMapping("/image")
    public ResponseEntity<ApiResponse<UserProfilePictureResponse>>  getProfileImage(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        UserProfilePictureResponse userProfilePictureResponse = userProfileService.getProfilePicture(user.getUserId());
        ApiResponse<UserProfilePictureResponse> response = ApiResponse.<UserProfilePictureResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Address data get successfully")
                .data(userProfilePictureResponse)
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
