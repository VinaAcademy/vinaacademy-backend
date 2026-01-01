package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho doanh thu theo tháng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDto {
    
    /**
     * Tên tháng: "T1", "T2", ...
     */
    private String month;
    
    /**
     * Doanh thu trong tháng
     */
    private BigDecimal revenue;
    
    /**
     * Số khóa học bán được trong tháng
     */
    private Integer courses;
}
