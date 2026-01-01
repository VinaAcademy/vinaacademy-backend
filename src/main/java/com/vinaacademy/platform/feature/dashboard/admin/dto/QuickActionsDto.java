package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho các hành động nhanh
 * Hiển thị số lượng các items cần xử lý
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickActionsDto {
    
    /**
     * Số khóa học đang chờ phê duyệt
     */
    private Long pendingCourses;
    
    /**
     * Số yêu cầu rút tiền đang chờ
     */
    private Long pendingWithdrawals;
    
    /**
     * Số báo cáo vi phạm
     */
    private Long reportedViolations;
    
    /**
     * Số yêu cầu hỗ trợ chưa giải quyết
     */
    private Long pendingSupports;
}
