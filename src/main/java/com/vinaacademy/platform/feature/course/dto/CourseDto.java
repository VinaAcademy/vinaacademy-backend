package com.vinaacademy.platform.feature.course.dto;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentProgressDto;
import com.vinaacademy.platform.feature.section.dto.SectionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto extends BaseDto {

    private UUID id;

    private String image;

    private String name;

    private String description;

    private String slug;

    private BigDecimal price;

    private CourseLevel level;

    private CourseStatus status;

    private String language;

    private String categoryName;

    private double rating;

    private long totalRating;

    private long totalStudent;

    private long totalSection;

    private long totalLesson;

    private EnrollmentProgressDto progress;

    private List<SectionDto> sections;

}
