# 工单工作流管理系统

基于以下技术栈实现的工单工作流管理系统 MVP：

- JDK 17
- Maven Wrapper
- Spring Boot 3.4.4
- MyBatis Spring Boot Starter 3.0.5
- PostgreSQL（生产）/ H2（本地快速运行）
- Flowable 7.2.0
- Vue 3 + Vite
- Element Plus

## 已实现能力

- 流程模板编排
  - 配置模板编码、名称、说明
  - 配置发起表单
  - 配置多个顺序审批节点
  - 每个节点可配置处理人员
  - 每个节点可自定义表单字段
- 动态部署 Flowable 流程
  - 保存模板时自动生成 BPMN 并部署
- 发起流程
  - 按模板动态渲染发起表单
  - 自动生成工单编号并启动 Flowable 流程实例
- 节点任务办理
  - 按处理人加载待办任务
  - 动态渲染当前节点表单
  - 办理后自动推进到下一节点
- 全链路处理留痕
  - 发起记录
  - 每个节点的处理记录
  - 每次提交的字段增量
  - 每次处理后的字段汇总快照
- 工单字段最终值统一保存
  - 所有节点处理后的字段会合并为统一快照
  - 同步写入字段索引表，支持检索
- 工单检索
  - 支持按关键词、发起人、状态检索
  - 支持按字段 Key + 字段值检索

## 后端结构

- `wf_template`：流程模板
- `wf_ticket_instance`：工单实例主表
- `wf_ticket_record`：处理记录留痕表
- `wf_ticket_field_index`：最终字段索引表
- Flowable 自带表：流程定义、运行时任务、历史表

## 本地启动

### 1. 启动后端

默认使用 `local` profile，走 H2 内存数据库，方便直接运行：

```bash
./mvnw spring-boot:run
```

后端地址：

- `http://localhost:8080`

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：

- `http://localhost:5173`

Vite 已配置 `/api` 代理到 `http://localhost:8080`。

## PostgreSQL 运行方式

如需切换 PostgreSQL：

1. 创建数据库 `ticket_workflow`
2. 修改 `src/main/resources/application-postgres.yml`
3. 使用 `postgres` profile 启动：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

## 前端核心页面

- 流程模板编排
- 发起流程
- 任务办理中心
- 工单检索与追踪

## 说明

当前版本为可运行的 MVP，流程编排采用“顺序节点编排”方式：

- 开始节点
- 多个顺序用户任务节点
- 结束节点

如果后续要继续增强，可以扩展：

- 并行网关 / 条件分支
- 候选人 / 候选组
- 抄送 / 催办
- 附件上传
- 流程撤回 / 驳回 / 转办
- 字段级权限
- 更完整的图形化设计器
