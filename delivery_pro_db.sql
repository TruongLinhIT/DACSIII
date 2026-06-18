-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Jun 05, 2026 at 09:41 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `delivery_pro_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `drivers`
--

CREATE TABLE `drivers` (
  `driver_id` int(11) NOT NULL,
  `license_plate` varchar(20) NOT NULL,
  `vehicle_type` varchar(50) DEFAULT 'Motorbike',
  `is_online` tinyint(1) DEFAULT 0,
  `current_lat` double DEFAULT NULL,
  `current_lng` double DEFAULT NULL,
  `wallet_balance` decimal(15,2) DEFAULT 0.00,
  `rating_avg` decimal(3,2) DEFAULT 5.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `drivers`
--

INSERT INTO `drivers` (`driver_id`, `license_plate`, `vehicle_type`, `is_online`, `current_lat`, `current_lng`, `wallet_balance`, `rating_avg`) VALUES
(42, '43AE-03344', 'Air blade 125', 1, 16.0985917, 108.24863, 84440.00, 5.00);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `title` varchar(150) NOT NULL,
  `body` text NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `data_json` text DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `user_id`, `title`, `body`, `type`, `data_json`, `is_read`, `created_at`) VALUES
(1, 42, 'Kết quả định danh', 'Hồ sơ định danh bị từ chối: Khong thay mat', 'identity_status', '{\"type\":\"identity_status\",\"status\":\"rejected\",\"reason\":\"Khong thay mat\"}', 1, '2026-05-20 16:57:06'),
(2, 42, 'Đơn hàng đã có tài xế', 'Đơn #10 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"10\"}', 1, '2026-05-20 17:00:46'),
(3, 43, 'Đơn hàng đã có tài xế', 'Đơn #17 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"17\"}', 1, '2026-05-27 16:58:40'),
(4, 35, 'Đơn hàng đã có tài xế', 'Đơn #13 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"13\"}', 1, '2026-05-27 16:59:38'),
(5, 43, 'Đơn hàng đã có tài xế', 'Đơn #16 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"16\"}', 1, '2026-05-27 16:59:43'),
(6, 1, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 43 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"43\"}', 0, '2026-05-27 17:00:53'),
(7, 35, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 43 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"43\"}', 1, '2026-05-27 17:00:53'),
(8, 43, 'Đơn hàng đã có tài xế', 'Đơn #18 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"18\"}', 1, '2026-05-27 17:26:18'),
(9, 43, 'Đơn hàng đã có tài xế', 'Đơn #20 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"20\"}', 1, '2026-05-27 18:02:22'),
(10, 35, 'Đơn hàng đã có tài xế', 'Đơn #15 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"15\"}', 1, '2026-05-27 18:03:11'),
(11, 43, 'Đơn hàng đã có tài xế', 'Đơn #21 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"21\"}', 1, '2026-05-27 18:05:20'),
(12, 42, 'Đơn hàng đã có tài xế', 'Đơn #12 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"12\"}', 1, '2026-05-27 18:05:41'),
(13, 1, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 42 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"42\"}', 0, '2026-05-27 18:06:15'),
(14, 35, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 42 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"42\"}', 1, '2026-05-27 18:06:15'),
(15, 43, 'Đơn hàng đã có tài xế', 'Đơn #22 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"22\"}', 1, '2026-05-29 13:55:33'),
(16, 43, 'Đơn hàng đã có tài xế', 'Đơn #19 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"19\"}', 1, '2026-05-29 13:55:58'),
(17, 35, 'Đơn hàng đã có tài xế', 'Đơn #14 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"14\"}', 1, '2026-05-29 13:56:00'),
(18, 42, 'Đơn hàng đã có tài xế', 'Đơn #9 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"9\"}', 1, '2026-05-29 13:57:47'),
(19, 42, 'Đơn hàng đã có tài xế', 'Đơn #8 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"8\"}', 1, '2026-05-29 13:57:51'),
(20, 43, 'Đơn hàng đã có tài xế', 'Đơn #23 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"23\"}', 1, '2026-05-31 05:57:59'),
(21, 43, 'Đơn hàng đã có tài xế', 'Đơn #24 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"24\"}', 1, '2026-05-31 05:58:18'),
(22, 1, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 43 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"43\"}', 0, '2026-05-31 06:03:09'),
(23, 35, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 43 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"43\"}', 1, '2026-05-31 06:03:09'),
(24, 43, 'Đơn hàng đã có tài xế', 'Đơn #25 đã được tài xế nhận.', 'order_accepted', '{\"type\":\"order_accepted\",\"order_id\":\"25\"}', 0, '2026-05-31 07:30:36'),
(25, 42, 'Đơn hàng gần điểm lấy', 'Bạn cách điểm lấy đơn #26 khoảng 0.86 km.', 'near_pickup', '{\"type\":\"near_pickup\",\"order_id\":\"26\",\"distance_km\":\"0.86\"}', 1, '2026-05-31 07:40:40'),
(26, 42, 'Đơn hàng gần điểm lấy', 'Bạn cách điểm lấy đơn #27 khoảng 0.86 km.', 'near_pickup', '{\"type\":\"near_pickup\",\"order_id\":\"27\",\"distance_km\":\"0.86\"}', 1, '2026-05-31 07:40:41'),
(27, 42, 'Đơn hàng gần điểm lấy', 'Bạn cách điểm lấy đơn #28 khoảng 0.86 km.', 'near_pickup', '{\"type\":\"near_pickup\",\"order_id\":\"28\",\"distance_km\":\"0.86\"}', 1, '2026-05-31 07:40:41'),
(28, 42, 'Đơn hàng gần điểm lấy', 'Bạn cách điểm lấy đơn #29 khoảng 0.86 km.', 'near_pickup', '{\"type\":\"near_pickup\",\"order_id\":\"29\",\"distance_km\":\"0.86\"}', 1, '2026-05-31 07:40:41'),
(29, 1, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 42 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"42\"}', 0, '2026-05-31 07:56:34'),
(30, 35, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 42 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"42\"}', 1, '2026-05-31 07:56:34'),
(31, 43, 'Kết quả định danh', 'Hồ sơ định danh đã được duyệt.', 'identity_status', '{\"type\":\"identity_status\",\"status\":\"verified\",\"reason\":\"\"}', 0, '2026-05-31 08:22:43'),
(32, 42, 'Kết quả định danh', 'Hồ sơ định danh đã được duyệt.', 'identity_status', '{\"type\":\"identity_status\",\"status\":\"verified\",\"reason\":\"\"}', 0, '2026-05-31 08:22:50'),
(33, 1, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 43 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"43\"}', 0, '2026-06-05 07:18:11'),
(34, 35, 'Hồ sơ eKYC mới cần duyệt', 'Người dùng ID 43 đã gửi hồ sơ định danh mới.', 'identity_submitted', '{\"type\":\"identity_submitted\",\"user_id\":\"43\"}', 0, '2026-06-05 07:18:11'),
(35, 43, 'Kết quả định danh', 'Hồ sơ định danh đã được duyệt.', 'identity_status', '{\"type\":\"identity_status\",\"status\":\"verified\",\"reason\":\"\"}', 0, '2026-06-05 07:19:12'),
(36, 42, 'Có đơn hàng mới gần bạn', 'Đơn #31 gần vị trí của bạn.', 'new_order', '{\"type\":\"new_order\",\"order_id\":\"31\"}', 0, '2026-06-05 07:34:48');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `driver_id` int(11) DEFAULT NULL,
  `package_type` enum('electronics','food','bulky','others') NOT NULL,
  `weight_kg` decimal(10,2) NOT NULL,
  `order_description` text DEFAULT NULL,
  `pickup_address` text NOT NULL,
  `delivery_address` text NOT NULL,
  `pickup_lat` double NOT NULL,
  `pickup_lng` double NOT NULL,
  `delivery_lat` double NOT NULL,
  `delivery_lng` double NOT NULL,
  `distance_km` decimal(10,2) DEFAULT NULL,
  `total_price` decimal(15,2) NOT NULL,
  `app_commission` decimal(15,2) NOT NULL,
  `driver_earning` decimal(15,2) NOT NULL,
  `photo_before_booking` varchar(255) NOT NULL,
  `photo_at_pickup` varchar(255) DEFAULT NULL,
  `photo_at_delivery` varchar(255) DEFAULT NULL,
  `sender_name` varchar(100) NOT NULL,
  `sender_phone` varchar(20) NOT NULL,
  `recipient_name` varchar(100) NOT NULL,
  `recipient_phone` varchar(20) NOT NULL,
  `pickup_note` text DEFAULT NULL,
  `delivery_note` text DEFAULT NULL,
  `package_size` enum('S','M','L') NOT NULL DEFAULT 'S',
  `cod_amount` decimal(15,2) DEFAULT 0.00,
  `payment_method` enum('sender_cash','recipient_cash') NOT NULL DEFAULT 'sender_cash',
  `status` enum('pending','accepted','picking_up','delivering','arrived_delivery','completed','cancelled') DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `accepted_at` timestamp NULL DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `customer_id`, `driver_id`, `package_type`, `weight_kg`, `order_description`, `pickup_address`, `delivery_address`, `pickup_lat`, `pickup_lng`, `delivery_lat`, `delivery_lng`, `distance_km`, `total_price`, `app_commission`, `driver_earning`, `photo_before_booking`, `photo_at_pickup`, `photo_at_delivery`, `sender_name`, `sender_phone`, `recipient_name`, `recipient_phone`, `pickup_note`, `delivery_note`, `package_size`, `cod_amount`, `payment_method`, `status`, `created_at`, `accepted_at`, `completed_at`) VALUES
(1, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6', 'Cau Song Han', 16.092767801008122, 108.2429713880901, 16.072166245380757, 108.23066353797913, 6.00, 50000.00, 10000.00, 40000.00, '/uploads/orders/before/20260505183430079_1f62e87e', '/uploads/orders/pickup/20260514031214759_4b15a829', NULL, '', '', '', '', NULL, NULL, 'S', 0.00, 'sender_cash', 'arrived_delivery', '2026-05-05 18:35:03', '2026-05-14 02:51:01', NULL),
(2, 42, 42, 'electronics', 5.00, 'Hang dien tu', '18 Man Quang 6', 'THPT TTT', 16.09293838258189, 108.24301326715249, 16.086916701914873, 108.23612077718968, 1.00, 25000.00, 5000.00, 20000.00, '/uploads/orders/before/20260510115036923_de6fe3bd', '/uploads/orders/pickup/20260514031147533_61a9442b', NULL, '', '', '', '', NULL, NULL, 'S', 0.00, 'sender_cash', 'arrived_delivery', '2026-05-10 11:50:45', '2026-05-14 02:51:08', NULL),
(3, 42, 42, 'electronics', 1.00, 'de vo', '18 Man Quang 6', 'Cau Rong', 16.09292742597586, 108.24300670775386, 16.061429271046222, 108.23212980311649, 3.69, 30450.00, 6090.00, 24360.00, '/uploads/orders/before/20260510120535057_6b6475ad', '/uploads/orders/pickup/20260514030813837_190ea160', '/uploads/orders/delivery/20260514042115310_0ee23ae2', '', '', '', '', NULL, NULL, 'S', 0.00, 'sender_cash', 'completed', '2026-05-10 12:05:50', '2026-05-10 12:43:02', '2026-05-14 04:21:15'),
(4, 42, 42, 'electronics', 1.00, 'Iphone 15', '18 Man Quang 6', 'Vincom plaza', 16.09292776260429, 108.24300879498952, 16.071788036279315, 108.2303745196877, 2.71, 25550.00, 5110.00, 20440.00, '/uploads/orders/before/20260514031646297_448a95da', '/uploads/orders/pickup/20260514031731338_c5df098e', '/uploads/orders/delivery/20260514032332131_5fb8cb44', '', '', '', '', NULL, NULL, 'S', 0.00, 'sender_cash', 'completed', '2026-05-14 03:16:51', '2026-05-14 03:17:14', '2026-05-14 03:23:32'),
(5, 42, 42, 'electronics', 4.00, 'Dien thoai', '18 Man Quang 6 - Tho Quang - Son Tra', '139 Tran Quang Khai - Tho Quang - Son Tra', 16.093089199999998, 108.2429628, 16.102191899999998, 108.2531083, 1.48, 25400.00, 5080.00, 20320.00, '/uploads/orders/before/20260516134028930_b674e7ea', NULL, NULL, '', '', '', '', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-16 13:40:33', '2026-05-20 16:12:38', NULL),
(6, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Vincom Da Nang', 16.093089199999998, 108.2429628, 16.071106800000003, 108.23077760000001, 2.77, 33850.00, 6770.00, 27080.00, '/uploads/orders/before/20260516141148985_ea106b01', NULL, NULL, 'Truong Linh', '0765539316', 'Khanh hang A', '123456789', NULL, NULL, 'S', 100000.00, 'recipient_cash', 'accepted', '2026-05-16 14:12:01', '2026-05-20 16:12:29', NULL),
(7, 42, 42, 'electronics', 10.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Cau Rong - Da Nang', 16.093089199999998, 108.2429628, 16.0611042, 108.2276926, 3.91, 49550.00, 9910.00, 39640.00, '/uploads/orders/before/20260517085317652_a0f7d11d', '/uploads/orders/pickup/20260520163230719_ab59255c', '/uploads/orders/delivery/20260520163246441_9e1247fe', 'Linh', '0765539316', 'Khanh hang A', '0765539316', NULL, 'Duoi chan cau', 'S', 500000.00, 'recipient_cash', 'completed', '2026-05-17 08:54:19', '2026-05-20 16:12:21', '2026-05-20 16:32:46'),
(8, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520162601229_39627599', NULL, NULL, 'Linh', '0765539316', 'Nguyen B', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-20 16:26:06', '2026-05-29 13:57:51', NULL),
(9, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520162601229_39627599', NULL, NULL, 'Linh', '0765539316', 'Nguyen B', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-20 16:26:12', '2026-05-29 13:57:47', NULL),
(10, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520162601229_39627599', NULL, NULL, 'Linh', '0765539316', 'Nguyen B', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-20 16:26:26', '2026-05-20 17:00:46', NULL),
(11, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520163021110_dfd7f2c4', NULL, NULL, 'Linh', '0765539316', 'ABC', '0765539316', NULL, NULL, 'S', 0.00, 'recipient_cash', 'accepted', '2026-05-20 16:30:23', '2026-05-20 16:33:02', NULL),
(12, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6', '15 Le Tan Trung - Tho Quang - Son Tra - Da Nang', 16.093089199999998, 108.2429628, 16.0998763, 108.2493154, 1.01, 25050.00, 5010.00, 20040.00, '/uploads/orders/before/20260521165700817_44a7243b', NULL, NULL, 'Linh', '0765539316', 'Nguyen Thi B', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-21 16:57:05', '2026-05-27 18:05:41', NULL),
(13, 35, 42, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang', 'Tran Duy Trien - Tho Quang - Son Tra', 16.093089199999998, 108.2429628, 16.1159837, 108.273341, 4.12, 40600.00, 8120.00, 32480.00, '/uploads/orders/before/20260521171347385_ad978dd3', NULL, NULL, 'Linh', '0765539316', 'Nguyen Van A', '0765539316', NULL, NULL, 'S', 0.00, 'recipient_cash', 'accepted', '2026-05-21 17:13:50', '2026-05-27 16:59:38', NULL),
(14, 35, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '15 Le Tan Trung - Tho Quang - Son Tra - Da Nang', 16.093089199999998, 108.2429628, 16.0998763, 108.2493154, 1.01, 25050.00, 5010.00, 20040.00, '/uploads/orders/before/20260521173830603_17e8410e', NULL, NULL, 'abvasd', '0765539316', 'Nguyen Van V', '0765539316', NULL, NULL, 'M', 0.00, 'recipient_cash', 'accepted', '2026-05-21 17:38:48', '2026-05-29 13:56:00', NULL),
(15, 35, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '139/59/38 Trần Quang Khải, Sơn Trà, Đà Nẵng, Vietnam', 16.093089199999998, 108.2429628, 16.102191899999998, 108.2531083, 1.48, 27400.00, 5480.00, 21920.00, '/uploads/orders/before/20260521180253982_d30ad79f', NULL, NULL, 'Truong', '0765539316', 'Linh', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-21 18:02:55', '2026-05-27 18:03:11', NULL),
(16, 43, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '32 Lê Độ, Thanh Khê, Đà Nẵng 550000, Vietnam', 16.093089199999998, 108.2429628, 16.0696723, 108.2016921, 5.12, 45600.00, 9120.00, 36480.00, '/uploads/orders/before/20260527165415934_40be5e48', NULL, NULL, 'Linh', '0765539316', 'nguyen Van A', '0765539316', NULL, NULL, 'S', 0.00, 'recipient_cash', 'accepted', '2026-05-27 16:54:34', '2026-05-27 16:59:43', NULL),
(17, 43, 42, 'electronics', 5.00, NULL, '19 Bình Than, Sơn Trà, Đà Nẵng, Vietnam', '62 Lê Độ, Thanh Khê, Đà Nẵng 550000, Vietnam', 16.0936547, 108.2428614, 16.0689939, 108.2014772, 5.20, 46000.00, 9200.00, 36800.00, '/uploads/orders/before/20260527165809809_c7d61c54', NULL, NULL, 'Linh', '0765539316', 'DAAAAA', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-27 16:58:17', '2026-05-27 16:58:40', NULL),
(18, 43, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '33 Nguyễn Công Trứ, An Hải, Đà Nẵng 550000, Vietnam', 16.093089199999998, 108.2429628, 16.0686555, 108.229751, 3.06, 35300.00, 7060.00, 28240.00, '/uploads/orders/before/20260527171405101_1ff3e576', NULL, NULL, 'Linh', '0765539316', 'Tran Thi B', '0765539316', NULL, NULL, 'S', 50000.00, 'recipient_cash', 'accepted', '2026-05-27 17:14:09', '2026-05-27 17:26:18', NULL),
(19, 43, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '15 Le Tan Trung - Tho Quang - Son Tra - Da Nang', 16.093089199999998, 108.2429628, 16.0998763, 108.2493154, 1.01, 25050.00, 5010.00, 20040.00, '/uploads/orders/before/20260527172809575_ccf5c827', NULL, NULL, 'Linh', '0765539316', 'ABAD', '0765539316', NULL, NULL, 'S', 0.00, 'recipient_cash', 'accepted', '2026-05-27 17:28:11', '2026-05-29 13:55:58', NULL),
(20, 43, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '122 Phan Châu Trinh, Hải Châu, Đà Nẵng 550000, Vietnam', 16.093089199999998, 108.2429628, 16.0644183, 108.21982100000001, 4.03, 40150.00, 8030.00, 32120.00, '/uploads/orders/before/20260527174832639_45c102f6', NULL, NULL, 'Lien', '0773447675', 'Linh', '1231231231', NULL, NULL, 'S', 5.00, 'sender_cash', 'accepted', '2026-05-27 17:48:37', '2026-05-27 18:02:22', NULL),
(21, 43, 42, 'electronics', 5.00, NULL, '18 Man Quang 6', '15 Le Tan Trung - Tho Quang - Son Tra - Da Nang', 16.093089199999998, 108.2429628, 16.0998763, 108.2493154, 1.01, 25050.00, 5010.00, 20040.00, '/uploads/orders/before/20260527180438651_2f07c9b8', NULL, NULL, 'adasd', '0765539316', 'dasd', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-27 18:04:43', '2026-05-27 18:05:20', NULL),
(22, 43, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '240 Võ Nguyên Giáp, An Hải, Đà Nẵng 550000, Vietnam', 16.093089199999998, 108.2429628, 16.0659696, 108.2448773, 3.02, 35100.00, 7020.00, 28080.00, '/uploads/orders/before/20260529135448266_9b27fb27', NULL, NULL, 'Truong Vinh', '0765539316', 'Linh', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'accepted', '2026-05-29 13:54:51', '2026-05-29 13:55:33', NULL),
(23, 43, 42, 'electronics', 30.00, NULL, '20 Mân Quang 7, Sơn Trà, Đà Nẵng, Vietnam', '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', 16.0931692, 108.2426627, 16.093089199999998, 108.2429628, 0.03, 70150.00, 14030.00, 56120.00, '/uploads/orders/before/20260529140507910_78a8cc74', NULL, NULL, 'Linh', '0765539316', 'Linh', '0765539316', NULL, NULL, 'L', 0.00, 'sender_cash', 'accepted', '2026-05-29 14:05:12', '2026-05-31 05:57:59', NULL),
(24, 43, 42, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '78 Trần Nhân Tông, Sơn Trà, Đà Nẵng, Vietnam', 16.093089199999998, 108.2429628, 16.090106105181064, 108.23993423803086, 0.46, 22300.00, 4460.00, 17840.00, '/uploads/orders/before/20260531055655746_dea776b7', '/uploads/orders/pickup/20260531060114752_ae117a86', NULL, 'Linh52', '0765539316', 'Lin', '0765539316', NULL, NULL, 'S', 0.00, 'recipient_cash', 'delivering', '2026-05-31 05:57:06', '2026-05-31 05:58:18', NULL),
(25, 43, 42, 'electronics', 30.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '118 Phan Thanh, Thanh Khê, Đà Nẵng 550000, Vietnam', 16.093089199999998, 108.2429628, 16.06173277174871, 108.20906639099121, 5.03, 95150.00, 19030.00, 76120.00, '/uploads/orders/before/20260531060429850_2f7f58d0', NULL, NULL, 'XYZ', '0765539316', 'ABC', '0765539316', NULL, NULL, 'L', 0.00, 'sender_cash', 'accepted', '2026-05-31 06:05:02', '2026-05-31 07:30:36', NULL),
(26, 43, NULL, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '15 Le Tan Trung - Tho Quang - Son Tra - Da Nang', 16.093089199999998, 108.2429628, 16.0998763, 108.2493154, 1.01, 25050.00, 5010.00, 20040.00, '/uploads/orders/before/20260531061213277_99c1df3c', NULL, NULL, 'linh', '0765539316', 'aqsqwe', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'pending', '2026-05-31 06:12:18', NULL, NULL),
(27, 43, NULL, 'electronics', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '978 Ng. Quyền, An Hải, Đà Nẵng 550000, Vietnam', 16.093089199999998, 108.2429628, 16.0642706, 108.23328099999999, 3.37, 36850.00, 7370.00, 29480.00, '/uploads/orders/before/20260531062144990_78ace453', NULL, NULL, 'Linh', '0765539316', 'AY BI CI', '0765539333', NULL, NULL, 'S', 0.00, 'recipient_cash', 'pending', '2026-05-31 06:22:59', NULL, NULL),
(28, 43, NULL, 'electronics', 5.00, NULL, '18 Man Quang 6', 'Cau Rong - Da Nang', 16.093089199999998, 108.2429628, 16.0611042, 108.2276926, 3.91, 39550.00, 7910.00, 31640.00, '/uploads/orders/before/20260531063248002_543ffc61', NULL, NULL, 'Liinh', '0765539316', 'asdas', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'pending', '2026-05-31 06:32:54', NULL, NULL),
(29, 43, NULL, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', '139 Tran Quang Khai - Tho Quang - Son Tra', 16.093089199999998, 108.2429628, 16.102191899999998, 108.2531083, 1.48, 27400.00, 5480.00, 21920.00, '/uploads/orders/before/20260531065735196_95d1443e', NULL, NULL, 'Linh', '0765539316', 'linh', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'pending', '2026-05-31 06:57:37', NULL, NULL),
(30, 43, NULL, 'electronics', 5.00, NULL, '183 Phan Thanh, Thanh Khê, Đà Nẵng 550000, Vietnam', '18 Man Quang 6', 16.0612378898431, 108.20958137512207, 16.093089199999998, 108.2429628, 5.03, 45150.00, 9030.00, 36120.00, '/uploads/orders/before/20260531073150092_b5ee01c3', NULL, NULL, 'Linh', '0765539316', 'Linh', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'pending', '2026-05-31 07:31:57', NULL, NULL),
(31, 43, NULL, 'food', 5.00, NULL, '16 Mân Quang 6, Sơn Trà, Đà Nẵng, Vietnam', '540 Nguyễn Hữu Thọ, Cẩm Lệ, Đà Nẵng 550000, Vietnam', 16.093089199999998, 108.2429628, 16.0260923, 108.2085214, 8.31, 61550.00, 12310.00, 49240.00, '/uploads/orders/before/20260605073438907_11a6eac2', NULL, NULL, 'Linh', '0765539316', 'ABAD', '0765539316', NULL, NULL, 'M', 500000.00, 'recipient_cash', 'pending', '2026-06-05 07:34:48', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `revenue_logs`
--

CREATE TABLE `revenue_logs` (
  `log_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `log_type` enum('commission','payout') NOT NULL,
  `note` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `revenue_logs`
--

INSERT INTO `revenue_logs` (`log_id`, `order_id`, `amount`, `log_type`, `note`, `created_at`) VALUES
(1, 4, 5110.00, 'commission', 'Commission from order', '2026-05-14 03:23:32'),
(2, 4, 20440.00, 'payout', 'Driver earning', '2026-05-14 03:23:32'),
(3, 3, 6090.00, 'commission', 'Commission from order', '2026-05-14 04:21:15'),
(4, 3, 24360.00, 'payout', 'Driver earning', '2026-05-14 04:21:15'),
(5, 7, 9910.00, 'commission', 'Commission from order', '2026-05-20 16:32:46'),
(6, 7, 39640.00, 'payout', 'Driver earning', '2026-05-20 16:32:46');

-- --------------------------------------------------------

--
-- Table structure for table `system_settings`
--

CREATE TABLE `system_settings` (
  `setting_key` varchar(50) NOT NULL,
  `setting_value` varchar(100) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `system_settings`
--

INSERT INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('commission_rate', '0.2', 'Tỷ lệ chiết khấu hệ thống thu (20%)');

-- --------------------------------------------------------

--
-- Table structure for table `driver_reports`
--

CREATE TABLE `driver_reports` (
  `report_id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `reporter_id` int(11) NOT NULL,
  `driver_id` int(11) NOT NULL,
  `reason_type` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `status` enum('pending','resolved') DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `resolved_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `role` enum('customer','driver','admin') NOT NULL DEFAULT 'customer',
  `avatar_url` varchar(255) DEFAULT NULL,
  `otp_code` varchar(1000) DEFAULT NULL,
  `otp_expiry` datetime DEFAULT NULL,
  `cccd_number` varchar(12) DEFAULT NULL,
  `id_card_front_url` varchar(255) DEFAULT NULL,
  `id_card_back_url` varchar(255) DEFAULT NULL,
  `portrait_url` varchar(255) DEFAULT NULL,
  `is_verified` enum('unverified','pending','verified','rejected') DEFAULT 'unverified',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `password` varchar(1000) NOT NULL,
  `fcm_token` varchar(255) DEFAULT NULL,
  `identity_reject_reason` text DEFAULT NULL,
  `is_locked` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `phone`, `full_name`, `email`, `role`, `avatar_url`, `otp_code`, `otp_expiry`, `cccd_number`, `id_card_front_url`, `id_card_back_url`, `portrait_url`, `is_verified`, `created_at`, `updated_at`, `password`, `fcm_token`, `identity_reject_reason`, `is_locked`) VALUES
(1, '0000000000', 'Quản Trị Viên', NULL, 'admin', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'verified', '2026-04-26 10:12:49', '2026-05-31 08:21:55', '', NULL, NULL, 1),
(2, '0123456789', NULL, NULL, 'customer', NULL, '617370', '2026-04-26 17:07:04', NULL, NULL, NULL, NULL, 'unverified', '2026-04-26 14:37:56', '2026-05-31 08:22:02', '', NULL, NULL, NULL),
(35, '', 'Truong Vinh Truong Linh', 'linhbangbang133@gmail.com', 'admin', NULL, NULL, NULL, '123123123123', NULL, NULL, NULL, 'unverified', '2026-04-29 23:51:13', '2026-05-31 08:22:06', '39067347c86cd98bf30564eb9e2d246db7dc15e6bba629cd090d66690119ef2e', 'e5oR5RtmS3er5QbwFvDXfE:APA91bFnGdnx8jDrtKkM6QY33sPAh1u4njAG8qduneNb65FQnaMTGLFyv24D7YUp1tDbcA_ifDYjTyJ_Ng8WWo3cJilPCeMX8wSx7UvJh3JFsBNKPoQX54s', NULL, NULL),
(42, '0536088340', 'Truong Linh', 'linhtvt.24it@vku.udn.vn', 'driver', '/uploads/avatar/20260517090627451_14340126', NULL, NULL, '012345678912', '/uploads/identity/front/20260531075634202_7937a74c', '/uploads/identity/back/20260531075634240_0865cf7f', '/uploads/identity/portrait/20260531075634275_3d828299', 'verified', '2026-05-04 14:12:47', '2026-05-31 08:22:50', '39067347c86cd98bf30564eb9e2d246db7dc15e6bba629cd090d66690119ef2e', 'e5oR5RtmS3er5QbwFvDXfE:APA91bFnGdnx8jDrtKkM6QY33sPAh1u4njAG8qduneNb65FQnaMTGLFyv24D7YUp1tDbcA_ifDYjTyJ_Ng8WWo3cJilPCeMX8wSx7UvJh3JFsBNKPoQX54s', NULL, NULL),
(43, '0535734590', 'Truong Vinh ABC', 'linhbangbang1333@gmail.com', 'customer', '/uploads/avatar/20260605073509639_c076edb4', NULL, NULL, '120112010100', '/uploads/identity/front/20260605071811317_f3491750', '/uploads/identity/back/20260605071811354_f1669f38', '/uploads/identity/portrait/20260605071811391_51df8ea7', 'verified', '2026-05-26 09:45:45', '2026-06-05 07:35:09', '39067347c86cd98bf30564eb9e2d246db7dc15e6bba629cd090d66690119ef2e', 'e5oR5RtmS3er5QbwFvDXfE:APA91bFnGdnx8jDrtKkM6QY33sPAh1u4njAG8qduneNb65FQnaMTGLFyv24D7YUp1tDbcA_ifDYjTyJ_Ng8WWo3cJilPCeMX8wSx7UvJh3JFsBNKPoQX54s', NULL, NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `driver_reports`
--
ALTER TABLE `driver_reports`
  ADD PRIMARY KEY (`report_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `reporter_id` (`reporter_id`),
  ADD KEY `driver_id` (`driver_id`);

--
-- Indexes for table `drivers`
--
ALTER TABLE `drivers`
  ADD PRIMARY KEY (`driver_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `driver_id` (`driver_id`);

--
-- Indexes for table `revenue_logs`
--
ALTER TABLE `revenue_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `order_id` (`order_id`);

--
-- Indexes for table `system_settings`
--
ALTER TABLE `system_settings`
  ADD PRIMARY KEY (`setting_key`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `phone` (`phone`),
  ADD UNIQUE KEY `cccd_number` (`cccd_number`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_users_fcm_token` (`fcm_token`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT for table `revenue_logs`
--
ALTER TABLE `revenue_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `driver_reports`
--
ALTER TABLE `driver_reports`
  MODIFY `report_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=44;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `drivers`
--
ALTER TABLE `drivers`
  ADD CONSTRAINT `drivers_ibfk_1` FOREIGN KEY (`driver_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`driver_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `driver_reports`
--
ALTER TABLE `driver_reports`
  ADD CONSTRAINT `driver_reports_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `driver_reports_ibfk_2` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `driver_reports_ibfk_3` FOREIGN KEY (`driver_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `revenue_logs`
--
ALTER TABLE `revenue_logs`
  ADD CONSTRAINT `revenue_logs_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
