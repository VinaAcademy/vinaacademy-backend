package com.vinaacademy.platform.feature.order_payment;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.order_payment.dto.PaymentDto;
import com.vinaacademy.platform.feature.order_payment.dto.PaymentRequest;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;
import com.vinaacademy.platform.feature.order_payment.service.PaymentService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
	private final PaymentService paymentService;
	
	
	@HasAnyRole({AuthConstants.STUDENT_ROLE}) 
    @PostMapping("/{orderId}")
    public ApiResponse<PaymentDto> createPayment(@PathVariable UUID orderId, HttpServletRequest request) {
        log.debug("Create payment for order "+orderId);
        return ApiResponse.success(paymentService.createPayment(orderId, request));
    }
    
//    @HasAnyRole({AuthConstants.STUDENT_ROLE})
//    @GetMapping("/list")
//    public ApiResponse<List<PaymentDto>> getPaymentList() {
//    	return ApiResponse.success(paymentService.getPaymentList());
//    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/detail/{paymentId}")
    public ApiResponse<PaymentDto> getPayment(@PathVariable UUID paymentId) {
    	log.debug("Get payment "+paymentId);
    	return ApiResponse.success(paymentService.getPayment(paymentId));
    }
    
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/valid")
    public ApiResponse<PaymentStatus> valiReturn(@RequestParam Map<String, String> requestParams) {
    	return ApiResponse.success(paymentService.validUrlReturn(requestParams));
    }
    
    
}
