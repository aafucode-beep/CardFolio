# CardFolio 工作计划

> 最后更新：2026-07-24
> 项目目标：构建 AI 驱动的名片数字化管理系统，支持 Android APP + Web 双端访问

---

## 阶段总览

| 阶段 | 名称 | 预估周期 | 交付物 |
|------|------|---------|--------|
| P0 | 项目初始化 | 0.5 天 | Git 仓库、项目骨架、.gitignore |
| P1 | 后端 API 核心 | 3-4 天 | FastAPI 服务、数据库、OCR 管线 |
| P2 | Web 前端 | 2-3 天 | 名片列表/详情/搜索/分类页面 |
| P3 | Android APP | 3-4 天 | 拍照上传、浏览搜索、APK 构建 |
| P4 | VPS 部署 | 1-2 天 | Docker Compose 部署、域名配置、HTTPS |
| P5 | 测试与优化 | 2-3 天 | 端到端测试、OCR 准确率优化、性能调优 |
| P6 | GitHub 上传 | 0.5 天 | 仓库整理、README 完善、推送 |

**总计预估：12-17 天**（可并行压缩至 10 天左右）

---

## P0 — 项目初始化（0.5 天）

### 任务清单

1. 创建项目目录结构（backend / android / web / docs）
2. 编写 `.gitignore`（排除 `.env`、密钥、构建产物、数据库文件）
3. 初始化 Git 仓库，首次提交
4. 创建 GitHub 远程仓库（通过用户提供的 Token）
5. 编写 `.env.example` 环境变量模板

### 关键决策

- 数据库选型：PostgreSQL（支持全文搜索 + JSONB 灵活字段）
- OCR 方案：调用大模型视觉 API（GLM-4V / GPT-4o），支持中英文名片
- 文件存储：MinIO（S3 兼容，自建对象存储）或本地磁盘（简单模式）

### .gitignore 关键项

```
# 环境与密钥
.env
.env.local
*.key
*.pem

# 数据库
*.db
*.sqlite
pgdata/

# 上传文件
uploads/
minio-data/

# Android 构建产物
android/app/build/
android/.gradle/
*.apk
*.aab
*.keystore

# 构建产物
__pycache__/
node_modules/
dist/
build/

# IDE
.idea/
.vscode/
*.iml
```

---

## P1 — 后端 API 核心（3-4 天）

### 1.1 项目骨架搭建

1. FastAPI 项目初始化，配置 CORS / 日志 / 异常处理
2. PostgreSQL 数据库连接（SQLAlchemy async）
3. Alembic 数据库迁移配置
4. Docker 开发环境（docker-compose.dev.yml）

### 1.2 数据库设计

**cards 表：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| company | VARCHAR | 公司名称 |
| person_name | VARCHAR | 姓名 |
| title | VARCHAR | 职位 |
| phone | VARCHAR[] | 电话（可能多个） |
| email | VARCHAR[] | 邮箱 |
| address | TEXT | 地址 |
| website | VARCHAR | 网址 |
| country | VARCHAR | 国家 |
| category | ENUM | supplier/competitor/domestic/international/other |
| tags | VARCHAR[] | 自定义标签 |
| front_image_path | VARCHAR | 正面图片路径 |
| back_image_path | VARCHAR | 背面图片路径 |
| raw_ocr_result | JSONB | OCR 原始返回 |
| notes | TEXT | 备注 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 1.3 API 设计

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /api/cards/upload | 上传名片图片（正/反面），触发 OCR |
| GET | /api/cards | 名片列表（分页、筛选、排序） |
| GET | /api/cards/{id} | 名片详情 |
| PUT | /api/cards/{id} | 编辑名片信息 |
| DELETE | /api/cards/{id} | 删除名片 |
| GET | /api/cards/search | 全文搜索 |
| GET | /api/cards/export | 导出（CSV / vCard） |
| GET | /api/categories/stats | 分类统计 |

### 1.4 OCR 处理管线

```
上传图片 → 图片预处理（旋转校正、增强）→ 调用大模型视觉 API → 结构化解析 → 存入数据库
```

**OCR Prompt 设计要点：**
1. 提取：姓名、职位、公司、电话、邮箱、地址、网址、国家
2. 语言自动检测（中/英/日/韩等多语言名片）
3. 返回 JSON 结构化数据
4. 置信度低于阈值时标记为"待人工确认"
5. 自动推断分类（根据公司名/行业关键词）

**大模型选择（按优先级）：**
1. GLM-4V（国内访问稳定，中文识别优秀）
2. GPT-4o（多语言能力强）
3. Claude 3.5 Sonnet（结构化输出准确）
4. 可配置切换，支持 fallback

### 1.5 图片处理

1. 接收正反面图片（MultipartFile）
2. 压缩 + 格式统一（转 JPEG，限制 2MB 以内）
3. 存储至 MinIO 或本地 `uploads/` 目录
4. 生成缩略图用于列表展示

---

## P2 — Web 前端（2-3 天）

### 2.1 技术栈

- React 18 + TypeScript
- Vite 构建工具
- TailwindCSS + Shadcn/UI 组件库
- TanStack Query（数据请求）
- React Router（路由）

### 2.2 页面规划

1. **名片列表页** — 卡片网格 / 表格切换，支持分类筛选、标签筛选、搜索
2. **名片详情页** — 完整信息展示 + 原图查看 + 编辑功能
3. **上传页** — 拖拽上传图片，实时显示 OCR 进度和结果
4. **统计面板** — 分类统计图表、近期添加趋势
5. **设置页** — API 配置、分类管理

### 2.3 功能要点

- 响应式设计（移动端适配，但主要面向桌面操作）
- 全文搜索（前端 fuzzy search + 后端 PostgreSQL tsvector）
- 批量操作（导出、删除、分类调整）
- 暗色模式支持

---

## P3 — Android APP（3-4 天）

### 3.1 技术栈

- Kotlin + Jetpack Compose
- CameraX（拍照）
- Retrofit + OkHttp（网络请求）
- Coil（图片加载）
- Hilt（依赖注入）
- Room（本地缓存，离线浏览已同步数据）

### 3.2 核心页面

1. **首页** — 名片列表（搜索栏 + 分类 Tab）
2. **拍照页** — 相机界面，支持正反面连续拍摄
3. **上传中页** — 上传进度 + OCR 处理状态
4. **名片详情页** — 信息展示 + 一键拨号/发邮件/访问网址
5. **分类浏览** — 按类别浏览名片

### 3.3 关键功能

1. **拍照优化**：CameraX 自动对焦、曝光补偿、边缘检测引导线
2. **离线缓存**：Room 数据库缓存已下载名片，无网络时可浏览
3. **一键操作**：拨打电话、发送邮件、打开网址、添加通讯录
4. **批量拍照**：展会场景下连续拍摄多张名片，批量上传
5. **APK 构建**：GitHub Actions 自动构建 Debug APK

### 3.4 APK 构建配置

- 使用 GitHub Actions workflow
- 构建 Debug APK（个人测试，无需签名）
- Artifact 上传至 GitHub Actions 产物

---

## P4 — VPS 部署（1-2 天）

### 4.1 部署清单

1. Docker + Docker Compose 安装
2. Nginx 反向代理配置
3. SSL 证书（Let's Encrypt / Certbot）
4. PostgreSQL 数据持久化
5. MinIO 对象存储部署
6. 后端服务部署
7. Web 前端部署
8. 防火墙配置（仅开放 80/443 端口）

### 4.2 docker-compose.yml 编排

```yaml
services:
  postgres:    # 数据库
  minio:       # 对象存储
  backend:     # FastAPI
  web:         # Nginx + 前端静态文件
  nginx:       # 反向代理 + SSL
```

### 4.3 域名配置

- 主域名访问 Web 前端
- `/api/*` 反向代理至后端
- HTTPS 自动续期

### 4.4 环境变量

```
DATABASE_URL=postgresql://user:pass@postgres:5432/cardfolio
MINIO_ENDPOINT=minio:9000
MINIO_ACCESS_KEY=xxx
MINIO_SECRET_KEY=xxx
OCR_API_KEY=xxx          # 大模型 API Key
OCR_MODEL=glm-4v         # OCR 模型选择
SECRET_KEY=xxx           # JWT 密钥
```

---

## P5 — 测试与优化（2-3 天）

### 5.1 功能测试

1. 名片上传 → OCR 识别 → 数据入库 全链路测试
2. 多语言名片测试（中文、英文、日文、双语名片）
3. 模糊/倾斜名片识别准确率
4. 搜索功能测试（中文搜索、拼音搜索、英文搜索）
5. Android APP 在多设备上的兼容性

### 5.2 性能优化

1. 图片上传压缩策略（客户端压缩 + 服务端压缩）
2. OCR 请求并发控制（排队机制，避免 API 限流）
3. 数据库索引优化（全文搜索索引、分类索引）
4. 图片缩略图缓存

### 5.3 用户体验优化

1. OCR 结果可编辑（识别错误时手动修正）
2. 重复名片检测（相似公司名/电话/邮箱提醒）
3. 批量导入（已有电子表格导入）
4. 名片夹导出（vCard 格式，可导入手机通讯录）

---

## P6 — GitHub 上传（0.5 天）

### 6.1 仓库整理

1. 确保 `.gitignore` 正确排除所有敏感信息
2. 检查代码中无硬编码密钥
3. 完善 README.md
4. 添加 LICENSE（个人使用声明）

### 6.2 推送流程

1. 用户提供 GitHub Access Token
2. 创建远程仓库（private 或 public 由用户决定）
3. 推送代码
4. 配置 GitHub Actions（APK 自动构建）
5. 验证 CI/CD 流程

### 6.3 安全检查清单

- [ ] `.env` 文件不在版本控制中
- [ ] API Key 通过环境变量注入，不硬编码
- [ ] 数据库密码不在代码中
- [ ] VPS 连接信息不在文档中
- [ ] `.env.example` 中只含占位符，不含真实值
- [ ] Android 签名密钥（如有）不上传

---

## 里程碑时间线

```
Day 1     P0 项目初始化 + P1 后端骨架开始
Day 2-4   P1 后端 API + OCR 管线开发
Day 5-7   P2 Web 前端 + P3 Android APP（可并行）
Day 8-9   P3 Android APP 完成 + P4 VPS 部署
Day 10-12 P5 测试优化 + P6 GitHub 上传
```

## 风险与应对

| 风险 | 影响 | 应对 |
|------|------|------|
| OCR 准确率不足 | 数据需大量人工修正 | 多模型 fallback + 人工编辑界面 + 持续优化 prompt |
| 大模型 API 费用 | 成本控制 | 图片压缩 + 缓存已识别结果 + 可切换本地模型 |
| VPS 配置不足 | 服务不稳定 | Docker 资源限制 + 图片压缩 + 异步处理队列 |
| Android 兼容性 | 部分设备无法使用 | 优先测试主流机型 + CameraX 兼容性处理 |

---

## 下一步行动

1. **确认技术选型** — 大模型 OCR 使用哪个 API？（GLM-4V / GPT-4o / 其他）
2. **提供 GitHub Token** — 用于创建仓库和推送代码
3. **提供 VPS 信息** — 域名、SSH 访问方式（部署阶段需要）
4. **开始 P0** — 创建项目骨架，初始化 Git 仓库
