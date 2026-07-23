"""Card API routes — upload, list, detail, update, delete, search, export, stats."""

import csv
import io
import json
import os
import uuid

from fastapi import APIRouter, Depends, File, HTTPException, Query, UploadFile
from fastapi.responses import StreamingResponse
from sqlalchemy import func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.db.database import get_db
from app.models.card import Card, CardCategory
from app.schemas.card import (
    CardListResponse,
    CardResponse,
    CardUpdate,
    CategoryStats,
    UploadResponse,
)
from app.services.image_service import process_uploaded_image
from app.services.ocr_service import perform_ocr

router = APIRouter(prefix="/api/cards", tags=["cards"])


# ── Upload (trigger OCR) ──
@router.post("/upload", response_model=UploadResponse)
async def upload_card(
    front: UploadFile | None = File(None),
    back: UploadFile | None = File(None),
    db: AsyncSession = Depends(get_db),
):
    if not front and not back:
        raise HTTPException(400, "At least one image (front or back) is required")

    front_path = None
    back_path = None

    try:
        if front:
            raw = await front.read()
            if len(raw) > settings.max_file_size_mb * 1024 * 1024:
                raise HTTPException(413, f"Image too large (max {settings.max_file_size_mb}MB)")
            front_path = process_uploaded_image(raw, front.filename or "front.jpg")

        if back:
            raw = await back.read()
            if len(raw) > settings.max_file_size_mb * 1024 * 1024:
                raise HTTPException(413, f"Image too large (max {settings.max_file_size_mb}MB)")
            back_path = process_uploaded_image(raw, back.filename or "back.jpg")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"Image processing failed: {e}")

    # Run OCR
    ocr_status = "success"
    ocr_message = ""
    ocr_result = None

    try:
        ocr_result = await perform_ocr(front_path, back_path)
        confidence = ocr_result.get("confidence", 0.0)
        if confidence < 0.7:
            ocr_status = "needs_review"
            ocr_message = f"OCR confidence low ({confidence:.0%}), please verify."
    except Exception as e:
        ocr_status = "failed"
        ocr_message = f"OCR failed: {e}. You can edit the card manually."

    # Create card record
    category_str = ocr_result.get("category", "other") if ocr_result else "other"
    try:
        category = CardCategory(category_str)
    except ValueError:
        category = CardCategory.other

    card = Card(
        company=ocr_result.get("company") if ocr_result else None,
        person_name=ocr_result.get("person_name") if ocr_result else None,
        title=ocr_result.get("title") if ocr_result else None,
        phone=ocr_result.get("phone", []) if ocr_result else [],
        email=ocr_result.get("email", []) if ocr_result else [],
        address=ocr_result.get("address") if ocr_result else None,
        website=ocr_result.get("website") if ocr_result else None,
        country=ocr_result.get("country") if ocr_result else None,
        category=category,
        front_image_path=front_path,
        back_image_path=back_path,
        raw_ocr_result=ocr_result,
        needs_review=(ocr_status != "success"),
    )
    db.add(card)
    await db.flush()

    return UploadResponse(
        card=CardResponse.model_validate(card),
        ocr_status=ocr_status,
        message=ocr_message,
    )


# ── List (paginated, filterable) ──
@router.get("", response_model=CardListResponse)
async def list_cards(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    category: CardCategory | None = None,
    country: str | None = None,
    tag: str | None = None,
    needs_review: bool | None = None,
    db: AsyncSession = Depends(get_db),
):
    query = select(Card)

    if category:
        query = query.where(Card.category == category)
    if country:
        query = query.where(Card.country.ilike(f"%{country}%"))
    if tag:
        query = query.where(Card.tags.any(tag))
    if needs_review is not None:
        query = query.where(Card.needs_review == needs_review)

    # Count total
    count_q = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_q)).scalar() or 0

    # Paginate
    query = query.order_by(Card.created_at.desc())
    query = query.offset((page - 1) * page_size).limit(page_size)
    result = await db.execute(query)
    cards = result.scalars().all()

    return CardListResponse(
        total=total,
        page=page,
        page_size=page_size,
        items=[CardResponse.model_validate(c) for c in cards],
    )


# ── Detail ──
@router.get("/{card_id}", response_model=CardResponse)
async def get_card(card_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    card = await db.get(Card, card_id)
    if not card:
        raise HTTPException(404, "Card not found")
    return CardResponse.model_validate(card)


# ── Update ──
@router.put("/{card_id}", response_model=CardResponse)
async def update_card(
    card_id: uuid.UUID, data: CardUpdate, db: AsyncSession = Depends(get_db)
):
    card = await db.get(Card, card_id)
    if not card:
        raise HTTPException(404, "Card not found")

    update_data = data.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(card, key, value)

    await db.flush()
    return CardResponse.model_validate(card)


# ── Delete ──
@router.delete("/{card_id}")
async def delete_card(card_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    card = await db.get(Card, card_id)
    if not card:
        raise HTTPException(404, "Card not found")

    # Delete image files
    for path in [card.front_image_path, card.back_image_path]:
        if path and os.path.exists(path):
            os.remove(path)

    await db.delete(card)
    return {"message": "Card deleted"}


# ── Search ──
@router.get("/search/q", response_model=CardListResponse)
async def search_cards(
    q: str = Query(..., min_length=1),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    pattern = f"%{q}%"
    query = select(Card).where(
        or_(
            Card.company.ilike(pattern),
            Card.person_name.ilike(pattern),
            Card.title.ilike(pattern),
            Card.address.ilike(pattern),
            Card.country.ilike(pattern),
            Card.tags.any(q),
            Card.phone.any(q),
            Card.email.any(q),
        )
    )

    count_q = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_q)).scalar() or 0

    query = query.order_by(Card.created_at.desc())
    query = query.offset((page - 1) * page_size).limit(page_size)
    result = await db.execute(query)
    cards = result.scalars().all()

    return CardListResponse(
        total=total,
        page=page,
        page_size=page_size,
        items=[CardResponse.model_validate(c) for c in cards],
    )


# ── Export CSV ──
@router.get("/export/csv")
async def export_csv(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Card).order_by(Card.created_at.desc()))
    cards = result.scalars().all()

    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow([
        "ID", "Company", "Name", "Title", "Phone", "Email",
        "Address", "Website", "Country", "Category", "Tags", "Notes", "Created",
    ])
    for c in cards:
        writer.writerow([
            str(c.id), c.company or "", c.person_name or "", c.title or "",
            "; ".join(c.phone or []), "; ".join(c.email or []),
            c.address or "", c.website or "", c.country or "",
            c.category.value, "; ".join(c.tags or []), c.notes or "",
            c.created_at.isoformat() if c.created_at else "",
        ])

    output.seek(0)
    return StreamingResponse(
        iter([output.getvalue()]),
        media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=cards_export.csv"},
    )


# ── Export vCard ──
@router.get("/export/vcard")
async def export_vcard(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Card).order_by(Card.created_at.desc()))
    cards = result.scalars().all()

    lines = []
    for c in cards:
        lines.append("BEGIN:VCARD")
        lines.append("VERSION:3.0")
        lines.append(f"FN:{c.person_name or ''}")
        lines.append(f"ORG:{c.company or ''}")
        lines.append(f"TITLE:{c.title or ''}")
        for p in (c.phone or []):
            lines.append(f"TEL:{p}")
        for e in (c.email or []):
            lines.append(f"EMAIL:{e}")
        lines.append(f"ADR:;;{c.address or ''};;;;")
        lines.append(f"URL:{c.website or ''}")
        lines.append(f"NOTE:Category={c.category.value}; Country={c.country or ''}")
        lines.append("END:VCARD")

    content = "\n".join(lines)
    return StreamingResponse(
        iter([content]),
        media_type="text/vcard",
        headers={"Content-Disposition": "attachment; filename=cards.vcf"},
    )


# ── Category Stats ──
@router.get("/categories/stats", response_model=list[CategoryStats])
async def category_stats(db: AsyncSession = Depends(get_db)):
    query = (
        select(Card.category, func.count(Card.id))
        .group_by(Card.category)
    )
    result = await db.execute(query)
    return [
        CategoryStats(category=row[0], count=row[1])
        for row in result.all()
    ]



