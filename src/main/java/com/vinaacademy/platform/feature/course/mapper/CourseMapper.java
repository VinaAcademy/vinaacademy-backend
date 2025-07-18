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
    CourseDto toDTO(Course course);
    
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.slug", target = "categorySlug")
    @Mapping(target = "instructors", ignore = true)
    @Mapping(target = "ownerInstructor", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    CourseDetailsResponse toCourseDetailsResponse(Course course);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "courseReviews", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "instructors", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "id", ignore = true)
    Course toEntity(CourseRequest courseDto);
}
