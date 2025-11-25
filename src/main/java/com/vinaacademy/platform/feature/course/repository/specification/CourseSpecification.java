package com.vinaacademy.platform.feature.course.repository.specification;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public static Specification<Course> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
        String containsLikePattern = getContainsLikePattern(keyword);

        Expression<String> unaccentName =
            criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("name")));
        Expression<String> unaccentDescription =
            criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("description")));

        return criteriaBuilder.or(
            criteriaBuilder.like(unaccentName, containsLikePattern),
            criteriaBuilder.like(unaccentDescription, containsLikePattern)
        );
        };
    }

    public static Specification<Course> hasStatus(CourseStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Course> dontHasStatus(CourseStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.notEqual(root.get("status"), status);
        };
    }

    public static Specification<Course> hasCategory(String categorySlug) {
        return (root, query, criteriaBuilder) -> {
            if (categorySlug == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("slug"), categorySlug);
        };
    }

    public static Specification<Course> hasCategories(List<String> categorySlugs) {
        if (categorySlugs != null && categorySlugs.size() > 50) {
            throw new IllegalArgumentException("Too many categories");
        }
        return (root, query, criteriaBuilder) -> {
            if (categorySlugs == null || categorySlugs.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            CriteriaBuilder.In<String> inClause =
                criteriaBuilder.in(root.get("category").get("slug"));
            for (String slug : categorySlugs) {
                inClause.value(slug);
            }
            return inClause;
        };
    }

    public static Specification<Course> hasLevel(CourseLevel level) {
        return (root, query, criteriaBuilder) -> {
            if (level == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("level"), level);
        };
    }

    public static Specification<Course> hasLanguage(String language) {
        return (root, query, criteriaBuilder) -> {
            if (language == null || language.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("language"), language);
        };
    }

    public static Specification<Course> hasMinPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Course> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Course> hasMinRating(Double minRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
        };
    }

    private static String getContainsLikePattern(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return "%";
        }
        return "%" + searchTerm.toLowerCase() + "%";
    }

    public static Specification<Course> hasInstructor(UUID instructorId) {
        if (instructorId == null) {
            return null;
        }

        return (root, query, cb) -> {
            Join<Course, CourseInstructor> instructorJoin = root.join("instructors", JoinType.INNER);
            return cb.equal(instructorJoin.get("instructor").get("id"), instructorId);
        };
    }
}