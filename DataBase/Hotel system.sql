CREATE DATABASE IF NOT EXISTS hotel_system;
USE hotel_system;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100),
    contact VARCHAR(50),
    room_type VARCHAR(20),
    room_price DECIMAL(10, 2),
    check_in_date DATE,
    check_out_date DATE,
    stay_duration INT,
    total_cost DECIMAL(10, 2),
    payment_reference VARCHAR(100),
    num_adults INT,
    num_children INT,
    checked_in BOOLEAN DEFAULT FALSE,
    checked_out BOOLEAN DEFAULT FALSE
)