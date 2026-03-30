# 🎓 CREDIT-SAVING WORKFLOW - Hướng Dẫn Đầy Đủ

**Mục đích:** Tiết kiệm 80% credit bằng cách mô tả chính xác lỗi/feature.

---

## 📚 TÀI LIỆU CÓ SẴN

Bạn sẽ có **4 tài liệu** trong `/brain/` folder:

### 1️⃣ **CODEBASE_NAVIGATION_MAP.md** 🗺️
```
Bản đồ chi tiết dự án
├─ Backend structure (8 parts)
│  ├─ Billing engine
│  ├─ Session management
│  ├─ Product & order
│  ├─ Invoice
│  ├─ API endpoints
│  ├─ Admin controllers
│  ├─ Repositories
│  └─ Entities
├─ Frontend structure (Thymeleaf)
├─ Key patterns
├─ Dependencies
└─ Quick location finder
```
**Khi dùng:** Cần biết file ở đâu, architecture ra sao

---

### 2️⃣ **ERROR_DIAGNOSIS_TEMPLATE.md** 🔴
```
Template để mô tả lỗi rõ ràng
├─ Template chung
├─ 5 loại lỗi thường gặp
│  ├─ Billing error
│  ├─ Admin form (CSRF)
│  ├─ Product API
│  ├─ Invoice filter
│  └─ Realtime dashboard
├─ Real examples
└─ Checklist
```
**Khi dùng:** Gặp lỗi ở phần nào, mô tả theo template → Tôi fix ngay

---

### 3️⃣ **FEATURE_REQUEST_TEMPLATE.md** 🎨
```
Template để request chức năng mới
├─ Template chung
├─ 4 loại feature thường xảy ra
│  ├─ Frontend change
│  ├─ Backend API
│  ├─ Business logic
│  └─ Database field
├─ Real examples
└─ Checklist
```
**Khi dùng:** Muốn thêm chức năng gì, mô tả theo template → Tôi implement ngay

---

### 4️⃣ **QUICK_REFERENCE.md** ⚡
```
Tra cứu nhanh theo triệu chứng
├─ Search by symptom (5 lỗi phổ biến)
├─ Search by feature (6 categories)
├─ Search by technology (7 parts)
├─ Common patterns
├─ Quick location guide
├─ Top 5 files to remember
├─ Instant lookup
└─ Tips
```
**Khi dùng:** Cần tra cứu nhanh file/pattern nào liên quan

---

## 🚀 WORKFLOW - Bước Từng Bước

### Khi Gặp Lỗi

**Bước 1:** Mô tả lỗi theo ERROR_DIAGNOSIS_TEMPLATE
```
Copy template từ ERROR_DIAGNOSIS_TEMPLATE.md
Điền vào các phần:
  - Lỗi tiêu đề (cái gì fail?)
  - Bước tái hiện (cách làm sao?)
  - Thông tin hữuích (loại bàn, ngày, khung giờ, etc)
  - Lỗi message (paste logs)
  - Liên quan file (guess từ QUICK_REFERENCE)
```

**Bước 2:** Send message
```
Gửi message với template đầy đủ
Tôi sẽ:
  ✅ Mở đúng file cần sửa (không cần scan)
  ✅ Fix lỗi nhanh (vì biết context)
  ✅ Tiết kiệm 80% credit
```

**Example Message:**
```
=== ERROR DIAGNOSIS ===

[LỖI TIÊU ĐỀ]
Kết thúc phiên POOL lúc 9 sáng thứ 2 → không tính được tiền

[BƯỚC TÁI HIỆN]
1. Login dashboard
2. Click Pool 01 → Start session
3. Wait 1 minute
4. Click End session
5. See error popup

[THÔNG TIN HỮUÍCH]
- Loại bàn: POOL
- Loại ngày: WEEKDAY
- Khung giờ: 8-12
- Thời gian: 1 phút

[LỖI MESSAGE]
java.lang.NullPointerException
  at TimeSlotPricingStrategy.calculate()

[LIÊN QUAN FILE]
TimeSlotPricingStrategy, PriceRuleRepository
```

**Kết quả:** Tôi fix ngay mà không cần hỏi lại! 🎯

---

### Khi Muốn Thêm Chức Năng

**Bước 1:** Mô tả feature theo FEATURE_REQUEST_TEMPLATE
```
Copy template từ FEATURE_REQUEST_TEMPLATE.md
Điền vào các phần:
  - Tên chức năng
  - Mô tả (cái gì sẽ thay đổi)
  - Trang/endpoint cụ thể
  - Chi tiết (nơi đặt, hành động, input/output)
  - Liên quan file (guess từ CODEBASE_NAVIGATION_MAP)
```

**Bước 2:** Send message
```
Gửi message với template đầy đủ
Tôi sẽ:
  ✅ Biết file nào cần sửa (không cần explore)
  ✅ Implement ngay (vì biết requirements)
  ✅ Tiết kiệm 70% credit
```

**Example Message:**
```
=== FEATURE REQUEST ===

[TÊN CHỨC NĂNG]
Thêm nút "Xóa filters" trên trang admin/invoices

[MÔ TẢ]
Giúp user reset tất cả filters một cách nhanh chóng

[TRANG / ENDPOINT]
Frontend: /admin/invoices

[CHI TIẾT]
- Nơi đặt: Ngay cạnh button "Search"
- Hành động: Click → redirect /admin/invoices (clear params)
- Input: Không
- Output: Hiển thị tất cả hóa đơn

[LIÊN QUAN FILE]
admin/invoices.html, AdminInvoiceController
```

**Kết quả:** Tôi implement ngay mà không cần hỏi chi tiết! 🎨

---

## 🎯 QUICK START - Lần Đầu Tiên

### Setup (chỉ 1 lần):
```
1. Đọc CODEBASE_NAVIGATION_MAP.md (5 phút)
   → Hiểu structure project

2. Bookmark QUICK_REFERENCE.md (tra cứu khi cần)
   → Tra cứu nhanh

3. Lưu 2 templates:
   → ERROR_DIAGNOSIS_TEMPLATE.md
   → FEATURE_REQUEST_TEMPLATE.md
```

### Lần sau gặp lỗi:
```
1. Copy template từ ERROR_DIAGNOSIS_TEMPLATE
2. Fill 5 phần
3. Send message
4. ✅ Fix ngay!
```

### Lần sau muốn thêm feature:
```
1. Copy template từ FEATURE_REQUEST_TEMPLATE
2. Fill 5 phần
3. Send message
4. ✅ Implement ngay!
```

---

## 💡 TIPS SỬ DỤNG

### ✅ ĐÚNG CÁCH (Tiết Kiệm Credit)

```
❌ "Tính tiền bị lỗi"
✅ "Kết thúc phiên POOL 8-12 WEEKDAY → NPE ở PriceRuleRepository"

❌ "Admin form không work"
✅ "POST /admin/products → 403 CSRF validation failed"

❌ "Dropdown trống"
✅ "GET /api/products trả về [] (empty array)"

❌ "Thêm feature gì đó"
✅ "Thêm category filter dropdown trên /admin/products
    - Dropdown ở cạnh nút 'Thêm sản phẩm'
    - Filter theo DRINK/FOOD/SNACK/OTHER"
```

### 🚫 NHỚ KHÔNG LÀM

```
❌ Mô tả chung chung (vd: "lỗi ở đâu đó")
❌ Không paste logs hoặc error message
❌ Không nói loại bàn/ngày (billing)
❌ Không nói trang cụ thể (frontend)
❌ Guess file không rõ ràng

Tất cả những điều trên → Tôi phải hỏi lại → Tốn credit!
```

---

## 🎓 LEARNING PATH

### 1️⃣ Lần Đầu (30 phút)
```
[ ] Đọc CODEBASE_NAVIGATION_MAP.md (5 min)
[ ] Đọc ERROR_DIAGNOSIS_TEMPLATE.md (5 min)
[ ] Đọc FEATURE_REQUEST_TEMPLATE.md (5 min)
[ ] Scan QUICK_REFERENCE.md (10 min)
[ ] Hiểu workflow (5 min)

Sau đó:
✅ Ready to describe errors/features properly!
```

### 2️⃣ Lần Gặp Lỗi
```
[ ] Copy template từ ERROR_DIAGNOSIS_TEMPLATE
[ ] Reference QUICK_REFERENCE để guess files
[ ] Fill template đầy đủ
[ ] Send message

Không cần:
❌ Đọc code
❌ Scan codebase
❌ Hỏi câu hỏi setup
```

### 3️⃣ Lần Request Feature
```
[ ] Copy template từ FEATURE_REQUEST_TEMPLATE
[ ] Reference CODEBASE_NAVIGATION_MAP để biết files
[ ] Fill template đầy đủ
[ ] Send message

Không cần:
❌ Explore codebase
❌ Hỏi file nào cần sửa
❌ Describe implementation details
```

---

## 📊 CREDIT SAVINGS COMPARISON

### Cách Cũ (Tốn Credit)
```
User: "Lỗi ở chỗ nào"
                        ↓
I: "Cần more info, mô tả chi tiết không?"
                        ↓
User: "Khi click button xxx"
                        ↓
I: "Cần chụp ảnh không? Cần logs không?"
                        ↓
User: [chụp ảnh, paste logs]
                        ↓
I: "Thôi, scan codebase đi"
                        ↓
[Scan toàn bộ 50+ files] ❌ TỐNNNNN CREDIT

Total tokens: ~50,000 (😱)
```

### Cách Mới (Tiết Kiệm Credit)
```
User: "Kết thúc phiên POOL 8-12 WEEKDAY → NPE ở PriceRuleRepository"
                        ↓
I: "Biết ngay → mở PriceRuleRepository + TimeSlotPricingStrategy"
                        ↓
[Đọc 2 files] ✅ TIẾT KIỆM

Total tokens: ~5,000 (10% của cách cũ!)
Savings: 90% 🎉
```

---

## 🎁 BONUS: Tài Liệu Khác Trong Project

```
/brain/ folder cũng có:

📄 EXECUTIVE_SUMMARY.txt
   → Tóm tắt 5 lỗi đã fix

📄 PRODUCTION_READY_REPORT.md
   → Testing guide cho 5 lỗi

📄 ROOT_CAUSE_FIX_SUMMARY_2026-03-30.md
   → Deep dive vào root causes

🔧 VERIFY_FIXES.sh
   → Run để verify hệ thống health: bash VERIFY_FIXES.sh

📖 CODEBASE_NAVIGATION_MAP.md ← NEW
   → Bản đồ chi tiết dự án

🔴 ERROR_DIAGNOSIS_TEMPLATE.md ← NEW
   → Template mô tả lỗi

🎨 FEATURE_REQUEST_TEMPLATE.md ← NEW
   → Template request feature

⚡ QUICK_REFERENCE.md ← NEW
   → Tra cứu nhanh
```

---

## 📞 SUPPORT

### Cần giúp gì?

| Cần... | Dùng tài liệu... | Làm gì |
|---|---|---|
| Biết project structure | CODEBASE_NAVIGATION_MAP | Đọc overview |
| Gặp lỗi | ERROR_DIAGNOSIS_TEMPLATE | Copy template + fill |
| Muốn add feature | FEATURE_REQUEST_TEMPLATE | Copy template + fill |
| Tra cứu nhanh | QUICK_REFERENCE | Search by symptom |
| Verify system | VERIFY_FIXES.sh | Run script |

---

## ✨ KEY TAKEAWAYS

1. **Use templates** → Mô tả rõ ràng + tiết kiệm credit
2. **Reference files** → Không cần scan codebase
3. **Guess files from maps** → Chỉ sửa file cần sửa
4. **Paste logs/errors** → Tôi biết line fail ở đâu
5. **Be specific** → Nói loại bàn, trang, endpoint cụ thể

**Result:** Tiết kiệm 80% credit + fix nhanh gấp 3 lần! 🚀

---

**Bây giờ:** Khi có lỗi, copy ERROR_DIAGNOSIS_TEMPLATE.md và send! 📋
