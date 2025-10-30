package com.vinaacademy.platform.feature.quiz.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.quiz.dto.QuizDto;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionRequest;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionResultDto;
import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.QuizSession;
import com.vinaacademy.platform.feature.quiz.service.QuizCacheService;
import com.vinaacademy.platform.feature.quiz.service.student.QuizStudentService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Student Quiz", description = "Student quiz APIs")
public class QuizStudentController {
    private final QuizStudentService quizService;
    private final QuizCacheService quizCacheService;

    @Autowired
    private SecurityHelper securityHelper;

    @Operation(summary = "Get quiz for student")
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
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/{id}")
    public ApiResponse<QuizDto> getQuiz(@PathVariable UUID id) {
        return ApiResponse.success(quizService.getQuizForStudent(id));
    }

    @Operation(summary = "Start a quiz and record server start time")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully started quiz session",
                    content = @Content(schema = @Schema(implementation = QuizSession.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Quiz not found"
            )
    })
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @PostMapping("/{quizId}/start")
    public ApiResponse<QuizSession> startQuiz(@PathVariable UUID quizId) {
        return ApiResponse.success(quizService.startQuiz(quizId));
    }

    @Operation(summary = "Cache một câu trả lời trong quá trình làm bài")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully cached answer"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or no active session"
            )
    })
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @PostMapping("/{quizId}/cache-answer")
    public ApiResponse<Void> cacheAnswer(@PathVariable UUID quizId, @RequestBody UserAnswerRequest request) {
        quizService.cacheQuizAnswer(quizId, request);
        return ApiResponse.success("Answer cached successfully");
    }

    @Operation(summary = "Lấy câu trả lời đã cache của bài quiz")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved cached answers"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No cached answers found"
            )
    })
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/{quizId}/cached-answers")
    public ApiResponse<Map<String, UserAnswerRequest>> getCachedAnswers(@PathVariable UUID quizId,
                                                                        @RequestParam UUID sessionId) {
        User currentUser = securityHelper.getCurrentUser();
        Map<String, UserAnswerRequest> cachedAnswers = quizCacheService.getCachedUserAnswers(currentUser.getId(), sessionId, quizId);

        if (cachedAnswers == null || cachedAnswers.isEmpty()) {
            return ApiResponse.success(new HashMap<>());
        }

        return ApiResponse.success(cachedAnswers);
    }

    @Operation(summary = "Submit quiz answers")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Successfully submitted quiz",
                    content = @Content(schema = @Schema(implementation = QuizSubmissionResultDto.class))
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
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuizSubmissionResultDto> submitQuiz(@RequestBody @Valid QuizSubmissionRequest request) {
        return ApiResponse.success(quizService.submitQuiz(request));
    }

    @Operation(summary = "Get latest quiz submission")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved latest submission",
                    content = @Content(schema = @Schema(implementation = QuizSubmissionResultDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No submission found"
            )
    })
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/{quizId}/submission/latest")
    public ApiResponse<QuizSubmissionResultDto> getLatestSubmission(@PathVariable UUID quizId) {
        return ApiResponse.success(quizService.getLatestSubmission(quizId));
    }

    @Operation(summary = "Get all quiz submissions by current student")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved submission history",
                    content = @Content(schema = @Schema(implementation = QuizSubmissionResultDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No submissions found"
            )
    })
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/{quizId}/submissions")
    public ApiResponse<List<QuizSubmissionResultDto>> getSubmissionHistory(@PathVariable UUID quizId) {
        return ApiResponse.success(quizService.getSubmissionHistory(quizId));
    }
}
