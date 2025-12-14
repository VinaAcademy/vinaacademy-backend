package com.vinaacademy.platform.feature.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating user roles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {
    
    @NotEmpty(message = "At least one role must be specified")
    private List<String> roleCodes; // e.g., ["ROLE_STUDENT", "ROLE_INSTRUCTOR"]
    
    private String reason; // Optional reason for role change
}
