-- ============================================
-- 工单处理系统 PostgreSQL 16 初始化数据
-- ============================================

insert into sys_role (role_id, role_code, role_name, data_scope, status, remark)
values
    (1, 'REQUESTER', '提单人', 'SELF_CREATED', 'ENABLED', '外部与内部提单用户'),
    (2, 'OPS', '运维人员', 'TEAM', 'ENABLED', '负责受理与处理运维类工单'),
    (3, 'RD', '技术研发人员', 'TEAM', 'ENABLED', '负责处理研发类工单'),
    (4, 'SUPERVISOR', '主管', 'TEAM', 'ENABLED', '负责分派、升级、关闭与报表查看'),
    (5, 'ADMIN', '管理员', 'ALL', 'ENABLED', '拥有全量系统管理能力')
on conflict (role_id) do nothing;

insert into biz_handle_group (handle_group_id, group_code, group_name, group_type, leader_user_id, leader_name, status, remark)
values
    (1, 'OPS_TEAM', '运维处理组', 'OPS', null, null, 'ENABLED', '负责运维处理与受理'),
    (2, 'RD_TEAM', '研发处理组', 'RD', null, null, 'ENABLED', '负责研发缺陷与需求处理'),
    (3, 'SUP_TEAM', '主管组', 'MANAGEMENT', null, null, 'ENABLED', '负责工单升级与协调')
on conflict (handle_group_id) do nothing;

insert into sys_user (
    user_id, username, display_name, user_type, external_user_flag, phone, email, status, sso_subject, remark
)
values
    (1, 'admin', '系统管理员', 'INTERNAL', 0, '13800000001', 'admin@demo.local', 'ENABLED', 'admin', '默认管理员账号'),
    (2, 'supervisor_demo', '主管演示账号', 'INTERNAL', 0, '13800000002', 'supervisor@demo.local', 'ENABLED', 'supervisor_demo', '默认主管账号'),
    (3, 'ops_demo', '运维演示账号', 'INTERNAL', 0, '13800000003', 'ops@demo.local', 'ENABLED', 'ops_demo', '默认运维账号'),
    (4, 'rd_demo', '研发演示账号', 'INTERNAL', 0, '13800000004', 'rd@demo.local', 'ENABLED', 'rd_demo', '默认研发账号'),
    (5, 'requester_demo', '提单人演示账号', 'EXTERNAL', 1, '13800000005', 'requester@demo.local', 'ENABLED', 'requester_demo', '默认提单人账号')
on conflict (user_id) do nothing;

insert into biz_handle_group_member (group_member_id, handle_group_id, user_id, leader_flag, primary_group_flag)
values
    (1, 3, 2, 1, 1),
    (2, 1, 3, 1, 1),
    (3, 2, 4, 1, 1)
on conflict (group_member_id) do nothing;

update biz_handle_group set leader_user_id = 2, leader_name = '主管演示账号' where handle_group_id = 3;
update biz_handle_group set leader_user_id = 3, leader_name = '运维演示账号' where handle_group_id = 1;
update biz_handle_group set leader_user_id = 4, leader_name = '研发演示账号' where handle_group_id = 2;

insert into sys_user_role (user_role_id, user_id, role_id)
values
    (1, 1, 5),
    (2, 2, 4),
    (3, 3, 2),
    (4, 4, 3),
    (5, 5, 1)
on conflict (user_role_id) do nothing;

insert into sys_menu (menu_id, parent_id, menu_name, menu_type, route_path, component_path, permission_code, icon, sort_order, visible_flag, enabled_flag, keep_alive_flag, remark)
values
    (1, 0, '工作台', 'MENU', '/dashboard', 'dashboard/DashboardView', 'dashboard:view', 'DataAnalysis', 1, 1, 1, 1, '系统首页'),
    (2, 0, '系统管理', 'MENU', '/system', null, 'system:view', 'Setting', 2, 1, 1, 1, '系统管理目录'),
    (3, 2, '用户管理', 'MENU', '/system/users', 'system/user/UserListView', 'sys:user:view', 'User', 1, 1, 1, 1, '用户管理页面'),
    (4, 2, '角色管理', 'MENU', '/system/roles', 'system/role/RoleListView', 'sys:role:view', 'Avatar', 2, 1, 1, 1, '角色管理页面'),
    (5, 2, '菜单管理', 'MENU', '/system/menus', 'system/menu/MenuListView', 'sys:menu:view', 'Menu', 3, 1, 1, 1, '菜单管理页面'),
    (6, 2, '处理组管理', 'MENU', '/system/groups', 'system/group/HandleGroupListView', 'sys:group:view', 'Connection', 4, 1, 1, 1, '处理组管理页面'),
    (7, 0, '工单管理', 'MENU', '/tickets', 'ticket/TicketListView', 'ticket:view', 'Document', 3, 1, 1, 1, '工单模块'),
    (8, 0, 'SLA 管理', 'MENU', '/sla', 'sla/SlaRuleView', 'ticket:sla:view', 'AlarmClock', 4, 1, 1, 1, 'SLA 配置页面'),
    (9, 0, '站内消息', 'MENU', '/notices', 'notice/NoticeListView', 'notice:view', 'Bell', 5, 1, 1, 1, '站内消息页面'),
    (10, 0, '审计日志', 'MENU', '/audit', 'audit/AuditLogView', 'audit:view', 'Notebook', 6, 1, 1, 1, '审计日志页面'),
    (11, 0, '报表中心', 'MENU', '/reports', 'report/ReportView', 'report:view', 'PieChart', 7, 1, 1, 1, '报表页面'),

    (101, 3, '用户新增', 'BUTTON', null, null, 'sys:user:create', null, 1, 1, 1, 0, '用户新增按钮'),
    (102, 3, '用户修改', 'BUTTON', null, null, 'sys:user:update', null, 2, 1, 1, 0, '用户修改按钮'),
    (103, 4, '角色新增', 'BUTTON', null, null, 'sys:role:create', null, 1, 1, 1, 0, '角色新增按钮'),
    (104, 4, '角色修改', 'BUTTON', null, null, 'sys:role:update', null, 2, 1, 1, 0, '角色修改按钮'),
    (105, 5, '菜单新增', 'BUTTON', null, null, 'sys:menu:create', null, 1, 1, 1, 0, '菜单新增按钮'),
    (106, 5, '菜单修改', 'BUTTON', null, null, 'sys:menu:update', null, 2, 1, 1, 0, '菜单修改按钮'),
    (107, 6, '处理组新增', 'BUTTON', null, null, 'sys:group:create', null, 1, 1, 1, 0, '处理组新增按钮'),
    (108, 6, '处理组修改', 'BUTTON', null, null, 'sys:group:update', null, 2, 1, 1, 0, '处理组修改按钮'),
    (201, 7, '创建工单', 'BUTTON', null, null, 'ticket:create', null, 1, 1, 1, 0, '创建工单按钮'),
    (202, 7, '受理工单', 'BUTTON', null, null, 'ticket:accept', null, 2, 1, 1, 0, '受理工单按钮'),
    (203, 7, '分派工单', 'BUTTON', null, null, 'ticket:assign', null, 3, 1, 1, 0, '分派工单按钮'),
    (204, 7, '转派工单', 'BUTTON', null, null, 'ticket:transfer', null, 4, 1, 1, 0, '转派工单按钮'),
    (205, 7, '挂起工单', 'BUTTON', null, null, 'ticket:suspend', null, 5, 1, 1, 0, '挂起工单按钮'),
    (206, 7, '完成工单', 'BUTTON', null, null, 'ticket:finish', null, 6, 1, 1, 0, '完成工单按钮'),
    (207, 7, '关闭工单', 'BUTTON', null, null, 'ticket:close', null, 7, 1, 1, 0, '关闭工单按钮'),
    (208, 7, '重开工单', 'BUTTON', null, null, 'ticket:reopen', null, 8, 1, 1, 0, '重开工单按钮'),
    (209, 7, '升级工单', 'BUTTON', null, null, 'ticket:escalate', null, 9, 1, 1, 0, '升级工单按钮'),
    (301, 8, 'SLA修改', 'BUTTON', null, null, 'ticket:sla:update', null, 1, 1, 1, 0, 'SLA 修改按钮')
on conflict (menu_id) do nothing;

insert into sys_role_menu (role_menu_id, role_id, menu_id)
values
    -- admin
    (1, 5, 1),(2, 5, 2),(3, 5, 3),(4, 5, 4),(5, 5, 5),(6, 5, 6),(7, 5, 7),(8, 5, 8),(9, 5, 9),(10, 5, 10),(11, 5, 11),
    (12, 5, 101),(13, 5, 102),(14, 5, 103),(15, 5, 104),(16, 5, 105),(17, 5, 106),(18, 5, 107),(19, 5, 108),
    (20, 5, 201),(21, 5, 202),(22, 5, 203),(23, 5, 204),(24, 5, 205),(25, 5, 206),(26, 5, 207),(27, 5, 208),(28, 5, 209),(29, 5, 301),
    -- supervisor
    (30, 4, 1),(31, 4, 7),(32, 4, 8),(33, 4, 9),(34, 4, 10),(35, 4, 11),
    (36, 4, 202),(37, 4, 203),(38, 4, 204),(39, 4, 205),(40, 4, 206),(41, 4, 207),(42, 4, 208),(43, 4, 209),
    -- ops
    (44, 2, 1),(45, 2, 7),(46, 2, 9),
    (47, 2, 201),(48, 2, 202),(49, 2, 203),(50, 2, 204),(51, 2, 205),(52, 2, 206),
    -- rd
    (53, 3, 1),(54, 3, 7),(55, 3, 9),
    (56, 3, 204),(57, 3, 205),(58, 3, 206),
    -- requester
    (59, 1, 1),(60, 1, 7),(61, 1, 9),
    (62, 1, 201),(63, 1, 208)
on conflict (role_menu_id) do nothing;

insert into sys_dict_type (dict_type_id, dict_type_code, dict_type_name, status, remark)
values
    (1, 'ticket_source', '工单来源', 'ENABLED', '工单来源字典'),
    (2, 'ticket_type', '工单类型', 'ENABLED', '工单类型字典'),
    (3, 'ticket_category', '工单分类', 'ENABLED', '工单分类字典'),
    (4, 'ticket_priority', '优先级', 'ENABLED', '优先级字典')
on conflict (dict_type_id) do nothing;

insert into sys_dict_item (dict_item_id, dict_type_id, item_code, item_name, item_value, sort_order, status)
values
    (1, 1, 'USER_SUBMIT', '用户提交', 'USER_SUBMIT', 1, 'ENABLED'),
    (2, 1, 'SERVICE_ENTRY', '客服录入', 'SERVICE_ENTRY', 2, 'ENABLED'),
    (3, 2, 'INCIDENT', '故障', 'INCIDENT', 1, 'ENABLED'),
    (4, 2, 'REQUEST', '需求', 'REQUEST', 2, 'ENABLED'),
    (5, 2, 'CHANGE', '变更', 'CHANGE', 3, 'ENABLED'),
    (6, 2, 'CONSULT', '咨询', 'CONSULT', 4, 'ENABLED'),
    (7, 3, 'ACCOUNT', '账号权限', 'ACCOUNT', 1, 'ENABLED'),
    (8, 3, 'NETWORK', '网络链路', 'NETWORK', 2, 'ENABLED'),
    (9, 3, 'DATABASE', '数据库', 'DATABASE', 3, 'ENABLED'),
    (10, 3, 'APPLICATION', '应用系统', 'APPLICATION', 4, 'ENABLED'),
    (11, 4, 'P1', 'P1', 'P1', 1, 'ENABLED'),
    (12, 4, 'P2', 'P2', 'P2', 2, 'ENABLED'),
    (13, 4, 'P3', 'P3', 'P3', 3, 'ENABLED'),
    (14, 4, 'P4', 'P4', 'P4', 4, 'ENABLED')
on conflict (dict_item_id) do nothing;

insert into ticket_sla_rule (sla_rule_id, priority_code, response_limit_minutes, resolve_limit_minutes, enabled_flag, remark)
values
    (1, 'P1', 15, 120, 1, '最高优先级'),
    (2, 'P2', 30, 240, 1, '高优先级'),
    (3, 'P3', 60, 480, 1, '中优先级'),
    (4, 'P4', 120, 1440, 1, '低优先级')
on conflict (sla_rule_id) do nothing;

select setval(pg_get_serial_sequence('sys_role', 'role_id'), coalesce((select max(role_id) from sys_role), 1), true);
select setval(pg_get_serial_sequence('biz_handle_group', 'handle_group_id'), coalesce((select max(handle_group_id) from biz_handle_group), 1), true);
select setval(pg_get_serial_sequence('sys_user', 'user_id'), coalesce((select max(user_id) from sys_user), 1), true);
select setval(pg_get_serial_sequence('biz_handle_group_member', 'group_member_id'), coalesce((select max(group_member_id) from biz_handle_group_member), 1), true);
select setval(pg_get_serial_sequence('sys_user_role', 'user_role_id'), coalesce((select max(user_role_id) from sys_user_role), 1), true);
select setval(pg_get_serial_sequence('sys_menu', 'menu_id'), coalesce((select max(menu_id) from sys_menu), 1), true);
select setval(pg_get_serial_sequence('sys_role_menu', 'role_menu_id'), coalesce((select max(role_menu_id) from sys_role_menu), 1), true);
select setval(pg_get_serial_sequence('sys_dict_type', 'dict_type_id'), coalesce((select max(dict_type_id) from sys_dict_type), 1), true);
select setval(pg_get_serial_sequence('sys_dict_item', 'dict_item_id'), coalesce((select max(dict_item_id) from sys_dict_item), 1), true);
select setval(pg_get_serial_sequence('ticket_sla_rule', 'sla_rule_id'), coalesce((select max(sla_rule_id) from ticket_sla_rule), 1), true);
