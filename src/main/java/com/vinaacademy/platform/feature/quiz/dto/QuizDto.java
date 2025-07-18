package com.vinaacademy.platform.feature.quiz.dto;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto extends BaseDto {
    private UUID id;
    private String title;
    private String description;
    private double totalPoints;
    private int duration;
    private UUID sectionId;
    private String sectionTitle;
    
    // Quiz settings
    private boolean randomizeQuestions;
    private boolean showCorrectAnswers;
    private boolean allowRetake;
    private boolean requirePassingScore;
    private double passingScore;
    private int timeLimit;
    
    private List<QuestionDto> questions = new ArrayList<>();
}