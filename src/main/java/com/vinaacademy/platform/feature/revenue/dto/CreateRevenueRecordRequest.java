package com.vinaacademy.platform.feature.revenue.dto;

import java.math.BigDecimal;
import java.util.UUID;

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
public class CreateRevenueRecordRequest {

	@NotNull
    private UUID courseId;
    
    @NotNull 
    private Long enrollmentId;
    
    @NotNull
    private UUID paymentId;
    
    @NotNull
    private UUID instructorId;
    
    @NotNull
    private UUID studentId;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false) 
    private BigDecimal instructorPercent;
    
    @NotBlank
    private String vnpayTxnRef;
    
    private String vnpayResponseCode;
    private String vnpayTransactionNo;
    private String vnpayOrderInfo;
    private BigDecimal vnpayAmount;
}
