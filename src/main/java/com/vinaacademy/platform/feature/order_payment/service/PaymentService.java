package com.vinaacademy.platform.feature.order_payment.service;

import com.vinaacademy.platform.feature.order_payment.dto.PaymentDto;
import com.vinaacademy.platform.feature.order_payment.dto.PaymentRequest;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {
	
	PaymentDto createPayment(UUID orderId, HttpServletRequest request);
	
	PaymentDto updatePayment(PaymentRequest paymentRequest);
		
	PaymentDto getPayment(UUID paymentId);
	
	List<PaymentDto> getPaymentList();
	
	PaymentStatus validPayment(Map<String, String> requestParams);
	
	PaymentStatus validUrlReturn(Map<String, String> requestParams);
	
	String testApi();
}
