package com.vinaacademy.platform.feature.revenue.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vinaacademy.platform.feature.revenue.entity.PayoutRequest;
import com.vinaacademy.platform.feature.revenue.enums.PayoutStatus;

public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, Long> {

	/**
     * Lấy danh sách yêu cầu rút tiền của giảng viên, sắp xếp theo ngày tạo giảm dần.
     *
     * @param instructorId ID của giảng viên
     * @param pageable     Thông tin phân trang
     * @return Trang các yêu cầu rút tiền
     */
    Page<PayoutRequest> findByInstructorIdOrderByCreatedDateDesc(UUID instructorId, Pageable pageable);

    /**
     * Lấy danh sách yêu cầu rút tiền theo trạng thái, sắp xếp theo ngày tạo giảm dần.
     *
     * @param status   Trạng thái yêu cầu rút tiền
     * @param pageable Thông tin phân trang
     * @return Trang các yêu cầu rút tiền theo trạng thái
     */
    Page<PayoutRequest> findByStatusOrderByCreatedDateDesc(PayoutStatus status, Pageable pageable);

    /**
     * Đếm số lượng yêu cầu rút tiền đang chờ xử lý của giảng viên.
     *
     * @param instructorId ID của giảng viên
     * @return Số lượng yêu cầu rút tiền ở trạng thái PENDING, REVIEWING, APPROVED
     */
    @Query("SELECT COUNT(p) FROM PayoutRequest p WHERE p.instructorId = :instructorId AND p.status IN ('PENDING', 'REVIEWING', 'APPROVED')")
    long countPendingRequestsByInstructor(@Param("instructorId") UUID instructorId);
    
}
