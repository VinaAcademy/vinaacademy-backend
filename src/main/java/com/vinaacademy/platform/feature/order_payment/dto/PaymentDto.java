package com.vinaacademy.platform.feature.order_payment.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private JsonNode paymentData;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private String urlPayment;
    
   
}