package com.kemiel.cakeordering.common.exception;

import com.kemiel.cakeordering.common.response.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全域例外處理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(resolveHttpStatus(e.getErrorCode()))
                .body(ApiResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.STOCK_VERSION_CONFLICT));
    }

    /**
     * 攔截 MySQL 死鎖與鎖等待逾時（Spring 統一轉譯為 CannotAcquireLockException）；
     * 屬於高併發下可預期的資料庫底層競爭，記 warn 而非 error，跟真正未知例外分開排查
     */
    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<ApiResponse<Void>> handleConcurrentUpdateConflict(CannotAcquireLockException e) {
        log.warn("資料庫鎖競爭（死鎖或鎖等待逾時），已回應 409 請前端重試", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.CONCURRENT_UPDATE_CONFLICT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED, INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case PRODUCT_NOT_FOUND, VARIANT_NOT_FOUND, ORDER_NOT_FOUND, ADMIN_ORDER_NOT_FOUND, CATEGORY_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;
            case INVALID_ORDER_STATUS_TRANSITION, ORDER_CANCEL_NOT_ALLOWED,
                 STOCK_VERSION_CONFLICT, CATEGORY_NAME_DUPLICATE, CATEGORY_IN_USE,
                 VARIANT_DELETE_NOT_ALLOWED, CONCURRENT_UPDATE_CONFLICT, ORDER_NO_DUPLICATE -> HttpStatus.CONFLICT;
            case VALIDATION_ERROR, CATEGORY_INVALID, INSUFFICIENT_STOCK -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}