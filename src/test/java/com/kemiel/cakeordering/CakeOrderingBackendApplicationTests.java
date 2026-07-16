package com.kemiel.cakeordering;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("依賴真實 DB 連線環境變數，GitHub Actions runner 無法連線；待第 3 週 Testcontainers 導入後以容器化 MySQL 復原")
@SpringBootTest
class CakeOrderingBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}