package com.vinaacademy.platform.feature.revenue.service;

import com.vinaacademy.platform.feature.order_payment.entity.Payment;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;

import java.util.Map;

public interface RevenueDistributionService {

    /**
     * Phân chia doanh thu sau khi thanh toán thành công
     * - Tạo RevenueRecord cho từng khóa học trong order
     * - Cộng tiền vào ví giảng viên
     * - Cộng tiền vào ví nền tảng
     * - Ghi lại WalletTransaction
     */
    void distributeRevenue(Payment payment, Map<String, String> vnpayResponse);

    /**
     * Tính toán tỷ lệ phân chia doanh thu
     * Mặc định: 70% cho giảng viên, 30% cho nền tảng
     */
    RevenueCalculation calculateRevenue(java.math.BigDecimal totalAmount);

    /**
     * DTO chứa thông tin tính toán doanh thu
     */
    record RevenueCalculation(
            java.math.BigDecimal totalAmount,
            java.math.BigDecimal instructorEarning,
            java.math.BigDecimal platformFee,
            java.math.BigDecimal instructorPercent
    ) {}
}