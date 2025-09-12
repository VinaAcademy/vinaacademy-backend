package com.vinaacademy.platform.feature.revenue.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vinaacademy.platform.feature.revenue.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
	
	 /**
     * Lấy danh sách giao dịch ví của giảng viên, sắp xếp theo ngày tạo giảm dần.
     *
     * @param instructorId ID của giảng viên
     * @param pageable     Thông tin phân trang
     * @return Trang các WalletTransaction
     */
    Page<WalletTransaction> findByInstructorIdOrderByCreatedDateDesc(UUID instructorId, Pageable pageable);

    /**
     * Tìm các giao dịch ví theo referenceId và referenceType.
     *
     * @param referenceId   ID tham chiếu
     * @param referenceType Loại tham chiếu
     * @return Danh sách WalletTransaction phù hợp
     */
    List<WalletTransaction> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);

}
