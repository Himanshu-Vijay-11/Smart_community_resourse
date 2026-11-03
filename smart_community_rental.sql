-- ========================================================
-- Smart Community Resource Rental & Booking System Database
-- Database: smart_community_rental
-- ========================================================

CREATE DATABASE IF NOT EXISTS smart_community_rental;
USE smart_community_rental;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 3. RESOURCES TABLE
CREATE TABLE IF NOT EXISTS resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    available INT NOT NULL DEFAULT 1,
    rent_per_day DOUBLE NOT NULL DEFAULT 0.0,
    status VARCHAR(20) DEFAULT 'Active'
);

-- 4. BOOKINGS TABLE
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_name VARCHAR(100),
    resource_id INT NOT NULL,
    resource_name VARCHAR(100),
    start_date VARCHAR(20),
    end_date VARCHAR(20),
    total_price DOUBLE,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
);

-- 5. PAYMENTS TABLE
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'COMPLETED',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- 6. RETURNS TABLE
CREATE TABLE IF NOT EXISTS returns (
    return_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    return_date VARCHAR(20),
    condition_status VARCHAR(50) DEFAULT 'Good',
    fine_amount DOUBLE DEFAULT 0.0,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- 7. FINES TABLE
CREATE TABLE IF NOT EXISTS fines (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    user_name VARCHAR(100),
    fine_amount DOUBLE NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING'
);

-- 8. FEEDBACK TABLE
CREATE TABLE IF NOT EXISTS feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_name VARCHAR(100),
    resource_name VARCHAR(100),
    rating INT DEFAULT 5,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================================
-- INITIAL SEED DATA
-- ========================================================

-- Initial Admin Account (Password: admin123)
INSERT IGNORE INTO users (user_id, name, email, phone, password, role) VALUES 
(1, 'Admin User', 'admin@gmail.com', '9999999999', 'admin123', 'ADMIN'),
(2, 'Rahul Sharma', 'rahul@gmail.com', '9876543210', '123456', 'USER'),
(3, 'Aman Verma', 'aman@gmail.com', '9876501234', '123456', 'USER');

-- Initial Categories
INSERT IGNORE INTO categories (category_id, name, description) VALUES 
(1, 'Tools', 'Heavy and light machinery, drills, kits'),
(2, 'Electronics', 'Projectors, sound systems, cameras'),
(3, 'Event', 'Chairs, tables, tents, decorative lights'),
(4, 'Sports', 'Cricket kits, badminton sets, footballs'),
(5, 'Medical', 'Wheelchairs, crutches, oxygen monitors'),
(6, 'Camping', 'Tents, sleeping bags, stoves');

-- Initial Resources
INSERT IGNORE INTO resources (resource_id, name, category, quantity, available, rent_per_day, status) VALUES 
(1, 'Heavy Duty Drill Machine', 'Tools', 5, 5, 150.0, 'Active'),
(2, 'HD LCD Projector', 'Electronics', 3, 3, 500.0, 'Active'),
(3, 'Event Plastic Chairs (Set of 10)', 'Event', 20, 20, 200.0, 'Active'),
(4, 'Full Cricket Kit Set', 'Sports', 4, 4, 250.0, 'Active'),
(5, 'Foldable Wheelchair', 'Medical', 3, 3, 100.0, 'Active'),
(6, '4-Person Camping Tent', 'Camping', 6, 6, 300.0, 'Active');

-- Initial Sample Bookings
INSERT IGNORE INTO bookings (booking_id, user_id, user_name, resource_id, resource_name, start_date, end_date, total_price, status) VALUES 
(1, 2, 'Rahul Sharma', 1, 'Heavy Duty Drill Machine', '2026-09-10', '2026-09-12', 300.0, 'APPROVED'),
(2, 3, 'Aman Verma', 2, 'HD LCD Projector', '2026-09-15', '2026-09-16', 500.0, 'PENDING');

-- Initial Payments
INSERT IGNORE INTO payments (payment_id, booking_id, user_id, amount, payment_method, transaction_id, status) VALUES 
(1, 1, 2, 300.0, 'UPI / Online', 'TXN987654321', 'COMPLETED');

-- Initial Feedback
INSERT IGNORE INTO feedback (feedback_id, user_id, user_name, resource_name, rating, comments) VALUES 
(1, 2, 'Rahul Sharma', 'Heavy Duty Drill Machine', 5, 'Great condition tool, saved a lot of time!');
