package com.vinaacademy.platform.feature.review.dto.sentiment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for moderation statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationStatisticsDto {
    
    private Long pending;           // Chờ xử lý
    private Long reviewed;          // Đã xem xét (legacy, có thể không dùng)
    private Long approved;          // Đã duyệt (xác nhận vi phạm, review bị ẩn)
    private Long rejected;          // Đã từ chối (không vi phạm, review giữ lại)
    private Long autoApproved;      // Tự động duyệt
    private Long totalFlags;        // Tổng số flags
    private Long criticalPending;   // Số flags ưu tiên cao (severity >= 4, PENDING)
}
