-- 工单处理系统 mock 数据
-- 说明：
-- 1. 数据覆盖多角色、多状态、多分类、多优先级和超时场景
-- 2. 统计报表验证建议直接基于本脚本生成的数据执行 SQL 聚合

insert into sys_user (user_id, username, password_hash, display_name, email, phone, department_code, department_name, team_code, team_name, status, deleted) values
('U1001', 'requester.lee', '{noop}ChangeMe123!', '李晓明', 'lee@example.com', '13800000001', 'CS', '客户成功部', 'CS-1', '客户一组', 'ENABLED', 0),
('U2001', 'service.desk', '{noop}ChangeMe123!', '王客服', 'desk@example.com', '13800000002', 'OPS', '服务运营部', 'OPS-1', '客服台', 'ENABLED', 0),
('U3001', 'tech.chen', '{noop}ChangeMe123!', '陈工', 'chen@example.com', '13800000003', 'TS', '技术支持部', 'TS-APP', '应用支持组', 'ENABLED', 0),
('U3002', 'tech.zhou', '{noop}ChangeMe123!', '周工', 'zhou@example.com', '13800000004', 'TS', '技术支持部', 'TS-OPS', '运维保障组', 'ENABLED', 0),
('U4001', 'supervisor.zhao', '{noop}ChangeMe123!', '赵主管', 'zhao@example.com', '13800000005', 'TS', '技术支持部', 'TS-APP', '应用支持组', 'ENABLED', 0),
('U9001', 'admin.root', '{noop}ChangeMe123!', '系统管理员', 'admin@example.com', '13800000006', 'IT', '信息化部', 'IT-PLT', '平台管理组', 'ENABLED', 0)
on conflict (user_id) do nothing;

insert into sys_role (role_id, role_code, role_name, data_scope, deleted) values
('R1', 'REQUESTER', '提单人', 'SELF_CREATED', 0),
('R2', 'CUSTOMER_SERVICE', '客服', 'TEAM', 0),
('R3', 'TECHNICIAN', '处理人员', 'SELF_ASSIGNED', 0),
('R4', 'SUPERVISOR', '主管', 'TEAM', 0),
('R5', 'ADMIN', '管理员', 'ALL', 0)
on conflict (role_id) do nothing;

insert into sys_user_role (id, user_id, role_id) values
('UR1', 'U1001', 'R1'),
('UR2', 'U2001', 'R2'),
('UR3', 'U3001', 'R3'),
('UR4', 'U3002', 'R3'),
('UR5', 'U4001', 'R4'),
('UR6', 'U9001', 'R5')
on conflict (id) do nothing;

insert into sla_rule (rule_id, rule_code, rule_name, category_code, category_name, response_minutes, resolve_minutes, response_warning_minutes, resolve_warning_minutes, auto_escalate, deleted) values
('SLA1', 'SLA-INC-P1', 'P1 故障响应规则', 'SYSTEM_ERROR', '系统故障', 15, 120, 5, 20, true, 0),
('SLA2', 'SLA-INC-P2', 'P2 接口响应规则', 'API_ERROR', '接口异常', 30, 240, 10, 30, true, 0),
('SLA3', 'SLA-SRV-P3', '一般服务请求规则', 'ACCOUNT', '账号权限', 60, 480, 20, 60, false, 0)
on conflict (rule_id) do nothing;

insert into ticket_order (
    ticket_id, ticket_no, title, description, requester_id, requester_name, contact_phone, customer_name, department_name,
    source, ticket_type, category_code, category_name, priority, urgency_level, status,
    current_assignee_id, current_assignee_name, current_group_code, current_group_name,
    response_deadline, resolve_deadline, created_at, updated_at, accepted_at, assigned_at, completed_at, closed_at,
    satisfaction_level, satisfaction_comment, escalated, timeout, deleted
)
select
    'TID-' || gs,
    'WO20260319' || lpad(gs::text, 4, '0'),
    '企业服务工单样例 #' || gs,
    '用于验证工单流程、报表统计、SLA 和权限的模拟数据。',
    'U1001',
    '李晓明',
    '13800001234',
    case when gs % 5 = 0 then '集团财务中心' when gs % 5 = 1 then '华东大客户中心' when gs % 5 = 2 then '供应链业务中心' when gs % 5 = 3 then '营销中心' else '信息化部' end,
    case when gs % 5 = 0 then '财务部' when gs % 5 = 1 then '客户成功部' when gs % 5 = 2 then '供应链部' when gs % 5 = 3 then '营销中心' else '信息化部' end,
    case when gs % 2 = 0 then 'MANUAL_SUBMISSION' else 'SERVICE_DESK_ENTRY' end,
    case when gs % 4 = 0 then '故障' when gs % 4 = 1 then '咨询' when gs % 4 = 2 then '需求' else '变更' end,
    case when gs % 6 = 0 then 'SYSTEM_ERROR' when gs % 6 = 1 then 'ACCOUNT' when gs % 6 = 2 then 'API_ERROR' when gs % 6 = 3 then 'PERFORMANCE' when gs % 6 = 4 then 'REPORT' else 'DEPLOY' end,
    case when gs % 6 = 0 then '系统故障' when gs % 6 = 1 then '账号权限' when gs % 6 = 2 then '接口异常' when gs % 6 = 3 then '性能问题' when gs % 6 = 4 then '报表问题' else '环境部署' end,
    case when gs % 4 = 0 then 'P1' when gs % 4 = 1 then 'P2' when gs % 4 = 2 then 'P3' else 'P4' end,
    case when gs % 4 = 0 then 'CRITICAL' when gs % 4 = 1 then 'HIGH' when gs % 4 = 2 then 'MEDIUM' else 'LOW' end,
    case when gs % 10 = 0 then 'PENDING_ACCEPT'
         when gs % 10 = 1 then 'ACCEPTED'
         when gs % 10 = 2 then 'PENDING_ASSIGN'
         when gs % 10 = 3 then 'PROCESSING'
         when gs % 10 = 4 then 'PENDING_CONFIRM'
         when gs % 10 = 5 then 'COMPLETED'
         when gs % 10 = 6 then 'CLOSED'
         when gs % 10 = 7 then 'CANCELLED'
         when gs % 10 = 8 then 'SUSPENDED'
         else 'REOPENED'
    end,
    case when gs % 3 = 0 then 'U3001' else 'U3002' end,
    case when gs % 3 = 0 then '陈工' else '周工' end,
    case when gs % 3 = 0 then 'TS-APP' else 'TS-OPS' end,
    case when gs % 3 = 0 then '应用支持组' else '运维保障组' end,
    now() - (gs || ' hours')::interval + interval '2 hours',
    now() - (gs || ' hours')::interval + interval '8 hours',
    now() - (gs || ' hours')::interval,
    now() - (gs || ' hours')::interval + interval '30 minutes',
    now() - (gs || ' hours')::interval + interval '20 minutes',
    now() - (gs || ' hours')::interval + interval '40 minutes',
    case when gs % 10 in (4,5,6) then now() - (gs || ' hours')::interval + interval '4 hours' else null end,
    case when gs % 10 = 6 then now() - (gs || ' hours')::interval + interval '5 hours' else null end,
    case when gs % 10 in (5,6) then '满意' else null end,
    case when gs % 10 in (5,6) then '问题处理及时' else null end,
    (gs % 6 = 0),
    (gs % 7 = 0),
    0
from generate_series(1, 80) as gs
on conflict (ticket_id) do nothing;

insert into ticket_flow_record (record_id, ticket_id, action_type, operator_id, operator_name, from_status, to_status, remark, operated_at, deleted)
select
    'FLOW-' || gs,
    'TID-' || gs,
    case when gs % 4 = 0 then 'ASSIGN' when gs % 4 = 1 then 'ACCEPT' when gs % 4 = 2 then 'PROCESS' else 'SUBMIT_SOLUTION' end,
    case when gs % 2 = 0 then 'U2001' else 'U3001' end,
    case when gs % 2 = 0 then '王客服' else '陈工' end,
    case when gs % 2 = 0 then 'PENDING_ACCEPT' else 'PROCESSING' end,
    case when gs % 2 = 0 then 'ACCEPTED' else 'PENDING_CONFIRM' end,
    '自动生成的流转记录，用于验证时间线与审计。',
    now() - (gs || ' hours')::interval,
    0
from generate_series(1, 120) as gs
on conflict (record_id) do nothing;

insert into ticket_comment_record (comment_id, ticket_id, author_id, author_name, author_role, content, created_at, deleted)
select
    'CMT-' || gs,
    'TID-' || ((gs % 40) + 1),
    case when gs % 3 = 0 then 'U1001' when gs % 3 = 1 then 'U2001' else 'U3001' end,
    case when gs % 3 = 0 then '李晓明' when gs % 3 = 1 then '王客服' else '陈工' end,
    case when gs % 3 = 0 then 'REQUESTER' when gs % 3 = 1 then 'CUSTOMER_SERVICE' else 'TECHNICIAN' end,
    '这是用于验证沟通记录的模拟评论 #' || gs,
    now() - (gs || ' minutes')::interval,
    0
from generate_series(1, 90) as gs
on conflict (comment_id) do nothing;

insert into notification_message (message_id, receiver_id, receiver_name, channel, title, content, read_flag, sent_at) values
('MSG-1', 'U2001', '王客服', 'INTERNAL', '新工单待受理', '工单 WO202603190012 待受理', false, now() - interval '20 minutes'),
('MSG-2', 'U3001', '陈工', 'INTERNAL', 'SLA 即将超时', '工单 WO202603190018 即将解决超时', false, now() - interval '40 minutes'),
('MSG-3', 'U1001', '李晓明', 'EMAIL', '工单待确认', '您的工单已处理完成，请确认', true, now() - interval '3 hours')
on conflict (message_id) do nothing;

insert into audit_log_record (log_id, module_name, operation_name, operator_id, operator_name, request_path, result, operated_at) values
('AUD-1', '工单管理', '创建工单', 'U1001', '李晓明', '/api/v1/tickets', 'SUCCESS', now() - interval '8 hours'),
('AUD-2', '分派中心', '分派工单', 'U2001', '王客服', '/api/v1/dispatch/board', 'SUCCESS', now() - interval '6 hours'),
('AUD-3', 'SLA 管理', '升级工单', 'U4001', '赵主管', '/api/v1/sla/overview', 'SUCCESS', now() - interval '2 hours'),
('AUD-4', '系统配置', '调整角色权限', 'U9001', '系统管理员', '/api/v1/system/overview', 'SUCCESS', now() - interval '1 hour')
on conflict (log_id) do nothing;
