package com.vinaacademy.platform.feature.order_payment;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ApiResponse<PaymentStatus> getPaymentIpn(@RequestParam Map<String, String> allParam) {
    	log.debug("IPN "+allParam.values().toString());
    	return ApiResponse.success(paymentService.validPayment(allParam));
    }

}