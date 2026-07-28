package com.kemiel.cakeordering.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理員登入請求 DTO
 */
public class LoginRequest {

    @NotBlank(message = "帳號不可為空")
    private String username;

    @NotBlank(message = "密碼不可為空")
    private String password;

    public LoginRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}