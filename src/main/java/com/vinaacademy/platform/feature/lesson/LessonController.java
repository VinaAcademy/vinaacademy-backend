package com.vinaacademy.platform.feature.lesson;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.service.LessonReorderService;
import com.vinaacademy.platform.feature.lesson.service.LessonService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lessons")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lessons", description = "Lesson management APIs")
public class LessonController {
    @Autowired
    private LessonService lessonService;
    @Autowired
    private LessonReorderService lessonReorderService;

    @Operation(summary = "Get lesson by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved lesson",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy bài học"
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<LessonDto> getLessonById(@PathVariable UUID id) {
        return ApiResponse.success(lessonService.getLessonById(id));
    }

    @Operation(summary = "Get lessons by section ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved lessons by section ID",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Section not found"
            )
    })
    @GetMapping("/section/{sectionId}")
    public ApiResponse<List<LessonDto>> getLessonsBySectionId(@PathVariable UUID sectionId) {
        return ApiResponse.success(lessonService.getLessonsBySectionId(sectionId));
    }

    @Operation(summary = "Create new lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully created lesson",
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
    public ApiResponse<LessonDto> createLesson(@RequestBody @Valid LessonRequest request) {
        return ApiResponse.success(lessonService.createLesson(request));
    }

    @Operation(summary = "Update lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated lesson",
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
                    description = "Không tìm thấy bài học"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/{id}")
    public ApiResponse<LessonDto> updateLesson(@PathVariable UUID id, @RequestBody @Valid LessonRequest request) {
        return ApiResponse.success(lessonService.updateLesson(id, request));
    }

    @Operation(summary = "Delete lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully deleted lesson"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy bài học"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLesson(@PathVariable UUID id) {
        lessonService.deleteLesson(id);
        return ApiResponse.success("Lesson deleted successfully");
    }

    @HasAnyRole({AuthConstants.STUDENT_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE})
    @PostMapping("/{lessonId}/complete")
    @Operation(summary = "Complete lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully completed lesson"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy bài học"
            )
    })
    public ApiResponse<Void> completeLesson(@PathVariable UUID lessonId) {
        log.info("Completing lesson with ID: {}", lessonId);
        lessonService.completeLesson(lessonId);
        return ApiResponse.success("Lesson completed successfully");
    }

    @Operation(summary = "Reorder lessons in a section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully reordered lessons"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid lesson IDs"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Section not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/reorder/{sectionId}")
    public ApiResponse<Void> reorderLessons(@PathVariable UUID sectionId, @RequestBody List<UUID> lessonIds) {
        lessonReorderService.reorderLessons(sectionId, lessonIds);
        return ApiResponse.success("Lessons reordered successfully");
    }
}
