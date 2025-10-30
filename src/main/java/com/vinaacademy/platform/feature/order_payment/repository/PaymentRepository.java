package com.vinaacademy.platform.feature.order_payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vinaacademy.platform.feature.order_payment.entity.Payment;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;

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
	
	//Kiểm tra nếu payment = status pending và ngày tạo <= ngày hạn(ngày hạn = ngày hiện tại - thgian hạn)
	// ví dụ hạn là 1 ngày và bây giờ là 16/6 thì cutoff sẽ là 16-1 = 15/6 thì nếu ngày tạo <= 15/6 thì hết hạn
	@Modifying
	@Query("""
	    UPDATE Payment p
	    SET p.paymentStatus = :cancelled
	    WHERE p.paymentStatus = :pending
	      AND p.createdAt <= :cutoff
	""")
	int cancelExpiredPendingPayments(@Param("cancelled") PaymentStatus cancelled,
	                                 @Param("pending")   PaymentStatus pending,
	                                 @Param("cutoff")    LocalDateTime cutoff);

}
