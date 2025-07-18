package com.vinaacademy.platform.feature.order_payment;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.order_payment.dto.CouponDto;
import com.vinaacademy.platform.feature.order_payment.service.CouponService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupon")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CouponController {
	private final CouponService couponService;
	
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE, AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE})
    @GetMapping("/list")
    public ApiResponse<List<CouponDto>> getValidDateCoupons() {
        List<CouponDto> validCoupons = couponService.getAllValidDateCoupons();
        return ApiResponse.success(validCoupons);
    }
    
}
