package com.vinaacademy.platform.feature.order_payment.entity;

import com.vinaacademy.platform.feature.cart.entity.Cart;
import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.order_payment.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "coupons")
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "usage_limit")
    private Long usageLimit ;

    @Column(name = "used_count")
    private Long usedCount = 0L;

    @OneToMany(mappedBy = "coupon")
    private List<Cart> carts;
    // private Order orders;

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                '}';
    }
}
