package com.vinaacademy.platform.feature.revenue.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.revenue.enums.RevenueStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "revenue_records", indexes = {
    @Index(name = "idx_revenue_instructor", columnList = "instructor_id"),
    @Index(name = "idx_revenue_course", columnList = "course_id"),
    @Index(name = "idx_revenue_created", columnList = "created_date"),
    @Index(name = "idx_revenue_status", columnList = "status")
},
	uniqueConstraints = {
	        @UniqueConstraint(name = "uk_revenue_payment_instructor_course", 
	                        columnNames = {"payment_id", "instructor_id", "course_id"})
	    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueRecord extends BaseEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private UUID courseId;
    
    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;
    
    @Column(name = "payment_id") 
    private UUID paymentId;
    
    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;
    
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    // Tong so tien hoc vien phai thanh toan
	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	// So tien giang vien nhan duoc
	@Column(name = "instructor_earning", nullable = false, precision = 10, scale = 2)
	private BigDecimal instructorEarning;
	
	// Phi nen tang 
	@Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
	private BigDecimal platformFee;
	
	// Ty le % giang vien nhan duoc
	@Column(name = "instructor_percent", nullable = false, precision = 5, scale = 4)
	private BigDecimal instructorPercent;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	@Builder.Default
	private RevenueStatus status = RevenueStatus.ACTIVE; // Doanh thu đang hoạt động
	
	@Column(name = "reason", length = 500)
	private String reason; // Lý do hoàn tiền nếu có
	
	/**
	 * Mã giao dịch từ VNPAY (vnp_TxnRef)
	 * Đây là ID unique được gửi đi và nhận về từ VNPAY
	 */
	@Column(name = "vnpay_txn_ref", nullable = false, unique = true, length = 100)
	private String vnpayTxnRef;

	/**
	 * Response code từ VNPAY callback
	 * 00: Thành công
	 * Khác 00: Thất bại với mã lỗi cụ thể
	 */
	@Column(name = "vnpay_response_code", length = 10)
	private String vnpayResponseCode;

	/**
	 * Transaction No từ VNPAY (vnp_TransactionNo)
	 * Mã giao dịch do VNPAY sinh ra (khác với vnp_TxnRef)
	 */
	@Column(name = "vnpay_transaction_no", length = 100)
	private String vnpayTransactionNo;

	/**
	 * Thông tin đơn hàng gửi đến VNPAY
	 */
	@Column(name = "vnpay_order_info", length = 255)
	private String vnpayOrderInfo;

	/**
	 * Số tiền gửi đến VNPAY (đã * 100)
	 */
	@Column(name = "vnpay_amount", precision = 15, scale = 0)
	private BigDecimal vnpayAmount;
}