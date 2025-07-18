package com.vinaacademy.platform.feature.cart.mapper;

import com.vinaacademy.platform.feature.cart.dto.CartDto;
import com.vinaacademy.platform.feature.cart.dto.CartItemDto;
import com.vinaacademy.platform.feature.cart.entity.Cart;
import com.vinaacademy.platform.feature.cart.entity.CartItem;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "coupon.id", target = "couponId")
    @Mapping(source = "user.id", target = "userId")
    CartDto toDTO(Cart cart);

   
    @InheritInverseConfiguration
    Cart toEntity(CartDto cartDTO);
    
    List<CartItemDto> toCartItemDTOList(List<CartItem> cartItems);

    @Mapping(source = "course.id", target = "courseId")
    CartItemDto toCartItemDTO(CartItem cartItem);

    
    List<CartItem> toCartItemEntityList(List<CartItemDto> cartItemDTOs);

    @InheritInverseConfiguration
    @Mapping(target = "cart", ignore = true)
    CartItem toCartItemEntity(CartItemDto cartItemDto);
}