package com.vinaacademy.platform.feature.quiz.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.course.enums.LessonType;
import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.service.LessonService;
import com.vinaacademy.platform.feature.quiz.dto.*;
import com.vinaacademy.platform.feature.quiz.service.AnswerService;
import com.vinaacademy.platform.feature.quiz.service.QuestionService;
import com.vinaacademy.platform.feature.quiz.service.QuizService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructor/quiz")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Instructor Quiz", description = "Instructor quiz management APIs")
public class QuizInstructorController {
    private final QuizService quizService;
    private final AnswerService answerService;
    private final QuestionService questionService;
    private final LessonService lessonService;

    @Operation(summary = "Create a new quiz as a lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Successfully created quiz lesson",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LessonDto> createQuizLesson(@RequestBody @Valid QuizCreateRequest request) {
        // Ensure the lesson type is set to QUIZ
        request.setType(LessonType.QUIZ);

        // First create the lesson/quiz entity
        LessonDto lessonDto = lessonService.createLesson(request);

        // If questions are provided, add them to the quiz
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (QuestionDto questionDto : request.getQuestions()) {
                questionService.createQuestion(lessonDto.getId(), questionDto);
            }

            // Fetch the updated lesson with questions
            lessonDto = lessonService.getLessonById(lessonDto.getId());
        }

        return ApiResponse.success(lessonDto);
    }


    @Operation(summary = "Update a quiz lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated quiz lesson",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Quiz lesson not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/{id}")
    public ApiResponse<LessonDto> updateQuizLesson(@PathVariable UUID id, @RequestBody @Valid QuizCreateRequest request) {
        // Ensure the lesson type is set to QUIZ
        request.setType(LessonType.QUIZ);

        // Update the lesson/quiz entity
        LessonDto lessonDto = lessonService.updateLesson(id, request);

        return ApiResponse.success(lessonDto);
    }

    @Operation(summary = "Get quiz by ID for instructor view")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved quiz",
                    content = @Content(schema = @Schema(implementation = QuizDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Quiz not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/{id}")
    public ApiResponse<QuizDto> getQuiz(@PathVariable UUID id) {
        return ApiResponse.success(quizService.getQuizByIdForInstructor(id));
    }

    @Operation(summary = "Get all quizzes for a course")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved quizzes",
                    content = @Content(schema = @Schema(implementation = QuizDto.class))
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<QuizDto>> getQuizzesByCourse(@PathVariable UUID courseId) {
        return ApiResponse.success(quizService.getQuizzesByCourseId(courseId));
    }

    @Operation(summary = "Get all quizzes for a section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved quizzes",
                    content = @Content(schema = @Schema(implementation = QuizDto.class))
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @GetMapping("/section/{sectionId}")
    public ApiResponse<List<QuizDto>> getQuizzesBySection(@PathVariable UUID sectionId) {
        return ApiResponse.success(quizService.getQuizzesBySectionId(sectionId));
    }

    @Operation(summary = "Create a new question")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Successfully created question",
                    content = @Content(schema = @Schema(implementation = QuestionDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Quiz not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PostMapping("/{quizId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuestionDto> createQuestion(
            @PathVariable UUID quizId,
            @RequestBody @Valid QuestionDto questionDto) {
        return ApiResponse.success(questionService.createQuestion(quizId, questionDto));
    }

    @Operation(summary = "Update an existing question")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated question",
                    content = @Content(schema = @Schema(implementation = QuestionDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/questions/{questionId}")
    public ApiResponse<QuestionDto> updateQuestion(
            @PathVariable UUID questionId,
            @RequestBody @Valid QuestionDto questionDto) {
        return ApiResponse.success(questionService.updateQuestion(questionId, questionDto));
    }

    @Operation(summary = "Delete a question")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully deleted question"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @DeleteMapping("/questions/{questionId}")
    public ApiResponse<Void> deleteQuestion(@PathVariable UUID questionId) {
        questionService.deleteQuestion(questionId);
        return ApiResponse.success("Question deleted successfully");
    }

    @Operation(summary = "Create a new answer for a question")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Successfully created answer",
                    content = @Content(schema = @Schema(implementation = AnswerDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PostMapping("/questions/{questionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AnswerDto> createAnswer(
            @PathVariable UUID questionId,
            @RequestBody @Valid AnswerDto answerDto) {
        return ApiResponse.success(answerService.createAnswer(questionId, answerDto));
    }

    @Operation(summary = "Update an existing answer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated answer",
                    content = @Content(schema = @Schema(implementation = AnswerDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Answer not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/answers/{answerId}")
    public ApiResponse<AnswerDto> updateAnswer(
            @PathVariable UUID answerId,
            @RequestBody @Valid AnswerDto answerDto) {
        return ApiResponse.success(answerService.updateAnswer(answerId, answerDto));
    }

    @Operation(summary = "Delete an answer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully deleted answer"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Answer not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @DeleteMapping("/answers/{answerId}")
    public ApiResponse<Void> deleteAnswer(@PathVariable UUID answerId) {
        answerService.deleteAnswer(answerId);
        return ApiResponse.success("Answer deleted successfully");
    }

    @Operation(summary = "Get all student submissions for a quiz")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved submissions",
                    content = @Content(schema = @Schema(implementation = QuizSubmissionResultDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Quiz not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @GetMapping("/{quizId}/submissions")
    public ApiResponse<List<QuizSubmissionResultDto>> getQuizSubmissions(@PathVariable UUID quizId) {
        return ApiResponse.success(quizService.getQuizSubmissions(quizId));
    }
}
