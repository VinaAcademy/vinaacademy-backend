package com.vinaacademy.platform.feature.lesson.mapper;

import com.vinaacademy.platform.feature.lesson.dto.UserProgressDto;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProgressMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonTitle", source = "lesson.title")
    @Mapping(target = "lastUpdated", source = "updatedDate")
    UserProgressDto toDto(UserProgress userProgress);

    List<UserProgressDto> toDtoList(List<UserProgress> userProgresses);
}