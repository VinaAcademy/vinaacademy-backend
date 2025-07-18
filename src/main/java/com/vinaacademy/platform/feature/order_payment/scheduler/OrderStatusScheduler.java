package com.vinaacademy.platform.feature.order_payment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vinaacademy.platform.feature.order_payment.entity.Order;
import com.vinaacademy.platform.feature.order_payment.entity.Payment;
import com.vinaacademy.platform.feature.order_payment.enums.OrderStatus;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;
import com.vinaacademy.platform.feature.order_payment.repository.OrderRepository;
import com.vinaacademy.platform.feature.order_payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OrderStatusScheduler {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 15000) // mỗi 15s
    public void checkPendingOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(15);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List <Payment> pendingPayments = paymentRepository.findPendingPaymentsCreatedBefore(PaymentStatus.PENDING, cutoffTime);
        for (Payment pay : pendingPayments) {
        	pay.setPaymentStatus(PaymentStatus.CANCELLED);
        	pay.getOrder().setStatus(OrderStatus.FAILED);
        	paymentRepository.save(pay);
        }
        
        List <Order> pendingOrderNullPayment = orderRepository.findUnpaidPendingOrdersUpdatedBefore(OrderStatus.PENDING, oneDayAgo);
        for (Order order : pendingOrderNullPayment) {
        	orderRepository.delete(order);
        }
    
    }
}
