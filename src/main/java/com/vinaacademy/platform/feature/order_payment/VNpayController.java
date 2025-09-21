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

/**
 * Controller xử lý các callback từ VNPay payment gateway.
 * 
 * <p>Controller này chịu trách nhiệm nhận và xử lý các thông báo từ VNPay
 * về trạng thái thanh toán, bao gồm IPN (Instant Payment Notification).
 * 
 * <p><strong>Endpoint chính:</strong> {@code POST /api/v1/paymentvnp/ipn}
 * 
 * @author VinaAcademy Development Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/paymentvnp")
@Slf4j
@RequiredArgsConstructor
public class VNpayController {
	
	private final PaymentService paymentService;
	
	/**
	 * Xử lý IPN (Instant Payment Notification) từ VNPay.
	 * 
	 * <p>Endpoint này được VNPay gọi để thông báo kết quả thanh toán.
	 * URL này phải được cấu hình chính xác trong VNPay merchant portal:
	 * {@code https://yourdomain.com/api/v1/paymentvnp/ipn}
	 * 
	 * <h3>Quy trình xử lý:</h3>
	 * <ol>
	 *   <li>Nhận parameters từ VNPay callback</li>
	 *   <li>Validate chữ ký và tính hợp lệ của request</li>
	 *   <li>Cập nhật trạng thái payment trong database</li>
	 *   <li>Phân phối doanh thu cho giảng viên</li>
	 *   <li>Trả về status confirmation cho VNPay</li>
	 * </ol>
	 * 
	 * @param allParam Map chứa tất cả parameters từ VNPay callback
	 *                 Bao gồm: vnp_ResponseCode, vnp_TransactionNo, vnp_Amount, v.v.
	 * @return ApiResponse với PaymentStatus để VNPay xác nhận đã nhận thông báo
	 */
	@PostMapping("/ipn")
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
	
	/**
	 * Endpoint GET cho testing IPN - chỉ sử dụng trong môi trường development.
	 * 
	 * <p>VNPay chính thức sử dụng POST method cho IPN, endpoint GET này
	 * chỉ để thuận tiện trong quá trình development và testing.
	 * 
	 * @param allParam Map chứa parameters để test
	 * @return ApiResponse với PaymentStatus
	 */
	@GetMapping("/ipn")
	public ApiResponse<PaymentStatus> getPaymentIpn(@RequestParam Map<String, String> allParam) {
		log.debug("VNPay IPN GET request (testing): {}", allParam);
		return ApiResponse.success(paymentService.validPayment(allParam));
	}
}