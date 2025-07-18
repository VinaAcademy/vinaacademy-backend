package com.vinaacademy.platform.feature.order_payment.service;

import com.vinaacademy.platform.feature.order_payment.dto.OrderItemDto;

import java.util.List;
import java.util.UUID;

public interface OrderItemService {
	
	List<OrderItemDto> getOrderItems(UUID orderId);

}
