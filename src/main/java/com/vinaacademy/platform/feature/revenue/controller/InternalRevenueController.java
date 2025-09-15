package com.vinaacademy.platform.feature.revenue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.revenue.dto.CreateRevenueRecordRequest;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;
import com.vinaacademy.platform.feature.revenue.service.RevenueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/internal/revenue")
@RequiredArgsConstructor
public class InternalRevenueController {
	
	private final RevenueService revenueService;
    
    /**
     * API này được gọi từ payment service khi VNPAY callback thành công
     * Tạo bản ghi doanh thu khi nhận callback thành công từ VNPAY (gọi từ payment service).
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Nhận POST request với thông tin giao dịch từ payment service.</li>
     *   <li>Ghi log thông tin giao dịch nhận được.</li>
     *   <li>Gọi {@link RevenueService#createRevenueRecord(CreateRevenueRecordRequest)} để tạo bản ghi doanh thu.</li>
     *   <li>Nếu thành công, trả về bản ghi doanh thu vừa tạo trong ApiResponse.</li>
     *   <li>Nếu có lỗi (ví dụ: trùng giao dịch, dữ liệu không hợp lệ), ghi log lỗi và trả về ApiResponse với error.</li>
     * </ul>
     *
     * @param request Thông tin giao dịch thanh toán thành công từ VNPAY
     * @return ResponseEntity chứa ApiResponse<RevenueRecord> nếu thành công, hoặc ApiResponse với error nếu lỗi
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RevenueRecord>> createRevenueFromPayment(
            @Valid @RequestBody CreateRevenueRecordRequest request) {
        
        // Ghi log thông tin giao dịch nhận được
        log.info("Creating revenue record from payment callback: {}", request.getVnpayTxnRef());
        
        try {
            // Gọi service để tạo bản ghi doanh thu
            RevenueRecord revenueRecord = revenueService.createRevenueRecord(request);
            return ResponseEntity.ok(ApiResponse.success("Tạo bản ghi doanh thu thành công", revenueRecord));
        } catch (Exception e) {
            // Ghi log lỗi và trả về ApiResponse với error
            log.error("Error creating revenue record: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Lỗi khi tạo bản ghi doanh thu: " + e.getMessage()));
        }
    }
}