-- These statements are for initial database and user setup.
-- Hibernate will handle table creation when ddl-auto is set to 'update' in application.yml.

CREATE DATABASE ecommerce_db;
CREATE USER ${DB_USERNAME} WITH PASSWORD '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ${DB_USERNAME};

-- Commented out table creation statements as Hibernate will handle this.

-- Create Customer table
-- CREATE TABLE customer (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     username VARCHAR(255) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL,
--     email VARCHAR(255) NOT NULL UNIQUE,
--     address VARCHAR(255),
--     phone_number VARCHAR(20),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- );

-- Create Product table
-- CREATE TABLE product (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     description TEXT,
--     price DECIMAL(10, 2) NOT NULL,
--     stock_quantity INT NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- );

-- Create Order table
-- CREATE TABLE `Order` (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     customer_id BIGINT NOT NULL,
--     order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     total_amount DECIMAL(10, 2) NOT NULL,
--     status VARCHAR(50) NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (customer_id) REFERENCES Customer(id)
-- );

-- Create OrderItem table
-- CREATE TABLE OrderItem (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     order_id BIGINT NOT NULL,
--     product_id BIGINT NOT NULL,
--     quantity INT NOT NULL,
--     price DECIMAL(10, 2) NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (order_id) REFERENCES `Order`(id),
--     FOREIGN KEY (product_id) REFERENCES Product(id)
-- );

-- Create ShoppingCart table
-- CREATE TABLE ShoppingCart (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     customer_id BIGINT NOT NULL UNIQUE,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (customer_id) REFERENCES Customer(id)
-- );

-- Create ShoppingCartItem table
-- CREATE TABLE ShoppingCartItem (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     shopping_cart_id BIGINT NOT NULL,
--     product_id BIGINT NOT NULL,
--     quantity INT NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (shopping_cart_id) REFERENCES ShoppingCart(id),
--     FOREIGN KEY (product_id) REFERENCES Product(id)
-- );