package com.kemiel.cakeordering.common.response;

import com.kemiel.cakeordering.common.exception.ErrorCode;

import java.time.LocalDateTime;

/**
 * 統一 API 回應格式
 *
 * @param <T> 回應資料的型別
 */
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode.getCode(), message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}