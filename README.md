# 工单系统

基于 Java 生态实现的工单系统 MVP，技术选型如下：

- JDK 17
- Maven Wrapper（兼容 Maven 3.9+ 使用方式）
- Spring Boot 3.4.4
- MyBatis Spring Boot Starter 3.0.5
- PostgreSQL 16（生产推荐）
- H2（本地快速运行）
- Vue 3
- Element Plus
- Vite

## 已实现模块

### 1. 表单管理
- 表单设计器
- 自定义字段（单行、多行、数字、日期、下拉）
- 字段必填与占位提示
- 表单 schema 持久化

### 2. 节点管理
- 节点状态：
  - START（开始）
  - AUDIT（审核）
  - HANDLE（处理）
  - CLOSE（关闭）
- 节点属性：
  - 节点名称
  - 节点编码
  - 关联表单 ID
  - 处理人
  - 节点执行动作

### 3. 流程管理
- 按节点编排流程
- 配置开始节点
- 配置节点之间的流转关系
- 支持一个节点配置多个 next 节点
- 校验流程有效性：
  - 只能有一个 START 节点
  - 至少有一个 CLOSE 节点
  - 非关闭节点至少一个 next 节点
  - CLOSE 节点不能再流转

### 4. 工单运行
- 按流程发起工单
- 开始节点动态表单渲染
- 待办任务按处理人查询
- 节点办理与流转
- 多 next 节点时可手动选择流转方向
- 工单统一业务数据快照
- 全量处理记录留痕

## 界面风格

前端采用现代化后台布局，参考 `vue-pure-admin` 的简洁设计思路：

- 深色左侧导航
- 卡片式内容区
- 清爽留白和中后台信息密度

## 启动方式

### 后端

默认使用本地 `local` profile，直接跑 H2：

```bash
./mvnw spring-boot:run
```

后端地址：

- `http://localhost:8080`

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：

- `http://localhost:5173`

## PostgreSQL 16

项目已内置 PostgreSQL 配置，生产推荐 PostgreSQL 16。

配置文件：

- `src/main/resources/application-postgres.yml`

切换方式：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

## 主要接口

- `/api/forms`
- `/api/nodes`
- `/api/flows`
- `/api/tickets`
- `/api/tasks`
- `/api/meta`
