package com.vinaacademy.platform.feature.revenue.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.revenue.dto.PayoutApprovalRequest;
import com.vinaacademy.platform.feature.revenue.dto.RevenueDashboardDto;
import com.vinaacademy.platform.feature.revenue.entity.PayoutRequest;
import com.vinaacademy.platform.feature.revenue.service.PayoutService;
import com.vinaacademy.platform.feature.revenue.service.RevenueService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/revenue")
@RequiredArgsConstructor
@HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
public class AdminRevenueController {

    private final PayoutService payoutService;
    private final RevenueService revenueService;
    
    /**
     * Lấy danh sách yêu cầu rút tiền đang chờ duyệt.
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Nhận yêu cầu GET với thông tin phân trang (page, size, sort).</li>
     *   <li>Gọi {@link PayoutService#getPendingPayoutRequests(Pageable)} để lấy danh sách các yêu cầu rút tiền có trạng thái "pending".</li>
     *   <li>Trả về danh sách dạng phân trang cho phía client.</li>
     * </ul>
     *
     * @param pageable thông tin phân trang (mặc định size=20)
     * @return ResponseEntity chứa ApiResponse<Page<PayoutRequest>> các yêu cầu rút tiền đang chờ duyệt
     */
    @GetMapping("/payout/pending")
    public ResponseEntity<ApiResponse<Page<PayoutRequest>>> getPendingPayoutRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        // Lấy danh sách yêu cầu rút tiền trạng thái pending
        Page<PayoutRequest> pendingRequests = payoutService.getPendingPayoutRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu rút tiền thành công", pendingRequests));
    }
    
    /**
     * Duyệt hoặc từ chối yêu cầu rút tiền của giảng viên.
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Nhận PUT request với thông tin adminId (header) và dữ liệu duyệt/từ chối (body).</li>
     *   <li>Ghi log thao tác duyệt/từ chối.</li>
     *   <li>Gọi {@link PayoutService#approvePayoutRequest(PayoutApprovalRequest, UUID)} để xử lý duyệt hoặc từ chối.</li>
     *   <li>Trả về thông tin yêu cầu rút tiền đã được cập nhật trạng thái.</li>
     * </ul>
     *
     * @param adminId UUID của admin thực hiện thao tác (lấy từ header X-Admin-ID)
     * @param approvalRequest thông tin duyệt/từ chối yêu cầu rút tiền
     * @return ResponseEntity chứa ApiResponse<PayoutRequest> đã được xử lý
     */
    @PutMapping("/payout/approve")
    public ResponseEntity<ApiResponse<PayoutRequest>> approvePayoutRequest(
            @Valid @RequestBody PayoutApprovalRequest approvalRequest) {
        // Ghi log thao tác duyệt/từ chối
        log.info("Admin processing payout request: {}, approved: {}", 
                approvalRequest.getPayoutRequestId(), approvalRequest.getApproved());
        // Xử lý duyệt/từ chối yêu cầu rút tiền
        PayoutRequest processedRequest = payoutService.approvePayoutRequest(approvalRequest);
        String message = approvalRequest.getApproved() ? "Duyệt yêu cầu rút tiền thành công" : "Từ chối yêu cầu rút tiền thành công";
        return ResponseEntity.ok(ApiResponse.success(message, processedRequest));
    }
    
    /**
     * Lấy thống kê tổng quan doanh thu cho dashboard admin.
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Nhận GET request từ phía dashboard admin.</li>
     *   <li>Gọi {@link RevenueService#getDashboardStats()} để lấy dữ liệu tổng quan (doanh thu, số lượng giao dịch, ...).</li>
     *   <li>Trả về dữ liệu dashboard cho phía client.</li>
     * </ul>
     *
     * @return ResponseEntity chứa ApiResponse<RevenueDashboardDto> với dữ liệu thống kê tổng quan
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<RevenueDashboardDto>> getDashboard() {
        // Lấy dữ liệu thống kê tổng quan
        RevenueDashboardDto dashboard = revenueService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu dashboard thành công", dashboard));
    }
    
    /**
     * Xử lý hoàn tiền cho giao dịch thanh toán qua VNPay.
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Nhận PUT request với mã giao dịch và lý do hoàn tiền.</li>
     *   <li>Ghi log thao tác hoàn tiền.</li>
     *   <li>Gọi {@link RevenueService#processRefund(String, String)} để thực hiện hoàn tiền.</li>
     *   <li>Trả về thông báo thành công cho phía client.</li>
     * </ul>
     *
     * @param vnpayTxnRef mã giao dịch VNPay cần hoàn tiền
     * @param reason lý do hoàn tiền
     * @return ResponseEntity chứa ApiResponse<String> thông báo hoàn tiền thành công
     */
    @PutMapping("/refund")
    public ResponseEntity<ApiResponse<String>> processRefund(
            @RequestParam String vnpayTxnRef,
            @RequestParam String reason) {
        // Ghi log thao tác hoàn tiền
        log.info("Admin processing refund for transaction: {}, reason: {}", vnpayTxnRef, reason);
        // Thực hiện hoàn tiền
        revenueService.processRefund(vnpayTxnRef, reason);
        return ResponseEntity.ok(ApiResponse.success("Hoàn tiền thành công", "Refund processed successfully"));
    }
}