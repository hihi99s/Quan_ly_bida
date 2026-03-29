# PHASE 2: Feature Expansion

## 6. Mục tiêu Phase 2
- Quản lý dịch vụ kèm theo (F&B: đồ uống, đồ ăn) gắn vào phiên chơi, trừ kho tự động.
- Hóa đơn chi tiết (breakdown segments giờ chơi + dịch vụ), in / xuất PDF.
- Trạng thái bàn mở rộng: Tạm dừng (PAUSED), Đã đặt (RESERVED), Bảo trì (MAINTENANCE).
- Báo cáo & thống kê doanh thu (theo ngày/tuần/tháng, top bàn, giờ cao điểm).
- Quản lý nhân viên & ca làm việc (check-in/check-out ca).
- Holiday tự nhận biết ngày lễ Việt Nam.
- Nâng cấp UX: filter/sort bàn, dark mode, thông báo, responsive tablet.
- Hệ thống khách hàng & Membership (tích điểm, giảm giá theo hạng).

## 7. Chi tiết quyết định Phase 2

### 7.1. Dịch vụ kèm theo (F&B)
- **Product**: Mỗi sản phẩm có danh mục (DRINK / FOOD / SNACK / OTHER), giá, tồn kho, ảnh.
- **Order flow**: Staff chọn bàn đang chơi → chọn sản phẩm + số lượng → tự động trừ kho.
- **Cảnh báo tồn kho thấp** khi quantity < 5 (hiện badge đỏ trên trang quản lý).
- **Tổng hóa đơn** = tiền bàn (billing engine) + tiền dịch vụ (sum OrderItems).
- **Dashboard**: Card bàn hiện badge số món đã order. Click vào → tab "Gọi món".
- ADMIN: CRUD sản phẩm tại `/admin/products`.
- STAFF: Chỉ được gọi món, không sửa/xóa sản phẩm.

### 7.2. Hóa đơn chi tiết (Invoice)
- Khi kết thúc phiên → tự động tạo Invoice với số hóa đơn (VD: `INV-20260329-001`).
- **Breakdown gồm 2 phần:**
  - Phần 1 - Thời gian chơi: liệt kê từng SessionSegment (khung giờ, phút, đơn giá, thành tiền).
  - Phần 2 - Dịch vụ: liệt kê từng OrderItem (tên, SL, đơn giá, thành tiền).
- Hiển thị popup/modal ngay khi kết thúc phiên.
- Lịch sử hóa đơn tại `/admin/invoices` (tìm theo ngày, bàn, nhân viên).
- **In hóa đơn**: Print CSS cho máy in nhiệt (80mm) + xuất PDF (A5).

### 7.3. Trạng thái bàn mở rộng
- **PAUSED (Tạm dừng):**
  - Staff click "Tạm dừng" → billing DỪNG tính tiền.
  - Card bàn: màu VÀNG, hiện "Tạm dừng - XX phút".
  - Click "Tiếp tục" → billing chạy lại.
  - Session lưu thêm: `pauseStart (LocalDateTime)`, `totalPausedMinutes (Integer)`.
  - Billing engine: tổng phút tính tiền = tổng phút chơi − totalPausedMinutes.

- **RESERVED (Đã đặt):**
  - Phase 2: Staff nhập thủ công (khách gọi điện đặt bàn).
  - Card bàn: màu CAM, hiện "Đã đặt - Tên khách - 19:00".
  - Auto-cancel nếu khách không đến sau 15 phút (configurable trong AppSettings).
  - Khi khách đến → Staff click "Bắt đầu" → chuyển RESERVED → PLAYING.

- **MAINTENANCE (Bảo trì):**
  - ADMIN đánh dấu bàn hỏng / đang sửa chữa.
  - Card bàn: màu XÁM, chặn mọi thao tác.

### 7.4. Báo cáo & Thống kê
- **Doanh thu tổng quan:**
  - KPI cards: doanh thu hôm nay, tuần này, tháng này (so sánh vs kỳ trước %).
  - Biểu đồ cột: doanh thu 7 ngày gần nhất.
  - Biểu đồ line: doanh thu 12 tháng.
  - Stacked bar: tiền bàn vs tiền dịch vụ.

- **Phân tích bàn:**
  - Top bàn được chơi nhiều nhất (theo số phiên + tổng giờ).
  - Tỷ lệ sử dụng bàn = % thời gian PLAYING / tổng thời gian mở cửa (8h-23h).
  - Doanh thu theo loại bàn (Pool vs Carom vs VIP) - pie chart.

- **Heatmap giờ cao điểm:**
  - Trục X = giờ (8h-23h), trục Y = thứ (T2-CN).
  - Ô sáng = nhiều khách, ô tối = ít khách.

- **Báo cáo nhân viên:**
  - Số phiên mỗi staff handle, doanh thu theo staff.

- **Tech**: Chart.js cho biểu đồ, API trả JSON, render client-side.

### 7.5. Quản lý nhân viên & Ca làm việc
- **Mở rộng User**: thêm phone, email, hireDate.
- **Shift (Ca)**: Tên ca ("Ca sáng", "Ca chiều", "Ca tối"), startTime, endTime.
- **StaffSchedule**: Xếp lịch nhân viên theo ngày + ca.
- ADMIN: Xếp lịch tại `/admin/staff` (calendar view theo tuần).
- STAFF: Check-in khi bắt đầu ca, check-out khi hết ca.
- Cảnh báo: ca chưa có ai, nhân viên chưa check-in quá 15 phút.

### 7.6. Holiday tự nhận biết
- **HolidayCalendar**: lưu danh sách ngày lễ (name, date, recurring).
- Seed sẵn ngày lễ cố định VN: 1/1, 30/4, 1/5, 2/9, Giỗ Tổ Hùng Vương.
- BillingCalculator tự check: hôm nay có trong HolidayCalendar? → dùng giá HOLIDAY.
- Giữ nút toggle thủ công (override) từ Phase 1.
- Dashboard hiện banner "Đang áp dụng giá ngày lễ" khi Holiday active.

### 7.7. Nâng cấp UX
- **Dashboard**: Filter (Tất cả / Trống / Đang chơi / Loại bàn), Sort (tên, thời gian, tiền).
- **Dark mode**: Toggle cho ca đêm, lưu preference user.
- **Notification**: Toast khi bàn start/end, đặt bàn mới, tồn kho thấp. Browser notification.
- **Responsive tablet**: Staff cầm tablet đi quanh quán thao tác.
- **Âm thanh**: Notification sound khi có sự kiện mới (configurable ON/OFF).

### 7.8. Khách hàng & Membership
- **Customer**: name, phone, totalSpent, points, membershipTier.
- **MembershipTier**: BRONZE (0-500k), SILVER (500k-2M), GOLD (2M-5M), DIAMOND (>5M).
- Gắn Customer vào Session (optional, walk-in không bắt buộc).
- **Tích điểm**: 1,000 VND = 1 point.
- **Giảm giá theo hạng**: Bronze 0%, Silver 5%, Gold 10%, Diamond 15%.
- Lịch sử chơi + chi tiêu của từng khách.
- Trang `/admin/customers`: danh sách, tìm kiếm, top VIP.

## 8. Thực thể dữ liệu Phase 2 (Entities mới)

### Product
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| name | String | Tên sản phẩm (VD: "Bia Tiger") |
| category | Enum | DRINK / FOOD / SNACK / OTHER |
| price | BigDecimal | Giá bán (VND) |
| stockQuantity | Integer | Tồn kho hiện tại |
| imageUrl | String | URL ảnh sản phẩm (nullable) |
| active | Boolean | Còn bán hay ngừng |
| createdAt | LocalDateTime | Ngày tạo |

### OrderItem
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| session | Session | FK → phiên chơi |
| product | Product | FK → sản phẩm |
| quantity | Integer | Số lượng |
| unitPrice | BigDecimal | Giá tại thời điểm order |
| amount | BigDecimal | = quantity × unitPrice |
| orderedAt | LocalDateTime | Thời điểm gọi |
| staff | User | FK → nhân viên gọi món |

### Invoice
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| session | Session | FK → phiên chơi |
| invoiceNumber | String | Mã hóa đơn unique (INV-yyyyMMdd-xxx) |
| tableCharge | BigDecimal | Tổng tiền bàn |
| serviceCharge | BigDecimal | Tổng tiền dịch vụ |
| discount | BigDecimal | Giảm giá (membership) |
| totalAmount | BigDecimal | = tableCharge + serviceCharge − discount |
| createdAt | LocalDateTime | Ngày tạo |
| staff | User | FK → nhân viên tạo |

### Reservation
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| table | BilliardTable | FK → bàn đặt |
| customerName | String | Tên khách đặt |
| customerPhone | String | SĐT khách |
| reservedTime | LocalDateTime | Thời gian đặt đến |
| durationMinutes | Integer | Dự kiến chơi bao lâu |
| note | String | Ghi chú (nullable) |
| status | Enum | PENDING / CONFIRMED / CANCELLED / COMPLETED |
| createdAt | LocalDateTime | Ngày tạo |
| staff | User | FK → nhân viên nhận đặt |

### HolidayCalendar
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| name | String | Tên ngày lễ (VD: "Quốc khánh") |
| date | LocalDate | Ngày lễ |
| recurring | Boolean | true = lặp hàng năm (VD: 1/1) |

### Customer
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| name | String | Tên khách hàng |
| phone | String | SĐT (unique) |
| email | String | Email (nullable) |
| membershipTier | Enum | BRONZE / SILVER / GOLD / DIAMOND |
| totalSpent | BigDecimal | Tổng chi tiêu tích lũy |
| points | Integer | Điểm tích lũy |
| createdAt | LocalDateTime | Ngày tạo |

### Shift
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| name | String | Tên ca (VD: "Ca sáng") |
| startTime | LocalTime | Giờ bắt đầu |
| endTime | LocalTime | Giờ kết thúc |

### StaffSchedule
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| user | User | FK → nhân viên |
| shift | Shift | FK → ca làm |
| date | LocalDate | Ngày làm |
| status | Enum | SCHEDULED / CHECKED_IN / CHECKED_OUT / ABSENT |
| checkInTime | LocalDateTime | Giờ check-in thực tế |
| checkOutTime | LocalDateTime | Giờ check-out thực tế |

## 9. Thay đổi Entity cũ (Phase 1 → Phase 2)

### BilliardTable (mở rộng)
| Field thêm | Type | Description |
|------------|------|-------------|
| status | Enum | Thêm: PAUSED, RESERVED, MAINTENANCE |

### Session (mở rộng)
| Field thêm | Type | Description |
|------------|------|-------------|
| pauseStart | LocalDateTime | Thời điểm tạm dừng hiện tại (null nếu không pause) |
| totalPausedMinutes | Integer | Tổng phút đã tạm dừng (default 0) |
| customer | Customer | FK → khách hàng (nullable, walk-in) |

### User (mở rộng)
| Field thêm | Type | Description |
|------------|------|-------------|
| phone | String | SĐT |
| email | String | Email |
| hireDate | LocalDate | Ngày vào làm |

## 10. Backlog (Phase 3+)
- [ ] Đặt bàn online (web public) + cọc online
- [ ] Thanh toán QR (Momo / VNPAY / ZaloPay)
- [ ] Layout kéo thả tùy chỉnh sơ đồ bàn (drag-drop canvas)
- [ ] Mobile app (React Native / Flutter)
- [ ] Nhiều chi nhánh (multi-branch)
- [ ] Chương trình khuyến mãi (voucher, combo giờ chơi + nước)
- [ ] Tích hợp máy POS / máy in nhiệt
- [ ] API public cho đối tác (booking platform)
