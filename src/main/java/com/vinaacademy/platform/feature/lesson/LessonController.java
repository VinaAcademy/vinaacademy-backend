package com.vinaacademy.platform.feature.lesson;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.dto.TTSRequestDto;
import com.vinaacademy.platform.feature.lesson.dto.TTSResponseDto;
import com.vinaacademy.platform.feature.lesson.service.LessonReorderService;
import com.vinaacademy.platform.feature.lesson.service.LessonService;
import com.vinaacademy.platform.feature.lesson.service.LessonTTSService;
import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
    @Autowired
    private LessonTTSService lessonTTSService;

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

    // ==================== ATTACHMENT ENDPOINTS ====================
    
    @Operation(summary = "Attach documents to lesson", 
               description = "Attach one or more document files to a lesson. Only DOCUMENT and OTHER file types are allowed.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully attached documents to lesson"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file IDs or file types"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Lesson or file not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PostMapping("/{lessonId}/attachments")
    public ApiResponse<Void> attachDocuments(
            @PathVariable UUID lessonId, 
            @RequestBody List<UUID> fileIds) {
        log.info("Attaching documents to lesson {}: {}", lessonId, fileIds);
        lessonService.attachDocuments(lessonId, fileIds);
        return ApiResponse.success("Documents attached successfully");
    }

    @Operation(summary = "Remove attachment from lesson",
               description = "Remove a specific document attachment from a lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully removed attachment"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Lesson or attachment not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @DeleteMapping("/{lessonId}/attachments/{fileId}")
    public ApiResponse<Void> removeAttachment(
            @PathVariable UUID lessonId,
            @PathVariable UUID fileId) {
        log.info("Removing attachment {} from lesson {}", fileId, lessonId);
        lessonService.removeAttachment(lessonId, fileId);
        return ApiResponse.success("Attachment removed successfully");
    }

    @Operation(summary = "Get all attachments of a lesson",
               description = "Retrieve all document attachments associated with a lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved attachments",
                    content = @Content(schema = @Schema(implementation = MediaFileDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Lesson not found"
            )
    })
    @GetMapping("/{lessonId}/attachments")
    public ApiResponse<List<MediaFileDto>> getAttachments(@PathVariable UUID lessonId) {
        log.debug("Getting attachments for lesson {}", lessonId);
        return ApiResponse.success(lessonService.getAttachments(lessonId));
    }

    @Operation(summary = "Get presigned download URL for attachment",
               description = "Generate a temporary presigned URL to download a specific lesson attachment. URL expires after 1 hour.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated presigned URL",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access - user must be enrolled in the course or be the instructor"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Attachment or lesson not found"
            )
    })
    @GetMapping("/{lessonId}/attachments/{attachmentId}/download-url")
    public ApiResponse<String> getAttachmentDownloadUrl(
            @PathVariable UUID lessonId,
            @PathVariable UUID attachmentId) {
        log.info("Generating presigned download URL for attachment {} in lesson {}", attachmentId, lessonId);
        String presignedUrl = lessonService.generateAttachmentDownloadUrl(lessonId, attachmentId);
        return ApiResponse.success(presignedUrl);
    }

    // ==================== Text-to-Speech Endpoints ====================

    @Operation(summary = "Generate Text-to-Speech audio for Reading lesson",
               description = "Convert Reading lesson content to audio using Azure Text-to-Speech service. " +
                             "Returns audio as base64 encoded string. Only works for Reading type lessons.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated audio",
                    content = @Content(schema = @Schema(implementation = TTSResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - not a Reading lesson or content too long"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized - user must be enrolled in the course or lesson must be free"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Lesson not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "TTS service unavailable"
            )
    })
    @PostMapping("/{lessonId}/tts")
    public ApiResponse<TTSResponseDto> generateLessonAudio(
            @PathVariable UUID lessonId,
            @RequestBody @Valid TTSRequestDto request) {
        log.info("Generating TTS audio for lesson: {} with voice: {}", lessonId, request.getVoice());
        TTSResponseDto response = lessonTTSService.generateAudioForLesson(lessonId, request);
        
        if ("error".equals(response.getStatus())) {
            return ApiResponse.error(response.getMessage());
        }
        
        return ApiResponse.success("Audio generated successfully", response);
    }

    @Operation(summary = "Stream Text-to-Speech audio for Reading lesson",
               description = "Stream audio directly instead of base64 encoding. " +
                             "Useful for large content to reduce memory usage. " +
                             "Returns audio/mpeg content type.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully streaming audio",
                    content = @Content(mediaType = "audio/mpeg")
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
                    description = "Lesson not found"
            )
    })
    @GetMapping("/{lessonId}/tts/stream")
    public ResponseEntity<StreamingResponseBody> streamLessonAudio(
            @PathVariable UUID lessonId,
            @RequestParam(defaultValue = "vi-VN-HoaiMyNeural") String voice) {
        log.info("Streaming TTS audio for lesson: {} with voice: {}", lessonId, voice);
        return lessonTTSService.streamAudioForLesson(lessonId, voice);
    }
}
