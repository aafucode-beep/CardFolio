# CardFolio — 智能名片管理系统

> AI 驱动的名片数字化管理平台：拍照 → OCR 识别 → 智能分类 → 数据库检索 → 多端访问

## 项目概述

将展会搜集的大量名片（国内/国际、供应商/竞争对手/客户）通过 AI OCR 技术批量数字化，
构建可搜索、可分类、可多端访问的名片数据库。

**核心链路：** Android APP 拍照 → 上传 VPS → 大模型 OCR + 结构化分析 → 数据库存储 → APP / Web 双端访问

## 技术栈概览

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| Android APP | Kotlin + Jetpack Compose | 拍照、上传、浏览、搜索 |
| 后端 API | Python FastAPI | RESTful API，处理上传/查询/分类 |
| OCR 引擎 | 大模型 API（GPT-4o / Claude / GLM-4V） | 图片识别 + 结构化信息提取 |
| 数据库 | PostgreSQL | 名片数据存储，支持全文搜索 |
| 文件存储 | MinIO / 本地磁盘 | 名片原图存储 |
| Web 前端 | React + Vite + TypeScript | 响应式 Web 界面 |
| 部署 | Docker Compose on VPS | 一键部署，域名访问 |
| CI/CD | GitHub Actions | 自动构建 APK + Docker 镜像 |

## 客户分类体系

```
├── 供应商 (Supplier) — 原材料、设备、服务供应商
├── 竞争对手 (Competitor) — 同行竞品公司
├── 国内客户 (Domestic Customer) — 中国大陆客户
├── 国外客户 (International Customer) — 海外客户
└── 其他 (Other) — 待分类/个人联系
```

每张名片可打多个标签，支持自定义扩展分类。

## 项目结构

```
CardFolio/
├── README.md                 # 项目说明（本文件）
├── WORKPLAN.md               # 详细工作计划
├── .gitignore                # Git 忽略规则
├── backend/                  # Python FastAPI 后端
│   ├── app/
│   │   ├── main.py           # FastAPI 入口
│   │   ├── config.py         # 配置管理
│   │   ├── models/           # 数据库模型
│   │   ├── schemas/          # Pydantic 数据模型
│   │   ├── api/              # API 路由
│   │   ├── services/         # 业务逻辑（OCR、分类等）
│   │   └── db/               # 数据库连接与迁移
│   ├── requirements.txt
│   ├── Dockerfile
│   └── tests/
├── android/                  # Android APP
│   ├── app/
│   │   ├── src/main/java/com/cardfolio/
│   │   │   ├── ui/           # Compose UI
│   │   │   ├── data/         # 数据层
│   │   │   ├── camera/       # 拍照模块
│   │   │   └── network/      # 网络请求
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── web/                      # Web 前端
│   ├── src/
│   │   ├── App.tsx
│   │   ├── components/
│   │   ├── pages/
│   │   └── api/
│   ├── package.json
│   ├── vite.config.ts
│   └── Dockerfile
├── docker-compose.yml        # 一键部署编排
├── .env.example              # 环境变量模板（不含真实密钥）
└── docs/                     # 项目文档
    ├── api-spec.md           # API 设计文档
    ├── deployment.md         # 部署指南
    └── ocr-design.md         # OCR 处理设计
```

## 部署架构

```
[Android APP] ──HTTPS──→ [VPS Nginx 反向代理]
                              ├── /api/     → FastAPI Backend :8000
                              ├── /         → Web Frontend (Nginx) :80
                              └── /minio/   → MinIO Object Storage :9000
                                    │
                          [PostgreSQL Database] :5432
                                    │
                          [AI OCR Service] → 大模型 API
```

## 快速开始

> 详细步骤见 [WORKPLAN.md](WORKPLAN.md)

1. 克隆仓库
2. 复制 `.env.example` 为 `.env`，填入配置
3. `docker compose up -d` 启动服务
4. Android APK 构建见 `android/` 目录说明

## License

Personal use only. All rights reserved.
