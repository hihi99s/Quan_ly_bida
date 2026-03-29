#!/bin/bash

# VERIFICATION SCRIPT - Check all fixes are properly applied
# Usage: bash /d/do_an_J2EE/brain/VERIFY_FIXES.sh

echo "======================================================================"
echo "   VERIFYING ALL FIXES - Billiard Management System"
echo "======================================================================"
echo ""

ERRORS=0
CHECKS=0

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_file_contains() {
    local file=$1
    local pattern=$2
    local description=$3

    ((CHECKS++))

    if [ ! -f "$file" ]; then
        echo -e "${RED}✗ FAIL${NC} - File not found: $file"
        ((ERRORS++))
        return 1
    fi

    if grep -q "$pattern" "$file" 2>/dev/null; then
        echo -e "${GREEN}✓ PASS${NC} - $description"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} - $description"
        echo "        Expected pattern in: $file"
        ((ERRORS++))
        return 1
    fi
}

# ============================================================================
# ERROR 1: Billing - Proper Exception Handling
# ============================================================================
echo -e "${YELLOW}[1] Billing Calculation Fixes${NC}"
echo "---"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/service/SessionService.java" \
    "billingCalculator.calculate(session);" \
    "SessionService: Direct call to calculate (no fallback)"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/billing/service/BillingCalculator.java" \
    "✓ Tính tiền thành công" \
    "BillingCalculator: Success logging with details"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/billing/strategy/TimeSlotPricingStrategy.java" \
    "IllegalStateException" \
    "TimeSlotPricingStrategy: Throws IllegalStateException (not RuntimeException)"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/billing/strategy/TimeSlotPricingStrategy.java" \
    "Không tìm thấy bảng giá" \
    "TimeSlotPricingStrategy: Clear error message with context"

echo ""

# ============================================================================
# ERROR 2: Security - CSRF Token in Forms
# ============================================================================
echo -e "${YELLOW}[2] Security - CSRF Tokens${NC}"
echo "---"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/config/SecurityConfig.java" \
    'ignoringRequestMatchers("/ws/\*\*", "/api/\*\*")' \
    "SecurityConfig: Only /ws/** and /api/** have CSRF disabled"

# Count CSRF tokens in products.html (should be 5)
csrf_count=$(grep -c 'th:name="\${_csrf.parameterName}"' \
    "/d/do_an_J2EE/code/backend/src/main/resources/templates/admin/products.html" 2>/dev/null || echo 0)

((CHECKS++))
if [ "$csrf_count" -ge 5 ]; then
    echo -e "${GREEN}✓ PASS${NC} - products.html: Found $csrf_count CSRF tokens (need ≥5)"
else
    echo -e "${RED}✗ FAIL${NC} - products.html: Only found $csrf_count CSRF tokens (need ≥5)"
    ((ERRORS++))
fi

echo ""

# ============================================================================
# ERROR 3: Product API
# ============================================================================
echo -e "${YELLOW}[3] Product API Endpoints${NC}"
echo "---"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/controller/api/ProductApiController.java" \
    "@GetMapping" \
    "ProductApiController: GET endpoints defined"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/controller/api/ProductApiController.java" \
    "/api/products" \
    "ProductApiController: /api/products endpoint"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/controller/api/ProductApiController.java" \
    "✓ GET /api/products" \
    "ProductApiController: Success logging"

echo ""

# ============================================================================
# ERROR 4: Invoice Filter
# ============================================================================
echo -e "${YELLOW}[4] Invoice Filter - Input Validation${NC}"
echo "---"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/controller/admin/AdminInvoiceController.java" \
    "DateTimeParseException" \
    "AdminInvoiceController: Catches DateTimeParseException"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/controller/admin/AdminInvoiceController.java" \
    "isAfter" \
    "AdminInvoiceController: Validates date range (from <= to)"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/controller/admin/AdminInvoiceController.java" \
    "if (invoices == null)" \
    "AdminInvoiceController: Null safety check"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/controller/admin/AdminInvoiceController.java" \
    "@Slf4j" \
    "AdminInvoiceController: Has logging annotation"

echo ""

# ============================================================================
# ERROR 5: Realtime Broadcast
# ============================================================================
echo -e "${YELLOW}[5] Realtime Dashboard - Periodic Broadcast${NC}"
echo "---"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/websocket/TableStatusBroadcaster.java" \
    "@Scheduled" \
    "TableStatusBroadcaster: @Scheduled annotation present"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/websocket/TableStatusBroadcaster.java" \
    "fixedDelay = 5000" \
    "TableStatusBroadcaster: 5-second periodic interval"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/websocket/TableStatusBroadcaster.java" \
    "broadcastTableStatusPeriodically" \
    "TableStatusBroadcaster: Periodic broadcast method"

check_file_contains \
    "/d/do_an_J2EE/code/backend/src/main/java/com/bida/BidaApplication.java" \
    "@EnableScheduling" \
    "BidaApplication: @EnableScheduling annotation present"

echo ""

# ============================================================================
# RESULTS
# ============================================================================
echo "======================================================================"
echo "   VERIFICATION RESULTS"
echo "======================================================================"

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ ALL CHECKS PASSED ($CHECKS/$CHECKS)${NC}"
    echo ""
    echo "🚀 All fixes are properly applied and ready for testing!"
    exit 0
else
    echo -e "${RED}✗ SOME CHECKS FAILED ($((CHECKS - ERRORS))/$CHECKS passed, $ERRORS failed)${NC}"
    echo ""
    echo "⚠️  Please review the failed checks above and apply the necessary fixes."
    exit 1
fi
