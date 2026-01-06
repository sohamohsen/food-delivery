package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.mapper.UserProfileMapper;
import com.fooddelivery.userservice.dto.request.*;
import com.fooddelivery.userservice.dto.response.UserProfileAddressResponse;
import com.fooddelivery.userservice.dto.response.UserProfileBasicResponse;
import com.fooddelivery.userservice.dto.response.UserProfilePictureResponse;
import com.fooddelivery.userservice.entity.Gender;
import com.fooddelivery.userservice.entity.Role;
import com.fooddelivery.userservice.entity.UserProfile;
import com.fooddelivery.userservice.entity.UserStatus;
import com.fooddelivery.userservice.exception.BadRequestException;
import com.fooddelivery.userservice.exception.ResourceNotFoundException;
import com.fooddelivery.userservice.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper mapper;

    @Transactional
    @Override
    public UserProfileBasicResponse upsertBasicData(AuthenticatedUser user, UserProfileUpsertBasicDataRequest request) {

        return userProfileRepository.findById(user.getUserId())
                .map(existingProfile -> updateProfile(existingProfile, request))
                .orElseGet(()-> createProfile(user, request));
    }

    private UserProfileBasicResponse createProfile(AuthenticatedUser authenticatedUser, UserProfileUpsertBasicDataRequest request) {
        validateCreateRequest(request);

        UserProfile profile = mapper.toEntity(request);

        profile.setUserId(authenticatedUser.getUserId());
        profile.setEmail(authenticatedUser.getEmail());
        profile.setRole(parseRole(authenticatedUser.getRole()));
        profile.setStatus(UserStatus.BASICDATA);

        UserProfile savedProfile = userProfileRepository.save(profile);
        return mapper.toBasicResponse(savedProfile);
    }

    private void validateCreateRequest(UserProfileUpsertBasicDataRequest request) {
        if (request.getFullName() == null ||
                request.getPhone() == null ||
                request.getGender() == null) {

            throw new BadRequestException(
                    "Full name, phone and gender are required when creating profile for the first time"
            );
        }
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role value in token");
        }
    }

    private UserProfileBasicResponse updateProfile(UserProfile profile, UserProfileUpsertBasicDataRequest request) {
        profile.ensureUpdateIsAllowed();
        mapper.updateEntityFromDto(request, profile);
        return mapper.toBasicResponse(userProfileRepository.save(profile));
    }

    @Transactional
    @Override
    public void updateAddress(UserProfileUpsertAddressRequest request, Long userId) {

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile not found for userId: " + userId)
                );


        mapper.updateAddressFromDto(request, userProfile);

        if (UserStatus.BASICDATA.equals(userProfile.getStatus())) {
            if (Role.RESTAURANT.equals(userProfile.getRole()))
                userProfile.setStatus(UserStatus.LOCATION);
            else
                userProfile.setStatus(UserStatus.ACTIVE);
        }
        userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfileAddressResponse getUserAddress(Long userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile not found for userId: " + userId)
                );

        return convertAddressToResponse(userProfile);
    }

    private UserProfileAddressResponse convertAddressToResponse(UserProfile userProfile) {
        return UserProfileAddressResponse.builder()
                .country(userProfile.getCountry())
                .city(userProfile.getCity())
                .area(userProfile.getArea())
                .street(userProfile.getStreet())
                .apartmentNumber(userProfile.getApartmentNumber())
                .buildingNumber(userProfile.getBuildingNumber())
                .latitude(userProfile.getLatitude())
                .longitude(userProfile.getLongitude())
                .build();
    }

    @Override
    public UserProfileBasicResponse getUserProfile(Long userId) {

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile not found for userId: " + userId)
                );

        return mapper.toBasicResponse(userProfile);
    }

    // ================= LOCATION =================

    @Transactional
    @Override
    public void updateLocation(UserProfileUpdateLocationRequest request, Long userId) {

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile not found for userId: " + userId)
                );

        if (userProfile.getRole() != Role.RESTAURANT) {
            throw new BadRequestException("Location update is allowed only for restaurants");
        }

        userProfile.setLatitude(request.getLatitude());
        userProfile.setLongitude(request.getLongitude());

        userProfileRepository.save(userProfile);
    }

    // ================= PROFILE IMAGE =================

    @Transactional
    @Override
    public void updateProfileImage(UserProfileImageRequest picRequest, Long userId) {

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile not found for userId: " + userId)
                );

        String imageUrl = uploadFile(picRequest.getImage());
        userProfile.setProfileImageUrl(imageUrl);

        userProfileRepository.save(userProfile);
    }

    private String uploadFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));

        String key = "profiles/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return "https://" + bucketName + ".s3.amazonaws.com/" + key;

        } catch (IOException e) {
            log.error("S3 upload failed", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload file",
                    e
            );
        }
    }

    // ================= PROFILE PICTURE =================

    @Override
    public UserProfilePictureResponse getProfilePicture(Long userId) {

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile not found for userId: " + userId)
                );

        return UserProfilePictureResponse.builder()
                .profileImageUrl(userProfile.getProfileImageUrl())
                .build();
    }
}
