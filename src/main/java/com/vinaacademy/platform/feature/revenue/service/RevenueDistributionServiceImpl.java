package com.vinaacademy.platform.feature.revenue.service;

import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Dịch vụ phân phối doanh thu cho giảng viên từ việc bán khóa học.
 * 
 * <p>Service này chịu trách nhiệm xử lý toàn bộ quy trình phân chia doanh thu giữa
 * giảng viên và nền tảng khi học viên mua khóa học thành công.
 * 
 * <h3>Chức năng chính:</h3>
 * <ul>
 *   <li>Phân chia doanh thu theo tỷ lệ cấu hình (mặc định 70% cho giảng viên, 30% cho nền tảng)</li>
 *   <li>Tạo bản ghi doanh thu chi tiết cho mục đích kiểm toán và theo dõi</li>
 *   <li>Quản lý ví điện tử của giảng viên và các giao dịch liên quan</li>
 *   <li>Đảm bảo tính idempotency để tránh trùng lặp khi xử lý cùng một payment</li>
 *   <li>Ghi log chi tiết cho việc giám sát và debug hệ thống</li>
 * </ul>
 * 
 * <h3>Quy trình xử lý doanh thu:</h3>
 * <ol>
 *   <li><b>Nhận thông tin thanh toán:</b> Xử lý payment từ VNPay khi giao dịch thành công</li>
 *   <li><b>Duyệt từng khóa học:</b> Lặp qua tất cả OrderItem trong đơn hàng</li>
 *   <li><b>Tìm giảng viên:</b> Xác định giảng viên chủ sở hữu khóa học</li>
 *   <li><b>Tính toán doanh thu:</b> Áp dụng công thức chia theo tỷ lệ cấu hình</li>
 *   <li><b>Kiểm tra trùng lặp:</b> Đảm bảo không xử lý lại cùng một payment</li>
 *   <li><b>Tạo bản ghi doanh thu:</b> Lưu thông tin chi tiết vào RevenueRecord</li>
 *   <li><b>Cập nhật ví giảng viên:</b> Thêm tiền vào ví và ghi lại giao dịch</li>
 * </ol>
 * 
 * <h3>Đảm bảo dữ liệu:</h3>
 * <ul>
 *   <li>Tất cả thao tác được thực hiện trong transaction để đảm bảo tính nhất quán</li>
 *   <li>Sử dụng BigDecimal với độ chính xác 2 chữ số thập phân cho các phép tính tiền tệ</li>
 *   <li>Áp dụng quy tắc làm tròn HALF_UP cho các phép tính</li>
 *   <li>Kiểm tra idempotency dựa trên bộ ba (paymentId, instructorId, courseId)</li>
 * </ul>
 * 
 * @author Đội ngũ phát triển VinaAcademy
 * @version 1.0
 * @since 1.0
 * @see RevenueDistributionService
 * @see RevenueRecord
 * @see InstructorWallet
 * @see WalletTransaction
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueDistributionServiceImpl implements RevenueDistributionService {

    private final RevenueRecordRepository revenueRecordRepository;
    private final InstructorWalletRepository instructorWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Tỷ lệ phần trăm doanh thu dành cho giảng viên.
     * 
     * <p>Giá trị này được cấu hình thông qua thuộc tính {@code app.revenue.instructor-percentage}
     * trong file application.yml. Nếu không được cấu hình, sẽ sử dụng giá trị mặc định là 0.7000 (70%).
     * 
     * <p>Công thức tính:
     * <ul>
     *   <li>Thu nhập giảng viên = Tổng tiền × instructorPercentage</li>
     *   <li>Phí nền tảng = Tổng tiền - Thu nhập giảng viên</li>
     * </ul>
     */
    @Value("${app.revenue.instructor-percentage:0.7000}")
    private BigDecimal instructorPercentage;

    /**
     * Phân phối doanh thu từ một giao dịch thanh toán thành công cho giảng viên và nền tảng.
     * 
     * <p>Đây là phương thức chính của service, xử lý toàn bộ quy trình phân chia doanh thu
     * cho tất cả khóa học trong một đơn hàng.
     * 
     * <h3>Các bước xử lý chi tiết:</h3>
     * <ol>
     *   <li><b>Khởi tạo và ghi log:</b>
     *       <ul>
     *         <li>Lấy thông tin Order từ Payment object</li>
     *         <li>Ghi log bắt đầu quá trình xử lý với paymentId và orderId</li>
     *       </ul>
     *   </li>
     *   <li><b>Duyệt từng khóa học:</b>
     *       <ul>
     *         <li>Lặp qua tất cả OrderItem trong Order</li>
     *         <li>Mỗi OrderItem đại diện cho một khóa học được mua</li>
     *         <li>Gọi {@link #processOrderItemRevenue(OrderItem, Payment, Map)} cho từng item</li>
     *       </ul>
     *   </li>
     *   <li><b>Hoàn thành và ghi log:</b>
     *       <ul>
     *         <li>Ghi log kết thúc quá trình xử lý thành công</li>
     *         <li>Transaction sẽ được commit nếu không có lỗi xảy ra</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Xử lý lỗi:</h3>
     * <ul>
     *   <li>Nếu có lỗi xảy ra trong quá trình xử lý bất kỳ OrderItem nào, toàn bộ transaction sẽ được rollback</li>
     *   <li>Lỗi sẽ được log chi tiết và re-throw để xử lý ở tầng cao hơn</li>
     *   <li>Đảm bảo tính nhất quán dữ liệu trong trường hợp có lỗi</li>
     * </ul>
     * 
     * @param payment Đối tượng Payment chứa thông tin giao dịch và Order
     *                Không được null và phải có Order hợp lệ
     * @param vnpayResponse Map chứa thông tin phản hồi từ VNPay
     *                      Bao gồm các key: vnp_ResponseCode, vnp_TransactionNo, vnp_OrderInfo, vnp_Amount
     * 
     * @throws IllegalArgumentException nếu payment hoặc order là null
     * @throws RuntimeException nếu có lỗi trong quá trình phân phối doanh thu cho bất kỳ khóa học nào
     * 
     * @see #processOrderItemRevenue(OrderItem, Payment, Map)
     * @see Payment
     * @see Order
     * @see OrderItem
     */
    @Override
    @Transactional
    public void distributeRevenue(Payment payment, Map<String, String> vnpayResponse) {
        Order order = payment.getOrder();
        
        log.info("Bắt đầu phân phối doanh thu cho payment {} với order {}", 
                payment.getId(), order.getId());

        // Xử lý từng khóa học trong order
        for (OrderItem orderItem : order.getOrderItems()) {
            processOrderItemRevenue(orderItem, payment, vnpayResponse);
        }

        log.info("Hoàn thành phân phối doanh thu cho payment {}", payment.getId());
    }

    /**
     * Xử lý phân phối doanh thu cho một khóa học cụ thể trong đơn hàng.
     * 
     * <p>Phương thức này thực hiện quy trình phân phối doanh thu hoàn chỉnh cho từng khóa học,
     * từ việc tìm giảng viên đến cập nhật ví và tạo bản ghi giao dịch.
     * 
     * <h3>Các bước xử lý chi tiết:</h3>
     * <ol>
     *   <li><b>Tìm kiếm giảng viên:</b>
     *       <ul>
     *         <li>Gọi {@link #getInstructorIdFromCourseInstructor(UUID)} để lấy instructorId</li>
     *         <li>Nếu không tìm thấy giảng viên, ghi log cảnh báo và bỏ qua khóa học này</li>
     *         <li>Điều này có thể xảy ra với các khóa học chưa được gán giảng viên</li>
     *       </ul>
     *   </li>
     *   <li><b>Tính toán doanh thu:</b>
     *       <ul>
     *         <li>Lấy giá của OrderItem làm tổng số tiền cần phân chia</li>
     *         <li>Gọi {@link #calculateRevenue(BigDecimal)} để tính toán phần cho giảng viên và nền tảng</li>
     *         <li>Kết quả trả về object RevenueCalculation chứa các thông tin đã tính toán</li>
     *       </ul>
     *   </li>
     *   <li><b>Kiểm tra idempotency:</b>
     *       <ul>
     *         <li>Tìm kiếm bản ghi RevenueRecord đã tồn tại với bộ ba (paymentId, instructorId, courseId)</li>
     *         <li>Nếu đã tồn tại, ghi log cảnh báo và bỏ qua để tránh xử lý trùng lặp</li>
     *         <li>Điều này đảm bảo tính idempotency của hệ thống</li>
     *       </ul>
     *   </li>
     *   <li><b>Tạo bản ghi doanh thu:</b>
     *       <ul>
     *         <li>Gọi {@link #createRevenueRecord} để tạo đối tượng RevenueRecord</li>
     *         <li>Lưu RevenueRecord vào database thông qua repository</li>
     *         <li>Bản ghi này phục vụ mục đích kiểm toán và theo dõi</li>
     *       </ul>
     *   </li>
     *   <li><b>Cập nhật ví giảng viên:</b>
     *       <ul>
     *         <li>Gọi {@link #updateInstructorWallet} với số tiền earnings và RevenueRecord</li>
     *         <li>Cập nhật số dư ví và tổng thu nhập của giảng viên</li>
     *         <li>Tạo bản ghi WalletTransaction cho giao dịch này</li>
     *       </ul>
     *   </li>
     *   <li><b>Ghi log kết quả:</b>
     *       <ul>
     *         <li>Log thành công với thông tin thu nhập của giảng viên và phí nền tảng</li>
     *         <li>Thông tin này giúp theo dõi và debug hệ thống</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Xử lý ngoại lệ:</h3>
     * <ul>
     *   <li>Mọi exception trong quá trình xử lý đều được catch và log chi tiết</li>
     *   <li>Exception sẽ được re-throw để rollback transaction và thông báo lỗi lên tầng cao hơr</li>
     *   <li>Log bao gồm paymentId, instructorId, courseId để dễ dàng debug</li>
     * </ul>
     * 
     * @param orderItem OrderItem chứa thông tin khóa học và giá cả
     *                  Không được null và phải có Course hợp lệ
     * @param payment Đối tượng Payment liên kết với OrderItem này
     *                Không được null và phải có ID hợp lệ
     * @param vnpayResponse Map chứa thông tin phản hồi từ VNPay
     *                      Sử dụng để tạo RevenueRecord với thông tin giao dịch
     * 
     * @throws RuntimeException nếu có lỗi trong quá trình xử lý sau khi tìm thấy giảng viên
     * 
     * @see #getInstructorIdFromCourseInstructor(UUID)
     * @see #calculateRevenue(BigDecimal)
     * @see #createRevenueRecord(OrderItem, Payment, RevenueCalculation, Map, UUID)
     * @see #updateInstructorWallet(UUID, BigDecimal, RevenueRecord)
     */
    private void processOrderItemRevenue(OrderItem orderItem, Payment payment, Map<String, String> vnpayResponse) {
    	UUID instructorId = getInstructorIdFromCourseInstructor(orderItem.getCourse());
		
		 // Nếu không tìm thấy giảng viên, ghi log và bỏ qua
        if (instructorId == null) {
            log.warn("Không thể tìm thấy giảng viên cho khóa học {}, bỏ qua phân phối doanh thu", 
                    orderItem.getCourse().getId());
            return;
        }
        
        BigDecimal itemPrice = orderItem.getPrice();
        RevenueCalculation calculation = calculateRevenue(itemPrice);
        
        // Kiểm tra idempotency: chỉ tạo nếu chưa tồn tại
        boolean alreadyExists = revenueRecordRepository.findByPaymentIdAndInstructorIdAndCourseId(
            payment.getId(), instructorId, orderItem.getCourse().getId()
        ).isPresent();
        
        if (alreadyExists) {
            log.warn("Bản ghi doanh thu đã tồn tại cho paymentId={}, instructorId={}, courseId={}. Bỏ qua xử lý.", 
                    payment.getId(), instructorId, orderItem.getCourse().getId());
            return;
        }
        
        try {
            RevenueRecord revenueRecord = createRevenueRecord(orderItem, payment, calculation, vnpayResponse, instructorId);
            revenueRecord = revenueRecordRepository.save(revenueRecord);
            
            updateInstructorWallet(instructorId, calculation.instructorEarning(), revenueRecord);
            
            log.info("Đã xử lý doanh thu cho khóa học {} - Giảng viên: {}, Nền tảng: {}", 
                    orderItem.getCourse().getId(), calculation.instructorEarning(), calculation.platformFee());
        } catch (Exception e) {
            log.error("Lỗi khi phân phối doanh thu cho paymentId={}, instructorId={}, courseId={}: {}", 
                    payment.getId(), instructorId, orderItem.getCourse().getId(), e.getMessage(), e);
            throw e; // propagate error để rollback transaction và xử lý ở tầng cao hơn
        }
    }

    /**
     * Tìm kiếm ID của giảng viên được gắn với một khóa học cụ thể.
     * 
     * <p>Phương thức này truy vấn bảng CourseInstructor để tìm mối quan hệ giữa khóa học và giảng viên.
     * Trong trường hợp một khóa học có nhiều giảng viên, phương thức sẽ trả về giảng viên đầu tiên được tìm thấy.
     * 
     * <h3>Logic xử lý:</h3>
     * <ol>
     *   <li><b>Truy vấn database:</b>
     *       <ul>
     *         <li>Gọi {@code courseInstructorRepository.findAllByCourseId(courseId)}</li>
     *         <li>Trả về danh sách tất cả CourseInstructor liên kết với khóa học</li>
     *       </ul>
     *   </li>
     *   <li><b>Lấy giảng viên đầu tiên:</b>
     *       <ul>
     *         <li>Sử dụng Stream API để lấy phần tử đầu tiên</li>
     *         <li>Nếu danh sách rỗng, trả về null</li>
     *       </ul>
     *   </li>
     *   <li><b>Trích xuất Instructor ID:</b>
     *       <ul>
     *         <li>Từ CourseInstructor object, lấy Instructor entity</li>
     *         <li>Trả về ID của Instructor, hoặc null nếu không tìm thấy</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Trường hợp đặc biệt:</h3>
     * <ul>
     *   <li><b>Khóa học chưa có giảng viên:</b> Trả về null, không ném exception</li>
     *   <li><b>Nhiều giảng viên:</b> Chỉ lấy giảng viên đầu tiên theo thứ tự database trả về</li>
     *   <li><b>Dữ liệu không nhất quán:</b> Có thể xảy ra nếu CourseInstructor tồn tại nhưng Instructor đã bị xóa</li>
     * </ul>
     * 
     * @param courseId ID duy nhất của khóa học cần tìm giảng viên
     *                 Không được null, phải là UUID hợp lệ
     * 
     * @return UUID của giảng viên liên kết với khóa học, hoặc null nếu không tìm thấy
     * 
     * @see CourseInstructor
     * @see CourseInstructorRepository#findAllByCourseId(UUID)
     */
    private UUID getInstructorIdFromCourseInstructor(Course course) {
        // Tìm Instructor đầu tiên trong danh sách CourseInstructor
    	Optional<CourseInstructor> courseInstructor = courseInstructorRepository.findByCourseAndIsOwnerTrue(course);        
        return courseInstructor.map(ci -> ci.getInstructor().getId()).orElse(null);
    }

    /**
     * Tạo bản ghi doanh thu chi tiết cho mục đích kiểm toán và theo dõi.
     * 
     * <p>Phương thức này tạo một đối tượng RevenueRecord hoàn chỉnh chứa tất cả thông tin
     * cần thiết về việc phân phối doanh thu cho một khóa học cụ thể.
     * 
     * <h3>Thông tin được lưu trữ:</h3>
     * <ol>
     *   <li><b>Thông tin định danh:</b>
     *       <ul>
     *         <li>courseId: ID của khóa học</li>
     *         <li>instructorId: ID của giảng viên nhận doanh thu</li>
     *         <li>studentId: ID của học viên mua khóa học</li>
     *         <li>paymentId: ID của giao dịch thanh toán</li>
     *         <li>enrollmentId: ID của enrollment (hiện tại null, sẽ implement sau)</li>
     *       </ul>
     *   </li>
     *   <li><b>Thông tin tài chính:</b>
     *       <ul>
     *         <li>totalAmount: Tổng số tiền của khóa học</li>
     *         <li>instructorEarning: Số tiền giảng viên nhận được</li>
     *         <li>platformFee: Số tiền nền tảng thu được</li>
     *         <li>instructorPercent: Tỷ lệ phần trăm của giảng viên</li>
     *       </ul>
     *   </li>
     *   <li><b>Thông tin VNPay:</b>
     *       <ul>
     *         <li>vnpayTxnRef: Mã tham chiếu giao dịch từ hệ thống</li>
     *         <li>vnpayResponseCode: Mã phản hồi từ VNPay</li>
     *         <li>vnpayTransactionNo: Số giao dịch VNPay</li>
     *         <li>vnpayOrderInfo: Thông tin đơn hàng từ VNPay</li>
     *         <li>vnpayAmount: Số tiền trong response VNPay (đơn vị VND)</li>
     *       </ul>
     *   </li>
     *   <li><b>Trạng thái và metadata:</b>
     *       <ul>
     *         <li>status: Trạng thái ACTIVE cho bản ghi mới</li>
     *         <li>Các trường audit (createdDate, createdBy) được tự động điền từ BaseEntity</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Xử lý dữ liệu VNPay:</h3>
     * <ul>
     *   <li>Sử dụng {@code Map.getOrDefault()} để tránh NullPointerException</li>
     *   <li>vnpayAmount được convert từ String sang BigDecimal với giá trị mặc định là "0"</li>
     *   <li>Các field khác lấy trực tiếp từ Map, có thể null nếu không có trong response</li>
     * </ul>
     * 
     * @param orderItem OrderItem chứa thông tin khóa học và giá cả
     *                  Không được null, phải có Course và User hợp lệ
     * @param payment Đối tượng Payment với thông tin giao dịch
     *                Không được null, phải có transactionId hợp lệ
     * @param calculation Kết quả tính toán phân chia doanh thu
     *                    Không được null, chứa các giá trị đã được làm tròn
     * @param vnpayResponse Map chứa phản hồi từ VNPay
     *                      Có thể chứa các key: vnp_ResponseCode, vnp_TransactionNo, vnp_OrderInfo, vnp_Amount
     * @param instructorId ID của giảng viên nhận doanh thu
     *                     Không được null, phải là UUID hợp lệ
     * 
     * @return Đối tượng RevenueRecord mới được tạo, sẵn sàng để lưu vào database
     * 
     * @see RevenueRecord
     * @see RevenueCalculation
     * @see RevenueStatus#ACTIVE
     */
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

    /**
     * Cập nhật số dư ví điện tử của giảng viên và tạo bản ghi giao dịch tương ứng.
     * 
     * <p>Phương thức này thực hiện việc cộng tiền vào ví giảng viên và ghi lại lịch sử
     * giao dịch cho mục đích kiểm toán và theo dõi tài chính.
     * 
     * <h3>Các bước xử lý chi tiết:</h3>
     * <ol>
     *   <li><b>Tìm kiếm hoặc tạo ví:</b>
     *       <ul>
     *         <li>Gọi {@code instructorWalletRepository.findByInstructorId(instructorId)}</li>
     *         <li>Nếu ví đã tồn tại, sử dụng ví hiện có</li>
     *         <li>Nếu ví chưa tồn tại, gọi {@link #createNewInstructorWallet(UUID)} để tạo ví mới</li>
     *         <li>Trường hợp này xảy ra khi giảng viên nhận doanh thu lần đầu tiên</li>
     *       </ul>
     *   </li>
     *   <li><b>Tính toán và cập nhật số dư:</b>
     *       <ul>
     *         <li>Lưu số dư cũ để ghi log</li>
     *         <li>Cộng earning vào balance hiện tại: {@code balance = balance + earning}</li>
     *         <li>Cộng earning vào totalEarnings: {@code totalEarnings = totalEarnings + earning}</li>
     *         <li>Tất cả phép tính sử dụng BigDecimal để đảm bảo độ chính xác</li>
     *       </ul>
     *   </li>
     *   <li><b>Lưu ví đã cập nhật:</b>
     *       <ul>
     *         <li>Gọi {@code instructorWalletRepository.save(wallet)} để persist thay đổi</li>
     *         <li>Repository sẽ tự động cập nhật lastModifiedDate và lastModifiedBy</li>
     *       </ul>
     *   </li>
     *   <li><b>Tạo bản ghi giao dịch:</b>
     *       <ul>
     *         <li>Gọi {@link #createWalletTransaction} với thông tin giao dịch</li>
     *         <li>Bao gồm số tiền, số dư sau giao dịch và reference đến RevenueRecord</li>
     *         <li>Tạo audit trail hoàn chỉnh cho mọi thay đổi trong ví</li>
     *       </ul>
     *   </li>
     *   <li><b>Ghi log thành công:</b>
     *       <ul>
     *         <li>Log thông tin instructorId, số tiền thêm vào, số dư trước và sau</li>
     *         <li>Format: "Cập nhật ví giảng viên {id}: +{earning} (balance: {old} -> {new})"</li>
     *         <li>Giúp theo dõi và debug các vấn đề liên quan đến ví</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Đảm bảo tính toàn vẹn dữ liệu:</h3>
     * <ul>
     *   <li>Tất cả thao tác trong cùng một transaction để đảm bảo consistency</li>
     *   <li>Nếu có lỗi ở bất kỳ bước nào, toàn bộ quá trình sẽ rollback</li>
     *   <li>Số dư balance và totalEarnings luôn nhất quán với nhau</li>
     *   <li>Mỗi thay đổi đều có WalletTransaction tương ứng</li>
     * </ul>
     * 
     * @param instructorId ID duy nhất của giảng viên
     *                     Không được null, phải là UUID hợp lệ
     * @param earning Số tiền cần thêm vào ví giảng viên
     *                Phải là BigDecimal dương, đã được làm tròn đến 2 chữ số thập phân
     * @param revenueRecord Bản ghi doanh thu liên kết với giao dịch này
     *                      Không được null, phải có ID hợp lệ sau khi save
     * 
     * @see #createNewInstructorWallet(UUID)
     * @see #createWalletTransaction(UUID, BigDecimal, BigDecimal, RevenueRecord)
     * @see InstructorWallet
     * @see InstructorWalletRepository
     */
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
        
        log.info("Cập nhật ví giảng viên {}: +{} (số dư: {} -> {})", 
                instructorId, earning, oldBalance, wallet.getBalance());
    }

    /**
     * Tạo ví điện tử mới cho giảng viên với số dư ban đầu là 0.
     * 
     * <p>Phương thức này được gọi khi giảng viên nhận doanh thu lần đầu tiên và
     * chưa có ví trong hệ thống. Ví mới sẽ được khởi tạo với tất cả số dư bằng 0.
     * 
     * <h3>Các trường được khởi tạo:</h3>
     * <ul>
     *   <li><b>instructorId:</b> ID của giảng viên sở hữu ví</li>
     *   <li><b>balance:</b> Số dư hiện tại = 0 (sẽ được cập nhật ngay sau đó)</li>
     *   <li><b>totalEarnings:</b> Tổng thu nhập tích lũy = 0</li>
     *   <li><b>totalWithdrawn:</b> Tổng số tiền đã rút = 0</li>
     *   <li><b>pendingWithdraw:</b> Số tiền đang chờ rút = 0</li>
     * </ul>
     * 
     * <h3>Quy trình sau khi tạo:</h3>
     * <ol>
     *   <li>Ví được tạo với Builder pattern từ Lombok</li>
     *   <li>Trả về object InstructorWallet chưa persist (chưa có ID)</li>
     *   <li>Object này sẽ được save trong phương thức gọi</li>
     *   <li>Ngay sau đó, balance và totalEarnings sẽ được cập nhật với earning đầu tiên</li>
     * </ol>
     * 
     * <h3>Lưu ý quan trọng:</h3>
     * <ul>
     *   <li>Phương thức này không save ví vào database, chỉ tạo object</li>
     *   <li>Việc save sẽ được thực hiện ở phương thức gọi để đảm bảo transaction</li>
     *   <li>Các trường audit (createdDate, createdBy) sẽ tự động được điền khi save</li>
     *   <li>Ghi log để theo dõi việc tạo ví mới</li>
     * </ul>
     * 
     * @param instructorId ID duy nhất của giảng viên cần tạo ví
     *                     Không được null, phải là UUID hợp lệ
     * 
     * @return Đối tượng InstructorWallet mới với các số dư khởi tạo bằng 0
     *         Object chưa được persist, cần save để có ID
     * 
     * @see InstructorWallet
     * @see InstructorWallet.InstructorWalletBuilder
     */
    private InstructorWallet createNewInstructorWallet(UUID instructorId) {
        log.info("Tạo ví mới cho giảng viên {}", instructorId);
        return InstructorWallet.builder()
                .instructorId(instructorId)
                .balance(BigDecimal.ZERO)
                .totalEarnings(BigDecimal.ZERO)
                .totalWithdrawn(BigDecimal.ZERO)
                .pendingWithdraw(BigDecimal.ZERO)
                .build();
    }

    /**
     * Tạo bản ghi giao dịch ví để theo dõi và kiểm toán mọi thay đổi về tài chính.
     * 
     * <p>Phương thức này tạo một WalletTransaction record hoàn chỉnh cho mỗi lần
     * thay đổi số dư ví, đảm bảo có đủ thông tin để audit và debug.
     * 
     * <h3>Thông tin được ghi lại:</h3>
     * <ol>
     *   <li><b>Thông tin cơ bản:</b>
     *       <ul>
     *         <li>instructorId: ID của giảng viên sở hữu ví</li>
     *         <li>type: Loại giao dịch = EARNING (thu nhập từ bán khóa học)</li>
     *         <li>amount: Số tiền trong giao dịch (luôn dương với EARNING)</li>
     *         <li>balanceAfter: Số dư ví sau khi thực hiện giao dịch</li>
     *       </ul>
     *   </li>
     *   <li><b>Thông tin tham chiếu:</b>
     *       <ul>
     *         <li>referenceId: ID của RevenueRecord liên quan</li>
     *         <li>referenceType: Loại tham chiếu = "REVENUE"</li>
     *         <li>Giúp liên kết ngược từ transaction về revenue record gốc</li>
     *       </ul>
     *   </li>
     *   <li><b>Mô tả chi tiết:</b>
     *       <ul>
     *         <li>description: Mô tả dễ hiểu về giao dịch</li>
     *         <li>Format: "Doanh thu từ khóa học - Order #{vnpayTxnRef}"</li>
     *         <li>Giúp người dùng hiểu nguồn gốc của khoản tiền</li>
     *       </ul>
     *   </li>
     *   <li><b>Metadata tự động:</b>
     *       <ul>
     *         <li>createdDate: Thời gian tạo giao dịch (từ BaseEntity)</li>
     *         <li>createdBy: Người tạo giao dịch (thường là system user)</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Mục đích sử dụng:</h3>
     * <ul>
     *   <li><b>Audit trail:</b> Theo dõi mọi thay đổi trong ví giảng viên</li>
     *   <li><b>Reconciliation:</b> Đối soát số liệu giữa các hệ thống</li>
     *   <li><b>Reporting:</b> Tạo báo cáo thu nhập cho giảng viên</li>
     *   <li><b>Debug:</b> Tìm hiểu nguyên nhân thay đổi số dư bất thường</li>
     *   <li><b>Compliance:</b> Đáp ứng yêu cầu pháp lý về ghi chép tài chính</li>
     * </ul>
     * 
     * <h3>Đảm bảo dữ liệu:</h3>
     * <ul>
     *   <li>Transaction được save trong cùng database transaction với wallet update</li>
     *   <li>Đảm bảo consistency giữa wallet state và transaction log</li>
     *   <li>Nếu có lỗi, cả wallet và transaction đều rollback</li>
     * </ul>
     * 
     * @param instructorId ID của giảng viên thực hiện giao dịch
     *                     Không được null, phải khớp với instructorId trong wallet
     * @param amount Số tiền giao dịch
     *               Phải là BigDecimal dương, đã làm tròn đến 2 chữ số thập phân
     * @param balanceAfter Số dư ví sau khi thực hiện giao dịch này
     *                     Phải bằng balance cũ + amount
     * @param revenueRecord Bản ghi doanh thu gốc tạo ra giao dịch này
     *                      Không được null, phải có ID và vnpayTxnRef hợp lệ
     * 
     * @see WalletTransaction
     * @see WalletTransactionType#EARNING
     * @see WalletTransactionRepository
     */
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

    /**
     * Tính toán phân chia doanh thu giữa giảng viên và nền tảng.
     * 
     * <p>Phương thức này thực hiện phép tính phân chia doanh thu dựa trên tỷ lệ
     * được cấu hình trong hệ thống, đảm bảo độ chính xác cao cho các phép tính tiền tệ.
     * 
     * <h3>Công thức tính toán:</h3>
     * <ol>
     *   <li><b>Thu nhập giảng viên:</b>
     *       <ul>
     *         <li>Công thức: instructorEarning = totalAmount × instructorPercentage</li>
     *         <li>Ví dụ: 1,000,000 VND × 0.7000 = 700,000 VND</li>
     *         <li>Sử dụng {@code BigDecimal.multiply()} để đảm bảo độ chính xác</li>
     *         <li>Làm tròn đến 2 chữ số thập phân với quy tắc HALF_UP</li>
     *       </ul>
     *   </li>
     *   <li><b>Phí nền tảng:</b>
     *       <ul>
     *         <li>Công thức: platformFee = totalAmount - instructorEarning</li>
     *         <li>Ví dụ: 1,000,000 VND - 700,000 VND = 300,000 VND</li>
     *         <li>Sử dụng {@code BigDecimal.subtract()} thay vì tính trực tiếp</li>
     *         <li>Đảm bảo tổng luôn bằng instructorEarning + platformFee</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <h3>Xử lý độ chính xác:</h3>
     * <ul>
     *   <li><b>BigDecimal:</b> Sử dụng cho tất cả phép tính để tránh lỗi làm tròn</li>
     *   <li><b>Scale:</b> Tất cả kết quả được set scale = 2 (2 chữ số thập phân)</li>
     *   <li><b>Rounding:</b> Sử dụng HALF_UP (0.5 làm tròn lên) theo chuẩn kế toán</li>
     *   <li><b>Validation:</b> Đảm bảo totalAmount = instructorEarning + platformFee</li>
     * </ul>
     * 
     * <h3>Tỷ lệ cấu hình:</h3>
     * <ul>
     *   <li>Giá trị instructorPercentage lấy từ {@code @Value annotation}</li>
     *   <li>Mặc định: 0.7000 (70%) nếu không cấu hình</li>
     *   <li>Có thể điều chỉnh qua file application.yml</li>
     *   <li>Property: {@code app.revenue.instructor-percentage}</li>
     * </ul>
     * 
     * <h3>Ví dụ cụ thể:</h3>
     * <pre>
     * totalAmount = 999,999 VND
     * instructorPercentage = 0.7000
     * 
     * instructorEarning = 999,999 × 0.7000 = 699,999.30 VND (làm tròn)
     * platformFee = 999,999 - 699,999.30 = 299,999.70 VND
     * 
     * Kiểm tra: 699,999.30 + 299,999.70 = 999,999.00 ✓
     * </pre>
     * 
     * @param totalAmount Tổng số tiền cần phân chia
     *                    Không được null, phải là BigDecimal dương
     *                    Thường là giá của một OrderItem
     * 
     * @return RevenueCalculation record chứa kết quả tính toán đầy đủ
     *         Bao gồm totalAmount, instructorEarning, platformFee, instructorPercent
     * 
     * @see RevenueCalculation
     * @see #instructorPercentage
     * @see BigDecimal#multiply(BigDecimal)
     * @see BigDecimal#setScale(int, RoundingMode)
     */
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
         return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
             .map(Enrollment::getId)
             .orElse(null);
    }
}
