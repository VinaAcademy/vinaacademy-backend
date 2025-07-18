package com.vinaacademy.platform.feature.order_payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vinaacademy.platform.feature.order_payment.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private UUID id;
    private OrderStatus status;
    private BigDecimal subTotal;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private UUID coupon_id;
    private UUID user_id;
    private UUID payment_id;
    private LocalDateTime createdDate;
    private PaymentDto paymentDto;
    
    private List<OrderItemDto> orderItemsDto = new ArrayList<>();;
}