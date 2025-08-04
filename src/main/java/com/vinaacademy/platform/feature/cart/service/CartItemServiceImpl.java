package com.vinaacademy.platform.feature.cart.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.cart.dto.CartItemDto;
import com.vinaacademy.platform.feature.cart.dto.CartItemRequest;
import com.vinaacademy.platform.feature.cart.entity.Cart;
import com.vinaacademy.platform.feature.cart.entity.CartItem;
import com.vinaacademy.platform.feature.cart.mapper.CartMapper;
import com.vinaacademy.platform.feature.cart.repository.CartItemRepository;
import com.vinaacademy.platform.feature.cart.repository.CartRepository;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CartItemServiceImpl implements CartItemService{
	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private CartItemRepository cartItemRepository;
	@Autowired
	private CartMapper cartMapper;
	@Autowired
	private CourseRepository courseRepository;
	
	@Override
	public CartItemDto addCartItem(CartItemRequest request) {
		Cart cart = cartRepository.findById(request.getCart_id()).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Cart ID này"));
		Course course = courseRepository.findById(request.getCourse_id()).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Course ID này"));
		
		if (cartItemRepository.existsByCourseIdAndCart(course.getId(), cart))
			throw BadRequestException.message("Duplicate course: Course id này đã tồn tại trong giỏ hàng");
		
		CartItem cartItem = CartItem.builder()
				.course(course)
				.cart(cart)
				.price(request.getPrice())
				.addedAt(LocalDateTime.now())
				.build();
		
		cartItemRepository.save(cartItem);
		
		CartItemDto cartItemDto = cartMapper.toCartItemDTO(cartItem);
		return cartItemDto;
	}

	@Override
	public CartItemDto updateCartItem(CartItemRequest request) {
		Cart cart = cartRepository.findById(request.getCart_id()).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Cart ID này"));
		Course course = courseRepository.findById(request.getCourse_id()).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Course ID này"));
		
		CartItem cartItem = cartItemRepository.findById(request.getId()).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Cart Item này"));
		
		
		cartItem.setCourse(course);
		cartItem.setCart(cart);
		cartItem.setPrice(request.getPrice());				
		
		cartItemRepository.save(cartItem);
		
		CartItemDto cartItemDto = cartMapper.toCartItemDTO(cartItem);
		return cartItemDto;
	}

	@Override
	public void deleteCartItem(Long cartItemId) {
		CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Cart Item này"));
		cartItemRepository.delete(cartItem);
	}

	@Override
	public List<CartItemDto> getCartItems(Long cartId) {
		Cart cart = cartRepository.findById(cartId).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Cart ID này"));
		List<CartItemDto> cartItemDtos = cartMapper.toCartItemDTOList(cart.getCartItems());
		return cartItemDtos;
	}
	
	@Override
	public CartItemDto getCartItem(Long cartItemId) {
		CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy ID Cart Item này"));
		CartItemDto cartItemDto = cartMapper.toCartItemDTO(cartItem);
		return cartItemDto;
	}

	@Override
	public List<CartItem> getCartItems(UUID userId) {
		Cart cart = cartRepository.findByUserId(userId).orElseThrow(
				() -> BadRequestException.message("Không tìm thấy Cart của User ID này"));
		return cart.getCartItems();
	}

}
