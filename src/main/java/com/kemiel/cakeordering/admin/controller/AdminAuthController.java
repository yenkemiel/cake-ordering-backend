package com.kemiel.cakeordering.admin.controller;

import com.kemiel.cakeordering.admin.dto.LoginRequest;
import com.kemiel.cakeordering.admin.dto.LoginResponse;
import com.kemiel.cakeordering.admin.service.AdminService;
import com.kemiel.cakeordering.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理員登入／登出 Controller
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Auth", description = "管理員登入／登出")
public class AdminAuthController {

    private final AdminService adminService;

    @Operation(summary = "[FR-ADM-001] 管理員登入")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success(adminService.login(request, session)));
    }

    @Operation(summary = "[FR-ADM-002] 管理員登出")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        adminService.logout(request.getSession(false));
        return ResponseEntity.ok(ApiResponse.success());
    }
}