package com.vinaacademy.platform.feature.course.dto;

import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchRequest {
    private String keyword;
    private String categorySlug;
    @Size(max = 10, message = "Maximum of 10 categories allowed")
    private List<String> categorieSlugs;
    private UUID instructorId;
    private CourseLevel level;
    private String language;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private CourseStatus status;
}