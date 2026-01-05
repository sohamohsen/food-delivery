package com.fooddelivery.userservice.dto.mapper;

import com.fooddelivery.userservice.dto.request.UserProfileUpsertAddressRequest;
import com.fooddelivery.userservice.dto.response.UserProfileAddressResponse;
import com.fooddelivery.userservice.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserProfileAddressMapper {

    UserProfileAddressResponse toAddressResponse(UserProfile userProfile);

    // This creates a NEW entity (use for first-time setup if needed)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    UserProfile toEntity(UserProfileUpsertAddressRequest request);

    // This UPDATES an existing entity (use for your Service logic)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateAddressFromDto(UserProfileUpsertAddressRequest dto, @MappingTarget UserProfile entity);
}