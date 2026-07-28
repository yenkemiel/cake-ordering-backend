package com.kemiel.cakeordering.admin.interceptor;

import com.kemiel.cakeordering.common.constant.SessionConstants;
import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 後台 Session 驗證攔截器，保護 /api/admin/** 路徑（登入／登出本身除外）
 */
@Slf4j
@Component
public class AdminSessionInterceptor implements HandlerInterceptor {

    /**
     * 檢查 Session 是否存在且含有效管理員屬性，否則直接拋出 401，不進入 Controller
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConstants.ADMIN_ID) == null) {
            log.info("未登入呼叫後台 API，path={}", request.getRequestURI());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return true;
    }
}