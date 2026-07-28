package com.kemiel.cakeordering.admin.dto;

/**
 * 管理員登入回應 DTO
 */
public class LoginResponse {

    private final String username;

    public LoginResponse(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}