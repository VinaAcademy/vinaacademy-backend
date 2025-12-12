package com.vinaacademy.platform.feature.instructor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho biểu đồ doanh thu theo thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDto {

    private List<RevenueDataPoint> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private String name;      // Tên thời điểm (T1, T2, ... hoặc Week 1, Week 2...)
        private BigDecimal revenue;  // Doanh thu
    }
}
