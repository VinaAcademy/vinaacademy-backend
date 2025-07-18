package com.vinaacademy.platform.feature.instructor.mapper;

import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.dto.CourseInstructorDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CourseInstructorMapper {

    CourseInstructorMapper INSTANCE = Mappers.getMapper(CourseInstructorMapper.class);
    
    @Mapping(source = "instructor.id", target = "userId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "isOwner", target = "isOwner")
    CourseInstructorDto toDto (CourseInstructor courseInstructor);
    
    
}
