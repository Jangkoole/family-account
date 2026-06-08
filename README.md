# 家庭记账本

一个面向家庭的 Web 端记账与财务分析系统，支持多成员协作、多维度统计分析和批量账单导入。

## 项目简介

家庭记账本旨在解决现代家庭财务管理的痛点——不同成员的收支数据分散在各平台，缺乏统一的记录和分析工具。系统基于 Web 端开发，无需安装客户端，通过浏览器即可使用。

**核心特性：**

- **多主题风格**：内置新中式、杂志风、手账风三种视觉主题，可在个人中心自由切换
- **家庭组协作**：创建家庭组，邀请成员共同记账，支持家庭管理员统一管理分类和成员
- **批量导入**：支持系统模板 Excel、微信/支付宝 CSV 账单的批量导入与自动字段映射
- **多维度统计**：按时间（日/周/月/年）、分类、成员等多维度汇总，支持图表展示
- **数据安全**：基于 Sa-Token 的登录认证，支持记录级可见性控制

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.14 |
| JDK | Java | 21 |
| ORM | MyBatis-Plus | 3.5.10 |
| 数据库 | MySQL | 8.x |
| 登录认证 | Sa-Token | 1.39.0 |
| 构建工具 | Maven | - |
| 前端框架 | Vue 3 | 3.5.32 |
| 构建工具 | Vite | 8.0.8 |
| UI 组件库 | Element Plus | 2.14.0 |
| 图表库 | ECharts | 6.1.0 |
| 状态管理 | Pinia | 3.0.4 |
| HTTP 客户端 | Axios | 1.16.1 |
| Node.js | - | ^20.19.0 \|\| >=22.12.0 |

## 功能清单

### 已实现功能

- **用户管理**：注册、登录、修改昵称、修改密码
- **收支分类管理**：系统内置默认分类 + 用户自定义分类；家庭成员可提交分类申请，管理员审核
- **收支记录管理**：增删改查，支持按时间/类型/分类/金额组合筛选
- **批量导入**：支持系统模板 Excel、微信 CSV、支付宝 CSV 格式，导入前可预览映射结果
- **记录可见性控制**：每条记录可设为"仅自己可见"或"家庭成员可见"
- **汇总统计**：按日/周/月/年汇总收支，按分类统计占比，按成员统计收支
- **图表展示**：分类占比饼图、收支对比柱状图、收支趋势折线图
- **首页仪表盘**：本月收支概览卡片、分类支出占比图、近一周趋势图、最近记录列表
- **家庭组管理**：创建家庭组、邀请码加入、成员审核、管理员转让、移除成员
- **多主题切换**：新中式、杂志风、手账风三种主题，在个人中心自由切换

### 未来扩展

- 微信/支付宝账单字段映射规则自定义保存，下次导入直接复用
- 账单截图 OCR 识别，自动填充记账表单
- 统计结果导出为 PDF 报告

## 快速开始

### 前置要求

- 一台 Linux 云服务器（本文以 Ubuntu 为例）
- 一个 MySQL 8.x 数据库实例（可部署在同一台服务器上）
- 域名

### 一、后端部署

#### 1. 安装 Java 21

```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
```

验证安装：

```bash
java -version
```

#### 2. 安装 Maven

```bash
sudo apt install maven -y
```

验证安装：

```bash
mvn -version
```

#### 3. 克隆项目

```bash
mkdir -p /home/app
cd /home
git clone https://github.com/Jangkoole/family-account.git
```

#### 4. 打包后端

```bash
cd /home/family-account/account-backend
mvn clean package -DskipTests
```

看到 `BUILD SUCCESS` 说明打包成功。生成的 jar 包位于 `target/account-0.0.1-SNAPSHOT.jar`。

#### 5. 复制 jar 包到应用目录

```bash
cp /home/family-account/account-backend/target/account-0.0.1-SNAPSHOT.jar /home/app/
```

#### 6. 创建数据库配置文件

在 `/home/app/` 目录下创建 `application-local.yaml`：

```bash
vim /home/app/application-local.yaml
```

填入以下内容（请替换为实际的数据库信息）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/family_account?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_db_username
    password: your_db_password
```

> 如果密码包含特殊字符，需要用引号括起来。

#### 7. 启动后端

```bash
nohup java -jar /home/app/account-0.0.1-SNAPSHOT.jar \
  --spring.config.additional-location=file:/home/app/application-local.yaml \
  > /home/app/app.log 2>&1 &
```

#### 8. 查看启动日志

```bash
tail -f /home/app/app.log
```

看到 `Started AccountApplication` 说明启动成功，按 `Ctrl + C` 退出日志查看。

#### 9. 验证部署

浏览器访问 `http://<服务器公网IP>:8090/test/hello`，看到以下内容说明后端部署成功：

```json
{"code": 200, "message": "success", "data": "Hello FamilyAccount!"}
```

> 注意：需要在云服务器安全组中开放 8090 端口。

---

### 二、前端部署

#### 1. 安装 Node.js

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install nodejs -y
```

验证安装：

```bash
node -v
npm -v
```

#### 2. 进入前端目录

```bash
cd /home/family-account/account-front
```

#### 3. 配置后端 API 地址

创建 `.env.local` 文件：

```bash
vim .env.local
```

填入以下内容（将 IP 替换为你的服务器公网 IP）：

```bash
VITE_API_BASE_URL=http://<服务器公网IP>:8090
```

#### 4. 安装依赖并打包

```bash
npm install
npm run build
```

打包完成后，静态文件生成在 `dist/` 目录下。

---

### 三、安装 Nginx 并部署前端

#### 1. 安装 Nginx

```bash
sudo apt install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### 2. 复制前端文件到 Nginx 目录

```bash
sudo cp -r /home/family-account/account-front/dist/* /var/www/html/
```

#### 3. 配置 Nginx

```bash
sudo vim /etc/nginx/sites-available/default
```

写入以下配置：

```nginx
server {
    listen 80;

    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

#### 4. 验证并重启 Nginx

```bash
sudo nginx -t
sudo systemctl restart nginx
```

> 记得在云服务器安全组中开放 80 端口。

#### 5. 访问系统

浏览器打开 `http://<服务器公网IP>`，即可进入家庭记账本系统。

---

### 四、数据库初始化

系统启动时会自动创建所需的数据库表，无需手动执行建表脚本。只需确保 MySQL 中已创建好数据库：

```sql
CREATE DATABASE IF NOT EXISTS family_account DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 项目结构

```
family-account/
├── account-backend/              # 后端项目
│   └── src/
│       ├── main/
│       │   ├── java/com/family/account/
│       │   │   ├── common/       # 公共组件（CORS、异常处理、统一响应）
│       │   │   ├── controller/   # 控制器层
│       │   │   ├── dto/          # 数据传输对象
│       │   │   ├── entity/       # 数据库实体
│       │   │   ├── mapper/       # MyBatis-Plus Mapper
│       │   │   └── service/      # 业务逻辑层
│       │   └── resources/
│       │       ├── mapper/       # MyBatis XML 映射
│       │       └── application.yaml
│       └── test/
├── account-front/                # 前端项目
│   └── src/
│       ├── api/                  # API 请求封装
│       ├── layouts/              # 主题布局组件
│       ├── router/               # 路由配置
│       ├── stores/               # Pinia 状态管理
│       ├── styles/               # 主题样式
│       ├── utils/                # 工具函数
│       └── views/                # 页面组件
└── docs/                         # 项目文档
```

## 开发环境

- **IDE**：IntelliJ IDEA（后端）/ VS Code（前端）
- **数据库管理**：DBeaver
- **接口调试**：Apifox
- **版本管理**：Git + GitHub
