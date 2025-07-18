package com.vinaacademy.platform.feature.order_payment.repository;

import com.vinaacademy.platform.feature.order_payment.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Optional<Coupon> findByCode(String code);
    
    
    @Query("SELECT c FROM Coupon c WHERE c.code = :code " +
           "AND c.startedAt <= :currentDateTime " +
           "AND c.expiredAt > :currentDateTime " +
           "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    Optional<Coupon> findValidCouponByCode(@Param("code") String code, 
                                          @Param("currentDateTime") LocalDateTime currentDateTime);
    
    
    @Query("SELECT c FROM Coupon c WHERE " +
           "c.startedAt <= :currentDateTime " +
           "AND c.expiredAt > :currentDateTime " +
           "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findAllValidCoupons(@Param("currentDateTime") LocalDateTime currentDateTime);
    
    @Query("SELECT c FROM Coupon c WHERE " +
            "c.startedAt <= :currentDateTime " +
            "AND c.expiredAt > :currentDateTime ")
    List<Coupon> findAllValidDateCoupons(@Param("currentDateTime") LocalDateTime currentDateTime);
    
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Coupon c " +
           "WHERE c.id = :id " +
           "AND c.startedAt <= :currentDateTime " +
           "AND c.expiredAt > :currentDateTime " +
           "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    boolean isCouponValid(@Param("id") UUID id, @Param("currentDateTime") LocalDateTime currentDateTime);
}