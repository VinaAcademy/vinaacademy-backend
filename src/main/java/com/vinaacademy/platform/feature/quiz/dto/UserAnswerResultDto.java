package com.vinaacademy.platform.feature.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerResultDto {
    private UUID questionId;
    private String questionText;
    private String explanation;
    private Double points;
    private Double earnedPoints;
    private Boolean isCorrect;
    private List<AnswerResultDto> answers = new ArrayList<>();
    private String textAnswer;
}