package com.vinaacademy.platform.feature.category.service;

import com.vinaacademy.platform.feature.category.dto.CategoryDto;
import com.vinaacademy.platform.feature.category.dto.CategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories();

    CategoryDto getCategory(String slug);

    CategoryDto createCategory(CategoryRequest request);

    CategoryDto updateCategory(String slug, CategoryRequest request);

    void deleteCategory(String slug);
}
