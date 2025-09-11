package com.vinaacademy.platform.feature.discussion.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.discussion.dto.DiscussionDto;
import com.vinaacademy.platform.feature.discussion.dto.request.DiscussionRequest;
import com.vinaacademy.platform.feature.discussion.service.DiscussionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/discussions")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;
    
    @PostMapping
    @Operation(summary = "Tạo bình luận thảo luận")
    public ApiResponse<DiscussionDto> createDiscussion(@RequestBody @Valid DiscussionRequest request) {
        return ApiResponse.success(discussionService.createDiscussion(request));
    }
    
    @GetMapping("/{lessonId}")
    @Operation(summary = "Lấy danh sách Root các thảo luận")
    public ApiResponse<Page<DiscussionDto>> getRootComments(
            @PathVariable UUID lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.success(discussionService.getRootCommentsWithReplyCount(lessonId, pageable));
    }

    @GetMapping("/{parentId}/replies")
    @Operation(summary = "Lấy danh sách các phản hồi của thảo luận")
    public ApiResponse<Page<DiscussionDto>> getReplies(
            @PathVariable UUID parentId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ApiResponse.success(discussionService.getRepliesWithReplyCount(parentId, pageable));
    }
    
    @PostMapping("/delete/{discussionId}")
    @Operation(summary = "Xóa bình luận thảo luận")
    public ApiResponse<String> deleteDiscussion(@PathVariable UUID lessonId) {
    	discussionService.deleteDiscussion(lessonId);
        return ApiResponse.success("Xóa thành công thảo luận " + lessonId);
    }
}
