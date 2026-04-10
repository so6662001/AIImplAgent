-- System User (用户管理)
CREATE TABLE IF NOT EXISTS t_sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(30),
    role VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(60),
    enabled BOOLEAN DEFAULT TRUE,
    last_login_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- LLM Provider Config (大模型供应商配置)
CREATE TABLE IF NOT EXISTS t_llm_provider_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(30) NOT NULL UNIQUE,
    provider_type VARCHAR(20) NOT NULL,
    api_endpoint VARCHAR(500) NOT NULL,
    api_key VARCHAR(500) NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    max_tokens INT,
    temperature DECIMAL(3,2),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Agent Config (智能体配置)
CREATE TABLE IF NOT EXISTS t_agent_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_code VARCHAR(30) NOT NULL UNIQUE,
    agent_name VARCHAR(60) NOT NULL,
    description VARCHAR(500),
    llm_provider_id BIGINT,
    fallback_llm_provider_id BIGINT,
    prompt_template TEXT,
    rag_enabled BOOLEAN DEFAULT FALSE,
    rag_collection_name VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

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

-- Supplier (供应商)
CREATE TABLE IF NOT EXISTS t_supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(32) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    short_name VARCHAR(40),
    supplier_type VARCHAR(20),
    credit_code VARCHAR(18),
    contact VARCHAR(30),
    phone VARCHAR(20),
    address VARCHAR(200),
    settlement_method VARCHAR(20),
    tax_rate DECIMAL(5,4),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Related Unit (相关单位)
CREATE TABLE IF NOT EXISTS t_related_unit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_code VARCHAR(32) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    unit_type VARCHAR(20),
    credit_code VARCHAR(18),
    contact_person VARCHAR(30),
    phone VARCHAR(20),
    address VARCHAR(200),
    bank_name VARCHAR(60),
    bank_account VARCHAR(40),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Training Daily Log (培训日志)
CREATE TABLE IF NOT EXISTS t_training_daily_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    topic VARCHAR(100) NOT NULL,
    trainer_name VARCHAR(30),
    attendee_count INT DEFAULT 0,
    sign_in_completed BOOLEAN DEFAULT FALSE,
    courseware_uploaded BOOLEAN DEFAULT FALSE,
    summary_uploaded BOOLEAN DEFAULT FALSE,
    exam_conducted BOOLEAN DEFAULT FALSE,
    daily_report_submitted BOOLEAN DEFAULT FALSE,
    issues VARCHAR(2000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Engineer Worklog (工程师工作日志)
CREATE TABLE IF NOT EXISTS t_engineer_worklog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    engineer_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    tasks_plan VARCHAR(2000),
    tasks_completed VARCHAR(2000),
    documents_submitted VARCHAR(2000),
    issues VARCHAR(2000),
    next_day_plan VARCHAR(2000),
    status VARCHAR(20) DEFAULT 'DRAFT',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Department (部门)
CREATE TABLE IF NOT EXISTS t_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code VARCHAR(20) NOT NULL UNIQUE,
    dept_name VARCHAR(60) NOT NULL,
    parent_id BIGINT,
    manager_id BIGINT,
    sort_order INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Employee (员工)
CREATE TABLE IF NOT EXISTS t_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(30) NOT NULL,
    gender VARCHAR(10),
    id_card VARCHAR(18),
    phone VARCHAR(20),
    email VARCHAR(60),
    dept_id BIGINT,
    position VARCHAR(30),
    join_date DATE,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Bank Account (银行账户)
CREATE TABLE IF NOT EXISTS t_bank_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_code VARCHAR(20) NOT NULL UNIQUE,
    account_name VARCHAR(60) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    bank_name VARCHAR(60),
    bank_account_no VARCHAR(40),
    bank_branch VARCHAR(100),
    currency VARCHAR(10) DEFAULT 'CNY',
    subject_code VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Storage Location (库位)
CREATE TABLE IF NOT EXISTS t_storage_location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_code VARCHAR(20) NOT NULL UNIQUE,
    location_name VARCHAR(60) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    location_type VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Account Subject (会计科目)
CREATE TABLE IF NOT EXISTS t_account_subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(60) NOT NULL,
    parent_code VARCHAR(20),
    subject_category VARCHAR(20) NOT NULL,
    balance_direction VARCHAR(10) NOT NULL,
    auxiliary_accounting VARCHAR(200),
    is_leaf BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Inventory Balance (库存期初余额)
CREATE TABLE IF NOT EXISTS t_inventory_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    location_id BIGINT,
    batch_no VARCHAR(40),
    inbound_date DATE,
    quantity DECIMAL(18,4),
    weight DECIMAL(18,4),
    pack_quantity DECIMAL(18,4),
    cost_unit_price DECIMAL(18,4),
    whole_units DECIMAL(18,4),
    odd_units DECIMAL(18,4),
    cost_amount DECIMAL(18,2),
    unit_weight DECIMAL(18,3),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Customer Balance (客户期初余额)
CREATE TABLE IF NOT EXISTS t_customer_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    doc_type VARCHAR(30),
    doc_no VARCHAR(40),
    doc_date DATE,
    receivable_amount DECIMAL(18,2),
    received_amount DECIMAL(18,2),
    balance DECIMAL(18,2) NOT NULL,
    balance_type VARCHAR(20),
    expected_date DATE,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Supplier Balance (供应商期初余额)
CREATE TABLE IF NOT EXISTS t_supplier_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    doc_type VARCHAR(30),
    doc_no VARCHAR(40),
    doc_date DATE,
    payable_amount DECIMAL(18,2),
    paid_amount DECIMAL(18,2),
    balance DECIMAL(18,2) NOT NULL,
    balance_type VARCHAR(20),
    expected_date DATE,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Account Balance (银行账户期初余额)
CREATE TABLE IF NOT EXISTS t_account_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    bank_account_id BIGINT NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    opening_balance DECIMAL(18,2) NOT NULL,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Subject Balance (科目期初余额)
CREATE TABLE IF NOT EXISTS t_subject_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    subject_name VARCHAR(60),
    debit_balance DECIMAL(18,2) DEFAULT 0,
    credit_balance DECIMAL(18,2) DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Other Receivable (其他应收)
CREATE TABLE IF NOT EXISTS t_other_receivable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    subject_code VARCHAR(20),
    object_type VARCHAR(20),
    object_id BIGINT,
    object_name VARCHAR(60),
    summary VARCHAR(200),
    amount DECIMAL(18,2),
    occur_date DATE,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Other Payable (其他应付)
CREATE TABLE IF NOT EXISTS t_other_payable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    subject_code VARCHAR(20),
    object_type VARCHAR(20),
    object_id BIGINT,
    object_name VARCHAR(60),
    summary VARCHAR(200),
    amount DECIMAL(18,2),
    occur_date DATE,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Invoice Balance (发票期初余额)
CREATE TABLE IF NOT EXISTS t_invoice_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    invoice_type VARCHAR(20) NOT NULL,
    counterparty_id BIGINT,
    counterparty_name VARCHAR(120),
    doc_no VARCHAR(40),
    product_id BIGINT,
    product_name VARCHAR(60),
    spec VARCHAR(80),
    material VARCHAR(30),
    quantity DECIMAL(18,4),
    unit_price DECIMAL(18,4),
    amount DECIMAL(18,2),
    tax_rate DECIMAL(5,4),
    tax_amount DECIMAL(18,2),
    total_amount DECIMAL(18,2),
    doc_date DATE,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Project Plan (项目计划)
CREATE TABLE IF NOT EXISTS t_project_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    total_days INT,
    milestones TEXT,
    wbs_items TEXT,
    resources TEXT,
    risks TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Required Course (KA必修课程矩阵)
CREATE TABLE IF NOT EXISTS t_required_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    industry_type VARCHAR(30) NOT NULL,
    course_module VARCHAR(50) NOT NULL,
    ka_required BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);
