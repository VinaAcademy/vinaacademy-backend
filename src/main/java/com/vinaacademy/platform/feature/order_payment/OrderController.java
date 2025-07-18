package com.vinaacademy.platform.feature.order_payment;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.order_payment.dto.OrderCouponRequest;
import com.vinaacademy.platform.feature.order_payment.dto.OrderDto;
import com.vinaacademy.platform.feature.order_payment.dto.OrderItemDto;
import com.vinaacademy.platform.feature.order_payment.dto.OrderRequest;
import com.vinaacademy.platform.feature.order_payment.service.OrderItemService;
import com.vinaacademy.platform.feature.order_payment.service.OrderService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
	private final OrderService orderService;
	
	private final OrderItemService orderItemService;
	
	@HasAnyRole({AuthConstants.STUDENT_ROLE}) 
    @PostMapping
    public ApiResponse<OrderDto> createOrder() {
        return ApiResponse.success(orderService.createOrder());
    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/detail/{orderId}")
    public ApiResponse<OrderDto> getOrder(@PathVariable UUID orderId) {
    	log.debug("Get order "+orderId);
    	return ApiResponse.success(orderService.getOrder(orderId));
    }
    
//    @HasAnyRole({AuthConstants.STUDENT_ROLE}) 
//    @DeleteMapping("/{orderId}")
//    public ApiResponse<Void> deleteCartItem(@PathVariable UUID orderId) {
//        log.debug("Cart item delete");
//        orderService.deleteOrder(orderId);
//        return ApiResponse.success("Xóa order "+orderId +" thanh cong");
//    }
    
//    @HasAnyRole({AuthConstants.STUDENT_ROLE}) 
//    @PutMapping
//    public ApiResponse<OrderDto> updateOrder(@RequestBody @Valid OrderRequest request) {
//        log.debug("Order updated");
//        return ApiResponse.success(orderService.updateOrder(request));
//    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE}) 
    @GetMapping("/list")
    public ApiResponse<Page<OrderDto>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDto> orders = orderService.getOrders(pageable);
        return ApiResponse.success(orders);
    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE}) 
    @GetMapping("/listitems/{orderId}")
    public ApiResponse<List<OrderItemDto>> getListCartItems(@PathVariable UUID orderId) {
    	log.debug("Get list items of order  "+orderId);
        return ApiResponse.success(orderItemService.getOrderItems(orderId));
    }
    
    @HasAnyRole({AuthConstants.STUDENT_ROLE}) 
    @PutMapping("/coupon")
    public ApiResponse<OrderDto> updateCouponForOrder(@RequestBody @Valid OrderCouponRequest orderCouponRequest) {
    	log.debug("Update coupon for "+orderCouponRequest.getOrderId());
        return ApiResponse.success(orderService.updateCoupon(orderCouponRequest));
    }
    
}
