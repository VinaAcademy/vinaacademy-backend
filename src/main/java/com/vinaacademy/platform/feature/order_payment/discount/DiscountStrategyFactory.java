package com.vinaacademy.platform.feature.order_payment.discount;

import com.vinaacademy.platform.feature.order_payment.enums.DiscountType;

public class DiscountStrategyFactory {

    public static DiscountStrategy getStrategy(DiscountType type) {
        return switch (type) {
            case PERCENTAGE -> new PercentageDiscount();
            case FIXED_AMOUNT -> new FixedAmountDiscount();
        };
    }
}