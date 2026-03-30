# 🎨 FEATURE REQUEST TEMPLATE - Thêm Chức Năng Mới

**Mục đích:** Mô tả chức năng mới để tôi biết file nào cần sửa.

---

## 📋 TEMPLATE - Copy & Fill

```
=== FEATURE REQUEST ===

[TÊN CHỨC NĂNG]
Ví dụ: "Thêm nút reset filter trên trang hóa đơn"

[MÔ TẢ]
Bạn muốn chức năng gì / thay đổi gì?

[TRANG / ENDPOINT]
Frontend: /admin/invoices / /dashboard?
Backend: /api/products / /admin/products?

[CHI TIẾT]
- Nơi đặt: Ở đâu trên giao diện? (top-left, button group, etc)
- Hành động: Click → làm gì?
- Input: Cần nhập gì không?
- Output: Kết quả là gì?

[UI REFERENCE]
Ảnh chụp hoặc mô tả cách hiển thị

[LIÊN QUAN FILE]
Guess file nào cần sửa (từ CODEBASE_NAVIGATION_MAP)
```

---

## 💡 EXAMPLES - Cách Mô Tả Hiệu Quả

### ❌ CHỨC NĂNG MỜ NHẠT (Tốn Credit)
```
"Thêm filter vào trang"
- Trang nào?
- Filter gì?
- Kết quả thế nào?
→ Tôi phải hỏi lại, tốn credit
```

### ✅ CHỨC NĂNG RÕ RÀNG (Tiết Kiệm Credit)
```
=== FEATURE REQUEST ===

[TÊN CHỨC NĂNG]
Thêm nút "Xóa tất cả filters" trên trang admin/invoices

[MÔ TẢ]
Khi user filter hóa đơn theo ngày, có nút để reset về không filter

[TRANG / ENDPOINT]
Frontend: /admin/invoices
Backend: Không cần (redirect page)

[CHI TIẾT]
- Nơi đặt: Ngay cạnh input "from" date
- Hành động: Click → redirect /admin/invoices (clear query params)
- Input: Không
- Output: Hiện tất cả hóa đơn (không filter)

[UI REFERENCE]
[from input] [to input] [Search] [Reset button] ← Thêm "Reset" button

[LIÊN QUAN FILE]
admin/invoices.html, AdminInvoiceController
```

→ **Ngay lập tức tôi biết:** Thêm button vào HTML + thêm link clear params!

---

## 🎯 LOẠI FEATURE THƯỜNG XẢY RA

### 1️⃣ Thêm/Thay Đổi Giao Diện (Frontend)

**Template:**
```
=== FRONTEND FEATURE ===

[Trang cần sửa]: /admin/products / /dashboard?
[Thay đổi]: Thêm nút / Input / Table column?
[Nơi đặt]: Top-left / Bottom-right / Modal?
[Hành động]: Click → gọi API nào?

→ Files: template HTML + Controller (nếu cần backend)
```

**Example:**
```
[Trang]: admin/products
[Thay đổi]: Thêm "Category filter dropdown" ở top
[Nơi đặt]: Bên cạnh nút "Thêm sản phẩm mới"
[Hành động]: Filter sản phẩm theo category (DRINK, FOOD, etc)

→ Sửa: admin/products.html + AdminProductController
```

---

### 2️⃣ Thêm API Endpoint (Backend)

**Template:**
```
=== API FEATURE ===

[Endpoint]: POST /api/products / GET /api/products?
[Request]: Body gồm gì? Query params?
[Response]: Trả về JSON gì?
[Database]: Query cái gì?

→ Files: [X]ApiController + [X]Service + [X]Repository
```

**Example:**
```
[Endpoint]: GET /api/products/by-stock?minStock=5
[Request]: Query param: minStock (int)
[Response]: JSON array of products có stock >= minStock
[Database]: ProductRepository.findByStockQuantityGreaterThanEqual()

→ Sửa: ProductApiController + ProductService + ProductRepository
```

---

### 3️⃣ Thay Đổi Business Logic (Service)

**Template:**
```
=== LOGIC FEATURE ===

[Quy tắc cũ]: Cách hiện tại hoạt động?
[Quy tắc mới]: Thay đổi thành như thế nào?
[Liên quan]: Ảnh hưởng các file nào?

→ Files: Service + Repository (+ Controller nếu cần API change)
```

**Example:**
```
[Quy tắc cũ]: Tính tiền = sum(segment amounts)
[Quy tắc mới]: Tính tiền = sum(segment amounts) - membership discount
[Liên quan]: InvoiceService, Customer entity

→ Sửa: InvoiceService.createInvoice() + logic tính discount
```

---

### 4️⃣ Thêm Database Field / Entity

**Template:**
```
=== DATABASE FEATURE ===

[Entity]: Sửa Product / Invoice / Customer?
[Field mới]: Tên field + type?
[Ý nghĩa]: Dùng để làm gì?

→ Files: Entity + Migration (nếu có) + Service + Repository + Controller
```

**Example:**
```
[Entity]: Product
[Field mới]: discountPercentage (BigDecimal)
[Ý nghĩa]: Giảm giá cho sản phẩm nào (VD: Bia 10%, Snack 5%)

→ Sửa: Product entity + Repository query + Service logic + Admin form
```

---

## 📝 REAL EXAMPLES - Copy Paste Format

### EXAMPLE 1: Thêm Nút Reset Filter
```
=== FEATURE REQUEST ===

[TÊN CHỨC NĂNG]
Thêm nút "Xóa filters" trên /admin/invoices

[MÔ TẢ]
Giúp user nhanh chóng xóa tất cả filter dates

[TRANG / ENDPOINT]
Frontend: /admin/invoices

[CHI TIẾT]
- Nơi đặt: Ngay cạnh button "Search"
- Hành động: Click → Redirect /admin/invoices (clear params)
- Input: Không
- Output: Hiển thị lại tất cả hóa đơn

[UI REFERENCE]
[From] [To] [🔍 Search] [✕ Reset]

[LIÊN QUAN FILE]
admin/invoices.html
```

→ **Tôi biết:** Chỉ cần thêm link button HTML, không cần backend change!

---

### EXAMPLE 2: Thêm API Filter
```
=== FEATURE REQUEST ===

[TÊN CHỨC NĂNG]
GET /api/products?category=DRINK (lọc sản phẩm theo category)

[MÔ TẢ]
Frontend dropdown cần gọi API để lấy sản phẩm theo category

[TRANG / ENDPOINT]
Backend: GET /api/products?category=DRINK

[CHI TIẾT]
- Input: Query param "category" (DRINK, FOOD, SNACK, OTHER)
- Output: JSON array các sản phẩm thuộc category đó
- Error: 400 nếu category invalid, 200 với empty array nếu không có

[LIÊN QUAN FILE]
ProductApiController, ProductService, ProductRepository
```

→ **Tôi biết:** Thêm @GetMapping + param trong ProductApiController, filter trong Repository!

---

### EXAMPLE 3: Thay Đổi Tính Tiền
```
=== FEATURE REQUEST ===

[TÊN CHỨC NĂNG]
Áp dụng membership discount khi tính hóa đơn

[MÔ TẢ]
Khách hàng GOLD được giảm 5%, SILVER 3%, BRONZE 0%

[TRANG / ENDPOINT]
Backend logic: InvoiceService

[CHI TIẾT]
- Cũ: totalAmount = tableCharge + serviceCharge
- Mới: totalAmount = (tableCharge + serviceCharge) - discount

[LIÊN QUAN FILE]
InvoiceService, Customer entity (membershipTier), Invoice entity (add discount field)
```

→ **Tôi biết:** Sửa createInvoice() logic + thêm discount field vào Invoice!

---

### EXAMPLE 4: Thêm Admin Form
```
=== FEATURE REQUEST ===

[TÊN CHỨC NĂNG]
Trang admin quản lý khách hàng VIP

[MÔ TẢ]
Có form tạo/sửa/xóa khách hàng VIP, gán membership tier

[TRANG / ENDPOINT]
Frontend: /admin/customers (hoặc /admin/customers/vip)
Backend: AdminCustomerController

[CHI TIẾT]
- List: Hiển thị tất cả customers, filter theo tier
- Create: Form thêm customer (name, phone, email, tier)
- Edit: Form sửa customer
- Delete: Xóa customer (confirm modal)

[LIÊN QUAN FILE]
AdminCustomerController, CustomerService, admin/customers.html template
```

→ **Tôi biết:** Sửa AdminCustomerController + tạo/sửa admin/customers.html!

---

## ✅ CHECKLIST - Trước Khi Send Message

- [ ] Nêu rõ tên chức năng
- [ ] Mô tả ý tưởng (cái gì sẽ thay đổi)
- [ ] Nói trang/endpoint cụ thể
- [ ] Mô tả chi tiết hành động
- [ ] Guess file liên quan (từ CODEBASE_NAVIGATION_MAP)
- [ ] **KHÔNG**: Chỉ nói "thêm feature" không rõ

---

## 🚀 TÓTAT

Khi mô tả feature theo format này:
- ✅ Tôi biết **file nào** cần sửa
- ✅ Tôi **không cần** hỏi lại
- ✅ **Tiết kiệm 70% credit**
- ✅ Implement nhanh gấp 2 lần

**Combo:**
1. CODEBASE_NAVIGATION_MAP.md (biết file ở đâu)
2. ERROR_DIAGNOSIS_TEMPLATE.md (mô tả lỗi)
3. FEATURE_REQUEST_TEMPLATE.md (request feature)

= **Tiết kiệm 80% credit!** 🎉
