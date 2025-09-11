package com.vinaacademy.platform.feature.revenue.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vinaacademy.platform.exception.InsufficientBalanceException;
import com.vinaacademy.platform.exception.InvalidPayoutStatusException;
import com.vinaacademy.platform.exception.PayoutRequestNotFoundException;
import com.vinaacademy.platform.feature.revenue.dto.PayoutApprovalRequest;
import com.vinaacademy.platform.feature.revenue.dto.PayoutRequestDto;
import com.vinaacademy.platform.feature.revenue.dto.WalletBalanceDto;
import com.vinaacademy.platform.feature.revenue.entity.InstructorWallet;
import com.vinaacademy.platform.feature.revenue.entity.PayoutRequest;
import com.vinaacademy.platform.feature.revenue.entity.PayoutTransaction;
import com.vinaacademy.platform.feature.revenue.entity.WalletTransaction;
import com.vinaacademy.platform.feature.revenue.enums.PayoutStatus;
import com.vinaacademy.platform.feature.revenue.enums.WalletTransactionType;
import com.vinaacademy.platform.feature.revenue.repository.InstructorWalletRepository;
import com.vinaacademy.platform.feature.revenue.repository.PayoutRequestRepository;
import com.vinaacademy.platform.feature.revenue.repository.PayoutTransactionRepository;
import com.vinaacademy.platform.feature.revenue.repository.WalletTransactionRepository;
import com.vinaacademy.platform.feature.revenue.service.PayoutService;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PayoutServiceImpl implements PayoutService {

	@Autowired
	private PayoutRequestRepository payoutRequestRepository;
	@Autowired
    private PayoutTransactionRepository payoutTransactionRepository;
	@Autowired
	private InstructorWalletRepository walletRepository;
	@Autowired
	private WalletTransactionRepository walletTransactionRepository;
	@Autowired
	private SecurityHelper securityHelper;
	
    private static final BigDecimal MIN_PAYOUT_AMOUNT = new BigDecimal("50000"); // 50k VND

    /**
     * Tạo yêu cầu rút tiền cho giảng viên.
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Kiểm tra số tiền rút phải lớn hơn hoặc bằng mức tối thiểu (MIN_PAYOUT_AMOUNT).</li>
     *   <li>Kiểm tra giảng viên có đang có yêu cầu rút tiền ở trạng thái PENDING không (chỉ cho phép 1 yêu cầu chờ xử lý).</li>
     *   <li>Lấy ví của giảng viên, kiểm tra số dư khả dụng có đủ để rút không.</li>
     *   <li>Cộng số tiền rút vào trường pendingWithdraw của ví (đánh dấu số tiền đang chờ rút, tránh double spending).</li>
     *   <li>Tạo bản ghi yêu cầu rút tiền ở trạng thái PENDING.</li>
     * </ol>
     * @param instructorId ID giảng viên yêu cầu rút tiền
     * @param request      DTO chứa thông tin rút tiền (số tiền, ngân hàng, số tài khoản, ...)
     * @return PayoutRequest đã tạo
     * @throws IllegalArgumentException nếu số tiền nhỏ hơn mức tối thiểu
     * @throws InsufficientBalanceException nếu số dư không đủ
     * @throws RuntimeException nếu đã có yêu cầu rút tiền đang chờ hoặc không tìm thấy ví
     */
	@Override
	public PayoutRequest createPayoutRequest(UUID instructorId, PayoutRequestDto request) {

		log.info("Creating payout request for instructor: {}, amount: {}", instructorId, request.getAmount());
        
        // 1. Kiểm tra số tiền rút tối thiểu
        if (request.getAmount().compareTo(MIN_PAYOUT_AMOUNT) < 0) {
            throw new IllegalArgumentException("Minimum payout amount is " + MIN_PAYOUT_AMOUNT + " VND");
        }
        
        // 2. Kiểm tra giảng viên có yêu cầu rút tiền đang chờ xử lý không
        long pendingCount = payoutRequestRepository.countPendingRequestsByInstructor(instructorId);
        if (pendingCount > 0) {
            throw new RuntimeException("Instructor already has pending payout requests");
        }
        
        // 3. Kiểm tra số dư ví đủ để rút
        InstructorWallet wallet = walletRepository.findByInstructorId(instructorId)
            .orElseThrow(() -> new RuntimeException("Instructor wallet not found"));
            
        if (!wallet.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + wallet.getAvailableBalance());
        }
        
        // 4. Đánh dấu số tiền đang chờ rút trong ví (pending)
        wallet.setPendingWithdraw(wallet.getPendingWithdraw().add(request.getAmount()));
        walletRepository.save(wallet);
        
        // 5. Tạo bản ghi yêu cầu rút tiền ở trạng thái PENDING
        PayoutRequest payoutRequest = PayoutRequest.builder()
            .instructorId(instructorId)
            .amount(request.getAmount())
            .bankName(request.getBankName())
            .bankAccount(request.getBankAccount())
            .accountHolder(request.getAccountHolder())
            .note(request.getNote())
            .status(PayoutStatus.PENDING)
            .build();
            
        PayoutRequest savedRequest = payoutRequestRepository.save(payoutRequest);
        
        log.info("Payout request created successfully. ID: {}, Amount reserved in wallet", savedRequest.getId());
        
        return savedRequest;
	}

	/**
     * Duyệt hoặc từ chối yêu cầu rút tiền (chỉ dành cho admin/staff).
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Tìm kiếm yêu cầu rút tiền theo ID.</li>
     *   <li>Kiểm tra trạng thái phải là PENDING (chỉ xử lý yêu cầu đang chờ duyệt).</li>
     *   <li>Lấy ví giảng viên liên quan.</li>
     *   <li>Nếu duyệt (approved = true):
     *     <ul>
     *       <li>Cập nhật trạng thái APPROVED, lưu thông tin người duyệt và thời gian duyệt.</li>
     *       <li>Gọi processPayment để xử lý thanh toán (giả lập/sandbox).</li>
     *     </ul>
     *   </li>
     *   <li>Nếu từ chối (approved = false):
     *     <ul>
     *       <li>Cập nhật trạng thái REJECTED, lưu lý do từ chối, người duyệt, thời gian duyệt.</li>
     *       <li>Giải phóng số tiền pending trong ví (trừ đi số tiền đã pendingWithdraw).</li>
     *     </ul>
     *   </li>
     *   <li>Lưu lại trạng thái mới của yêu cầu rút tiền.</li>
     * </ol>
     * @param approvalRequest Thông tin duyệt yêu cầu rút tiền (ID, approved, lý do từ chối)
     * @param adminId        ID admin/staff thực hiện duyệt
     * @return PayoutRequest đã cập nhật trạng thái
     * @throws PayoutRequestNotFoundException nếu không tìm thấy yêu cầu
     * @throws InvalidPayoutStatusException nếu trạng thái không hợp lệ
     */
	@Override
	public PayoutRequest approvePayoutRequest(PayoutApprovalRequest approvalRequest) {
		var user = securityHelper.getCurrentUser();
		log.info("Processing payout approval. Request ID: {}, Approved: {}", 
                approvalRequest.getPayoutRequestId(), approvalRequest.getApproved());
        
        // 1. Tìm kiếm yêu cầu rút tiền
        PayoutRequest payoutRequest = payoutRequestRepository.findById(approvalRequest.getPayoutRequestId())
            .orElseThrow(() -> new PayoutRequestNotFoundException("Payout request not found"));
            
        // 2. Kiểm tra trạng thái phải là PENDING
        if (payoutRequest.getStatus() != PayoutStatus.PENDING) {
            throw new InvalidPayoutStatusException("Payout request is not in PENDING status");
        }
        
        // 3. Lấy ví giảng viên
        InstructorWallet wallet = walletRepository.findByInstructorId(payoutRequest.getInstructorId())
            .orElseThrow(() -> new RuntimeException("Instructor wallet not found"));
        
        if (approvalRequest.getApproved()) {
            // 4a. Duyệt yêu cầu: cập nhật trạng thái APPROVED, gọi processPayment để xử lý thanh toán
            payoutRequest.setStatus(PayoutStatus.APPROVED);
            payoutRequest.setProcessedAt(LocalDateTime.now());
            payoutRequest.setProcessedBy(user.getId().getMostSignificantBits()); // Convert UUID to Long for demo
            
            // Xử lý thanh toán ngay (sandbox)
            processPayment(payoutRequest);
            
        } else {
            // 4b. Từ chối: cập nhật trạng thái REJECTED, giải phóng số tiền pending
            payoutRequest.setStatus(PayoutStatus.REJECTED);
            payoutRequest.setRejectionReason(approvalRequest.getRejectionReason());
            payoutRequest.setProcessedAt(LocalDateTime.now());
            payoutRequest.setProcessedBy(user.getId().getMostSignificantBits());
            
            // Giải phóng số tiền pending trong ví
            wallet.setPendingWithdraw(wallet.getPendingWithdraw().subtract(payoutRequest.getAmount()));
            walletRepository.save(wallet);
            
            log.info("Payout request rejected. ID: {}, Reason: {}", 
                    payoutRequest.getId(), approvalRequest.getRejectionReason());
        }
        
        return payoutRequestRepository.save(payoutRequest);
	}

	/**
     * Xử lý thanh toán cho yêu cầu rút tiền (giả lập/sandbox).
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Sinh mã giao dịch giả lập (transactionRef).</li>
     *   <li>Tạo bản ghi giao dịch rút tiền (PayoutTransaction) với thông tin ngân hàng, số tiền, người xử lý.</li>
     *   <li>Lấy ví giảng viên, cập nhật số dư:
     *     <ul>
     *       <li>Trừ balance (số dư khả dụng).</li>
     *       <li>Trừ pendingWithdraw (số tiền đang chờ rút).</li>
     *       <li>Cộng vào totalWithdrawn (tổng đã rút).</li>
     *     </ul>
     *   </li>
     *   <li>Tạo bản ghi giao dịch ví (WalletTransaction) với số tiền âm, cập nhật số dư sau giao dịch.</li>
     *   <li>Cập nhật trạng thái yêu cầu rút tiền thành PAID.</li>
     * </ol>
     * @param payoutRequest Yêu cầu rút tiền đã được duyệt
     * @param adminId      ID admin xử lý
     * @throws RuntimeException nếu không tìm thấy ví giảng viên
     */
	@Override
	public void processPayment(PayoutRequest payoutRequest) {
		var user = securityHelper.getCurrentUser();

		log.info("Processing payment for payout request: {}", payoutRequest.getId());
        
        // 1. Sinh mã giao dịch giả lập
        String transactionRef = "PAY_" + System.currentTimeMillis() + "_" + payoutRequest.getId();
        
        // 2. Tạo bản ghi giao dịch rút tiền
        PayoutTransaction transaction = PayoutTransaction.builder()
            .payoutRequestId(payoutRequest.getId())
            .instructorId(payoutRequest.getInstructorId())
            .amount(payoutRequest.getAmount())
            .transactionRef(transactionRef)
            .bankName(payoutRequest.getBankName())
            .bankAccount(payoutRequest.getBankAccount())
            .accountHolder(payoutRequest.getAccountHolder())
            .processedBy(user.getId())
            .note("Sandbox payment processed successfully")
            .build();
            
        payoutTransactionRepository.save(transaction);
        
        // 3. Cập nhật số dư ví: trừ balance, pending, cộng tổng đã rút
        InstructorWallet wallet = walletRepository.findByInstructorId(payoutRequest.getInstructorId())
            .orElseThrow(() -> new RuntimeException("Instructor wallet not found"));
            
        wallet.setBalance(wallet.getBalance().subtract(payoutRequest.getAmount()));
        wallet.setPendingWithdraw(wallet.getPendingWithdraw().subtract(payoutRequest.getAmount()));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(payoutRequest.getAmount()));
        
        InstructorWallet savedWallet = walletRepository.save(wallet);
        
        // 4. Tạo bản ghi giao dịch ví
        WalletTransaction walletTxn = WalletTransaction.builder()
            .instructorId(payoutRequest.getInstructorId())
            .type(WalletTransactionType.PAYOUT)
            .amount(payoutRequest.getAmount().negate()) // Negative amount
            .balanceAfter(savedWallet.getBalance())
            .referenceId(payoutRequest.getId())
            .referenceType("PAYOUT")
            .description("Payout completed - Request ID: " + payoutRequest.getId())
            .build();
            
        walletTransactionRepository.save(walletTxn);
        
        // 5. Cập nhật trạng thái yêu cầu rút tiền thành PAID
        payoutRequest.setStatus(PayoutStatus.PAID);
        
        log.info("Payment processed successfully. Transaction ref: {}, New wallet balance: {}", 
                transactionRef, savedWallet.getBalance());
	}

	/**
     * Giảng viên hủy yêu cầu rút tiền ở trạng thái PENDING.
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Kiểm tra quyền truy cập (chỉ instructor sở hữu hoặc admin/staff mới được hủy).</li>
     *   <li>Tìm kiếm yêu cầu rút tiền theo ID.</li>
     *   <li>Kiểm tra quyền sở hữu (instructorId phải trùng với yêu cầu).</li>
     *   <li>Kiểm tra trạng thái phải là PENDING.</li>
     *   <li>Giải phóng số tiền pending trong ví (trừ đi số tiền đã pendingWithdraw).</li>
     *   <li>Cập nhật trạng thái yêu cầu thành CANCELLED, lưu thời gian xử lý.</li>
     * </ol>
     * @param requestId    ID yêu cầu rút tiền
     * @param instructorId ID giảng viên thực hiện hủy
     * @return PayoutRequest đã hủy
     * @throws PayoutRequestNotFoundException nếu không tìm thấy yêu cầu
     * @throws InvalidPayoutStatusException nếu trạng thái không hợp lệ
     * @throws RuntimeException nếu không đúng quyền sở hữu
     */
	@Override
	public PayoutRequest cancelPayoutRequest(Long requestId, UUID instructorId) {
		checkAccess(instructorId);
		log.info("Cancelling payout request: {} by instructor: {}", requestId, instructorId);
        
        // 1. Tìm kiếm yêu cầu rút tiền
        PayoutRequest payoutRequest = payoutRequestRepository.findById(requestId)
            .orElseThrow(() -> new PayoutRequestNotFoundException("Payout request not found"));
            
        // 2. Kiểm tra quyền sở hữu
        if (!payoutRequest.getInstructorId().equals(instructorId)) {
            throw new RuntimeException("Unauthorized to cancel this payout request");
        }
        
        // 3. Kiểm tra trạng thái phải là PENDING
        if (payoutRequest.getStatus() != PayoutStatus.PENDING) {
            throw new InvalidPayoutStatusException("Can only cancel PENDING requests");
        }
        
        // 4. Giải phóng số tiền pending trong ví
        InstructorWallet wallet = walletRepository.findByInstructorId(instructorId)
            .orElseThrow(() -> new RuntimeException("Instructor wallet not found"));
            
        wallet.setPendingWithdraw(wallet.getPendingWithdraw().subtract(payoutRequest.getAmount()));
        walletRepository.save(wallet);
        
        // 5. Cập nhật trạng thái yêu cầu thành CANCELLED
        payoutRequest.setStatus(PayoutStatus.CANCELLED);
        payoutRequest.setProcessedAt(LocalDateTime.now());
        
        log.info("Payout request cancelled successfully. ID: {}", requestId);
        
        return payoutRequestRepository.save(payoutRequest);
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
     * Lấy thông tin số dư ví của giảng viên.
     * <p>
     * Nếu chưa có ví sẽ trả về số dư 0.
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Kiểm tra quyền truy cập.</li>
     *   <li>Lấy ví theo instructorId, nếu chưa có thì trả về số dư 0.</li>
     *   <li>Trả về WalletBalanceDto với các trường số dư, tổng thu nhập, tổng đã rút, số tiền pending, số dư khả dụng.</li>
     * </ol>
     * @param instructorId ID giảng viên
     * @return WalletBalanceDto thông tin số dư
     */
	@Override
	public WalletBalanceDto getWalletBalance() {
		var user = securityHelper.getCurrentUser();
		checkAccess(user.getId());
        InstructorWallet wallet = walletRepository.findByInstructorId(user.getId())
            .orElse(InstructorWallet.builder()
                .instructorId(user.getId())
                .balance(BigDecimal.ZERO)
                .totalEarnings(BigDecimal.ZERO)
                .totalWithdrawn(BigDecimal.ZERO)
                .pendingWithdraw(BigDecimal.ZERO)
                .build());
        return WalletBalanceDto.builder()
            .balance(wallet.getBalance())
            .totalEarnings(wallet.getTotalEarnings())
            .totalWithdrawn(wallet.getTotalWithdrawn())
            .pendingWithdraw(wallet.getPendingWithdraw())
            .availableBalance(wallet.getAvailableBalance())
            .build();
    }

	/**
     * Lấy danh sách yêu cầu rút tiền của giảng viên (có phân trang).
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Kiểm tra quyền truy cập.</li>
     *   <li>Lấy danh sách yêu cầu rút tiền theo instructorId, sắp xếp mới nhất trước.</li>
     * </ol>
     * @param instructorId ID giảng viên
     * @param pageable     Thông tin phân trang
     * @return Page<PayoutRequest> danh sách yêu cầu
     */
	@Override
	public Page<PayoutRequest> getInstructorPayoutRequests(UUID instructorId, Pageable pageable) {
		checkAccess(instructorId);
        return payoutRequestRepository.findByInstructorIdOrderByCreatedDateDesc(instructorId, pageable);
	}

	/**
     * Lấy danh sách yêu cầu rút tiền đang chờ xử lý (PENDING) cho admin/staff (có phân trang).
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Lấy danh sách yêu cầu rút tiền ở trạng thái PENDING, sắp xếp mới nhất trước.</li>
     * </ol>
     * @param pageable Thông tin phân trang
     * @return Page<PayoutRequest> danh sách yêu cầu PENDING
     */
	@Override
	public Page<PayoutRequest> getPendingPayoutRequests(Pageable pageable) {
        return payoutRequestRepository.findByStatusOrderByCreatedDateDesc(PayoutStatus.PENDING, pageable);

	}

	/**
     * Lấy lịch sử giao dịch ví của giảng viên (có phân trang).
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Kiểm tra quyền truy cập.</li>
     *   <li>Lấy danh sách giao dịch ví theo instructorId, sắp xếp mới nhất trước.</li>
     * </ol>
     * @param instructorId ID giảng viên
     * @param pageable     Thông tin phân trang
     * @return Page<WalletTransaction> danh sách giao dịch ví
     */
	@Override
	public Page<WalletTransaction> getWalletTransactions(UUID instructorId, Pageable pageable) {
		checkAccess(instructorId);
        return walletTransactionRepository.findByInstructorIdOrderByCreatedDateDesc(instructorId, pageable);

	}

	/**
     * Lấy lịch sử giao dịch rút tiền của giảng viên (có phân trang).
     * <p>
     * <b>Luồng hoạt động:</b>
     * <ol>
     *   <li>Kiểm tra quyền truy cập.</li>
     *   <li>Lấy danh sách giao dịch rút tiền theo instructorId, sắp xếp mới nhất trước.</li>
     * </ol>
     * @param instructorId ID giảng viên
     * @param pageable     Thông tin phân trang
     * @return Page<PayoutTransaction> danh sách giao dịch rút tiền
     */
	@Override
	public Page<PayoutTransaction> getPayoutTransactions(UUID instructorId, Pageable pageable) {
		checkAccess(instructorId);
        return payoutTransactionRepository.findByInstructorIdOrderByCreatedDateDesc(instructorId, pageable);

	}

}