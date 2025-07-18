package com.vinaacademy.platform.feature.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

	private Long id;

    private Long cart_id;

    private UUID course_id;

    private BigDecimal price;

}