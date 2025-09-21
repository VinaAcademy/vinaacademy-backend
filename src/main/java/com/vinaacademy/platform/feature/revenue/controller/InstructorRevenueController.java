package com.vinaacademy.platform.feature.revenue.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.revenue.dto.PayoutRequestDto;
import com.vinaacademy.platform.feature.revenue.dto.WalletBalanceDto;
import com.vinaacademy.platform.feature.revenue.entity.PayoutRequest;
import com.vinaacademy.platform.feature.revenue.entity.PayoutTransaction;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;
import com.vinaacademy.platform.feature.revenue.entity.WalletTransaction;
import com.vinaacademy.platform.feature.revenue.service.PayoutService;
import com.vinaacademy.platform.feature.revenue.service.RevenueService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/instructor/revenue")
@RequiredArgsConstructor
public class InstructorRevenueController {

	private final RevenueService revenueService;
	private final PayoutService payoutService;
	
	/**
     * Lấy thông tin số dư ví của giảng viên.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Nhận instructorId từ header</li>
     *   <li>Gọi service để lấy số dư ví</li>
     *   <li>Trả về thông tin số dư ví</li>
     * </ul>
     * @param instructorId ID giảng viên (lấy từ header X-Instructor-ID)
     * @return ResponseEntity chứa ApiResponse<WalletBalanceDto> với thông tin số dư ví
     */
	@HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/wallet/balance")
    public ResponseEntity<ApiResponse<WalletBalanceDto>> getWalletBalance(UUID instructorId) {
        
        WalletBalanceDto balance = payoutService.getWalletBalance();
        return ResponseEntity.ok(ApiResponse.success("Lấy số dư ví thành công", balance));
    }
	
	/**
     * Lấy lịch sử giao dịch ví của giảng viên.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Nhận instructorId từ header và thông tin phân trang</li>
     *   <li>Gọi service để lấy danh sách giao dịch ví</li>
     *   <li>Trả về danh sách giao dịch ví</li>
     * </ul>
     * @param instructorId ID giảng viên
     * @param pageable     Thông tin phân trang
     * @return ResponseEntity chứa ApiResponse<Page<WalletTransaction>> với danh sách giao dịch ví
     */
    @GetMapping("/wallet/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransaction>>> getWalletTransactions(UUID instructorId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<WalletTransaction> transactions = payoutService.getWalletTransactions(instructorId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử giao dịch ví thành công", transactions));
    }
    
    /**
     * Lấy lịch sử doanh thu của giảng viên.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Nhận instructorId từ header và thông tin phân trang</li>
     *   <li>Gọi service để lấy danh sách doanh thu</li>
     *   <li>Trả về danh sách doanh thu</li>
     * </ul>
     * @param instructorId ID giảng viên
     * @param pageable     Thông tin phân trang
     * @return ResponseEntity chứa ApiResponse<Page<RevenueRecord>> với danh sách doanh thu
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<RevenueRecord>>> getRevenueHistory(@PageableDefault(size = 20) Pageable pageable) {
        
        Page<RevenueRecord> revenues = revenueService.getInstructorRevenue(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử doanh thu thành công", revenues));
    }
    
    /**
     * Tạo yêu cầu rút tiền cho giảng viên.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Nhận instructorId từ header và thông tin yêu cầu từ body</li>
     *   <li>Gọi service để tạo yêu cầu rút tiền</li>
     *   <li>Trả về thông tin yêu cầu rút tiền vừa tạo</li>
     * </ul>
     * @param instructorId ID giảng viên
     * @param request      Thông tin yêu cầu rút tiền
     * @return ResponseEntity chứa ApiResponse<PayoutRequest> với yêu cầu rút tiền vừa tạo
     */
    @PostMapping("/payout/request")
    public ResponseEntity<ApiResponse<PayoutRequest>> createPayoutRequest(UUID instructorId,
            @Valid @RequestBody PayoutRequestDto request) {
        
        log.info("Instructor {} requesting payout: {}", instructorId, request.getAmount());
        
        PayoutRequest payoutRequest = payoutService.createPayoutRequest(instructorId, request);
        return ResponseEntity.ok(ApiResponse.success("Tạo yêu cầu rút tiền thành công", payoutRequest));
    }
    
    /**
     * Lấy danh sách yêu cầu rút tiền của giảng viên.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Nhận instructorId từ header và thông tin phân trang</li>
     *   <li>Gọi service để lấy danh sách yêu cầu rút tiền</li>
     *   <li>Trả về danh sách yêu cầu rút tiền</li>
     * </ul>
     * @param instructorId ID giảng viên
     * @param pageable     Thông tin phân trang
     * @return ResponseEntity chứa ApiResponse<Page<PayoutRequest>> với danh sách yêu cầu rút tiền
     */
    @GetMapping("/payout/requests")
    public ResponseEntity<ApiResponse<Page<PayoutRequest>>> getPayoutRequests(UUID instructorId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<PayoutRequest> requests = payoutService.getInstructorPayoutRequests(instructorId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu rút tiền thành công", requests));
    }
    
    /**
     * Hủy yêu cầu rút tiền của giảng viên.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Nhận instructorId từ header và requestId từ path</li>
     *   <li>Gọi service để hủy yêu cầu rút tiền</li>
     *   <li>Trả về thông tin yêu cầu rút tiền đã hủy</li>
     * </ul>
     * @param instructorId ID giảng viên
     * @param requestId    ID yêu cầu rút tiền
     * @return ResponseEntity chứa ApiResponse<PayoutRequest> với yêu cầu rút tiền đã hủy
     */
    @PutMapping("/payout/requests/{requestId}/cancel")
    public ResponseEntity<ApiResponse<PayoutRequest>> cancelPayoutRequest(UUID instructorId,
            @PathVariable Long requestId) {
        
        PayoutRequest cancelledRequest = payoutService.cancelPayoutRequest(requestId, instructorId);
        return ResponseEntity.ok(ApiResponse.success("Hủy yêu cầu rút tiền thành công", cancelledRequest));
    }
    
    /**
     * Lấy lịch sử thanh toán rút tiền đã hoàn thành của giảng viên.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Nhận instructorId từ header và thông tin phân trang</li>
     *   <li>Gọi service để lấy danh sách giao dịch rút tiền</li>
     *   <li>Trả về danh sách giao dịch rút tiền</li>
     * </ul>
     * @param instructorId ID giảng viên
     * @param pageable     Thông tin phân trang
     * @return ResponseEntity chứa ApiResponse<Page<PayoutTransaction>> với danh sách giao dịch rút tiền
     */
    @GetMapping("/payout/transactions")
    public ResponseEntity<ApiResponse<Page<PayoutTransaction>>> getPayoutTransactions(UUID instructorId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<PayoutTransaction> transactions = payoutService.getPayoutTransactions(instructorId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử giao dịch rút tiền thành công", transactions));
    }
}