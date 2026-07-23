# CardFolio API 设计文档

## Base URL

```
Development:  http://localhost:8000
Production:   https://your-domain.com/api
```

## 认证

当前版本（个人使用）暂不要求认证。后续可添加 API Key / JWT 认证。

---

## API 端点

### 1. 上传名片（触发 OCR）

```
POST /api/cards/upload
Content-Type: multipart/form-data
```

**Parameters:**
- `front` (file, optional): 名片正面图片
- `back` (file, optional): 名片背面图片

至少提供一张图片。

**Response (200):**
```json
{
  "card": {
    "id": "uuid",
    "company": "某某科技有限公司",
    "person_name": "张三",
    "title": "销售总监",
    "phone": ["+86 138-xxxx-xxxx"],
    "email": ["zhangsan@example.com"],
    "address": "上海市浦东新区...",
    "website": "https://example.com",
    "country": "China",
    "category": "domestic",
    "tags": null,
    "notes": null,
    "front_image_path": "/app/uploads/front.jpg",
    "back_image_path": null,
    "raw_ocr_result": { ... },
    "needs_review": false,
    "created_at": "2026-07-24T02:30:00Z",
    "updated_at": "2026-07-24T02:30:00Z"
  },
  "ocr_status": "success",
  "message": ""
}
```

**ocr_status 值：**
- `success`: OCR 成功，置信度高
- `needs_review`: OCR 完成，但置信度低，建议人工核对
- `failed`: OCR 失败，需手动编辑

---

### 2. 名片列表（分页 + 筛选）

```
GET /api/cards?page=1&page_size=20&category=domestic&country=China&tag=展会2026&needs_review=false
```

**Response:**
```json
{
  "total": 150,
  "page": 1,
  "page_size": 20,
  "items": [ { ...card }, ... ]
}
```

---

### 3. 名片详情

```
GET /api/cards/{card_id}
```

---

### 4. 编辑名片

```
PUT /api/cards/{card_id}
Content-Type: application/json
```

```json
{
  "company": "修正后的公司名",
  "category": "supplier",
  "tags": ["展会2026", "电子产品"]
}
```

---

### 5. 删除名片

```
DELETE /api/cards/{card_id}
```

---

### 6. 搜索

```
GET /api/cards/search/q?q=张三&page=1&page_size=20
```

搜索范围：公司名、姓名、职位、地址、国家、电话、邮箱、标签。

---

### 7. 导出

```
GET /api/cards/export/csv    → 下载 CSV 文件
GET /api/cards/export/vcard  → 下载 vCard (.vcf) 文件
```

---

### 8. 分类统计

```
GET /api/cards/categories/stats
```

**Response:**
```json
[
  {"category": "supplier", "count": 45},
  {"category": "competitor", "count": 12},
  {"category": "domestic", "count": 68},
  {"category": "international", "count": 25},
  {"category": "other", "count": 3}
]
```

---

### 9. 健康检查

```
GET /api/health
```

```json
{"status": "ok", "model": "glm-4v"}
```

---

### 10. 图片访问

```
GET /uploads/{filename}
```

返回上传的名片图片。
