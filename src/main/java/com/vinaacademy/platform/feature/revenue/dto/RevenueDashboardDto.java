package com.vinaacademy.platform.feature.revenue.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDashboardDto {
	
	private BigDecimal totalRevenue;
    private BigDecimal totalPlatformFee;
    private BigDecimal totalInstructorEarnings;
    private BigDecimal totalPendingPayouts;
    private BigDecimal totalCompletedPayouts;
    private Long pendingPayoutRequests;
    private Long totalInstructors;
    private Long totalTransactions;

}
