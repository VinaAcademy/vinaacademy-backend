package com.vinaacademy.platform.feature.category.service;


import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.category.dto.CategoryDto;
import com.vinaacademy.platform.feature.category.dto.CategoryRequest;
import com.vinaacademy.platform.feature.category.mapper.CategoryMapper;
import com.vinaacademy.platform.feature.category.repository.CategoryRepository;
import com.vinaacademy.platform.feature.category.utils.CategoryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryMediaFileServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private Category parentCategory;
    private CategoryDto categoryDto;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        // Setup parent category
        parentCategory = Category.builder()
                .id(1L)
                .name("Parent Category")
                .slug("parent-category")
                .build();

        // Setup category
        category = Category.builder()
                .id(2L)
                .name("Test Category")
                .slug("test-category")
                .parent(parentCategory)
                .build();

        // Setup category DTO
        categoryDto = new CategoryDto();
        categoryDto.setId(2L);
        categoryDto.setName("Test Category");
        categoryDto.setSlug("test-category");

        // Setup category request
        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Test Category");
        categoryRequest.setSlug("test-category");
        categoryRequest.setParentSlug("parent-category");
    }

    @Test
    void getCategories_ShouldReturnCategoryDtoList() {
        // Arrange
        List<Category> rootCategories = Arrays.asList(parentCategory);
        when(categoryRepository.findAllRootCategoriesWithChildren()).thenReturn(rootCategories);

        try (MockedStatic<CategoryUtils> categoryUtilsMockedStatic = mockStatic(CategoryUtils.class)) {
            categoryUtilsMockedStatic.when(() -> CategoryUtils.buildCategoryHierarchy(any(Category.class), any(CategoryMapper.class)))
                    .thenReturn(categoryDto);

            // Act
            List<CategoryDto> result = categoryService.getCategories();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(categoryDto);
            verify(categoryRepository).findAllRootCategoriesWithChildren();
            categoryUtilsMockedStatic.verify(() -> CategoryUtils.buildCategoryHierarchy(parentCategory, categoryMapper));
        }
    }

    @Test
    void getCategory_WithExistingSlug_ShouldReturnCategoryDto() {
        // Arrange
        String slug = "test-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));

        try (MockedStatic<CategoryUtils> categoryUtilsMockedStatic = mockStatic(CategoryUtils.class)) {
            categoryUtilsMockedStatic.when(() -> CategoryUtils.buildCategoryHierarchy(any(Category.class), any(CategoryMapper.class)))
                    .thenReturn(categoryDto);

            // Act
            CategoryDto result = categoryService.getCategory(slug);

            // Assert
            assertThat(result).isEqualTo(categoryDto);
            verify(categoryRepository).findBySlug(slug);
            categoryUtilsMockedStatic.verify(() -> CategoryUtils.buildCategoryHierarchy(category, categoryMapper));
        }
    }

    @Test
    void getCategory_WithNonExistingSlug_ShouldThrowException() {
        // Arrange
        String slug = "non-existing-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> categoryService.getCategory(slug));
        assertThat(exception.getMessage()).contains("Danh mục không tồn tại");
        verify(categoryRepository).findBySlug(slug);
    }

    @Test
    void createCategory_WithValidRequest_ShouldReturnCategoryDto() {
        // Arrange
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.findBySlug("parent-category")).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryDto);

        // Act
        CategoryDto result = categoryService.createCategory(categoryRequest);

        // Assert
        assertThat(result).isEqualTo(categoryDto);
        verify(categoryRepository).existsBySlug(categoryRequest.getSlug());
        verify(categoryRepository).findBySlug(categoryRequest.getParentSlug());
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toDto(any(Category.class));
    }

    @Test
    void createCategory_WithExistingSlug_ShouldThrowException() {
        // Arrange
        when(categoryRepository.existsBySlug(anyString())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.createCategory(categoryRequest));
        assertThat(exception.getMessage()).contains("Slug đã tồn tại");
        verify(categoryRepository).existsBySlug(categoryRequest.getSlug());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_WithNonExistingParent_ShouldThrowException() {
        // Arrange
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.findBySlug("parent-category")).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.createCategory(categoryRequest));
        assertThat(exception.getMessage()).contains("Danh mục cha không tồn tại");
        verify(categoryRepository).existsBySlug(categoryRequest.getSlug());
        verify(categoryRepository).findBySlug(categoryRequest.getParentSlug());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WithValidRequest_ShouldReturnCategoryDto() {
        // Arrange
        String slug = "test-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));
//        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.findBySlug("parent-category")).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryDto);

        try (MockedStatic<CategoryUtils> categoryUtilsMockedStatic = mockStatic(CategoryUtils.class)) {
            categoryUtilsMockedStatic.when(() -> CategoryUtils.isParent(any(Category.class), any(Category.class)))
                    .thenReturn(false);

            // Act
            CategoryDto result = categoryService.updateCategory(slug, categoryRequest);

            // Assert
            assertThat(result).isEqualTo(categoryDto);
            verify(categoryRepository).findBySlug(slug);
            verify(categoryRepository).findBySlug(categoryRequest.getParentSlug());
            verify(categoryRepository).save(category);
            verify(categoryMapper).toDto(category);
        }
    }

    @Test
    void updateCategory_WithNonExistingCategory_ShouldThrowException() {
        // Arrange
        String slug = "non-existing-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.updateCategory(slug, categoryRequest));
        assertThat(exception.getMessage()).contains("Danh mục không tồn tại");
        verify(categoryRepository).findBySlug(slug);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WithCircularHierarchy_ShouldThrowException() {
        // Arrange
        String slug = "test-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));
//        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.findBySlug("parent-category")).thenReturn(Optional.of(parentCategory));

        try (MockedStatic<CategoryUtils> categoryUtilsMockedStatic = mockStatic(CategoryUtils.class)) {
            categoryUtilsMockedStatic.when(() -> CategoryUtils.isParent(any(Category.class), any(Category.class)))
                    .thenReturn(true);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> categoryService.updateCategory(slug, categoryRequest));
            assertThat(exception.getMessage()).contains("Danh mục cha không hợp lệ");
            verify(categoryRepository).findBySlug(slug);
            verify(categoryRepository).findBySlug(categoryRequest.getParentSlug());
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Test
    void deleteCategory_WithExistingCategory_ShouldDeleteSuccessfully() {
        // Arrange
        String slug = "test-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParent(category)).thenReturn(false);
        when(categoryRepository.existsByCourses(category)).thenReturn(false);

        // Act
        categoryService.deleteCategory(slug);

        // Assert
        verify(categoryRepository).findBySlug(slug);
        verify(categoryRepository).existsByParent(category);
        verify(categoryRepository).existsByCourses(category);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_WithNonExistingCategory_ShouldThrowException() {
        // Arrange
        String slug = "non-existing-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.deleteCategory(slug));
        assertThat(exception.getMessage()).contains("Danh mục không tồn tại");
        verify(categoryRepository).findBySlug(slug);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_WithChildCategories_ShouldThrowException() {
        // Arrange
        String slug = "test-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParent(category)).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.deleteCategory(slug));
        assertThat(exception.getMessage()).contains("Danh mục có danh mục con");
        verify(categoryRepository).findBySlug(slug);
        verify(categoryRepository).existsByParent(category);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_WithCourses_ShouldThrowException() {
        // Arrange
        String slug = "test-category";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParent(category)).thenReturn(false);
        when(categoryRepository.existsByCourses(category)).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.deleteCategory(slug));
        assertThat(exception.getMessage()).contains("Danh mục có khóa học");
        verify(categoryRepository).findBySlug(slug);
        verify(categoryRepository).existsByParent(category);
        verify(categoryRepository).existsByCourses(category);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
