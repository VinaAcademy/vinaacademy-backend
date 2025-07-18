package com.vinaacademy.platform.feature.enrollment.dto;

import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentProgressDto {
    private Long id;
    private Double progressPercentage = 0.0;
    private ProgressStatus status = ProgressStatus.IN_PROGRESS;
    private LocalDateTime startAt;
    private LocalDateTime completeAt;
}
