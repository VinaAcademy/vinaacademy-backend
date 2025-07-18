package com.vinaacademy.platform.feature.quiz.service;

import com.vinaacademy.platform.feature.quiz.dto.QuestionDto;

import java.util.UUID;

public interface QuestionService {
    /**
     * Create a new question for a quiz
     */
    QuestionDto createQuestion(UUID quizId, QuestionDto questionDto);

    /**
     * Update an existing question
     */
    QuestionDto updateQuestion(UUID questionId, QuestionDto questionDto);

    /**
     * Delete a question
     */
    void deleteQuestion(UUID questionId);
}
