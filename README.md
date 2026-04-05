# 🎱 Billiards Management System (QuanLyBida)

A comprehensive, full-stack management solution for billiard halls, featuring real-time table tracking, automated billing, product inventory (F&B), and role-based access control.

[![Java](https://img.shields.io/badge/Java-17-orange.svg?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg?style=flat-square&logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5.0-646CFF.svg?style=flat-square&logo=vite)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg?style=flat-square&logo=mysql)](https://www.mysql.com/)

---

## 🌟 Key Features

- **📍 Real-time Table Dashboard**: Monitor and manage all tables (Pool, Carom, VIP) with live status updates.
- **🧾 Automated Billing**: Precise cost calculation based on configurable price rules, duration, and food/drink orders.
- **💵 Professional Invoicing**: Quick PDF receipt generation with branding and itemized costs.
- **🍔 Product Inventory**: Integrated Point of Sale (POS) for managing kitchen and bar items.
- **🏢 Staff Management**: Secure login for Admin and Staff with distinct permissions.
- **⏳ Dynamic Pricing**: Flexible price rules for weekdays, weekends, holidays, and specific time ranges.
- **📅 Holiday Calendar**: Pre-defined holidays to automate surcharge application.
- **📊 Analytics**: Simple sales and performance tracking (Daily/Monthly reports).

---

## 🏗️ Project Structure

The project is split into a **Backend** (Spring Boot) and a **Frontend** (React/Vite).

```text
.
├── code/
│   ├── backend/          # Spring Boot 3.2 (Java 17, Maven)
│   └── frontend/         # React SPA (Vite, JavaScript)
├── README.md             # This file
└── .gitignore            # Global ignore rules
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 17 JDK** or higher
- **Node.js** (v18+) & **npm/yarn**
- **MySQL 8.0**
- **Maven 3.9+**

### 2. Database Setup
1. Create a MySQL database named `bida_db`.
2. Configure credentials in `code/backend/src/main/resources/application-dev.properties` (or set environment variables).

### 3. Backend Setup (Spring Boot)
```bash
cd code/backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- The backend will seed sample data (tables, users, products) on the first run.
- Default Admin: `admin` / `admin123`
- Default Staff: `staff1` / `staff123`

### 4. Frontend Setup (React)
```bash
cd code/frontend
npm install
npm run dev
```
- Open your browser at `http://localhost:5173` (default Vite port).

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: MySQL 8.0
- **Security**: Spring Security (JWT and Session based)
- **Reporting**: PDF generation for invoices
- **Validation**: Jakarta Bean Validation

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **UI Components**: Modern custom CSS & Bootstrap integrations
- **State Management**: React Hooks & Context API
- **API Client**: Axios

---

## 📝 Usage Notes

- **Price Rules**: Admin can configure different rates for various table types (e.g., VIP vs standard) and time segments.
- **Billing**: The system automatically calculates duration since "Check-in" and closes the session upon "Check-out", generating a final bill including all consumed products.
- **Staff Roles**: Staff can manage check-ins and orders, while only Admins can access analytics and system settings.

---

## 🛡️ License

This project is intended for educational purposes and internal management.

---

*Made with ❤️ for more efficient billiard hall operations.*
