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
    special_requirements VARCHAR(500),
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
    -- basic_info extended
    legal_person VARCHAR(30),
    registered_capital VARCHAR(30),
    establishment_date DATE,
    address VARCHAR(200),
    -- production
    total_production_lines INT DEFAULT 0,
    production_shifts VARCHAR(20),
    mes_current_status VARCHAR(20),
    quality_standards VARCHAR(200),
    -- warehouse & inventory
    total_warehouse_count INT DEFAULT 0,
    total_warehouse_area_sqm DECIMAL(18,2),
    total_crane_count INT DEFAULT 0,
    inventory_turnover_rate DECIMAL(8,2),
    inventory_management_method VARCHAR(30),
    -- sales
    monthly_volume DECIMAL(18,2),
    monthly_amount DECIMAL(18,2),
    pricing_model VARCHAR(30),
    settlement_methods VARCHAR(100),
    credit_policy VARCHAR(200),
    sales_mode VARCHAR(30),
    -- customer_base
    total_customer_count INT,
    customer_types VARCHAR(200),
    top_customers VARCHAR(500),
    -- organization
    total_staff INT DEFAULT 0,
    departments VARCHAR(500),
    key_positions VARCHAR(500),
    decision_chain VARCHAR(200),
    -- existing_systems
    existing_systems VARCHAR(1000),
    -- project_scope
    target_modules VARCHAR(500),
    module_priorities VARCHAR(500),
    -- goals
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
    report_content CLOB,
    auto_generated BOOLEAN DEFAULT FALSE,
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

-- Simulation Report (模拟演练报告)
CREATE TABLE IF NOT EXISTS t_simulation_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    total_scenes INT DEFAULT 0,
    passed_scenes INT DEFAULT 0,
    failed_scenes INT DEFAULT 0,
    overall_pass_rate DECIMAL(6,2),
    overall_score DECIMAL(6,2),
    ready_for_training BOOLEAN DEFAULT FALSE,
    report_content TEXT,
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
    plan_uploaded BOOLEAN DEFAULT FALSE,
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
    customization_level VARCHAR(20),
    deadline DATE,
    budget VARCHAR(50),
    integration_requirements VARCHAR(500),
    gantt_data TEXT,
    risk_level VARCHAR(10),
    auto_generated BOOLEAN DEFAULT FALSE,
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

-- Client User (客户端用户 — 培训学员)
CREATE TABLE IF NOT EXISTS t_client_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_name VARCHAR(30) NOT NULL,
    role VARCHAR(30),
    department VARCHAR(30),
    points INT DEFAULT 100,
    access_token VARCHAR(64) NOT NULL UNIQUE,
    token_expires_at TIMESTAMP,
    enabled BOOLEAN DEFAULT TRUE,
    last_active_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- QA Session (问答会话)
CREATE TABLE IF NOT EXISTS t_qa_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    client_user_id BIGINT NOT NULL,
    title VARCHAR(60),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    message_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- QA Message (问答消息)
CREATE TABLE IF NOT EXISTS t_qa_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    related_module VARCHAR(60),
    related_video_url VARCHAR(500),
    helpful BOOLEAN,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Import Progress (数据导入进度)
CREATE TABLE IF NOT EXISTS t_import_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL UNIQUE,
    current_batch INT DEFAULT 1,
    batch1_status VARCHAR(20) DEFAULT 'PENDING',
    batch2_status VARCHAR(20) DEFAULT 'PENDING',
    batch3_status VARCHAR(20) DEFAULT 'PENDING',
    batch4_status VARCHAR(20) DEFAULT 'PENDING',
    batch5_status VARCHAR(20) DEFAULT 'PENDING',
    overall_status VARCHAR(20) DEFAULT 'NOT_STARTED',
    last_error TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Support Ticket (工单管理)
CREATE TABLE IF NOT EXISTS t_support_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    reporter_id BIGINT,
    reporter_name VARCHAR(60),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    level VARCHAR(10),
    category VARCHAR(20),
    status VARCHAR(20) DEFAULT 'OPEN',
    assigned_to VARCHAR(60),
    resolution TEXT,
    resolved_at TIMESTAMP,
    ai_suggestion TEXT,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- System Alert (系统告警)
CREATE TABLE IF NOT EXISTS t_system_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    alert_type VARCHAR(30),
    severity VARCHAR(20),
    title VARCHAR(200),
    description TEXT,
    suggestion TEXT,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(60),
    acknowledged_at TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Research Report (调研分析报告)
CREATE TABLE IF NOT EXISTS t_research_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    report_content TEXT,
    overall_risk_level VARCHAR(10),
    status VARCHAR(20) DEFAULT 'DRAFT',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- After Sales Ticket (售后工单)
CREATE TABLE IF NOT EXISTS t_after_sales_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    customer_name VARCHAR(100),
    reporter_name VARCHAR(30),
    reporter_contact VARCHAR(30),
    channel VARCHAR(20),
    intent_type VARCHAR(20),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    sla_priority VARCHAR(10),
    sla_deadline TIMESTAMP,
    status VARCHAR(20) DEFAULT 'NEW',
    assigned_engineer_id BIGINT,
    assigned_engineer_name VARCHAR(30),
    resolution TEXT,
    resolved_at TIMESTAMP,
    knowledge_created BOOLEAN DEFAULT FALSE,
    customer_satisfaction INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Customer Health Record (客户健康度)
CREATE TABLE IF NOT EXISTS t_customer_health (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    customer_name VARCHAR(100),
    check_date DATE,
    ticket_count30d INT DEFAULT 0,
    ticket_trend VARCHAR(10),
    open_ticket_count INT DEFAULT 0,
    avg_resolution_hours DECIMAL(10,2),
    customer_satisfaction_avg DECIMAL(4,2),
    health_score INT DEFAULT 100,
    health_level VARCHAR(20),
    care_actions TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Video Resource (视频资源库)
CREATE TABLE IF NOT EXISTS t_video_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(30) NOT NULL,
    function_name VARCHAR(60) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    video_url VARCHAR(500) NOT NULL,
    duration INT,
    sort_order INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Video Clip (视频片段)
CREATE TABLE IF NOT EXISTS t_video_clip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    clip_title VARCHAR(100) NOT NULL,
    start_second INT DEFAULT 0,
    end_second INT DEFAULT 0,
    related_page VARCHAR(60),
    related_field VARCHAR(60),
    operation_step TEXT,
    subtitle_text TEXT,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Input Assist Log (录入辅助日志)
CREATE TABLE IF NOT EXISTS t_input_assist_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    page VARCHAR(60) NOT NULL,
    field VARCHAR(60),
    trigger_type VARCHAR(30),
    question TEXT,
    answer TEXT,
    video_clip_id BIGINT,
    video_played BOOLEAN DEFAULT FALSE,
    resolved BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Field Help Content (字段帮助内容)
CREATE TABLE IF NOT EXISTS t_field_help_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    page VARCHAR(60) NOT NULL,
    field_name VARCHAR(60) NOT NULL,
    help_text TEXT NOT NULL,
    format_example VARCHAR(200),
    common_errors TEXT,
    related_video_clip_id BIGINT,
    sort_order INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Knowledge Entry (知识库)
CREATE TABLE IF NOT EXISTS t_knowledge_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(30) NOT NULL,
    layer VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    keywords TEXT,
    source TEXT,
    project_id BIGINT,
    access_level VARCHAR(20) DEFAULT 'PUBLIC',
    view_count INT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(60),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Agent Decision Log (智能体决策日志)
CREATE TABLE IF NOT EXISTS t_agent_decision_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_code VARCHAR(30) NOT NULL,
    project_id BIGINT,
    user_id BIGINT,
    trigger_type VARCHAR(20),
    input_summary TEXT,
    output_summary TEXT,
    model_used TEXT,
    knowledge_used TEXT,
    confidence DECIMAL(5,4),
    execution_time_ms INT,
    human_reviewed BOOLEAN DEFAULT FALSE,
    review_result VARCHAR(20),
    review_comment TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Course (课程)
CREATE TABLE IF NOT EXISTS t_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    industry_type VARCHAR(30),
    module VARCHAR(50),
    cover_image_url VARCHAR(500),
    total_chapters INT DEFAULT 0,
    total_duration INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Course Chapter (课程章节)
CREATE TABLE IF NOT EXISTS t_course_chapter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    chapter_number INT NOT NULL,
    chapter_name VARCHAR(100) NOT NULL,
    description TEXT,
    video_url VARCHAR(500) NOT NULL,
    video_duration INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Learning Progress (学习进度)
CREATE TABLE IF NOT EXISTS t_learning_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    chapter_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'NOT_STARTED',
    watched_duration INT DEFAULT 0,
    completed_at TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Exam Paper (在线考试试卷)
CREATE TABLE IF NOT EXISTS t_exam_paper (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT,
    module VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    title VARCHAR(200),
    total_questions INT DEFAULT 0,
    total_score INT DEFAULT 100,
    questions TEXT,
    created_for BIGINT,
    status VARCHAR(20) DEFAULT 'CREATED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- Exam Submission (考试提交)
CREATE TABLE IF NOT EXISTS t_exam_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paper_id BIGINT NOT NULL,
    client_user_id BIGINT NOT NULL,
    answers TEXT,
    score INT DEFAULT 0,
    passed BOOLEAN DEFAULT FALSE,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    wrong_questions TEXT,
    started_at TIMESTAMP,
    submitted_at TIMESTAMP,
    duration INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- User Video (UGC视频)
CREATE TABLE IF NOT EXISTS t_user_video (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publisher_id BIGINT NOT NULL,
    publisher_name VARCHAR(30),
    project_id BIGINT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category_module VARCHAR(50),
    video_url VARCHAR(500) NOT NULL,
    video_duration INT DEFAULT 0,
    thumbnail_url VARCHAR(500),
    points_cost INT DEFAULT 0,
    total_views INT DEFAULT 0,
    total_learners INT DEFAULT 0,
    publisher_earned_points INT DEFAULT 0,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    approved_by VARCHAR(60),
    approved_at TIMESTAMP,
    rejection_reason VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- User Video Learning (UGC视频学习记录)
CREATE TABLE IF NOT EXISTS t_user_video_learning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    learner_id BIGINT NOT NULL,
    learner_name VARCHAR(30),
    watched_duration INT DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    points_paid INT DEFAULT 0,
    completed_at TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Points Transaction (积分交易记录)
CREATE TABLE IF NOT EXISTS t_points_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(30),
    transaction_type VARCHAR(30) NOT NULL,
    amount INT NOT NULL,
    balance_before INT NOT NULL,
    balance_after INT NOT NULL,
    related_video_id BIGINT,
    description TEXT,
    external_api_called BOOLEAN DEFAULT FALSE,
    external_api_response TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
