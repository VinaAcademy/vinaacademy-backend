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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OrderStatusScheduler {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 120000) // mỗi 2p
    @Transactional
    public void checkPendingOrders() {
    	LocalDateTime cutoffPayment = LocalDateTime.now().minusMinutes(15); //15p truoc
        LocalDateTime cutoffOrder   = LocalDateTime.now().minusMinutes(90); //90p truoc

        int cancelled = paymentRepository.cancelExpiredPendingPayments(
                PaymentStatus.CANCELLED,
                PaymentStatus.PENDING,
                cutoffPayment); 

        int failed = orderRepository.failOldUnpaidOrders(
                OrderStatus.FAILED,
                OrderStatus.PENDING,
                cutoffOrder);

        log.info("Scheduler: {} payments cancelled, {} orders failed", cancelled, failed);
    }
}
