package com.vinaacademy.platform.feature.order_payment.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCouponRequest {
	
	private UUID orderId;
	private UUID couponId;

}
