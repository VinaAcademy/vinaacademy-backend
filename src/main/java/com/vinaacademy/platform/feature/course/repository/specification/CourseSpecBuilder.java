package com.vinaacademy.platform.feature.course.repository.specification;

import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Builder for course specifications.
 * Centralizes specification building logic for reuse across different services.
 */
@UtilityClass
public class CourseSpecBuilder {

    /**
     * Builds specification for public course search (only published courses)
     *
     * @param searchRequest The search criteria
     * @return Specification for public course search
     */
    public static Specification<Course> buildPublicSearch(CourseSearchRequest searchRequest) {
        return Specification.where(CourseSpecification.hasKeyword(searchRequest.getKeyword()))
                .and(CourseSpecification.hasStatus(CourseStatus.PUBLISHED))
                .and(CourseSpecification.hasCategory(searchRequest.getCategorySlug()))
                .and(CourseSpecification.hasLevel(searchRequest.getLevel()))
                .and(CourseSpecification.hasLanguage(searchRequest.getLanguage()))
                .and(CourseSpecification.hasMinPrice(searchRequest.getMinPrice()))
                .and(CourseSpecification.hasMaxPrice(searchRequest.getMaxPrice()))
                .and(CourseSpecification.hasMinRating(searchRequest.getMinRating()))
                .and(CourseSpecification.hasInstructor(searchRequest.getInstructorId()));
    }

    /**
     * Builds specification for admin course search (all statuses except draft)
     *
     * @param searchRequest The search criteria
     * @return Specification for admin course search
     */
    public static Specification<Course> buildAdminSearch(CourseSearchRequest searchRequest) {
        return Specification.where(CourseSpecification.hasKeyword(searchRequest.getKeyword()))
                .and(CourseSpecification.hasStatus(
                        searchRequest.getStatus() != null ? searchRequest.getStatus() : null))
                .and(CourseSpecification.dontHasStatus(CourseStatus.DRAFT))
                .and(CourseSpecification.hasCategory(searchRequest.getCategorySlug()))
                .and(CourseSpecification.hasLevel(searchRequest.getLevel()))
                .and(CourseSpecification.hasLanguage(searchRequest.getLanguage()))
                .and(CourseSpecification.hasMinPrice(searchRequest.getMinPrice()))
                .and(CourseSpecification.hasMaxPrice(searchRequest.getMaxPrice()))
                .and(CourseSpecification.hasMinRating(searchRequest.getMinRating()));
    }

    /**
     * Builds specification for instructor course search (all instructor's courses)
     *
     * @param instructorId  The instructor ID
     * @param searchRequest The search criteria
     * @return Specification for instructor course search
     */
    public static Specification<Course> buildInstructorSearch(UUID instructorId, CourseSearchRequest searchRequest) {
        return Specification.where(CourseSpecification.hasInstructor(instructorId))
                .and(CourseSpecification.hasKeyword(searchRequest.getKeyword()))
                .and(CourseSpecification.hasStatus(searchRequest.getStatus()))
                .and(CourseSpecification.hasCategory(searchRequest.getCategorySlug()))
                .and(CourseSpecification.hasLevel(searchRequest.getLevel()))
                .and(CourseSpecification.hasLanguage(searchRequest.getLanguage()))
                .and(CourseSpecification.hasMinPrice(searchRequest.getMinPrice()))
                .and(CourseSpecification.hasMaxPrice(searchRequest.getMaxPrice()))
                .and(CourseSpecification.hasMinRating(searchRequest.getMinRating()));
    }

    /**
     * Builds specification for published courses by instructor
     *
     * @param instructorId The instructor ID
     * @return Specification for published instructor courses
     */
    public static Specification<Course> buildPublishedByInstructor(UUID instructorId) {
        return Specification.where(CourseSpecification.hasInstructor(instructorId))
                .and(CourseSpecification.hasStatus(CourseStatus.PUBLISHED));
    }

    /**
     * Builds specification for courses by category and rating
     *
     * @param categorySlug The category slug (optional)
     * @param minRating    The minimum rating (optional)
     * @return Specification for filtered courses
     */
    public static Specification<Course> buildCategoryAndRatingFilter(String categorySlug, Double minRating) {
        Specification<Course> spec = Specification.where(CourseSpecification.hasStatus(CourseStatus.PUBLISHED));

        if (categorySlug != null && !categorySlug.trim().isEmpty()) {
            spec = spec.and(CourseSpecification.hasCategory(categorySlug));
        }

        if (minRating != null && minRating > 0) {
            spec = spec.and(CourseSpecification.hasMinRating(minRating));
        }

        return spec;
    }
}
