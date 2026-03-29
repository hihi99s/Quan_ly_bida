# Phase 2: Feature Expansion Plan - Billiard Management System

> **Brain Agent** | Dựa trên backlog PRD Phase 1 + ý tưởng mở rộng.
> **Prerequisite:** Phase 1 hoàn thành (core billing, dashboard realtime, auth 2 role).

**Goal:** Nâng cấp hệ thống từ "quản lý cơ bản" thành "quản lý chuyên nghiệp" - thêm dịch vụ kèm theo, hóa đơn chi tiết, đặt bàn, báo cáo doanh thu, và trải nghiệm UX tốt hơn.

---

## Feature 1: Quản lý Dịch vụ kèm theo (F&B - Food & Beverage)

### Mô tả
Khách chơi bida thường gọi thêm nước, bia, snack. Cần hệ thống order dịch vụ gắn vào phiên chơi, tính chung vào hóa đơn cuối.

### Chi tiết
- **Entity mới:**
  - `Product` (id, name, category, price, stockQuantity, imageUrl, active)
  - `ProductCategory` enum: DRINK, FOOD, SNACK, OTHER
  - `OrderItem` (id, session FK, product FK, quantity, unitPrice, amount, orderedAt, staff FK)

- **Tính năng:**
  - ADMIN: CRUD sản phẩm (tên, giá, danh mục, tồn kho, ảnh)
  - STAFF: Gọi món cho bàn đang chơi → chọn sản phẩm + số lượng
  - Tự động trừ kho khi order, cảnh báo khi tồn kho thấp (< 5)
  - Hiển thị danh sách order trên card bàn (dashboard)
  - Tổng hóa đơn = tiền bàn + tiền dịch vụ

- **UI:**
  - Dashboard: click bàn đang chơi → tab "Gọi món" với grid sản phẩm
  - Admin: trang `/admin/products` quản lý sản phẩm + tồn kho
  - Badge số lượng order trên card bàn

### Ước lượng: ~4 giờ

---

## Feature 2: Hóa đơn chi tiết (Invoice Breakdown)

### Mô tả
Phase 1 chỉ hiện tổng tiền. Phase 2 cần hóa đơn chi tiết: breakdown từng segment giờ chơi + từng món đã order.

### Chi tiết
- **Entity mới:**
  - `Invoice` (id, session FK, invoiceNumber, tableCharge, serviceCharge, totalAmount, createdAt, staff FK)

- **Tính năng:**
  - Khi kết thúc phiên → tạo Invoice tự động
  - Hiển thị popup/modal hóa đơn chi tiết:
    ```
    ╔══════════════════════════════════════╗
    ║     HOA DON - BAN 03 (CAROM)        ║
    ║  So: INV-20260329-001               ║
    ╠══════════════════════════════════════╣
    ║ THOI GIAN CHOI:                     ║
    ║  17:00 - 19:30 (150 phut)           ║
    ║  ├ 17:00-17:00: 0ph × 70k/h = 0    ║
    ║  ├ 17:00-19:30: 150ph × 80k/h      ║
    ║  Tien ban:            200,000 VND   ║
    ╠══════════════════════════════════════╣
    ║ DICH VU:                            ║
    ║  2x Bia Tiger         60,000 VND    ║
    ║  1x Nuoc suoi          8,000 VND    ║
    ║  Tien dich vu:        68,000 VND    ║
    ╠══════════════════════════════════════╣
    ║ TONG CONG:           268,000 VND    ║
    ╚══════════════════════════════════════╝
    ```
  - Lịch sử hóa đơn: trang `/admin/invoices` xem lại tất cả
  - Tìm kiếm theo ngày, bàn, nhân viên
  - In hóa đơn (print CSS / PDF export)

### Ước lượng: ~3 giờ

---

## Feature 3: Trạng thái bàn mở rộng (Pause + Reserved)

### Mô tả
Thêm 2 trạng thái: Tạm dừng (khách đi WC/nghỉ) và Đã đặt (giữ bàn cho khách).

### Chi tiết
- **Enum mở rộng:**
  - `TableStatus`: AVAILABLE, PLAYING, **PAUSED**, **RESERVED**, **MAINTENANCE**

- **PAUSED (Tạm dừng):**
  - Staff click "Tạm dừng" → billing DỪNG tính tiền
  - Card bàn: màu VÀNG, hiện "Tạm dừng - XX phút"
  - Click "Tiếp tục" → billing chạy lại
  - Entity: thêm `pauseStart`, `totalPausedMinutes` vào Session
  - Billing engine: trừ thời gian pause khỏi tổng

- **RESERVED (Đã đặt):**
  - Đặt bàn qua điện thoại (staff nhập thủ công Phase 2, online Phase 3)
  - Entity mới: `Reservation` (id, table FK, customerName, customerPhone, reservedTime, duration, note, status: PENDING/CONFIRMED/CANCELLED/COMPLETED)
  - Card bàn: màu CAM, hiện "Đã đặt - Nguyễn Văn A - 19:00"
  - Auto-cancel nếu khách không đến sau 15 phút (configurable)

- **MAINTENANCE (Bảo trì):**
  - Admin đánh dấu bàn hỏng / đang sửa
  - Card: màu XÁM, không cho phép thao tác

### Ước lượng: ~4 giờ

---

## Feature 4: Báo cáo & Thống kê Doanh thu

### Mô tả
Admin cần báo cáo để ra quyết định kinh doanh: doanh thu theo ngày/tuần/tháng, bàn nào hot nhất, giờ cao điểm.

### Chi tiết
- **Trang `/admin/reports`** với các tab:

- **4.1 Doanh thu tổng quan:**
  - Biểu đồ doanh thu theo ngày (7 ngày gần nhất) - bar chart
  - Biểu đồ doanh thu theo tháng (12 tháng) - line chart
  - So sánh: tiền bàn vs tiền dịch vụ (stacked bar)
  - KPI cards: Doanh thu hôm nay, tuần này, tháng này

- **4.2 Phân tích bàn:**
  - Top bàn được chơi nhiều nhất (theo số phiên + tổng giờ)
  - Tỷ lệ sử dụng bàn (% thời gian PLAYING / tổng thời gian mở cửa)
  - Doanh thu theo loại bàn (Pool vs Carom vs VIP)

- **4.3 Phân tích giờ cao điểm:**
  - Heatmap: trục X = giờ (8h-23h), trục Y = thứ (T2-CN)
  - Ô sáng = nhiều khách, ô tối = ít khách
  - Gợi ý: tăng giá giờ cao điểm, khuyến mãi giờ thấp điểm

- **4.4 Báo cáo nhân viên:**
  - Số phiên mỗi nhân viên handle
  - Doanh thu theo nhân viên
  - Thống kê ca làm việc

- **Tech:** Chart.js hoặc ApexCharts cho biểu đồ. API trả JSON, render client-side.

### Ước lượng: ~5 giờ

---

## Feature 5: Quản lý Nhân viên & Ca làm việc

### Mô tả
Quản lý nhân viên chi tiết hơn: thông tin cá nhân, ca làm việc, check-in/check-out.

### Chi tiết
- **Entity mở rộng User:**
  - Thêm: phone, email, avatar, hireDate, salary

- **Entity mới:**
  - `Shift` (id, name: "Ca sáng"/"Ca chiều"/"Ca tối", startTime, endTime)
  - `StaffSchedule` (id, user FK, shift FK, date, status: SCHEDULED/CHECKED_IN/CHECKED_OUT/ABSENT)

- **Tính năng:**
  - ADMIN: Xếp lịch làm việc cho nhân viên (drag-drop calendar)
  - STAFF: Check-in khi bắt đầu ca, check-out khi hết ca
  - Trang `/admin/staff`: danh sách NV + lịch làm tuần
  - Cảnh báo: ca chưa có người, nhân viên chưa check-in

### Ước lượng: ~4 giờ

---

## Feature 6: Bảng Holiday tự nhận biết

### Mô tả
Phase 1 admin bật Holiday thủ công. Phase 2 hệ thống tự nhận biết ngày lễ Việt Nam.

### Chi tiết
- **Entity mới:**
  - `HolidayCalendar` (id, name, date, recurring: boolean)
  - VD: Tết Dương lịch (1/1, recurring), Giỗ Tổ Hùng Vương (10/3 AL, manual), 30/4, 1/5, 2/9...

- **Tính năng:**
  - ADMIN: Quản lý danh sách ngày lễ (CRUD)
  - Seed sẵn các ngày lễ cố định Việt Nam
  - BillingCalculator tự check: ngày hiện tại có trong HolidayCalendar? → dùng giá Holiday
  - Vẫn giữ nút toggle thủ công (override) cho trường hợp đặc biệt
  - Thông báo trên dashboard khi đang áp dụng giá Holiday

### Ước lượng: ~2 giờ

---

## Feature 7: Giao diện nâng cao (UX Upgrade)

### Mô tả
Nâng cấp UI/UX cho dashboard và admin pages.

### Chi tiết
- **7.1 Dashboard cải tiến:**
  - Filter bàn: Tất cả / Trống / Đang chơi / VIP / Pool / Carom
  - Sort: theo tên, thời gian chơi, tiền tạm tính
  - Compact view vs Detailed view toggle
  - Dark mode cho ca đêm
  - Âm thanh thông báo khi có sự kiện mới

- **7.2 Layout bàn kéo thả:**
  - Admin tùy chỉnh vị trí bàn trên dashboard (drag-drop)
  - Lưu layout vào DB, load khi mở dashboard
  - Phản ánh mặt bằng thực tế của quán

- **7.3 Responsive mobile:**
  - Dashboard hoạt động tốt trên tablet (staff cầm tablet đi quanh quán)
  - Swipe actions trên mobile

- **7.4 Notification system:**
  - Toast notification khi: bàn bắt đầu/kết thúc, đặt bàn mới, tồn kho thấp
  - Browser notification (với permission)

### Ước lượng: ~5 giờ

---

## Feature 8: Khách hàng & Membership (Bonus)

### Mô tả
Quản lý khách hàng thường xuyên, tích điểm, giảm giá member.

### Chi tiết
- **Entity mới:**
  - `Customer` (id, name, phone, email, membershipTier, totalSpent, points, createdAt)
  - `MembershipTier` enum: BRONZE (0-500k), SILVER (500k-2M), GOLD (2M-5M), DIAMOND (5M+)

- **Tính năng:**
  - Gắn khách hàng vào Session (optional, cho walk-in không cần)
  - Tích điểm: 1,000 VND = 1 point
  - Giảm giá theo tier: Bronze 0%, Silver 5%, Gold 10%, Diamond 15%
  - Lịch sử chơi của khách hàng
  - Top khách hàng VIP

### Ước lượng: ~4 giờ

---

## Tóm tắt Phase 2

| # | Feature | Ưu tiên | Ước lượng |
|---|---------|---------|-----------|
| F1 | Dịch vụ kèm (F&B) + trừ kho | **CAO** | 4h |
| F2 | Hóa đơn chi tiết + in | **CAO** | 3h |
| F3 | Trạng thái Pause/Reserved/Maintenance | **CAO** | 4h |
| F4 | Báo cáo & Thống kê doanh thu | **CAO** | 5h |
| F5 | Quản lý NV & Ca làm việc | TRUNG BÌNH | 4h |
| F6 | Holiday tự nhận biết | TRUNG BÌNH | 2h |
| F7 | UX Upgrade (filter, drag-drop, dark mode) | TRUNG BÌNH | 5h |
| F8 | Khách hàng & Membership | THẤP | 4h |
| **TỔNG** | | | **~31 giờ** |

---

## Thứ tự triển khai đề xuất

```
Sprint 1 (Ưu tiên cao - Core Business):
  F1 → F2 → F3
  Lý do: F&B là nguồn thu lớn, hóa đơn chi tiết cần cho kế toán,
         Pause/Reserve là nhu cầu hàng ngày.

Sprint 2 (Analytics & Operations):
  F4 → F6 → F5
  Lý do: Báo cáo giúp ra quyết định, Holiday tự động giảm thao tác,
         Quản lý NV cho vận hành chuyên nghiệp.

Sprint 3 (UX & Growth):
  F7 → F8
  Lý do: UX nâng cao trải nghiệm, Membership giữ chân khách.
```

---

## Lưu ý kỹ thuật Phase 2
- Giữ nguyên kiến trúc monolith Spring Boot (chưa cần tách microservice)
- Thêm Flyway/Liquibase cho database migration (thay vì ddl-auto=update)
- Cân nhắc thêm Redis cache cho báo cáo nặng
- API versioning: `/api/v2/` cho endpoints mới
- Test coverage target: >70% cho billing + F&B logic
