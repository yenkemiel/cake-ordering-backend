package com.kemiel.cakeordering.order.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmailApiClient 私有方法的行為驗證，聚焦 escapeHtml() 的 HTML 特殊字元跳脫邏輯
 * escapeHtml() 為 private 方法，不調整方法可見度以維持封裝，改用 ReflectionTestUtils
 * 直接呼叫驗證，避免為了測試破壞既有的存取權限設計
 */
class EmailApiClientTest {

    private final EmailApiClient emailApiClient = new EmailApiClient(new RestTemplateBuilder());

    @Test
    void escapeHtml_純文字_原樣返回() {
        String result = ReflectionTestUtils.invokeMethod(emailApiClient, "escapeHtml", "王小明");
        assertThat(result).isEqualTo("王小明");
    }

    @Test
    void escapeHtml_含script標籤_正確轉義() {
        String result = ReflectionTestUtils.invokeMethod(
                emailApiClient, "escapeHtml", "<script>alert(1)</script>");
        assertThat(result).isEqualTo("&lt;script&gt;alert(1)&lt;/script&gt;");
    }

    @Test
    void escapeHtml_含AndSymbol_不重複轉義() {
        String result = ReflectionTestUtils.invokeMethod(emailApiClient, "escapeHtml", "A & B < C");
        assertThat(result).isEqualTo("A &amp; B &lt; C");
    }

    @Test
    void escapeHtml_含雙引號與單引號_正確轉義() {
        String result = ReflectionTestUtils.invokeMethod(emailApiClient, "escapeHtml", "\"test\" 'quote'");
        assertThat(result).isEqualTo("&quot;test&quot; &#x27;quote&#x27;");
    }

    @Test
    void escapeHtml_輸入為null_回傳空字串() {
        String result = ReflectionTestUtils.invokeMethod(emailApiClient, "escapeHtml", (String) null);
        assertThat(result).isEqualTo("");
    }
}