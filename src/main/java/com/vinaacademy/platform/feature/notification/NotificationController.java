package com.vinaacademy.platform.feature.notification;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.notification.dto.NotificationCreateDTO;
import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import com.vinaacademy.platform.feature.notification.service.NotificationService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    
//    @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STUDENT_ROLE})
//    @PostMapping
//    public ApiResponse<NotificationDTO> createNotification(@RequestBody NotificationCreateDTO dto) {
//        log.debug("create notification for user with id {}", dto.getUserId());
//        return ApiResponse.success(notificationService.createNotification(dto));
//    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @Operation(summary = "Lấy thông báo", description = "Danh sách thông báo của student")
    @GetMapping("/paginated")
    public ApiResponse<Page<NotificationDTO>> getUserNotificationsPaginated(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
    	
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        log.debug("get pagination page: {}, size: {}, sortBy: {}, direction: {}, type: {}, isRead: {}", page, size, sortBy, direction, type, isRead);
        return ApiResponse.success(notificationService.getUserNotificationsPaginated(isRead, type, pageable));
        
    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @Operation(summary = "Đọc thông báo", description = "Đánh dấu thông báo này đã đọc")
    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        log.debug("Mark read notification ", notificationId);
    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @Operation(summary = "Đọc tất cả thông báo", description = "Đánh dấu tất cả thông báo này đã đọc")
    @PostMapping("/readall")
    public void markAllAsRead() {
        notificationService.markReadAll();
        log.debug("Marked read all notifications");
    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
    @Operation(summary = "Xóa thông báo", description = "Đánh dấu thông báo này bị xóa")
    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        log.debug("Mark delete notification {}", notificationId);
    }
    
    
    
}