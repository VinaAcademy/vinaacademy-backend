package com.vinaacademy.platform.feature.order_payment.repository;

import com.vinaacademy.platform.feature.order_payment.entity.Order;
import com.vinaacademy.platform.feature.order_payment.enums.OrderStatus;
import com.vinaacademy.platform.feature.order_payment.enums.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	Optional<Order> findFirstByUser_IdAndOrderItems_Course_IdAndStatusOrderByCreatedDateAsc(UUID userId, UUID courseId,
			OrderStatus status);

	Page<Order> findByUserId(UUID userId, Pageable pageable);

//	@Query("SELECT o FROM Order o " + "WHERE o.status = :status " + "AND o.payment IS NULL "
//			+ "AND o.updatedDate <= :cutoff")
//	List<Order> findUnpaidPendingOrdersUpdatedBefore(@Param("status") OrderStatus status,
//			@Param("cutoff") LocalDateTime cutoff);
	
	//Kiểm tra nếu order = status pending và ngày tạo <= ngày hạn(ngày hạn = ngày hiện tại - thgian hạn)
		// ví dụ hạn là 1 ngày và bây giờ là 16/6 thì cutoff sẽ là 16-1 = 15/6 thì nếu ngày tạo <= 15/6 thì hết hạn
	@Modifying
	@Query("""
	    UPDATE Order o
	    SET o.status = :failed
	    WHERE o.status = :pending
	      AND o.payment IS NULL
	      AND o.updatedDate <= :cutoff
	""")
	int failOldUnpaidOrders(@Param("failed")  OrderStatus failed,
	                        @Param("pending") OrderStatus pending,
	                        @Param("cutoff")  LocalDateTime cutoff);


}
