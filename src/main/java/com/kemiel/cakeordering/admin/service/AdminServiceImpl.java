package com.kemiel.cakeordering.admin.service;

import com.kemiel.cakeordering.admin.dto.LoginRequest;
import com.kemiel.cakeordering.admin.dto.LoginResponse;
import com.kemiel.cakeordering.admin.entity.Admin;
import com.kemiel.cakeordering.admin.repository.AdminRepository;
import com.kemiel.cakeordering.common.constant.SessionConstants;
import com.kemiel.cakeordering.common.exception.BusinessException;
import com.kemiel.cakeordering.common.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理員 Service 實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 驗證帳密並於通過後將 adminId 寫入 Session，帳號不存在或密碼錯誤一律回同一種例外
     */
    @Override
    public LoginResponse login(LoginRequest request, HttpSession session) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        session.setAttribute(SessionConstants.ADMIN_ID, admin.getId());
        log.info("管理員登入成功，adminId={}", admin.getId());
        return new LoginResponse(admin.getUsername());
    }

    /**
     * 使 Session 失效；呼叫時未帶有效 Session 視為已登出，仍屬成功（冪等）
     *
     * @param session 目前請求關聯的 Session，未登入時為 null
     */
    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
            log.info("管理員已登出");
        } else {
            log.info("登出呼叫時未帶有效 Session，視為已登出");
        }
    }
}