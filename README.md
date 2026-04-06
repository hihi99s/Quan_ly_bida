# Hệ Thống Quản Lý Quán Bida

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4-06B6D4?style=flat-square&logo=tailwindcss)](https://tailwindcss.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)

Ứng dụng web full-stack quản lý quán bida, hỗ trợ theo dõi bàn chơi theo thời gian thực, tính tiền tự động, quản lý kho hàng (đồ ăn & nước uống), phân quyền người dùng và báo cáo doanh thu.

---

## Mục Lục

- [Tính Năng Chính](#tính-năng-chính)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Hướng Dẫn Cài Đặt](#hướng-dẫn-cài-đặt)
- [Tài Khoản Mặc Định](#tài-khoản-mặc-định)
- [API Endpoints](#api-endpoints)
- [Cơ Sở Dữ Liệu](#cơ-sở-dữ-liệu)
- [Phân Quyền](#phân-quyền)
- [Triển Khai Với Docker](#triển-khai-với-docker)
- [CI/CD](#cicd)

---

## Tính Năng Chính

| Tính năng | Mô tả |
|-----------|-------|
| **Quản lý bàn chơi** | Theo dõi trạng thái bàn (Pool, Carom, VIP) theo thời gian thực qua WebSocket |
| **Tính tiền tự động** | Tính chi phí dựa trên thời gian chơi, loại bàn, khung giờ, ngày lễ/cuối tuần |
| **Quản lý hoá đơn** | Tạo hoá đơn chi tiết với thông tin bàn chơi, đồ ăn/uống, giảm giá |
| **Quản lý sản phẩm** | Hệ thống POS cho đồ ăn & nước uống, cảnh báo tồn kho thấp |
| **Quản lý khách hàng** | Hồ sơ khách hàng, tích điểm thành viên (Bronze/Silver/Gold/Diamond) |
| **Đặt bàn trước** | Hệ thống đặt bàn với theo dõi trạng thái |
| **Lịch nghỉ lễ** | Cấu hình ngày lễ để tự động áp dụng phụ phí |
| **Mã giảm giá** | Quản lý mã khuyến mãi theo phần trăm |
| **Báo cáo & Thống kê** | Biểu đồ doanh thu, sản phẩm bán chạy, khách hàng VIP, hiệu suất bàn |
| **Lịch làm việc** | Phân ca, theo dõi chấm công, lịch làm việc cá nhân |
| **Xuất PDF** | In hoá đơn định dạng PDF với iText |

---

## Công Nghệ Sử Dụng

### Backend

| Thành phần | Công nghệ |
|------------|-----------|
| Framework | Spring Boot 3.2.5 |
| Ngôn ngữ | Java 17 |
| Build tool | Maven 3.9 |
| ORM | Spring Data JPA / Hibernate |
| Cơ sở dữ liệu | MySQL 8.0 |
| Bảo mật | Spring Security 6 (Session-based, BCrypt) |
| WebSocket | STOMP + SockJS |
| Xuất PDF | iText 7.2.5 |
| Tiện ích | Lombok, Spring DevTools |

### Frontend

| Thành phần | Công nghệ |
|------------|-----------|
| Framework | React 19 |
| Build tool | Vite 8 |
| CSS | Tailwind CSS 4 |
| Router | React Router DOM 7 |
| Biểu đồ | Chart.js + react-chartjs-2 |
| PWA | Vite PWA Plugin (Workbox) |

---

## Cấu Trúc Dự Án

```
do_an_J2EE/
├── code/
│   ├── backend/                          # Spring Boot API
│   │   ├── src/main/java/com/bida/
│   │   │   ├── billing/                  # Logic tính tiền (SegmentSplitter, PricingStrategy)
│   │   │   ├── config/                   # Security, WebSocket, DataSeeder
│   │   │   ├── controller/
│   │   │   │   ├── admin/                # Controller trang admin (Thymeleaf)
│   │   │   │   └── api/                  # REST API controllers
│   │   │   ├── dto/                      # Data Transfer Objects
│   │   │   ├── entity/                   # JPA entities (17 bảng)
│   │   │   ├── repository/               # Spring Data repositories
│   │   │   ├── service/                  # Business logic (17 services)
│   │   │   └── websocket/               # Phát trạng thái bàn theo thời gian thực
│   │   ├── src/main/resources/
│   │   │   ├── templates/                # Thymeleaf templates
│   │   │   └── application.properties    # Cấu hình ứng dụng
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   └── frontend/                         # React SPA
│       ├── src/
│       │   ├── pages/                    # 18 trang giao diện
│       │   ├── App.jsx                   # Router & xác thực
│       │   ├── Layout.jsx                # Sidebar & navigation
│       │   └── api.js                    # API client
│       ├── vite.config.js
│       └── package.json
│
├── .github/workflows/                    # GitHub Actions CI
└── README.md
```

---

## Hướng Dẫn Cài Đặt

### Yêu Cầu Hệ Thống

- Java 17 JDK trở lên
- Node.js 18+ và npm
- MySQL 8.0
- Maven 3.9+

### 1. Cài Đặt Cơ Sở Dữ Liệu

```sql
CREATE DATABASE bida_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Cấu hình kết nối trong `code/backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bida_db
spring.datasource.username=root
spring.datasource.password=
```

> Hệ thống sử dụng `ddl-auto=update` nên các bảng sẽ được tạo tự động khi chạy lần đầu.

### 2. Khởi Động Backend

```bash
cd code/backend
mvn clean install
mvn spring-boot:run
```

Backend chạy tại `http://localhost:8080`. Dữ liệu mẫu (bàn chơi, sản phẩm, tài khoản, giá) sẽ được tự động tạo khi khởi động lần đầu.

### 3. Khởi Động Frontend

```bash
cd code/frontend
npm install
npm run dev
```

Frontend chạy tại `http://localhost:5173`. API được proxy tự động sang backend qua cấu hình Vite.

---

## Tài Khoản Mặc Định

| Vai trò | Tên đăng nhập | Mật khẩu |
|---------|---------------|----------|
| Admin | `admin` | `admin123` |
| Nhân viên | `staff1` | `staff123` |

> Dữ liệu mẫu được tạo bởi `DataSeeder`: 10 bàn chơi, 27 quy tắc giá, 10 sản phẩm, 5 ca làm việc.

---

## API Endpoints

### Xác Thực (`/api/auth`)

| Phương thức | Đường dẫn | Mô tả |
|-------------|-----------|-------|
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/logout` | Đăng xuất |
| GET | `/api/auth/me` | Thông tin người dùng hiện tại |

### Quản Lý Bàn (`/api/tables`)

| Phương thức | Đường dẫn | Mô tả |
|-------------|-----------|-------|
| GET | `/api/tables` | Danh sách bàn và trạng thái |
| POST | `/api/tables/{id}/start` | Bắt đầu phiên chơi |
| POST | `/api/tables/{id}/end` | Kết thúc & tính tiền |
| POST | `/api/tables/{id}/pause` | Tạm dừng phiên |
| POST | `/api/tables/{id}/resume` | Tiếp tục phiên |
| POST | `/api/tables/{id}/transfer` | Chuyển bàn |
| POST | `/api/tables/{id}/maintenance` | Bật/tắt chế độ bảo trì |
| GET | `/api/tables/{id}/orders` | Danh sách order của phiên |
| POST | `/api/tables/{id}/orders` | Thêm order |

### Dashboard (`/api/dashboard`)

| Phương thức | Đường dẫn | Mô tả |
|-------------|-----------|-------|
| GET | `/api/dashboard/full` | Toàn bộ dữ liệu dashboard |
| GET | `/api/dashboard/summary` | Tổng quan hệ thống |
| GET | `/api/dashboard/kpis` | Chỉ số KPI |
| GET | `/api/dashboard/revenue-chart` | Biểu đồ doanh thu |
| GET | `/api/dashboard/top-products` | Sản phẩm bán chạy |
| GET | `/api/dashboard/top-customers` | Khách hàng hàng đầu |

### Các API Khác

| Module | Đường dẫn | Mô tả |
|--------|-----------|-------|
| Hoá đơn | `/api/invoices` | CRUD hoá đơn |
| Sản phẩm | `/api/products` | CRUD sản phẩm |
| Khách hàng | `/api/customers` | CRUD khách hàng |
| Giá | `/api/prices` | CRUD quy tắc giá |
| Đặt bàn | `/api/reservations` | CRUD đặt bàn |
| Mã giảm giá | `/api/discounts` | CRUD mã khuyến mãi |
| Ngày lễ | `/api/holidays` | CRUD lịch nghỉ lễ |
| Lịch làm việc | `/api/schedules` | CRUD ca làm việc |
| Báo cáo | `/api/reports` | Báo cáo doanh thu |
| Nhân viên | `/api/users` | CRUD tài khoản |

### WebSocket

```
Endpoint:    /ws (STOMP + SockJS)
Subscribe:   /topic/tables    — Nhận cập nhật trạng thái bàn mỗi 5 giây
```

---

## Cơ Sở Dữ Liệu

### Sơ Đồ Thực Thể (17 bảng)

```
billiard_tables ──┬── sessions ──┬── session_segments
                  │              ├── order_items ── products
                  │              └── invoices ── discount_codes
                  └── reservations

users ──┬── staff_schedules ── shifts
        └── schedule_audit_logs

customers ── sessions

price_rules        (loại bàn x loại ngày x khung giờ)
holiday_calendars  (ngày lễ → áp dụng giá đặc biệt)
shift_closings     (tổng kết cuối ca)
app_settings       (cấu hình hệ thống)
```

### Các Enum

| Enum | Giá trị |
|------|---------|
| `UserRole` | ADMIN, STAFF |
| `TableType` | POOL, CAROM, VIP |
| `TableStatus` | AVAILABLE, OCCUPIED, DISABLED, MAINTENANCE |
| `SessionStatus` | ACTIVE, PAUSED, CLOSED |
| `DayType` | WEEKDAY, WEEKEND, HOLIDAY |
| `ReservationStatus` | PENDING, CONFIRMED, CANCELLED, COMPLETED |
| `MembershipTier` | BRONZE (0-500K), SILVER (500K-2M), GOLD (2M-5M), DIAMOND (5M+) |

---

## Phân Quyền

| Chức năng | Admin | Nhân viên |
|-----------|:-----:|:---------:|
| Dashboard | x | x |
| Quản lý bàn chơi | x | x |
| Xem hoá đơn | x | x |
| Quản lý khách hàng | x | x |
| Đặt bàn | x | x |
| Lịch làm việc cá nhân | x | x |
| Báo cáo & thống kê | x | |
| Quản lý sản phẩm | x | |
| Cấu hình giá | x | |
| Quản lý nghỉ lễ | x | |
| Quản lý mã giảm giá | x | |
| Quản lý nhân viên | x | |
| Phân ca làm việc | x | |

---

## Triển Khai Với Docker

### Backend

```bash
cd code/backend
docker build -t bida-backend .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host:3306/bida_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=yourpassword \
  -e SPRING_PROFILES_ACTIVE=prod \
  bida-backend
```

### Biến Môi Trường

| Biến | Mô tả | Mặc định |
|------|-------|----------|
| `DB_URL` | JDBC connection string | `jdbc:mysql://localhost:3306/bida_db` |
| `DB_USERNAME` | Tài khoản database | `root` |
| `DB_PASSWORD` | Mật khẩu database | (trống) |
| `SPRING_PROFILES_ACTIVE` | Profile Spring Boot | `dev` |

---

## CI/CD

Project sử dụng **GitHub Actions** để tự động chạy test backend:

- **Trigger**: Push hoặc Pull Request vào nhánh `main`
- **Quy trình**: Checkout → Setup JDK 17 → Maven test

File cấu hình: `.github/workflows/backend-ci.yml`

---

## Giải Thích Thuật Toán Tính Tiền

Hệ thống tính tiền dựa trên các yếu tố:

1. **Phân đoạn thời gian** — `SegmentSplitter` chia phiên chơi tại mốc 00:00 nếu kéo qua ngày
2. **Chiến lược giá** — `PricingStrategy` áp dụng giá theo loại bàn, loại ngày (thường/cuối tuần/lễ), và khung giờ
3. **Trừ thời gian tạm dừng** — Tự động trừ thời gian bàn bị tạm dừng
4. **Giảm giá** — Áp dụng giảm giá thành viên + mã khuyến mãi
5. **Tổng hợp** — Cộng phí bàn chơi + phí đồ ăn/uống → tạo hoá đơn

---

## Giấy Phép

Dự án phục vụ mục đích học tập (Đồ án môn J2EE).
