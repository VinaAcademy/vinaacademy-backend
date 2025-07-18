package com.vinaacademy.platform.feature.order_payment;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vinaacademy.platform.feature.cart.service.CartItemService;
import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.order_payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/paymenttest")
@Slf4j
@RequiredArgsConstructor
public class testVNpayController {
	private final PaymentService paymentService;
	private final CartItemService cartItemService;
	
//    @GetMapping
//    public ApiResponse<String> getPaymentUrl() {
//    	return ApiResponse.success(paymentService.testApi());
//    }
    
//    @GetMapping("/submitOrder")
//    public String submidOrder(@RequestParam("amount") int orderTotal,
//                            @RequestParam("orderInfo") String orderInfo,
//                            HttpServletRequest request){
//        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
//        String vnpayUrl = "";
//		try {
//			vnpayUrl = VNPayConfig.createOrder(orderTotal, orderInfo, baseUrl);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        return  vnpayUrl;
//    }

    
    
//    @GetMapping("/get-ip")
//    public String getClientIp(@RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
//                              @RequestHeader(value = "X-Real-IP", required = false) String xRealIp) {
//        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
//            return "Client IP: " + xForwardedFor.split(",")[0];
//        } else if (xRealIp != null && !xRealIp.isEmpty()) {
//            return "Client IP: " + xRealIp;
//        } else {
//            return "Không xác định được IP (thiếu header)";
//        }
//    }
}