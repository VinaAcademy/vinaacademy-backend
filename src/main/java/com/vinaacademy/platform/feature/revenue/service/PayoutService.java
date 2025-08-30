package com.vinaacademy.platform.feature.revenue.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vinaacademy.platform.feature.revenue.dto.PayoutApprovalRequest;
import com.vinaacademy.platform.feature.revenue.dto.PayoutRequestDto;
import com.vinaacademy.platform.feature.revenue.dto.WalletBalanceDto;
import com.vinaacademy.platform.feature.revenue.entity.PayoutRequest;
import com.vinaacademy.platform.feature.revenue.entity.PayoutTransaction;
import com.vinaacademy.platform.feature.revenue.entity.WalletTransaction;

public interface PayoutService {

	PayoutRequest createPayoutRequest(UUID instructorId, PayoutRequestDto request);
	PayoutRequest approvePayoutRequest(PayoutApprovalRequest approvalRequest, UUID adminId);
	void processPayment(PayoutRequest payoutRequest, UUID adminId);
	PayoutRequest cancelPayoutRequest(Long requestId, UUID instructorId);
	Page<PayoutRequest> getInstructorPayoutRequests(UUID instructorId, Pageable pageable);
	Page<PayoutRequest> getPendingPayoutRequests(Pageable pageable);
	Page<WalletTransaction> getWalletTransactions(UUID instructorId, Pageable pageable);
	Page<PayoutTransaction> getPayoutTransactions(UUID instructorId, Pageable pageable);
	WalletBalanceDto getWalletBalance();
}
