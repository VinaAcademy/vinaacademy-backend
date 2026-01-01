package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho tổng quan doanh thu
 * Bao gồm dữ liệu theo tháng và phân bố theo danh mục
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueOverviewDto {
    
    /**
     * Danh sách doanh thu theo tháng (12 tháng)
     */
    private List<MonthlyRevenueDto> monthlyRevenue;
    
    /**
     * Phân bố doanh thu theo danh mục
     */
    private List<RevenueDistributionDto> distribution;
    
    /**
     * Tổng doanh thu cả năm
     */
    private BigDecimal yearlyRevenue;
    
    /**
     * Phí nền tảng (20% tổng doanh thu)
     */
    private BigDecimal platformFee;
    
    /**
     * Thu nhập của giảng viên (80% tổng doanh thu)
     */
    private BigDecimal instructorEarnings;
    
    /**
     * Giá trị đơn hàng trung bình
     */
    private BigDecimal averageOrderValue;
}
