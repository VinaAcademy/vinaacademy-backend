package com.vinaacademy.platform.feature.quiz.dto;

import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizCreateRequest extends LessonRequest {
    @Valid
    private List<QuestionDto> questions = new ArrayList<>();
}