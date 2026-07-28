package com.kemiel.cakeordering.admin.config;

import com.kemiel.cakeordering.admin.entity.Admin;
import com.kemiel.cakeordering.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 應用程式啟動時依環境變數建立初始管理員帳號，帳號已存在則略過
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeedRunner implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_INITIAL_USERNAME}")
    private String initialUsername;

    @Value("${ADMIN_INITIAL_PASSWORD}")
    private String initialPassword;

    /**
     * 檢查初始帳號是否已存在，不存在才建立，密碼經 BCrypt 雜湊後寫入
     */
    @Override
    public void run(String... args) {
        if (adminRepository.existsByUsername(initialUsername)) {
            log.info("初始管理員帳號已存在，略過建立");
            return;
        }
        Admin admin = new Admin(initialUsername, passwordEncoder.encode(initialPassword));
        adminRepository.save(admin);
        log.info("初始管理員帳號已建立，username={}", initialUsername);
    }
}