package com.vinaacademy.platform.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for filtering users in admin panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    private String keyword; // Search in name, email, username
    private String role; // STUDENT, INSTRUCTOR, ADMIN, STAFF
    private String status; // ACTIVE, INACTIVE, LOCKED
    private LocalDate fromDate; // Registration date from
    private LocalDate toDate; // Registration date to
    private Boolean isCollaborator;
    private Boolean isEnabled;
}
