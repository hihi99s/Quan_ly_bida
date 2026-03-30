# 🗺️ CODEBASE NAVIGATION MAP - Tiết kiệm Credit

**Mục đích:** Giúp bạn mô tả lỗi/yêu cầu mà không cần scan toàn bộ codebase.

---

## 📋 BACKEND STRUCTURE (Spring Boot)

### 🔧 Core Billing Engine
```
src/main/java/com/bida/billing/
├── service/BillingCalculator.java          ← Tính tiền chính
├── strategy/
│   ├── PricingStrategy.java                ← Interface
│   └── TimeSlotPricingStrategy.java        ← Implement khung giờ
├── dto/
│   ├── BillingResult.java                  ← Kết quả tính tiền
│   └── BillingSegment.java                 ← Chi tiết từng segment
└── service/SegmentSplitter.java            ← Chia nhỏ theo khung giờ
```

**Khi lỗi billing:** Mô tả "lỗi tính tiền cho POOL vào 9 sáng thứ 2" → Tôi biết mở TimeSlotPricingStrategy

---

### 📊 Quản lý Phiên Chơi (Session)
```
src/main/java/com/bida/service/
├── SessionService.java                     ← Start/End/Pause/Resume phiên
├── BilliardTableService.java               ← Quản lý bàn
└── TableService.java                       ← Get all tables

src/main/java/com/bida/repository/
├── SessionRepository.java                  ← Query phiên
├── SessionSegmentRepository.java           ← Chi tiết segment
└── BilliardTableRepository.java            ← Query bàn
```

**Khi lỗi session:** "Không thể bắt đầu phiên" → Mở SessionService.startSession()

---

### 🛒 Quản lý Sản Phẩm (F&B)
```
src/main/java/com/bida/service/
├── ProductService.java                     ← CRUD sản phẩm, trừ kho

src/main/java/com/bida/repository/
├── ProductRepository.java                  ← Query sản phẩm

src/main/java/com/bida/entity/
└── Product.java                            ← Entity sản phẩm
```

**Khi lỗi product:** "Dropdown không load sản phẩm" → ProductApiController + ProductService

---

### 💰 Hóa Đơn (Invoice)
```
src/main/java/com/bida/service/
└── InvoiceService.java                     ← Tạo hóa đơn, query

src/main/java/com/bida/repository/
└── InvoiceRepository.java                  ← Query hóa đơn

src/main/java/com/bida/entity/
└── Invoice.java                            ← Entity hóa đơn
```

**Khi lỗi invoice:** "Lọc hóa đơn theo ngày fail" → AdminInvoiceController

---

### 🔌 API REST
```
src/main/java/com/bida/controller/api/
├── TableApiController.java                 ← /api/tables (start/end/pause)
├── ProductApiController.java               ← /api/products (NEW)
├── CustomerApiController.java              ← /api/customers
└── ReservationApiController.java           ← /api/reservations
```

**Khi frontend cần API:** "GET danh sách sản phẩm" → ProductApiController

---

### 🖥️ Admin Controllers
```
src/main/java/com/bida/controller/admin/
├── AdminProductController.java             ← /admin/products (CRUD form)
├── AdminInvoiceController.java             ← /admin/invoices (query + filter)
├── AdminPriceController.java               ← /admin/prices (quản lý giá)
├── AdminTableController.java               ← /admin/tables
└── AdminStaffController.java               ← /admin/staff
```

**Khi lỗi admin form:** "Không thể tạo sản phẩm" → AdminProductController

---

### ⚙️ Cấu Hình & WebSocket
```
src/main/java/com/bida/config/
├── SecurityConfig.java                     ← Spring Security, CSRF
├── WebSocketConfig.java                    ← WebSocket setup
├── DataSeeder.java                         ← Seed dữ liệu (27 PriceRules)
└── JpaAuditingConfig.java                  ← Audit created/updated time

src/main/java/com/bida/websocket/
└── TableStatusBroadcaster.java             ← Broadcast trạng thái bàn
```

**Khi frontend không update:** "Timer không update" → TableStatusBroadcaster (@Scheduled)

---

### 📦 Repositories (Database Queries)
```
src/main/java/com/bida/repository/
├── SessionRepository.java
├── PriceRuleRepository.java                ← Query bảng giá
│   └── findByTableTypeAndDayTypeOrderByStartTimeAsc()
├── ProductRepository.java
├── InvoiceRepository.java
├── BilliardTableRepository.java
└── CustomerRepository.java
```

**Khi query lỗi:** "Không tìm thấy giá cho POOL-WEEKDAY" → PriceRuleRepository

---

### 🏗️ Entities (Database Schema)
```
src/main/java/com/bida/entity/
├── Session.java                            ← Phiên chơi
├── BilliardTable.java                      ← Bàn bida
├── SessionSegment.java                     ← Chi tiết tính giá
├── Product.java                            ← Sản phẩm F&B
├── OrderItem.java                          ← Đơn hàng
├── Invoice.java                            ← Hóa đơn
├── PriceRule.java                          ← Bảng giá
└── Customer.java                           ← Khách hàng
```

---

## 🎨 FRONTEND STRUCTURE (Thymeleaf Templates)

### Dashboard (Chính)
```
src/main/resources/templates/
├── dashboard.html                          ← Giao diện chính (quản lý bàn)
│   └── Hiển thị: timer, tiền tạm, order count, buttons
└── DashboardController.java                ← Xử lý
```

**Khi lỗi giao diện chính:** "Timer không hiển thị" → dashboard.html

---

### Admin Pages
```
src/main/resources/templates/admin/
├── products.html                           ← Quản lý sản phẩm
│   └── AdminProductController.java
├── invoices.html                           ← Danh sách hóa đơn
│   └── AdminInvoiceController.java
├── invoice-detail.html                     ← Chi tiết hóa đơn
├── prices.html                             ← Quản lý bảng giá
│   └── AdminPriceController.java
├── tables.html                             ← Quản lý bàn
├── customers.html                          ← Quản lý khách hàng
├── holidays.html                           ← Quản lý ngày lễ
└── staff.html                              ← Quản lý nhân viên
```

**Khi lỗi admin page:** "Admin không thể tạo sản phẩm" → admin/products.html

---

## 🔍 KEY PATTERNS TO KNOW

### Pattern 1: Billing
```
User kết thúc phiên → SessionService.endSession()
  → BillingCalculator.calculate(session)
    → TimeSlotPricingStrategy.calculate()
      → PriceRuleRepository.findByTableTypeAndDayType()
      → SegmentSplitter.split()
  → InvoiceService.createInvoice()
```

### Pattern 2: Product Order
```
Staff gọi món → OrderService.createOrderItem()
  → ProductService.reduceStock()
  → OrderItem saved
  → Dashboard updated (WebSocket broadcast)
```

### Pattern 3: Realtime Updates
```
TableStatusBroadcaster (@Scheduled every 5s)
  → SessionService.getAllTableStatuses()
  → messagingTemplate.convertAndSend("/topic/tables", statuses)
  → Frontend subscribes /topic/tables
```

### Pattern 4: Admin Forms
```
Form POST → AdminXxxController
  → Service.doSomething()
  → Redirect or show error
  → SecurityConfig: CSRF tokens required
```

---

## ⚡ QUICK LOCATION FINDER

| Nếu lỗi liên quan tới... | Tìm file... | Pattern |
|---|---|---|
| Tính tiền | BillingCalculator, TimeSlotPricingStrategy | `/billing/**` |
| Bảng giá | PriceRuleRepository, AdminPriceController | `PriceRule*`, `AdminPrice*` |
| Phiên chơi | SessionService, SessionRepository | `Session*` |
| Sản phẩm | ProductService, AdminProductController, ProductApiController | `Product*` |
| Hóa đơn | InvoiceService, AdminInvoiceController | `Invoice*` |
| Dashboard realtime | TableStatusBroadcaster, WebSocketConfig | `Broadcast*`, `WebSocket*` |
| Admin forms | Admin[X]Controller | `/admin/**` |
| API | [X]ApiController | `/api/**` |
| Database | [X]Repository | `/repository/**` |
| Security | SecurityConfig, CSRF tokens | `Security*` |

---

## 📝 DEPENDENCIES YOU SHOULD KNOW

```xml
<!-- Spring Boot -->
<spring-boot-starter-web>           ← MVC, REST
<spring-boot-starter-data-jpa>      ← Database
<spring-boot-starter-security>      ← Auth, CSRF
<spring-boot-starter-websocket>     ← WebSocket

<!-- Thymeleaf -->
<spring-boot-starter-thymeleaf>     ← Template engine

<!-- Lombok -->
<lombok>                            ← @Slf4j, @RequiredArgsConstructor
```

---

## 🚀 DEPLOYMENT NOTES

```
Database: H2 (auto-seed via DataSeeder)
Seeding: 27 PriceRules, products, users tự động chạy khi start
WebSocket: /ws endpoint, enable @EnableScheduling
CSRF: Enabled on /admin/**, tokens in Thymeleaf forms
```

---

## 💡 TIPS TO SAVE CREDITS

1. **Mô tả lỗi chi tiết** thay vì "không hoạt động"
   - ❌ "Lỗi ở chỗ nào"
   - ✅ "Khi kết thúc phiên POOL lúc 9 sáng, hiển thị lỗi: Không tìm thấy bảng giá"
   - → Tôi tìm ngay PriceRuleRepository + TimeSlotPricingStrategy

2. **Nói trang cụ thể** cho frontend changes
   - ❌ "Sửa giao diện"
   - ✅ "Thêm nút reset filter trên trang admin/invoices"
   - → Tôi biết mở admin/invoices.html + AdminInvoiceController

3. **Dùng file references** từ bản đồ này
   - ❌ "Sản phẩm không load"
   - ✅ "GET /api/products không trả về gì"
   - → Tôi mở ProductApiController + ProductService

4. **Tham khảo QUICK_LOCATION_FINDER**
   - Nếu lỗi liên quan "tính tiền" → Nói thế là đủ

---

**Next Step:** Dùng ERROR_DIAGNOSIS_TEMPLATE.md khi gặp lỗi
