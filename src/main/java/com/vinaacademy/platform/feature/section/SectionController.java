package com.vinaacademy.platform.feature.section;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.dto.SectionRequest;
import com.vinaacademy.platform.feature.section.service.SectionService;
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
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sections", description = "Section management APIs")
public class SectionController {

    private final SectionService sectionService;

    @Operation(summary = "Get sections by course ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved sections",
                    content = @Content(schema = @Schema(implementation = SectionDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Course not found"
            )
    })
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<SectionDto>> getSectionsByCourse(@PathVariable UUID courseId) {
        return ApiResponse.success(sectionService.getSectionsByCourse(courseId));
    }

    @Operation(summary = "Get sections by course slug")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved sections",
                    content = @Content(schema = @Schema(implementation = SectionDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Course not found"
            )
    })
    @GetMapping("/course/slug/{courseSlug}")
    public ApiResponse<List<SectionDto>> getSectionsByCourseSlug(@PathVariable String courseSlug) {
        return ApiResponse.success(sectionService.getSectionsByCourseSlug(courseSlug));
    }

    @Operation(summary = "Get section by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved section",
                    content = @Content(schema = @Schema(implementation = SectionDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Section not found"
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<SectionDto> getSectionById(@PathVariable UUID id) {
        return ApiResponse.success(sectionService.getSectionById(id));
    }

    @Operation(summary = "Create new section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Successfully created section",
                    content = @Content(schema = @Schema(implementation = SectionDto.class))
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
    public ApiResponse<SectionDto> createSection(@RequestBody @Valid SectionRequest request) {
        return ApiResponse.success(sectionService.createSection(request));
    }

    @Operation(summary = "Update section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated section",
                    content = @Content(schema = @Schema(implementation = SectionDto.class))
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
                    description = "Section not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/{id}")
    public ApiResponse<SectionDto> updateSection(@PathVariable UUID id, @RequestBody @Valid SectionRequest request) {
        return ApiResponse.success(sectionService.updateSection(id, request));
    }

    @Operation(summary = "Delete section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully deleted section"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Section has lessons"
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
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSection(@PathVariable UUID id) {
        sectionService.deleteSection(id);
        return ApiResponse.success("Section deleted successfully");
    }

    @Operation(summary = "Reorder sections")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully reordered sections"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid section IDs"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Course not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/reorder/{courseId}")
    public ApiResponse<Void> reorderSections(@PathVariable UUID courseId, @RequestBody List<UUID> sectionIds) {
        sectionService.reorderSections(courseId, sectionIds);
        return ApiResponse.success("Sections reordered successfully");
    }

    @Operation(summary = "Reorder sections by course slug")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully reordered sections"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid section IDs"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Course not found"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PutMapping("/reorder/slug/{courseSlug}")
    public ApiResponse<Void> reorderSectionsBySlug(@PathVariable String courseSlug, @RequestBody List<UUID> sectionIds) {
        sectionService.reorderSectionsBySlug(courseSlug, sectionIds);
        return ApiResponse.success("Sections reordered successfully");
    }
}
