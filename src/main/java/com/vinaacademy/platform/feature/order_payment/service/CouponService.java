package com.vinaacademy.platform.feature.order_payment.service;

import java.util.List;

import com.vinaacademy.platform.feature.order_payment.dto.CouponDto;

public interface CouponService {
	List<CouponDto> getAllCoupons();
	List<CouponDto> getAllValidDateCoupons();
}
