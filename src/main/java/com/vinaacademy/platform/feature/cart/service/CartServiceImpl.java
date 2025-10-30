package com.vinaacademy.platform.feature.cart.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.cart.dto.CartDto;
import com.vinaacademy.platform.feature.cart.dto.CartRequest;
import com.vinaacademy.platform.feature.cart.entity.Cart;
import com.vinaacademy.platform.feature.cart.mapper.CartMapper;
import com.vinaacademy.platform.feature.cart.repository.CartRepository;
import com.vinaacademy.platform.feature.order_payment.entity.Coupon;
import com.vinaacademy.platform.feature.order_payment.repository.CouponRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private SecurityHelper securityHelper;

    @Override
    public CartDto getCart() {
    	User user = securityHelper.getCurrentUser();
    	UUID userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        CartDto cartDto = cartMapper.toDTO(cart);

        if (cart == null) {
            cartDto = createCart(CartRequest.builder()
                    .user_id(userId)
                    .coupon_id(null)
                    .build());
        }
        return cartDto;
    }

    @Override
    public CartDto createCart(CartRequest request) {
        if (cartRepository.existsByUserId(request.getUser_id()))
            throw BadRequestException.message("Cart của ID người dùng này đã tồn tại");

        User userp = userRepository.findById(request.getUser_id())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy ID người dùng này"));
        Cart cart = Cart.builder().user(userp).coupon(null).build();
        cartRepository.save(cart);
        CartDto cartDto = cartMapper.toDTO(cart);
        return cartDto;
    }

    @Override
    public CartDto updateCart(CartRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUser_id())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy Cart của ID người dùng này"));
        User user = securityHelper.getCurrentUser();
    	UUID userId = user.getId();
    	
    	if (request.getUser_id() != userId) {
    		throw BadRequestException.message("Bạn không có quyền sở hữu với cart này");
    	}
    	
        Coupon coupon = null;
        if (request.getCoupon_id() != null) {
            coupon = couponRepository.findById(request.getCoupon_id())
                    .orElseThrow(() -> BadRequestException.message("Không tìm thấy coupon"));
        }
        
        cart.setCoupon(coupon);
        cart.setUser(user);
        cartRepository.save(cart);
        CartDto cartDto = cartMapper.toDTO(cart);
        return cartDto;
    }

    @Override
    public void deleteCart(CartRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUser_id())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy Cart của ID người dùng này"));
        
        User user = securityHelper.getCurrentUser();
    	UUID userId = user.getId();
    	if (request.getUser_id() != userId) {
    		throw BadRequestException.message("Bạn không có quyền sở hữu với cart này");
    	}
    	
        cartRepository.delete(cart);
    }

}
