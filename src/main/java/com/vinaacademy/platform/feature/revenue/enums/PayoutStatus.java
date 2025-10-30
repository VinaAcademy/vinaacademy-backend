package com.vinaacademy.platform.feature.revenue.enums;

public enum PayoutStatus {
	PENDING,     // Chờ duyệt
    REVIEWING,   // Đang xem xét
    APPROVED,    // Đã duyệt
    PROCESSING,  // Đang xử lý
    PAID,        // Đã thanh toán
    REJECTED,    // Bị từ chối
    CANCELLED    // Đã hủy
}
