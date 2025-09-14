# ZThread - 动态线程池管理框架

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

ZThread 是一个基于 Spring Boot 的动态线程池管理框架，支持线程池参数的动态调整、实时监控、告警通知等功能。通过集成主流配置中心（Nacos、Apollo），实现线程池配置的热更新，提升应用的性能调优能力。

## ✨ 核心特性

- 🔄 **动态配置**: 支持运行时动态调整线程池参数，无需重启应用
- 📊 **实时监控**: 提供线程池运行状态的实时监控和可视化面板
- 🚨 **智能告警**: 支持队列积压、线程活跃度等多维度告警
- 🔌 **多配置中心**: 集成 Nacos、Apollo 等主流配置中心
- 📈 **指标采集**: 集成 Micrometer，支持 Prometheus 等监控系统
- 🎛️ **管理面板**: 提供 Web 控制台进行线程池管理和监控
- 🔧 **易于集成**: 基于 Spring Boot Starter，开箱即用

## 🏗️ 项目架构

```
zthread/
├── core/                           # 核心模块 - 线程池基础功能
├── spring-base/                    # Spring 基础模块 - 框架集成
├── starter/                        # Spring Boot Starter 模块
│   ├── common-spring-boot-starter/         # 通用启动器
│   ├── nacos-cloud-spring-boot-starter/    # Nacos 集成启动器
│   ├── apollo-spring-boot-starter/         # Apollo 集成启动器
│   ├── dashboard-dev-spring-boot-starter/  # 开发面板启动器
│   └── adapter/                            # 适配器模块
│       └── web-spring-boot-starter/        # Web 适配器
├── dashboard-dev/                  # 开发控制台
└── example/                        # 示例项目
    └── nacos-cloud-example/        # Nacos 集成示例
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Spring Boot 3.0.7+
- Maven 3.6+

### 1. 添加依赖

在你的 Spring Boot 项目中添加 ZThread 依赖：

```xml
<dependency>
    <groupId>com.zicca.zthread</groupId>
    <artifactId>zthread-nacos-cloud-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2. 启用 ZThread

在主启动类上添加 `@EnableZThread` 注解：

```java
@EnableZThread
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. 配置线程池

在配置文件中定义线程池配置：

```yaml
zthread:
  nacos:
    data-id: your-app.yaml
    group: DEFAULT_GROUP
  config-file-type: yaml
  web:
    core-pool-size: 10
    maximum-pool-size: 200
    keep-alive-time: 60
    notify:
      receives: admin
    notify-platforms:
      platform: DING
      url: your-dingtalk-webhook-url
  executors:
    - thread-pool-id: business-executor
      core-pool-size: 10
      maximum-pool-size: 200
      keep-alive-time: 19999
      work-queue: ResizableCapacityLinkedBlockingQueue
      queue-capacity: 10000
      rejected-handler: CallerRunsPolicy
      allow-core-thread-time-out: false
      notify:
        receives: admin
        interval: 5
      alarm:
        enable: true
        queue-threshold: 80
        active-threshold: 80
```

## 📖 使用指南

### 配置中心集成

#### Nacos 集成

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
        namespace: public
      discovery:
        server-addr: localhost:8848
```

#### Apollo 集成

```xml
<dependency>
    <groupId>com.zicca.zthread</groupId>
    <artifactId>zthread-apollo-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 监控集成

#### Prometheus 指标

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info
  prometheus:
    metrics:
      export:
        enabled: true
```

### 开发控制台

添加开发控制台依赖：

```xml
<dependency>
    <groupId>com.zicca.zthread</groupId>
    <artifactId>zthread-dashboard-dev-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

访问控制台：`http://localhost:8080/zthread-dashboard`

## 🔧 配置参数

### 线程池配置

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `thread-pool-id` | 线程池唯一标识 | - |
| `core-pool-size` | 核心线程数 | 1 |
| `maximum-pool-size` | 最大线程数 | Integer.MAX_VALUE |
| `keep-alive-time` | 线程空闲时间(秒) | 60 |
| `work-queue` | 工作队列类型 | LinkedBlockingQueue |
| `queue-capacity` | 队列容量 | Integer.MAX_VALUE |
| `rejected-handler` | 拒绝策略 | AbortPolicy |

### 告警配置

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `alarm.enable` | 是否启用告警 | false |
| `alarm.queue-threshold` | 队列使用率告警阈值(%) | 80 |
| `alarm.active-threshold` | 活跃线程告警阈值(%) | 80 |
| `notify.receives` | 告警接收人 | - |
| `notify.interval` | 告警间隔(分钟) | 5 |

## 🎯 示例项目

查看 `example/nacos-cloud-example` 目录获取完整的使用示例。

运行示例：

```bash
cd example/nacos-cloud-example
mvn spring-boot:run
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！请查看 [贡献指南](CONTRIBUTING.md) 了解如何参与项目开发。

### 开发环境搭建

1. 克隆项目
```bash
git clone https://github.com/your-org/zthread.git
cd zthread
```

2. 编译项目
```bash
mvn clean compile
```

3. 运行测试
```bash
mvn test
```

## 📄 许可证

本项目基于 [Apache License 2.0](LICENSE) 许可证开源。

## 🙏 致谢

感谢所有为 ZThread 项目做出贡献的开发者！

---

如果 ZThread 对你有帮助，请给我们一个 ⭐️！