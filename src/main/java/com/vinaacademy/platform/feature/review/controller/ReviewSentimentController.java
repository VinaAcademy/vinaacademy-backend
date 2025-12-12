package com.vinaacademy.platform.feature.review.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.course.service.CourseManagementService;
import com.vinaacademy.platform.feature.review.dto.sentiment.*;
import com.vinaacademy.platform.feature.review.entity.ReviewModerationFlag;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import com.vinaacademy.platform.feature.review.repository.ReviewModerationFlagRepository;
import com.vinaacademy.platform.feature.review.service.ReviewSentimentQueryService;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Controller cho các endpoint phân tích cảm xúc
 * Cung cấp API cho Sinh viên, Giảng viên và Admin
 */
@RestController
@RequestMapping("/api/v1/reviews/sentiment")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Review Sentiment API", description = "API phân tích cảm xúc đánh giá khóa học")
public class ReviewSentimentController {
    
    private final ReviewSentimentQueryService queryService;
    private final ReviewModerationFlagRepository flagRepository;
    private final SecurityHelper securityHelper;
    private final CourseManagementService courseManagementService;
    
    // ========== API CHO SINH VIÊN ==========
    
    @Operation(summary = "Lấy tóm tắt điểm cộng/điểm trừ của khóa học")
    @GetMapping("/course/{courseId}/pros-cons")
    public ResponseEntity<ApiResponse<ProsConsResponse>> getProsConsSummary(
        @PathVariable UUID courseId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.debug("Lấy tóm tắt pros/cons cho khóa học: {}", courseId);
        
        ProsConsResponse response = queryService.getProsConsSummary(courseId, limit);
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy tóm tắt pros/cons thành công",
            response
        ));
    }
    
    @Operation(summary = "Lấy danh sách đánh giá theo cảm xúc")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<Page<ReviewWithSentimentDto>>> getReviewsBySentiment(
        @PathVariable UUID courseId,
        @RequestParam(required = false) SentimentType sentiment,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("Lấy danh sách đánh giá cho khóa học {} với bộ lọc cảm xúc: {}", courseId, sentiment);
        
        Page<ReviewWithSentimentDto> reviews = queryService.getReviewsBySentiment(
            courseId,
            sentiment,
            pageable
        );
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy danh sách đánh giá thành công",
            reviews
        ));
    }
    
    // ========== API CHO GIẢNG VIÊN ==========
    
    @Operation(summary = "Lấy dashboard phân tích sentiment cho giảng viên")
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE})
    @GetMapping("/course/{courseId}/dashboard")
    public ResponseEntity<ApiResponse<SentimentDashboardResponse>> getInstructorDashboard(
        @PathVariable UUID courseId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.debug("Lấy dashboard giảng viên cho khóa học: {}", courseId);

        // Thêm kiểm tra ownership trước khi trả về dữ liệu
        UUID instructorId = securityHelper.getCurrentUser().getId();
        if (!courseManagementService.isInstructorOfCourse(courseId, instructorId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập dashboard của khóa học này");
        }
        
        SentimentDashboardResponse dashboard = queryService.getInstructorDashboard(
            courseId,
            startDate,
            endDate
        );
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy dashboard thành công",
            dashboard
        ));
    }
    
    // ========== API CHO ADMIN ==========
    
    @Operation(summary = "Lấy danh sách đánh giá cần kiểm duyệt")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/admin/flagged")
    public ResponseEntity<ApiResponse<Page<FlaggedReviewDto>>> getFlaggedReviews(
        @RequestParam(required = false) ModerationStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        log.debug("Lấy danh sách đánh giá bị gắn cờ với trạng thái: {}", status);
        
        Page<FlaggedReviewDto> flaggedReviews = queryService.getFlaggedReviews(status, pageable);
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy danh sách đánh giá cần kiểm duyệt thành công",
            flaggedReviews
        ));
    }
    
    @Operation(summary = "Xử lý đánh giá bị flag (approve/reject)")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @PostMapping("/admin/flags/{flagId}/moderate")
    public ResponseEntity<ApiResponse<Void>> moderateFlag(
        @PathVariable Long flagId,
        @Valid @RequestBody ModerationActionRequest request
    ) {
        log.info("Kiểm duyệt cờ {} với hành động: {}", flagId, request.getAction());
        
        ReviewModerationFlag flag = flagRepository.findById(flagId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy cờ: " + flagId));
        
        UUID moderatorId = securityHelper.getCurrentUser().getId();
        
        if ("approve".equalsIgnoreCase(request.getAction())) {
            flag.approve(moderatorId, request.getNotes());
            
            // Nếu được yêu cầu, cũng xóa/ẩn đánh giá
            if (Boolean.TRUE.equals(request.getDeleteReview())) {
                // TODO: Triển khai chức năng ẩn/xóa đánh giá
                log.info("Đánh giá {} cần được xóa/ẩn", flag.getReview().getId());
            }
        } else if ("reject".equalsIgnoreCase(request.getAction())) {
            flag.reject(moderatorId, request.getNotes());
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                "error",
                "Hành động không hợp lệ. Phải là 'approve' hoặc 'reject'",
                null
            ));
        }
        
        flagRepository.save(flag);
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Xử lý kiểm duyệt thành công",
            null
        ));
    }
    
    @Operation(summary = "Lấy thống kê kiểm duyệt tổng quan")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/admin/moderation-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getModerationStatistics() {
        log.debug("Lấy thống kê kiểm duyệt");
        
        Map<String, Object> stats = queryService.getModerationStatistics();
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy thống kê thành công",
            stats
        ));
    }
    
    @Operation(summary = "Lấy danh sách đánh giá toxic cần ưu tiên xử lý")
    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/admin/flagged/critical")
    public ResponseEntity<ApiResponse<Page<FlaggedReviewDto>>> getCriticalFlags(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        log.debug("Lấy danh sách đánh giá bị gắn cờ quan trọng");
        
        Page<FlaggedReviewDto> criticalFlags = queryService.getFlaggedReviews(
            ModerationStatus.PENDING,
            pageable
        );
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lấy danh sách đánh giá cần ưu tiên xử lý thành công",
            criticalFlags
        ));
    }
}
