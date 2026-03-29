# Task List: Phase 1 Core Implementation

## Task 1: Project Setup & Configuration (~30 phút)
- [ ] Cleanup old StudentManagement project
- [ ] Setup Maven `pom.xml` (Spring Boot 3.2.x, Java 17)
- [ ] Configure `application.properties` (MySQL + Thymeleaf)
- [ ] Create `BidaApplication.java` main class
- [ ] Verify: `mvn clean compile` pass

## Task 2: Domain Entities & Enums (~45 phút)
- [ ] Enums: TableType, TableStatus, DayType, SessionStatus, UserRole
- [ ] Entity: `BilliardTable` (name, tableType, status)
- [ ] Entity: `PriceRule` (tableType, dayType, startTime, endTime, pricePerHour)
- [ ] Entity: `Session` (table FK, startTime, endTime, totalAmount, status, staff FK)
- [ ] Entity: `SessionSegment` (session FK, priceRule FK, times, duration, amount)
- [ ] Entity: `User` (username, password BCrypt, fullName, role)
- [ ] Verify: Hibernate tạo bảng đúng

## Task 3: Repositories & Seed Data (~30 phút)
- [ ] `BilliardTableRepository` (findByStatus, findByTableType)
- [ ] `PriceRuleRepository` (findByTableTypeAndDayType)
- [ ] `SessionRepository` (findByTableAndStatus)
- [ ] `UserRepository` (findByUsername)
- [ ] `DataSeeder`: 10 bàn + PriceRules (3 loại × 3 ngày × 3 khung) + 3 users
- [ ] Verify: Khởi động app, MySQL có đủ data

## Task 4: Spring Security - 2 Role (~30 phút)
- [ ] `CustomUserDetailsService` (load từ DB, map role)
- [ ] `SecurityConfig` (ADMIN: /admin/**, STAFF: /dashboard/**)
- [ ] Trang `login.html` (form login Thymeleaf)
- [ ] Verify: Login ADMIN + STAFF, phân quyền chặn đúng

## Task 5: Core Billing Engine (~60 phút)
- [ ] DTO: `BillingSegment` + `BillingResult`
- [ ] Interface: `PricingStrategy`
- [ ] `SegmentSplitter` (chia session → segments theo PriceRule)
- [ ] `TimeSlotPricingStrategy` (implement strategy)
- [ ] `BillingCalculator` service (facade + holiday check)
- [ ] Unit Tests: 5 test cases (1 khung, cross khung, ngắn, holiday, weekend)
- [ ] Verify: Tất cả tests PASS

## Task 6: Table Management & REST API (~45 phút)
- [ ] `TableService` (CRUD bàn)
- [ ] `SessionService` (startSession, endSession, getCurrentAmount)
- [ ] `TableApiController` REST (GET /api/tables, POST start/end)
- [ ] `AdminTableController` Thymeleaf (CRUD UI cho ADMIN)
- [ ] Trang `admin/tables.html`
- [ ] Verify: API hoạt động + CRUD bàn qua UI

## Task 7: Price Rule Admin (~30 phút)
- [ ] `PriceRuleService` (CRUD rules, grouped by type/day)
- [ ] `AppSettings` entity (Holiday toggle)
- [ ] `AdminPriceController` + `admin/prices.html`
- [ ] Holiday ON/OFF toggle button
- [ ] Verify: Thêm/sửa/xóa giá + toggle Holiday

## Task 8: WebSocket Realtime Dashboard (~60 phút)
- [ ] `WebSocketConfig` (STOMP endpoint /ws, broker /topic)
- [ ] `TableStatusBroadcaster` (push /topic/tables)
- [ ] `DashboardController` (GET /dashboard)
- [ ] `dashboard.html` (Grid card AlpineJS)
- [ ] `dashboard.js` (SockJS + STOMP subscribe + polling backup)
- [ ] Card actions: click Trống → Start, click Đang chơi → End
- [ ] Verify: 2 browser, realtime sync

## Task 9: Integration & Polish (~30 phút)
- [ ] E2E flow test (login → start → wait → end → verify)
- [ ] Phân quyền test (staff bị chặn /admin)
- [ ] Billing test (cross khung, holiday, VIP vs Pool)
- [ ] UI cleanup (CSS responsive, error handling, loading)

---

**Tổng ước lượng: ~6 giờ**
**Lưu ý cho Code Agent: Mỗi task xong → `git commit`. Đọc PRD trước khi code.**
