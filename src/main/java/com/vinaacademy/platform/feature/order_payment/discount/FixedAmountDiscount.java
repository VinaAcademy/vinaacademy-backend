package com.vinaacademy.platform.feature.order_payment.discount;

import java.math.BigDecimal;

import com.vinaacademy.platform.feature.order_payment.entity.Coupon;

public class FixedAmountDiscount implements DiscountStrategy {

	@Override
	public BigDecimal calculateDiscount(BigDecimal subTotal, Coupon coupon) {
		if (coupon.getDiscountValue() == null)
			return BigDecimal.ZERO;

		BigDecimal discount = coupon.getDiscountValue();
		return discount.compareTo(subTotal) > 0 ? subTotal : discount;
	}

}
