package com.fooddelivery.userservice.entity;

import com.fooddelivery.userservice.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

    /* ========= Identity ========= */
    @Id
    private Long userId;

    /* ========= Basic Info ========= */
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(nullable = false)
    private String email;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    /* ========= Address ========= */
    private String country;
    private String city;
    private String area;
    private String street;
    private String buildingNumber;
    private String apartmentNumber;

    // for delivery & nearest restaurants
    private Double latitude;
    private Double longitude;

    /* ========= Profile ========= */
    private String profileImageUrl;

    /* ========= Role (duplicated intentionally) ========= */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    // read-only – source of truth: Auth Service

    /* ========= Account Status ========= */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    /* ========= Audit ========= */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void ensureUpdateIsAllowed() {
        if (this.status != UserStatus.ACTIVE) {
            throw new BadRequestException("Cannot update an inactive profile");
        }
    }
}
