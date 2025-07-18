package com.vinaacademy.platform.feature.quiz.service.impl;

import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.quiz.dto.AnswerDto;
import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.mapper.QuizMapper;
import com.vinaacademy.platform.feature.quiz.repository.AnswerRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public AnswerDto createAnswer(UUID questionId, AnswerDto answerDto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found with id: " + questionId));

        // For single choice, ensure we don't have multiple correct answers
        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE ||
                question.getQuestionType() == QuestionType.TRUE_FALSE) {

            if (Boolean.TRUE.equals(answerDto.getIsCorrect())) {
                // Check if there's already a correct answer
                boolean hasCorrectAnswer = !answerRepository.findByQuestionIdAndIsCorrect(questionId, true).isEmpty();

                if (hasCorrectAnswer) {
                    throw new ValidationException("Single choice and true/false questions can only have one correct answer");
                }
            }
        }

        Answer answer = Answer.builder()
                .question(question)
                .answerText(answerDto.getAnswerText())
                .isCorrect(answerDto.getIsCorrect() != null ? answerDto.getIsCorrect() : false)
                .build();

        Answer savedAnswer = answerRepository.save(answer);
        return QuizMapper.INSTANCE.answerToAnswerDto(savedAnswer);
    }

    @Override
    @Transactional
    public AnswerDto updateAnswer(UUID answerId, AnswerDto answerDto) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found with id: " + answerId));

        Question question = answer.getQuestion();

        // Check if we're changing this to be a correct answer in single choice questions
        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE ||
                question.getQuestionType() == QuestionType.TRUE_FALSE) {

            if (Boolean.TRUE.equals(answerDto.getIsCorrect()) && !answer.getIsCorrect()) {
                // If we're making this the correct answer, remove correct flag from other answers
                answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true)
                        .forEach(a -> {
                            a.setIsCorrect(false);
                            answerRepository.save(a);
                        });
            }
        }

        answer.setAnswerText(answerDto.getAnswerText());

        if (answerDto.getIsCorrect() != null) {
            answer.setIsCorrect(answerDto.getIsCorrect());
        }

        Answer savedAnswer = answerRepository.save(answer);
        return QuizMapper.INSTANCE.answerToAnswerDto(savedAnswer);
    }

    @Override
    @Transactional
    public void deleteAnswer(UUID answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found with id: " + answerId));

        answerRepository.delete(answer);
    }
}
