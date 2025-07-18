package com.vinaacademy.platform.feature.quiz.service.impl;

import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.quiz.dto.QuestionDto;
import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.mapper.QuizMapper;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import com.vinaacademy.platform.feature.quiz.service.AnswerService;
import com.vinaacademy.platform.feature.quiz.service.QuestionService;
import com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission;
import com.vinaacademy.platform.feature.user.constant.ResourceConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    private final AnswerService answerService;

    @Override
    @Transactional
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.CREATE)
    public QuestionDto createQuestion(UUID quizId, QuestionDto questionDto) {
        Quiz quiz = findQuizById(quizId);

        Question question = Question.builder()
                .quiz(quiz)
                .questionText(questionDto.getQuestionText())
                .explanation(questionDto.getExplanation())
                .point(questionDto.getPoint() != null ? questionDto.getPoint() : 1.0)
                .questionType(questionDto.getQuestionType() != null ? questionDto.getQuestionType() : QuestionType.SINGLE_CHOICE)
                .build();

        Question savedQuestion = questionRepository.save(question);

        // Process associated answers if they exist
        if (questionDto.getAnswers() != null && !questionDto.getAnswers().isEmpty()) {
            questionDto.getAnswers().forEach(answerDto ->
                    answerService.createAnswer(savedQuestion.getId(), answerDto));
        }

        // Update quiz total points
        updateQuizTotalPoints(quiz);

        return questionRepository.findById(savedQuestion.getId())
                .map(QuizMapper.INSTANCE::questionToQuestionDto)
                .orElseThrow(() -> new NotFoundException("Question not found after creation"));
    }

    @Override
    @Transactional
    public QuestionDto updateQuestion(UUID questionId, QuestionDto questionDto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found with id: " + questionId));

        question.setQuestionText(questionDto.getQuestionText());
        question.setExplanation(questionDto.getExplanation());

        if (questionDto.getPoint() != null) {
            question.setPoint(questionDto.getPoint());
        }

        if (questionDto.getQuestionType() != null) {
            updateQuestionType(question, questionDto.getQuestionType());
        }

        Question savedQuestion = questionRepository.save(question);

        // Update quiz total points
        updateQuizTotalPoints(savedQuestion.getQuiz());

        return QuizMapper.INSTANCE.questionToQuestionDto(savedQuestion);
    }

    private void updateQuestionType(Question question, QuestionType questionType) {
        QuestionType oldType = question.getQuestionType();
        if (oldType == questionType) {
            return;
        }

        question.setQuestionType(questionType);

        // Xóa toàn bộ answer cũ bằng cách thao tác trực tiếp trên collection
        List<Answer> existingAnswers = question.getAnswers();
        boolean willClear = questionType == QuestionType.TRUE_FALSE ||
                questionType == QuestionType.TEXT;
        if (existingAnswers != null && willClear) {
            existingAnswers.clear();
        }

        if (questionType == QuestionType.TRUE_FALSE) {
            Answer trueAnswer = Answer.builder()
                    .answerText("Đúng")
                    .isCorrect(true)
                    .build();

            Answer falseAnswer = Answer.builder()
                    .answerText("Sai")
                    .isCorrect(false)
                    .build();

            question.addAnswer(trueAnswer); // sử dụng helper method đã có
            question.addAnswer(falseAnswer);
        }

        if (questionType == QuestionType.SINGLE_CHOICE) {
            assert question.getAnswers() != null;
            long correctCount = question.getAnswers().stream().filter(Answer::getIsCorrect).count();
            if (correctCount > 1) {
                throw new ValidationException("Câu hỏi loại single choice chỉ được có một đáp án đúng");
            }
        }
    }


    @Override
    @Transactional
    public void deleteQuestion(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found with id: " + questionId));

        Quiz quiz = question.getQuiz();
        questionRepository.delete(question);

        // Update quiz total points
        updateQuizTotalPoints(quiz);
    }

    /**
     * Helper method to update a quiz's total points based on its questions
     */
    private void updateQuizTotalPoints(Quiz quiz) {
        List<Question> questions = questionRepository.findByQuizOrderByCreatedDate(quiz);

        double totalPoints = questions.stream()
                .mapToDouble(Question::getPoint)
                .sum();

        quiz.setTotalPoints(totalPoints);
        quizRepository.save(quiz);
    }

    /**
     * Helper method to find a quiz by ID
     */
    private Quiz findQuizById(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quiz not found with id: " + id));
    }
}
