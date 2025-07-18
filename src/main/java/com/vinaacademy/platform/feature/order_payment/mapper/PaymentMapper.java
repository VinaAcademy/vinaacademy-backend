package com.vinaacademy.platform.feature.order_payment.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.vinaacademy.platform.feature.order_payment.dto.PaymentDto;
import com.vinaacademy.platform.feature.order_payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

	@Mapping(source = "order.id", target = "orderId")
	@Mapping(target = "urlPayment", expression = "java(getUrlTransaction(payment.getPaymentData()))")
	PaymentDto toDTO(Payment payment);

	List<PaymentDto> toDTOList(List<Payment> payments);

	default String getUrlTransaction(JsonNode node) {
		if (node != null && node.has("urlTransaction")) {
			return node.get("urlTransaction").asText();
		}
		return null;
	}
}
