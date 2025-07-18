package com.vinaacademy.platform.feature.lesson.dto;

import com.vinaacademy.platform.feature.course.enums.LessonType;
import com.vinaacademy.platform.feature.quiz.dto.QuestionDto;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    private String title;
    
    @NotNull(message = "Chương học không được để trống")
    private UUID sectionId;
    
    @NotNull(message = "Loại bài học không được để trống")
    private LessonType type;

    private String description;
    
    private boolean free;
    
    @Min(value = 0, message = "Thứ tự không được âm")
    private Integer orderIndex;
    
    // Fields specific to lesson types

    // For Video lessons
    private String videoUrl;
    private String thumbnailUrl;
    private VideoStatus status;
    private Double videoDuration;
    
    // For Reading lessons
    private String content;
    
    // For Quiz lessons
//    @Min(value = 0, message = "Điểm đạt không được âm")
    private Double passPoint;
    
//    @Min(value = 0, message = "Tổng điểm không được âm")
    private Double totalPoint;
    
//    @Min(value = 0, message = "Thời gian làm bài không được âm")
    private Integer duration;
    
    // Quiz settings - map stores settings like randomizeQuestions, showCorrectAnswers, etc.
    private Map<String, Object> settings;
    
    // List of questions for batch creation with quiz
    private List<QuestionDto> questions = new ArrayList<>();
}
