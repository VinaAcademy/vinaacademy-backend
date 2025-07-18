package com.vinaacademy.platform.feature.quiz.mapper;

import com.vinaacademy.platform.feature.quiz.dto.AnswerDto;
import com.vinaacademy.platform.feature.quiz.dto.QuestionDto;
import com.vinaacademy.platform.feature.quiz.dto.QuizDto;
import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuizMapper {
    QuizMapper INSTANCE = Mappers.getMapper(QuizMapper.class);

    // Quiz mapping
    QuizDto quizToQuizDto(Quiz quiz);
    
    // Setting hideCorrectAnswers = true will not expose the correct answers in student view
    @Mapping(target = "questions", source = "questions", qualifiedByName = "mapQuestionsWithoutCorrectAnswers")
    QuizDto quizToQuizDtoHideCorrectAnswers(Quiz quiz);
    
    // Question mapping
    QuestionDto questionToQuestionDto(Question question);
    
    // Answer mapping
    AnswerDto answerToAnswerDto(Answer answer);
    
    // Hide correct answers when mapping for student view
    @Named("mapQuestionsWithoutCorrectAnswers")
    default List<QuestionDto> mapQuestionsWithoutCorrectAnswers(List<Question> questions) {
        if (questions == null) {
            return null;
        }
        
        return questions.stream()
                .map(question -> {
                    QuestionDto dto = questionToQuestionDto(question);
                    
                    // Remove isCorrect flag from answers
                    if (dto.getAnswers() != null) {
                        dto.getAnswers().forEach(answerDto -> answerDto.setIsCorrect(null));
                    }
                    
                    return dto;
                })
                .toList();
    }
}