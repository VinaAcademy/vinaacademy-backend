package com.vinaacademy.platform.feature.quiz.dto;

import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
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
public class QuestionDto {
    private UUID id;
    private String questionText;
    private String explanation;
    private Double point;
    private QuestionType questionType;
    private List<AnswerDto> answers = new ArrayList<>();
}