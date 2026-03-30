# 🔴 ERROR DIAGNOSIS TEMPLATE - Mô tả Lỗi để Tôi Fix Nhanh

**Mục đích:** Mô tả lỗi đầy đủ để tôi không cần scan codebase.

---

## 📋 TEMPLATE - Copy & Fill

```
=== LỖI DIAGNOSIS ===

[LỖI TIÊU ĐỀ]
Ví dụ: "Kết thúc phiên Pool lúc 9 sáng thứ 2 → lỗi tính tiền"

[BƯỚC TÁI HIỆN]
1. Bước 1: Cách đạo từ giao diện
2. Bước 2: Cách làm để lỗi xảy ra
3. Bước 3: Kết quả mong đợi vs thực tế

[THÔNG TIN HỮU DỤN]
- Loại bàn (POOL/CAROM/VIP):
- Loại ngày (WEEKDAY/WEEKEND/HOLIDAY):
- Khung giờ (8-12 / 12-17 / 17-23):
- Thời gian chơi (bao lâu):

[LỖI MESSAGE / LOGS]
Paste error message từ console hoặc logs
(Nếu có: stack trace, line number)

[LIÊN QUAN FILE]
Guess file nào có vấn đề (từ CODEBASE_NAVIGATION_MAP):
Ví dụ: TimeSlotPricingStrategy, PriceRuleRepository, ...
```

---

## 💡 EXAMPLES - Cách Mô Tả Hiệu Quả

### ❌ LỖI KHÔNG RÕ (Tốn Credit)
```
"Lỗi khi tính tiền"
- Không biết loại bàn nào
- Không biết loại ngày nào
- Không biết khung giờ nào
- Tôi phải scan toàn bộ codebase
```

### ✅ LỖI RÕ RÀNG (Tiết Kiệm Credit)
```
=== LỖI DIAGNOSIS ===

[LỖI TIÊU ĐỀ]
Kết thúc phiên POOL lúc 9-10 sáng thứ 2 → NullPointerException

[BƯỚC TÁI HIỆN]
1. Login admin
2. Go to dashboard → click "Pool 01" → Start session
3. Wait 1 minute → click "End session"
4. See error popup

[THÔNG TIN HỮU DỤC]
- Loại bàn: POOL
- Loại ngày: WEEKDAY (thứ 2)
- Khung giờ: 8-12
- Thời gian chơi: 1 phút

[LỖI MESSAGE]
```
java.lang.NullPointerException: null
    at com.bida.billing.strategy.TimeSlotPricingStrategy.calculate(...)
    at com.bida.billing.service.BillingCalculator.calculate(...)
```

[LIÊN QUAN FILE]
TimeSlotPricingStrategy, PriceRuleRepository
```

→ **Tôi biết ngay:** Lỗi ở line nào trong TimeSlotPricingStrategy, không cần scan!

---

## 🎯 LOẠI LỖI THƯỜNG GẶP

### 1️⃣ Lỗi Tính Tiền (Billing)

**Template chi tiết:**
```
=== BILLING ERROR ===

[Loại bàn]: POOL / CAROM / VIP?
[Loại ngày]: WEEKDAY / WEEKEND / HOLIDAY?
[Khung giờ]: 8-12 / 12-17 / 17-23?
[Thời gian chơi]: X phút
[Lỗi]: NullPointerException / IllegalStateException / etc?
[Logs]: Paste error message

→ Tôi mở: BillingCalculator + TimeSlotPricingStrategy + PriceRuleRepository
```

---

### 2️⃣ Lỗi Admin Form (Security / CSRF)

**Template chi tiết:**
```
=== ADMIN FORM ERROR ===

[Hành động]: Tạo / Sửa / Xóa sản phẩm?
[Trang]: /admin/products / /admin/prices / ...?
[HTTP Status]: 403 / 400 / 500?
[Lỗi]: CSRF token / Database error / etc?

→ Tôi mở: AdminProductController + admin/products.html + SecurityConfig
```

---

### 3️⃣ Lỗi API (Product Dropdown)

**Template chi tiết:**
```
=== API ERROR ===

[Endpoint]: GET /api/products?
[HTTP Status]: 200 nhưng trả về gì? / 500?
[Response]: Empty array / Error message?
[Browser Console Error]: Paste error

→ Tôi mở: ProductApiController + ProductService
```

---

### 4️⃣ Lỗi Dashboard (Realtime)

**Template chi tiết:**
```
=== REALTIME ERROR ===

[Vấn đề]: Timer không update / currentAmount không update?
[Tần suất]: Một lần / Liên tục?
[Browser Console]: Paste WebSocket error (nếu có)
[Server Logs]: "Broadcast periodic" có xuất hiện không?

→ Tôi mở: TableStatusBroadcaster + BidaApplication (@EnableScheduling)
```

---

### 5️⃣ Lỗi Invoice Filter

**Template chi tiết:**
```
=== INVOICE FILTER ERROR ===

[Filter]: Ngày từ / đến?
[Input Format]: YYYY-MM-DD? Invalid format?
[Lỗi]: DateTimeParseException / Null result?
[URL]: /admin/invoices?from=2026-03-30?

→ Tôi mở: AdminInvoiceController
```

---

## 📝 REAL EXAMPLE - Copy Paste Format

```
=== ERROR DIAGNOSIS ===

[LỖI TIÊU ĐỀ]
Không thể tạo sản phẩm mới ở /admin/products

[BƯỚC TÁI HIỆN]
1. Login as admin
2. Go to /admin/products
3. Click "Thêm sản phẩm mới"
4. Fill: name="Bia Tiger", price="25000", category="DRINK"
5. Click "Thêm sản phẩm" button
6. See error: 403 Forbidden

[THÔNG TIN HỮUÍCH]
- Loại: DRINK
- Giá: 25000
- Hành động: POST /admin/products

[LỖI MESSAGE]
HTTP 403 Forbidden
CSRF token validation failed

[LIÊN QUAN FILE]
SecurityConfig, admin/products.html, AdminProductController
```

→ **Ngay lập tức tôi biết:** CSRF token chưa được add vào form!

---

## ✅ CHECKLIST - Trước Khi Send Message

- [ ] Mô tả rõ lỗi tiêu đề
- [ ] Nêu bước tái hiện cụ thể (1, 2, 3)
- [ ] Đưa thông tin hữu ích (loại bàn, khung giờ, etc)
- [ ] Paste lỗi message hoặc logs (nếu có)
- [ ] Guess file liên quan (từ CODEBASE_NAVIGATION_MAP)
- [ ] **KHÔNG**: "Sửa giúp" không rõ

---

## 🚀 TÓTAT

Khi mô tả lỗi theo format này:
- ✅ Tôi mở **đúng file** cần sửa
- ✅ Tôi **không cần** scan toàn bộ codebase
- ✅ **Tiết kiệm 80% credit**
- ✅ Fix nhanh gấp 3 lần

**Next:** Khi muốn thêm feature → dùng FEATURE_REQUEST_TEMPLATE.md
