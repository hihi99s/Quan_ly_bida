# Phase 1: Core Implementation Plan - Billiard Management System

> **Brain Agent** | **Skill:** writing-plans/
> **For Code Agent:** Implement task-by-task. Đọc PRD tại `brain/prd/2026-03-28-bida-core-spec.md` trước khi bắt đầu.
> Mỗi task xong → git commit. Feedback cho Brain Agent nếu plan chưa rõ.

**Goal:** Xây dựng hệ thống quản lý bàn bida với billing engine tính tiền theo phút thực tế, dynamic pricing (khung giờ + ngày + lễ), dashboard realtime, và phân quyền 2 role.

**Architecture:** Monolith Spring Boot, Strategy Pattern cho Billing, WebSocket STOMP cho Realtime, Spring Security cho Auth.

**Tech Stack:** Spring Boot 3.x, JPA, MySQL, WebSocket, Thymeleaf, AlpineJS, Maven.

---

## Task 1: Project Setup & Configuration
**Ước lượng:** ~30 phút
**Files:**
- Create: `code/backend/pom.xml`
- Create: `code/backend/src/main/resources/application.properties`
- Create: `code/backend/src/main/java/com/bida/BidaApplication.java`

### Steps:
- [ ] **Step 1.1: Cleanup**
  Xóa toàn bộ file `StudentManagement` cũ trong `code/backend/` (nếu có).

- [ ] **Step 1.2: Khởi tạo Maven project**
  Tạo `pom.xml` với dependencies:
  ```
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-security
  - spring-boot-starter-websocket
  - spring-boot-starter-thymeleaf
  - spring-boot-starter-validation
  - mysql-connector-j
  - lombok
  - spring-boot-devtools
  - spring-boot-starter-test
  ```
  GroupId: `com.bida` | ArtifactId: `bida-management`
  Java version: 17 | Spring Boot: 3.2.x

- [ ] **Step 1.3: Cấu hình application.properties**
  ```properties
  # MySQL
  spring.datasource.url=jdbc:mysql://localhost:3306/bida_db
  spring.datasource.username=root
  spring.datasource.password=
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true

  # WebSocket - không cần config đặc biệt ở đây

  # Thymeleaf
  spring.thymeleaf.cache=false
  ```

- [ ] **Step 1.4: Tạo BidaApplication.java (main class)**

- [ ] **Step 1.5: Verify** → `mvn clean compile` phải pass.

**→ Code Agent: Commit "Task 1: Project setup with Spring Boot + Maven"**

---

## Task 2: Domain Entities & Enums
**Ước lượng:** ~45 phút
**Files:**
- Create: `com/bida/entity/enums/TableType.java`
- Create: `com/bida/entity/enums/TableStatus.java`
- Create: `com/bida/entity/enums/DayType.java`
- Create: `com/bida/entity/enums/SessionStatus.java`
- Create: `com/bida/entity/enums/UserRole.java`
- Create: `com/bida/entity/BilliardTable.java`
- Create: `com/bida/entity/PriceRule.java`
- Create: `com/bida/entity/Session.java`
- Create: `com/bida/entity/SessionSegment.java`
- Create: `com/bida/entity/User.java`

### Steps:
- [ ] **Step 2.1: Tạo Enums**
  ```java
  TableType:     POOL, CAROM, VIP
  TableStatus:   AVAILABLE, PLAYING
  DayType:       WEEKDAY, WEEKEND, HOLIDAY
  SessionStatus: ACTIVE, COMPLETED
  UserRole:      ADMIN, STAFF
  ```

- [ ] **Step 2.2: Tạo Entity BilliardTable**
  Fields: id, name, tableType (Enum), status (Enum), createdAt.
  `@Enumerated(EnumType.STRING)` cho enums.

- [ ] **Step 2.3: Tạo Entity PriceRule**
  Fields: id, tableType (Enum), dayType (Enum), startTime (LocalTime), endTime (LocalTime), pricePerHour (BigDecimal).
  Unique constraint: (tableType, dayType, startTime).

- [ ] **Step 2.4: Tạo Entity Session**
  Fields: id, table (ManyToOne → BilliardTable), startTime, endTime, totalAmount, status (Enum), staff (ManyToOne → User).
  `@ManyToOne @JoinColumn` cho FK.

- [ ] **Step 2.5: Tạo Entity SessionSegment**
  Fields: id, session (ManyToOne → Session), priceRule (ManyToOne → PriceRule), startTime, endTime, durationMinutes, amount.

- [ ] **Step 2.6: Tạo Entity User**
  Fields: id, username (unique), password, fullName, role (Enum), active.

- [ ] **Step 2.7: Verify** → `mvn clean compile` pass + kiểm tra Hibernate tạo bảng đúng.

**→ Code Agent: Commit "Task 2: Domain entities and enums"**

---

## Task 3: JPA Repositories & Seed Data
**Ước lượng:** ~30 phút
**Files:**
- Create: `com/bida/repository/BilliardTableRepository.java`
- Create: `com/bida/repository/PriceRuleRepository.java`
- Create: `com/bida/repository/SessionRepository.java`
- Create: `com/bida/repository/SessionSegmentRepository.java`
- Create: `com/bida/repository/UserRepository.java`
- Create: `com/bida/config/DataSeeder.java`

### Steps:
- [ ] **Step 3.1: Tạo Repositories**
  ```java
  BilliardTableRepository:
    - findByStatus(TableStatus status)
    - findByTableType(TableType type)

  PriceRuleRepository:
    - findByTableTypeAndDayType(TableType, DayType)
    - findByTableTypeAndDayTypeOrderByStartTime(TableType, DayType)

  SessionRepository:
    - findByTableAndStatus(BilliardTable, SessionStatus)
    - findByStatus(SessionStatus)

  UserRepository:
    - findByUsername(String username)
  ```

- [ ] **Step 3.2: Tạo DataSeeder (CommandLineRunner)**
  Seed data mẫu:
  - **Bàn**: 10 bàn (4 Pool, 3 Carom, 3 VIP)
  - **PriceRule**: 3 bộ (weekday/weekend/holiday) × 3 loại bàn × 3 khung giờ
    - Pool weekday: 8-12h: 40k, 12-17h: 50k, 17-23h: 70k
    - Pool weekend: 8-12h: 50k, 12-17h: 60k, 17-23h: 80k
    - (tương tự cho Carom +10k, VIP +30k)
  - **User**: 1 admin (admin/admin123), 2 staff (staff1/staff123, staff2/staff123)
  - Chỉ seed nếu DB trống (check count == 0).

- [ ] **Step 3.3: Verify** → Khởi động app, kiểm tra MySQL có đủ data.

**→ Code Agent: Commit "Task 3: Repositories and seed data"**

---

## Task 4: Spring Security (2 Role)
**Ước lượng:** ~30 phút
**Files:**
- Create: `com/bida/config/SecurityConfig.java`
- Create: `com/bida/service/CustomUserDetailsService.java`
- Create: `src/main/resources/templates/login.html`

### Steps:
- [ ] **Step 4.1: Tạo CustomUserDetailsService**
  Implement `UserDetailsService`, load từ `UserRepository`.
  Map `UserRole` → `GrantedAuthority` ("ROLE_ADMIN", "ROLE_STAFF").

- [ ] **Step 4.2: Tạo SecurityConfig**
  ```java
  - /login → permitAll
  - /admin/** → hasRole("ADMIN")
  - /api/** → authenticated
  - /dashboard/** → authenticated (cả ADMIN + STAFF)
  - /ws/** → permitAll (WebSocket)
  - /css/**, /js/** → permitAll (static resources)
  - Password encoder: BCryptPasswordEncoder
  - Form login → /login, default success → /dashboard
  ```

- [ ] **Step 4.3: Tạo trang Login (Thymeleaf)**
  Form đơn giản: username + password + nút Login.
  Hiển thị lỗi nếu sai.

- [ ] **Step 4.4: Verify** → Login admin/admin123 → /dashboard. Login staff1/staff123 → /dashboard. Truy cập /admin/ bằng staff → 403.

**→ Code Agent: Commit "Task 4: Spring Security with ADMIN/STAFF roles"**

---

## Task 5: Core Billing Engine (Strategy Pattern)
**Ước lượng:** ~60 phút
**Files:**
- Create: `com/bida/billing/strategy/PricingStrategy.java` (interface)
- Create: `com/bida/billing/strategy/TimeSlotPricingStrategy.java`
- Create: `com/bida/billing/model/BillingSegment.java` (DTO)
- Create: `com/bida/billing/model/BillingResult.java` (DTO)
- Create: `com/bida/billing/service/SegmentSplitter.java`
- Create: `com/bida/billing/service/BillingCalculator.java`
- Create: `test/.../billing/BillingCalculatorTest.java`

### Steps:
- [ ] **Step 5.1: Tạo DTO BillingSegment & BillingResult**
  ```java
  BillingSegment {
    LocalDateTime startTime;
    LocalDateTime endTime;
    long durationMinutes;
    BigDecimal pricePerHour;
    BigDecimal amount;  // (minutes / 60) * pricePerHour
  }

  BillingResult {
    List<BillingSegment> segments;
    BigDecimal totalAmount;
    long totalMinutes;
  }
  ```

- [ ] **Step 5.2: Tạo PricingStrategy interface**
  ```java
  interface PricingStrategy {
    BillingResult calculate(LocalDateTime start, LocalDateTime end,
                           TableType tableType, DayType dayType);
  }
  ```

- [ ] **Step 5.3: Tạo SegmentSplitter**
  Logic chính:
  ```
  Input: start, end, List<PriceRule> (sorted by startTime)
  Output: List<BillingSegment>

  Algorithm:
  1. cursor = start
  2. Tìm PriceRule chứa cursor.toLocalTime()
  3. segmentEnd = min(end, cursor.toLocalDate().atTime(rule.endTime))
  4. Tạo segment: duration = ChronoUnit.MINUTES.between(cursor, segmentEnd)
  5. amount = (duration / 60.0) * pricePerHour
  6. cursor = segmentEnd
  7. Lặp lại cho đến cursor >= end
  ```
  **Edge cases:**
  - Phiên chơi qua nửa đêm (VD: 22:00 → 01:00) → cần xử lý ngày tiếp theo.
  - Nếu không có PriceRule cho khung giờ → dùng giá mặc định hoặc throw exception.

- [ ] **Step 5.4: Tạo TimeSlotPricingStrategy**
  Implement `PricingStrategy`, dùng `SegmentSplitter` + `PriceRuleRepository`.

- [ ] **Step 5.5: Tạo BillingCalculator (Service)**
  Facade service:
  ```java
  @Service
  BillingCalculator {
    - PricingStrategy strategy; (inject)
    - DayType getCurrentDayType(); // check holiday flag, weekend, weekday
    - BillingResult calculate(Session session);
    - BigDecimal calculateCurrentAmount(Session session); // for realtime display
  }
  ```
  **Holiday flag**: lưu trong application config hoặc database setting.

- [ ] **Step 5.6: Unit Tests**
  ```
  Test 1: Phiên trong 1 khung giờ (10:00-11:30 weekday Pool) → 90 phút × 40k/h
  Test 2: Phiên cross khung giờ (11:00-13:00) → segment 11-12 + segment 12-13
  Test 3: Phiên ngắn (10:00-10:15) → 15 phút tính chính xác
  Test 4: Holiday flag ON → dùng giá holiday
  Test 5: Weekend → dùng giá weekend
  ```

- [ ] **Step 5.7: Verify** → Tất cả tests pass.

**→ Code Agent: Commit "Task 5: Billing engine with Strategy Pattern + unit tests"**

---

## Task 6: Table Management Service & REST API
**Ước lượng:** ~45 phút
**Files:**
- Create: `com/bida/service/TableService.java`
- Create: `com/bida/service/SessionService.java`
- Create: `com/bida/controller/api/TableApiController.java`
- Create: `com/bida/controller/admin/AdminTableController.java`
- Create: `src/main/resources/templates/admin/tables.html`

### Steps:
- [ ] **Step 6.1: Tạo TableService**
  ```java
  - getAllTables() → List<BilliardTable>
  - getTablesByStatus(TableStatus) → List
  - createTable(name, type) → BilliardTable (ADMIN only)
  - updateTable(id, name, type) → BilliardTable (ADMIN only)
  - deleteTable(id) → void (ADMIN only, chỉ xóa nếu AVAILABLE)
  ```

- [ ] **Step 6.2: Tạo SessionService**
  ```java
  - startSession(tableId, staffId) → Session
    → Set table status = PLAYING
    → Create Session(status=ACTIVE, startTime=now)

  - endSession(tableId, staffId) → Session
    → Set endTime = now
    → Call BillingCalculator.calculate(session)
    → Set totalAmount, status = COMPLETED
    → Set table status = AVAILABLE
    → Return session with totalAmount

  - getCurrentAmount(tableId) → BigDecimal
    → Tính tiền tạm (cho dashboard realtime)
  ```

- [ ] **Step 6.3: Tạo TableApiController (REST)**
  ```
  GET    /api/tables              → Danh sách tất cả bàn + trạng thái
  POST   /api/tables/{id}/start   → Bắt đầu phiên (STAFF+)
  POST   /api/tables/{id}/end     → Kết thúc phiên (STAFF+)
  GET    /api/tables/{id}/current  → Tiền tạm tính hiện tại
  ```

- [ ] **Step 6.4: Tạo AdminTableController (Thymeleaf)**
  ```
  GET  /admin/tables           → Trang quản lý bàn (list + form CRUD)
  POST /admin/tables           → Thêm bàn
  POST /admin/tables/{id}/edit → Sửa bàn
  POST /admin/tables/{id}/delete → Xóa bàn
  ```

- [ ] **Step 6.5: Tạo trang admin/tables.html**
  Bảng danh sách bàn + form thêm/sửa. Dùng AlpineJS cho toggle form.

- [ ] **Step 6.6: Verify** → Test API bằng curl hoặc browser. CRUD bàn hoạt động.

**→ Code Agent: Commit "Task 6: Table management service and REST API"**

---

## Task 7: Price Rule Admin (CRUD giá)
**Ước lượng:** ~30 phút
**Files:**
- Create: `com/bida/service/PriceRuleService.java`
- Create: `com/bida/controller/admin/AdminPriceController.java`
- Create: `src/main/resources/templates/admin/prices.html`
- Create: `com/bida/config/AppSettings.java`

### Steps:
- [ ] **Step 7.1: Tạo PriceRuleService**
  ```java
  - getRulesByTableType(TableType) → grouped by DayType
  - createRule(tableType, dayType, start, end, price) → PriceRule
  - updateRule(id, start, end, price) → PriceRule
  - deleteRule(id) → void
  ```

- [ ] **Step 7.2: Tạo AppSettings (Holiday toggle)**
  ```java
  @Entity AppSettings {
    String key;    // "HOLIDAY_MODE"
    String value;  // "true" / "false"
  }
  ```
  Service method: `isHolidayMode()`, `toggleHolidayMode()`.

- [ ] **Step 7.3: Tạo AdminPriceController + trang prices.html**
  - Xem bảng giá theo loại bàn (tab POOL / CAROM / VIP).
  - Mỗi tab có 3 bảng: Weekday / Weekend / Holiday.
  - Form thêm/sửa/xóa rule.
  - Nút bật/tắt Holiday Mode (toggle lớn, rõ ràng).

- [ ] **Step 7.4: Verify** → Thêm/sửa/xóa giá qua UI. Holiday toggle hoạt động.

**→ Code Agent: Commit "Task 7: Price rule admin with holiday toggle"**

---

## Task 8: WebSocket Realtime Dashboard
**Ước lượng:** ~60 phút
**Files:**
- Create: `com/bida/config/WebSocketConfig.java`
- Create: `com/bida/websocket/TableStatusBroadcaster.java`
- Create: `com/bida/controller/DashboardController.java`
- Create: `src/main/resources/templates/dashboard.html`
- Create: `src/main/resources/static/js/dashboard.js`

### Steps:
- [ ] **Step 8.1: Cấu hình WebSocket STOMP**
  ```java
  @Configuration @EnableWebSocketMessageBroker
  - Endpoint: /ws (SockJS fallback)
  - Broker prefix: /topic
  - App prefix: /app
  ```

- [ ] **Step 8.2: Tạo TableStatusBroadcaster**
  ```java
  @Service
  - SimpMessagingTemplate template;
  - broadcastTableUpdate(BilliardTable table, BigDecimal currentAmount)
    → Send to /topic/tables
  - broadcastAllTables()
    → Send toàn bộ trạng thái bàn
  ```
  Gọi broadcaster từ SessionService khi start/end session.

- [ ] **Step 8.3: Tạo DashboardController**
  ```java
  GET /dashboard → render dashboard.html
  ```

- [ ] **Step 8.4: Tạo dashboard.html (Thymeleaf + AlpineJS)**
  Layout:
  ```
  ┌─────────────────────────────────────────────┐
  │  🎱 QUẢN LÝ QUÁN BIDA    [Holiday: ON/OFF] │
  ├─────────────────────────────────────────────┤
  │  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐   │
  │  │Bàn 01│  │Bàn 02│  │Bàn 03│  │Bàn 04│   │
  │  │ Pool │  │ Pool │  │Carom │  │Carom │   │
  │  │🟢Trống│  │🔴45ph│  │🟢Trống│  │🔴1h20│   │
  │  │      │  │150k  │  │      │  │210k  │   │
  │  └──────┘  └──────┘  └──────┘  └──────┘   │
  │  ┌──────┐  ┌──────┐  ...                    │
  │  │Bàn 05│  │Bàn 06│                         │
  │  │ VIP  │  │ VIP  │                         │
  │  │🟢Trống│  │🟢Trống│                         │
  │  └──────┘  └──────┘                         │
  └─────────────────────────────────────────────┘
  ```

- [ ] **Step 8.5: JavaScript WebSocket client**
  ```javascript
  // dashboard.js
  - Connect SockJS → /ws
  - Subscribe /topic/tables
  - On message → update AlpineJS data → re-render grid
  - Mỗi 60s → fetch /api/tables (backup polling)
  ```

- [ ] **Step 8.6: Card actions (AlpineJS)**
  - Click bàn Trống → Popup confirm "Bắt đầu phiên?" → POST /api/tables/{id}/start
  - Click bàn Đang chơi → Popup "Kết thúc? Tổng: XXXk" → POST /api/tables/{id}/end
  - Cập nhật tiền tạm mỗi 30 giây (polling /api/tables/{id}/current hoặc WebSocket push).

- [ ] **Step 8.7: Verify** → Mở 2 browser, bắt đầu phiên ở browser 1, browser 2 tự cập nhật realtime.

**→ Code Agent: Commit "Task 8: Realtime dashboard with WebSocket STOMP"**

---

## Task 9: Integration Testing & Polish
**Ước lượng:** ~30 phút

### Steps:
- [ ] **Step 9.1: End-to-end flow test**
  ```
  1. Login admin → /dashboard
  2. Click bàn Pool 01 → Bắt đầu phiên
  3. Chờ 2-3 phút → kiểm tra tiền tạm tính cập nhật
  4. Kết thúc phiên → hiển thị tổng tiền
  5. Bàn chuyển về Trống
  6. Mở tab mới login staff → verify realtime sync
  ```

- [ ] **Step 9.2: Test phân quyền**
  ```
  - Staff truy cập /admin/tables → bị chặn
  - Staff thao tác dashboard → OK
  - Admin thao tác mọi thứ → OK
  ```

- [ ] **Step 9.3: Test Billing**
  ```
  - Tạo phiên chơi cross khung giờ → verify segments
  - Bật Holiday → verify giá holiday áp dụng
  - Bàn VIP vs Pool → verify giá khác nhau
  ```

- [ ] **Step 9.4: UI cleanup**
  - CSS cho dashboard (responsive grid, màu sắc rõ ràng)
  - Error handling (thông báo khi thao tác lỗi)
  - Loading states

**→ Code Agent: Commit "Task 9: Integration testing and UI polish"**

---

## Tóm tắt Phase 1

| Task | Mô tả | Ước lượng |
|------|--------|-----------|
| Task 1 | Project Setup & Config | 30 phút |
| Task 2 | Domain Entities & Enums | 45 phút |
| Task 3 | Repositories & Seed Data | 30 phút |
| Task 4 | Spring Security (2 role) | 30 phút |
| Task 5 | Billing Engine (Strategy) | 60 phút |
| Task 6 | Table Management & API | 45 phút |
| Task 7 | Price Rule Admin | 30 phút |
| Task 8 | WebSocket Dashboard | 60 phút |
| Task 9 | Integration & Polish | 30 phút |
| **TỔNG** | | **~6 giờ** |
