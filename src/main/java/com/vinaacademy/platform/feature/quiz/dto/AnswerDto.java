package com.vinaacademy.platform.feature.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {
    private UUID id;
    private String answerText;
    // isCorrect is only included in responses for instructors and in result view
    private Boolean isCorrect;
}