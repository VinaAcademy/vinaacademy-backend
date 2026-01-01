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

	// ==================== Admin Dashboard Queries ====================
	
	/**
	 * Tính tổng revenue từ completed orders sau một thời điểm
	 * Dùng cho dashboard stats
	 */
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
	       "WHERE o.payment.paymentStatus = 'COMPLETED' " +
	       "AND o.createdDate >= :startDate")
	java.math.BigDecimal getTotalRevenueSince(@Param("startDate") LocalDateTime startDate);
	
	/**
	 * Tính tổng revenue trong khoảng thời gian
	 * Dùng để so sánh với kỳ trước
	 */
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
	       "WHERE o.payment.paymentStatus = 'COMPLETED' " +
	       "AND o.createdDate >= :startDate AND o.createdDate < :endDate")
	java.math.BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate,
	                                            @Param("endDate") LocalDateTime endDate);
	
	/**
	 * Lấy monthly revenue data cho 12 tháng
	 * Returns: [year-month, revenue, course_count]
	 */
    @Query("SELECT TO_CHAR(o.createdDate, 'YYYY-MM') as month, " +
	       "COALESCE(SUM(o.totalAmount), 0) as revenue, " +
	       "COUNT(DISTINCT o.id) as orderCount " +
	       "FROM Order o " +
	       "WHERE o.payment.paymentStatus = 'COMPLETED' " +
	       "AND o.createdDate >= :startDate " +
	       "GROUP BY TO_CHAR(o.createdDate, 'YYYY-MM') " +
	       "ORDER BY TO_CHAR(o.createdDate, 'YYYY-MM')")
	List<Object[]> getMonthlyRevenue(@Param("startDate") LocalDateTime startDate);
	
	/**
	 * Lấy revenue distribution theo category
	 * Returns: [category_name, total_revenue]
	 */
	@Query("SELECT c.name, COALESCE(SUM(oi.price), 0) " +
	       "FROM Order o " +
	       "JOIN o.orderItems oi " +
	       "JOIN oi.course co " +
	       "JOIN co.category c " +
	       "WHERE o.payment.paymentStatus = 'COMPLETED' " +
	       "GROUP BY c.id, c.name " +
	       "ORDER BY SUM(oi.price) DESC")
	List<Object[]> getRevenueByCategory();
	
	/**
	 * Tính average order value
	 */
	@Query("SELECT AVG(o.totalAmount) FROM Order o " +
	       "WHERE o.payment.paymentStatus = 'COMPLETED' " +
	       "AND o.createdDate >= :startDate")
	java.math.BigDecimal getAverageOrderValue(@Param("startDate") LocalDateTime startDate);

}

