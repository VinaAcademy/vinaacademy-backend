package com.vinaacademy.platform.feature.revenue.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vinaacademy.platform.feature.revenue.dto.CreateRevenueRecordRequest;
import com.vinaacademy.platform.feature.revenue.dto.RevenueDashboardDto;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;

public interface RevenueService {

	RevenueRecord createRevenueRecord(CreateRevenueRecordRequest request);
	void updateInstructorWallet(UUID instructorId, BigDecimal earning, Long revenueRecordId);
	void processRefund(String vnpayTxnRef, String refundReason);
	Page<RevenueRecord> getInstructorRevenue(Pageable pageable);
	RevenueDashboardDto getDashboardStats();
}
