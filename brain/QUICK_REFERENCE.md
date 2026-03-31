# ⚡ QUICK REFERENCE - Tra Cứu Nhanh

**Dùng để:** Tra cứu nhanh file/class/pattern mà không cần đọc toàn bộ bản đồ.

---

## 🎉 SESSION UPDATE: 2026-04-01

### ✅ Implemented Today
| Feature/Fix | Files | Status |
|---|---|---|
| **Discount Code System (Phase 2)** | DiscountCode, DiscountCodeService, AdminDiscountCodeController, admin/discount-codes.html | ✅ Complete |
| Dashboard text visibility fix | dashboard.html (CSS) | ✅ Complete |
| Admin form CSRF token fix | admin/tables.html | ✅ Complete |
| Transaction rollback error fix | SessionService.java (early validation) | ✅ Complete |
| Navbar "Mã giảm giá" links | 10 template files | ✅ Complete |
| Delete discount code guard | InvoiceRepository, DiscountCodeService | ✅ Complete |

### 💰 Discount Code Features
```
WHAT:  Percentage-based discount codes with usage tracking
WHERE: /admin/discount-codes (CRUD)
       Dashboard end session modal (apply code)
       /admin/invoices (show applied codes)

RULES: - Code validation happens EARLY in SessionService.endSession()
       - Prevents transaction rollback leak
       - Can't delete code if used in any invoice
       - Stacks with membership discount (additive)
       - Usage count increments atomically with invoice creation

MESSAGES (Vietnamese):
  "Mã giảm giá không tồn tại: <code>"
  "Mã giảm giá không còn hoạt động"     ← inactive code
  "Mã giảm giá đã hết hạn"              ← expired
  "Mã giảm giá đã hết lượt sử dụng"     ← usage limit exceeded
  "Không thể xóa mã giảm giá đã được sử dụng trong hóa đơn..."
```

### 🐛 Fixes Applied
```
1. CSS Dark Background Bug
   Problem: text-muted was invisible on #0f1923 background
   Fix:     Override --bs-secondary-color in body CSS
   
2. Admin Form 403 Forbidden
   Problem: Edit form missing CSRF token (no th:action)
   Fix:     Added hidden CSRF input field
   
3. Transaction Rollback Leak
   Problem: Discount code validation inside @Transactional marked TX as rollback-only
   Fix:     Validate code BEFORE any DB writes in SessionService.endSession()
   
4. Used Code Deletion
   Problem: Deleting used code showed SQL FK error to user
   Fix:     Check invoiceRepository.existsByDiscountCodeId() before delete
```

---

## 🔍 SEARCH BY SYMPTOM

### 💰 "Lỗi Tính Tiền"
```
Triệu chứng: totalAmount = 0, NPE, không tìm bảng giá
Files:
  → BillingCalculator.calculate()
  → TimeSlotPricingStrategy.calculate()
  → PriceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc()
  → DataSeeder.seedPriceRules() (check seed data)

Kiểm tra:
  1. PriceRule có cho loại bàn đó + loại ngày không?
  2. Khung giờ có cover 8-12, 12-17, 17-23 không?
  3. TableType enum có khớp (POOL, CAROM, VIP)?
  4. DayType enum có khớp (WEEKDAY, WEEKEND, HOLIDAY)?
```

### 🔒 "403 Forbidden trên POST /admin/**"
```
Triệu chứng: Không thể create/edit/delete ở admin panel
Files:
  → SecurityConfig.java (check CSRF config)
  → admin/[page].html (check CSRF token in form)
  → [X]AdminController.java (POST endpoint)

Kiểm tra:
  1. HTML form có input hidden CSRF token không?
  2. Form method="post" không?
  3. SecurityConfig có disable CSRF sai không?
  4. Browser cookies có JSESSIONID không?
```

### 🛒 "Dropdown sản phẩm trống"
```
Triệu chứng: GET /api/products trả về [] hoặc null
Files:
  → ProductApiController.getAllProducts()
  → ProductService.getActiveProducts()
  → ProductRepository.findAll()
  → DataSeeder.seedProducts()

Kiểm tra:
  1. Database có product nào active=true không?
  2. GET /api/products trả về HTTP 200 không?
  3. Response JSON format đúng không?
  4. Frontend subscribe /topic/tables đúng không? (nếu WebSocket)
```

### 📋 "Lọc hóa đơn crash"
```
Triệu chứng: DateTimeParseException, invalid date format
Files:
  → AdminInvoiceController.listInvoices()
  → InvoiceService.getInvoicesByDateRange()
  → InvoiceRepository

Kiểm tra:
  1. Nhập date format YYYY-MM-DD không?
  2. From date > to date không?
  3. Controller có catch DateTimeParseException không?
  4. Error message hiển thị cho user không?
```

### 📡 "Timer không update"
```
Triệu chứng: Dashboard playingMinutes đóng băng, currentAmount không thay đổi
Files:
  → TableStatusBroadcaster.broadcastTableStatusPeriodically() @Scheduled
  → BidaApplication.java @EnableScheduling
  → WebSocketConfig.java
  → SessionService.getAllTableStatuses()

Kiểm tra:
  1. @EnableScheduling có trong main class không?
  2. @Scheduled(fixedDelay=5000) đúng không?
  3. /topic/tables broadcast đang chạy không? (check logs)
  4. Frontend subscribe /topic/tables không?
  5. WebSocket connection active không? (check DevTools)
```

### 🎟️ "Mã giảm giá bị reject"
```
Triệu chứng: Nhập mã, submit nhưng báo lỗi hoặc bị xóa không được
Files:
  → DiscountCodeService.findAndValidate()
  → SessionService.endSession() (early validation)
  → InvoiceRepository.existsByDiscountCodeId()
  → admin/discount-codes.html

Kiểm tra:
  1. Mã có tồn tại (active=true) không?
  2. Mã có hết hạn không? (expiryDate < today)
  3. Mã có đã hết lượt sử dụng không? (usageCount >= maxUsageCount)
  4. Nếu xóa: mã có được dùng trong invoice không?
  5. Message có hiện tiếng Việt không? (không lộ SQL/FK error)
```

---

## 🗂️ SEARCH BY FEATURE

### 💳 Billing / Pricing
```
Tính tiền:              BillingCalculator, TimeSlotPricingStrategy
Bảng giá:              PriceRuleRepository, AdminPriceController
Invoice:               InvoiceService, AdminInvoiceController
Segment detail:        SessionSegment, SessionSegmentRepository
```

### 🎮 Session Management
```
Start/End/Pause:       SessionService
Query session:         SessionRepository
Table status:          BilliardTableService, TableStatusDTO
Current amount:        BillingCalculator.calculateCurrentAmount()
```

### 🛒 Products & Orders
```
CRUD Product:          ProductService, AdminProductController
Product API:           ProductApiController
Create order:          OrderService
Stock management:      ProductService.reduceStock()
List products:         ProductRepository.findByActiveTrue()
```

### 👤 Customers
```
Customer info:         CustomerService, CustomerRepository
Membership tier:       Customer entity, MembershipTier enum
Customer API:          CustomerApiController
Spending tracking:     Customer.addSpending()
```

### 🎟️ Discount Codes (Phase 2)
```
CRUD:                  DiscountCodeService, AdminDiscountCodeController
Validation:            DiscountCodeService.findAndValidate()
Usage tracking:        DiscountCodeService.incrementUsage()
Apply in invoice:      InvoiceService.createInvoice()
Check usage:           InvoiceRepository.existsByDiscountCodeId()
Admin UI:              /admin/discount-codes
Apply in dashboard:    dashboard.html (end session modal)
```

### 🏪 Admin Pages
```
Products:         /admin/products → AdminProductController
Prices:           /admin/prices → AdminPriceController
Invoices:         /admin/invoices → AdminInvoiceController
Customers:        /admin/customers → AdminCustomerController
Discount Codes:   /admin/discount-codes → AdminDiscountCodeController (Phase 2)
Tables:           /admin/tables → AdminTableController
Staff:            /admin/staff → AdminStaffController
Holidays:         /admin/holidays → AdminHolidayController
```

### 🌐 APIs
```
Tables:        /api/tables → TableApiController
Products:      /api/products → ProductApiController
Customers:     /api/customers → CustomerApiController
Reservations:  /api/reservations → ReservationApiController
```

---

## 🔧 SEARCH BY TECHNOLOGY

### 🗄️ Database Layer
```
JPA Repositories:      src/main/java/com/bida/repository/
Entities:              src/main/java/com/bida/entity/
H2 Database:           auto-seed via DataSeeder.java
Key queries:           [X]Repository.java
```

### 🎮 Service Layer
```
Business logic:        src/main/java/com/bida/service/
Billing logic:         src/main/java/com/bida/billing/service/
Pricing strategy:      src/main/java/com/bida/billing/strategy/
```

### 🕸️ Web Layer
```
REST APIs:             src/main/java/com/bida/controller/api/
Admin pages:           src/main/java/com/bida/controller/admin/
Dashboard:             src/main/java/com/bida/controller/DashboardController.java
```

### 🎨 Frontend
```
Templates:             src/main/resources/templates/
Admin templates:       src/main/resources/templates/admin/
Static assets:         src/main/resources/static/
Thymeleaf engine:      auto-configured
```

### 🔐 Security & Config
```
Spring Security:       SecurityConfig.java
CSRF protection:       Enabled on /admin/**, disabled on /api/** + /ws/**
WebSocket:             WebSocketConfig.java
Scheduling:            @EnableScheduling in BidaApplication.java
JPA Auditing:          JpaAuditingConfig.java
```

### 📡 Real-time
```
WebSocket endpoint:    /ws
Topic:                 /topic/tables
Broadcaster:           TableStatusBroadcaster.java
Scheduler:             @Scheduled(fixedDelay=5000)
Message template:      SimpMessagingTemplate
```

---

## 📊 COMMON PATTERNS

### Pattern: Controller → Service → Repository
```
AdminProductController.java
  ↓ calls
ProductService.java
  ↓ calls
ProductRepository.java
  ↓ queries
Product entity
```

### Pattern: Entity → DTO → JSON
```
Invoice entity
  ↓ mapped to
BillingResult DTO
  ↓ serialized to
JSON response
```

### Pattern: Form POST → Controller → Service
```
admin/products.html [form]
  ↓ POST
AdminProductController.createProduct()
  ↓ calls
ProductService.createProduct()
  ↓ saves
Product entity
```

### Pattern: Real-time WebSocket
```
TableStatusBroadcaster @Scheduled(5s)
  ↓ calls
SessionService.getAllTableStatuses()
  ↓ sends via
messagingTemplate.convertAndSend("/topic/tables", ...)
  ↓ frontend listens
/topic/tables
```

---

## 🎯 QUICK LOCATION GUIDE

| Tìm... | Ở file... | Path |
|---|---|---|
| Tính tiền | TimeSlotPricingStrategy | `/billing/strategy/` |
| Bảng giá | PriceRuleRepository | `/repository/` |
| Phiên chơi | SessionService | `/service/` |
| Sản phẩm | ProductService | `/service/` |
| Hóa đơn | InvoiceService | `/service/` |
| API | [X]ApiController | `/controller/api/` |
| Admin form | Admin[X]Controller | `/controller/admin/` |
| Entity | [X].java | `/entity/` |
| Repository | [X]Repository | `/repository/` |
| Template | [X].html | `/templates/` |
| Security | SecurityConfig | `/config/` |
| WebSocket | TableStatusBroadcaster | `/websocket/` |

---

## 🔴 TOP 5 FILES TO REMEMBER

1. **BillingCalculator.java** (Tính tiền chính)
2. **SessionService.java** (Quản lý phiên)
3. **ProductApiController.java** (Sản phẩm API)
4. **AdminInvoiceController.java** (Hóa đơn)
5. **TableStatusBroadcaster.java** (Realtime update)

---

## 🚀 INSTANT LOOKUP

### Lỗi ở đâu?
```
❌ "Không tính được tiền"
   → BillingCalculator, TimeSlotPricingStrategy, PriceRuleRepository

❌ "Admin form 403"
   → SecurityConfig, admin/products.html, CSRF token

❌ "Dropdown trống"
   → ProductApiController, ProductService, ProductRepository

❌ "Lọc hóa đơn crash"
   → AdminInvoiceController (DateTimeParseException)

❌ "Timer đóng băng"
   → TableStatusBroadcaster, @EnableScheduling, WebSocket
```

### File nào cần sửa?
```
Tính tiền               → BillingCalculator.java
Giá bàn                 → PriceRuleRepository.java
Phiên chơi              → SessionService.java
Sản phẩm                → ProductService.java
Giao diện               → admin/[page].html
API                     → [X]ApiController.java
Realtime                → TableStatusBroadcaster.java
Security                → SecurityConfig.java
Database                → [X].java entity
```

---

## 💡 TIPS

1. **Mô tả lỗi + Reference file từ đây** → Tôi fix ngay, không cần scan
2. **Nói trang cụ thể** → Tôi biết Template nào
3. **Mention loại bàn/ngày** (billing) → Tôi biết cấu hình nào
4. **Paste error logs** (nếu có) → Tôi biết line nào fail

---

**Next:** Dùng templates khi có lỗi hoặc feature request!
