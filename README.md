# Spring Boot Demo

这是一个最小可运行的 Spring Boot 示例项目，基于 Java 21 和 Maven Wrapper。

## 功能

- 启动一个 Spring Boot Web 应用
- 提供一个可直接访问的 JSON 接口：`GET /api/hello`

## 启动方式

```bash
./mvnw spring-boot:run
```

应用默认启动在 `http://localhost:8080`。

## 接口示例

默认请求：

```bash
curl "http://localhost:8080/api/hello"
```

返回示例：

```json
{"message":"Hello, World!","framework":"Spring Boot"}
```

带参数请求：

```bash
curl "http://localhost:8080/api/hello?name=Cursor"
```

返回示例：

```json
{"message":"Hello, Cursor!","framework":"Spring Boot"}
```

## 运行测试

```bash
./mvnw test
```
