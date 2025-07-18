package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.configuration.AppConfig;
import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.instructor.dto.InstructorInfoDto;
import com.vinaacademy.platform.feature.notification.dto.NotificationCreateDTO;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import com.vinaacademy.platform.feature.notification.service.NotificationService;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import com.vinaacademy.platform.feature.user.role.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class InstructorServiceImpl implements InstructorService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private SecurityHelper securityHelper;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public InstructorInfoDto getInstructorInfo(UUID instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy giảng viên"));

        // Kiểm tra xem user có role INSTRUCTOR không
        boolean isInstructor = instructor.getRoles().stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase(AuthConstants.INSTRUCTOR_ROLE));

        if (!isInstructor) {
            throw BadRequestException.message("Người dùng không phải là giảng viên");
        }

        InstructorInfoDto dto = new InstructorInfoDto();
        dto.setFullName(instructor.getFullName());
        dto.setUsername(instructor.getUsername());
        dto.setEmail(instructor.getEmail());
        dto.setDescription(instructor.getDescription());
        dto.setAvatarUrl(instructor.getAvatarUrl());
        return dto;
    }

    @Override
    @Transactional
    public InstructorInfoDto registerAsInstructor() {
        // Lấy thông tin người dùng hiện tại
        User currentUser = securityHelper.getCurrentUser();
        if (currentUser == null) {
            throw BadRequestException.message("Bạn cần đăng nhập để thực hiện thao tác này");
        }

        // Kiểm tra xem người dùng hiện tại có đúng một role là STUDENT không
        Set<Role> userRoles = currentUser.getRoles();
        if (userRoles.size() != 1 || !userRoles.stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase(AuthConstants.STUDENT_ROLE))) {
            throw BadRequestException.message("Chỉ học viên mới có thể đăng ký làm giảng viên");
        }

        // Kiểm tra xem người dùng đã có role INSTRUCTOR chưa
        boolean alreadyInstructor = userRoles.stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase(AuthConstants.INSTRUCTOR_ROLE));
        if (alreadyInstructor) {
            throw BadRequestException.message("Bạn đã là giảng viên rồi");
        }

        // Lấy role INSTRUCTOR từ cơ sở dữ liệu
        Role instructorRole = roleRepository.findByCode(AuthConstants.INSTRUCTOR_ROLE);
        if (instructorRole == null) {
            throw BadRequestException.message("Không tìm thấy vai trò giảng viên trong hệ thống");
        }

        // Thêm role INSTRUCTOR cho người dùng
        userRoles.add(instructorRole);
        currentUser.setRoles(userRoles);

        // Lưu vào cơ sở dữ liệu
        User updatedUser = userRepository.save(currentUser);

        // Tạo và trả về thông tin giảng viên
        InstructorInfoDto dto = new InstructorInfoDto();
        dto.setFullName(updatedUser.getFullName());
        dto.setUsername(updatedUser.getUsername());
        dto.setEmail(updatedUser.getEmail());
        dto.setDescription(updatedUser.getDescription());
        dto.setAvatarUrl(updatedUser.getAvatarUrl());

        // send welcome notification to user
        sendWelcomeNotification(updatedUser.getId());
        return dto;
    }

    private void sendWelcomeNotification(UUID userId) {
        String title = "Chào mừng bạn đến cộng đồng giảng viên VinaAcademy";
        String message = "Chúng tôi rất vui khi bạn trở thành một phần của cộng đồng giảng viên tại VinaAcademy.";
        String targetUrl = AppConfig.INSTANCE.getFrontendUrl() + "/instructor/dashboard";
        NotificationCreateDTO request = NotificationCreateDTO.builder()
                .userId(userId)
                .title(title)
                .content(message)
                .targetUrl(targetUrl)
                .type(NotificationType.SYSTEM)
                .build();

        notificationService.createNotification(request);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public boolean isInstructor(UUID userId) {
//        // Tìm người dùng trong hệ thống
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng"));
//
//        // Kiểm tra xem người dùng có role INSTRUCTOR không
//        return user.getRoles().stream()
//                .anyMatch(role -> role.getCode().equalsIgnoreCase(AuthConstants.INSTRUCTOR_ROLE));
//    }
}