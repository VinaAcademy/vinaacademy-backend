package com.vinaacademy.platform.feature.category.dto;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto extends BaseDto {
    private Long id;
    private String name;
    private String slug;
    private String parentSlug;
    private long coursesCount;

    List<CategoryDto> children;
}
