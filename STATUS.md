# 项目状态说明

> 最后更新：2026-07-24

## GitHub 仓库

- **仓库地址**：https://github.com/yjhkrepo/CardFolio
- **可见性**：私有仓库（Private）
- **默认分支**：`main`
- **本地路径**：`C:\Users\liuya\Documents\Lobster\CardFolio`
- **本地状态**：与 origin/main 同步（已 pull 最新 commits）

## 提交历史（12 commits）

```
e89ed14  feat: Web 导航栏添加 APK 下载链接
d75f0ef  fix: 修复 Android 编译错误（POST import + AndroidView key）
c794fdf  fix: 添加 Android 启动图标资源
dd47457  fix: 添加 gradle.properties 启用 AndroidX
d69ed69  fix: 提交 gradle-wrapper.jar 并标记为 binary
5563388  fix: 添加 Gradle wrapper 文件修复 CI 构建
e8c07b1  deploy: 适配 /cardfolio/ 子路径部署
f123d5c  feat: Web 名片详情编辑页 + Android 服务器地址可配置 + 启用缩略图生成
bd18732  feat: 添加 MiniMax M3 模型支持，设为默认 OCR 引擎
461483e  P3: Android APP — Kotlin+Compose+CameraX 拍照上传+...
d372675  P1+P2: 后端 API + OCR 管线 + Web 前端 + Docker 编排 + Nginx + 文档
cb6cbb0  P0: 项目初始化 - 骨架、计划、.gitignore、.env.example
```

## 总体进度

| 阶段 | 名称 | 状态 | 备注 |
|------|------|------|------|
| P0 | 项目初始化 | ✅ 完成 | Git 仓库、目录骨架、配置模板 |
| P1 | 后端 API 核心 | ✅ 完成 | FastAPI + PostgreSQL + OCR 管线 |
| P2 | Web 前端 | ✅ 完成 | React + TypeScript + 4 个页面 + 详情编辑页 |
| P3 | Android APP | ✅ 完成 | Kotlin + Compose + CameraX + 6 个屏幕 |
| P4 | VPS 部署 | ⏳ 待开始 | 需要用户提供 VPS 信息（域名、SSH） |
| P5 | 测试与优化 | ⏳ 待开始 | OCR 准确率优化、性能调优 |
| P6 | GitHub 上传 | ✅ 完成 | 12 commits 全部推送 |

## 当前技术栈

### 后端（Python）
- FastAPI 0.115（异步框架）
- SQLAlchemy 2.0 async + asyncpg
- PostgreSQL 16（数据库）
- Pillow（图片处理）
- httpx（大模型 API 调用）
- Alembic（迁移工具，已在依赖中）

### Web 前端（TypeScript）
- React 18 + TypeScript
- Vite 5（构建工具）
- TanStack Query（数据请求）
- TailwindCSS（样式）
- Lucide React（图标）
- 部署路径：`/cardfolio/` 子路径

### Android（Kotlin）
- Kotlin 2.0 + Jetpack Compose
- CameraX 1.3（拍照）
- Retrofit 2.11 + OkHttp 4.12（网络）
- Coil 2.7（图片加载）
- minSdk 26（Android 8.0） / targetSdk 34
- Gradle wrapper（已添加）
- APK 通过 GitHub Actions 自动构建

### 部署
- Docker Compose 编排
- PostgreSQL 16-alpine
- Nginx 反向代理（HTTP/HTTPS）
- 支持 /cardfolio/ 子路径部署

## OCR 大模型配置

**当前默认：MiniMax M3**

```bash
OCR_MODEL=minimax-m3
OCR_API_KEY=***        # 用户自有 MiniMax Token
OCR_API_BASE=https://api.minimax.chat/v1
```

**支持的模型（环境变量切换，无需改代码）：**
- `minimax-m3` — 当前默认（MiniMax CN Token Plan）
- `glm-4v` — 智谱 GLM-4V-Plus（国内访问稳定）
- `gpt-4o` — OpenAI
- `claude-3.5-sonnet` — Anthropic

⚠️ **风险提示**：MiniMax M3 是否支持图片输入（多模态）需要验证。如果不支持，需要切换到 MiniMax 支持视觉的模型，或采用"本地 OCR + M3 结构化"两步方案。

## 文件结构

```
CardFolio/
├── README.md                        项目说明
├── WORKPLAN.md                      详细工作计划
├── STATUS.md                        状态说明（本文件）
├── .env.example                     环境变量模板（无真实密钥）
├── .gitignore                       Git 忽略规则
├── docker-compose.yml               Docker 编排
├── backend/                         FastAPI 后端
│   ├── app/
│   │   ├── main.py                  入口
│   │   ├── config.py                配置管理
│   │   ├── api/cards.py             名片 REST API
│   │   ├── api/uploads.py           图片访问
│   │   ├── models/card.py           数据库模型
│   │   ├── schemas/card.py          Pydantic schemas
│   │   ├── services/ocr_service.py  大模型 OCR 调用
│   │   ├── services/image_service.py 图片预处理
│   │   └── db/database.py           数据库连接
│   ├── Dockerfile
│   └── requirements.txt
├── web/                             React 前端
│   ├── src/
│   │   ├── App.tsx
│   │   ├── pages/
│   │   │   ├── CardListPage.tsx     名片列表（已修改适配子路径）
│   │   │   ├── UploadPage.tsx       上传页
│   │   │   ├── CardDetailPage.tsx   详情/编辑页
│   │   │   ├── SearchPage.tsx       搜索页
│   │   │   └── StatsPage.tsx        统计页
│   │   ├── api/client.ts
│   │   └── main.tsx
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts               已配置 /cardfolio/ base
│   ├── tailwind.config.js
│   ├── index.html
│   └── Dockerfile
├── android/                         Android APP
│   ├── app/
│   │   ├── build.gradle.kts
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/cardfolio/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── CardFolioApp.kt
│   │   │   │   ├── data/            Models / API Client / Settings
│   │   │   │   │   ├── ApiClient.kt
│   │   │   │   │   ├── Models.kt
│   │   │   │   │   ├── ImageUtils.kt
│   │   │   │   │   └── SettingsDataStore.kt  新增（服务器地址配置）
│   │   │   │   └── ui/
│   │   │   │       ├── Navigation.kt
│   │   │   │       ├── theme/Theme.kt
│   │   │   │       └── screens/
│   │   │   │           ├── CameraScreen.kt          拍照页
│   │   │   │           ├── CardListScreen.kt        列表页
│   │   │   │           ├── CardDetailScreen.kt      详情页
│   │   │   │           ├── SearchScreen.kt          搜索页
│   │   │   │           ├── StatsScreen.kt           统计页
│   │   │   │           ├── UploadResultScreen.kt    上传结果
│   │   │   │           └── SettingsScreen.kt        新增（服务器设置）
│   │   │   └── res/values/
│   │   └── proguard-rules.pro
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle/libs.versions.toml
│   ├── gradle/wrapper/              已添加 wrapper 文件
│   ├── gradlew
│   └── gradlew.bat
├── nginx/nginx.conf                 反向代理配置
├── docs/
│   ├── api-spec.md
│   ├── deployment.md
│   └── ocr-design.md
└── .github/workflows/android.yml    APK 自动构建
```

**总计**：77 个文件

## 关键特性

### 后端 API
- 完整的 CRUD：上传、列表、详情、编辑、删除
- 多模型 OCR 引擎（MiniMax / GLM-4V / GPT-4o / Claude）
- 自动分类（供应商/竞争对手/国内/国外/其他）
- 全文搜索（公司、姓名、电话、邮箱、地址等）
- CSV / vCard 导出
- 分类统计
- 置信度阈值标记需人工核对

### Web 前端
- 名片列表（分类筛选 + 分页）
- 拖拽上传 + OCR 实时反馈
- 全文搜索
- 分类统计图表
- 名片详情 + 编辑
- **已适配 /cardfolio/ 子路径部署**（vite.config base）
- 导航栏 APK 下载链接

### Android APP
- CameraX 拍照（正反面连续拍摄）
- 前后摄像头切换
- **可配置服务器地址**（Settings 页面，DataStore 持久化）
- 一键拨号/发邮件/打开网址
- 离线浏览缓存
- 6 个完整屏幕：列表/拍照/搜索/统计/详情/上传结果/设置

### 部署
- Docker Compose 一键启动
- PostgreSQL + Backend + Web + Nginx
- 支持 /cardfolio/ 子路径
- HTTPS 证书预留
- 持久化数据卷

## 已修复的构建问题

根据提交历史，已修复：
- ✅ 缺少 `POST` import 和 `AndroidView key` 参数（Android 编译）
- ✅ 添加启动图标资源（mipmap drawable）
- ✅ gradle.properties 启用 AndroidX
- ✅ gradle-wrapper.jar 标记为 binary
- ✅ 添加 Gradle wrapper 文件（修复 CI 构建）
- ✅ Web 适配 /cardfolio/ 子路径
- ✅ Web 导航栏 APK 下载链接

## 待办事项（P4 + P5）

### P4 — VPS 部署（需要用户提供信息）
- [ ] VPS 服务器信息（IP、SSH 端口、用户名）
- [ ] 域名（可选，用于 HTTPS）
- [ ] 在 VPS 上安装 Docker + Docker Compose
- [ ] 创建 `.env` 文件填入真实配置
  - [ ] MiniMax API Key
  - [ ] 数据库密码
  - [ ] Secret Key（生成随机字符串）
- [ ] `docker compose --profile production up -d`
- [ ] 配置 Nginx + SSL 证书（certbot）
- [ ] 配置防火墙（80/443）

### P5 — 测试与优化
- [ ] OCR 准确率验证（多语言名片）
- [ ] MiniMax M3 视觉能力验证（⚠️ 待确认）
- [ ] 上传/下载性能测试
- [ ] 移动端兼容性测试
- [ ] OCR Prompt 优化（提升准确率）
- [ ] 重复名片检测
- [ ] vCard 导出兼容性测试

### 风险与提示

1. **MiniMax M3 多模态能力**：需验证是否支持图片输入；不支持则需要更换模型
2. **VPS 资源**：OCR 调用对网络有要求，建议至少 1GB RAM
3. **API 成本**：MiniMax Token Plan 可能有调用次数限制，注意监控
4. **Android APK 签名**：当前为 Debug APK，个人测试可用；正式发布需签名
5. **HTTPS**：个人测试可不配置 HTTPS（usesCleartextTraffic 已开启），但生产建议配置

## 已知限制

- 单用户使用，无认证授权（个人工具）
- 无重复检测（同一名片多次上传）
- 无离线 OCR 备份
- 无批量导入（Excel/CSV）
- 无 OCR 历史版本回溯

## 下一步行动

1. **验证 MiniMax M3 视觉能力** —— 测试上传一张名片图片，确认 OCR 是否能返回结构化数据
2. **提供 VPS 信息** —— 域名 / IP / SSH 访问，启动 P4 部署
3. **等待 P4 完成** —— 部署上线后进行 P5 端到端测试

---

## 快速命令参考

```bash
# 拉取最新代码
git pull origin main

# 本地启动后端（需先启动 PostgreSQL）
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload

# 本地启动 Web
cd web
npm install
npm run dev

# 启动整个 stack（Docker）
docker compose up -d

# 触发 APK 构建
# 在 GitHub 页面 → Actions → Build Android Debug APK → Run workflow
```

## 联系信息

项目维护：用户 + LobsterAI 协作
创建时间：2026-07-24