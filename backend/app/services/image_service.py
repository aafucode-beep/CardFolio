"""Image processing service — compress, resize, convert to JPEG."""

import io
from pathlib import Path

from PIL import Image, ImageOps

from app.config import settings


def process_uploaded_image(raw_bytes: bytes, filename: str) -> str:
    """Process an uploaded image: auto-orient, resize, compress, save.

    Returns the relative path where the processed image is stored.
    """
    img = Image.open(io.BytesIO(raw_bytes))

    # Auto-orient based on EXIF
    img = ImageOps.exif_transpose(img)

    # Convert to RGB (strip alpha for JPEG)
    if img.mode in ("RGBA", "P", "LA"):
        img = img.convert("RGB")
    elif img.mode != "RGB":
        img = img.convert("RGB")

    # Resize if too large (max 2000px on longest side)
    max_dim = 2000
    if max(img.size) > max_dim:
        ratio = max_dim / max(img.size)
        new_size = (int(img.size[0] * ratio), int(img.size[1] * ratio))
        img = img.resize(new_size, Image.Resampling.LANCZOS)

    # Generate filename
    safe_name = Path(filename).stem.replace(" ", "_")
    out_name = f"{safe_name}_{Path(filename).suffix or '.jpg'}"
    if not out_name.endswith(".jpg"):
        out_name = f"{safe_name}.jpg"
    out_path = settings.upload_path / out_name

    # Avoid collision
    counter = 1
    while out_path.exists():
        out_path = settings.upload_path / f"{safe_name}_{counter}.jpg"
        counter += 1

    # Save with quality optimization
    img.save(out_path, "JPEG", quality=85, optimize=True)

    return str(out_path)


def generate_thumbnail(image_path: str, size: tuple[int, int] = (200, 200)) -> str:
    """Generate a square thumbnail for list display.

    Returns the thumbnail path.
    """
    src = Path(image_path)
    thumb_name = f"thumb_{src.name}"
    thumb_path = settings.upload_path / thumb_name

    img = Image.open(src)
    img = ImageOps.exif_transpose(img)
    img = ImageOps.fit(img, size, Image.Resampling.LANCZOS)

    if img.mode != "RGB":
        img = img.convert("RGB")

    img.save(thumb_path, "JPEG", quality=75)
    return str(thumb_path)
