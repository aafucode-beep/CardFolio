"""Pydantic schemas for Card CRUD operations."""

import uuid
from datetime import datetime
from pathlib import Path

from pydantic import BaseModel, ConfigDict, model_validator

from app.models.card import CardCategory


class CardBase(BaseModel):
    company: str | None = None
    person_name: str | None = None
    title: str | None = None
    phone: list[str] | None = None
    email: list[str] | None = None
    address: str | None = None
    website: str | None = None
    country: str | None = None
    category: CardCategory = CardCategory.other
    tags: list[str] | None = None
    notes: str | None = None


class CardCreate(CardBase):
    pass


class CardUpdate(BaseModel):
    company: str | None = None
    person_name: str | None = None
    title: str | None = None
    phone: list[str] | None = None
    email: list[str] | None = None
    address: str | None = None
    website: str | None = None
    country: str | None = None
    category: CardCategory | None = None
    tags: list[str] | None = None
    notes: str | None = None
    needs_review: bool | None = None


class CardResponse(CardBase):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    front_image_path: str | None = None
    back_image_path: str | None = None
    front_thumbnail_url: str | None = None
    back_thumbnail_url: str | None = None
    raw_ocr_result: dict | None = None
    needs_review: bool = False
    created_at: datetime
    updated_at: datetime

    @model_validator(mode="after")
    def compute_thumbnail_urls(self):
        if self.front_image_path:
            self.front_thumbnail_url = f"/cardfolio/uploads/thumb_{Path(self.front_image_path).name}"
        if self.back_image_path:
            self.back_thumbnail_url = f"/cardfolio/uploads/thumb_{Path(self.back_image_path).name}"
        return self


class CardListResponse(BaseModel):
    total: int
    page: int
    page_size: int
    items: list[CardResponse]


class UploadResponse(BaseModel):
    card: CardResponse
    ocr_status: str  # "success" | "needs_review" | "failed"
    message: str = ""


class CategoryStats(BaseModel):
    category: CardCategory
    count: int
