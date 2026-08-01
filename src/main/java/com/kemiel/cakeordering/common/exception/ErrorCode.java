package com.kemiel.cakeordering.common.exception;

/**
 * 系統錯誤代碼
 */
public enum ErrorCode {

    // 通用
    SUCCESS("SUCCESS", "操作成功"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系統發生非預期錯誤，請稍後再試"),
    CONCURRENT_UPDATE_CONFLICT("CONCURRENT_UPDATE_CONFLICT", "系統忙碌，請稍後再試"),
    VALIDATION_ERROR("VALIDATION_ERROR", "請求參數格式或內容不正確，請確認後重新送出"),
    UNAUTHORIZED("UNAUTHORIZED", "請先登入管理後台"),
    FORBIDDEN("FORBIDDEN", "您無此操作權限"),

    // 管理員帳號
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "帳號或密碼錯誤，請重新輸入"),

    // 商品
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "找不到此商品，可能不存在或已被移除"),
    CATEGORY_INVALID("CATEGORY_INVALID", "指定的商品分類不存在，請重新選擇"),

    // 變體（各尺寸）
    VARIANT_NOT_FOUND("VARIANT_NOT_FOUND", "找不到此尺寸選項，可能已被移除"),
    VARIANT_DELETE_NOT_ALLOWED("VARIANT_DELETE_NOT_ALLOWED", "商品至少需保留一個尺寸選項，如需整個商品下架請使用刪除商品功能"),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "商品庫存不足，無法完成訂購"),
    STOCK_VERSION_CONFLICT("STOCK_VERSION_CONFLICT", "商品庫存狀態已被其他訂單異動，請重新整理後再試一次"),

    // 訂單
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "查無此訂單，請確認訂單編號與電話是否正確"),
    INVALID_ORDER_STATUS_TRANSITION("INVALID_ORDER_STATUS_TRANSITION", "訂單目前狀態不允許此操作"),
    ORDER_CANCEL_NOT_ALLOWED("ORDER_CANCEL_NOT_ALLOWED", "此訂單已出貨或已完成，無法取消"),

    // Email 通知（僅供內部記錄，不對外回傳給前端）
    EMAIL_SEND_FAILED("EMAIL_SEND_FAILED", "訂單通知信寄送失敗"),

    // 商品分類管理
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "商品分類不存在"),
    CATEGORY_NAME_DUPLICATE("CATEGORY_NAME_DUPLICATE", "此分類名稱已存在，請使用其他名稱"),
    CATEGORY_IN_USE("CATEGORY_IN_USE", "此分類目前仍有商品使用中，請先將商品改分類或下架後再刪除");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}