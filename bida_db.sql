-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th4 06, 2026 lúc 08:53 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `bida_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `app_settings`
--

CREATE TABLE `app_settings` (
  `id` bigint(20) NOT NULL,
  `setting_key` varchar(255) NOT NULL,
  `setting_value` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `app_settings`
--

INSERT INTO `app_settings` (`id`, `setting_key`, `setting_value`) VALUES
(1, 'HOLIDAY_MODE', 'false');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `billiard_tables`
--

CREATE TABLE `billiard_tables` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `status` varchar(20) NOT NULL,
  `table_type` enum('POOL','CAROM','VIP') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `billiard_tables`
--

INSERT INTO `billiard_tables` (`id`, `created_at`, `name`, `status`, `table_type`) VALUES
(1, '2026-03-29 12:09:39.000000', 'Pool 01', 'AVAILABLE', 'POOL'),
(2, '2026-03-29 12:09:39.000000', 'Pool 02', 'AVAILABLE', 'POOL'),
(3, '2026-03-29 12:09:39.000000', 'Pool 03', 'PLAYING', 'POOL'),
(4, '2026-03-29 12:09:39.000000', 'Pool 04', 'AVAILABLE', 'POOL'),
(5, '2026-03-29 12:09:39.000000', 'Carom 01', 'AVAILABLE', 'CAROM'),
(6, '2026-03-29 12:09:39.000000', 'Carom 02', 'AVAILABLE', 'CAROM'),
(7, '2026-03-29 12:09:39.000000', 'Carom 03', 'AVAILABLE', 'CAROM'),
(8, '2026-03-29 12:09:39.000000', 'VIP 01', 'AVAILABLE', 'VIP'),
(9, '2026-03-29 12:09:39.000000', 'VIP 02', 'AVAILABLE', 'VIP'),
(10, '2026-03-29 12:09:39.000000', 'VIP 03', 'AVAILABLE', 'VIP'),
(14, '2026-03-31 20:44:23.000000', 'Carom 01', 'AVAILABLE', 'CAROM'),
(15, '2026-04-06 23:47:34.000000', '11', 'AVAILABLE', 'CAROM');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `customers`
--

CREATE TABLE `customers` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `membership_tier` enum('BRONZE','SILVER','GOLD','DIAMOND') NOT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `points` int(11) NOT NULL,
  `total_spent` decimal(12,0) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `customers`
--

INSERT INTO `customers` (`id`, `created_at`, `email`, `membership_tier`, `name`, `phone`, `points`, `total_spent`) VALUES
(1, '2026-03-29 22:50:41.000000', 'a@gmail.com', 'BRONZE', 'Nguyen Van A', '0987654321', 0, 0),
(2, '2026-03-29 22:50:41.000000', 'b@gmail.com', 'SILVER', 'Tran Van B', '0987654322', 800, 800000),
(3, '2026-03-29 22:50:41.000000', 'c@gmail.com', 'DIAMOND', 'Le Thi C', '0987654323', 6878, 6878851),
(4, '2026-04-04 01:17:03.000000', '', 'BRONZE', 'Nguy', '0901234567', 0, 0);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `discount_codes`
--

CREATE TABLE `discount_codes` (
  `id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `code` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `discount_percent` decimal(38,2) NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `max_usage_count` int(11) DEFAULT NULL,
  `usage_count` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `discount_codes`
--

INSERT INTO `discount_codes` (`id`, `active`, `code`, `created_at`, `discount_percent`, `expiry_date`, `max_usage_count`, `usage_count`) VALUES
(1, b'1', 'TUDEPTRAI', '2026-03-31 20:57:53.000000', 20.00, '2026-04-30', 50, 5),
(3, b'1', '1', '2026-04-01 01:02:38.000000', 1.00, '2026-04-01', 2, 2);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `holiday_calendar`
--

CREATE TABLE `holiday_calendar` (
  `id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `name` varchar(255) NOT NULL,
  `recurring` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `holiday_calendar`
--

INSERT INTO `holiday_calendar` (`id`, `date`, `name`, `recurring`) VALUES
(2, '2026-04-18', 'Gio To Hung Vuong', b'0'),
(3, '2026-04-30', 'Ngay Giai phong mien Nam', b'1'),
(4, '2026-05-01', 'Ngay Quoc te Lao dong', b'1'),
(5, '2026-09-02', 'Ngay Quoc khanh', b'1');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `invoices`
--

CREATE TABLE `invoices` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `discount` decimal(12,0) NOT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `service_charge` decimal(12,0) NOT NULL,
  `table_charge` decimal(12,0) NOT NULL,
  `total_amount` decimal(12,0) NOT NULL,
  `session_id` bigint(20) NOT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `manual_table_charge` decimal(12,0) DEFAULT NULL,
  `code_discount_amount` decimal(12,0) DEFAULT NULL,
  `discount_code_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `invoices`
--

INSERT INTO `invoices` (`id`, `created_at`, `discount`, `invoice_number`, `service_charge`, `table_charge`, `total_amount`, `session_id`, `staff_id`, `manual_table_charge`, `code_discount_amount`, `discount_code_id`) VALUES
(1, '2026-03-29 22:51:27.000000', 0, 'INV-20260329-001', 0, 0, 0, 3, 1, NULL, NULL, NULL),
(2, '2026-03-29 22:55:34.000000', 0, 'INV-20260329-002', 0, 6000, 6000, 1, 2, NULL, NULL, NULL),
(3, '2026-03-29 22:56:03.000000', 0, 'INV-20260329-003', 0, 6000, 6000, 2, 3, NULL, NULL, NULL),
(4, '2026-03-30 01:03:14.000000', 0, 'INV-20260330-001', 0, 0, 0, 5, 1, NULL, NULL, NULL),
(5, '2026-03-30 01:05:55.000000', 0, 'INV-20260330-002', 0, 0, 0, 4, 1, NULL, NULL, NULL),
(6, '2026-03-30 01:05:56.000000', 0, 'INV-20260330-003', 0, 0, 0, 12, 1, NULL, NULL, NULL),
(7, '2026-03-30 01:05:58.000000', 0, 'INV-20260330-004', 0, 0, 0, 13, 1, NULL, NULL, NULL),
(8, '2026-03-30 01:05:59.000000', 0, 'INV-20260330-005', 0, 0, 0, 6, 1, NULL, NULL, NULL),
(9, '2026-03-30 01:06:00.000000', 0, 'INV-20260330-006', 0, 0, 0, 7, 1, NULL, NULL, NULL),
(10, '2026-03-30 01:06:01.000000', 0, 'INV-20260330-007', 0, 0, 0, 8, 1, NULL, NULL, NULL),
(11, '2026-03-30 01:06:02.000000', 0, 'INV-20260330-008', 0, 0, 0, 9, 1, NULL, NULL, NULL),
(12, '2026-03-30 01:06:03.000000', 0, 'INV-20260330-009', 0, 0, 0, 10, 1, NULL, NULL, NULL),
(13, '2026-03-30 01:06:04.000000', 0, 'INV-20260330-010', 0, 0, 0, 11, 1, NULL, NULL, NULL),
(14, '2026-03-31 20:22:04.000000', 0, 'INV-20260331-001', 0, 12, 12, 17, 1, 12, NULL, NULL),
(15, '2026-03-31 20:23:14.000000', 0, 'INV-20260331-002', 0, 0, 0, 18, 1, NULL, NULL, NULL),
(16, '2026-03-31 20:41:21.000000', 0, 'INV-20260331-003', 0, 1445334, 1445334, 14, 1, NULL, NULL, NULL),
(17, '2026-03-31 20:41:28.000000', 0, 'INV-20260331-004', 0, 1441334, 1441334, 15, 1, NULL, NULL, NULL),
(18, '2026-03-31 20:41:30.000000', 0, 'INV-20260331-005', 0, 1361334, 1361334, 16, 1, NULL, NULL, NULL),
(19, '2026-03-31 20:42:43.000000', 0, 'INV-20260331-006', 0, 0, 0, 19, 1, NULL, NULL, NULL),
(20, '2026-03-31 20:42:58.000000', 0, 'INV-20260331-007', 0, 1, 1, 21, 1, 1, NULL, NULL),
(21, '2026-03-31 20:42:59.000000', 0, 'INV-20260331-008', 0, 0, 0, 20, 1, NULL, NULL, NULL),
(22, '2026-03-31 21:27:42.000000', 0, 'INV-20260331-009', 0, 2667, 2134, 22, 1, NULL, 533, 1),
(23, '2026-03-31 21:28:11.000000', 0, 'INV-20260331-010', 0, 10000, 8000, 23, 1, 10000, 2000, 1),
(24, '2026-04-01 01:01:55.000000', 0, 'INV-20260401-001', 0, 281334, 225068, 24, 1, NULL, 56266, 1),
(25, '2026-04-01 01:02:46.000000', 0, 'INV-20260401-002', 0, 0, 0, 25, 1, NULL, 0, 3),
(26, '2026-04-01 01:12:54.000000', 0, 'INV-20260401-003', 0, 13334, 13201, 26, 1, NULL, 133, 3),
(27, '2026-04-01 01:23:53.000000', 0, 'INV-20260401-004', 0, 0, 0, 27, 2, NULL, 0, NULL),
(28, '2026-04-01 18:16:37.000000', 0, 'INV-20260401-005', 0, 17334, 17334, 28, 1, NULL, 0, NULL),
(29, '2026-04-01 20:45:51.000000', 0, 'INV-20260401-006', 0, 198667, 198667, 29, 1, NULL, 0, NULL),
(30, '2026-04-01 20:55:52.000000', 0, 'INV-20260401-007', 0, 9334, 9334, 30, 1, NULL, 0, NULL),
(31, '2026-04-02 20:18:20.000000', 0, 'INV-20260402-001', 0, 2116667, 2116667, 31, 1, NULL, 0, NULL),
(32, '2026-04-02 20:34:00.000000', 0, 'INV-20260402-002', 0, 20000, 20000, 32, 1, NULL, 0, NULL),
(33, '2026-04-02 20:35:58.000000', 0, 'INV-20260402-003', 0, 1334, 1334, 33, 1, NULL, 0, NULL),
(34, '2026-04-02 21:59:55.000000', 0, 'INV-20260402-004', 0, 94500, 94500, 34, 1, NULL, 0, NULL),
(35, '2026-04-02 22:58:07.000000', 0, 'INV-20260402-005', 35000, 66500, 101500, 35, 2, NULL, 0, NULL),
(36, '2026-04-02 23:07:07.000000', 0, 'INV-20260402-006', 40000, 1334, 41334, 36, 1, NULL, 0, NULL),
(37, '2026-04-02 23:12:16.000000', 0, 'INV-20260402-007', 0, 0, 0, 37, 2, NULL, 0, NULL),
(38, '2026-04-02 23:18:45.000000', 0, 'INV-20260402-008', 0, 0, 0, 38, 1, NULL, 0, NULL),
(39, '2026-04-02 23:19:10.000000', 0, 'INV-20260402-009', 0, 0, 0, 39, 1, NULL, 0, NULL),
(40, '2026-04-02 23:46:31.000000', 0, 'INV-20260402-010', 0, 0, 0, 40, 1, NULL, 0, NULL),
(41, '2026-04-03 00:02:04.000000', 0, 'INV-20260403-001', 0, 0, 0, 41, 1, NULL, 0, NULL),
(42, '2026-04-03 18:25:05.000000', 0, 'INV-20260403-002', 0, 0, 0, 42, 1, 0, 0, NULL),
(43, '2026-04-03 18:25:51.000000', 0, 'INV-20260403-003', 48000, 0, 48000, 43, 1, 0, 0, NULL),
(44, '2026-04-04 00:49:26.000000', 0, 'INV-20260404-001', 30000, 3000, 33000, 44, 1, NULL, 0, NULL),
(45, '2026-04-04 00:54:01.000000', 0, 'INV-20260404-002', 0, 150000, 150000, 45, 1, 150000, 0, NULL),
(46, '2026-04-04 01:10:59.000000', 0, 'INV-20260404-003', 0, 150000, 150000, 47, 1, 150000, 0, NULL),
(47, '2026-04-04 12:44:27.000000', 0, 'INV-20260404-004', 35000, 806667, 673334, 46, 1, NULL, 168333, 1),
(48, '2026-04-06 20:23:42.000000', 403783, 'INV-20260406-001', 0, 4037834, 3634051, 49, 1, NULL, 0, NULL),
(49, '2026-04-06 20:34:06.000000', 0, 'INV-20260406-002', 95000, 3528667, 3623667, 48, 1, NULL, 0, NULL),
(50, '2026-04-06 21:43:28.000000', 0, 'INV-20260406-003', 0, 0, 0, 52, 1, NULL, 0, NULL),
(51, '2026-04-07 01:01:25.000000', 0, 'INV-20260407-001', 24000, 423334, 447334, 51, 2, NULL, 0, NULL),
(52, '2026-04-07 01:01:39.000000', 0, 'INV-20260407-002', 0, 423334, 338668, 50, 2, NULL, 84666, 1),
(53, '2026-04-07 01:02:02.000000', 43200, 'INV-20260407-003', 288000, 0, 244800, 53, 2, NULL, 0, NULL),
(54, '2026-04-07 01:02:13.000000', 0, 'INV-20260407-004', 0, 0, 0, 54, 2, NULL, 0, NULL),
(55, '2026-04-07 01:04:40.000000', 0, 'INV-20260407-005', 0, 0, 0, 55, 2, NULL, 0, NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `order_items`
--

CREATE TABLE `order_items` (
  `id` bigint(20) NOT NULL,
  `amount` decimal(12,0) NOT NULL,
  `ordered_at` datetime(6) NOT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(10,0) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `session_id` bigint(20) NOT NULL,
  `staff_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `order_items`
--

INSERT INTO `order_items` (`id`, `amount`, `ordered_at`, `quantity`, `unit_price`, `product_id`, `session_id`, `staff_id`) VALUES
(1, 20000, '2026-04-02 22:57:58.000000', 1, 20000, 13, 35, 2),
(2, 15000, '2026-04-02 22:58:06.000000', 1, 15000, 8, 35, 2),
(3, 40000, '2026-04-02 23:07:00.000000', 2, 20000, 13, 36, 1),
(4, 48000, '2026-04-03 18:22:38.000000', 2, 24000, 1, 43, 1),
(5, 30000, '2026-04-04 00:47:41.000000', 2, 15000, 8, 44, 1),
(6, 35000, '2026-04-04 12:41:13.000000', 1, 35000, 10, 46, 1),
(7, 20000, '2026-04-06 20:28:24.000000', 1, 20000, 13, 48, 1),
(8, 20000, '2026-04-06 20:28:35.000000', 1, 20000, 2, 48, 1),
(9, 15000, '2026-04-06 20:29:16.000000', 1, 15000, 3, 48, 1),
(10, 20000, '2026-04-06 20:29:36.000000', 1, 20000, 7, 48, 1),
(11, 20000, '2026-04-06 20:30:03.000000', 1, 20000, 7, 48, 1),
(13, 24000, '2026-04-06 23:41:01.000000', 1, 24000, 1, 51, 1),
(14, 288000, '2026-04-07 01:01:59.000000', 12, 24000, 1, 53, 2);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `price_rules`
--

CREATE TABLE `price_rules` (
  `id` bigint(20) NOT NULL,
  `day_type` enum('WEEKDAY','WEEKEND','HOLIDAY') NOT NULL,
  `end_time` time(6) NOT NULL,
  `price_per_hour` decimal(10,0) NOT NULL,
  `start_time` time(6) NOT NULL,
  `table_type` enum('POOL','CAROM','VIP') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `price_rules`
--

INSERT INTO `price_rules` (`id`, `day_type`, `end_time`, `price_per_hour`, `start_time`, `table_type`) VALUES
(29, 'WEEKDAY', '17:00:00.000000', 50000, '12:00:00.000000', 'POOL'),
(30, 'WEEKDAY', '23:00:00.000000', 70000, '17:00:00.000000', 'POOL'),
(31, 'WEEKEND', '12:00:00.000000', 50000, '08:00:00.000000', 'POOL'),
(32, 'WEEKEND', '17:00:00.000000', 60000, '12:00:00.000000', 'POOL'),
(33, 'WEEKEND', '23:00:00.000000', 80000, '17:00:00.000000', 'POOL'),
(34, 'HOLIDAY', '12:00:00.000000', 60000, '08:00:00.000000', 'POOL'),
(35, 'HOLIDAY', '17:00:00.000000', 70000, '12:00:00.000000', 'POOL'),
(36, 'HOLIDAY', '23:00:00.000000', 90000, '17:00:00.000000', 'POOL'),
(37, 'WEEKDAY', '12:00:00.000000', 50000, '08:00:00.000000', 'CAROM'),
(38, 'WEEKDAY', '17:00:00.000000', 60000, '12:00:00.000000', 'CAROM'),
(39, 'WEEKDAY', '23:00:00.000000', 80000, '17:00:00.000000', 'CAROM'),
(40, 'WEEKEND', '12:00:00.000000', 60000, '08:00:00.000000', 'CAROM'),
(41, 'WEEKEND', '17:00:00.000000', 70000, '12:00:00.000000', 'CAROM'),
(42, 'WEEKEND', '23:00:00.000000', 90000, '17:00:00.000000', 'CAROM'),
(43, 'HOLIDAY', '12:00:00.000000', 70000, '08:00:00.000000', 'CAROM'),
(44, 'HOLIDAY', '17:00:00.000000', 80000, '12:00:00.000000', 'CAROM'),
(45, 'HOLIDAY', '23:00:00.000000', 100000, '17:00:00.000000', 'CAROM'),
(46, 'WEEKDAY', '12:00:00.000000', 70000, '08:00:00.000000', 'VIP'),
(47, 'WEEKDAY', '17:00:00.000000', 80000, '12:00:00.000000', 'VIP'),
(48, 'WEEKDAY', '23:00:00.000000', 100000, '17:00:00.000000', 'VIP'),
(49, 'WEEKEND', '12:00:00.000000', 80000, '08:00:00.000000', 'VIP'),
(50, 'WEEKEND', '17:00:00.000000', 90000, '12:00:00.000000', 'VIP'),
(51, 'WEEKEND', '23:00:00.000000', 110000, '17:00:00.000000', 'VIP'),
(52, 'HOLIDAY', '12:00:00.000000', 90000, '08:00:00.000000', 'VIP'),
(53, 'HOLIDAY', '17:00:00.000000', 100000, '12:00:00.000000', 'VIP'),
(54, 'HOLIDAY', '23:00:00.000000', 120000, '17:00:00.000000', 'VIP'),
(55, 'WEEKDAY', '08:00:00.000000', 70000, '23:00:00.000000', 'POOL'),
(56, 'WEEKEND', '08:00:00.000000', 80000, '23:00:00.000000', 'POOL'),
(57, 'HOLIDAY', '08:00:00.000000', 90000, '23:00:00.000000', 'POOL'),
(58, 'WEEKDAY', '08:00:00.000000', 80000, '23:00:00.000000', 'CAROM'),
(59, 'WEEKEND', '08:00:00.000000', 90000, '23:00:00.000000', 'CAROM'),
(60, 'HOLIDAY', '08:00:00.000000', 100000, '23:00:00.000000', 'CAROM'),
(61, 'WEEKDAY', '08:00:00.000000', 100000, '23:00:00.000000', 'VIP'),
(62, 'WEEKEND', '08:00:00.000000', 110000, '23:00:00.000000', 'VIP'),
(63, 'HOLIDAY', '08:00:00.000000', 120000, '23:00:00.000000', 'VIP'),
(64, 'WEEKDAY', '12:00:00.000000', 40000, '08:00:00.000000', 'POOL');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `products`
--

CREATE TABLE `products` (
  `id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `category` enum('DRINK','FOOD','SNACK','OTHER') NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `price` decimal(10,0) NOT NULL,
  `stock_quantity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `products`
--

INSERT INTO `products` (`id`, `active`, `category`, `created_at`, `image_url`, `name`, `price`, `stock_quantity`) VALUES
(1, b'1', 'DRINK', '2026-03-29 22:50:41.000000', 'https://cdnv2.tgdd.vn/bhx-static/bhx/Products/Images/2282/328901/bhx/httpscdnv2tgddvnbhx-staticbhxproductsimages2282328901bhxlon-250ml202412031318572325_202412041000314324.jpg', 'Bia Tiger', 24000, 0),
(2, b'1', 'DRINK', '2026-03-29 22:50:41.000000', '', 'Bia Saigon', 20000, 0),
(3, b'1', 'DRINK', '2026-03-29 22:50:41.000000', NULL, 'Coca Cola', 15000, 49),
(4, b'1', 'DRINK', '2026-03-29 22:50:41.000000', NULL, 'Pepsi', 15000, 50),
(5, b'1', 'DRINK', '2026-03-29 22:50:41.000000', NULL, 'Nuoc suoi', 10000, 80),
(6, b'1', 'DRINK', '2026-03-29 22:50:41.000000', NULL, 'Tra da', 5000, 200),
(7, b'1', 'DRINK', '2026-03-29 22:50:41.000000', NULL, 'Ca phe sua da', 20000, 48),
(8, b'1', 'DRINK', '2026-03-29 22:50:41.000000', NULL, 'Red Bull', 15000, 27),
(9, b'1', 'FOOD', '2026-03-29 22:50:41.000000', NULL, 'Mi tom', 25000, 30),
(10, b'1', 'FOOD', '2026-03-29 22:50:41.000000', NULL, 'Com chien', 35000, 19),
(11, b'1', 'SNACK', '2026-03-29 22:50:41.000000', NULL, 'Dau phong', 15000, 40),
(12, b'1', 'SNACK', '2026-03-29 22:50:41.000000', NULL, 'Kho bo', 30000, 25),
(13, b'1', 'SNACK', '2026-03-29 22:50:41.000000', '', 'Banh trang trung', 20000, 10),
(14, b'1', 'OTHER', '2026-03-29 22:50:41.000000', NULL, 'Thuoc la', 25000, 50),
(17, b'1', 'DRINK', '2026-04-04 01:16:18.000000', '', 'Heineken', 25000, 20);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reservations`
--

CREATE TABLE `reservations` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_name` varchar(255) NOT NULL,
  `customer_phone` varchar(255) DEFAULT NULL,
  `duration_minutes` int(11) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `reserved_time` datetime(6) NOT NULL,
  `status` enum('PENDING','CONFIRMED','CANCELLED','COMPLETED') NOT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `table_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `reservations`
--

INSERT INTO `reservations` (`id`, `created_at`, `customer_name`, `customer_phone`, `duration_minutes`, `note`, `reserved_time`, `status`, `staff_id`, `table_id`) VALUES
(1, '2026-04-06 21:12:47.000000', 'Tran Van B', '0987654322', 60, '', '2026-05-07 15:00:00.000000', 'COMPLETED', 1, 2),
(2, '2026-04-06 21:23:14.000000', 'Le Thi C', '0987654323', 60, '', '2026-04-06 10:00:00.000000', 'CANCELLED', 1, 5);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `schedule_audit_logs`
--

CREATE TABLE `schedule_audit_logs` (
  `id` bigint(20) NOT NULL,
  `action` varchar(20) NOT NULL,
  `details` varchar(1000) DEFAULT NULL,
  `performed_at` datetime(6) NOT NULL,
  `performed_by` varchar(255) NOT NULL,
  `schedule_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `schedule_audit_logs`
--

INSERT INTO `schedule_audit_logs` (`id`, `action`, `details`, `performed_at`, `performed_by`, `schedule_id`) VALUES
(1, 'CREATED', 'Xep lich cho Nhân viên 1 ca ca sáng ngay 2026-04-07', '2026-04-07 00:43:23.000000', 'admin', 5),
(2, 'CREATED', 'Xep lich cho Nhân viên 2 ca ca tối ngay 2026-04-07', '2026-04-07 00:43:34.000000', 'admin', 6),
(3, 'CREATED', 'Xep lich cho Nhân viên 1 ca Ca sang ngay 2026-04-07', '2026-04-07 00:48:14.000000', 'admin', 7),
(4, 'CREATED', 'Xep lich cho Nhân viên 1 ca Ca chieu ngay 2026-04-07', '2026-04-07 00:48:18.000000', 'admin', 8),
(5, 'DELETED', 'Xoa lich cua Nhân viên 2 ca ca tối ngay 2026-04-07', '2026-04-07 00:48:24.000000', 'admin', 6),
(6, 'DELETED', 'Xoa lich cua Nhân viên 1 ca Ca chieu ngay 2026-04-07', '2026-04-07 00:48:29.000000', 'admin', 8),
(7, 'DELETED', 'Xoa lich cua Nhân viên 1 ca ca sáng ngay 2026-04-07', '2026-04-07 00:54:09.000000', 'admin', 5),
(8, 'DELETED', 'Xoa lich cua Nhân viên 1 ca Ca sang ngay 2026-04-07', '2026-04-07 00:54:10.000000', 'admin', 7),
(9, 'CREATED', 'Xep lich cho Nhân viên 1 ca ca quỷ ngay 2026-04-07', '2026-04-07 00:54:14.000000', 'admin', 9),
(10, 'CHECKED_IN', 'Check-in luc 2026-04-07T00:57:32.847695900', '2026-04-07 00:57:32.000000', 'staff1', 9),
(11, 'CHECKED_OUT', 'Check-out luc 2026-04-07T00:57:38.093672600', '2026-04-07 00:57:38.000000', 'staff1', 9),
(12, 'CREATED', 'Xep lich cho Quản lý ca ca sáng ngay 2026-04-07', '2026-04-07 00:58:01.000000', 'admin', 10);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `sessions`
--

CREATE TABLE `sessions` (
  `id` bigint(20) NOT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `start_time` datetime(6) NOT NULL,
  `status` enum('ACTIVE','COMPLETED') NOT NULL,
  `total_amount` decimal(12,0) DEFAULT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `table_id` bigint(20) NOT NULL,
  `pause_start` datetime(6) DEFAULT NULL,
  `total_paused_minutes` int(11) NOT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `original_table_type` enum('POOL','CAROM','VIP') DEFAULT NULL,
  `transferred_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `sessions`
--

INSERT INTO `sessions` (`id`, `end_time`, `start_time`, `status`, `total_amount`, `staff_id`, `table_id`, `pause_start`, `total_paused_minutes`, `customer_id`, `original_table_type`, `transferred_at`) VALUES
(1, '2026-03-29 22:55:34.000000', '2026-03-29 22:51:08.000000', 'COMPLETED', 6000, 1, 6, NULL, 0, NULL, NULL, NULL),
(2, '2026-03-29 22:56:03.000000', '2026-03-29 22:51:17.000000', 'COMPLETED', 6000, 1, 5, NULL, 0, NULL, NULL, NULL),
(3, '2026-03-29 22:51:27.000000', '2026-03-29 22:51:18.000000', 'COMPLETED', 0, 1, 7, NULL, 0, NULL, NULL, NULL),
(4, '2026-03-30 01:05:55.000000', '2026-03-29 23:19:13.000000', 'COMPLETED', 0, 1, 6, NULL, 0, NULL, NULL, NULL),
(5, '2026-03-30 01:03:13.000000', '2026-03-29 23:21:04.000000', 'COMPLETED', 0, 1, 7, NULL, 0, NULL, NULL, NULL),
(6, '2026-03-30 01:05:59.000000', '2026-03-29 23:23:22.000000', 'COMPLETED', 0, 1, 8, NULL, 0, NULL, NULL, NULL),
(7, '2026-03-30 01:06:00.000000', '2026-03-29 23:23:23.000000', 'COMPLETED', 0, 1, 9, NULL, 0, NULL, NULL, NULL),
(8, '2026-03-30 01:06:01.000000', '2026-03-29 23:23:25.000000', 'COMPLETED', 0, 1, 10, NULL, 0, NULL, NULL, NULL),
(9, '2026-03-30 01:06:02.000000', '2026-03-29 23:23:26.000000', 'COMPLETED', 0, 1, 1, NULL, 0, NULL, NULL, NULL),
(10, '2026-03-30 01:06:03.000000', '2026-03-29 23:23:27.000000', 'COMPLETED', 0, 1, 2, NULL, 0, NULL, NULL, NULL),
(11, '2026-03-30 01:06:04.000000', '2026-03-29 23:23:29.000000', 'COMPLETED', 0, 1, 3, NULL, 0, NULL, NULL, NULL),
(12, '2026-03-30 01:05:56.000000', '2026-03-29 23:23:30.000000', 'COMPLETED', 0, 1, 5, NULL, 0, NULL, NULL, NULL),
(13, '2026-03-30 01:05:58.000000', '2026-03-29 23:23:32.000000', 'COMPLETED', 0, 1, 4, NULL, 0, NULL, NULL, NULL),
(14, '2026-03-31 20:41:21.000000', '2026-03-30 23:51:31.000000', 'COMPLETED', 1445334, 1, 7, NULL, 0, NULL, NULL, NULL),
(15, '2026-03-31 20:41:28.000000', '2026-03-30 23:54:10.000000', 'COMPLETED', 1441334, 1, 6, NULL, 0, NULL, NULL, NULL),
(16, '2026-03-31 20:41:30.000000', '2026-03-31 00:54:46.000000', 'COMPLETED', 1361334, 1, 5, NULL, 0, NULL, NULL, NULL),
(17, '2026-03-31 20:22:04.000000', '2026-03-31 20:21:52.000000', 'COMPLETED', 0, 1, 1, NULL, 0, NULL, NULL, NULL),
(18, '2026-03-31 20:23:14.000000', '2026-03-31 20:23:13.000000', 'COMPLETED', 0, 1, 1, NULL, 0, NULL, NULL, NULL),
(19, '2026-03-31 20:42:43.000000', '2026-03-31 20:42:41.000000', 'COMPLETED', 0, 1, 6, NULL, 0, NULL, NULL, NULL),
(20, '2026-03-31 20:42:59.000000', '2026-03-31 20:42:45.000000', 'COMPLETED', 0, 1, 6, NULL, 0, NULL, NULL, NULL),
(21, '2026-03-31 20:42:58.000000', '2026-03-31 20:42:53.000000', 'COMPLETED', 0, 1, 7, NULL, 0, NULL, NULL, NULL),
(22, '2026-03-31 21:27:42.000000', '2026-03-31 21:25:27.000000', 'COMPLETED', 2667, 1, 6, NULL, 0, NULL, NULL, NULL),
(23, '2026-03-31 21:28:10.000000', '2026-03-31 21:27:54.000000', 'COMPLETED', 0, 1, 14, NULL, 0, NULL, NULL, NULL),
(24, '2026-04-01 01:01:54.000000', '2026-03-31 21:29:02.000000', 'COMPLETED', 281334, 1, 6, NULL, 0, NULL, NULL, NULL),
(25, '2026-04-01 01:02:46.000000', '2026-04-01 01:02:00.000000', 'COMPLETED', 0, 1, 6, NULL, 0, NULL, NULL, NULL),
(26, '2026-04-01 01:12:54.000000', '2026-04-01 01:02:02.000000', 'COMPLETED', 13334, 1, 14, NULL, 0, NULL, NULL, NULL),
(27, '2026-04-01 01:23:53.000000', '2026-04-01 01:23:11.000000', 'COMPLETED', 0, 2, 14, NULL, 0, NULL, NULL, NULL),
(28, '2026-04-01 18:16:37.000000', '2026-04-01 18:02:48.000000', 'COMPLETED', 17334, 1, 14, NULL, 0, NULL, NULL, NULL),
(29, '2026-04-01 20:45:51.000000', '2026-04-01 18:16:50.000000', 'COMPLETED', 198667, 1, 6, NULL, 0, NULL, NULL, NULL),
(30, '2026-04-01 20:55:52.000000', '2026-04-01 20:47:55.000000', 'COMPLETED', 9334, 1, 14, NULL, 0, NULL, NULL, NULL),
(31, '2026-04-02 20:18:20.000000', '2026-04-01 20:55:56.000000', 'COMPLETED', 2116667, 1, 14, NULL, 0, NULL, NULL, NULL),
(32, '2026-04-02 20:34:00.000000', '2026-04-02 20:18:52.000000', 'COMPLETED', 20000, 1, 5, NULL, 0, NULL, NULL, NULL),
(33, '2026-04-02 20:35:58.000000', '2026-04-02 20:34:41.000000', 'COMPLETED', 1334, 1, 5, NULL, 0, NULL, NULL, NULL),
(34, '2026-04-02 21:59:54.000000', '2026-04-02 20:38:08.000000', 'COMPLETED', 94500, 1, 1, NULL, 0, NULL, NULL, NULL),
(35, '2026-04-02 22:58:07.000000', '2026-04-02 22:00:19.000000', 'COMPLETED', 66500, 1, 5, NULL, 37, NULL, 'POOL', '2026-04-02 22:57:26.000000'),
(36, '2026-04-02 23:07:07.000000', '2026-04-02 23:05:10.000000', 'COMPLETED', 1334, 1, 5, NULL, 0, NULL, NULL, NULL),
(37, '2026-04-02 23:12:15.000000', '2026-04-02 23:12:12.000000', 'COMPLETED', 0, 2, 6, NULL, 0, NULL, NULL, NULL),
(38, '2026-04-02 23:18:45.000000', '2026-04-02 23:18:43.000000', 'COMPLETED', 0, 1, 6, NULL, 0, NULL, NULL, NULL),
(39, '2026-04-02 23:19:10.000000', '2026-04-02 23:19:09.000000', 'COMPLETED', 0, 1, 14, NULL, 0, NULL, NULL, NULL),
(40, '2026-04-02 23:46:31.000000', '2026-04-02 23:46:29.000000', 'COMPLETED', 0, 1, 6, NULL, 0, NULL, NULL, NULL),
(41, '2026-04-03 00:02:04.000000', '2026-04-03 00:02:03.000000', 'COMPLETED', 0, 1, 6, NULL, 0, NULL, NULL, NULL),
(42, '2026-04-03 18:25:05.000000', '2026-04-03 18:13:14.000000', 'COMPLETED', 12834, 1, 2, NULL, 0, NULL, 'POOL', '2026-04-03 18:16:38.000000'),
(43, '2026-04-03 18:25:51.000000', '2026-04-03 18:22:05.000000', 'COMPLETED', 3500, 1, 2, NULL, 0, NULL, 'POOL', '2026-04-03 18:25:19.000000'),
(44, '2026-04-04 00:49:26.000000', '2026-04-04 00:46:41.000000', 'COMPLETED', 3000, 1, 6, NULL, 0, NULL, 'CAROM', '2026-04-04 00:48:21.000000'),
(45, '2026-04-04 00:54:01.000000', '2026-04-04 00:53:18.000000', 'COMPLETED', 0, 1, 1, NULL, 0, NULL, NULL, NULL),
(46, '2026-04-04 12:44:27.000000', '2026-04-04 00:57:26.000000', 'COMPLETED', 806667, 1, 4, NULL, 0, NULL, NULL, NULL),
(47, '2026-04-04 01:10:58.000000', '2026-04-04 01:10:09.000000', 'COMPLETED', 0, 1, 1, NULL, 0, NULL, NULL, NULL),
(48, '2026-04-06 20:34:06.000000', '2026-04-04 16:10:17.000000', 'COMPLETED', 3528667, 1, 5, NULL, 0, NULL, 'POOL', '2026-04-06 20:34:01.000000'),
(49, '2026-04-06 20:23:42.000000', '2026-04-04 16:10:25.000000', 'COMPLETED', 4037834, 1, 6, NULL, 0, 3, NULL, NULL),
(50, '2026-04-07 01:01:39.000000', '2026-04-06 20:46:15.000000', 'COMPLETED', 423334, 1, 10, NULL, 0, NULL, NULL, NULL),
(51, '2026-04-07 01:01:25.000000', '2026-04-06 20:46:29.000000', 'COMPLETED', 423334, 1, 9, NULL, 0, NULL, NULL, NULL),
(52, '2026-04-06 21:43:28.000000', '2026-04-06 21:43:20.000000', 'COMPLETED', 0, 1, 2, NULL, 0, NULL, NULL, NULL),
(53, '2026-04-07 01:02:02.000000', '2026-04-07 01:01:51.000000', 'COMPLETED', 0, 2, 8, NULL, 0, 3, NULL, NULL),
(54, '2026-04-07 01:02:13.000000', '2026-04-07 01:02:09.000000', 'COMPLETED', 0, 2, 6, NULL, 0, NULL, NULL, NULL),
(55, '2026-04-07 01:04:40.000000', '2026-04-07 01:04:37.000000', 'COMPLETED', 0, 2, 6, NULL, 0, NULL, NULL, NULL),
(56, NULL, '2026-04-07 01:05:41.000000', 'ACTIVE', 0, 1, 3, NULL, 0, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `session_segments`
--

CREATE TABLE `session_segments` (
  `id` bigint(20) NOT NULL,
  `amount` decimal(12,0) NOT NULL,
  `duration_minutes` int(11) NOT NULL,
  `end_time` datetime(6) NOT NULL,
  `start_time` datetime(6) NOT NULL,
  `price_rule_id` bigint(20) DEFAULT NULL,
  `session_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `session_segments`
--

INSERT INTO `session_segments` (`id`, `amount`, `duration_minutes`, `end_time`, `start_time`, `price_rule_id`, `session_id`) VALUES
(1, 0, 0, '2026-03-29 22:51:27.000000', '2026-03-29 22:51:18.000000', NULL, 3),
(2, 6000, 4, '2026-03-29 22:55:34.000000', '2026-03-29 22:51:08.000000', NULL, 1),
(3, 6000, 4, '2026-03-29 22:56:03.000000', '2026-03-29 22:51:17.000000', NULL, 2),
(4, 0, 0, '2026-03-31 20:22:04.000000', '2026-03-31 20:21:52.000000', NULL, 17),
(5, 0, 0, '2026-03-31 20:23:14.000000', '2026-03-31 20:23:13.000000', NULL, 18),
(6, 650667, 488, '2026-03-31 08:00:00.000000', '2026-03-30 23:51:31.000000', NULL, 14),
(7, 200000, 240, '2026-03-31 12:00:00.000000', '2026-03-31 08:00:00.000000', NULL, 14),
(8, 300000, 300, '2026-03-31 17:00:00.000000', '2026-03-31 12:00:00.000000', NULL, 14),
(9, 294667, 221, '2026-03-31 20:41:21.000000', '2026-03-31 17:00:00.000000', NULL, 14),
(10, 646667, 485, '2026-03-31 08:00:00.000000', '2026-03-30 23:54:10.000000', NULL, 15),
(11, 200000, 240, '2026-03-31 12:00:00.000000', '2026-03-31 08:00:00.000000', NULL, 15),
(12, 300000, 300, '2026-03-31 17:00:00.000000', '2026-03-31 12:00:00.000000', NULL, 15),
(13, 294667, 221, '2026-03-31 20:41:28.000000', '2026-03-31 17:00:00.000000', NULL, 15),
(14, 566667, 425, '2026-03-31 08:00:00.000000', '2026-03-31 00:54:46.000000', NULL, 16),
(15, 200000, 240, '2026-03-31 12:00:00.000000', '2026-03-31 08:00:00.000000', NULL, 16),
(16, 300000, 300, '2026-03-31 17:00:00.000000', '2026-03-31 12:00:00.000000', NULL, 16),
(17, 294667, 221, '2026-03-31 20:41:30.000000', '2026-03-31 17:00:00.000000', NULL, 16),
(18, 0, 0, '2026-03-31 20:42:43.000000', '2026-03-31 20:42:41.000000', NULL, 19),
(19, 0, 0, '2026-03-31 20:42:58.000000', '2026-03-31 20:42:53.000000', NULL, 21),
(20, 0, 0, '2026-03-31 20:42:59.000000', '2026-03-31 20:42:45.000000', NULL, 20),
(21, 2667, 2, '2026-03-31 21:27:42.000000', '2026-03-31 21:25:27.000000', NULL, 22),
(22, 0, 0, '2026-03-31 21:28:10.000000', '2026-03-31 21:27:54.000000', NULL, 23),
(25, 120000, 90, '2026-03-31 23:00:00.000000', '2026-03-31 21:29:02.000000', NULL, 24),
(26, 161334, 121, '2026-04-01 01:01:54.000000', '2026-03-31 23:00:00.000000', NULL, 24),
(27, 0, 0, '2026-04-01 01:02:46.000000', '2026-04-01 01:02:00.000000', NULL, 25),
(28, 13334, 10, '2026-04-01 01:12:54.000000', '2026-04-01 01:02:02.000000', NULL, 26),
(29, 0, 0, '2026-04-01 01:23:53.000000', '2026-04-01 01:23:11.000000', NULL, 27),
(30, 17334, 13, '2026-04-01 18:16:37.000000', '2026-04-01 18:02:48.000000', NULL, 28),
(31, 198667, 149, '2026-04-01 20:45:51.000000', '2026-04-01 18:16:50.000000', NULL, 29),
(32, 9334, 7, '2026-04-01 20:55:52.000000', '2026-04-01 20:47:55.000000', NULL, 30),
(33, 206667, 124, '2026-04-01 23:00:00.000000', '2026-04-01 20:55:56.000000', NULL, 31),
(34, 100000, 60, '2026-04-02 00:00:00.000000', '2026-04-01 23:00:00.000000', NULL, 31),
(35, 800000, 480, '2026-04-02 08:00:00.000000', '2026-04-02 00:00:00.000000', NULL, 31),
(36, 280000, 240, '2026-04-02 12:00:00.000000', '2026-04-02 08:00:00.000000', NULL, 31),
(37, 400000, 300, '2026-04-02 17:00:00.000000', '2026-04-02 12:00:00.000000', NULL, 31),
(38, 330000, 198, '2026-04-02 20:18:20.000000', '2026-04-02 17:00:00.000000', NULL, 31),
(39, 20000, 15, '2026-04-02 20:34:00.000000', '2026-04-02 20:18:52.000000', NULL, 32),
(40, 1334, 1, '2026-04-02 20:35:58.000000', '2026-04-02 20:34:41.000000', NULL, 33),
(41, 94500, 81, '2026-04-02 21:59:54.000000', '2026-04-02 20:38:08.000000', NULL, 34),
(42, 66500, 57, '2026-04-02 22:57:26.000000', '2026-04-02 22:00:19.000000', NULL, 35),
(43, 0, 0, '2026-04-02 22:58:07.000000', '2026-04-02 22:57:26.000000', NULL, 35),
(44, 1334, 1, '2026-04-02 23:07:07.000000', '2026-04-02 23:05:10.000000', NULL, 36),
(45, 0, 0, '2026-04-02 23:12:15.000000', '2026-04-02 23:12:12.000000', NULL, 37),
(46, 0, 0, '2026-04-02 23:18:45.000000', '2026-04-02 23:18:43.000000', NULL, 38),
(47, 0, 0, '2026-04-02 23:19:10.000000', '2026-04-02 23:19:09.000000', NULL, 39),
(48, 0, 0, '2026-04-02 23:46:31.000000', '2026-04-02 23:46:29.000000', NULL, 40),
(49, 0, 0, '2026-04-03 00:02:04.000000', '2026-04-03 00:02:03.000000', NULL, 41),
(50, 3500, 3, '2026-04-03 18:16:38.000000', '2026-04-03 18:13:14.000000', NULL, 42),
(51, 9334, 8, '2026-04-03 18:25:05.000000', '2026-04-03 18:16:38.000000', NULL, 42),
(52, 3500, 3, '2026-04-03 18:25:19.000000', '2026-04-03 18:22:05.000000', NULL, 43),
(53, 0, 0, '2026-04-03 18:25:51.000000', '2026-04-03 18:25:19.000000', NULL, 43),
(54, 1500, 1, '2026-04-04 00:48:21.000000', '2026-04-04 00:46:41.000000', NULL, 44),
(55, 1500, 1, '2026-04-04 00:49:26.000000', '2026-04-04 00:48:21.000000', NULL, 44),
(56, 0, 0, '2026-04-04 00:54:01.000000', '2026-04-04 00:53:18.000000', NULL, 45),
(57, 0, 0, '2026-04-04 01:10:58.000000', '2026-04-04 01:10:09.000000', NULL, 47),
(58, 562667, 422, '2026-04-04 08:00:00.000000', '2026-04-04 00:57:26.000000', NULL, 46),
(59, 200000, 240, '2026-04-04 12:00:00.000000', '2026-04-04 08:00:00.000000', NULL, 46),
(60, 44000, 44, '2026-04-04 12:44:27.000000', '2026-04-04 12:00:00.000000', NULL, 46),
(61, 57167, 49, '2026-04-04 17:00:00.000000', '2026-04-04 16:10:25.000000', NULL, 49),
(62, 540000, 360, '2026-04-04 23:00:00.000000', '2026-04-04 17:00:00.000000', NULL, 49),
(63, 90000, 60, '2026-04-05 00:00:00.000000', '2026-04-04 23:00:00.000000', NULL, 49),
(64, 720000, 480, '2026-04-05 08:00:00.000000', '2026-04-05 00:00:00.000000', NULL, 49),
(65, 240000, 240, '2026-04-05 12:00:00.000000', '2026-04-05 08:00:00.000000', NULL, 49),
(66, 350000, 300, '2026-04-05 17:00:00.000000', '2026-04-05 12:00:00.000000', NULL, 49),
(67, 540000, 360, '2026-04-05 23:00:00.000000', '2026-04-05 17:00:00.000000', NULL, 49),
(68, 90000, 60, '2026-04-06 00:00:00.000000', '2026-04-05 23:00:00.000000', NULL, 49),
(69, 640000, 480, '2026-04-06 08:00:00.000000', '2026-04-06 00:00:00.000000', NULL, 49),
(70, 200000, 240, '2026-04-06 12:00:00.000000', '2026-04-06 08:00:00.000000', NULL, 49),
(71, 300000, 300, '2026-04-06 17:00:00.000000', '2026-04-06 12:00:00.000000', NULL, 49),
(72, 270667, 203, '2026-04-06 20:23:42.000000', '2026-04-06 17:00:00.000000', NULL, 49),
(73, 49000, 49, '2026-04-04 17:00:00.000000', '2026-04-04 16:10:17.000000', NULL, 48),
(74, 480000, 360, '2026-04-04 23:00:00.000000', '2026-04-04 17:00:00.000000', NULL, 48),
(75, 80000, 60, '2026-04-05 00:00:00.000000', '2026-04-04 23:00:00.000000', NULL, 48),
(76, 640000, 480, '2026-04-05 08:00:00.000000', '2026-04-05 00:00:00.000000', NULL, 48),
(77, 200000, 240, '2026-04-05 12:00:00.000000', '2026-04-05 08:00:00.000000', NULL, 48),
(78, 300000, 300, '2026-04-05 17:00:00.000000', '2026-04-05 12:00:00.000000', NULL, 48),
(79, 480000, 360, '2026-04-05 23:00:00.000000', '2026-04-05 17:00:00.000000', NULL, 48),
(80, 80000, 60, '2026-04-06 00:00:00.000000', '2026-04-05 23:00:00.000000', NULL, 48),
(81, 560000, 480, '2026-04-06 08:00:00.000000', '2026-04-06 00:00:00.000000', NULL, 48),
(82, 160000, 240, '2026-04-06 12:00:00.000000', '2026-04-06 08:00:00.000000', NULL, 48),
(83, 250000, 300, '2026-04-06 17:00:00.000000', '2026-04-06 12:00:00.000000', NULL, 48),
(84, 249667, 214, '2026-04-06 20:34:01.000000', '2026-04-06 17:00:00.000000', NULL, 48),
(85, 0, 0, '2026-04-06 20:34:06.000000', '2026-04-06 20:34:01.000000', NULL, 48),
(86, 0, 0, '2026-04-06 21:43:28.000000', '2026-04-06 21:43:20.000000', NULL, 52),
(87, 221667, 133, '2026-04-06 23:00:00.000000', '2026-04-06 20:46:29.000000', NULL, 51),
(88, 100000, 60, '2026-04-07 00:00:00.000000', '2026-04-06 23:00:00.000000', NULL, 51),
(89, 101667, 61, '2026-04-07 01:01:25.000000', '2026-04-07 00:00:00.000000', NULL, 51),
(90, 221667, 133, '2026-04-06 23:00:00.000000', '2026-04-06 20:46:15.000000', NULL, 50),
(91, 100000, 60, '2026-04-07 00:00:00.000000', '2026-04-06 23:00:00.000000', NULL, 50),
(92, 101667, 61, '2026-04-07 01:01:39.000000', '2026-04-07 00:00:00.000000', NULL, 50),
(93, 0, 0, '2026-04-07 01:02:02.000000', '2026-04-07 01:01:51.000000', NULL, 53),
(94, 0, 0, '2026-04-07 01:02:13.000000', '2026-04-07 01:02:09.000000', NULL, 54),
(95, 0, 0, '2026-04-07 01:04:40.000000', '2026-04-07 01:04:37.000000', NULL, 55);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `shifts`
--

CREATE TABLE `shifts` (
  `id` bigint(20) NOT NULL,
  `end_time` time(6) NOT NULL,
  `name` varchar(255) NOT NULL,
  `start_time` time(6) NOT NULL,
  `max_staff` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `shifts`
--

INSERT INTO `shifts` (`id`, `end_time`, `name`, `start_time`, `max_staff`) VALUES
(1, '14:00:00.000000', 'Ca sang', '08:00:00.000000', 3),
(2, '20:00:00.000000', 'Ca chieu', '14:00:00.000000', 3),
(3, '23:00:00.000000', 'Ca toi', '20:00:00.000000', NULL),
(4, '03:45:00.000000', 'ca sáng', '01:45:00.000000', 3),
(5, '13:04:00.000000', 'ca tối', '13:39:00.000000', 3),
(6, '16:00:00.000000', 'ca quỷ', '00:56:00.000000', 2);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `shift_closings`
--

CREATE TABLE `shift_closings` (
  `id` bigint(20) NOT NULL,
  `actual_cash` decimal(12,0) NOT NULL,
  `closing_time` datetime(6) NOT NULL,
  `discrepancy` decimal(12,0) NOT NULL,
  `note` varchar(500) DEFAULT NULL,
  `system_revenue` decimal(12,0) NOT NULL,
  `shift_id` bigint(20) NOT NULL,
  `staff_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `shift_closings`
--

INSERT INTO `shift_closings` (`id`, `actual_cash`, `closing_time`, `discrepancy`, `note`, `system_revenue`, `shift_id`, `staff_id`) VALUES
(1, 500000, '2026-04-03 18:28:55.000000', 500000, 'Chốt ca trực tiếp từ Postman', 0, 1, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `staff_schedules`
--

CREATE TABLE `staff_schedules` (
  `id` bigint(20) NOT NULL,
  `check_in_time` datetime(6) DEFAULT NULL,
  `check_out_time` datetime(6) DEFAULT NULL,
  `date` date NOT NULL,
  `status` enum('SCHEDULED','CHECKED_IN','CHECKED_OUT','ABSENT') NOT NULL,
  `shift_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `staff_schedules`
--

INSERT INTO `staff_schedules` (`id`, `check_in_time`, `check_out_time`, `date`, `status`, `shift_id`, `user_id`) VALUES
(1, NULL, NULL, '2026-03-30', 'SCHEDULED', 4, 2),
(2, NULL, NULL, '2026-03-30', 'SCHEDULED', 3, 3),
(3, NULL, NULL, '2026-04-01', 'SCHEDULED', 4, 2),
(4, NULL, NULL, '2026-04-01', 'SCHEDULED', 5, 3),
(9, '2026-04-07 00:57:32.000000', '2026-04-07 00:57:38.000000', '2026-04-07', 'CHECKED_OUT', 6, 2),
(10, NULL, NULL, '2026-04-07', 'SCHEDULED', 4, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','STAFF') NOT NULL,
  `username` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `active`, `full_name`, `password`, `role`, `username`, `email`, `hire_date`, `phone`) VALUES
(1, b'1', 'Quản lý', '$2a$10$mqk2bfKyxPjOz1jWBt1m8O./DvWkLAsdHVsvEtNmtxx5pP6h4ETea', 'ADMIN', 'admin', NULL, NULL, NULL),
(2, b'1', 'Nhân viên 1', '$2a$10$zu7eO8O4qsV19yVFF.UOlO4obo/xcCfgHaPWUto0v0Tt3OB78Jp7u', 'STAFF', 'staff1', NULL, NULL, NULL),
(3, b'1', 'Nhân viên 2', '$2a$10$bTzZsxyK0vRSW9XxsUQ3.eibO7MSDyMmeggtWuX8BS7cm9IPhUwLO', 'STAFF', 'staff2', NULL, NULL, NULL),
(4, b'1', 'Test Staff', '$2a$12$5hDFC7I03NNMcPdL5jq..OEnOqyDJ9aq2nZJY54Nl1C/GR.LNjLKC', 'STAFF', 'staff_test', '', NULL, '');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `app_settings`
--
ALTER TABLE `app_settings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_7p82g7l6uve2vd8l30djhxpel` (`setting_key`);

--
-- Chỉ mục cho bảng `billiard_tables`
--
ALTER TABLE `billiard_tables`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_m3iom37efaxd5eucmxjqqcbe9` (`phone`);

--
-- Chỉ mục cho bảng `discount_codes`
--
ALTER TABLE `discount_codes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_ekkk9piidfon0rluedtqr82uv` (`code`);

--
-- Chỉ mục cho bảng `holiday_calendar`
--
ALTER TABLE `holiday_calendar`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_l1x55mfsay7co0r3m9ynvipd5` (`invoice_number`),
  ADD UNIQUE KEY `UK_ieprf1q2oymsua39y89sp0v0w` (`session_id`),
  ADD KEY `FKhqxowhwodj3k26gm7xhv13bhn` (`staff_id`),
  ADD KEY `FKqqmci4dq129yrg27n3ft2385u` (`discount_code_id`);

--
-- Chỉ mục cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  ADD KEY `FK6w4ephwm2mmgi7l741eox2thk` (`session_id`),
  ADD KEY `FKiv2147fa7ps0h4gr9wsx4jd4d` (`staff_id`);

--
-- Chỉ mục cho bảng `price_rules`
--
ALTER TABLE `price_rules`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKmrg28giy8epsya6odhkuwm2ga` (`table_type`,`day_type`,`start_time`);

--
-- Chỉ mục cho bảng `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKbbwn3lvynin5hrtiv7u6hteyy` (`staff_id`),
  ADD KEY `FKqgl6lluol2quheqoken2o8rt3` (`table_id`);

--
-- Chỉ mục cho bảng `schedule_audit_logs`
--
ALTER TABLE `schedule_audit_logs`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `sessions`
--
ALTER TABLE `sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKodq9hadc349jmv9jpkk8debvc` (`staff_id`),
  ADD KEY `FK4gfnsavu42v8xkmehp6ft3dg` (`table_id`),
  ADD KEY `FKm6knee6ksh93kdkx9mpo01pjw` (`customer_id`);

--
-- Chỉ mục cho bảng `session_segments`
--
ALTER TABLE `session_segments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK2llh5pfg2vofnokqb4x8rdkrn` (`price_rule_id`),
  ADD KEY `FKart0xia1nl3xg2ypvuqmawycc` (`session_id`);

--
-- Chỉ mục cho bảng `shifts`
--
ALTER TABLE `shifts`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `shift_closings`
--
ALTER TABLE `shift_closings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKci2tt40v2h83qqeov2onn26il` (`shift_id`),
  ADD KEY `FKhsen5alhh6xd5xj8xypkg45kb` (`staff_id`);

--
-- Chỉ mục cho bảng `staff_schedules`
--
ALTER TABLE `staff_schedules`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKacvp4a4kpk04lnbljhsfl80fq` (`shift_id`),
  ADD KEY `FKa8mao3r0dk938e4wu82tksui8` (`user_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `app_settings`
--
ALTER TABLE `app_settings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `billiard_tables`
--
ALTER TABLE `billiard_tables`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT cho bảng `customers`
--
ALTER TABLE `customers`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `discount_codes`
--
ALTER TABLE `discount_codes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `holiday_calendar`
--
ALTER TABLE `holiday_calendar`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT cho bảng `invoices`
--
ALTER TABLE `invoices`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- AUTO_INCREMENT cho bảng `order_items`
--
ALTER TABLE `order_items`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT cho bảng `price_rules`
--
ALTER TABLE `price_rules`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=65;

--
-- AUTO_INCREMENT cho bảng `products`
--
ALTER TABLE `products`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT cho bảng `reservations`
--
ALTER TABLE `reservations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `schedule_audit_logs`
--
ALTER TABLE `schedule_audit_logs`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT cho bảng `sessions`
--
ALTER TABLE `sessions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- AUTO_INCREMENT cho bảng `session_segments`
--
ALTER TABLE `session_segments`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=96;

--
-- AUTO_INCREMENT cho bảng `shifts`
--
ALTER TABLE `shifts`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT cho bảng `shift_closings`
--
ALTER TABLE `shift_closings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `staff_schedules`
--
ALTER TABLE `staff_schedules`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `FKhqxowhwodj3k26gm7xhv13bhn` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKpyk1gld13fn87pxq0xgfysnhd` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`),
  ADD CONSTRAINT `FKqqmci4dq129yrg27n3ft2385u` FOREIGN KEY (`discount_code_id`) REFERENCES `discount_codes` (`id`);

--
-- Các ràng buộc cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `FK6w4ephwm2mmgi7l741eox2thk` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`),
  ADD CONSTRAINT `FKiv2147fa7ps0h4gr9wsx4jd4d` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Các ràng buộc cho bảng `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `FKbbwn3lvynin5hrtiv7u6hteyy` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKqgl6lluol2quheqoken2o8rt3` FOREIGN KEY (`table_id`) REFERENCES `billiard_tables` (`id`);

--
-- Các ràng buộc cho bảng `sessions`
--
ALTER TABLE `sessions`
  ADD CONSTRAINT `FK4gfnsavu42v8xkmehp6ft3dg` FOREIGN KEY (`table_id`) REFERENCES `billiard_tables` (`id`),
  ADD CONSTRAINT `FKm6knee6ksh93kdkx9mpo01pjw` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  ADD CONSTRAINT `FKodq9hadc349jmv9jpkk8debvc` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `session_segments`
--
ALTER TABLE `session_segments`
  ADD CONSTRAINT `FK2llh5pfg2vofnokqb4x8rdkrn` FOREIGN KEY (`price_rule_id`) REFERENCES `price_rules` (`id`),
  ADD CONSTRAINT `FKart0xia1nl3xg2ypvuqmawycc` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`);

--
-- Các ràng buộc cho bảng `shift_closings`
--
ALTER TABLE `shift_closings`
  ADD CONSTRAINT `FKci2tt40v2h83qqeov2onn26il` FOREIGN KEY (`shift_id`) REFERENCES `shifts` (`id`),
  ADD CONSTRAINT `FKhsen5alhh6xd5xj8xypkg45kb` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `staff_schedules`
--
ALTER TABLE `staff_schedules`
  ADD CONSTRAINT `FKa8mao3r0dk938e4wu82tksui8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKacvp4a4kpk04lnbljhsfl80fq` FOREIGN KEY (`shift_id`) REFERENCES `shifts` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
