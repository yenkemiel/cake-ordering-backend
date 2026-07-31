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

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封裝呼叫 Resend 寄送訂單通知信的 HTTP Client
 */
@Slf4j
@Component
public class EmailApiClient {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${EMAIL_API_KEY}")
    private String apiKey;

    @Value("${EMAIL_SENDER_ADDRESS}")
    private String senderAddress;

    private final RestTemplate restTemplate;

    public EmailApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 呼叫 Resend API 寄送訂單成立通知信，失敗時例外直接往外拋，由呼叫端負責捕捉與記錄 log
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
     * 組裝訂單確認信的 HTML 內文，涵蓋訂單編號、品項明細、金額、配送資訊與留言
     */
    private String buildHtmlContent(OrderResponse order) {
        StringBuilder html = new StringBuilder();
        html.append("<h2>WishCake 訂單確認</h2>");
        html.append("<p>訂單編號：").append(order.getOrderNo()).append("</p>");
        html.append("<p>下單時間：").append(order.getCreatedAt()).append("</p>");

        html.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
        html.append("<tr><th>品項</th><th>尺寸</th><th>數量</th><th>單價</th><th>小計</th></tr>");
        for (OrderItemResponse item : order.getItems()) {
            html.append("<tr>")
                    .append("<td>").append(item.getProductName()).append("</td>")
                    .append("<td>").append(item.getVariantSize() == null ? "-" : item.getVariantSize()).append("</td>")
                    .append("<td>").append(item.getQuantity()).append("</td>")
                    .append("<td>").append(item.getUnitPrice()).append("</td>")
                    .append("<td>").append(item.getSubtotal()).append("</td>")
                    .append("</tr>");
        }
        html.append("</table>");

        html.append("<p>取貨／配送方式：").append(order.getShippingMethod()).append("</p>");
        html.append("<p>運費：").append(order.getShippingFee()).append("</p>");
        html.append("<p>取貨／配送日期：").append(order.getPickupDate()).append("</p>");

        if (order.getRemark() != null && !order.getRemark().isBlank()) {
            html.append("<p>留言：").append(order.getRemark()).append("</p>");
        }

        html.append("<p><strong>總金額：").append(order.getTotalAmount()).append("</strong></p>");

        return html.toString();
    }
}