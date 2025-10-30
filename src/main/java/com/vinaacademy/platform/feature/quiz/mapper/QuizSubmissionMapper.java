package com.vinaacademy.platform.feature.quiz.mapper;

import com.vinaacademy.platform.feature.quiz.dto.AnswerResultDto;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionResultDto;
import com.vinaacademy.platform.feature.quiz.dto.UserAnswerResultDto;
import com.vinaacademy.platform.feature.quiz.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuizSubmissionMapper {
    QuizSubmissionMapper INSTANCE = Mappers.getMapper(QuizSubmissionMapper.class);

    @Mapping(target = "quizId", source = "quizSession.quiz.id")
    @Mapping(target = "quizTitle", source = "quizSession.quiz.title")
    @Mapping(target = "answers", expression = "java(mapUserAnswers(submission))")
    QuizSubmissionResultDto toSubmissionResultDto(QuizSubmission submission);

    default List<UserAnswerResultDto> mapUserAnswers(QuizSubmission submission) {
        Quiz quiz = submission.getQuizSession().getQuiz();
        boolean showCorrectAnswers = quiz.isShowCorrectAnswers();

        return submission.getUserAnswers().stream()
                .map(userAnswer -> mapUserAnswer(userAnswer, showCorrectAnswers))
                .collect(Collectors.toList());
    }

    private UserAnswerResultDto mapUserAnswer(UserAnswer userAnswer, boolean showCorrectAnswers) {
        Question question = userAnswer.getQuestion();
        List<Answer> selectedAnswers = userAnswer.getSelectedAnswers() != null ? userAnswer.getSelectedAnswers() : List.of();

        List<AnswerResultDto> answerResults = question.getAnswers().stream()
                .map(answer -> mapAnswerResult(answer, selectedAnswers, showCorrectAnswers))
                .collect(Collectors.toList());

        return UserAnswerResultDto.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .explanation(showCorrectAnswers ? question.getExplanation() : null)
                .points(question.getPoint())
                .earnedPoints(userAnswer.getEarnedPoints())
                .isCorrect(userAnswer.isCorrect())
                .textAnswer(userAnswer.getTextAnswer())
                .answers(answerResults)
                .build();
    }

    private AnswerResultDto mapAnswerResult(Answer answer, List<Answer> selectedAnswers, boolean showCorrectAnswers) {
        return AnswerResultDto.builder()
                .id(answer.getId())
                .text(answer.getAnswerText())
                .isSelected(selectedAnswers.contains(answer))
                .isCorrect(showCorrectAnswers ? answer.getIsCorrect() : null)
                .build();
    }

}
