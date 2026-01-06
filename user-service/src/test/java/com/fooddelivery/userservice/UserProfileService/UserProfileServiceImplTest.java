package com.fooddelivery.userservice.UserProfileService;

import com.fooddelivery.userservice.dto.mapper.UserProfileMapper;
import com.fooddelivery.userservice.dto.request.*;
import com.fooddelivery.userservice.dto.response.UserProfileBasicResponse;
import com.fooddelivery.userservice.entity.Gender;
import com.fooddelivery.userservice.entity.Role;
import com.fooddelivery.userservice.entity.UserProfile;
import com.fooddelivery.userservice.entity.UserStatus;
import com.fooddelivery.userservice.exception.BadRequestException;
import com.fooddelivery.userservice.exception.ResourceNotFoundException;
import com.fooddelivery.userservice.repository.UserProfileRepository;
import com.fooddelivery.userservice.service.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {


    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper mapper;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private UserProfileServiceImpl service;

    private AuthenticatedUser authenticatedUser;
    private UserProfileUpsertBasicDataRequest basicRequest;
    private UserProfile userProfile;

    @BeforeEach
    void setup() {
        authenticatedUser = new AuthenticatedUser(
                1L,
                "test@mail.com",
                "RESTAURANT"
        );

        basicRequest = new UserProfileUpsertBasicDataRequest();
        basicRequest.setFullName("Soha Mohsen");
        basicRequest.setPhone("01000000000");
        basicRequest.setGender("FEMALE");

        userProfile = new UserProfile();
        userProfile.setUserId(1L);
        userProfile.setEmail("test@mail.com");
        userProfile.setRole(Role.RESTAURANT);
        userProfile.setStatus(UserStatus.BASICDATA);
    }

    // ================= BASIC DATA =================

    @Test
    void upsertBasicData_createNewProfile_success() {

        when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(mapper.toEntity(basicRequest)).thenReturn(userProfile);
        when(userProfileRepository.save(any())).thenReturn(userProfile);
        when(mapper.toBasicResponse(userProfile))
                .thenReturn(UserProfileBasicResponse.builder().fullName("Soha Mohsen").build());

        UserProfileBasicResponse response =
                service.upsertBasicData(authenticatedUser, basicRequest);

        assertNotNull(response);
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void upsertBasicData_updateExistingProfile_success() {
        userProfile.setFullName("Soha Mohsen");
        userProfile.setPhone("01000000000");
        userProfile.setGender(Gender.FEMALE);
        userProfile.setStatus(UserStatus.ACTIVE);
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);
        when(mapper.toBasicResponse(userProfile))
                .thenReturn(UserProfileBasicResponse.builder().fullName("Updated").build());

        UserProfileBasicResponse response =
                service.upsertBasicData(authenticatedUser, basicRequest);

        assertNotNull(response);
        verify(mapper).updateEntityFromDto(basicRequest, userProfile);
    }

    @Test
    void upsertBasicData_missingRequiredFields_throwException() {

        UserProfileUpsertBasicDataRequest invalidRequest =
                new UserProfileUpsertBasicDataRequest();

        when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> service.upsertBasicData(authenticatedUser, invalidRequest)
        );
    }

    // ================= ADDRESS =================

    @Test
    void updateAddress_profileNotFound_throwException() {

        when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateAddress(new UserProfileUpsertAddressRequest(), 1L)
        );
    }

    @Test
    void updateAddress_basicDataToActive_success() {

        userProfile.setRole(Role.ADMIN);
        userProfile.setStatus(UserStatus.BASICDATA);

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(userProfile));

        service.updateAddress(new UserProfileUpsertAddressRequest(), 1L);

        assertEquals(UserStatus.ACTIVE, userProfile.getStatus());
        verify(userProfileRepository).save(userProfile);
    }

    // ================= LOCATION =================

    @Test
    void updateLocation_notRestaurant_throwException() {

        userProfile.setRole(Role.ADMIN);
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(userProfile));

        assertThrows(
                BadRequestException.class,
                () -> service.updateLocation(new UserProfileUpdateLocationRequest(), 1L)
        );
    }

    @Test
    void updateLocation_restaurant_success() {

        userProfile.setRole(Role.RESTAURANT);
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(userProfile));

        UserProfileUpdateLocationRequest request = new UserProfileUpdateLocationRequest();
        request.setLatitude(30.0);
        request.setLongitude(31.0);

        service.updateLocation(request, 1L);

        assertEquals(30.0, userProfile.getLatitude());
        assertEquals(31.0, userProfile.getLongitude());
        verify(userProfileRepository).save(userProfile);
    }

    // ================= PROFILE IMAGE =================

    @Test
    void updateProfileImage_profileNotFound_throwException() {

        when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateProfileImage(new UserProfileImageRequest(), 1L)
        );
    }

    @Test
    void updateProfileImage_nullFile_throwException() {

        when(userProfileRepository.findById(1L))
                .thenReturn(Optional.of(userProfile));

        UserProfileImageRequest request = new UserProfileImageRequest();
        request.setImage(null);

        assertThrows(
                BadRequestException.class,
                () -> service.updateProfileImage(request, 1L)
        );
    }

}
