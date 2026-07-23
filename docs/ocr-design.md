# OCR 处理设计

## 流程

```
用户上传图片 (正/反面)
       ↓
  图片预处理
  ├── EXIF 自动旋转校正
  ├── 转 RGB (去除 alpha 通道)
  ├── 长边压缩至 2000px
  └── JPEG quality=85 压缩保存
       ↓
  调用大模型视觉 API
  ├── 构造 system prompt (OCR 专家)
  ├── 构造 user message (图片 base64)
  └── 请求大模型 (GLM-4V / GPT-4o / Claude)
       ↓
  解析 JSON 响应
  ├── 提取结构化字段
  ├── 解析分类 (supplier/competitor/domestic/international/other)
  └── 评估置信度
       ↓
  存入数据库
  ├── 置信度 ≥ 0.7 → needs_review = false
  └── 置信度 < 0.7 → needs_review = true
```

## 多模型支持

| 模型 | API | 优势 | 适用场景 |
|------|-----|------|---------|
| GLM-4V-Plus | 智谱 OpenAPI | 中文识别优秀，国内访问快 | 默认首选 |
| GPT-4o | OpenAI | 多语言强，结构化输出稳定 | 国际名片 |
| Claude 3.5 Sonnet | Anthropic | 复杂版面理解准确 | 双面/复杂名片 |

通过环境变量 `OCR_MODEL` 切换，支持 fallback 机制（P2 优化）。

## Prompt 策略

1. **System prompt** 定义角色：名片 OCR 专家，输出严格 JSON
2. **字段定义** 明确每个字段的类型和含义
3. **分类规则** 给出明确的分类标准
4. **容错规则** 缺失字段用 null / []，不猜测
5. **多语言** 自动检测语言，双语名片保留两种语言信息

## 优化方向（P5）

1. **图片预处理增强**：透视校正、去噪、增强对比度
2. **Fallback 机制**：主模型失败时自动切换备用模型
3. **批量处理**：展会场景连续拍照批量上传
4. **缓存**：相同图片不重复 OCR
5. **人工反馈学习**：记录人工修正，优化 prompt
