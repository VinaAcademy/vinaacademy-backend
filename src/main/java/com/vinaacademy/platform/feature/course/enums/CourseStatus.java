package com.vinaacademy.platform.feature.course.enums;

public enum CourseStatus {
    DRAFT("bản nháp"), PENDING("chờ duyệt"),
    PUBLISHED("đã duyệt"), REJECTED("bị từ chối");
    private final String value;

    CourseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
