package com.vinaacademy.platform.feature.order_payment.discount;

import java.math.BigDecimal;

import com.vinaacademy.platform.feature.order_payment.entity.Coupon;

public class PercentageDiscount implements DiscountStrategy{

	@Override
    public BigDecimal calculateDiscount(BigDecimal subTotal, Coupon coupon) {
        if (coupon.getDiscountValue() == null) return BigDecimal.ZERO;

        BigDecimal discount = subTotal.multiply(coupon.getDiscountValue().divide(new BigDecimal("100")));

        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        return discount;
    }

}
