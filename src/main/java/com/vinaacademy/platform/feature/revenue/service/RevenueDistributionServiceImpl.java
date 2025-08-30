package com.vinaacademy.platform.feature.revenue.service;

import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.order_payment.entity.Order;
import com.vinaacademy.platform.feature.order_payment.entity.OrderItem;
import com.vinaacademy.platform.feature.order_payment.entity.Payment;
import com.vinaacademy.platform.feature.revenue.entity.InstructorWallet;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;
import com.vinaacademy.platform.feature.revenue.entity.WalletTransaction;
import com.vinaacademy.platform.feature.revenue.enums.RevenueStatus;
import com.vinaacademy.platform.feature.revenue.enums.WalletTransactionType;
import com.vinaacademy.platform.feature.revenue.repository.InstructorWalletRepository;
import com.vinaacademy.platform.feature.revenue.repository.RevenueRecordRepository;
import com.vinaacademy.platform.feature.revenue.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueDistributionServiceImpl implements RevenueDistributionService {

    private final RevenueRecordRepository revenueRecordRepository;
    private final InstructorWalletRepository instructorWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CourseInstructorRepository courseInstructorRepository;

    @Value("${app.revenue.instructor-percentage:0.7000}")
    private BigDecimal instructorPercentage;

    @Override
    @Transactional
    public void distributeRevenue(Payment payment, Map<String, String> vnpayResponse) {
        Order order = payment.getOrder();
        
        log.info("Starting revenue distribution for payment {} with order {}", 
                payment.getId(), order.getId());

        // Xử lý từng khóa học trong order
        for (OrderItem orderItem : order.getOrderItems()) {
            processOrderItemRevenue(orderItem, payment, vnpayResponse);
        }

        log.info("Completed revenue distribution for payment {}", payment.getId());
    }

    private void processOrderItemRevenue(OrderItem orderItem, Payment payment, Map<String, String> vnpayResponse) {
        // Lấy instructor ID từ CourseInstructor
        UUID instructorId = getInstructorIdFromCourseInstructor(orderItem.getCourse().getId());
        if (instructorId == null) {
            log.warn("Cannot find instructor for course {}, skipping revenue distribution", 
                    orderItem.getCourse().getId());
            return;
        }
        
        BigDecimal itemPrice = orderItem.getPrice();
        
        // Tính toán phân chia doanh thu
        RevenueCalculation calculation = calculateRevenue(itemPrice);
        
        // Tạo RevenueRecord
        RevenueRecord revenueRecord = createRevenueRecord(orderItem, payment, calculation, vnpayResponse, instructorId);
        revenueRecord = revenueRecordRepository.save(revenueRecord);
        
        // Cộng tiền vào ví giảng viên
        updateInstructorWallet(instructorId, calculation.instructorEarning(), revenueRecord);
        
        log.info("Processed revenue for course {} - Instructor: {}, Platform: {}", 
                orderItem.getCourse().getId(), 
                calculation.instructorEarning(), 
                calculation.platformFee());
    }

    private UUID getInstructorIdFromCourseInstructor(UUID courseId) {
        // Tìm Instructor đầu tiên trong danh sách CourseInstructor
        CourseInstructor courseInstructor = courseInstructorRepository.findByCourseId(courseId)
                .stream().findFirst().orElse(null);
        
        return courseInstructor != null ? courseInstructor.getInstructor().getId() : null;
    }

    private RevenueRecord createRevenueRecord(OrderItem orderItem, Payment payment, 
                                            RevenueCalculation calculation, Map<String, String> vnpayResponse,
                                            UUID instructorId) {
        Order order = payment.getOrder();
        
        return RevenueRecord.builder()
                .courseId(orderItem.getCourse().getId())
                .enrollmentId(getEnrollmentId(order.getUser().getId(), orderItem.getCourse().getId()))
                .paymentId(payment.getId())
                .instructorId(instructorId) // Sử dụng instructorId parameter thay vì course.getUser()
                .studentId(order.getUser().getId())
                .totalAmount(calculation.totalAmount())
                .instructorEarning(calculation.instructorEarning())
                .platformFee(calculation.platformFee())
                .instructorPercent(calculation.instructorPercent())
                .status(RevenueStatus.ACTIVE)
                .vnpayTxnRef(payment.getTransactionId())
                .vnpayResponseCode(vnpayResponse.get("vnp_ResponseCode"))
                .vnpayTransactionNo(vnpayResponse.get("vnp_TransactionNo"))
                .vnpayOrderInfo(vnpayResponse.get("vnp_OrderInfo"))
                .vnpayAmount(new BigDecimal(vnpayResponse.getOrDefault("vnp_Amount", "0")))
                .build();
    }

    private void updateInstructorWallet(UUID instructorId, BigDecimal earning, RevenueRecord revenueRecord) {
        // Tìm hoặc tạo ví giảng viên
        InstructorWallet wallet = instructorWalletRepository.findByInstructorId(instructorId)
                .orElseGet(() -> createNewInstructorWallet(instructorId));
        
        // Cập nhật số dư và tổng thu nhập
        BigDecimal oldBalance = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().add(earning));
        wallet.setTotalEarnings(wallet.getTotalEarnings().add(earning));
        
        wallet = instructorWalletRepository.save(wallet);
        
        // Tạo WalletTransaction
        createWalletTransaction(instructorId, earning, wallet.getBalance(), revenueRecord);
        
        log.info("Updated instructor {} wallet: +{} (balance: {} -> {})", 
                instructorId, earning, oldBalance, wallet.getBalance());
    }

    private InstructorWallet createNewInstructorWallet(UUID instructorId) {
        log.info("Creating new wallet for instructor {}", instructorId);
        return InstructorWallet.builder()
                .instructorId(instructorId)
                .balance(BigDecimal.ZERO)
                .totalEarnings(BigDecimal.ZERO)
                .totalWithdrawn(BigDecimal.ZERO)
                .pendingWithdraw(BigDecimal.ZERO)
                .build();
    }

    private void createWalletTransaction(UUID instructorId, BigDecimal amount, BigDecimal balanceAfter, RevenueRecord revenueRecord) {
        WalletTransaction transaction = WalletTransaction.builder()
                .instructorId(instructorId)
                .type(WalletTransactionType.EARNING) // Sửa từ REVENUE thành EARNING
                .amount(amount)
                .balanceAfter(balanceAfter)
                .referenceId(revenueRecord.getId())
                .referenceType("REVENUE")
                .description(String.format("Doanh thu từ khóa học - Order #%s", 
                        revenueRecord.getVnpayTxnRef()))
                .build();
        
        walletTransactionRepository.save(transaction);
    }

    @Override
    public RevenueCalculation calculateRevenue(BigDecimal totalAmount) {
        BigDecimal instructorEarning = totalAmount
                .multiply(instructorPercentage)
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal platformFee = totalAmount
                .subtract(instructorEarning)
                .setScale(2, RoundingMode.HALF_UP);
        
        return new RevenueCalculation(
                totalAmount,
                instructorEarning,
                platformFee,
                instructorPercentage
        );
    }

    private Long getEnrollmentId(UUID userId, UUID courseId) {
        // TODO: Lấy enrollment ID từ EnrollmentRepository
        // Tạm thời return null, sẽ được implement sau
        return null;
    }
}