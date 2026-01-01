package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho số liệu người dùng theo tháng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyUsersDto {
    
    /**
     * Tên tháng: "T1", "T2", ...
     */
    private String month;
    
    /**
     * Số người dùng hoạt động trong tháng
     */
    private Long activeUsers;
    
    /**
     * Số người dùng mới trong tháng
     */
    private Long newUsers;
}
