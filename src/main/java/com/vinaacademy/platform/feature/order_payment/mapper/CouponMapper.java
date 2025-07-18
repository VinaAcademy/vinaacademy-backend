package com.vinaacademy.platform.feature.order_payment.mapper;

import com.vinaacademy.platform.feature.order_payment.dto.CouponDto;
import com.vinaacademy.platform.feature.order_payment.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CouponMapper {

    CouponDto toDto(Coupon coupon);

    Coupon toEntity(CouponDto couponDTO);

    List<CouponDto> toDtoList(List<Coupon> coupons);

//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "carts", ignore = true)
//    @Mapping(target = "createdDate", ignore = true)
//    @Mapping(target = "updatedDate", ignore = true)
//    void updateCouponFromDto(CouponDto couponDTO, @MappingTarget Coupon coupon);
}