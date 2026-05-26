-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 20, 2026 at 06:35 PM
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
(42, '43AE-03344', 'Air blade 125', 0, NULL, NULL, 84440.00, 5.00);

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
(8, 42, NULL, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520162601229_39627599', NULL, NULL, 'Linh', '0765539316', 'Nguyen B', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'pending', '2026-05-20 16:26:06', NULL, NULL),
(9, 42, NULL, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520162601229_39627599', NULL, NULL, 'Linh', '0765539316', 'Nguyen B', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'pending', '2026-05-20 16:26:12', NULL, NULL),
(10, 42, NULL, 'electronics', 5.00, NULL, '18 Man Quang 6 - Tho Quang - Son Tra', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520162601229_39627599', NULL, NULL, 'Linh', '0765539316', 'Nguyen B', '0765539316', NULL, NULL, 'S', 0.00, 'sender_cash', 'pending', '2026-05-20 16:26:26', NULL, NULL),
(11, 42, 42, 'electronics', 5.00, NULL, '18 Man Quang 6', 'Dai hoc Kinh Te Da Nang', 16.093089199999998, 108.2429628, 16.0473935, 108.2394734, 5.09, 45450.00, 9090.00, 36360.00, '/uploads/orders/before/20260520163021110_dfd7f2c4', NULL, NULL, 'Linh', '0765539316', 'ABC', '0765539316', NULL, NULL, 'S', 0.00, 'recipient_cash', 'accepted', '2026-05-20 16:30:23', '2026-05-20 16:33:02', NULL);

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
  `fcm_token` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `phone`, `full_name`, `email`, `role`, `avatar_url`, `otp_code`, `otp_expiry`, `cccd_number`, `id_card_front_url`, `id_card_back_url`, `portrait_url`, `is_verified`, `created_at`, `updated_at`, `password`, `fcm_token`) VALUES
(1, '0000000000', 'Quản Trị Viên', NULL, 'admin', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'verified', '2026-04-26 10:12:49', '2026-04-26 10:12:49', '', NULL),
(2, '0123456789', NULL, NULL, 'customer', NULL, '617370', '2026-04-26 17:07:04', NULL, NULL, NULL, NULL, 'unverified', '2026-04-26 14:37:56', '2026-04-26 15:05:04', '', NULL),
(35, '', 'Truong Vinh Truong Linh', 'linhbangbang133@gmail.com', 'admin', NULL, NULL, NULL, '123123123123', NULL, NULL, NULL, 'unverified', '2026-04-29 23:51:13', '2026-05-10 11:34:19', '39067347c86cd98bf30564eb9e2d246db7dc15e6bba629cd090d66690119ef2e', NULL),
(42, '0536088340', 'Truong Linh', 'linhtvt.24it@vku.udn.vn', 'customer', '/uploads/avatar/20260517090627451_14340126', 'e34e1023f4a5b421da03201327e495cbc19e5b3ab906d3f9776077e775996c76', '2026-05-17 16:11:45', '012345678912', '/uploads/identity/front/20260517093452527_0cf2c6c3', '/uploads/identity/back/20260517093452574_51e3252c', '/uploads/identity/portrait/20260517093452630_bbf48eaf', 'rejected', '2026-05-04 14:12:47', '2026-05-20 16:33:20', '39067347c86cd98bf30564eb9e2d246db7dc15e6bba629cd090d66690119ef2e', 'e5oR5RtmS3er5QbwFvDXfE:APA91bFnGdnx8jDrtKkM6QY33sPAh1u4njAG8qduneNb65FQnaMTGLFyv24D7YUp1tDbcA_ifDYjTyJ_Ng8WWo3cJilPCeMX8wSx7UvJh3JFsBNKPoQX54s');

--
-- Indexes for dumped tables
--

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
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `revenue_logs`
--
ALTER TABLE `revenue_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

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
-- Constraints for table `revenue_logs`
--
ALTER TABLE `revenue_logs`
  ADD CONSTRAINT `revenue_logs_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
