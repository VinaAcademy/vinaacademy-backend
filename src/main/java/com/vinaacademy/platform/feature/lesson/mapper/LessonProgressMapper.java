package com.vinaacademy.platform.feature.lesson.mapper;

import com.vinaacademy.platform.feature.lesson.dto.LessonProgressDto;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LessonProgressMapper {
    LessonProgressMapper INSTANCE = Mappers.getMapper(LessonProgressMapper.class);

    @Mapping(target = "lessonId", source = "lesson.id")
    LessonProgressDto toDto(UserProgress userProgress);
}
