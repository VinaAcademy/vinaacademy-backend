package com.vinaacademy.platform.feature.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRequest {
    @Size(min = 2, max = 50, message = "Họ tên phải có độ dài từ 2 đến 50 ký tự")
    private String fullName;
    
    @Size(max = 1500, message = "Mô tả không được vượt quá 1500 ký tự")
    private String description;
    
    @Size(max = 255, message = "URL ảnh đại diện không được vượt quá 255 ký tự")
    private String avatarUrl;
    
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate birthday;
    
    @Pattern(regexp = "^(0|\\+84|84)(3|5|7|8|9)([0-9]{8})$", 
            message = "Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam hợp lệ")
    private String phone;
}