package com.vinaacademy.platform.feature.cart.service;

import com.vinaacademy.platform.feature.cart.dto.CartItemDto;
import com.vinaacademy.platform.feature.cart.dto.CartItemRequest;
import com.vinaacademy.platform.feature.cart.entity.CartItem;

import java.util.List;
import java.util.UUID;

public interface CartItemService {
	 
	 CartItemDto addCartItem(CartItemRequest request);
	 
	 CartItemDto updateCartItem(CartItemRequest request);
	 
	 void deleteCartItem(Long cartItemId);
	 
	 List<CartItemDto> getCartItems(Long cartId);
	 
	 List<CartItem> getCartItems(UUID userId);
	 
	 CartItemDto getCartItem(Long cartItemId);
}
