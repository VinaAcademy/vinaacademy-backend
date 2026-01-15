package com.vinaacademy.platform.feature.course.mapper;


import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseRequest;
import com.vinaacademy.platform.feature.course.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseMapper INSTANCE = Mappers.getMapper(CourseMapper.class);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "nameInstructorOwner", ignore = true)
    @Mapping(target = "estimatedTime", expression = "java(course.getEstimatedTime())")
    CourseDto toDTO(Course course);
    
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.slug", target = "categorySlug")
    @Mapping(target = "instructors", ignore = true)
    @Mapping(target = "ownerInstructor", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "estimatedTime", expression = "java(course.getEstimatedTime())")
    CourseDetailsResponse toCourseDetailsResponse(Course course);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "courseReviews", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "instructors", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "estimatedTimeInfo", ignore = true)
    @Mapping(target = "id", ignore = true)
    Course toEntity(CourseRequest courseDto);

    /**
     * Helper method to extract owner instructor name from course
     * Should be called after mapping to set nameInstructorOwner field
     */
    default String extractOwnerInstructorName(Course course) {
        if (course == null || course.getInstructors() == null || course.getInstructors().isEmpty()) {
            return null;
        }
        return course.getInstructors().stream()
                .filter(ci -> ci != null && Boolean.TRUE.equals(ci.getIsOwner()))
                .map(ci -> ci.getInstructor() != null ? ci.getInstructor().getFullName() : null)
                .findFirst()
                .orElse(null);
    }
}
