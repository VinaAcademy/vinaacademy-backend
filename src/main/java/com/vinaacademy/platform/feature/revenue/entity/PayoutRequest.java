package com.vinaacademy.platform.feature.revenue.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.revenue.enums.PayoutStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payout_requests", indexes = {
	@jakarta.persistence.Index(name = "idx_instructor_id", columnList = "instructor_id"),
	@jakarta.persistence.Index(name = "idx_status", columnList = "status"),
	@jakarta.persistence.Index(name = "idx_created_date", columnList = "created_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequest extends BaseEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;
    
    /**
     * Số tiền yêu cầu rút
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;
    
    /**
     * Thông tin ngân hàng (giả lập)
     */
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "bank_account", length = 50)
    private String bankAccount;
    
    @Column(name = "account_holder", length = 100)
    private String accountHolder;
    
    /**
     * Ghi chú từ giảng viên
     */
    @Column(name = "note", length = 500)
    private String note;
    
    /**
     * Lý do từ chối (nếu có)
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    

    /**
     * Thời gian admin xử lý
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    /**
     * Admin xử lý
     */
    @Column(name = "processed_by")
    private Long processedBy;
    
}
