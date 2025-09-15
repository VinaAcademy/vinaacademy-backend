package com.vinaacademy.platform.feature.revenue.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;

@Repository
public interface RevenueRecordRepository extends JpaRepository<RevenueRecord, Long> {

	/**
     * Lấy danh sách bản ghi doanh thu của giảng viên, sắp xếp theo ngày tạo giảm dần.
     *
     * @param instructorId ID của giảng viên
     * @return Danh sách RevenueRecord
     */
    List<RevenueRecord> findByInstructorIdOrderByCreatedDateDesc(UUID instructorId);

    /**
     * Tính tổng thu nhập của giảng viên với các bản ghi ở trạng thái ACTIVE.
     *
     * @param instructorId ID của giảng viên
     * @return Tổng thu nhập (BigDecimal)
     */
    @Query("SELECT SUM(r.instructorEarning) FROM RevenueRecord r WHERE r.instructorId = :instructorId AND r.status = 'ACTIVE'")
    BigDecimal getTotalEarningsByInstructor(@Param("instructorId") UUID instructorId);

    /**
     * Tìm bản ghi doanh thu theo mã giao dịch VNPay.
     *
     * @param vnpayTxnRef Mã giao dịch VNPay
     * @return Optional chứa RevenueRecord nếu tìm thấy
     */
    Optional<RevenueRecord> findByVnpayTxnRef(String vnpayTxnRef);

    /**
     * Lấy danh sách bản ghi doanh thu của giảng viên, hỗ trợ phân trang.
     *
     * @param instructorId ID của giảng viên
     * @param pageable     Thông tin phân trang
     * @return Trang các RevenueRecord
     */
    Page<RevenueRecord> findByInstructorId(UUID instructorId, Pageable pageable);

    /**
     * Tìm bản ghi doanh thu theo paymentId, instructorId, courseId.
     */
    Optional<RevenueRecord> findByPaymentIdAndInstructorIdAndCourseId(UUID paymentId, UUID instructorId, UUID courseId);

}