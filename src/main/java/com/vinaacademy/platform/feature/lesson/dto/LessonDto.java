package com.vinaacademy.platform.feature.lesson.dto;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import com.vinaacademy.platform.feature.course.enums.LessonType;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = false, of = {"id"})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto extends BaseDto {
    private UUID id;
    private String title;
    private String description;
    private LessonType type;
    private boolean free;
    private int orderIndex;
    private UUID sectionId;
    private String sectionTitle;
    private UUID authorId;
    private String authorName;
    private UUID courseId;
    private String courseName;

    // Fields specific to lesson types

    private UserProgress currentUserProgress;

    // For Video lessons
    private String thumbnailUrl;
    private VideoStatus status;
    private String videoUrl;
    private Double videoDuration;

    // For Reading lessons
    private String content;

    // For Quiz lessons
    private Double passPoint;
    private Double totalPoint;
    private Integer duration;
}
