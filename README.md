# Hệ Thống Quản Lý Quán Bida

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4-06B6D4?style=flat-square&logo=tailwindcss)](https://tailwindcss.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)

Ung dung web full-stack quan ly quan bida, ho tro theo doi ban choi theo thoi gian thuc, tinh tien tu dong, quan ly kho hang (do an & nuoc uong), phan quyen nguoi dung va bao cao doanh thu.

---

## Muc Luc

- [Tinh Nang Chinh](#tinh-nang-chinh)
- [Cong Nghe Su Dung](#cong-nghe-su-dung)
- [Cau Truc Du An](#cau-truc-du-an)
- [Huong Dan Cai Dat](#huong-dan-cai-dat)
- [Tai Khoan Mac Dinh](#tai-khoan-mac-dinh)
- [API Endpoints](#api-endpoints)
- [Co So Du Lieu](#co-so-du-lieu)
- [Phan Quyen](#phan-quyen)
- [Trien Khai Voi Docker](#trien-khai-voi-docker)
- [CI/CD](#cicd)

---

## Tinh Nang Chinh

| Tinh nang | Mo ta |
|-----------|-------|
| **Quan ly ban choi** | Theo doi trang thai ban (Pool, Carom, VIP) theo thoi gian thuc qua WebSocket |
| **Tinh tien tu dong** | Tinh chi phi dua tren thoi gian choi, loai ban, khung gio, ngay le/cuoi tuan |
| **Quan ly hoa don** | Tao hoa don chi tiet voi thong tin ban choi, do an/uong, giam gia |
| **Quan ly san pham** | He thong POS cho do an & nuoc uong, canh bao ton kho thap |
| **Quan ly khach hang** | Ho so khach hang, tich diem thanh vien (Bronze/Silver/Gold/Diamond) |
| **Dat ban truoc** | He thong dat ban voi theo doi trang thai |
| **Lich nghi le** | Cau hinh ngay le de tu dong ap dung phu phi |
| **Ma giam gia** | Quan ly ma khuyen mai theo phan tram |
| **Bao cao & Thong ke** | Bieu do doanh thu, san pham ban chay, khach hang VIP, hieu suat ban |
| **Lich lam viec** | Phan ca, theo doi cham cong, lich lam viec ca nhan |
| **Xuat PDF** | In hoa don dinh dang PDF voi iText |

---

## Cong Nghe Su Dung

### Backend

| Thanh phan | Cong nghe |
|------------|-----------|
| Framework | Spring Boot 3.2.5 |
| Ngon ngu | Java 17 |
| Build tool | Maven 3.9 |
| ORM | Spring Data JPA / Hibernate |
| Co so du lieu | MySQL 8.0 |
| Bao mat | Spring Security 6 (Session-based, BCrypt) |
| WebSocket | STOMP + SockJS |
| Xuat PDF | iText 7.2.5 |
| Utilities | Lombok, Spring DevTools |

### Frontend

| Thanh phan | Cong nghe |
|------------|-----------|
| Framework | React 19 |
| Build tool | Vite 8 |
| CSS | Tailwind CSS 4 |
| Router | React Router DOM 7 |
| Bieu do | Chart.js + react-chartjs-2 |
| PWA | Vite PWA Plugin (Workbox) |

---

## Cau Truc Du An

```
do_an_J2EE/
├── code/
│   ├── backend/                          # Spring Boot API
│   │   ├── src/main/java/com/bida/
│   │   │   ├── billing/                  # Logic tinh tien (SegmentSplitter, PricingStrategy)
│   │   │   ├── config/                   # Security, WebSocket, DataSeeder
│   │   │   ├── controller/
│   │   │   │   ├── admin/                # Controller trang admin (Thymeleaf)
│   │   │   │   └── api/                  # REST API controllers
│   │   │   ├── dto/                      # Data Transfer Objects
│   │   │   ├── entity/                   # JPA entities (17 bang)
│   │   │   ├── repository/               # Spring Data repositories
│   │   │   ├── service/                  # Business logic (17 services)
│   │   │   └── websocket/               # Phat trang thai ban theo thoi gian thuc
│   │   ├── src/main/resources/
│   │   │   ├── templates/                # Thymeleaf templates
│   │   │   └── application.properties    # Cau hinh ung dung
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   └── frontend/                         # React SPA
│       ├── src/
│       │   ├── pages/                    # 18 trang giao dien
│       │   ├── App.jsx                   # Router & xac thuc
│       │   ├── Layout.jsx                # Sidebar & navigation
│       │   └── api.js                    # API client
│       ├── vite.config.js
│       └── package.json
│
├── .github/workflows/                    # GitHub Actions CI
└── README.md
```

---

## Huong Dan Cai Dat

### Yeu Cau He Thong

- Java 17 JDK tro len
- Node.js 18+ va npm
- MySQL 8.0
- Maven 3.9+

### 1. Cai Dat Co So Du Lieu

```sql
CREATE DATABASE bida_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Cau hinh ket noi trong `code/backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bida_db
spring.datasource.username=root
spring.datasource.password=
```

> He thong su dung `ddl-auto=update` nen cac bang se duoc tao tu dong khi chay lan dau.

### 2. Khoi Dong Backend

```bash
cd code/backend
mvn clean install
mvn spring-boot:run
```

Backend chay tai `http://localhost:8080`. Du lieu mau (ban choi, san pham, tai khoan, gia) se duoc tu dong tao khi khoi dong lan dau.

### 3. Khoi Dong Frontend

```bash
cd code/frontend
npm install
npm run dev
```

Frontend chay tai `http://localhost:5173`. API duoc proxy tu dong sang backend qua cau hinh Vite.

---

## Tai Khoan Mac Dinh

| Vai tro | Ten dang nhap | Mat khau |
|---------|---------------|----------|
| Admin | `admin` | `admin123` |
| Nhan vien | `staff1` | `staff123` |

> Du lieu mau duoc tao boi `DataSeeder`: 10 ban choi, 27 quy tac gia, 10 san pham, 5 ca lam viec.

---

## API Endpoints

### Xac Thuc (`/api/auth`)

| Phuong thuc | Duong dan | Mo ta |
|-------------|-----------|-------|
| POST | `/api/auth/login` | Dang nhap |
| POST | `/api/auth/logout` | Dang xuat |
| GET | `/api/auth/me` | Thong tin nguoi dung hien tai |

### Quan Ly Ban (`/api/tables`)

| Phuong thuc | Duong dan | Mo ta |
|-------------|-----------|-------|
| GET | `/api/tables` | Danh sach ban va trang thai |
| POST | `/api/tables/{id}/start` | Bat dau phien choi |
| POST | `/api/tables/{id}/end` | Ket thuc & tinh tien |
| POST | `/api/tables/{id}/pause` | Tam dung phien |
| POST | `/api/tables/{id}/resume` | Tiep tuc phien |
| POST | `/api/tables/{id}/transfer` | Chuyen ban |
| POST | `/api/tables/{id}/maintenance` | Bat/tat che do bao tri |
| GET | `/api/tables/{id}/orders` | Danh sach order cua phien |
| POST | `/api/tables/{id}/orders` | Them order |

### Dashboard (`/api/dashboard`)

| Phuong thuc | Duong dan | Mo ta |
|-------------|-----------|-------|
| GET | `/api/dashboard/full` | Toan bo du lieu dashboard |
| GET | `/api/dashboard/summary` | Tong quan he thong |
| GET | `/api/dashboard/kpis` | Chi so KPI |
| GET | `/api/dashboard/revenue-chart` | Bieu do doanh thu |
| GET | `/api/dashboard/top-products` | San pham ban chay |
| GET | `/api/dashboard/top-customers` | Khach hang hang dau |

### Cac API Khac

| Module | Duong dan | Mo ta |
|--------|-----------|-------|
| Hoa don | `/api/invoices` | CRUD hoa don |
| San pham | `/api/products` | CRUD san pham |
| Khach hang | `/api/customers` | CRUD khach hang |
| Gia | `/api/prices` | CRUD quy tac gia |
| Dat ban | `/api/reservations` | CRUD dat ban |
| Ma giam gia | `/api/discounts` | CRUD ma khuyen mai |
| Ngay le | `/api/holidays` | CRUD lich nghi le |
| Lich lam viec | `/api/schedules` | CRUD ca lam viec |
| Bao cao | `/api/reports` | Bao cao doanh thu |
| Nhan vien | `/api/users` | CRUD tai khoan |

### WebSocket

```
Endpoint:    /ws (STOMP + SockJS)
Subscribe:   /topic/tables    — Nhan cap nhat trang thai ban moi 5 giay
```

---

## Co So Du Lieu

### So Do Thuc The (17 bang)

```
billiard_tables ──┬── sessions ──┬── session_segments
                  │              ├── order_items ── products
                  │              └── invoices ── discount_codes
                  └── reservations
                  
users ──┬── staff_schedules ── shifts
        └── schedule_audit_logs

customers ── sessions

price_rules        (loai_ban × loai_ngay × khung_gio)
holiday_calendars  (ngay le → ap dung gia dac biet)
shift_closings     (tong ket cuoi ca)
app_settings       (cau hinh he thong)
```

### Cac Enum

| Enum | Gia tri |
|------|---------|
| `UserRole` | ADMIN, STAFF |
| `TableType` | POOL, CAROM, VIP |
| `TableStatus` | AVAILABLE, OCCUPIED, DISABLED, MAINTENANCE |
| `SessionStatus` | ACTIVE, PAUSED, CLOSED |
| `DayType` | WEEKDAY, WEEKEND, HOLIDAY |
| `ReservationStatus` | PENDING, CONFIRMED, CANCELLED, COMPLETED |
| `MembershipTier` | BRONZE (0-500K), SILVER (500K-2M), GOLD (2M-5M), DIAMOND (5M+) |

---

## Phan Quyen

| Chuc nang | Admin | Nhan vien |
|-----------|:-----:|:---------:|
| Dashboard | x | x |
| Quan ly ban choi | x | x |
| Xem hoa don | x | x |
| Quan ly khach hang | x | x |
| Dat ban | x | x |
| Lich lam viec ca nhan | x | x |
| Bao cao & thong ke | x | |
| Quan ly san pham | x | |
| Cau hinh gia | x | |
| Quan ly nghi le | x | |
| Quan ly ma giam gia | x | |
| Quan ly nhan vien | x | |
| Phan ca lam viec | x | |

---

## Trien Khai Voi Docker

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

### Bien Moi Truong

| Bien | Mo ta | Mac dinh |
|------|-------|----------|
| `DB_URL` | JDBC connection string | `jdbc:mysql://localhost:3306/bida_db` |
| `DB_USERNAME` | Tai khoan database | `root` |
| `DB_PASSWORD` | Mat khau database | (trong) |
| `SPRING_PROFILES_ACTIVE` | Profile Spring Boot | `dev` |

---

## CI/CD

Project su dung **GitHub Actions** de tu dong chay test backend:

- **Trigger**: Push hoac Pull Request vao nhanh `main`
- **Quy trinh**: Checkout → Setup JDK 17 → Maven test

File cau hinh: `.github/workflows/backend-ci.yml`

---

## Giai Thich Thuat Toan Tinh Tien

He thong tinh tien dua tren cac yeu to:

1. **Phan doan thoi gian** — `SegmentSplitter` chia phien choi tai moc 00:00 neu keo qua ngay
2. **Chien luoc gia** — `PricingStrategy` ap dung gia theo loai ban, loai ngay (thuong/cuoi tuan/le), va khung gio
3. **Tru thoi gian tam dung** — Tu dong tru thoi gian ban bi tam dung
4. **Giam gia** — Ap dung giam gia thanh vien + ma khuyen mai
5. **Tong hop** — Cong phi ban choi + phi do an/uong → tao hoa don

---

## Giay Phep

Du an phuc vu muc dich hoc tap (Do an mon J2EE).
