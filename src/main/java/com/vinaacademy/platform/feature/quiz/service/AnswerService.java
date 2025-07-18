package com.vinaacademy.platform.feature.quiz.service;

import com.vinaacademy.platform.feature.quiz.dto.AnswerDto;

import java.util.UUID;

public interface AnswerService {
    /**
     * Create an answer for a question
     */
    AnswerDto createAnswer(UUID questionId, AnswerDto answerDto);

    /**
     * Update an existing answer
     */
    AnswerDto updateAnswer(UUID answerId, AnswerDto answerDto);

    /**
     * Delete an answer
     */
    void deleteAnswer(UUID answerId);
}
