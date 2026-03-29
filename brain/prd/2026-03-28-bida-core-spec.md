# PRD: Billiard Management System (Phase 1 & 2)

## 1. Overview
Hệ thống quản lý quán bida tập trung vào việc tối ưu hóa quy trình vận hành, tính tiền tự động và quản lý trạng thái bàn realtime.

## 2. Mục tiêu Phase 1
- Quản lý danh mục bàn (Billiard Table) với phân loại: Pool / Carom / VIP.
- Hệ thống tính tiền tự động (Core Billing Engine) theo phút thực tế.
- Dynamic Pricing: khung giờ + ngày thường / cuối tuần / lễ (admin bật thủ công).
- Realtime Table Status Dashboard (WebSocket STOMP) dạng grid card.
- Phân quyền 2 role: ADMIN và STAFF (Spring Security).
- Seed data mẫu + CRUD quản lý bàn & giá.

## 3. Kiến trúc hệ thống
- **Backend**: Spring Boot 3.x, Spring Data JPA, Spring Security, MySQL.
- **Realtime**: Spring WebSocket (STOMP) + SockJS.
- **Billing**: Strategy Pattern for Pricing.
- **Frontend**: Thymeleaf + AlpineJS.
- **Build**: Maven.

## 4. Chi tiết quyết định (từ Brainstorming)

### 4.1. Trạng thái bàn
- **Trống** (AVAILABLE): Sẵn sàng cho khách.
- **Đang chơi** (PLAYING): Khách đang chơi, billing đang chạy.
- **KHÔNG có trạng thái Tạm dừng** trong Phase 1.
- **KHÔNG có trạng thái Đã đặt** trong Phase 1 (đặt bàn online → Phase 2).

### 4.2. Loại bàn & Giá
- **Nhiều loại bàn, mỗi loại giá KHÁC nhau.**
  - VD: Pool: 50k/h, Carom: 70k/h, VIP: 100k/h.
- Mỗi loại bàn (TableType) có bộ PriceRule riêng.

### 4.3. Dynamic Pricing
- **3 bộ PriceRule** cho mỗi loại bàn:
  - `WEEKDAY`: Giá ngày thường (Thứ 2 – Thứ 6).
  - `WEEKEND`: Giá cuối tuần (Thứ 7 – Chủ nhật).
  - `HOLIDAY`: Giá ngày lễ.
- **Admin bật thủ công** chế độ Holiday (không tự nhận biết).
- Mỗi bộ PriceRule gồm nhiều TimeSlot:
  - VD Weekday Pool: 8h-12h: 40k, 12h-17h: 50k, 17h-23h: 70k.

### 4.4. Logic tính tiền (Billing Engine)
- **Input**: Start Time, End Time, TableType, DayType (weekday/weekend/holiday).
- **Step 1**: Lấy bộ PriceRule phù hợp (theo TableType + DayType).
- **Step 2**: Tách Session thành Segments dựa trên TimeSlot boundaries.
- **Step 3**: Mỗi Segment tính duration = phút thực tế (KHÔNG làm tròn).
- **Step 4**: Thành tiền segment = `(actualMinutes / 60) * pricePerHour`.
- **Step 5**: Tổng tiền = Sum of all Segments.
- **Design**: Strategy Pattern cho phép mở rộng rule sau này.

### 4.5. Dashboard Realtime
- **Grid card đơn giản**: mỗi bàn 1 card.
- **Màu sắc**: Xanh = Trống, Đỏ = Đang chơi.
- **Thông tin card**: Tên bàn, loại, trạng thái, thời gian chơi, tiền tạm tính.
- **Click card** → thao tác: Bắt đầu / Kết thúc phiên.
- **WebSocket STOMP**: auto-refresh khi có thay đổi.

### 4.6. Hóa đơn
- Phase 1: **Chỉ hiển thị tổng tiền** khi kết thúc phiên.
- Chi tiết hóa đơn (breakdown segments) → Phase 2.

### 4.7. Phân quyền
- **ADMIN**: CRUD bàn, cấu hình giá, bật/tắt Holiday, xem dashboard.
- **STAFF**: Thao tác bàn (bắt đầu/kết thúc phiên), xem dashboard.
- Sử dụng Spring Security + form login.

### 4.8. Data
- **Seed data mẫu** khi khởi động (data.sql hoặc CommandLineRunner).
- Admin có thể thêm/sửa/xóa qua UI.

## 5. Thực thể dữ liệu (Entities)

### BilliardTable
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK, auto-increment |
| name | String | Tên bàn (VD: "Bàn 01") |
| tableType | Enum | POOL / CAROM / VIP |
| status | Enum | AVAILABLE / PLAYING |
| createdAt | LocalDateTime | Ngày tạo |

### PriceRule
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| tableType | Enum | POOL / CAROM / VIP |
| dayType | Enum | WEEKDAY / WEEKEND / HOLIDAY |
| startTime | LocalTime | Bắt đầu khung giờ (VD: 08:00) |
| endTime | LocalTime | Kết thúc khung giờ (VD: 12:00) |
| pricePerHour | BigDecimal | Giá mỗi giờ (VND) |

### Session
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| table | BilliardTable | FK → bàn đang chơi |
| startTime | LocalDateTime | Thời gian bắt đầu |
| endTime | LocalDateTime | Thời gian kết thúc (null nếu đang chơi) |
| totalAmount | BigDecimal | Tổng tiền (tính khi kết thúc) |
| status | Enum | ACTIVE / COMPLETED |
| staff | User | FK → nhân viên thao tác |

### SessionSegment
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| session | Session | FK → phiên chơi |
| priceRule | PriceRule | FK → rule áp dụng |
| startTime | LocalDateTime | Bắt đầu segment |
| endTime | LocalDateTime | Kết thúc segment |
| durationMinutes | Integer | Thời gian thực tế (phút) |
| amount | BigDecimal | Thành tiền segment |

### User (Auth)
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK |
| username | String | Tên đăng nhập |
| password | String | BCrypt hash |
| fullName | String | Họ tên |
| role | Enum | ADMIN / STAFF |
| active | Boolean | Trạng thái tài khoản |

---
