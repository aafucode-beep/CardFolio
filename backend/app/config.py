"""CardFolio configuration — reads from environment variables / .env file."""

from pathlib import Path
from typing import Literal

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )

    # ── App ──
    app_host: str = "0.0.0.0"
    app_port: int = 8000
    debug: bool = True

    # ── Database ──
    database_url: str = "postgresql+asyncpg://cardfolio:password@localhost:5432/cardfolio"

    # ── File storage ──
    upload_dir: str = "./uploads"
    max_file_size_mb: int = 10

    # ── OCR ──
    ocr_model: Literal["glm-4v", "gpt-4o", "claude-3.5-sonnet", "minimax-m3"] = "minimax-m3"
    ocr_api_key: str = ""
    ocr_api_base: str = "https://api.minimax.chat/v1"

    # ── Security ──
    secret_key: str = "dev-secret-change-in-production"
    cors_origins: str = "http://localhost:5173,http://localhost:3000"

    @property
    def cors_origin_list(self) -> list[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]

    @property
    def upload_path(self) -> Path:
        p = Path(self.upload_dir)
        p.mkdir(parents=True, exist_ok=True)
        return p


settings = Settings()
