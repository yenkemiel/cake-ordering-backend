package com.kemiel.cakeordering;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

/**
 * 整合測試共用基底類別，統一管理容器化 MySQL。刻意不用 @Container／
 *
 * @Testcontainers（JUnit5 會在每個測試類別跑完時停掉這個共用容器，
 * 導致下一個類別的 Spring context 沿用已失效的舊連線，詳見日誌-12），
 * 改為 static 區塊手動啟動一次、交由 Ryuk 於 JVM 結束時清理。
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("cake_ordering_test")
            .withUsername("test")
            .withPassword("test");

    static {
        mysql.start();
    }
}