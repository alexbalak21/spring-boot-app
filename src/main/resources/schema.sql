-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL
);

-- Add some initial data
INSERT INTO products (name, price) VALUES 
    ('Laptop', 999.99),
    ('Smartphone', 699.99),
    ('Headphones', 199.99)
ON DUPLICATE KEY UPDATE name=name;
