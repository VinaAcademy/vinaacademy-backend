package com.vinaacademy.platform.feature.order_payment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vinaacademy.platform.feature.order_payment.dto.CouponDto;
import com.vinaacademy.platform.feature.order_payment.entity.Coupon;
import com.vinaacademy.platform.feature.order_payment.mapper.CouponMapper;
import com.vinaacademy.platform.feature.order_payment.repository.CouponRepository;
import com.vinaacademy.platform.feature.order_payment.utils.Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService{
		
	private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;
    
    private final Utils utils;

    @Override
    @Transactional(readOnly = true)
    public List<CouponDto> getAllCoupons() {
    	List<Coupon> coupons = couponRepository.findAll();

        return coupons.stream()
            .map(coupon -> {
                CouponDto dto = couponMapper.toDto(coupon);
                dto.setValid(utils.isCouponValid(coupon));
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CouponDto> getAllValidDateCoupons() {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> validCoupons = couponRepository.findAllValidDateCoupons(now);
        return validCoupons.stream()
                .map(coupon -> {
                    CouponDto dto = couponMapper.toDto(coupon);
                    dto.setValid(utils.isCouponValid(coupon));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
}
