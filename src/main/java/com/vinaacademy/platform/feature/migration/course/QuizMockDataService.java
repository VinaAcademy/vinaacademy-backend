package com.vinaacademy.platform.feature.migration.course;

import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.repository.AnswerRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizMockDataService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizRepository quizRepository;

    /**
     * Create quiz questions and answers for a quiz with Vietnamese content
     */
    public void createQuizQuestions(Quiz quiz, String courseName, String categoryName) {
        // Create multiple choice question
        Question multipleChoiceQuestion = Question.builder()
                .quiz(quiz)
                .questionText("Những thành phần chính nào được đề cập trong khóa học " + categoryName + " này? (Chọn tất cả đáp án đúng)")
                .explanation("Đây là những thành phần chính mà chúng tôi tập trung trong chương trình giảng dạy.")
                .point(25.0)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .build();

        questionRepository.save(multipleChoiceQuestion);

        // Add answers for multiple choice question
        Answer answer1 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Nền tảng lý thuyết và nguyên lý")
                .isCorrect(true)
                .build();

        Answer answer2 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Kỹ thuật thực hành và ứng dụng")
                .isCorrect(true)
                .build();

        Answer answer3 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Phương pháp giải quyết vấn đề nâng cao")
                .isCorrect(true)
                .build();

        Answer answer4 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Các chủ đề không liên quan không được đề cập trong khóa học này")
                .isCorrect(false)
                .build();

        answerRepository.save(answer1);
        answerRepository.save(answer2);
        answerRepository.save(answer3);
        answerRepository.save(answer4);

        // Create single choice question
        Question singleChoiceQuestion = Question.builder()
                .quiz(quiz)
                .questionText("Mục tiêu chính của khóa học " + courseName + " là gì?")
                .explanation("Hiểu rõ mục tiêu chính giúp định hướng kỳ vọng học tập của bạn.")
                .point(25.0)
                .questionType(QuestionType.SINGLE_CHOICE)
                .build();

        questionRepository.save(singleChoiceQuestion);

        // Add answers for single choice question
        Answer singleAnswer1 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Giảng dạy kỹ năng và kiến thức " + categoryName + " toàn diện")
                .isCorrect(true)
                .build();

        Answer singleAnswer2 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Chỉ tập trung vào khía cạnh lý thuyết mà không có ứng dụng thực tế")
                .isCorrect(false)
                .build();

        Answer singleAnswer3 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Cung cấp giải trí mà không có nội dung giáo dục")
                .isCorrect(false)
                .build();

        Answer singleAnswer4 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Giảng dạy các chủ đề không liên quan không được đề cập trong mô tả khóa học")
                .isCorrect(false)
                .build();

        answerRepository.save(singleAnswer1);
        answerRepository.save(singleAnswer2);
        answerRepository.save(singleAnswer3);
        answerRepository.save(singleAnswer4);

        // Create true/false question
        Question trueFalseQuestion = Question.builder()
                .quiz(quiz)
                .questionText("Khóa học này bao gồm cả kiến thức lý thuyết và ứng dụng thực tế.")
                .explanation("Đây là khía cạnh cơ bản trong phương pháp giảng dạy của chúng tôi.")
                .point(25.0)
                .questionType(QuestionType.TRUE_FALSE)
                .build();

        questionRepository.save(trueFalseQuestion);

        // Add answers for true/false question
        Answer trueAnswer = Answer.builder()
                .question(trueFalseQuestion)
                .answerText("Đúng")
                .isCorrect(true)
                .build();

        Answer falseAnswer = Answer.builder()
                .question(trueFalseQuestion)
                .answerText("Sai")
                .isCorrect(false)
                .build();

        answerRepository.save(trueAnswer);
        answerRepository.save(falseAnswer);

        // Create programming-related question if the category is related to programming
        if (categoryName.contains("lập trình") || categoryName.contains("Lập trình") ||
                categoryName.contains("ngôn ngữ") || categoryName.contains("IT") ||
                categoryName.contains("phần mềm") || categoryName.contains("web") ||
                categoryName.contains("CNTT") || categoryName.contains("phát triển")) {

            Question programmingQuestion = Question.builder()
                    .quiz(quiz)
                    .questionText("Đâu là một cách hiệu quả để kiểm tra lỗi trong quá trình phát triển phần mềm?")
                    .explanation("Kiểm tra lỗi và gỡ lỗi là kỹ năng quan trọng trong phát triển phần mềm.")
                    .point(25.0)
                    .questionType(QuestionType.SINGLE_CHOICE)
                    .build();

            questionRepository.save(programmingQuestion);

            Answer pAnswer1 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Kiểm thử đơn vị (Unit testing)")
                    .isCorrect(true)
                    .build();

            Answer pAnswer2 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Chỉ dựa vào đánh giá trực quan")
                    .isCorrect(false)
                    .build();

            Answer pAnswer3 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Không cần kiểm tra cho đến khi hoàn thành toàn bộ dự án")
                    .isCorrect(false)
                    .build();

            Answer pAnswer4 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Xóa code và viết lại mỗi khi gặp lỗi")
                    .isCorrect(false)
                    .build();

            answerRepository.save(pAnswer1);
            answerRepository.save(pAnswer2);
            answerRepository.save(pAnswer3);
            answerRepository.save(pAnswer4);

        }

        // Update quiz total points based on questions
        double totalPoints = 100.0; // All questions add up to 100 points
        quiz.setTotalPoints(totalPoints);
        quizRepository.save(quiz);
    }
}
