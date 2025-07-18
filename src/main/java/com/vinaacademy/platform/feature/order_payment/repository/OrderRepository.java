package com.vinaacademy.platform.feature.order_payment.repository;

import com.vinaacademy.platform.feature.order_payment.entity.Order;
import com.vinaacademy.platform.feature.order_payment.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

	@Query("SELECT o FROM Order o " + "WHERE o.status = :status " + "AND o.payment IS NULL "
			+ "AND o.updatedDate <= :cutoff")
	List<Order> findUnpaidPendingOrdersUpdatedBefore(@Param("status") OrderStatus status,
			@Param("cutoff") LocalDateTime cutoff);

}
