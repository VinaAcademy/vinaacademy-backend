package com.vinaacademy.platform.feature.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private UUID couponId;
    private UUID userId;
    private List<CartItemDto> cartItems = new ArrayList<>();
}