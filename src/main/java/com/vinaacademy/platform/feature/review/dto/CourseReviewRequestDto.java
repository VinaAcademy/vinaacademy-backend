package com.vinaacademy.platform.feature.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewRequestDto {
    @NotNull(message = "Course ID not null")
    private UUID courseId;

    @NotNull(message = "Rating not null")
    @Min(value = 1, message = "Rating 1 - 5")
    @Max(value = 5, message = "Rating 1 - 5")
    private Integer rating;

    private String review;
}
