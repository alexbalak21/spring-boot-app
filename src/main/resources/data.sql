-- Insert initial data into products table
MERGE INTO products (id, name, price) VALUES 
    (1, 'Laptop', 999.99),
    (2, 'Smartphone', 699.99),
    (3, 'Headphones', 199.99);
