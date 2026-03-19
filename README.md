# 工单处理系统

基于 **Spring Boot 3 + MyBatis Plus + PostgreSQL 16 + Redis + JWT + Spring Security** 与 **Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router** 的企业级工单处理系统原型。

## 仓库结构

```text
.
├── backend/                 # Spring Boot 后端
├── docs/                    # 架构与业务设计文档
├── src/                     # Vue 3 前端
└── public/                  # 前端静态资源
```

## 核心模块

### 后端

- 认证中心：JWT 登录与 Spring Security 认证骨架
- 工单管理：工单主实体、详情、流转规则、评论记录
- 分派中心：待分派、重开、升级工单处理入口
- SLA 管理：规则配置、预警概览
- 报表统计：趋势、状态、分类、人员、部门、满意度
- 系统配置：角色权限、分类、优先级
- 日志审计：关键操作审计追踪

### 前端

- 登录页
- 工作台
- 工单管理
- 分派中心
- SLA 管理
- 通知中心
- 报表中心
- 系统配置
- 审计日志

## 关键设计说明

详细设计请查看：

- `docs/ticket-system-design.md`

其中包含：

- 角色职责
- 状态流转规则
- RBAC 与数据权限设计
- SLA 设计
- 报表指标
- mock 数据策略

## 前端启动

```bash
npm install
npm run dev
```

## 前端构建

```bash
npm run build
```

## 后端打包

```bash
npm run backend:package
```

## 数据库脚本

后端已提供 PostgreSQL 初始化脚本：

- `backend/src/main/resources/db/schema-postgresql.sql`
- `backend/src/main/resources/db/mock-data-postgresql.sql`
