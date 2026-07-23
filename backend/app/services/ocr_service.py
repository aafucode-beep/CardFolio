"""OCR service — calls vision LLM to extract structured info from business card images."""

import base64
import json
import re

import httpx

from app.config import settings

# ── Prompt for the vision model ──
OCR_SYSTEM_PROMPT = """You are a business card OCR specialist. Extract ALL information from the card image(s) and return STRICT JSON.

Extract these fields:
- person_name: the person's name (string, or null)
- company: company/organization name (string, or null)
- title: job title / position (string, or null)
- phone: list of phone numbers (array of strings, empty if none)
- email: list of email addresses (array of strings, empty if none)
- address: full postal address (string, or null)
- website: website URL (string, or null)
- country: country name — infer from language, address, phone area code (string, or null)
- category: classify as one of: "supplier", "competitor", "domestic", "international", "other"
  - supplier: provides materials/equipment/services to your company
  - competitor: same industry, competing products
  - domestic: Chinese mainland customer (if card is from China and not supplier/competitor)
  - international: overseas customer (non-China)
  - other: cannot determine
- confidence: your confidence level 0.0-1.0
- raw_text: ALL text visible on the card, line by line

Rules:
1. Return ONLY valid JSON, no markdown, no explanation.
2. If a field is missing, use null (for strings) or [] (for arrays).
3. For bilingual cards, prefer the primary language for names but keep both if unclear.
4. Detect country from: address, phone country code (+86=China, +1=USA, +81=Japan, etc.), language."""

OCR_USER_TEMPLATE = """Please extract information from this business card image. Return JSON only."""


def _image_to_base64(image_path: str) -> str:
    """Read image file and return base64-encoded string."""
    with open(image_path, "rb") as f:
        return base64.b64encode(f.read()).decode("utf-8")


def _build_messages(front_path: str | None, back_path: str | None) -> list[dict]:
    """Build messages for the vision model with image(s)."""
    content: list[dict] = [{"type": "text", "text": OCR_USER_TEMPLATE}]

    if front_path:
        b64 = _image_to_base64(front_path)
        content.append({
            "type": "image_url",
            "image_url": {"url": f"data:image/jpeg;base64,{b64}"},
        })
    if back_path:
        b64 = _image_to_base64(back_path)
        content.append({
            "type": "image_url",
            "image_url": {"url": f"data:image/jpeg;base64,{b64}"},
        })

    return [
        {"role": "system", "content": OCR_SYSTEM_PROMPT},
        {"role": "user", "content": content},
    ]


def _parse_json_response(text: str) -> dict:
    """Extract JSON from model response (handles markdown code blocks)."""
    # Try direct parse first
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        pass

    # Try extracting from ```json ... ``` blocks
    match = re.search(r"```(?:json)?\s*\n?(.*?)\n?```", text, re.DOTALL)
    if match:
        try:
            return json.loads(match.group(1))
        except json.JSONDecodeError:
            pass

    # Try finding first { ... last }
    start = text.find("{")
    end = text.rfind("}")
    if start != -1 and end != -1 and end > start:
        try:
            return json.loads(text[start : end + 1])
        except json.JSONDecodeError:
            pass

    return {}


async def perform_ocr(front_path: str | None, back_path: str | None) -> dict:
    """Call vision LLM to OCR the card image(s). Returns parsed result dict.

    Returns:
        {
            "person_name": str | None,
            "company": str | None,
            "title": str | None,
            "phone": list[str],
            "email": list[str],
            "address": str | None,
            "website": str | None,
            "country": str | None,
            "category": str,
            "confidence": float,
            "raw_text": str,
        }
    """
    if not settings.ocr_api_key:
        raise RuntimeError("OCR_API_KEY is not configured. Set it in .env")

    messages = _build_messages(front_path, back_path)

    # ── GLM-4V (Zhipu) ──
    if settings.ocr_model == "glm-4v":
        headers = {
            "Authorization": f"Bearer {settings.ocr_api_key}",
            "Content-Type": "application/json",
        }
        payload = {
            "model": "glm-4v-plus",
            "messages": messages,
            "temperature": 0.1,
            "max_tokens": 2000,
        }
        url = f"{settings.ocr_api_base}/chat/completions"

    # ── GPT-4o (OpenAI-compatible) ──
    elif settings.ocr_model == "gpt-4o":
        headers = {
            "Authorization": f"Bearer {settings.ocr_api_key}",
            "Content-Type": "application/json",
        }
        payload = {
            "model": "gpt-4o",
            "messages": messages,
            "temperature": 0.1,
            "max_tokens": 2000,
        }
        url = f"{settings.ocr_api_base}/chat/completions"

    # ── MiniMax M3 (OpenAI-compatible) ──
    elif settings.ocr_model == "minimax-m3":
        headers = {
            "Authorization": f"Bearer {settings.ocr_api_key}",
            "Content-Type": "application/json",
        }
        payload = {
            "model": "MiniMax-M3",
            "messages": messages,
            "temperature": 0.1,
            "max_tokens": 2000,
        }
        url = f"{settings.ocr_api_base}/chat/completions"

    elif settings.ocr_model == "claude-3.5-sonnet":
        headers = {
            "x-api-key": settings.ocr_api_key,
            "anthropic-version": "2023-06-01",
            "Content-Type": "application/json",
        }
        # Claude uses a different message format
        system_msg = OCR_SYSTEM_PROMPT
        user_content = messages[1]["content"]
        payload = {
            "model": "claude-3-5-sonnet-20241022",
            "max_tokens": 2000,
            "system": system_msg,
            "messages": [{"role": "user", "content": user_content}],
        }
        url = f"{settings.ocr_api_base}/messages"
    else:
        raise ValueError(f"Unknown OCR model: {settings.ocr_model}")

    async with httpx.AsyncClient(timeout=60.0) as client:
        resp = await client.post(url, json=payload, headers=headers)
        resp.raise_for_status()
        data = resp.json()

    # Extract text from response (handle both OpenAI and Anthropic formats)
    if "choices" in data:
        text = data["choices"][0]["message"]["content"]
    elif "content" in data:
        text = data["content"][0]["text"]
    else:
        text = str(data)

    result = _parse_json_response(text)

    # Ensure required fields exist
    result.setdefault("person_name", None)
    result.setdefault("company", None)
    result.setdefault("title", None)
    result.setdefault("phone", [])
    result.setdefault("email", [])
    result.setdefault("address", None)
    result.setdefault("website", None)
    result.setdefault("country", None)
    result.setdefault("category", "other")
    result.setdefault("confidence", 0.0)
    result.setdefault("raw_text", text)

    return result
