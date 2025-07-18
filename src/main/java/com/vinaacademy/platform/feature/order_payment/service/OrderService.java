package com.vinaacademy.platform.feature.order_payment.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vinaacademy.platform.feature.order_payment.dto.OrderCouponRequest;
import com.vinaacademy.platform.feature.order_payment.dto.OrderDto;
import com.vinaacademy.platform.feature.order_payment.dto.OrderRequest;

public interface OrderService {
	
	OrderDto createOrder();
	
	void deleteOrder(UUID orderId);
	
	OrderDto updateOrder(OrderRequest orderRequest);
	
	OrderDto getOrder(UUID orderId);
	
	Page<OrderDto> getOrders(Pageable pageable);
	
	OrderDto updateCoupon(OrderCouponRequest orderCouponRequest);

}
