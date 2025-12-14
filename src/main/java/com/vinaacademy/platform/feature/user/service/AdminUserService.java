package com.vinaacademy.platform.feature.user.service;

import com.vinaacademy.platform.feature.user.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for admin user management operations
 */
public interface AdminUserService {
    
    /**
     * Get all users with filtering and pagination
     *
     * @param filterRequest the filter criteria
     * @param pageable pagination information
     * @return page of admin user DTOs
     */
    Page<AdminUserDto> getAllUsers(UserFilterRequest filterRequest, Pageable pageable);
    
    /**
     * Get user statistics for admin dashboard
     *
     * @return user statistics DTO
     */
    UserStatisticsDto getUserStatistics();
    
    /**
     * Get single user details for admin view
     *
     * @param userId the user ID
     * @return admin user DTO
     */
    AdminUserDto getUserById(UUID userId);
    
    /**
     * Create a new user
     *
     * @param request the create user request
     * @return created admin user DTO
     */
    AdminUserDto createUser(CreateUserRequest request);
    
    /**
     * Update user status (enable/disable)
     *
     * @param userId the user ID
     * @param request the status update request
     * @return updated admin user DTO
     */
    AdminUserDto updateUserStatus(UUID userId, UpdateUserStatusRequest request);
    
    /**
     * Update user roles
     *
     * @param userId the user ID
     * @param request the role update request
     * @return updated admin user DTO
     */
    AdminUserDto updateUserRoles(UUID userId, UpdateUserRoleRequest request);
    
    /**
     * Delete a user (soft delete by disabling)
     *
     * @param userId the user ID
     */
    void deleteUser(UUID userId);
    
    /**
     * Toggle collaborator status for a user
     *
     * @param userId the user ID
     * @param isCollaborator the new collaborator status
     * @return updated admin user DTO
     */
    AdminUserDto updateCollaboratorStatus(UUID userId, boolean isCollaborator);
}
