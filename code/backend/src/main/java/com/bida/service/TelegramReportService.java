package com.bida.service;

import com.bida.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service gửi báo cáo tự động qua Telegram Bot API vào 23:00 hàng ngày.
 *
 * Cấu hình trong application.properties:
 *   telegram.bot.token=YOUR_BOT_TOKEN
 *   telegram.chat.id=YOUR_CHAT_ID
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramReportService {

    private final ReportService reportService;
    private final InvoiceRepository invoiceRepository;

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    private int lastUpdateId = 0; // Để theo dõi tin nhắn mới nhất

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";
    private static final String TELEGRAM_UPDATES_URL = "https://api.telegram.org/bot%s/getUpdates?offset=%d&timeout=10";

    /**
     * Gửi báo cáo doanh thu cuối ngày vào lúc 23:00 mỗi ngày.
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void sendDailyReport() {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.debug("Telegram bot chưa được cấu hình, bỏ qua báo cáo.");
            return;
        }

        try {
            String message = buildDailyReportMessage();
            sendTelegramMessage(message);
            log.info("Đã gửi báo cáo Telegram thành công.");
        } catch (Exception e) {
            log.error("Lỗi gửi báo cáo Telegram: {}", e.getMessage(), e);
        }
    }

    /**
     * Thu thập dữ liệu và xây dựng nội dung tin nhắn báo cáo.
     */
    private String buildDailyReportMessage() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        // 1. Tổng doanh thu hôm nay
        Map<String, Object> kpis = reportService.getKPIs();
        BigDecimal todayRevenue = (BigDecimal) kpis.get("todayRevenue");
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        // 2. Số lượt chơi (số hóa đơn trong ngày)
        long sessionCount = invoiceRepository.countByDateRange(todayStart, todayEnd);

        // 3. Sản phẩm bán chạy nhất
        List<Map<String, Object>> topProducts = reportService.getTopSellingProducts(todayStart, todayEnd);
        String topProductName;
        String topProductQty;
        if (!topProducts.isEmpty()) {
            topProductName = String.valueOf(topProducts.get(0).get("name"));
            topProductQty = String.valueOf(topProducts.get(0).get("quantity"));
        } else {
            topProductName = "Không có";
            topProductQty = "0";
        }

        // 4. So sánh với hôm qua
        BigDecimal todayChange = (BigDecimal) kpis.get("todayChange");
        if (todayChange == null) {
            todayChange = BigDecimal.ZERO;
        }
        String changeEmoji = todayChange.compareTo(BigDecimal.ZERO) >= 0 ? "\uD83D\uDCC8" : "\uD83D\uDCC9"; // 📈 or 📉
        String changeSign = todayChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";

        // Format số tiền kiểu Việt Nam (dấu chấm ngăn cách hàng nghìn)
        String formattedRevenue = formatCurrency(todayRevenue);
        String formattedDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        return String.join("\n",
                "\uD83D\uDCCA *BÁO CÁO CUỐI NGÀY*",                          // 📊
                "\uD83D\uDCC5 Ngày: " + formattedDate,                          // 📅
                "─────────────────",
                "\uD83D\uDCB0 Tổng doanh thu: *" + formattedRevenue + " VNĐ*",  // 💰
                changeEmoji + " So với hôm qua: *" + changeSign + todayChange + "%*",
                "\uD83C\uDFB1 Số lượt chơi: *" + sessionCount + "*",            // 🎱
                "\uD83C\uDFC6 SP bán chạy nhất: *" + topProductName + "* (" + topProductQty + " lần)", // 🏆
                "─────────────────",
                "\uD83E\uDD16 _Báo cáo tự động - Quán Bida_"                    // 🤖
        );
    }

    /**
     * Gửi tin nhắn đến Telegram Bot API.
     */
    private void sendTelegramMessage(String text) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(TELEGRAM_API_URL, botToken);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "Markdown");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(url, request, String.class);
    }

    /**
     * Tự động kiểm tra tin nhắn mới từ Telegram mỗi 5 giây (Long Polling).
     */
    @Scheduled(fixedDelay = 5000)
    public void checkForMessages() {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format(TELEGRAM_UPDATES_URL, botToken, lastUpdateId + 1);
            
            // Dùng ObjectMapper của Jackson để parse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(restTemplate.getForObject(url, String.class));
            JsonNode result = root.path("result");

            if (result.isArray()) {
                for (JsonNode update : result) {
                    lastUpdateId = update.path("update_id").asInt();
                    
                    JsonNode messageNode = update.path("message");
                    String text = messageNode.path("text").asText();
                    String incomingChatId = messageNode.path("chat").path("id").asText();

                    // CHỈ TRẢ LỜI nếu đúng Chat ID admin đã cấu hình
                    if (incomingChatId.equals(chatId)) {
                        String cleanText = text.trim().toLowerCase();
                        if (cleanText.equals("doanh thu") || cleanText.equals("/doanhthu") || cleanText.equals("dt")) {
                            log.info("ℹ️ Nhận lệnh báo cáo tức thời từ Telegram...");
                            String report = buildDailyReportMessage();
                            String prefix = "📢 *BÁO CÁO TỨC THỜI* (Lúc " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ")\n\n";
                            sendTelegramMessage(prefix + report);
                        } else if (cleanText.equals("/start") || cleanText.equals("hi") || cleanText.equals("hello")) {
                            sendTelegramMessage("👋 *Chào bạn!*\n\nTôi là Bot quản lý Bida. Nhắn 'doanh thu' để xem tình hình quán ngay bây giờ nhé!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Chưa có tin nhắn mới hoặc lỗi polling: {}", e.getMessage());
        }
    }

    /**
     * Gửi thử một báo cáo ngay lập tức để kiểm tra kết nối.
     */
    public void sendTestReport() {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            throw new RuntimeException("Chưa cấu hình Telegram Bot Token hoặc Chat ID trong application.properties");
        }
        String message = "✅ *KẾT NỐI THÀNH CÔNG!*\n\nHệ thống quản lý Bida đã kết nối được với Telegram của bạn.\nBáo cáo doanh thu sẽ được gửi tự động vào **23:00** hàng đêm.";
        sendTelegramMessage(message);
    }

    /**
     * Format số tiền theo chuẩn Việt Nam: 1.500.000
     */
    private String formatCurrency(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(amount);
    }
}
