PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS ord_invoices;
DROP TABLE IF EXISTS ord_order_lines;
DROP TABLE IF EXISTS ord_orders;
DROP TABLE IF EXISTS ord_balance;
DROP TABLE IF EXISTS ord_catalogue;
DROP TABLE IF EXISTS ord_merchants;

CREATE TABLE ord_merchants (
    merchant_id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    merchant_name TEXT NOT NULL,
    account_status TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE ord_catalogue (
    item_id TEXT PRIMARY KEY,
    description TEXT NOT NULL,
    package_type TEXT NOT NULL,
    unit TEXT NOT NULL,
    units_in_pack INTEGER NOT NULL,
    package_cost REAL NOT NULL,
    availability_packs INTEGER NOT NULL,
    stock_limit_packs INTEGER NOT NULL
);

CREATE TABLE ord_orders (
    order_no TEXT PRIMARY KEY,
    merchant_id INTEGER NOT NULL,
    order_date TEXT NOT NULL,
    status TEXT NOT NULL,
    grand_total REAL NOT NULL,
    FOREIGN KEY (merchant_id) REFERENCES ord_merchants(merchant_id)
);

CREATE TABLE ord_order_lines (
    order_no TEXT NOT NULL,
    item_id TEXT NOT NULL,
    description TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_cost REAL NOT NULL,
    total REAL NOT NULL,
    PRIMARY KEY (order_no, item_id),
    FOREIGN KEY (order_no) REFERENCES ord_orders(order_no),
    FOREIGN KEY (item_id) REFERENCES ord_catalogue(item_id)
);

CREATE TABLE ord_balance (
    merchant_id INTEGER PRIMARY KEY,
    outstanding_amount REAL NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (merchant_id) REFERENCES ord_merchants(merchant_id)
);

CREATE TABLE ord_invoices (
    invoice_no TEXT PRIMARY KEY,
    order_no TEXT NOT NULL UNIQUE,
    merchant_ID INTEGER NOT NULL,
    issued_at TEXT NOT NULL,
    subtotal REAL NOT NULL,
    vat REAL NOT NULL,
    grand_total REAL NOT NULL,
    FOREIGN KEY (order_no) REFERENCES ord_orders(order_no),
    FOREIGN KEY (merchant_ID) REFERENCES ord_merchants(merchant_id)
);

INSERT INTO ord_merchants (merchant_id, username, password, merchant_name, account_status, created_at) VALUES
(1, 'merchant.alpha', 'alpha123', 'Alpha Traders Ltd', 'NORMAL', '2026-01-10 09:00:00'),
(2, 'merchant.beta', 'beta123', 'Beta Retail Co', 'NORMAL', '2026-01-15 10:30:00');

INSERT INTO ord_catalogue (item_id, description, package_type, unit, units_in_pack, package_cost, availability_packs, stock_limit_packs) VALUES
('100 00001', 'Paracetamol', 'box', 'Caps', 20, 0.10, 10345, 300),
('100 00002', 'Aspirin', 'box', 'Caps', 20, 0.50, 12453, 500),
('100 00003', 'Analgin', 'box', 'Caps', 10, 1.20, 4235, 200),
('100 00004', 'Celebrex, caps 100 mg', 'box', 'Caps', 10, 10.00, 3420, 200),
('100 00005', 'Celebrex, caps 200 mg', 'box', 'caps', 10, 18.50, 1450, 150),
('100 00006', 'Retin-A Tretin, 30 g', 'box', 'caps', 20, 25.00, 2013, 200),
('100 00007', 'Lipitor TB, 20 mg', 'box', 'caps', 30, 15.50, 1562, 200),
('100 00008', 'Claritin CR, 60g', 'box', 'caps', 20, 19.50, 2540, 200),
('200 00004', 'Iodine tincture', 'bottle', 'ml', 100, 0.30, 22134, 200),
('200 00005', 'Rhynol', 'bottle', 'ml', 200, 2.50, 1908, 300),
('300 00001', 'Ospen', 'box', 'caps', 20, 10.50, 809, 200),
('300 00002', 'Amopen', 'box', 'caps', 30, 15.00, 1340, 300),
('400 00001', 'Vitamin C', 'box', 'caps', 30, 1.20, 3258, 300),
('400 00002', 'Vitamin B12', 'box', 'caps', 30, 1.30, 2673, 300);

INSERT INTO ord_orders (order_no, merchant_id, order_date, status, grand_total) VALUES
('ORD-2026-0001', 1, '2026-03-12 12:15:00', 'DELIVERED', 508.60),
('ORD-2026-0002', 1, '2026-03-28 15:45:00', 'SHIPPED', 39.00),
('ORD-2026-0003', 2, '2026-04-02 10:20:00', 'PROCESSING', 46.90),
('ORD-2026-0004', 2, '2026-04-10 09:05:00', 'PLACED', 33.30);

INSERT INTO ord_order_lines (order_no, item_id, description, quantity, unit_cost, total) VALUES
('ORD-2026-0001', '100 00001', 'Paracetamol', 10, 0.10, 1.00),
('ORD-2026-0001', '100 00003', 'Analgin', 20, 1.20, 24.00),
('ORD-2026-0001', '200 00004', 'Iodine tincture', 20, 0.30, 3.60),
('ORD-2026-0001', '200 00005', 'Rhynol', 10, 2.50, 25.00),
('ORD-2026-0001', '300 00001', 'Ospen', 10, 10.50, 105.00),
('ORD-2026-0001', '300 00002', 'Amopen', 20, 15.00, 300.00),
('ORD-2026-0001', '400 00001', 'Vitamin C', 20, 1.20, 24.00),
('ORD-2026-0001', '400 00002', 'Vitamin B12', 20, 1.30, 26.00);

INSERT INTO ord_balance (merchant_id, outstanding_amount, updated_at) VALUES
(1, 97.80, '2026-04-15 18:00:00'),
(2, 80.20, '2026-04-16 09:30:00');

INSERT INTO ord_invoices (invoice_no, order_no, merchant_ID, issued_at, subtotal, vat, grand_total) VALUES
('INV-2026-1101', 'ORD-2026-0001', 1, '2026-03-13 09:00:00', 423.83, 84.77, 508.60),
('INV-2026-1102', 'ORD-2026-0002', 1, '2026-03-29 08:30:00', 32.50, 6.50, 39.00),
('INV-2026-1103', 'ORD-2026-0003', 2, '2026-04-03 11:10:00', 39.08, 7.82, 46.90),
('INV-2026-1104', 'ORD-2026-0004', 2, '2026-04-10 11:00:00', 27.75, 5.55, 33.30);
