package com.kemiel.cakeordering.order.email;

import com.kemiel.cakeordering.order.dto.OrderItemResponse;
import com.kemiel.cakeordering.order.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封裝呼叫 Resend HTTP API 寄送訂單通知信。
 * 僅負責「怎麼呼叫 Resend、怎麼組信件內容」，失敗時例外往外拋，
 * 由呼叫端（OrderServiceImpl）負責 try-catch 與記 log
 */
@Slf4j
@Component
public class EmailApiClient {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    /**
     * 下單時間顯示格式，取代 LocalDateTime 預設 toString() 帶 'T' 與微秒的格式
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String SHOP_PHONE = "03-4973-712";

    private static final String SHOP_BUSINESS_HOURS = "10:00–16:00（星期日公休）";

    @Value("${EMAIL_API_KEY}")
    private String apiKey;

    @Value("${EMAIL_SENDER_ADDRESS}")
    private String senderAddress;

    private final RestTemplate restTemplate;

    /**
     * 建構子注入 RestTemplate，並設定連線／讀取逾時，避免 Email 服務商異常時無限期卡住下單流程
     *
     * @param builder Spring 提供的 RestTemplateBuilder
     */
    public EmailApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 寄送訂單成立通知信
     *
     * @param order 已建立成功的訂單資料
     */
    public void sendOrderConfirmation(OrderResponse order) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", senderAddress);
        body.put("to", List.of(order.getEmail()));
        body.put("subject", "WishCake 訂單確認 - " + order.getOrderNo());
        body.put("html", buildHtmlContent(order));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(RESEND_API_URL, requestEntity, String.class);

        log.info("訂單通知信已送出，orderNo={}, to={}", order.getOrderNo(), order.getEmail());
    }

    /**
     * 組裝訂單確認信的 HTML 內容
     * 版面採 table-based layout、樣式一律行內 style 屬性，
     * 對應 HTML Email 在各家信箱（Gmail／Outlook 等）常見的相容性限制，
     * 不使用 &lt;style&gt; 區塊或 Flexbox／Grid
     *
     * @param order 訂單資料
     * @return 完整 HTML 字串
     */
    private String buildHtmlContent(OrderResponse order) {
        StringBuilder html = new StringBuilder();

        html.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#f0eae0; padding:40px 0; "
                + "font-family:'Noto Sans TC','Microsoft JhengHei',sans-serif;\">");
        html.append("<tr><td align=\"center\">");
        html.append("<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#ffffff; border-radius:12px; overflow:hidden;\">");

        appendGreeting(html, order);
        appendOrderInfoCard(html, order);
        appendItemsTable(html, order);
        appendShippingInfoCard(html, order);
        appendTotalAmount(html, order);
        appendFooter(html);

        html.append("</table>");
        html.append("</td></tr>");
        html.append("</table>");

        return html.toString();
    }

    /**
     * 問候語與感謝文字
     */
    private void appendGreeting(StringBuilder html, OrderResponse order) {
        html.append("<tr><td style=\"padding:32px 40px 0 40px;\">");
        html.append("<p style=\"font-size:16px; color:#6d675b; margin:0 0 8px 0;\">親愛的 ")
                .append(escapeHtml(order.getCustomerName()))
                .append(" 您好，</p>");
        html.append("<p style=\"font-size:14px; color:#6d675b; margin:0; line-height:1.6;\">"
                + "感謝您在 WishCake 訂購，我們已收到您的訂單，以下是訂單明細：</p>");
        html.append("</td></tr>");
    }

    /**
     * 訂單基本資訊卡：訂單編號、聯絡電話、下單時間
     */
    private void appendOrderInfoCard(StringBuilder html, OrderResponse order) {
        html.append("<tr><td style=\"padding:24px 40px 0 40px;\">");
        html.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#f0eae0; border-radius:8px;\">");
        html.append("<tr><td style=\"padding:16px 20px;\">");
        html.append("<p style=\"margin:0 0 6px 0; font-size:14px; color:#6d675b;\">訂單編號：<strong>")
                .append(order.getOrderNo())
                .append("</strong></p>");
        html.append("<p style=\"margin:0 0 6px 0; font-size:14px; color:#6d675b;\">下單時間：")
                .append(order.getCreatedAt().format(DATE_TIME_FORMATTER))
                .append("</p>");
        html.append("<p style=\"margin:0; font-size:14px; color:#6d675b;\">訂購人電話：")
                .append(escapeHtml(order.getPhone()))
                .append("</p>");
        html.append("</td></tr>");
        html.append("</table>");
        html.append("</td></tr>");

        html.append("<tr><td style=\"padding:8px 40px 0 40px;\">");
        html.append("<p style=\"margin:0; font-size:12px; color:#a88f80;\">"
                + "日後如需查詢訂單狀態，請至官網的訂單查詢頁面，輸入上方訂單編號與聯絡電話即可查詢</p>");
        html.append("</td></tr>");
    }

    /**
     * 品項明細表格
     */
    private void appendItemsTable(StringBuilder html, OrderResponse order) {
        html.append("<tr><td style=\"padding:24px 40px 0 40px;\">");
        html.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;\">");

        html.append("<tr style=\"background-color:#a88f80;\">");
        html.append("<th style=\"padding:10px 8px; color:#ffffff; font-size:13px; text-align:left;\">品項</th>");
        html.append("<th style=\"padding:10px 8px; color:#ffffff; font-size:13px; text-align:left;\">尺寸</th>");
        html.append("<th style=\"padding:10px 8px; color:#ffffff; font-size:13px; text-align:center;\">數量</th>");
        html.append("<th style=\"padding:10px 8px; color:#ffffff; font-size:13px; text-align:right;\">單價</th>");
        html.append("<th style=\"padding:10px 8px; color:#ffffff; font-size:13px; text-align:right;\">小計</th>");
        html.append("</tr>");

        for (OrderItemResponse item : order.getItems()) {
            html.append("<tr style=\"border-bottom:1px solid #ead4d4;\">");
            html.append("<td style=\"padding:10px 8px; font-size:13px; color:#6d675b;\">")
                    .append(item.getProductName()).append("</td>");
            html.append("<td style=\"padding:10px 8px; font-size:13px; color:#6d675b;\">")
                    .append(item.getVariantSize() == null ? "-" : item.getVariantSize()).append("</td>");
            html.append("<td style=\"padding:10px 8px; font-size:13px; color:#6d675b; text-align:center;\">")
                    .append(item.getQuantity()).append("</td>");
            html.append("<td style=\"padding:10px 8px; font-size:13px; color:#6d675b; text-align:right;\">")
                    .append(formatAmount(item.getUnitPrice())).append("</td>");
            html.append("<td style=\"padding:10px 8px; font-size:13px; color:#6d675b; text-align:right;\">")
                    .append(formatAmount(item.getSubtotal())).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("</td></tr>");
    }

    /**
     * 配送資訊卡：取貨／配送方式、地址、付款方式、運費、日期、留言
     */
    private void appendShippingInfoCard(StringBuilder html, OrderResponse order) {
        html.append("<tr><td style=\"padding:24px 40px 0 40px;\">");
        html.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#f0eae0; border-radius:8px;\">");
        html.append("<tr><td style=\"padding:16px 20px;\">");

        appendInfoLine(html, "取貨／配送方式", translateShippingMethod(order.getShippingMethod()));

        if (order.getAddress() != null && !order.getAddress().isBlank()) {
            appendInfoLine(html, "配送地址", escapeHtml(order.getAddress()));
        }

        appendInfoLine(html, "付款方式", translatePaymentMethod(order.getPaymentMethod()));
        appendInfoLine(html, "付款狀態", translatePaymentStatus(order.getPaymentMethod()));
        appendInfoLine(html, "運費", formatAmount(order.getShippingFee()));
        appendInfoLine(html, "取貨／配送日期", order.getPickupDate().format(DATE_FORMATTER));

        if (order.getRemark() != null && !order.getRemark().isBlank()) {
            appendInfoLine(html, "留言", escapeHtml(order.getRemark()));
        }

        html.append("</td></tr>");
        html.append("</table>");
        html.append("</td></tr>");
    }

    /**
     * 配送資訊卡內單行「標籤：內容」的共用組裝邏輯
     *
     * @param html  組裝中的 StringBuilder
     * @param label 欄位標籤
     * @param value 欄位內容
     */
    private void appendInfoLine(StringBuilder html, String label, String value) {
        html.append("<p style=\"margin:0 0 6px 0; font-size:14px; color:#6d675b;\">")
                .append(label).append("：").append(value).append("</p>");
    }

    /**
     * 總金額強調區塊
     */
    private void appendTotalAmount(StringBuilder html, OrderResponse order) {
        html.append("<tr><td style=\"padding:24px 40px 0 40px; text-align:right;\">");
        html.append("<p style=\"margin:0; font-size:20px; color:#b7908f; font-weight:bold;\">總金額：")
                .append(formatAmount(order.getTotalAmount()))
                .append("</p>");
        html.append("</td></tr>");
    }

    /**
     * 信件結尾：感謝文字、異動/取消提醒、客服電話與營業時間、品牌 slogan
     */
    private void appendFooter(StringBuilder html) {
        html.append("<tr><td style=\"padding:40px 40px 32px 40px; text-align:center;\">");
        html.append("<p style=\"margin:0 0 8px 0; font-size:13px; color:#a88f80;\">"
                + "感謝您選擇 WishCake，期待您的下一次訂購</p>");
        html.append("<p style=\"margin:0 0 8px 0; font-size:12px; color:#a88f80;\">"
                + "如需異動或取消訂單，請致電門市洽詢，恕無法透過本信件或線上系統異動訂單內容</p>");
        html.append("<p style=\"margin:0 0 16px 0; font-size:13px; color:#a88f80;\">客服電話：")
                .append(SHOP_PHONE)
                .append("　｜　營業時間：")
                .append(SHOP_BUSINESS_HOURS)
                .append("</p>");
        html.append("<p style=\"margin:0; font-size:12px; color:#6d675b;\">WishCake．手作甜點</p>");
        html.append("</td></tr>");
    }

    /**
     * 取貨／配送方式代碼轉中文顯示
     * 與 CreateOrderRequest 的 @Pattern(regexp = "^(PICKUP|DELIVERY)$") 規則同步維護，
     * 若該處新增第三種配送方式，這裡也要一併補上對應分支，否則會 fallback 印出英文代碼
     *
     * @param shippingMethod 原始代碼（DELIVERY／PICKUP）
     * @return 中文顯示文字
     */
    private String translateShippingMethod(String shippingMethod) {
        if ("DELIVERY".equals(shippingMethod)) {
            return "宅配到府";
        }
        if ("PICKUP".equals(shippingMethod)) {
            return "門市取貨";
        }
        return shippingMethod;
    }

    /**
     * 付款方式代碼轉中文顯示
     * 與 CreateOrderRequest 的 @Pattern(regexp = "^(ONLINE_PAYMENT|STORE_PAYMENT)$") 規則同步維護，
     * 若該處新增第三種付款方式，這裡也要一併補上對應分支，否則會 fallback 印出英文代碼
     *
     * @param paymentMethod 原始代碼（ONLINE_PAYMENT／STORE_PAYMENT）
     * @return 中文顯示文字
     */
    private String translatePaymentMethod(String paymentMethod) {
        if ("ONLINE_PAYMENT".equals(paymentMethod)) {
            return "線上付款";
        }
        if ("STORE_PAYMENT".equals(paymentMethod)) {
            return "到店付款";
        }
        return paymentMethod;
    }

    /**
     * 依付款方式代碼推導付款狀態顯示文字
     * 注意：此為顯示層推導值，非真實金流回調確認——系統未串接第三方金流，
     * ONLINE_PAYMENT 僅代表客人選擇的付款方式，不代表已實際收到扣款成功通知
     *
     * @param paymentMethod 原始代碼（ONLINE_PAYMENT／STORE_PAYMENT）
     * @return 中文付款狀態顯示文字
     */
    private String translatePaymentStatus(String paymentMethod) {
        return "ONLINE_PAYMENT".equals(paymentMethod) ? "已付款" : "未付款";
    }

    /**
     * 金額格式化為千分位、無小數點顯示，與前台 shared/js/format.js 的 formatCurrency() 顯示格式一致
     *
     * @param amount 金額
     * @return 格式化後字串，例如 "NT$ 1,090"
     */
    private String formatAmount(BigDecimal amount) {
        return "NT$ " + new DecimalFormat("#,##0").format(amount);
    }

    /**
     * 將使用者輸入內容中的 HTML 特殊字元跳脫，避免建立訂單時填入的
     * customerName／address／remark 被當成 HTML 標籤解析，造成信件排版被破壞
     * 或夾帶連結內容（訂單建立為免登入訪客操作，此三欄位無內容過濾，
     * 直接拼接進 HTML 屬於可被濫用的風險）
     * 處理順序刻意將 &amp; 放在最前面，避免後續跳脫產生的 &amp;lt; 等字串被重複跳脫
     *
     * @param input 原始使用者輸入字串，可為 null
     * @return 跳脫後的安全字串；輸入為 null 時回傳空字串
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}