package com.vinaacademy.platform.feature.order_payment.mapper;

import com.vinaacademy.platform.feature.order_payment.dto.OrderDto;
import com.vinaacademy.platform.feature.order_payment.dto.OrderRequest;
import com.vinaacademy.platform.feature.order_payment.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { PaymentMapper.class})
public interface OrderMapper {
		  
	
	@Mapping(target = "user_id", source = "user.id")
    @Mapping(target = "coupon_id", source = "coupon.id")
	@Mapping(target = "payment_id", expression = "java(order.getPayment() != null ? order.getPayment().getId() : null)")
	@Mapping(target = "paymentDto", source = "payment")
	OrderDto toOrderDto(Order order); 
	   

    Order toEntity(OrderDto orderDto);

    OrderRequest toOrderRequest(OrderDto orderDto);
}
