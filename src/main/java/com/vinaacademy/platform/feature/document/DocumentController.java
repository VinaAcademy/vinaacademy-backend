package com.vinaacademy.platform.feature.document;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.document.service.DocumentService;
import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documents", description = "Document management APIs")
public class DocumentController {
    private final DocumentService documentService;

    @Operation(summary = "Upload document file", 
               description = "Upload a document (PDF, Word, Excel, etc.) to be used as lesson attachment. Returns MediaFile with type DOCUMENT.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully uploaded document",
                    content = @Content(schema = @Schema(implementation = MediaFileDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or file type"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaFileDto> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("Uploading document file: {}", file.getOriginalFilename());
        MediaFileDto uploadedDocument = documentService.uploadDocument(file);
        log.info("Document uploaded successfully with ID: {}", uploadedDocument.getId());
        return ApiResponse.success(uploadedDocument);
    }

    @Operation(summary = "View document details", 
               description = "Get document file information by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved document",
                    content = @Content(schema = @Schema(implementation = MediaFileDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document not found"
            )
    })
    @GetMapping("/{id}")
    public ApiResponse<MediaFileDto> viewDocument(@PathVariable UUID id) {
        log.debug("Viewing document with ID: {}", id);
        return ApiResponse.success(documentService.viewDocument(id));
    }

    @Operation(summary = "Get presigned download URL for document",
               description = "Generate a temporary presigned URL to download a document file. URL expires after 1 hour.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated presigned URL",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file type or file path not available"
            )
    })
    @GetMapping("/{id}/download-url")
    public ApiResponse<String> getDownloadUrl(@PathVariable UUID id) {
        log.info("Generating presigned download URL for document: {}", id);
        String presignedUrl = documentService.generateDownloadUrl(id);
        return ApiResponse.success(presignedUrl);
    }
}
