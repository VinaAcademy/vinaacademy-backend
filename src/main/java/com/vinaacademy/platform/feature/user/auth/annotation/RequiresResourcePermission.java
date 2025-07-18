package com.vinaacademy.platform.feature.user.auth.annotation;

import com.vinaacademy.platform.feature.user.constant.ResourceConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check if the current user can access a specific resource.
 * Use this annotation on methods that access resources requiring authorization checks.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresResourcePermission {
    
    /**
     * The type of resource being accessed (e.g., ResourceConstants.QUIZ, ResourceConstants.LESSON)
     */
    String resourceType();
    
    /**
     * The name of the method parameter that contains the resource ID.
     * If not specified, the first UUID parameter will be used.
     */
    String idParam() default "";
    
    /**
     * The permission required to access the resource (ResourceConstants.VIEW, ResourceConstants.EDIT, etc.)
     */
    String permission() default ResourceConstants.VIEW;
}