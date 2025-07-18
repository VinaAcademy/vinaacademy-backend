package com.vinaacademy.platform.feature.order_payment.mapper;

import com.vinaacademy.platform.feature.cart.entity.CartItem;
import com.vinaacademy.platform.feature.order_payment.dto.OrderItemDto;
import com.vinaacademy.platform.feature.order_payment.entity.Order;
import com.vinaacademy.platform.feature.order_payment.entity.OrderItem;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "course", source = "cartItem.course")
	@Mapping(target = "price", source = "cartItem.price")
	@Mapping(target = "order", expression = "java(order)")
	OrderItem toOrderItem(CartItem cartItem, @Context Order order);

	List<OrderItem> toOrderItemList(List<CartItem> cartItems, @Context Order order);
	
	@Mapping(target = "order_id", source = "order.id")
	@Mapping(target = "course_id", source = "course.id")
	@Mapping(target = "course_name", source = "course.name")
	@Mapping(target = "url_image", source = "course.image")
	OrderItemDto toOrderItemDto(OrderItem orderItem);
	
	List<OrderItemDto> toOrderItemDtoList(List<OrderItem> cartItems);


}
