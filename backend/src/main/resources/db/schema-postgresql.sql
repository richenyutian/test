-- 工单处理系统 PostgreSQL 16 初始化脚本
-- 说明：
-- 1. 该脚本用于快速初始化核心表结构
-- 2. 命名采用真实项目常见的 sys_* / ticket_* / sla_* / audit_* 前缀

create table if not exists sys_user (
    user_id varchar(64) primary key,
    username varchar(64) not null unique,
    password_hash varchar(255) not null,
    display_name varchar(64) not null,
    email varchar(128),
    phone varchar(32),
    department_code varchar(64),
    department_name varchar(64),
    team_code varchar(64),
    team_name varchar(64),
    status varchar(32) not null default 'ENABLED',
    deleted smallint not null default 0
);

create table if not exists sys_role (
    role_id varchar(64) primary key,
    role_code varchar(64) not null unique,
    role_name varchar(64) not null,
    data_scope varchar(32) not null,
    deleted smallint not null default 0
);

create table if not exists sys_user_role (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    role_id varchar(64) not null
);

create table if not exists sla_rule (
    rule_id varchar(64) primary key,
    rule_code varchar(64) not null unique,
    rule_name varchar(128) not null,
    category_code varchar(64) not null,
    category_name varchar(64) not null,
    response_minutes integer not null,
    resolve_minutes integer not null,
    response_warning_minutes integer not null,
    resolve_warning_minutes integer not null,
    auto_escalate boolean not null default false,
    deleted smallint not null default 0
);

create table if not exists ticket_order (
    ticket_id varchar(64) primary key,
    ticket_no varchar(64) not null unique,
    title varchar(255) not null,
    description text not null,
    requester_id varchar(64) not null,
    requester_name varchar(64) not null,
    contact_phone varchar(32),
    customer_name varchar(128),
    department_name varchar(64),
    source varchar(32) not null,
    ticket_type varchar(32) not null,
    category_code varchar(64) not null,
    category_name varchar(64) not null,
    priority varchar(16) not null,
    urgency_level varchar(16) not null,
    status varchar(32) not null,
    current_assignee_id varchar(64),
    current_assignee_name varchar(64),
    current_group_code varchar(64),
    current_group_name varchar(64),
    response_deadline timestamp,
    resolve_deadline timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,
    accepted_at timestamp,
    assigned_at timestamp,
    completed_at timestamp,
    closed_at timestamp,
    satisfaction_level varchar(16),
    satisfaction_comment varchar(255),
    escalated boolean not null default false,
    timeout boolean not null default false,
    deleted smallint not null default 0
);

create table if not exists ticket_flow_record (
    record_id varchar(64) primary key,
    ticket_id varchar(64) not null,
    action_type varchar(32) not null,
    operator_id varchar(64),
    operator_name varchar(64) not null,
    from_status varchar(32),
    to_status varchar(32),
    remark varchar(500),
    operated_at timestamp not null,
    deleted smallint not null default 0
);

create table if not exists ticket_comment_record (
    comment_id varchar(64) primary key,
    ticket_id varchar(64) not null,
    author_id varchar(64),
    author_name varchar(64) not null,
    author_role varchar(32) not null,
    content text not null,
    created_at timestamp not null,
    deleted smallint not null default 0
);

create table if not exists notification_message (
    message_id varchar(64) primary key,
    receiver_id varchar(64) not null,
    receiver_name varchar(64) not null,
    channel varchar(32) not null,
    title varchar(128) not null,
    content varchar(500) not null,
    read_flag boolean not null default false,
    sent_at timestamp not null
);

create table if not exists audit_log_record (
    log_id varchar(64) primary key,
    module_name varchar(64) not null,
    operation_name varchar(64) not null,
    operator_id varchar(64),
    operator_name varchar(64) not null,
    request_path varchar(255),
    result varchar(32) not null,
    operated_at timestamp not null
);

create index if not exists idx_ticket_order_status on ticket_order(status);
create index if not exists idx_ticket_order_category on ticket_order(category_code);
create index if not exists idx_ticket_order_assignee on ticket_order(current_assignee_id);
create index if not exists idx_ticket_flow_record_ticket_id on ticket_flow_record(ticket_id);
