package com.vinaacademy.platform.feature.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vinaacademy.platform.feature.common.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for admin user management view
 * Contains additional information useful for administrators
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminUserDto extends BaseDto {
    private UUID id;
    private String fullName;
    private String email;
    private String username;
    private String phone;
    private String avatarUrl;
    private String description;
    @JsonProperty("isCollaborator")
    private boolean isCollaborator;
    private LocalDate birthday;
    
    // Role information
    private List<String> roles;
    private String primaryRole; // STUDENT, INSTRUCTOR, ADMIN
    
    // Status information
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("isEnabled")
    private boolean isEnabled;
    private LocalDateTime lockTime;
    private int failedAttempts;
    
    // Statistics
    private long enrollmentCount;
    private long createdCourseCount;
    private long completedCourseCount;
    
    // Activity tracking
    private LocalDateTime lastActive;
    private LocalDateTime joinDate;
}
