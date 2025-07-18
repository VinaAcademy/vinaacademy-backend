package com.vinaacademy.platform.feature.section.dto;

import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionDto {
    private UUID id;
    private String title;
    private int orderIndex;
    private int lessonCount;
    private UUID courseId;
    private String courseName;
    private List<LessonDto> lessons;
}
