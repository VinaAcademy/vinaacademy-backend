package com.vinaacademy.platform.feature.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.*;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private String message;
    private Integer code;
    private T data;
    private Instant timestamp;

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public ApiResponse(String status, String message, Integer code, T data) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", null, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>("ERROR", message, code, null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return new ApiResponse<>("ERROR", message, code, data);
    }
}