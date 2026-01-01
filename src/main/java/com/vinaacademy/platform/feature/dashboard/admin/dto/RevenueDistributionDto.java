package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho phân bố doanh thu theo danh mục
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDistributionDto {
    
    /**
     * Tên danh mục
     */
    private String categoryName;
    
    /**
     * % doanh thu của danh mục
     */
    private Double percentage;
    
    /**
     * Số tiền doanh thu của danh mục
     */
    private BigDecimal amount;
}
