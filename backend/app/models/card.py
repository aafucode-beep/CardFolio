"""Card database model."""

import enum
import uuid
from datetime import datetime

from sqlalchemy import ARRAY, DateTime, Enum, String, Text, func
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.db.database import Base


class CardCategory(str, enum.Enum):
    supplier = "supplier"
    competitor = "competitor"
    domestic = "domestic"
    international = "international"
    other = "other"


class Card(Base):
    __tablename__ = "cards"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    company: Mapped[str | None] = mapped_column(String(500), index=True)
    person_name: Mapped[str | None] = mapped_column(String(200), index=True)
    title: Mapped[str | None] = mapped_column(String(200))
    phone: Mapped[list[str] | None] = mapped_column(ARRAY(String))
    email: Mapped[list[str] | None] = mapped_column(ARRAY(String))
    address: Mapped[str | None] = mapped_column(Text)
    website: Mapped[str | None] = mapped_column(String(500))
    country: Mapped[str | None] = mapped_column(String(100), index=True)
    category: Mapped[CardCategory] = mapped_column(
        Enum(CardCategory), default=CardCategory.other, index=True
    )
    tags: Mapped[list[str] | None] = mapped_column(ARRAY(String))
    front_image_path: Mapped[str | None] = mapped_column(String(500))
    back_image_path: Mapped[str | None] = mapped_column(String(500))
    raw_ocr_result: Mapped[dict | None] = mapped_column(JSONB)
    notes: Mapped[str | None] = mapped_column(Text)
    needs_review: Mapped[bool] = mapped_column(default=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )
