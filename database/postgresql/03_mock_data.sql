-- ============================================
-- 工单处理系统 PostgreSQL 16 业务样例数据
-- ============================================

insert into ticket_order (
    ticket_id, ticket_no, title, description, requester_user_id, requester_name, contact_phone,
    external_user_flag, source_code, ticket_type_code, category_code, priority_code, current_status,
    current_handler_user_id, current_handle_group_id, escalated_flag, timeout_flag, response_timeout_flag, resolve_timeout_flag,
    response_warn_notified_flag, resolve_warn_notified_flag, response_sla_minutes, resolve_sla_minutes,
    response_deadline, resolve_deadline, response_finished_at, resolve_finished_at, accepted_at, assigned_at,
    processed_at, user_confirmed_at, closed_at, resolution_summary, reopen_count, version, deleted_flag, created_at, updated_at
)
values
    (1001, 'WO202603190001', '生产环境接口超时', '支付核心链路接口偶发超时，需要紧急定位。', 5, '提单人演示账号', '13800000005',
     1, 'USER_SUBMIT', 'INCIDENT', 'APPLICATION', 'P1', 'PROCESSING',
     3, 1, 0, 0, 0, 0,
     0, 0, 15, 120,
     current_timestamp + interval '10 minutes', current_timestamp + interval '90 minutes',
     current_timestamp - interval '5 minutes', null, current_timestamp - interval '5 minutes', current_timestamp - interval '4 minutes',
     null, null, null, null, 0, 0, 0, current_timestamp - interval '30 minutes', current_timestamp - interval '2 minutes'),

    (1002, 'WO202603190002', '数据库连接池告警', '数据库连接数接近阈值，请尽快处理。', 5, '提单人演示账号', '13800000005',
     1, 'USER_SUBMIT', 'INCIDENT', 'DATABASE', 'P2', 'PENDING_CONFIRM',
     4, 2, 1, 0, 0, 0,
     1, 1, 30, 240,
     current_timestamp - interval '1 hours', current_timestamp + interval '1 hours',
     current_timestamp - interval '1 hours', current_timestamp - interval '10 minutes', current_timestamp - interval '2 hours', current_timestamp - interval '110 minutes',
     current_timestamp - interval '10 minutes', null, null, '已调整连接池参数并重启相关服务。', 0, 0, 0, current_timestamp - interval '3 hours', current_timestamp - interval '10 minutes'),

    (1003, 'WO202603190003', '账号权限开通申请', '新入职同事需要开通生产只读权限。', 5, '提单人演示账号', '13800000005',
     1, 'USER_SUBMIT', 'REQUEST', 'ACCOUNT', 'P3', 'PENDING_ACCEPT',
     null, null, 0, 0, 0, 0,
     0, 0, 60, 480,
     current_timestamp + interval '30 minutes', current_timestamp + interval '7 hours',
     null, null, null, null,
     null, null, null, null, 0, 0, 0, current_timestamp - interval '20 minutes', current_timestamp - interval '20 minutes'),

    (1004, 'WO202603190004', '夜间批处理失败', '夜间批处理任务失败，需研发分析。', 5, '提单人演示账号', '13800000005',
     1, 'SERVICE_ENTRY', 'INCIDENT', 'APPLICATION', 'P2', 'SUSPENDED',
     4, 2, 0, 1, 0, 1,
     1, 1, 30, 240,
     current_timestamp - interval '4 hours', current_timestamp - interval '30 minutes',
     current_timestamp - interval '5 hours', null, current_timestamp - interval '5 hours', current_timestamp - interval '4 hours',
     null, null, null, null, 0, 0, 0, current_timestamp - interval '6 hours', current_timestamp - interval '15 minutes')
on conflict (ticket_id) do nothing;

insert into ticket_flow_record (
    flow_record_id, ticket_id, action_code, from_status, to_status, operator_user_id, operator_name, operate_description, operate_time, request_path, deleted_flag
)
values
    (2001, 1001, 'CREATE', null, 'PENDING_ACCEPT', 5, '提单人演示账号', '创建工单', current_timestamp - interval '30 minutes', '/api/v1/tickets', 0),
    (2002, 1001, 'ACCEPT', 'PENDING_ACCEPT', 'ACCEPTED', 3, '运维演示账号', '受理工单', current_timestamp - interval '5 minutes', '/api/v1/tickets/1001/accept', 0),
    (2003, 1001, 'ASSIGN', 'ACCEPTED', 'PENDING_ASSIGN', 2, '主管演示账号', '分派至运维组', current_timestamp - interval '4 minutes', '/api/v1/tickets/1001/assign', 0),
    (2004, 1001, 'PROCESS', 'PENDING_ASSIGN', 'PROCESSING', 3, '运维演示账号', '接单开始处理', current_timestamp - interval '2 minutes', '/api/v1/tickets/1001/claim', 0),
    (2005, 1002, 'CREATE', null, 'PENDING_ACCEPT', 5, '提单人演示账号', '创建工单', current_timestamp - interval '3 hours', '/api/v1/tickets', 0),
    (2006, 1002, 'SUBMIT_SOLUTION', 'PROCESSING', 'PENDING_CONFIRM', 4, '研发演示账号', '提交处理结果，待用户确认', current_timestamp - interval '10 minutes', '/api/v1/tickets/1002/complete', 0)
on conflict (flow_record_id) do nothing;

insert into sys_notice (
    notice_id, receiver_user_id, notice_title, notice_content, notice_type, business_type, business_id, read_flag, read_time, created_at, deleted_flag
)
values
    (3001, 3, '工单已分派', '工单 WO202603190001 已分派给你，请及时处理。', 'ASSIGN', 'TICKET', 1001, 0, null, current_timestamp - interval '4 minutes', 0),
    (3002, 5, '工单待确认', '工单 WO202603190002 已处理完成，请确认结果。', 'CONFIRM', 'TICKET', 1002, 0, null, current_timestamp - interval '10 minutes', 0),
    (3003, 4, '工单解决已超时', '工单 WO202603190004 已发生解决超时。', 'SLA', 'TICKET', 1004, 0, null, current_timestamp - interval '15 minutes', 0)
on conflict (notice_id) do nothing;

insert into sys_audit_log (
    audit_log_id, operator_user_id, operator_name, module_name, operation_type, business_id, operation_description, request_path, request_ip, created_at
)
values
    (4001, 5, 'requester_demo', '认证中心', '登录', null, '登录成功', '/api/v1/auth/login', '127.0.0.1', current_timestamp - interval '4 hours'),
    (4002, 5, 'requester_demo', '工单管理', '创建工单', 1001, '创建成功', '/api/v1/tickets', '127.0.0.1', current_timestamp - interval '30 minutes'),
    (4003, 3, 'ops_demo', '工单管理', '受理工单', 1001, '受理成功', '/api/v1/tickets/1001/accept', '127.0.0.1', current_timestamp - interval '5 minutes'),
    (4004, 2, 'supervisor_demo', '工单管理', '分派工单', 1001, '分派成功', '/api/v1/tickets/1001/assign', '127.0.0.1', current_timestamp - interval '4 minutes'),
    (4005, 1, 'admin', 'SLA 管理', '修改SLA规则', 2, '修改成功', '/api/v1/sla/rules/2', '127.0.0.1', current_timestamp - interval '2 hours')
on conflict (audit_log_id) do nothing;

select setval(pg_get_serial_sequence('ticket_order', 'ticket_id'), coalesce((select max(ticket_id) from ticket_order), 1), true);
select setval(pg_get_serial_sequence('ticket_flow_record', 'flow_record_id'), coalesce((select max(flow_record_id) from ticket_flow_record), 1), true);
select setval(pg_get_serial_sequence('sys_notice', 'notice_id'), coalesce((select max(notice_id) from sys_notice), 1), true);
select setval(pg_get_serial_sequence('sys_audit_log', 'audit_log_id'), coalesce((select max(audit_log_id) from sys_audit_log), 1), true);
