package com.vinaacademy.platform.feature.enrollment.dto;

import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressDto {
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private UUID courseId;
    private String courseName;
    private Double progress;
    private ProgressStatus status;
}