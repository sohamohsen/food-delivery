package com.fooddelivery.userservice.dto.mapper;

import com.fooddelivery.userservice.dto.request.UserProfileUpsertAddressRequest;
import com.fooddelivery.userservice.dto.request.UserProfileUpsertBasicDataRequest;
import com.fooddelivery.userservice.dto.response.UserProfileAddressResponse;
import com.fooddelivery.userservice.dto.response.UserProfileBasicResponse;
import com.fooddelivery.userservice.entity.Gender;
import com.fooddelivery.userservice.entity.UserProfile;
import com.fooddelivery.userservice.exception.BadRequestException;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserProfileMapper {

    // 1. Convert Entity to Response DTO
    // MapStruct handles Enum to String automatically using .name()
    UserProfileBasicResponse toBasicResponse(UserProfile userProfile);

    // 2. Convert Request DTO to Entity (for Creation)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "role", ignore = true) // Set manually from token in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "gender", source = "gender", qualifiedByName = "stringToGender")
    UserProfile toEntity(UserProfileUpsertBasicDataRequest request);

    // 3. Partial Update
    @Mapping(target = "gender", source = "gender", qualifiedByName = "stringToGender")
    @Mapping(target = "userId", ignore = true)   // Security: ID should never change
    @Mapping(target = "status", ignore = true)   // Security: Status changed via business logic
    @Mapping(target = "role", ignore = true)     // Security: Role changed via Auth service
    @Mapping(target = "email", ignore = true)    // Security: Email usually requires verification
    void updateEntityFromDto(UserProfileUpsertBasicDataRequest dto, @MappingTarget UserProfile entity);

    @Named("stringToGender")
    default Gender stringToGender(String gender) {
        if (gender == null) return null;
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid gender value: " + gender);
        }
    }

    UserProfileAddressResponse toAddressResponse(UserProfile userProfile);

    // This UPDATES an existing entity (use for your Service logic)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateAddressFromDto(UserProfileUpsertAddressRequest dto, @MappingTarget UserProfile entity);

}