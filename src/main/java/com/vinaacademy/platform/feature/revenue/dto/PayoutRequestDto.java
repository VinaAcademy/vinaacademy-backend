package com.vinaacademy.platform.feature.revenue.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequestDto {

	@NotNull
    @DecimalMin(value = "50000", message = "Minimum payout amount is 50,000 VND")
    private BigDecimal amount;
    
    @NotBlank
    private String bankName;
    
    @NotBlank
    private String bankAccount;
    
    @NotBlank
    private String accountHolder;
    
    private String note;
}
