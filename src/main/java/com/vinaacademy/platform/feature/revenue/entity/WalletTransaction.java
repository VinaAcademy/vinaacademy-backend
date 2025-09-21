package com.vinaacademy.platform.feature.revenue.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.revenue.enums.WalletTransactionType;

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
@Table(name = "wallet_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "instructor_id", nullable = false)
	private UUID instructorId;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private WalletTransactionType type;
	
	// số tiền (dương = cộng, âm = trừ)
	@Column(name = "amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;
	
	// số dư sau giao dịch
	@Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
	private BigDecimal balanceAfter;
	
	// id bản ghi giao dịch liên quan (nếu có) (ví dụ: id của withdrawal request, refund request, etc.)
	@Column(name = "reference_id")
	private Long referenceId;
	
	// loại reference (nếu có) (REVENUE, WITHDRAW, REFUND, etc.)
	@Column(name = "reference_type")
	private String referenceType;
	
	// Mô tả giao dịch
	@Column(name = "description", length = 500)
	private String description;
	
}
