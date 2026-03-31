package com.bida.config;

import com.bida.entity.*;
import com.bida.entity.enums.*;
import com.bida.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final BilliardTableRepository billiardTableRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final UserRepository userRepository;
    private final AppSettingRepository appSettingRepository;
    private final ProductRepository productRepository;
    private final HolidayCalendarRepository holidayCalendarRepository;
    private final CustomerRepository customerRepository;
    private final ShiftRepository shiftRepository;
    private final DiscountCodeRepository discountCodeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        int seededTables = seedBilliardTables();
        int seededRules = seedPriceRules();
        int seededUsers = seedUsers();
        int seededSettings = seedAppSettings();
        int seededProducts = seedProducts();
        int seededHolidays = seedHolidays();
        int seededCustomers = seedCustomers();
        int seededShifts = seedShifts();
        int seededDiscountCodes = seedDiscountCodes();

        int total = seededTables + seededRules + seededUsers + seededSettings
                + seededProducts + seededHolidays + seededCustomers + seededShifts + seededDiscountCodes;

        if (total > 0) {
            log.info(
                    "Seeded: {} tables, {} rules, {} users, {} settings, {} products, {} holidays, {} customers, {} shifts, {} discount codes",
                    seededTables, seededRules, seededUsers, seededSettings,
                    seededProducts, seededHolidays, seededCustomers, seededShifts, seededDiscountCodes);
        } else {
            log.info("Database already seeded — skipping data initialization.");
        }
    }

    // -------------------------------------------------------------------------
    // 1. BILLIARD TABLES
    // -------------------------------------------------------------------------
    private int seedBilliardTables() {
        if (billiardTableRepository.count() > 0)
            return 0;

        List<BilliardTable> tables = new ArrayList<>();
        tables.add(buildTable("Pool 01", TableType.POOL));
        tables.add(buildTable("Pool 02", TableType.POOL));
        tables.add(buildTable("Pool 03", TableType.POOL));
        tables.add(buildTable("Pool 04", TableType.POOL));
        tables.add(buildTable("Carom 01", TableType.CAROM));
        tables.add(buildTable("Carom 02", TableType.CAROM));
        tables.add(buildTable("Carom 03", TableType.CAROM));
        tables.add(buildTable("VIP 01", TableType.VIP));
        tables.add(buildTable("VIP 02", TableType.VIP));
        tables.add(buildTable("VIP 03", TableType.VIP));

        billiardTableRepository.saveAll(tables);
        return tables.size();
    }

    private BilliardTable buildTable(String name, TableType type) {
        return BilliardTable.builder().name(name).tableType(type).status(TableStatus.AVAILABLE).build();
    }

    // -------------------------------------------------------------------------
    // 2. PRICE RULES
    // -------------------------------------------------------------------------
    private int seedPriceRules() {
        if (priceRuleRepository.count() > 0)
            return 0;

        List<PriceRule> rules = new ArrayList<>();
        LocalTime t08 = LocalTime.of(8, 0), t12 = LocalTime.of(12, 0);
        LocalTime t17 = LocalTime.of(17, 0), t23 = LocalTime.of(23, 0);

        // POOL
        rules.add(buildRule(TableType.POOL, DayType.WEEKDAY, t08, t12, 40_000));
        rules.add(buildRule(TableType.POOL, DayType.WEEKDAY, t12, t17, 50_000));
        rules.add(buildRule(TableType.POOL, DayType.WEEKDAY, t17, t23, 70_000));
        rules.add(buildRule(TableType.POOL, DayType.WEEKEND, t08, t12, 50_000));
        rules.add(buildRule(TableType.POOL, DayType.WEEKEND, t12, t17, 60_000));
        rules.add(buildRule(TableType.POOL, DayType.WEEKEND, t17, t23, 80_000));
        rules.add(buildRule(TableType.POOL, DayType.HOLIDAY, t08, t12, 60_000));
        rules.add(buildRule(TableType.POOL, DayType.HOLIDAY, t12, t17, 70_000));
        rules.add(buildRule(TableType.POOL, DayType.HOLIDAY, t17, t23, 90_000));

        // CAROM
        rules.add(buildRule(TableType.CAROM, DayType.WEEKDAY, t08, t12, 50_000));
        rules.add(buildRule(TableType.CAROM, DayType.WEEKDAY, t12, t17, 60_000));
        rules.add(buildRule(TableType.CAROM, DayType.WEEKDAY, t17, t23, 80_000));
        rules.add(buildRule(TableType.CAROM, DayType.WEEKEND, t08, t12, 60_000));
        rules.add(buildRule(TableType.CAROM, DayType.WEEKEND, t12, t17, 70_000));
        rules.add(buildRule(TableType.CAROM, DayType.WEEKEND, t17, t23, 90_000));
        rules.add(buildRule(TableType.CAROM, DayType.HOLIDAY, t08, t12, 70_000));
        rules.add(buildRule(TableType.CAROM, DayType.HOLIDAY, t12, t17, 80_000));
        rules.add(buildRule(TableType.CAROM, DayType.HOLIDAY, t17, t23, 100_000));

        // VIP
        rules.add(buildRule(TableType.VIP, DayType.WEEKDAY, t08, t12, 70_000));
        rules.add(buildRule(TableType.VIP, DayType.WEEKDAY, t12, t17, 80_000));
        rules.add(buildRule(TableType.VIP, DayType.WEEKDAY, t17, t23, 100_000));
        rules.add(buildRule(TableType.VIP, DayType.WEEKEND, t08, t12, 80_000));
        rules.add(buildRule(TableType.VIP, DayType.WEEKEND, t12, t17, 90_000));
        rules.add(buildRule(TableType.VIP, DayType.WEEKEND, t17, t23, 110_000));
        rules.add(buildRule(TableType.VIP, DayType.HOLIDAY, t08, t12, 90_000));
        rules.add(buildRule(TableType.VIP, DayType.HOLIDAY, t12, t17, 100_000));
        rules.add(buildRule(TableType.VIP, DayType.HOLIDAY, t17, t23, 120_000));

        // POOL - Night shift (23:00 → 08:00 next day, same as 17-23 rate)
        rules.add(buildRule(TableType.POOL, DayType.WEEKDAY, t23, t08, 70_000));
        rules.add(buildRule(TableType.POOL, DayType.WEEKEND, t23, t08, 80_000));
        rules.add(buildRule(TableType.POOL, DayType.HOLIDAY, t23, t08, 90_000));

        // CAROM - Night shift (same as 17-23 rate)
        rules.add(buildRule(TableType.CAROM, DayType.WEEKDAY, t23, t08, 80_000));
        rules.add(buildRule(TableType.CAROM, DayType.WEEKEND, t23, t08, 90_000));
        rules.add(buildRule(TableType.CAROM, DayType.HOLIDAY, t23, t08, 100_000));

        // VIP - Night shift (same as 17-23 rate)
        rules.add(buildRule(TableType.VIP, DayType.WEEKDAY, t23, t08, 100_000));
        rules.add(buildRule(TableType.VIP, DayType.WEEKEND, t23, t08, 110_000));
        rules.add(buildRule(TableType.VIP, DayType.HOLIDAY, t23, t08, 120_000));

        priceRuleRepository.saveAll(rules);
        return rules.size();
    }

    private PriceRule buildRule(TableType tableType, DayType dayType, LocalTime start, LocalTime end, long price) {
        return PriceRule.builder().tableType(tableType).dayType(dayType)
                .startTime(start).endTime(end).pricePerHour(BigDecimal.valueOf(price)).build();
    }

    // -------------------------------------------------------------------------
    // 3. USERS
    // -------------------------------------------------------------------------
    private int seedUsers() {
        if (userRepository.count() > 0)
            return 0;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        List<User> users = List.of(
                User.builder().username("admin").password(encoder.encode("admin123"))
                        .fullName("Quan ly").role(UserRole.ADMIN).active(true)
                        .phone("0901234567").email("admin@bida.vn").hireDate(LocalDate.of(2025, 1, 1)).build(),
                User.builder().username("staff1").password(encoder.encode("staff123"))
                        .fullName("Nhan vien 1").role(UserRole.STAFF).active(true)
                        .phone("0912345678").email("staff1@bida.vn").hireDate(LocalDate.of(2025, 3, 1)).build(),
                User.builder().username("staff2").password(encoder.encode("staff123"))
                        .fullName("Nhan vien 2").role(UserRole.STAFF).active(true)
                        .phone("0923456789").email("staff2@bida.vn").hireDate(LocalDate.of(2025, 6, 1)).build());
        userRepository.saveAll(users);
        return users.size();
    }

    // -------------------------------------------------------------------------
    // 4. APP SETTINGS
    // -------------------------------------------------------------------------
    private int seedAppSettings() {
        if (appSettingRepository.count() > 0)
            return 0;

        List<AppSetting> settings = List.of(
                AppSetting.builder().settingKey("HOLIDAY_MODE").settingValue("false").build(),
                AppSetting.builder().settingKey("RESERVATION_TIMEOUT_MINUTES").settingValue("15").build());
        appSettingRepository.saveAll(settings);
        return settings.size();
    }

    // -------------------------------------------------------------------------
    // 5. PRODUCTS (Phase 2: F&B)
    // -------------------------------------------------------------------------
    private int seedProducts() {
        if (productRepository.count() > 0)
            return 0;

        List<Product> products = List.of(
                // DRINK
                Product.builder().name("Bia Tiger").category(ProductCategory.DRINK).price(BigDecimal.valueOf(25000))
                        .stockQuantity(100).active(true).build(),
                Product.builder().name("Bia Saigon").category(ProductCategory.DRINK).price(BigDecimal.valueOf(20000))
                        .stockQuantity(100).active(true).build(),
                Product.builder().name("Coca Cola").category(ProductCategory.DRINK).price(BigDecimal.valueOf(15000))
                        .stockQuantity(50).active(true).build(),
                Product.builder().name("Pepsi").category(ProductCategory.DRINK).price(BigDecimal.valueOf(15000))
                        .stockQuantity(50).active(true).build(),
                Product.builder().name("Nuoc suoi").category(ProductCategory.DRINK).price(BigDecimal.valueOf(10000))
                        .stockQuantity(80).active(true).build(),
                Product.builder().name("Tra da").category(ProductCategory.DRINK).price(BigDecimal.valueOf(5000))
                        .stockQuantity(200).active(true).build(),
                Product.builder().name("Ca phe sua da").category(ProductCategory.DRINK).price(BigDecimal.valueOf(20000))
                        .stockQuantity(50).active(true).build(),
                Product.builder().name("Red Bull").category(ProductCategory.DRINK).price(BigDecimal.valueOf(15000))
                        .stockQuantity(30).active(true).build(),
                // FOOD
                Product.builder().name("Mi tom").category(ProductCategory.FOOD).price(BigDecimal.valueOf(25000))
                        .stockQuantity(30).active(true).build(),
                Product.builder().name("Com chien").category(ProductCategory.FOOD).price(BigDecimal.valueOf(35000))
                        .stockQuantity(20).active(true).build(),
                // SNACK
                Product.builder().name("Dau phong").category(ProductCategory.SNACK).price(BigDecimal.valueOf(15000))
                        .stockQuantity(40).active(true).build(),
                Product.builder().name("Kho bo").category(ProductCategory.SNACK).price(BigDecimal.valueOf(30000))
                        .stockQuantity(25).active(true).build(),
                Product.builder().name("Banh trang trung").category(ProductCategory.SNACK)
                        .price(BigDecimal.valueOf(20000)).stockQuantity(35).active(true).build(),
                // OTHER
                Product.builder().name("Thuoc la").category(ProductCategory.OTHER).price(BigDecimal.valueOf(25000))
                        .stockQuantity(50).active(true).build());
        productRepository.saveAll(products);
        return products.size();
    }

    // -------------------------------------------------------------------------
    // 6. HOLIDAYS (Phase 2: VN holiday calendar)
    // -------------------------------------------------------------------------
    private int seedHolidays() {
        if (holidayCalendarRepository.count() > 0)
            return 0;

        int year = LocalDate.now().getYear();
        List<HolidayCalendar> holidays = List.of(
                HolidayCalendar.builder().name("Tet Duong lich").date(LocalDate.of(year, 1, 1)).recurring(true).build(),
                HolidayCalendar.builder().name("Gio To Hung Vuong").date(LocalDate.of(year, 4, 18)).recurring(false)
                        .build(),
                HolidayCalendar.builder().name("Ngay Giai phong mien Nam").date(LocalDate.of(year, 4, 30))
                        .recurring(true).build(),
                HolidayCalendar.builder().name("Ngay Quoc te Lao dong").date(LocalDate.of(year, 5, 1)).recurring(true)
                        .build(),
                HolidayCalendar.builder().name("Ngay Quoc khanh").date(LocalDate.of(year, 9, 2)).recurring(true)
                        .build());
        holidayCalendarRepository.saveAll(holidays);
        return holidays.size();
    }

    // -------------------------------------------------------------------------
    // 7. CUSTOMERS (Phase 2: sample customers)
    // -------------------------------------------------------------------------
    private int seedCustomers() {
        if (customerRepository.count() > 0)
            return 0;

        List<Customer> customers = List.of(
                Customer.builder().name("Nguyen Van A").phone("0987654321").email("a@gmail.com")
                        .membershipTier(MembershipTier.BRONZE).totalSpent(BigDecimal.ZERO).points(0).build(),
                Customer.builder().name("Tran Van B").phone("0987654322").email("b@gmail.com")
                        .membershipTier(MembershipTier.SILVER).totalSpent(BigDecimal.valueOf(800000)).points(800)
                        .build(),
                Customer.builder().name("Le Thi C").phone("0987654323").email("c@gmail.com")
                        .membershipTier(MembershipTier.GOLD).totalSpent(BigDecimal.valueOf(3000000)).points(3000)
                        .build());
        customerRepository.saveAll(customers);
        return customers.size();
    }

    // -------------------------------------------------------------------------
    // 8. SHIFTS (Phase 2: sample shifts)
    // -------------------------------------------------------------------------
    private int seedShifts() {
        if (shiftRepository.count() > 0)
            return 0;

        List<Shift> shifts = List.of(
                Shift.builder().name("Ca sang").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(14, 0)).build(),
                Shift.builder().name("Ca chieu").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(20, 0)).build(),
                Shift.builder().name("Ca toi").startTime(LocalTime.of(20, 0)).endTime(LocalTime.of(23, 0)).build());
        shiftRepository.saveAll(shifts);
        return shifts.size();
    }

    // -------------------------------------------------------------------------
    // 9. DISCOUNT CODES (Phase 2: sample discount codes)
    // -------------------------------------------------------------------------
    private int seedDiscountCodes() {
        if (discountCodeRepository.count() > 0)
            return 0;

        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        List<DiscountCode> codes = List.of(
                DiscountCode.builder()
                        .code("SUMMER50")
                        .discountPercent(new BigDecimal("10"))
                        .maxUsageCount(50)
                        .usageCount(0)
                        .active(true)
                        .expiryDate(nextMonth)
                        .build(),
                DiscountCode.builder()
                        .code("NEW2024")
                        .discountPercent(new BigDecimal("15"))
                        .maxUsageCount(100)
                        .usageCount(0)
                        .active(true)
                        .expiryDate(nextMonth.plusMonths(1))
                        .build());
        discountCodeRepository.saveAll(codes);
        return codes.size();
    }
}
