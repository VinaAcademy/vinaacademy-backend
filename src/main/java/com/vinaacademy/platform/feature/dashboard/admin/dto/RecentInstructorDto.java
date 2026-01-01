package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho giảng viên mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentInstructorDto {
    
    /**
     * ID người dùng
     */
    private String userId;
    
    /**
     * Tên giảng viên
     */
    private String name;
    
    /**
     * Chữ cái đầu tên (VD: "NT", "HL")
     */
    private String initials;
    
    /**
     * Chuyên môn
     */
    private String expertise;
    
    /**
     * Thời gian gia nhập
     */
    private LocalDateTime joinedAt;
}
