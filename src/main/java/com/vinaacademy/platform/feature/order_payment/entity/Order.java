package com.vinaacademy.platform.feature.order_payment.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.order_payment.discount.DiscountStrategy;
import com.vinaacademy.platform.feature.order_payment.discount.DiscountStrategyFactory;
import com.vinaacademy.platform.feature.order_payment.enums.DiscountType;
import com.vinaacademy.platform.feature.order_payment.enums.OrderStatus;
import com.vinaacademy.platform.feature.order_payment.utils.Utils;
import com.vinaacademy.platform.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    
    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private Payment payment;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    
    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }
    
    @PrePersist
    @PreUpdate
    public void calculateAmounts() {
        if (orderItems != null && !orderItems.isEmpty()) {
            this.subTotal = orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else 
            this.subTotal = BigDecimal.ZERO;
        
        
        if (coupon != null && Utils.isCouponValid(coupon)) {
            if (coupon.getMinOrderValue() != null && this.subTotal.compareTo(coupon.getMinOrderValue()) < 0) {
                this.discountAmount = BigDecimal.ZERO;
            } else {
            	DiscountStrategy strategy = DiscountStrategyFactory.getStrategy(coupon.getDiscountType());
                this.discountAmount = strategy.calculateDiscount(this.subTotal, coupon);
            }
        } else 
            this.discountAmount = BigDecimal.ZERO;
        
        
        this.totalAmount = this.subTotal.subtract(this.discountAmount);
    }
    
    
}