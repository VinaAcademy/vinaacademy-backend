package com.vinaacademy.platform.feature.migration;

import com.vinaacademy.platform.feature.order_payment.entity.Coupon;
import com.vinaacademy.platform.feature.order_payment.enums.DiscountType;
import com.vinaacademy.platform.feature.order_payment.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponMigrationService {
    private final CouponRepository couponRepository;

    @Transactional
    public void createCouponSeedData() {
        if (couponRepository.count() > 0) {
            log.info("Coupons already exist in the database, skipping coupon seed data creation");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Active percentage coupons
        Coupon percentageCoupon1 = Coupon.builder()
                .code("WELCOME10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .startedAt(now.minusDays(7))
                .expiredAt(now.plusDays(30))
                .maxDiscountAmount(BigDecimal.valueOf(100000))
                .minOrderValue(BigDecimal.valueOf(50000))
                .usageLimit(100L)
                .usedCount(5L)
                .build();

        Coupon percentageCoupon2 = Coupon.builder()
                .code("STUDENT15")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15))
                .startedAt(now.minusDays(3))
                .expiredAt(now.plusDays(60))
                .maxDiscountAmount(BigDecimal.valueOf(200000))
                .minOrderValue(BigDecimal.valueOf(100000))
                .usageLimit(200L)
                .usedCount(12L)
                .build();

        // Active fixed amount coupons
        Coupon fixedCoupon1 = Coupon.builder()
                .code("SAVE50K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(50000))
                .startedAt(now.minusDays(5))
                .expiredAt(now.plusDays(45))
                .minOrderValue(BigDecimal.valueOf(200000))
                .usageLimit(50L)
                .usedCount(8L)
                .build();

        Coupon fixedCoupon2 = Coupon.builder()
                .code("NEWUSER25K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(25000))
                .startedAt(now.minusDays(1))
                .expiredAt(now.plusDays(90))
                .minOrderValue(BigDecimal.valueOf(100000))
                .usageLimit(500L)
                .usedCount(45L)
                .build();

        // Premium coupons with higher values
        Coupon premiumCoupon = Coupon.builder()
                .code("PREMIUM20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .startedAt(now.minusDays(10))
                .expiredAt(now.plusDays(15))
                .maxDiscountAmount(BigDecimal.valueOf(500000))
                .minOrderValue(BigDecimal.valueOf(500000))
                .usageLimit(20L)
                .usedCount(3L)
                .build();

        // Limited time flash sale coupon
        Coupon flashSale = Coupon.builder()
                .code("FLASH30")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(30))
                .startedAt(now.minusHours(2))
                .expiredAt(now.plusHours(22))
                .maxDiscountAmount(BigDecimal.valueOf(300000))
                .minOrderValue(BigDecimal.valueOf(300000))
                .usageLimit(10L)
                .usedCount(7L)
                .build();

        // Expired coupons for testing
        Coupon expiredCoupon = Coupon.builder()
                .code("EXPIRED10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .startedAt(now.minusDays(30))
                .expiredAt(now.minusDays(5))
                .maxDiscountAmount(BigDecimal.valueOf(50000))
                .minOrderValue(BigDecimal.valueOf(50000))
                .usageLimit(100L)
                .usedCount(85L)
                .build();

        // Future coupon (not yet active)
        Coupon futureCoupon = Coupon.builder()
                .code("FUTURE25")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(25))
                .startedAt(now.plusDays(7))
                .expiredAt(now.plusDays(37))
                .maxDiscountAmount(BigDecimal.valueOf(250000))
                .minOrderValue(BigDecimal.valueOf(200000))
                .usageLimit(50L)
                .usedCount(0L)
                .build();

        // Fully used coupon
        Coupon fullyUsedCoupon = Coupon.builder()
                .code("SOLD100K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(100000))
                .startedAt(now.minusDays(15))
                .expiredAt(now.plusDays(15))
                .minOrderValue(BigDecimal.valueOf(500000))
                .usageLimit(5L)
                .usedCount(5L)
                .build();

        // Unlimited usage coupon
        Coupon unlimitedCoupon = Coupon.builder()
                .code("ALWAYS5")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(5))
                .startedAt(now.minusDays(365))
                .expiredAt(now.plusDays(365))
                .maxDiscountAmount(BigDecimal.valueOf(50000))
                .minOrderValue(BigDecimal.valueOf(50000))
                .usageLimit(null) // Unlimited
                .usedCount(150L)
                .build();

        // Save all coupons
        couponRepository.save(percentageCoupon1);
        couponRepository.save(percentageCoupon2);
        couponRepository.save(fixedCoupon1);
        couponRepository.save(fixedCoupon2);
        couponRepository.save(premiumCoupon);
        couponRepository.save(flashSale);
        couponRepository.save(expiredCoupon);
        couponRepository.save(futureCoupon);
        couponRepository.save(fullyUsedCoupon);
        couponRepository.save(unlimitedCoupon);

        log.info("Successfully created 10 coupon seed data entries");
    }
}
