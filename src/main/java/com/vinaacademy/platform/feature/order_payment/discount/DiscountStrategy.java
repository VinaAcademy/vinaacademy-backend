package com.vinaacademy.platform.feature.order_payment.discount;

import java.math.BigDecimal;

import com.vinaacademy.platform.feature.order_payment.entity.Coupon;

public interface DiscountStrategy {
	
	BigDecimal calculateDiscount(BigDecimal subTotal, Coupon coupon);

}
