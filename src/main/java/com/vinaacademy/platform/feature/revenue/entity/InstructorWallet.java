package com.vinaacademy.platform.feature.revenue.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instructor_wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorWallet extends BaseEntity {
	
	@Id
	@Column(name = "instructor_id", nullable = false)
	private UUID instructorId;
	
	// số dư ví của giảng viên
	@Column(name = "balance", nullable = false, precision = 15, scale = 2)
	@Builder.Default
	private BigDecimal balance = BigDecimal.ZERO;
	
	// tổng thu nhập
	@Column(name = "total_earnings", nullable = false, precision = 15, scale = 2)
	@Builder.Default
	private BigDecimal totalEarnings = BigDecimal.ZERO;
	
	// tổng số tiền đã rút
	@Column(name = "total_withdrawn", nullable = false, precision = 15, scale = 2)
	@Builder.Default
	private BigDecimal totalWithdrawn = BigDecimal.ZERO;
	
	// số tiền đang chờ xử lý (pending withdraw)
	@Column(name = "pending_withdraw", nullable = false, precision = 15, scale = 2)
	@Builder.Default
	private BigDecimal pendingWithdraw = BigDecimal.ZERO;
	
	// tính số tiền có thể rút
	public BigDecimal getAvailableBalance() {
		return balance.subtract(pendingWithdraw);
	}
	
	// kiểm tra xem ví có đủ tiền để rút không
	public boolean hasSufficientBalance(BigDecimal amount) {
		return getAvailableBalance().compareTo(amount) >= 0;
	}

}
