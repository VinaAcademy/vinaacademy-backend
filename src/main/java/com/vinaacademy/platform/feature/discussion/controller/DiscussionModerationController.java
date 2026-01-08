package com.vinaacademy.platform.feature.discussion.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.discussion.dto.moderation.DiscussionModerationActionRequest;
import com.vinaacademy.platform.feature.discussion.dto.moderation.DiscussionModerationStatisticsDto;
import com.vinaacademy.platform.feature.discussion.dto.moderation.FlaggedDiscussionDto;
import com.vinaacademy.platform.feature.discussion.service.DiscussionModerationService;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for discussion moderation
 * Provides API for Admin/Staff to manage flagged discussions
 */
@RestController
@RequestMapping("/api/v1/discussions/moderation")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Discussion Moderation API", description = "API kiểm duyệt bình luận")
public class DiscussionModerationController {
    
    private final DiscussionModerationService moderationService;
    
    @Operation(summary = "Lấy danh sách bình luận cần kiểm duyệt")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/flagged")
    public ResponseEntity<ApiResponse<Page<FlaggedDiscussionDto>>> getFlaggedDiscussions(
        @RequestParam(required = false) ModerationStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        log.debug("Lấy danh sách bình luận bị gắn cờ với trạng thái: {}", status);
        
        Page<FlaggedDiscussionDto> flaggedDiscussions = moderationService.getFlaggedDiscussions(status, pageable);
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy danh sách bình luận cần kiểm duyệt thành công",
            flaggedDiscussions
        ));
    }
    
    @Operation(summary = "Lấy lịch sử đã kiểm duyệt")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/flagged/history")
    public ResponseEntity<ApiResponse<Page<FlaggedDiscussionDto>>> getModerationHistory(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        log.debug("Lấy lịch sử kiểm duyệt bình luận");
        
        Page<FlaggedDiscussionDto> history = moderationService.getModerationHistory(pageable);
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy lịch sử kiểm duyệt thành công",
            history
        ));
    }
    
    @Operation(summary = "Xử lý bình luận bị flag (approve/reject)")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @PostMapping("/flags/{flagId}/moderate")
    public ResponseEntity<ApiResponse<Void>> moderateFlag(
        @PathVariable Long flagId,
        @Valid @RequestBody DiscussionModerationActionRequest request
    ) {
        log.info("Kiểm duyệt cờ {} với hành động: {}", flagId, request.getAction());
        
        if ("approve".equalsIgnoreCase(request.getAction())) {
            // APPROVE = Xác nhận vi phạm → ẨN bình luận và gửi thông báo
            boolean hideDiscussion = request.getHideDiscussion() != null ? request.getHideDiscussion() : true;
            moderationService.approveFlag(flagId, request.getNotes(), hideDiscussion);
            
            log.info("Đã chấp nhận cờ {} và xử lý bình luận", flagId);
            
        } else if ("reject".equalsIgnoreCase(request.getAction())) {
            // REJECT = Từ chối cờ, bình luận không vi phạm
            moderationService.rejectFlag(flagId, request.getNotes());
            
            log.info("Đã từ chối cờ {}, bình luận không vi phạm", flagId);
            
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                "error",
                "Hành động không hợp lệ. Phải là 'approve' hoặc 'reject'",
                null
            ));
        }
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Xử lý kiểm duyệt thành công",
            null
        ));
    }
    
    @Operation(summary = "Lấy thống kê kiểm duyệt tổng quan")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DiscussionModerationStatisticsDto>> getModerationStatistics() {
        log.debug("Lấy thống kê kiểm duyệt bình luận");
        
        DiscussionModerationStatisticsDto stats = moderationService.getModerationStatistics();
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy thống kê thành công",
            stats
        ));
    }
    
    @Operation(summary = "Lấy danh sách bình luận quan trọng cần ưu tiên xử lý")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/flagged/critical")
    public ResponseEntity<ApiResponse<Page<FlaggedDiscussionDto>>> getCriticalFlags(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        log.debug("Lấy danh sách bình luận bị gắn cờ quan trọng");
        
        Page<FlaggedDiscussionDto> criticalFlags = moderationService.getFlaggedDiscussions(
            ModerationStatus.PENDING,
            pageable
        );
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy danh sách bình luận cần ưu tiên xử lý thành công",
            criticalFlags
        ));
    }
}
