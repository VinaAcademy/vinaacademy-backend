package com.vinaacademy.platform.feature.revenue.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vinaacademy.platform.feature.revenue.entity.InstructorWallet;


@Repository
public interface InstructorWalletRepository extends JpaRepository<InstructorWallet, UUID> {

	/**
     * Tìm ví của giảng viên theo ID giảng viên.
     *
     * @param instructorId ID của giảng viên
     * @return Optional chứa InstructorWallet nếu tìm thấy, ngược lại là Optional rỗng
     */
    Optional<InstructorWallet> findByInstructorId(UUID instructorId);

    /**
     * Cập nhật số dư ví của giảng viên.
     *
     * @param instructorId ID của giảng viên
     * @param balance      Số dư mới cần cập nhật
     */
    @Modifying
    @Query("UPDATE InstructorWallet iw SET iw.balance = :balance WHERE iw.instructorId = :instructorId")
    void updateBalance(@Param("instructorId") UUID instructorId, @Param("balance") BigDecimal balance);

    /**
     * Cập nhật tổng thu nhập của giảng viên.
     *
     * @param instructorId  ID của giảng viên
     * @param totalEarnings Tổng thu nhập mới cần cập nhật
     */
    @Modifying
    @Query("UPDATE InstructorWallet iw SET iw.totalEarnings = :totalEarnings WHERE iw.instructorId = :instructorId")
    void updateTotalEarnings(@Param("instructorId") UUID instructorId, @Param("totalEarnings") BigDecimal totalEarnings);

    /**
     * Lấy danh sách ví giảng viên có số dư lớn hơn 0, hỗ trợ phân trang.
     *
     * @param pageable Thông tin phân trang
     * @return Trang các InstructorWallet có số dư > 0
     */
    @Query("SELECT w FROM InstructorWallet w WHERE w.balance > 0")
    Page<InstructorWallet> findWalletsWithBalance(Pageable pageable);

}
