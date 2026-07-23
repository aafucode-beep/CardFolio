"""Static file serving for uploaded images."""

from fastapi import APIRouter
from fastapi.responses import FileResponse
from pathlib import Path

from app.config import settings

router = APIRouter(prefix="/uploads", tags=["uploads"])


@router.get("/{filename}")
async def serve_image(filename: str):
    """Serve uploaded card images."""
    # Prevent path traversal
    safe_name = Path(filename).name
    file_path = settings.upload_path / safe_name
    if not file_path.exists():
        from fastapi import HTTPException
        raise HTTPException(404, "Image not found")
    return FileResponse(file_path)
