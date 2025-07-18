package com.vinaacademy.platform.feature.order_payment.utils;

import com.vinaacademy.platform.feature.order_payment.entity.Coupon;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {
	
	public static boolean isCouponValid(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now(); 
        if (coupon.getStartedAt() != null && coupon.getStartedAt().isAfter(now)) {
            return false; 
        }
        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(now)) {
            return false; 
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return false; 
        }
        return true;
    }
	
	public static PaymentStatus orderReturn(Map<String, String> requestParams) {
		Map<String, String> fields = new HashMap<>();
		
		for (Entry<String, String> entry : requestParams.entrySet()) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }
		String vnp_SecureHash = requestParams.get("vnp_SecureHash");
		if (fields.containsKey("vnp_SecureHashType")) {
			fields.remove("vnp_SecureHashType");
		}
		if (fields.containsKey("vnp_SecureHash")) {
			fields.remove("vnp_SecureHash");
		}
		String signValue = VNPayConfig.hashAllFields(fields);
		if (signValue.equals(vnp_SecureHash)) {
			if ("00".equals(requestParams.get("vnp_TransactionStatus"))) {
				return PaymentStatus.COMPLETED;
			} else {
				return PaymentStatus.CANCELLED;
			}
		} else {
			return PaymentStatus.FAILED;
		}
	}
}
