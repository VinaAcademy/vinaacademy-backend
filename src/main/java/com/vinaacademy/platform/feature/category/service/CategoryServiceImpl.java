package com.vinaacademy.platform.feature.category.service;

import com.vinaacademy.platform.configuration.cache.CacheConstants;
import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.category.dto.CategoryDto;
import com.vinaacademy.platform.feature.category.dto.CategoryRequest;
import com.vinaacademy.platform.feature.category.mapper.CategoryMapper;
import com.vinaacademy.platform.feature.category.repository.CategoryRepository;
import com.vinaacademy.platform.feature.category.utils.CategoryUtils;
import com.vinaacademy.platform.feature.common.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = CacheConstants.CATEGORIES)
    public List<CategoryDto> getCategories() {
        // Root categories
        List<Category> categories = categoryRepository.findAllRootCategoriesWithChildren();

        return categories.stream()
                .map(v -> CategoryUtils.buildCategoryHierarchy(v, categoryMapper))
                .toList();
    }

    @Override
    @Cacheable(value = CacheConstants.CATEGORY, key = "#slug")
    public CategoryDto getCategory(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BadRequestException.message("Danh mục không tồn tại"));

        return CategoryUtils.buildCategoryHierarchy(category, categoryMapper);
    }

    @Override
    @CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true)
    @CachePut(value = CacheConstants.CATEGORY, key = "#result.slug")
    public CategoryDto createCategory(CategoryRequest request) {
        String slug = StringUtils.isBlank(request.getSlug())
                ? request.getSlug() : SlugUtils.toSlug(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            throw BadRequestException.message("Slug đã tồn tại");
        }

        Category parent = null;
        if (StringUtils.isNotBlank(request.getParentSlug())) {
            parent = categoryRepository.findBySlug(request.getParentSlug())
                    .orElseThrow(() -> BadRequestException.message("Danh mục cha không tồn tại"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .parent(parent)
                .build();

        categoryRepository.save(category);

        return categoryMapper.toDto(category);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true),
                    @CacheEvict(value = CacheConstants.CATEGORY, key = "#slug")
            },
            put = {
                    @CachePut(value = CacheConstants.CATEGORY, key = "#slug")
            }
    )
    public CategoryDto updateCategory(String slug, CategoryRequest request) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BadRequestException.message("Danh mục không tồn tại"));

        String newSlug = StringUtils.isBlank(request.getSlug())
                ? request.getSlug() : SlugUtils.toSlug(request.getName());

        if (!slug.equals(newSlug) && categoryRepository.existsBySlug(newSlug)) {
            throw BadRequestException.message("Slug đã tồn tại");
        }

        Category parent = null;
        if (StringUtils.isNotBlank(request.getParentSlug())) {
            parent = categoryRepository.findBySlug(request.getParentSlug())
                    .orElseThrow(() -> BadRequestException.message("Danh mục cha không tồn tại"));

            // Check if parent is child of category
            if (CategoryUtils.isParent(category, parent)) {
                throw BadRequestException.message("Danh mục cha không hợp lệ");
            }
        }

        category.setName(request.getName());
        category.setSlug(newSlug);
        category.setParent(parent);

        categoryRepository.save(category);

        return categoryMapper.toDto(category);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true),
            @CacheEvict(value = CacheConstants.CATEGORY, key = "#slug")
    })
    public void deleteCategory(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BadRequestException.message("Danh mục không tồn tại"));
        if (categoryRepository.existsByParent(category)) {
            throw BadRequestException.message("Danh mục có danh mục con");
        }
        if (categoryRepository.existsByCourses(category)) {
            throw BadRequestException.message("Danh mục có khóa học");
        }

        categoryRepository.delete(category);
    }
}
