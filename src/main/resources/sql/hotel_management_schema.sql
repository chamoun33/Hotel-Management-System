CREATE DATABASE hotelManagementSystem;
Use hotelManagementSystem;
CREATE TABLE users (
                       id CHAR(36) PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       phone_number VARCHAR(30),
                       created_at DATETIME NOT NULL
);

CREATE TABLE guests (
                        id CHAR(36) PRIMARY KEY,
                        full_name VARCHAR(150) NOT NULL,
                        email VARCHAR(150) UNIQUE,
                        phone_number VARCHAR(30)
);

CREATE TABLE rooms (
                       room_number INT PRIMARY KEY,
                       capacity INT NOT NULL,
                       room_type VARCHAR(50) NOT NULL,
                       price_per_night DOUBLE NOT NULL,
                       status VARCHAR(50) NOT NULL
);

CREATE TABLE reservations (
                              id CHAR(36) PRIMARY KEY,
                              guest_id CHAR(36) NOT NULL,
                              room_number INT NOT NULL,
                              created_at DATETIME NOT NULL,
                              check_in DATE NOT NULL,
                              check_out DATE NOT NULL,
                              status VARCHAR(50) NOT NULL,

                              FOREIGN KEY (guest_id) REFERENCES guests(id),
                              FOREIGN KEY (room_number) REFERENCES rooms(room_number)
);

CREATE TABLE payments (
                          id CHAR(36) PRIMARY KEY,
                          reservation_id CHAR(36) NOT NULL,
                          payment_date DATETIME,
                          amount DOUBLE NOT NULL,
                          status VARCHAR(50) NOT NULL,
                          method VARCHAR(50) NOT NULL,

                          FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

CREATE DATABASE hotelManagementSystemTest;
Use hotelManagementSystemTest;
CREATE TABLE users (
                       id CHAR(36) PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       phone_number VARCHAR(30),
                       created_at DATETIME NOT NULL
);

CREATE TABLE guests (
                        id CHAR(36) PRIMARY KEY,
                        full_name VARCHAR(150) NOT NULL,
                        email VARCHAR(150) UNIQUE,
                        phone_number VARCHAR(30)
);

CREATE TABLE rooms (
                       room_number INT PRIMARY KEY,
                       capacity INT NOT NULL,
                       room_type VARCHAR(50) NOT NULL,
                       price_per_night DOUBLE NOT NULL,
                       status VARCHAR(50) NOT NULL
);

CREATE TABLE reservations (
                              id CHAR(36) PRIMARY KEY,
                              guest_id CHAR(36) NOT NULL,
                              room_number INT NOT NULL,
                              created_at DATETIME NOT NULL,
                              check_in DATE NOT NULL,
                              check_out DATE NOT NULL,
                              status VARCHAR(50) NOT NULL,

                              FOREIGN KEY (guest_id) REFERENCES guests(id),
                              FOREIGN KEY (room_number) REFERENCES rooms(room_number)
);

CREATE TABLE payments (
                          id CHAR(36) PRIMARY KEY,
                          reservation_id CHAR(36) NOT NULL,
                          payment_date DATETIME,
                          amount DOUBLE NOT NULL,
                          status VARCHAR(50) NOT NULL,
                          method VARCHAR(50) NOT NULL,

                          FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);