package com.vinaacademy.platform.feature.user;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.user.dto.*;
import com.vinaacademy.platform.feature.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controller for admin user management operations
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin User Management", description = "APIs for managing users (Admin only)")
@Slf4j
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    @GetMapping
    @Operation(summary = "Get all users with filtering and pagination", 
               description = "Retrieve paginated list of users with optional filters for role, status, date range, etc.")
    public ApiResponse<Page<AdminUserDto>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) Boolean isCollaborator,
            @RequestParam(required = false) Boolean isEnabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Admin requesting user list - page: {}, size: {}, filters: keyword={}, role={}, status={}", 
                 page, size, keyword, role, status);
        
        UserFilterRequest filterRequest = UserFilterRequest.builder()
                .keyword(keyword)
                .role(role)
                .status(status)
                .fromDate(fromDate)
                .toDate(toDate)
                .isCollaborator(isCollaborator)
                .isEnabled(isEnabled)
                .build();
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AdminUserDto> users = adminUserService.getAllUsers(filterRequest, pageable);
        
        return ApiResponse.success("Users retrieved successfully", users);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", 
               description = "Retrieve comprehensive statistics about users (counts by role, status, new users, etc.)")
    public ApiResponse<UserStatisticsDto> getUserStatistics() {
        log.info("Admin requesting user statistics");
        
        UserStatisticsDto statistics = adminUserService.getUserStatistics();
        
        return ApiResponse.success("User statistics retrieved successfully", statistics);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user details", 
               description = "Retrieve detailed information about a specific user")
    public ApiResponse<AdminUserDto> getUserById(@PathVariable UUID userId) {
        log.info("Admin requesting user details for ID: {}", userId);
        
        AdminUserDto user = adminUserService.getUserById(userId);
        
        return ApiResponse.success("User details retrieved successfully", user);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new user", 
               description = "Create a new user account with specified roles and permissions")
    public ApiResponse<AdminUserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Admin creating new user with username: {}", request.getUsername());
        
        AdminUserDto createdUser = adminUserService.createUser(request);
        
        return ApiResponse.success("User created successfully", createdUser);
    }
    
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", 
               description = "Enable or disable a user account")
    public ApiResponse<AdminUserDto> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        log.info("Admin updating status for user ID: {} to enabled={}", userId, request.getEnabled());
        
        AdminUserDto updatedUser = adminUserService.updateUserStatus(userId, request);
        
        return ApiResponse.success("User status updated successfully", updatedUser);
    }
    
    @PutMapping("/{userId}/roles")
    @Operation(summary = "Update user roles", 
               description = "Assign or modify roles for a user")
    public ApiResponse<AdminUserDto> updateUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        log.info("Admin updating roles for user ID: {} to {}", userId, request.getRoleCodes());
        
        AdminUserDto updatedUser = adminUserService.updateUserRoles(userId, request);
        
        return ApiResponse.success("User roles updated successfully", updatedUser);
    }
    
    @PutMapping("/{userId}/collaborator")
    @Operation(summary = "Toggle collaborator status", 
               description = "Enable or disable collaborator status for a user")
    public ApiResponse<AdminUserDto> updateCollaboratorStatus(
            @PathVariable UUID userId,
            @RequestParam boolean isCollaborator
    ) {
        log.info("Admin updating collaborator status for user ID: {} to {}", userId, isCollaborator);
        
        AdminUserDto updatedUser = adminUserService.updateCollaboratorStatus(userId, isCollaborator);
        
        return ApiResponse.success("Collaborator status updated successfully", updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", 
               description = "Soft delete a user by disabling their account")
    public ApiResponse<Void> deleteUser(@PathVariable UUID userId) {
        log.info("Admin deleting user ID: {}", userId);
        
        adminUserService.deleteUser(userId);
        
        return ApiResponse.success("User deleted successfully");
    }
    
    @PostMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user account", 
               description = "Unlock a locked user account and reset failed login attempts")
    public ApiResponse<AdminUserDto> unlockUser(@PathVariable UUID userId) {
        log.info("Admin unlocking user account ID: {}", userId);
        
        // Unlock by enabling and clearing lock time
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .enabled(true)
                .reason("Unlocked by admin")
                .build();
        
        AdminUserDto updatedUser = adminUserService.updateUserStatus(userId, request);
        
        return ApiResponse.success("User account unlocked successfully", updatedUser);
    }
}
