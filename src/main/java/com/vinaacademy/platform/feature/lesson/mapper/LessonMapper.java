package com.vinaacademy.platform.feature.lesson.mapper;

import com.vinaacademy.platform.feature.course.enums.LessonType;
import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.reading.Reading;
import com.vinaacademy.platform.feature.video.entity.Video;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonMapper {

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionTitle", source = "section.title")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.fullName")
    @Mapping(target = "courseId", source = "section.course.id")
    @Mapping(target = "courseName", source = "section.course.name")
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "videoUrl", ignore = true)
    @Mapping(target = "videoDuration", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "passPoint", ignore = true)
    @Mapping(target = "totalPoint", ignore = true)
    @Mapping(target = "duration", ignore = true)
    LessonDto lessonToLessonDto(Lesson lesson);

    @AfterMapping
    default void mapSpecificFields(@MappingTarget LessonDto.LessonDtoBuilder<?, ?> builder, Lesson lesson) {
        if (lesson instanceof Video video) {
            builder.thumbnailUrl(video.getThumbnailUrl());
            builder.videoDuration(video.getDuration());
            builder.status(video.getStatus());
        } else if (lesson instanceof Reading reading) {
            builder.content(reading.getContent());
        } else if (lesson instanceof Quiz quiz) {
            builder.passPoint(quiz.getPassingScore());
            builder.totalPoint(quiz.getTotalPoints());
            builder.duration(quiz.getDuration());
        }
    }

    /**
     * Factory method to create the appropriate Lesson type based on the DTO
     * @param dto The LessonDto to convert
     * @return The concrete implementation of Lesson
     */
    default Lesson toEntity(LessonDto dto) {
        if (dto == null) {
            return null;
        }
        
        LessonType type = dto.getType();
        if (type == null) {
            // Default to Reading if type is not specified
            type = LessonType.READING;
        }
        
        switch (type) {
            case VIDEO:
                return createVideo(dto);
            case QUIZ:
                return createQuiz(dto);
            case READING:
            default:
                return createReading(dto);
        }
    }
    
    /**
     * Creates a Video entity from LessonDto
     */
    default Video createVideo(LessonDto dto) {
        Video video = Video.builder()
                .title(dto.getTitle())
                .free(dto.isFree())
                .orderIndex(dto.getOrderIndex())
                .thumbnailUrl(dto.getThumbnailUrl())
                .duration(dto.getVideoDuration())
                .status(dto.getStatus())
                .build();
        
        if (dto.getId() != null) {
            video.setId(dto.getId());
        }
        
        return video;
    }
    
    /**
     * Creates a Reading entity from LessonDto
     */
    default Reading createReading(LessonDto dto) {
        Reading reading = Reading.builder()
                .title(dto.getTitle())
                .free(dto.isFree())
                .orderIndex(dto.getOrderIndex())
                .content(dto.getContent())
                .build();
        
        if (dto.getId() != null) {
            reading.setId(dto.getId());
        }
        
        return reading;
    }
    
    /**
     * Creates a Quiz entity from LessonDto
     */
    default Quiz createQuiz(LessonDto dto) {
        Quiz quiz = Quiz.builder()
                .title(dto.getTitle())
                .free(dto.isFree())
                .orderIndex(dto.getOrderIndex())
                .passingScore(dto.getPassPoint())
                .totalPoints(dto.getTotalPoint())
                .duration(dto.getDuration())
                .build();
        
        if (dto.getId() != null) {
            quiz.setId(dto.getId());
        }
        
        return quiz;
    }
}
