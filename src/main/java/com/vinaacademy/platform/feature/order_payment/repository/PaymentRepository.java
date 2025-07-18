package com.vinaacademy.platform.feature.order_payment.repository;

import com.vinaacademy.platform.feature.order_payment.entity.Payment;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

	@Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId")
	List<Payment> findAllByUserId(@Param("userId") UUID userId);

	@Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId ORDER BY p.createdAt DESC")
	List<Payment> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

	@Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId AND p.paymentStatus = :status")
	List<Payment> findAllByUserIdAndPaymentStatus(@Param("userId") UUID userId, @Param("status") String status);
	
	Optional<Payment> findByTransactionId(String transactionId);
	
	Optional<Payment> findByOrderId(UUID uuid);
	
	@Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status AND p.createdAt <= :cutoff")
    List<Payment> findPendingPaymentsCreatedBefore(
        @Param("status") PaymentStatus status,
        @Param("cutoff") LocalDateTime cutoff
    );
}
