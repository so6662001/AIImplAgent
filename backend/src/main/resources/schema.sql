-- Project
CREATE TABLE IF NOT EXISTS t_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_code VARCHAR(32) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    industry_type VARCHAR(30),
    scale VARCHAR(20),
    pm_id BIGINT,
    status VARCHAR(30) DEFAULT 'PENDING',
    modules VARCHAR(500),
    region VARCHAR(50),
    start_date DATE,
    end_date DATE,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Product Category
CREATE TABLE IF NOT EXISTS t_product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(20) NOT NULL UNIQUE,
    category_name VARCHAR(60) NOT NULL,
    parent_id BIGINT,
    level INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    category_group VARCHAR(30),
    default_unit VARCHAR(10),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Product
CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(40) NOT NULL UNIQUE,
    product_name VARCHAR(60) NOT NULL,
    spec VARCHAR(80),
    material VARCHAR(30),
    steel_mill VARCHAR(60),
    surface VARCHAR(30),
    category_id BIGINT,
    unit VARCHAR(10),
    pricing_unit VARCHAR(10),
    theoretical_weight DECIMAL(18,4),
    tax_rate DECIMAL(5,4),
    enabled BOOLEAN DEFAULT TRUE,
    code_auto_generated BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Customer
CREATE TABLE IF NOT EXISTS t_customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(32) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    short_name VARCHAR(40),
    customer_type VARCHAR(20),
    credit_code VARCHAR(18),
    contact VARCHAR(30),
    phone VARCHAR(20),
    province VARCHAR(20),
    city VARCHAR(20),
    district VARCHAR(20),
    address VARCHAR(200),
    industry VARCHAR(30),
    category VARCHAR(30),
    settlement_method VARCHAR(20),
    credit_limit DECIMAL(18,2),
    tax_rate DECIMAL(5,4),
    invoice_title VARCHAR(120),
    invoice_tax_no VARCHAR(20),
    sales_rep_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Warehouse
CREATE TABLE IF NOT EXISTS t_warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(20) NOT NULL UNIQUE,
    warehouse_name VARCHAR(60) NOT NULL,
    warehouse_type VARCHAR(20),
    warehouse_nature VARCHAR(20),
    management_mode VARCHAR(20),
    address VARCHAR(200),
    contact VARCHAR(30),
    phone VARCHAR(20),
    area_sqm DECIMAL(12,2),
    crane_count INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Trainee Profile
CREATE TABLE IF NOT EXISTS t_trainee_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_id BIGINT,
    employee_name VARCHAR(30),
    role VARCHAR(30),
    department VARCHAR(30),
    ka_user BOOLEAN DEFAULT FALSE,
    progress_percent INT DEFAULT 0,
    attendance_days INT DEFAULT 0,
    total_days INT DEFAULT 0,
    risk_level VARCHAR(10) DEFAULT 'NORMAL',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Exam Record
CREATE TABLE IF NOT EXISTS t_exam_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    trainee_id BIGINT NOT NULL,
    module VARCHAR(50) NOT NULL,
    exam_type VARCHAR(20),
    score INT,
    passed BOOLEAN DEFAULT FALSE,
    required_course BOOLEAN DEFAULT FALSE,
    weak_points VARCHAR(500),
    retry_of BIGINT,
    exam_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);
