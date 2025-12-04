package com.vinaacademy.platform.feature.order_payment;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;
import com.vinaacademy.platform.feature.order_payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1/paymentvnp")
@Slf4j
@RequiredArgsConstructor
public class VNpayController {
	
	private final PaymentService paymentService;
	
	
	@GetMapping("/ipn")
	public ApiResponse<PaymentStatus> handlePaymentIpn(@RequestParam Map<String, String> allParam) {
		log.info("VNPay IPN received with parameters count: {}", allParam.size());
		log.debug("VNPay IPN parameters: {}", allParam);
		
		try {
			PaymentStatus status = paymentService.validPayment(allParam);
			log.info("Payment validation result: {}", status);
			return ApiResponse.success(status);
		} catch (Exception e) {
			log.error("Error processing VNPay IPN: {}", e.getMessage(), e);
			throw e;
		}
	}
}