package com.vinaacademy.platform.feature.user.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthConstants {
    public static final String ADMIN_ROLE = "admin";
    public static final String STAFF_ROLE = "staff";
    public static final String STUDENT_ROLE = "student";
    public static final String INSTRUCTOR_ROLE = "instructor";

    public static final int ACTION_TOKEN_LENGTH = 32;
    public static final int ACTION_TOKEN_EXPIRED_HOURS = 1; // 1 hours
}
