package com.vinaacademy.platform.feature.user.auth.service;

import com.vinaacademy.platform.feature.user.entity.User;

import java.util.UUID;

/**
 * Service responsible for authorization decisions.
 * Centralizes all authorization logic in one place.
 */
public interface AuthorizationService {

    /**
     * Checks if the current user has a specific role
     *
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    boolean hasRole(String role);

    /**
     * Checks if the current user has any of the specified roles
     *
     * @param roles The roles to check
     * @return true if the user has any of the roles, false otherwise
     */
    boolean hasAnyRole(String... roles);

    /**
     * Checks if the current user can modify a resource
     *
     * @param resourceAuthorId The ID of the resource author
     * @return true if the user is the author or an admin
     */
    boolean canModifyResource(UUID resourceAuthorId);

    /**
     * Checks if the current user has access to a lesson
     *
     * @param lessonId The ID of the lesson
     * @return true if the user has access to the lesson
     */
    boolean canAccessLesson(UUID lessonId);

    /**
     * Checks if the specified user has access to a lesson
     *
     * @param lessonId The ID of the lesson
     * @param user     The user to check access for
     * @return true if the user has access to the lesson
     */
    boolean canAccessLesson(UUID lessonId, User user);

    boolean canModifyLesson(UUID lessonId);

    boolean canModifyCourse(UUID courseId);

    boolean canAccessCourse(UUID courseId);

    boolean canAccessSection(UUID sectionId);

    boolean canModifySection(UUID sectionId);
}