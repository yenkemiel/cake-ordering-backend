package com.kemiel.cakeordering.admin.service;

import com.kemiel.cakeordering.admin.dto.LoginRequest;
import com.kemiel.cakeordering.admin.dto.LoginResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 管理員 Service 介面
 */
public interface AdminService {

    LoginResponse login(LoginRequest request, HttpSession session);

    void logout(HttpSession session);
}