package com.vinaacademy.platform.feature.enrollment.mapper;

import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentProgressDto;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {
    EnrollmentMapper INSTANCE = Mappers.getMapper(EnrollmentMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.name", target = "courseName")
    @Mapping(source = "course.image", target = "courseImage")
    @Mapping(source = "course.totalLesson", target = "totalLessons")
    @Mapping(source = "course.category.name", target = "category")
    EnrollmentResponse toDto(Enrollment enrollment);
    
    EnrollmentProgressDto toDto2(Enrollment courseEnrollment);
}

