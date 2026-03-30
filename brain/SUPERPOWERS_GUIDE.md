# 🦸 SUPERPOWERS FRAMEWORK - Hướng Dẫn Dùng

**Location:** `/d/do_an_J2EE/brain/superpowers/`

**Là gì:** Framework để tạo **custom AI skills** cho Claude Code tự động trigger

---

## 📚 14 SKILLS CÓ SẴN

```
1. brainstorming
   → Giúp brainstorm ideas trước khi code

2. systematic-debugging ⭐
   → Hướng dẫn debug logic đơn giản

3. test-driven-development ⭐
   → Generate tests trước khi implement

4. subagent-driven-development ⭐
   → Dispatch parallel agents cho tasks

5. executing-plans ⭐
   → Implement theo plan

6. verification-before-completion ⭐
   → Verify trước khi hoàn thành

7. writing-plans
   → Viết implementation plan

8. requesting-code-review
   → Request code review từ reviewers

9. receiving-code-review
   → Nhận code review feedback

10. dispatching-parallel-agents
    → Chạy agents song song

11. finishing-a-development-branch
    → Hoàn thành branch + merge

12. using-git-worktrees
    → Dùng git worktrees tự động

13. using-superpowers
    → Guide về dùng superpowers

14. writing-skills
    → Tạo custom skills riêng
```

---

## 🎯 USEFUL SKILLS CHO DỰ ÁN CỦA BẠN

### **1️⃣ systematic-debugging** (Khi gặp lỗi)
```
Trigger: Describe error logic, ask to find root cause
What it does:
  • Analyze error symptoms
  • Trace execution path
  • Find root cause
  • Suggest fixes

Files:
  - SKILL.md (main guide)
  - root-cause-tracing.md
  - defense-in-depth.md
```

**Example:**
```
"Billing calculation returns 0 when PriceRule missing -
why does this happen and how to fix?"

→ Skill tự động:
  1. Analyze lỗi symptoms
  2. Trace flow từ SessionService → BillingCalculator → Repository
  3. Tìm root cause (missing PriceRule not handled)
  4. Suggest fix (throw IllegalStateException instead of silent fail)
```

---

### **2️⃣ test-driven-development** (Khi viết tests)
```
Trigger: "Write tests for [feature]" or "Test this code"
What it does:
  • Generate test cases
  • Write actual tests
  • Run tests
  • Coverage analysis

Strategy:
  • Red → Green → Refactor cycle
  • YAGNI (You Aren't Gonna Need It)
  • DRY (Don't Repeat Yourself)
```

**Example:**
```
"Write tests for ProductApiController.getAllProducts()"

→ Skill tự động:
  1. Analyze ProductApiController
  2. Generate test cases
     - Happy path (returns products)
     - Empty case (no products)
     - Error case (exception)
  3. Write JUnit tests
  4. Run tests
  5. Report coverage
```

---

### **3️⃣ subagent-driven-development** (Khi implement lớn)
```
Trigger: Automatically when implementing plan with multiple tasks
What it does:
  • Spawn multiple agents
  • Assign tasks in parallel
  • Verify each agent's work
  • Coordinate results

Flow:
  Plan → Split into tasks → Dispatch agents → Verify → Merge
```

**Example:**
```
Plan has 5 tasks:
1. Create ProductApiController
2. Create ProductService
3. Update ProductRepository
4. Write tests
5. Verify integration

→ Skill tự động:
  • Agent 1 tạo Controller
  • Agent 2 tạo Service (parallel)
  • Agent 3 update Repository (parallel)
  • All agents work simultaneously
  • Verify khi done
  • Merge results
```

---

### **4️⃣ executing-plans** (Khi implement)
```
Trigger: After approval of plan, say "implement"
What it does:
  • Follow plan step-by-step
  • Create commits for each step
  • Verify each step
  • Run tests

Flow:
  Step 1 → Commit → Verify → Step 2 → Commit → ...
```

---

### **5️⃣ verification-before-completion** (Cuối cùng)
```
Trigger: Automatically before completing task
What it does:
  • Verify all code works
  • Run all tests
  • Check edge cases
  • Final review

Checks:
  ✓ Compilation success
  ✓ All tests pass
  ✓ No warnings
  ✓ Code quality
  ✓ Edge cases handled
```

---

## 🚀 CÁCH DÙNG SUPERPOWERS

### **Method 1: Auto-Trigger (Recommended)**
```
Skills trigger automatically khi bạn:
- "Help me debug this" → systematic-debugging
- "Write tests for" → test-driven-development
- "Let me plan this feature" → writing-plans
- "Implement the plan" → executing-plans + subagent-driven-development
- "Verify before completing" → verification-before-completion

Không cần làm gì đặc biệt!
```

### **Method 2: Explicit Request**
```
Nếu auto-trigger không work:

/systematic-debugging
  → Analyze root cause của lỗi

/test-driven-development
  → Generate & write tests

/subagent-driven-development
  → Dispatch parallel agents

/executing-plans
  → Follow plan step-by-step
```

### **Method 3: Using Skill Tool**
```
Tôi có thể call skill via Skill tool:

Skill("systematic-debugging", args: "Describe error")
  → Explicit skill invocation
```

---

## 💡 BEST PRACTICES

### **Khi gặp lỗi:**
```
BAD: "Fix this error"
GOOD: "I got NullPointerException in BillingCalculator.
       It happens when I end a session.
       Help me find root cause and fix it systematically."

→ Triggers: systematic-debugging skill
→ Skill will trace execution, find cause, suggest fix
```

### **Khi viết feature lớn:**
```
FLOW:
1. "Let me plan this feature"
   → writing-plans skill triggers
   → I create detailed plan

2. "Implement the plan"
   → executing-plans + subagent-driven-development
   → Multiple agents work in parallel

3. "Verify before shipping"
   → verification-before-completion
   → All checks pass before merge
```

### **Khi debug systematically:**
```
KHÔNG:
- Randomly change code
- Hope it works
- Tốn credit với scanning

CÓ:
- Use systematic-debugging skill
- Trace execution logically
- Find root cause
- Implement minimal fix
- Verify works
- Efficient + effective!
```

---

## 🎯 ỨNG DỤNG CHO DỰ ÁN BẠN

### **Scenario 1: Gặp lỗi mới**
```
Bạn: "Mình test và gặp lỗi billing:
      Session end returns totalAmount = null instead of 105000"

→ Tôi trigger: systematic-debugging
→ Skill sẽ:
  1. Ask: "Loại bàn? Khung giờ? TableType nào?"
  2. Trace: SessionService.endSession()
     → BillingCalculator.calculate()
     → TimeSlotPricingStrategy.calculate()
  3. Find: "PriceRule không tìm thấy → NullPointerException"
  4. Fix: "Throw IllegalStateException instead"

→ Bạn: Verify + approved
→ Tôi: Implement + commit + push

Result: Bug fixed systematically, not randomly! ✅
```

---

### **Scenario 2: Thêm feature lớn**
```
Bạn: "Muốn thêm membership discount system"

→ Tôi:
  1. Trigger: writing-plans
     • Design entity (MembershipTier, discount logic)
     • Design API (getDiscount, applyDiscount)
     • Design tests

  2. Ask: "Bạn approve plan này không?"

  3. When approved, trigger: executing-plans + subagent-driven-development
     • Agent 1: Create MembershipTier enum + Customer entity update
     • Agent 2: Create DiscountService + Business logic
     • Agent 3: Create tests
     • Agent 4: Update InvoiceService
     (All parallel = 4x faster!)

  4. Verify: verification-before-completion
     • All tests pass
     • No null checks missed
     • Edge cases handled

  5. Merge + done

Result: Large feature implemented in parallel! 🚀
```

---

### **Scenario 3: Code Review Workflow**
```
Bạn: "Hãy review đoạn code này"

→ Tôi trigger: requesting-code-review
  • Analyze code quality
  • Find issues
  • Suggest improvements

→ Bạn: "Fix theo suggestions"

→ Tôi trigger: receiving-code-review
  • Implement fixes
  • Re-test
  • Verify better quality

Result: Professional code review cycle ✅
```

---

## 📊 SKILLS MATRIX - Khi nào dùng?

| Situation | Skill | Trigger | Benefit |
|-----------|-------|---------|---------|
| Gặp lỗi lạ | systematic-debugging | Auto or `/systematic-debugging` | Find root cause, not symptoms |
| Viết tests | test-driven-development | Auto or `/test-driven-development` | Comprehensive test coverage |
| Plan feature | writing-plans | Auto or `/writing-plans` | Clear implementation road map |
| Implement | executing-plans | Auto or `/executing-plans` | Step-by-step with commits |
| Parallel tasks | subagent-driven-development | Auto when needed | 3-4x faster (multiple agents) |
| Final check | verification-before-completion | Auto | No bugs slip through |
| Code review | requesting-code-review | Auto or explicit | Professional review |
| Tricky debug | systematic-debugging | Explicit request | Logical trace, find root cause |

---

## ⚡ TIPS TO MAXIMIZE

### **Tip 1: Describe Error Clearly**
```
BAD: "Lỗi ở tính tiền"
GOOD: "POOL 8-12 WEEKDAY → NullPointerException ở TimeSlotPricingStrategy.calculate()"

→ systematic-debugging có context rõ ràng
→ Trace nhanh hơn, fix đúng hơn
```

### **Tip 2: Use for Complex Tasks**
```
SMALL TASK: Write simple method
→ Don't need subagent-driven-development

LARGE TASK: Add membership system (5+ components)
→ Use subagent-driven-development
→ Parallel agents = 4x faster
```

### **Tip 3: Always Verify**
```
BEFORE: Ship code without checking
AFTER: Let verification-before-completion check
  ✓ Tests all pass
  ✓ No null checks missed
  ✓ Edge cases covered
  ✓ Code quality good
```

---

## 🎓 NEXT STEPS

### **Option 1: Use Auto-Triggered Skills**
```
Just describe errors/tasks clearly:
- "Help me debug this error" (triggers systematic-debugging)
- "Write tests for this" (triggers test-driven-development)
- "Implement this feature" (triggers executing-plans)
- Everything automatic!
```

### **Option 2: Explicit Skill Requests**
```
If auto-trigger doesn't work:

"I have a complex bug.
 Use /systematic-debugging to trace it logically"

→ Skill explicitly invoked
```

### **Option 3: Learn Skill Details**
```
Read skill documentation:
- /brain/superpowers/skills/systematic-debugging/SKILL.md
- /brain/superpowers/skills/test-driven-development/SKILL.md
- /brain/superpowers/skills/subagent-driven-development/SKILL.md

Understand how each works
```

---

## 🚀 FINAL ADVICE

**Superpowers + Credit-Saving Templates = 🔥 Combo!**

```
✅ Systematic-debugging skill     (when bug hunting)
✅ Credit-saving templates         (when describing)
✅ Subagent-driven-development    (when parallelizing)

= Professional development workflow!
```

---

**Next:** When you encounter a complex error, trigger systematic-debugging! 🎯
