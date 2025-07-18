package com.vinaacademy.platform.feature.user.constant;

import lombok.experimental.UtilityClass;

/**
 * Constants for resource types and permissions used in authorization checks.
 */
@UtilityClass
public class ResourceConstants {

    // Resource Types
    public static final String LESSON = "lesson";
    public static final String COURSE = "course";
    public static final String SECTION = "section";

    // Permission Types
    public static final String VIEW_OWN = "view_own";
    public static final String VIEW = "view";
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";
    public static final String CREATE = "create";
}