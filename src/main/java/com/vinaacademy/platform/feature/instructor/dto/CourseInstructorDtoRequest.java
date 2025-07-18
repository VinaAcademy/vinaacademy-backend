package com.vinaacademy.platform.feature.instructor.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseInstructorDtoRequest {
    private UUID userId;
    private UUID courseId;
    private Boolean isOwner;
}
