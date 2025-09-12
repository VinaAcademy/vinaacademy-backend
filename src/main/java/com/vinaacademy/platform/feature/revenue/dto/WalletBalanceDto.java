package com.vinaacademy.platform.feature.revenue.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceDto {

	private BigDecimal balance;
    private BigDecimal totalEarnings;
    private BigDecimal totalWithdrawn;
    private BigDecimal pendingWithdraw;
    private BigDecimal availableBalance;
}
