# 📍 NEW CHAT SESSION - Cách Giữ Context Không Tốn Credit

**Problem:** Khi bạn mở chat mới, tôi không có context về dự án
**Solution:** Bạn provide context via templates + guides, không cần tôi scan

---

## 🚀 WHEN YOU START NEW CHAT

### **Option A: Simple Issue (Recommended)**

**Bạn làm:**
```
1. Copy: ERROR_DIAGNOSIS_TEMPLATE.md
2. Fill: 5 parts (đầy đủ context)
3. Send: First message ngay

Ví dụ:
"=== ERROR DIAGNOSIS ===

[LỖI TIÊU ĐỀ]
End session POOL 8-12 WEEKDAY → NPE ở TimeSlotPricingStrategy

[BƯỚC TÁI HIỆN]
1. Click Pool 01
2. Start session
3. Wait 1 minute
4. Click End session → NullPointerException

[THÔNG TIN]
- Project: Billiard Management System (Spring Boot backend)
- Loại bàn: POOL
- Loại ngày: WEEKDAY
- Khung giờ: 8-12

[LỖI MESSAGE]
java.lang.NullPointerException
  at com.bida.billing.strategy.TimeSlotPricingStrategy.calculate()
  at com.bida.billing.service.BillingCalculator.calculate()

[LIÊN QUAN FILE]
BillingCalculator.java
TimeSlotPricingStrategy.java
PriceRuleRepository.java
SessionService.java"
```

**I làm:**
```
✅ Read template → biết exact context
✅ Open 3-4 files mentioned
✅ Trace logic
✅ Find root cause
✅ Fix

Total tokens: ~5,000 (no scanning needed) ✅
```

---

### **Option B: Complex Issue**

**Bạn làm:**
```
1. Add context at top of chat:

"=== PROJECT CONTEXT ===

Project: Billiard Management System (Phase 2)
Tech Stack: Spring Boot + H2 Database + Thymeleaf + WebSocket
Recent Changes: Fixed 5 critical errors (see /brain/EXECUTIVE_SUMMARY.txt)

Key Components:
- Billing Engine: BillingCalculator, TimeSlotPricingStrategy
- Session Management: SessionService, Session entity
- Products: ProductService, ProductApiController
- Admin: AdminProductController, AdminInvoiceController
- Realtime: TableStatusBroadcaster, WebSocket

File Locations Reference:
- /code/backend/src/main/java/com/bida/
  - /service/ (SessionService, ProductService, InvoiceService)
  - /controller/api/ (ProductApiController, TableApiController)
  - /controller/admin/ (AdminProductController, AdminInvoiceController)
  - /billing/ (BillingCalculator, TimeSlotPricingStrategy)
  - /repository/ (ProductRepository, PriceRuleRepository, SessionRepository)
- /code/backend/src/main/resources/templates/
  - /admin/ (products.html, invoices.html, prices.html)

Recent Fixes:
1. Billing: IllegalStateException when PriceRule missing
2. Security: CSRF tokens added to admin forms
3. API: ProductApiController created (GET /api/products)
4. Invoice: Input validation + error handling
5. Realtime: @Scheduled broadcast every 5s

Next Session Issue:
[PASTE YOUR ISSUE HERE]"

2. Then copy: ERROR_DIAGNOSIS_TEMPLATE
3. Fill: 5 parts
4. Send
```

**I làm:**
```
✅ Read context block → understand project layout
✅ Know recent fixes → avoid re-fixing
✅ Know file locations → open exact files
✅ Read issue template → understand exact problem
✅ Fix immediately

Total tokens: ~6-7,000 (context provided, minimal scanning) ✅
```

---

### **Option C: Multiple Issues in Same Session**

**Bạn làm:**
```
First message (project context):

"=== SESSION START ===

Project: Billiard Management System (Phase 2 - completed)
Recent work: Fixed 5 critical errors
Reference docs: /brain/EXECUTIVE_SUMMARY.txt, /brain/CODEBASE_NAVIGATION_MAP.md

I'll report issues using templates below."

Then message 2, 3, etc:

"=== ISSUE #1 ===
[ERROR_DIAGNOSIS_TEMPLATE filled]"

"=== ISSUE #2 ===
[ERROR_DIAGNOSIS_TEMPLATE filled]"

"=== FEATURE #3 ===
[FEATURE_REQUEST_TEMPLATE filled]"
```

**I làm:**
```
✅ Read first message → project context set
✅ For each issue → use context from first message
✅ No re-scanning needed across messages
✅ Multiple issues resolved efficiently

Total tokens per issue: ~4-5,000 (context reused) ✅
```

---

## 📋 TEMPLATE FOR FIRST MESSAGE (Copy-Paste This)

```
=== NEW SESSION - PROJECT CONTEXT ===

Project Name: Billiard Management System (Phase 2)
Repository: /d/do_an_J2EE
Tech: Spring Boot + H2 + Thymeleaf + WebSocket

IMPORTANT CONTEXT:
(Copy relevant sections from /brain/ guides)

Main Services:
- SessionService → Start/End/Pause sessions
- BillingCalculator → Calculate billing
- ProductService → Manage products
- InvoiceService → Create/query invoices

Main Controllers:
- /controller/api/ProductApiController
- /controller/admin/AdminProductController
- /controller/admin/AdminInvoiceController

Key Database:
- Session (phiên chơi)
- BilliardTable
- Product (F&B)
- Invoice
- PriceRule

Recent Fixes (Don't re-fix):
1. Billing → IllegalStateException when PriceRule missing
2. Security → CSRF tokens on admin forms
3. API → GET /api/products endpoint created
4. Invoice → Input validation added
5. Realtime → @Scheduled broadcast every 5s

Reference Guides:
- CODEBASE_NAVIGATION_MAP.md (file locations)
- QUICK_REFERENCE.md (quick lookup)
- ERROR_DIAGNOSIS_TEMPLATE.md (error format)
- FEATURE_REQUEST_TEMPLATE.md (feature format)

===

Now let me describe my issue:

[PASTE TEMPLATE HERE - ERROR or FEATURE]
```

---

## 🎯 BEST PRACTICES FOR NEW SESSION

### **✅ DO THIS:**

```
1. First message: Provide project context
   - Project name
   - Tech stack
   - Key components
   - Recent fixes (if any)
   - Reference guides

2. Second message: Use template for issue/feature
   - ERROR_DIAGNOSIS_TEMPLATE or
   - FEATURE_REQUEST_TEMPLATE
   - Fill 5 parts completely

3. Ongoing: Reference context from first message
   - "As mentioned, the billing engine..."
   - "Using the file locations from earlier..."
```

### **❌ DON'T DO THIS:**

```
❌ "Gặp lỗi ở tính tiền"
   → Tôi phải scan codebase → tốn 30-50K tokens

❌ "Thêm feature nhưng không nói file nào"
   → Tôi phải explore → tốn 20-40K tokens

❌ Random description without template
   → Tôi phải clarify + scan → tốn 50K+ tokens

❌ Open chat, immediately ask without context
   → Tôi phải learn project from scratch → tốn huge tokens
```

---

## 📊 TOKEN COMPARISON: New Chat

| Approach | Tokens | Speed |
|----------|--------|-------|
| **Random description** | 50,000 | Slow (questions) |
| **With context + template** | 5,000 | Fast (immediate) |
| **Multiple issues + shared context** | 20,000 total | Very fast (reuse context) |

---

## 🔄 WORKFLOW FOR NEXT TIME YOU START CHAT

### **Step 1: Prepare Context (2 minutes)**
```
Copy from: /d/do_an_J2EE/brain/
1. Project name
2. Tech stack
3. Key components (from CODEBASE_NAVIGATION_MAP)
4. Recent fixes (if known)
5. Reference guides location
```

### **Step 2: First Message**
```
"=== PROJECT CONTEXT ===

Project: Billiard Management System
Tech: Spring Boot + H2 + Thymeleaf + WebSocket
Repo: /d/do_an_J2EE

Key files/folders:
- /code/backend/src/main/java/com/bida/
- /code/backend/src/main/resources/templates/
- /brain/ (guides & templates)

Reference docs:
- CODEBASE_NAVIGATION_MAP.md (file structure)
- QUICK_REFERENCE.md (quick lookup)
- ERROR_DIAGNOSIS_TEMPLATE.md (format)
- FEATURE_REQUEST_TEMPLATE.md (format)

Ready to help!"
```

### **Step 3: Second Message (Your Issue)**
```
"=== ERROR DIAGNOSIS ===

[Copy template + fill 5 parts]"

OR

"=== FEATURE REQUEST ===

[Copy template + fill 5 parts]"
```

### **Result:**
```
✅ I understand project context
✅ I know exact files to open
✅ I don't scan codebase
✅ ~5,000 tokens per issue
✅ Fast resolution! 🚀
```

---

## 🎁 SHORTCUT: Save This as Template

Create a file `_NEW_CHAT_TEMPLATE.txt` for yourself:

```
=== NEW SESSION START ===

Project: Billiard Management System (Phase 2)
Tech: Spring Boot + H2 + Thymeleaf + WebSocket
Repo: /d/do_an_J2EE

Context from /brain/ guides:
- CODEBASE_NAVIGATION_MAP.md (browse here first)
- QUICK_REFERENCE.md (for quick lookup)
- ERROR_DIAGNOSIS_TEMPLATE.md (copy when issue)
- FEATURE_REQUEST_TEMPLATE.md (copy when feature)

Key locations:
- Services: /service/
- Controllers: /controller/api/ + /controller/admin/
- Entities: /entity/
- Repositories: /repository/

Ready! Describe your issue:
```

**Then copy-paste this + add your issue** = instant context! ✅

---

## 💡 REAL EXAMPLE: What To Send in New Chat

```
=== NEW SESSION ===

Project: Billiard Management System
Tech: Spring Boot backend + H2 DB + Thymeleaf frontend

Key components (from CODEBASE_NAVIGATION_MAP.md):
- Billing: BillingCalculator, TimeSlotPricingStrategy
- Session: SessionService
- Products: ProductService, ProductApiController
- Admin: AdminProductController, AdminInvoiceController
- Realtime: TableStatusBroadcaster (WebSocket)

Recent fixes (don't re-do):
✓ Billing exception handling (IllegalStateException)
✓ CSRF tokens on admin forms
✓ Product API (GET /api/products)
✓ Invoice filter validation
✓ WebSocket periodic broadcast

---

NOW - My issue:

=== ERROR DIAGNOSIS ===

[LỖI TIÊU ĐỀ]
Starting multiple sessions → database connection pool exhausted

[BƯỚC TÁI HIỆN]
1. Open 3 browser tabs
2. Each tab: start session on different tables
3. After ~10 sessions started → DB error

[THÔNG TIN]
- Concurrent sessions: 3
- Total sessions: 10
- Error: Connection pool exhausted

[LỖI MESSAGE]
HikariPool - Connection is not available, request timed out after 30000ms.

[LIÊN QUAN FILE]
SessionService, SessionRepository, DatabaseConfig
```

**Send this** → I'll know:
- Project context ✅
- Recent fixes ✅
- Exact problem ✅
- Files to check ✅
- No scanning needed ✅

---

## ✨ FINAL TIP

**Create a text file you always copy-paste:**

```
_START_NEW_CHAT.txt

=== CONTEXT ===
Project: Billiard Management System
Docs: /d/do_an_J2EE/brain/CODEBASE_NAVIGATION_MAP.md
Templates: /d/do_an_J2EE/brain/ERROR_DIAGNOSIS_TEMPLATE.md
Reference: /d/do_an_J2EE/brain/QUICK_REFERENCE.md

===

[PASTE ISSUE USING TEMPLATE BELOW]
```

**Every new chat: paste this file + your issue** = instant context! 🚀

---

## 🎯 SUMMARY

**When starting NEW chat:**

1️⃣ Provide project context (project name, tech, key components)
2️⃣ Reference guides location (/brain/ folder)
3️⃣ Use template for issue/feature (ERROR or FEATURE)
4️⃣ Fill all 5 parts completely
5️⃣ Send

**Result:**
- ✅ I understand project fully
- ✅ I don't need to scan codebase
- ✅ ~5,000 tokens spent
- ✅ 90% credit saved
- ✅ Fast resolution!

---

**When next time you start new chat, follow this pattern and I'll know everything!** 🎉
