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

-- Customer Profile (调研分析)
CREATE TABLE IF NOT EXISTS t_customer_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    industry_type VARCHAR(30),
    business_model VARCHAR(30),
    trade_mode VARCHAR(20),
    trade_scope VARCHAR(20),
    main_business VARCHAR(1000),
    total_production_lines INT DEFAULT 0,
    total_warehouse_count INT DEFAULT 0,
    total_warehouse_area_sqm DECIMAL(18,2),
    total_crane_count INT DEFAULT 0,
    monthly_volume DECIMAL(18,2),
    monthly_amount DECIMAL(18,2),
    total_staff INT DEFAULT 0,
    management_goals VARCHAR(1000),
    process_goals VARCHAR(1000),
    efficiency_goals VARCHAR(1000),
    risk_control_goals VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Account Set (帐套管理)
CREATE TABLE IF NOT EXISTS t_account_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL UNIQUE,
    set_name VARCHAR(60) NOT NULL,
    accounting_system VARCHAR(30),
    pricing_method VARCHAR(30),
    qty_decimals INT DEFAULT 2,
    wgt_decimals INT DEFAULT 2,
    prc_decimals INT DEFAULT 2,
    amt_decimals INT DEFAULT 2,
    use_weight BOOLEAN DEFAULT TRUE,
    currency VARCHAR(10) DEFAULT 'CNY',
    fiscal_year_start INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'DRAFT',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Delivery Report (交付报告)
CREATE TABLE IF NOT EXISTS t_delivery_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    report_type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    training_pass_rate DECIMAL(6,2),
    data_import_completion_rate DECIMAL(6,2),
    total_issues INT DEFAULT 0,
    resolved_issues INT DEFAULT 0,
    customer_satisfaction_score DECIMAL(3,1),
    schedule_deviation_percent DECIMAL(6,2),
    document_completion_rate DECIMAL(6,2),
    overall_score DECIMAL(6,2),
    ai_comment VARCHAR(2000),
    status VARCHAR(20) DEFAULT 'DRAFT',
    confirmed_by VARCHAR(60),
    confirmed_at TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Engineer (人力管理)
CREATE TABLE IF NOT EXISTS t_engineer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    engineer_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(30) NOT NULL,
    level VARCHAR(20),
    skills VARCHAR(500),
    current_status VARCHAR(20) DEFAULT 'IDLE',
    current_project_id BIGINT,
    join_date DATE,
    phone VARCHAR(20),
    email VARCHAR(60),
    composite_score DECIMAL(6,2),
    monthly_idle_rate DECIMAL(6,2),
    monthly_project_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Project Evaluation (项目评价)
CREATE TABLE IF NOT EXISTS t_project_evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    evaluator_id BIGINT,
    schedule_score INT,
    quality_score INT,
    csat_score INT,
    process_score INT,
    cost_score INT,
    pqi_score DECIMAL(6,2),
    rating VARCHAR(20),
    ai_comment VARCHAR(2000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Server Profile (服务器连接管理)
CREATE TABLE IF NOT EXISTS t_server_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    server_name VARCHAR(60) NOT NULL,
    host VARCHAR(200) NOT NULL,
    port INT NOT NULL,
    db_type VARCHAR(20),
    db_name VARCHAR(60) NOT NULL,
    ssl_enabled BOOLEAN DEFAULT FALSE,
    os_type VARCHAR(30),
    erp_version VARCHAR(30),
    api_base_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'UNCONFIGURED',
    last_health_check TIMESTAMP,
    network_latency_ms INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Go-Live Check Item (上线检查)
CREATE TABLE IF NOT EXISTS t_go_live_check_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    check_result VARCHAR(20) DEFAULT 'UNCHECKED',
    detail VARCHAR(1000),
    checked_at TIMESTAMP,
    checked_by VARCHAR(60),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Simulation Scene (模拟演练)
CREATE TABLE IF NOT EXISTS t_simulation_scene (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    scene_name VARCHAR(100) NOT NULL,
    scene_type VARCHAR(30) NOT NULL,
    description VARCHAR(1000),
    steps VARCHAR(4000),
    expected_result VARCHAR(2000),
    actual_result VARCHAR(2000),
    status VARCHAR(20) DEFAULT 'PENDING',
    executed_at TIMESTAMP,
    executed_by VARCHAR(60),
    deviation VARCHAR(2000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);
