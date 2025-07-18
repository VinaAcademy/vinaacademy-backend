package com.vinaacademy.platform.feature.lesson;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.lesson.dto.UserProgressDto;
import com.vinaacademy.platform.feature.lesson.service.UserProgressService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Progress", description = "User progress management APIs")
public class UserProgressController {

    private final UserProgressService userProgressService;

//    @Operation(summary = "Get progress by user for a course")
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Successfully retrieved user progress",
//                    content = @Content(schema = @Schema(implementation = UserProgressDto.class))
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "403",
//                    description = "Unauthorized access"
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "404",
//                    description = "User or course not found"
//            )
//    })
//    @GetMapping("/user/{userId}/course/{courseId}")
//    @PreAuthorize("isAuthenticated()")
//    public ApiResponse<List<UserProgressDto>> getProgressByUser(
//            @PathVariable UUID userId,
//            @PathVariable UUID courseId) {
//        return ApiResponse.success(userProgressService.getProgressByUser(userId, courseId));
//    }
//
//    @Operation(summary = "Get progress by course")
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Successfully retrieved course progress",
//                    content = @Content(schema = @Schema(implementation = UserProgressDto.class))
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "403",
//                    description = "Unauthorized access"
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "404",
//                    description = "Course not found"
//            )
//    })
//    @GetMapping("/course/{courseId}")
//    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
//    public ApiResponse<Page<UserProgressDto>> getProgressByCourse(
//            @PathVariable UUID courseId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size) {
//        return ApiResponse.success(userProgressService.getProgressByCourse(courseId, page, size));
//    }
//
//    @Operation(summary = "Get progress by lesson")
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Successfully retrieved lesson progress",
//                    content = @Content(schema = @Schema(implementation = UserProgressDto.class))
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "403",
//                    description = "Unauthorized access"
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "404",
//                    description = "Không tìm thấy bài học"
//            )
//    })
//    @GetMapping("/lesson/{lessonId}")
//    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
//    public ApiResponse<Page<UserProgressDto>> getProgressByLesson(
//            @PathVariable UUID lessonId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size) {
//        return ApiResponse.success(userProgressService.getProgressByLesson(lessonId, page, size));
//    }
//
//    @Operation(summary = "Update user progress")
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Successfully updated user progress",
//                    content = @Content(schema = @Schema(implementation = UserProgressDto.class))
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "403",
//                    description = "Unauthorized access"
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "404",
//                    description = "User or lesson not found"
//            )
//    })
//    @PutMapping("/user/{userId}/lesson/{lessonId}")
//    @PreAuthorize("isAuthenticated()")
//    public ApiResponse<UserProgressDto> updateProgress(
//            @PathVariable UUID userId,
//            @PathVariable UUID lessonId,
//            @RequestParam boolean completed,
//            @RequestParam(required = false) Long lastWatchedTime) {
//        return ApiResponse.success(userProgressService.updateProgress(userId, lessonId, completed, lastWatchedTime));
//    }
}