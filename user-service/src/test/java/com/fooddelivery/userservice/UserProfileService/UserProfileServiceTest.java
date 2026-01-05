package com.fooddelivery.userservice.UserProfileService;

import com.fooddelivery.userservice.dto.mapper.UserProfileMapper;
import com.fooddelivery.userservice.dto.request.AuthenticatedUser;
import com.fooddelivery.userservice.dto.request.UserProfileUpsertBasicDataRequest;
import com.fooddelivery.userservice.dto.response.UserProfileBasicResponse;
import com.fooddelivery.userservice.entity.UserProfile;
import com.fooddelivery.userservice.entity.UserStatus;
import com.fooddelivery.userservice.repository.UserProfileRepository;
import com.fooddelivery.userservice.service.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository repository;

    @Mock
    private UserProfileMapper mapper;

    @InjectMocks
    private UserProfileServiceImpl service;

    private AuthenticatedUser authUser;
    private UserProfileUpsertBasicDataRequest request;

    @BeforeEach
    void setUp() {
        authUser = new AuthenticatedUser(1L, "test@email.com", "CUSTOMER");
        request = new UserProfileUpsertBasicDataRequest("John Doe", "123456789", LocalDate.parse("1990-01-01"), "MALE");
    }

    @Test
    void upsert_ShouldCreateProfile_WhenProfileDoesNotExist() {
        // Arrange
        UserProfile mockEntity = new UserProfile();
        UserProfileBasicResponse mockResponse = UserProfileBasicResponse.builder()
                .fullName(request.getFullName())
                .build();

        when(repository.findById(authUser.getUserId())).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(mockEntity);
        when(repository.save(any(UserProfile.class))).thenReturn(mockEntity);
        when(mapper.toBasicResponse(mockEntity)).thenReturn(mockResponse);

        // Act
        UserProfileBasicResponse result = service.upsertBasicData(authUser, request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getFullName(), result.getFullName());

        // Capture the entity to check if service enriched it correctly
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(repository).save(profileCaptor.capture());

        UserProfile savedProfile = profileCaptor.getValue();
        assertEquals(UserStatus.BASICDATA, savedProfile.getStatus());
        assertEquals(authUser.getUserId(), savedProfile.getUserId());
    }

    @Test
    void upsert_ShouldUpdateProfile_WhenProfileExistsAndIsActive() {
        // Arrange
        UserProfile existingProfile = UserProfile.builder()
                .userId(1L)
                .status(UserStatus.ACTIVE)
                .fullName("Old Name")
                .build();

        when(repository.findById(authUser.getUserId())).thenReturn(Optional.of(existingProfile));
        // We don't strictly need to mock updateEntityFromDto because it's void,
        // but we should verify it was called.
        when(repository.save(existingProfile)).thenReturn(existingProfile);
        when(mapper.toBasicResponse(existingProfile)).thenReturn(new UserProfileBasicResponse());

        // Act
        service.upsertBasicData(authUser, request);

        // Assert
        verify(mapper).updateEntityFromDto(request, existingProfile);
        verify(repository).save(existingProfile);
    }
}