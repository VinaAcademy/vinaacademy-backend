package com.vinaacademy.platform.feature.revenue.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vinaacademy.platform.exception.DuplicateVnpayTransactionException;
import com.vinaacademy.platform.feature.revenue.dto.CreateRevenueRecordRequest;
import com.vinaacademy.platform.feature.revenue.dto.RevenueDashboardDto;
import com.vinaacademy.platform.feature.revenue.entity.InstructorWallet;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;
import com.vinaacademy.platform.feature.revenue.entity.WalletTransaction;
import com.vinaacademy.platform.feature.revenue.enums.PayoutStatus;
import com.vinaacademy.platform.feature.revenue.enums.RevenueStatus;
import com.vinaacademy.platform.feature.revenue.enums.WalletTransactionType;
import com.vinaacademy.platform.feature.revenue.repository.InstructorWalletRepository;
import com.vinaacademy.platform.feature.revenue.repository.PayoutRequestRepository;
import com.vinaacademy.platform.feature.revenue.repository.RevenueRecordRepository;
import com.vinaacademy.platform.feature.revenue.repository.WalletTransactionRepository;
import com.vinaacademy.platform.feature.revenue.service.RevenueService;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j	
@Service
public class RevenueServiceImpl implements RevenueService {
	
	@Autowired
	private RevenueRecordRepository revenueRecordRepository;
	@Autowired
    private InstructorWalletRepository walletRepository;
	@Autowired
    private WalletTransactionRepository walletTransactionRepository;
	@Autowired
    private SecurityHelper securityHelper;
	@Autowired
	private PayoutRequestRepository payoutRequestRepository;

    RevenueServiceImpl(PayoutRequestRepository payoutRequestRepository) {
        this.payoutRequestRepository = payoutRequestRepository;
    }

	/**
     * Tạo bản ghi doanh thu khi học viên thanh toán thành công.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Kiểm tra trùng lặp giao dịch VNPAY bằng vnpayTxnRef.</li>
     *   <li>Tính toán chia sẻ doanh thu giữa giảng viên và nền tảng dựa trên phần trăm hoa hồng.</li>
     *   <li>Tạo bản ghi doanh thu (RevenueRecord) với trạng thái ACTIVE.</li>
     *   <li>Cập nhật ví giảng viên: cộng doanh thu vào balance và totalEarnings.</li>
     *   <li>Tạo bản ghi giao dịch ví loại EARNING.</li>
     * </ul>
     * @param request Thông tin tạo bản ghi doanh thu
     * @return RevenueRecord đã tạo
     * @throws DuplicateVnpayTransactionException nếu giao dịch VNPAY đã tồn tại
     */
	@Override
	@Transactional
	public RevenueRecord createRevenueRecord(CreateRevenueRecordRequest request) {
		log.info("Creating revenue record for vnpay transaction: {}", request.getVnpayTxnRef());
        // 1. Kiểm tra trùng lặp giao dịch VNPAY
        if (revenueRecordRepository.findByVnpayTxnRef(request.getVnpayTxnRef()).isPresent()) {
            throw new DuplicateVnpayTransactionException("VNPAY transaction already processed: " + request.getVnpayTxnRef());
        }
        // 2. Tính toán chia sẻ doanh thu
        BigDecimal instructorEarning = request.getTotalAmount()
            .multiply(request.getInstructorPercent())
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformFee = request.getTotalAmount().subtract(instructorEarning);
        // 3. Tạo bản ghi doanh thu
        RevenueRecord revenueRecord = RevenueRecord.builder()
            .courseId(request.getCourseId())
            .enrollmentId(request.getEnrollmentId())
            .paymentId(request.getPaymentId())
            .instructorId(request.getInstructorId())
            .studentId(request.getStudentId())
            .totalAmount(request.getTotalAmount())
            .instructorEarning(instructorEarning)
            .platformFee(platformFee)
            .instructorPercent(request.getInstructorPercent())
            .vnpayTxnRef(request.getVnpayTxnRef())
            .vnpayResponseCode(request.getVnpayResponseCode())
            .vnpayTransactionNo(request.getVnpayTransactionNo())
            .vnpayOrderInfo(request.getVnpayOrderInfo())
            .vnpayAmount(request.getVnpayAmount())
            .status(RevenueStatus.ACTIVE)
            .build();
        RevenueRecord savedRecord = revenueRecordRepository.save(revenueRecord);
        // 4. Cập nhật ví giảng viên
        updateInstructorWallet(request.getInstructorId(), instructorEarning, savedRecord.getId());
        log.info("Revenue record created successfully. ID: {}, Instructor earning: {}", 
                savedRecord.getId(), instructorEarning);
        return savedRecord;
	}

	/**
     * Cập nhật ví giảng viên khi phát sinh doanh thu mới.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Tìm ví giảng viên theo instructorId (nếu chưa có thì khởi tạo mới).</li>
     *   <li>Cộng earning vào balance và totalEarnings.</li>
     *   <li>Lưu ví đã cập nhật.</li>
     *   <li>Tạo bản ghi giao dịch ví (WalletTransaction) loại EARNING.</li>
     *   <li>Lưu bản ghi giao dịch.</li>
     * </ul>
     * @param instructorId   Mã giảng viên
     * @param earning        Số tiền doanh thu nhận được
     * @param revenueRecordId Mã bản ghi doanh thu liên quan
     */
	@Override
	public void updateInstructorWallet(UUID instructorId, BigDecimal earning, Long revenueRecordId) {
		// 1. Tìm ví giảng viên hoặc khởi tạo mới nếu chưa có
		InstructorWallet wallet = walletRepository.findByInstructorId(instructorId)
	            .orElseGet(() -> InstructorWallet.builder()
	                .instructorId(instructorId)
	                .balance(BigDecimal.ZERO)
	                .totalEarnings(BigDecimal.ZERO)
	                .totalWithdrawn(BigDecimal.ZERO)
	                .pendingWithdraw(BigDecimal.ZERO)
	                .build());
	        // 2. Cập nhật số dư và tổng doanh thu
	        BigDecimal oldBalance = wallet.getBalance();
	        wallet.setBalance(wallet.getBalance().add(earning));
	        wallet.setTotalEarnings(wallet.getTotalEarnings().add(earning));
	        // 3. Lưu ví đã cập nhật
	        InstructorWallet savedWallet = walletRepository.save(wallet);
	        // 4. Tạo bản ghi giao dịch ví
	        WalletTransaction transaction = WalletTransaction.builder()
	            .instructorId(instructorId)
	            .type(WalletTransactionType.EARNING)
	            .amount(earning)
	            .balanceAfter(savedWallet.getBalance())
	            .referenceId(revenueRecordId)
	            .referenceType("REVENUE")
	            .description("Earning from course sale - Revenue ID: " + revenueRecordId)
	            .build();
	        // 5. Lưu bản ghi giao dịch
	        walletTransactionRepository.save(transaction);
	        log.info("Instructor wallet updated. ID: {}, Old balance: {}, New balance: {}", 
	                instructorId, oldBalance, savedWallet.getBalance());
	}

	/**
     * Xử lý hoàn tiền: trừ doanh thu khỏi ví giảng viên và cập nhật các bản ghi liên quan.
     * <p>
     * Quy trình:
     * <ul>
     *   <li>Tìm bản ghi doanh thu theo vnpayTxnRef.</li>
     *   <li>Kiểm tra trạng thái đã hoàn tiền chưa.</li>
     *   <li>Cập nhật trạng thái bản ghi doanh thu sang REFUNDED, lưu lý do hoàn tiền.</li>
     *   <li>Lưu lại bản ghi doanh thu đã cập nhật.</li>
     *   <li>Tìm ví giảng viên liên quan.</li>
     *   <li>Trừ số tiền earning khỏi balance và totalEarnings của ví.</li>
     *   <li>Lưu lại ví đã cập nhật.</li>
     *   <li>Tạo bản ghi giao dịch ví với số tiền âm (loại REFUND).</li>
     *   <li>Lưu bản ghi giao dịch hoàn tiền.</li>
     * </ul>
     * @param vnpayTxnRef   Mã giao dịch VNPAY
     * @param refundReason  Lý do hoàn tiền
     * @throws RuntimeException nếu không tìm thấy bản ghi doanh thu hoặc ví giảng viên
     */
	@Override
	public void processRefund(String vnpayTxnRef, String refundReason) {
		log.info("Processing refund for transaction: {}", vnpayTxnRef);
        // 1. Tìm bản ghi doanh thu
        RevenueRecord revenueRecord = revenueRecordRepository.findByVnpayTxnRef(vnpayTxnRef)
            .orElseThrow(() -> new RuntimeException("Revenue record not found: " + vnpayTxnRef));
        // 2. Kiểm tra trạng thái hoàn tiền
        if (revenueRecord.getStatus() == RevenueStatus.REFUNDED) {
            throw new RuntimeException("Transaction already refunded: " + vnpayTxnRef);
        }
        // 3. Cập nhật trạng thái và lý do hoàn tiền
        revenueRecord.setStatus(RevenueStatus.REFUNDED);
        revenueRecord.setReason(refundReason);
        // 4. Lưu bản ghi doanh thu
        revenueRecordRepository.save(revenueRecord);
        // 5. Tìm ví giảng viên
        InstructorWallet wallet = walletRepository.findByInstructorId(revenueRecord.getInstructorId())
            .orElseThrow(() -> new RuntimeException("Instructor wallet not found"));
        // 6. Trừ số tiền earning khỏi ví
        BigDecimal refundAmount = revenueRecord.getInstructorEarning();
        wallet.setBalance(wallet.getBalance().subtract(refundAmount));
        wallet.setTotalEarnings(wallet.getTotalEarnings().subtract(refundAmount));
        // 7. Lưu ví đã cập nhật
        InstructorWallet savedWallet = walletRepository.save(wallet);
        // 8. Tạo bản ghi giao dịch hoàn tiền
        WalletTransaction transaction = WalletTransaction.builder()
            .instructorId(revenueRecord.getInstructorId())
            .type(WalletTransactionType.REFUND)
            .amount(refundAmount.negate()) // Negative amount
            .balanceAfter(savedWallet.getBalance())
            .referenceId(revenueRecord.getId())
            .referenceType("REFUND")
            .description("Refund deduction - Revenue ID: " + revenueRecord.getId())
            .build();
        // 9. Lưu bản ghi giao dịch hoàn tiền
        walletTransactionRepository.save(transaction);
        log.info("Refund processed successfully. Amount: {}, New balance: {}", 
                refundAmount, savedWallet.getBalance());
	}

	/**
     * Kiểm tra quyền truy cập tài nguyên ví/yêu cầu rút tiền của giảng viên.
     * <p>
     * - Admin/Staff: truy cập toàn bộ.
     * - Instructor: chỉ truy cập tài nguyên của chính mình.
     * - Nếu không đủ quyền sẽ ném RuntimeException.
     * </p>
     * @param instructorId ID giảng viên cần kiểm tra quyền truy cập
     * @throws RuntimeException nếu không đủ quyền
     */
	private void checkAccess(UUID instructorId) {
		if (securityHelper.hasRole(AuthConstants.ADMIN_ROLE) || securityHelper.hasRole(AuthConstants.STAFF_ROLE)) {
			return; // Admin/Staff: full access
		}
		if (securityHelper.hasRole(AuthConstants.INSTRUCTOR_ROLE)) {
			return; // Instructor: self access
		}
		throw new RuntimeException("You do not have permission to access this resource");
	}

	/**
     * Lấy danh sách doanh thu của giảng viên (có phân trang).
     * <p>
     * Chỉ cho phép admin, staff hoặc chính instructor truy cập.
     * <ul>
     *   <li>Kiểm tra quyền truy cập bằng checkAccess.</li>
     *   <li>Lấy danh sách doanh thu từ repository theo instructorId và pageable.</li>
     * </ul>
     * @param instructorId Mã giảng viên
     * @param pageable     Thông tin phân trang
     * @return Page<RevenueRecord> danh sách doanh thu
     */
	@Override
	public Page<RevenueRecord> getInstructorRevenue(Pageable pageable) {
		var user = securityHelper.getCurrentUser();
		checkAccess(user.getId());
		return revenueRecordRepository.findByInstructorId(user.getId(), pageable);
	}
	
	/**
     * Lấy thống kê dashboard cho admin.
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Lấy tổng doanh thu, phí nền tảng, thu nhập giảng viên, số lượng payout pending/completed, số instructor, số giao dịch.</li>
     *   <li>Sử dụng các phương thức private để tổng hợp dữ liệu từ repository.</li>
     *   <li>Trả về đối tượng RevenueDashboardDto tổng hợp.</li>
     * </ul>
     * @return RevenueDashboardDto thống kê dashboard
     */
    @Override
    public RevenueDashboardDto getDashboardStats() {
        // Implementation sử dụng repository queries
        return RevenueDashboardDto.builder()
            .totalRevenue(getTotalRevenue())
            .totalPlatformFee(getTotalPlatformFee())
            .totalInstructorEarnings(getTotalInstructorEarnings())
            .totalPendingPayouts(getTotalPendingPayouts())
            .totalCompletedPayouts(getTotalCompletedPayouts())
            .pendingPayoutRequests(getPendingPayoutRequestsCount())
            .totalInstructors(getTotalInstructorsCount())
            .totalTransactions(getTotalTransactionsCount())
            .build();
    }

    /**
     * Lấy tổng doanh thu từ các bản ghi doanh thu trạng thái ACTIVE.
     * @return Tổng doanh thu
     */
	private BigDecimal getTotalRevenue() {
        // Query sum of all totalAmount from revenue_records where status = ACTIVE
        return revenueRecordRepository.findAll().stream()
            .filter(r -> r.getStatus() == RevenueStatus.ACTIVE)
            .map(RevenueRecord::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    /**
     * Lấy tổng phí nền tảng từ các bản ghi doanh thu trạng thái ACTIVE.
     * @return Tổng phí nền tảng
     */
    private BigDecimal getTotalPlatformFee() {
        return revenueRecordRepository.findAll().stream()
            .filter(r -> r.getStatus() == RevenueStatus.ACTIVE)
            .map(RevenueRecord::getPlatformFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    /**
     * Lấy tổng thu nhập giảng viên từ các bản ghi doanh thu trạng thái ACTIVE.
     * @return Tổng thu nhập giảng viên
     */
    private BigDecimal getTotalInstructorEarnings() {
        return revenueRecordRepository.findAll().stream()
            .filter(r -> r.getStatus() == RevenueStatus.ACTIVE)
            .map(RevenueRecord::getInstructorEarning)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    /**
     * Lấy tổng số tiền payout đang chờ xử lý.
     * @return Tổng số tiền payout pending
     */
    private BigDecimal getTotalPendingPayouts() {
        return walletRepository.findAll().stream()
            .map(InstructorWallet::getPendingWithdraw)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    /**
     * Lấy tổng số tiền payout đã hoàn thành.
     * @return Tổng số tiền payout completed
     */
    private BigDecimal getTotalCompletedPayouts() {
        return walletRepository.findAll().stream()
            .map(InstructorWallet::getTotalWithdrawn)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    /**
     * Lấy số lượng yêu cầu payout đang chờ xử lý.
     * @return Số lượng payout request pending
     */
    private Long getPendingPayoutRequestsCount() {
        return payoutRequestRepository.findAll().stream()
            .filter(r -> r.getStatus() == PayoutStatus.PENDING)
            .count();
    }
    /**
     * Lấy tổng số instructor có ví.
     * @return Số lượng instructor
     */
    private Long getTotalInstructorsCount() {
        return walletRepository.count();
    }
    /**
     * Lấy tổng số giao dịch doanh thu.
     * @return Số lượng giao dịch
     */
    private Long getTotalTransactionsCount() {
        return revenueRecordRepository.count();
    }
}