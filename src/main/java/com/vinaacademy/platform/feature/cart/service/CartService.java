package com.vinaacademy.platform.feature.cart.service;

import com.vinaacademy.platform.feature.cart.dto.CartDto;
import com.vinaacademy.platform.feature.cart.dto.CartRequest;

import java.util.UUID;

public interface CartService {
	
	 CartDto getCart(UUID userId);
	 
	 CartDto createCart(CartRequest request);

	 CartDto updateCart(CartRequest request);
	 
	 void deleteCart(CartRequest request);
	
}
