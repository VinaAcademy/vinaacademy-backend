package com.vinaacademy.platform.feature.revenue.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;



/**
 * Lịch sử thanh toán đã hoàn thành
 */
@Entity
@Table(name = "payout_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutTransaction extends BaseEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payout_request_id", nullable = false)
    private Long payoutRequestId;
    
    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;
    
    /**
     * Số tiền đã thanh toán
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    /**
     * Mã giao dịch giả lập
     */
    @Column(name = "transaction_ref", nullable = false, unique = true)
    private String transactionRef;
    
    /**
     * Thông tin ngân hàng
     */
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "bank_account", length = 50)
    private String bankAccount;
    
    @Column(name = "account_holder", length = 100)
    private String accountHolder;
    
    /**
     * Admin thực hiện thanh toán
     */
    @Column(name = "processed_by", nullable = false)
    private UUID processedBy;
    
    @Column(name = "note", length = 255)
    private String note;

}
