# 工单处理系统设计说明

## 1. 项目定位

本项目定位为企业级软件服务工单处理系统，面向一线客服、技术支持、运维、研发、主管和管理员等角色，提供统一受理、分派、处理、跟踪、评价、统计与审计能力。

## 2. 技术架构

### 2.1 后端技术栈

- JDK 17
- Spring Boot 3
- Spring Security + JWT
- MyBatis Plus
- PostgreSQL 16
- Redis

### 2.2 前端技术栈

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router

## 3. 模块划分

### 3.1 后端模块

- `auth`：登录认证、JWT 签发、用户上下文
- `security`：认证过滤器、权限校验、访问控制
- `ticket`：工单主流程、详情、流转、评论、附件、评价
- `dispatch`：分派中心、转派、协作、升级
- `sla`：SLA 规则、超时预警、超时统计
- `report`：工单趋势、时效、满意度、部门绩效
- `notification`：站内消息、邮件通知
- `system`：用户、角色、菜单、按钮、分类、优先级、流程规则
- `audit`：审计日志、系统日志、关键操作追溯
- `common`：通用响应、异常、分页、枚举、工具类

### 3.2 前端模块

- 登录与权限
- 工作台总览
- 工单管理
- 分派中心
- SLA 管理
- 通知中心
- 报表中心
- 系统配置
- 审计日志

## 4. 核心领域对象

### 4.1 工单主实体 Ticket

关键字段包含：

- 工单编号 `ticketNo`
- 标题 `title`
- 问题描述 `description`
- 提单人 `requesterId / requesterName`
- 联系方式 `contactPhone`
- 所属客户/部门 `customerName / departmentName`
- 工单来源 `source`
- 工单类型 `ticketType`
- 工单分类 `categoryCode`
- 优先级 `priority`
- 紧急程度 `urgencyLevel`
- 当前状态 `status`
- 当前处理人 `currentAssigneeId`
- 当前处理组 `currentGroupCode`
- SLA 响应时限 `responseDeadline`
- SLA 解决时限 `resolveDeadline`
- 受理/分派/完成/关闭时间
- 评价结果/评价内容
- 是否升级/是否超时

### 4.2 配套实体

- `TicketFlowRecord`：流转记录
- `TicketCommentRecord`：评论/沟通记录
- `TicketOperateLog`：工单操作日志
- `TicketAttachment`：附件信息
- `SlaRule`：SLA 规则
- `NotificationMessage`：站内消息
- `AuditLogRecord`：审计日志
- `SysUser / SysRole / SysPermission`：RBAC 基础模型

## 5. 角色与职责

### 5.1 提单人

- 创建工单
- 查看本人提交工单
- 补充说明
- 催办
- 确认是否解决
- 评价

### 5.2 客服

- 受理工单
- 初步分类
- 设置优先级
- 分派
- 沟通
- 关闭无效工单

### 5.3 处理人员

- 接单
- 处理
- 填写处理记录
- 转派
- 申请协助
- 标记完成

### 5.4 主管

- 查看本组工单
- 干预分派
- 升级工单
- 查看 SLA 超时
- 查看团队绩效

### 5.5 系统管理员

- 配置分类、优先级、流程规则、SLA 规则
- 配置用户、角色、菜单、按钮、数据权限
- 查看系统日志与审计日志

## 6. 状态流转设计

### 6.1 状态定义

- `PENDING_ACCEPT`：待受理
- `ACCEPTED`：已受理
- `PENDING_ASSIGN`：待分派
- `PROCESSING`：处理中
- `PENDING_CONFIRM`：待用户确认
- `COMPLETED`：已完成
- `CLOSED`：已关闭
- `CANCELLED`：已取消
- `SUSPENDED`：已挂起
- `REOPENED`：已重开

### 6.2 状态流转主链路

1. 提单人/客服创建工单 -> `PENDING_ACCEPT`
2. 客服受理 -> `ACCEPTED`
3. 客服完成分类与规则绑定 -> `PENDING_ASSIGN`
4. 客服/主管分派 -> `PROCESSING`
5. 处理人员提交处理结果 -> `PENDING_CONFIRM`
6. 提单人确认解决 -> `COMPLETED`
7. 客服/系统管理员归档关闭 -> `CLOSED`

### 6.3 特殊流转

- `PENDING_ACCEPT` -> `CANCELLED`
  - 执行人：客服、管理员
  - 场景：无效单、重复单、误提单

- `PROCESSING` -> `SUSPENDED`
  - 执行人：处理人员、主管
  - 场景：等待外部依赖、客户补充信息

- `SUSPENDED` -> `PROCESSING`
  - 执行人：处理人员、主管
  - 场景：依赖恢复、信息补齐

- `PENDING_CONFIRM` -> `REOPENED`
  - 执行人：提单人
  - 场景：用户确认未解决

- `REOPENED` -> `PENDING_ASSIGN`
  - 执行人：客服、主管
  - 场景：重新分派

- `PROCESSING` -> `PROCESSING`
  - 执行人：处理人员、主管
  - 场景：转派、协作、升级，状态不变但责任人/责任组变更

## 7. 角色可执行的状态变更

| 当前状态 | 可执行动作 | 目标状态 | 允许角色 |
| --- | --- | --- | --- |
| PENDING_ACCEPT | 受理 | ACCEPTED | 客服 |
| PENDING_ACCEPT | 取消 | CANCELLED | 客服、管理员 |
| ACCEPTED | 分类并提交分派 | PENDING_ASSIGN | 客服 |
| PENDING_ASSIGN | 分派 | PROCESSING | 客服、主管 |
| PROCESSING | 挂起 | SUSPENDED | 处理人员、主管 |
| SUSPENDED | 恢复处理 | PROCESSING | 处理人员、主管 |
| PROCESSING | 提交处理完成 | PENDING_CONFIRM | 处理人员 |
| PENDING_CONFIRM | 确认解决 | COMPLETED | 提单人 |
| PENDING_CONFIRM | 确认未解决 | REOPENED | 提单人 |
| REOPENED | 重新分派 | PENDING_ASSIGN | 客服、主管 |
| COMPLETED | 关闭 | CLOSED | 客服、管理员 |

## 8. 权限模型设计

### 8.1 RBAC

系统采用 `用户 -> 角色 -> 权限` 的 RBAC 模型。

### 8.2 权限层级

- 菜单权限：控制页面可见性
- 按钮权限：控制新增、受理、分派、升级、关闭等操作
- 数据权限：控制工单可见范围

### 8.3 数据权限范围

- `SELF_CREATED`：仅本人创建
- `SELF_ASSIGNED`：仅本人处理
- `DEPARTMENT`：本部门
- `TEAM`：本组
- `ALL`：全部

## 9. SLA 规则设计

每条工单在受理与分派时绑定 SLA 规则：

- 响应时限：多久内必须首次响应
- 解决时限：多久内必须解决
- 预警阈值：例如剩余 30 分钟时预警
- 升级策略：
  - 超响应自动通知客服主管
  - 超解决自动升级到高级处理组

## 10. 报表指标

- 工单数量趋势
- 按状态统计
- 按类型/分类统计
- 按人员处理量统计
- 按部门处理量统计
- SLA 达成率
- 平均响应时长
- 平均解决时长
- 用户满意度
- 超时工单统计

## 11. 可扩展性设计

- 状态流转规则抽象为配置模型，后续可接入更完整规则引擎
- 通知中心抽象站内信、邮件两种通道，后续可扩展短信、企业微信、钉钉
- 报表层可从 mock 聚合平滑替换为 SQL 聚合
- 附件、审计、评论、流转均拆分独立实体，便于扩展

## 12. mock 数据策略

系统将内置多角色、多部门、多工单分类、多优先级、多状态、多 SLA 达成情况的 mock 数据，用于：

- 列表筛选验证
- 状态流转验证
- 分派与升级验证
- 报表统计验证
- 审计与通知验证
