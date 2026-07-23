# 部署指南

## 开发环境

### 前置要求

- Docker + Docker Compose
- Python 3.12+（本地开发）
- Node.js 20+（Web 前端开发）

### 启动开发服务

```bash
# 1. 复制环境变量
cp .env.example .env
# 编辑 .env 填入真实配置

# 2. 启动 PostgreSQL + Backend
docker compose up -d postgres backend

# 3. 本地启动 Web 前端开发服务器
cd web
npm install
npm run dev
```

### 访问地址

- Backend API: http://localhost:8000
- API Docs (Swagger): http://localhost:8000/docs
- Web Frontend: http://localhost:5173

---

## VPS 生产部署

### 1. 服务器准备

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | sh
systemctl enable docker

# 安装 Docker Compose
apt install docker-compose-plugin
```

### 2. 部署步骤

```bash
# 克隆代码
git clone https://github.com/YOUR_USERNAME/CardFolio.git
cd CardFolio

# 配置环境变量
cp .env.example .env
nano .env  # 填入生产配置

# 启动所有服务
docker compose --profile production up -d
```

### 3. Nginx + SSL 配置

```bash
# 安装 certbot
apt install certbot

# 申请 SSL 证书
certbot certonly --standalone -d your-domain.com

# 配置 Nginx
# 编辑 nginx/nginx.conf，填入域名和证书路径
```

### 4. 防火墙

```bash
# 仅开放必要端口
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
ufw enable
```

### 5. 数据备份

```bash
# 备份数据库
docker exec cardfolio-postgres-1 pg_dump -U cardfolio cardfolio > backup_$(date +%Y%m%d).sql

# 备份上传文件
tar -czf uploads_$(date +%Y%m%d).tar.gz uploads/
```

---

## Android APK 构建

### 本地构建

```bash
cd android
./gradlew assembleDebug
# APK 输出: app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions 自动构建

见 `.github/workflows/android.yml`（P3 阶段添加）。
