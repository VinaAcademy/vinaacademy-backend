package com.vinaacademy.platform.feature.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequest {

	private Long id;

	private UUID coupon_id;

	private UUID user_id;

}