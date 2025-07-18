package com.vinaacademy.platform.feature.order_payment.dto;

import com.vinaacademy.platform.feature.order_payment.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDto {

    private UUID id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime expiredAt;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private LocalDateTime startedAt;
    private Long usageLimit;
    private Long usedCount;
    private Boolean valid;
    
}



