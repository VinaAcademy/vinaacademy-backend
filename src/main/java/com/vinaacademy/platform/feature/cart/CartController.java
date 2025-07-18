package com.vinaacademy.platform.feature.cart;

import com.vinaacademy.platform.feature.cart.dto.CartDto;
import com.vinaacademy.platform.feature.cart.dto.CartItemDto;
import com.vinaacademy.platform.feature.cart.dto.CartItemRequest;
import com.vinaacademy.platform.feature.cart.dto.CartRequest;
import com.vinaacademy.platform.feature.cart.service.CartItemService;
import com.vinaacademy.platform.feature.cart.service.CartService;
import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Quản lý giỏ hàng")
public class CartController {
    @Autowired
	private CartService cartService;
    @Autowired
	private CartItemService cartItemService;
	
	@HasAnyRole({AuthConstants.STUDENT_ROLE}) 
    @PostMapping
    @Operation(summary = "Tạo giỏ hàng mới", description = "Tạo giỏ hàng mới cho học viên")
    public ResponseEntity<ApiResponse<CartDto>> createCart(@RequestBody @Valid CartRequest request) {
        log.debug("Cart created");
        CartDto cart = cartService.createCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CartDto>builder()
                        .status("success")
                        .message("Tạo giỏ hàng thành công")
                        .data(cart)
                        .build());    }

    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/{userId}")
    @Operation(summary = "Lấy thông tin giỏ hàng", description = "Lấy thông tin giỏ hàng của học viên")
    public ResponseEntity<ApiResponse<CartDto>> getCart(@PathVariable UUID userId) {
        log.debug("Get cart for user " + userId);
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.<CartDto>builder()
                .status("success")
                .data(cart)
                .build());
    }

    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @PutMapping
    @Operation(summary = "Cập nhật giỏ hàng", description = "Cập nhật thông tin giỏ hàng (áp dụng mã giảm giá)")
    public ResponseEntity<ApiResponse<CartDto>> updateCart(@RequestBody @Valid CartRequest request) {
        log.debug("CART updated");
        // Only student can update his cart
        CartDto cart = cartService.updateCart(request);
        return ResponseEntity.ok(ApiResponse.<CartDto>builder()
                .status("success")
                .message("Cập nhật giỏ hàng thành công")
                .data(cart)
                .build());
    }

    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/{userId}/items")
    @Operation(summary = "Lấy danh sách sản phẩm", description = "Lấy danh sách các khóa học trong giỏ hàng")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getListCartItems(@PathVariable UUID userId) {
        log.debug("Get list cart items for user " + userId);
        List<CartItemDto> cartItems = cartService.getCart(userId).getCartItems();
        return ResponseEntity.ok(ApiResponse.<List<CartItemDto>>builder()
                .status("success")
                .data(cartItems)
                .build());
    }


    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @PostMapping("/items")
    @Operation(summary = "Thêm khóa học vào giỏ", description = "Thêm một khóa học mới vào giỏ hàng")
    public ResponseEntity<ApiResponse<CartItemDto>> createCartItem(@RequestBody @Valid CartItemRequest request) {
        log.debug("Cart item create");
        CartItemDto cartItem = cartItemService.addCartItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CartItemDto>builder()
                        .status("success")
                        .message("Thêm khóa học vào giỏ hàng thành công")
                        .data(cartItem)
                        .build());
    }

    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @PutMapping("/items")
    @Operation(summary = "Cập nhật khóa học", description = "Cập nhật thông tin khóa học trong giỏ hàng")
    public ResponseEntity<ApiResponse<CartItemDto>> updateCartItem(@RequestBody @Valid CartItemRequest request) {
        log.debug("Cart item update");
        CartItemDto cartItem = cartItemService.updateCartItem(request);
        return ResponseEntity.ok(ApiResponse.<CartItemDto>builder()
                .status("success")
                .message("Cập nhật khóa học trong giỏ hàng thành công")
                .data(cartItem)
                .build());
    }

    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Xóa khóa học", description = "Xóa một khóa học khỏi giỏ hàng")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(@PathVariable Long itemId) {
        log.debug("Cart item delete");
        cartItemService.deleteCartItem(itemId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa khóa học khỏi giỏ hàng thành công")
                .build());
    }

    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @GetMapping("/items/{itemId}")
    @Operation(summary = "Xem chi tiết sản phẩm", description = "Xem thông tin chi tiết của một khóa học trong giỏ hàng")
    public ResponseEntity<ApiResponse<CartItemDto>> getCartItem(@PathVariable Long itemId) {
        log.debug("Get 1 cart item");
        CartItemDto cartItem = cartItemService.getCartItem(itemId);
        return ResponseEntity.ok(ApiResponse.<CartItemDto>builder()
                .status("success")
                .data(cartItem)
                .build());
    }

}
