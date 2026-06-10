"""
Generate Google Play Store assets for iLeader Android.
- play_icon_512.png      512x512  app icon (white background, iL logo)
- feature_graphic_1024x500.png  feature banner (dark gradient + logo + name + slogan)
"""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from pathlib import Path

ROOT = Path(__file__).parent
LOGO_PATH = ROOT.parent / "app" / "src" / "main" / "res" / "drawable" / "logo_light.png"
OUT_ICON = ROOT / "play_icon_512.png"
OUT_FEATURE = ROOT / "feature_graphic_1024x500.png"

FONT_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_REG = "C:/Windows/Fonts/segoeui.ttf"
FONT_BLACK = "C:/Windows/Fonts/arialbd.ttf"

RED = (229, 53, 53, 255)
DARK_BG_TOP = (10, 10, 10, 255)
DARK_BG_BOT = (26, 26, 30, 255)


def load_logo_tight():
    """Load logo and crop to its non-transparent bounding box."""
    im = Image.open(LOGO_PATH).convert("RGBA")
    bbox = im.getbbox()
    return im.crop(bbox)


def build_icon():
    """512x512 app icon: white background, centered iL logo with safe-zone padding."""
    size = 512
    canvas = Image.new("RGBA", (size, size), (255, 255, 255, 255))

    logo = load_logo_tight()
    # Android adaptive-icon safe zone ~66% of viewport; for Play 512 icon
    # design typically fills ~78% of canvas. Keep a clear margin.
    target = int(size * 0.72)
    lw, lh = logo.size
    scale = target / max(lw, lh)
    new_w, new_h = int(lw * scale), int(lh * scale)
    logo_resized = logo.resize((new_w, new_h), Image.LANCZOS)

    x = (size - new_w) // 2
    y = (size - new_h) // 2
    canvas.paste(logo_resized, (x, y), logo_resized)

    canvas.convert("RGB").save(OUT_ICON, "PNG", optimize=True)
    print(f"icon written: {OUT_ICON} ({OUT_ICON.stat().st_size // 1024} KB)")


def vertical_gradient(size, top, bot):
    w, h = size
    grad = Image.new("RGBA", size, top)
    px = grad.load()
    for y in range(h):
        t = y / max(h - 1, 1)
        r = int(top[0] * (1 - t) + bot[0] * t)
        g = int(top[1] * (1 - t) + bot[1] * t)
        b = int(top[2] * (1 - t) + bot[2] * t)
        for x in range(w):
            px[x, y] = (r, g, b, 255)
    return grad


def radial_glow(size, center, radius, color):
    """Soft radial glow overlay (additive feel via alpha)."""
    w, h = size
    glow = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    cx, cy = center
    # Draw concentric circles with decreasing alpha
    steps = 60
    for i in range(steps, 0, -1):
        a = int(color[3] * (i / steps) ** 2)
        r = int(radius * (i / steps))
        draw.ellipse((cx - r, cy - r, cx + r, cy + r),
                     fill=(color[0], color[1], color[2], a))
    return glow.filter(ImageFilter.GaussianBlur(radius=20))


def build_feature():
    """1024x500 banner: dark gradient, red glow, logo left, title + slogan right."""
    W, H = 1024, 500
    bg = vertical_gradient((W, H), DARK_BG_TOP, DARK_BG_BOT)

    # Red glow behind the logo area for brand pop
    glow = radial_glow((W, H), center=(260, 250), radius=320,
                       color=(229, 53, 53, 110))
    bg = Image.alpha_composite(bg, glow)

    # Subtle diagonal accent stripe (very faint)
    accent = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    adraw = ImageDraw.Draw(accent)
    adraw.polygon([(W - 360, 0), (W, 0), (W, 110), (W - 240, 110)],
                  fill=(229, 53, 53, 28))
    bg = Image.alpha_composite(bg, accent)

    # Logo on the left
    logo = load_logo_tight()
    logo_target = 340  # px height
    lw, lh = logo.size
    scale = logo_target / lh
    nl_w, nl_h = int(lw * scale), int(lh * scale)
    logo_resized = logo.resize((nl_w, nl_h), Image.LANCZOS)
    logo_x = 90
    logo_y = (H - nl_h) // 2
    bg.paste(logo_resized, (logo_x, logo_y), logo_resized)

    # Text block on the right
    draw = ImageDraw.Draw(bg)
    title_font = ImageFont.truetype(FONT_BLACK, 96)
    slogan_font = ImageFont.truetype(FONT_BOLD, 32)
    sub_font = ImageFont.truetype(FONT_REG, 24)

    text_x = 480
    # Title: "iLeader"
    title = "iLeader"
    tb = draw.textbbox((0, 0), title, font=title_font)
    th = tb[3] - tb[1]
    title_y = 150
    draw.text((text_x, title_y), title, font=title_font, fill=(255, 255, 255, 255))

    # Red underline accent under title
    tw = tb[2] - tb[0]
    underline_y = title_y + th + 18
    draw.rounded_rectangle(
        (text_x, underline_y, text_x + 110, underline_y + 6),
        radius=3, fill=RED,
    )

    # Slogan
    slogan = "Спортивная платформа"
    sub = "Турниры • Рейтинги • Команды"
    draw.text((text_x, underline_y + 28), slogan,
              font=slogan_font, fill=(245, 245, 247, 255))
    draw.text((text_x, underline_y + 78), sub,
              font=sub_font, fill=(170, 170, 180, 255))

    # Save as 24-bit PNG (no alpha) per Play requirements
    bg.convert("RGB").save(OUT_FEATURE, "PNG", optimize=True)
    print(f"feature written: {OUT_FEATURE} ({OUT_FEATURE.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    build_icon()
    build_feature()
