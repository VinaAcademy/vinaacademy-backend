package com.vinaacademy.platform.feature.revenue.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vinaacademy.platform.feature.revenue.entity.PayoutTransaction;

public interface PayoutTransactionRepository extends JpaRepository<PayoutTransaction, Long> {

	/**
	 * Tìm các giao dịch rút tiền của một giảng viên, sắp xếp theo ngày tạo giảm dần.
	 * <p>
	 * Luồng hoạt động:
	 * <ul>
	 *   <li>Nhận vào instructorId và thông tin phân trang (Pageable).</li>
	 *   <li>Thực hiện truy vấn cơ sở dữ liệu để lấy các bản ghi PayoutTransaction có instructorId tương ứng.</li>
	 *   <li>Kết quả được sắp xếp theo trường createdDate giảm dần.</li>
	 *   <li>Trả về kết quả dưới dạng Page để hỗ trợ phân trang.</li>
	 * </ul>
	 *
	 * @param instructorId UUID của giảng viên cần truy vấn giao dịch rút tiền
	 * @param pageable     Thông tin phân trang (số trang, kích thước trang, sắp xếp)
	 * @return Page<PayoutTransaction> danh sách giao dịch rút tiền của giảng viên theo phân trang
	 */
	Page<PayoutTransaction> findByInstructorIdOrderByCreatedDateDesc(UUID instructorId, Pageable pageable);

}