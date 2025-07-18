package com.vinaacademy.platform.feature.instructor.dto;

import lombok.Data;

@Data
public class InstructorInfoDto {
    private String fullName;
    private String username;
    private String email;
    private String description;
    private String avatarUrl;
}