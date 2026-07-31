package com.kemiel.cakeordering;

import org.junit.jupiter.api.Test;

/**
 * 應用程式啟動與 Spring Context 載入驗證，改用 Testcontainers 容器化 MySQL，不依賴本機 .env
 */
class CakeOrderingBackendApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}