package com.vinaacademy.platform.feature.course.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CourseModerationStatus {
    public static final int PENDING = 0; // Chờ duyệt
    public static final int APPROVED = 1; // Đã duyệt
    public static final int REJECTED = 2; // Bị từ chối
}
