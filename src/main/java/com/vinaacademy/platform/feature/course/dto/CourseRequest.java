package com.vinaacademy.platform.feature.course.dto;


import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    private String image;

    @NotBlank(message = "Tên khóa học không được để trống")
    private String name;

    private String description;

    private String slug;

    @Min(value = 0, message = "Giá khóa học không được nhỏ hơn 0")
    private BigDecimal price;

    @NotNull(message = "Cấp độ khóa học không được để trống")
    private CourseLevel level;

    private CourseStatus status;

    @NotBlank(message = "Ngôn ngữ khóa học không được để trống")
    private String language;

    @NotBlank(message = "Danh mục khóa học không được để trống")
    private String categorySlug;


}