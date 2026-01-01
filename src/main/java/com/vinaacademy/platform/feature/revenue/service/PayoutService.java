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

	PayoutRequest createPayoutRequest(PayoutRequestDto request);
	PayoutRequest approvePayoutRequest(PayoutApprovalRequest approvalRequest);
	void processPayment(PayoutRequest payoutRequest);
	PayoutRequest cancelPayoutRequest(Long requestId);
	Page<PayoutRequest> getInstructorPayoutRequests(Pageable pageable);
	Page<PayoutRequest> getPendingPayoutRequests(Pageable pageable);
	Page<WalletTransaction> getWalletTransactions(UUID instructorId, Pageable pageable);
	Page<PayoutTransaction> getPayoutTransactions(UUID instructorId, Pageable pageable);
	WalletBalanceDto getWalletBalance();
	
	/**
	 * Đếm số lượng yêu cầu rút tiền đang chờ xử lý
	 * Dùng cho admin dashboard quick actions
	 */
	Long countPendingPayouts();
}
