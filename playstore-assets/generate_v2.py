"""
Three clean feature-graphic variants for iLeader Android (1024x500).
v1_light  — white background, centered logo + wordmark (Apple-style minimalism)
v2_dark   — pure black background, big logo left, clean type right
v3_split  — diagonal red/black split, logo on red, text on black
"""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

ROOT = Path(__file__).parent
LOGO_PATH = ROOT.parent / "app" / "src" / "main" / "res" / "drawable" / "logo_light.png"

FONT_BLACK = "C:/Windows/Fonts/arialbd.ttf"
FONT_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_REG = "C:/Windows/Fonts/segoeui.ttf"
FONT_LIGHT = "C:/Windows/Fonts/segoeuil.ttf"

RED = (229, 53, 53, 255)
NAVY = (28, 35, 90, 255)
WHITE = (255, 255, 255, 255)
NEAR_BLACK = (10, 10, 12, 255)
SOFT_GRAY = (140, 140, 150, 255)

W, H = 1024, 500


def load_logo():
    im = Image.open(LOGO_PATH).convert("RGBA")
    return im.crop(im.getbbox())


def fit(img, target_h):
    w, h = img.size
    s = target_h / h
    return img.resize((int(w * s), int(h * s)), Image.LANCZOS)


# ---------- v1: clean white ----------
def v1_light():
    canvas = Image.new("RGBA", (W, H), WHITE)
    draw = ImageDraw.Draw(canvas)

    logo = fit(load_logo(), 300)
    lw, lh = logo.size

    # Layout: logo + wordmark centered horizontally as a single group
    title = "iLeader"
    title_font = ImageFont.truetype(FONT_BLACK, 130)
    tb = draw.textbbox((0, 0), title, font=title_font)
    tw = tb[2] - tb[0]
    th = tb[3] - tb[1]

    gap = 40
    group_w = lw + gap + tw
    start_x = (W - group_w) // 2
    logo_y = (H - lh) // 2 - 20
    canvas.paste(logo, (start_x, logo_y), logo)

    # Wordmark — sit it on the visual baseline of the logo
    text_x = start_x + lw + gap
    text_y = logo_y + (lh - th) // 2 - tb[1] - 10
    draw.text((text_x, text_y), title, font=title_font, fill=NEAR_BLACK)

    # Slogan under the whole group
    slogan_font = ImageFont.truetype(FONT_REG, 28)
    slogan = "Спортивная платформа · Турниры · Рейтинги · Команды"
    sb = draw.textbbox((0, 0), slogan, font=slogan_font)
    sw = sb[2] - sb[0]
    draw.text(((W - sw) // 2, H - 70), slogan, font=slogan_font, fill=(110, 110, 120, 255))

    canvas.convert("RGB").save(ROOT / "feature_v1_light.png", "PNG", optimize=True)


# ---------- v2: pure dark, no glow ----------
def v2_dark():
    canvas = Image.new("RGBA", (W, H), NEAR_BLACK)
    draw = ImageDraw.Draw(canvas)

    # Subtle thin red rule on the left for brand mark
    draw.rectangle((0, 0, 6, H), fill=RED)

    logo = fit(load_logo(), 360)
    lw, lh = logo.size
    logo_x = 80
    logo_y = (H - lh) // 2
    canvas.paste(logo, (logo_x, logo_y), logo)

    # Right text block
    title_font = ImageFont.truetype(FONT_BLACK, 110)
    slogan_font = ImageFont.truetype(FONT_BOLD, 34)
    sub_font = ImageFont.truetype(FONT_LIGHT, 26)

    text_x = 540
    title = "iLeader"
    tb = draw.textbbox((0, 0), title, font=title_font)
    th = tb[3] - tb[1]
    title_y = 160
    draw.text((text_x, title_y), title, font=title_font, fill=WHITE)

    draw.text((text_x, title_y + th + 36), "Спортивная платформа",
              font=slogan_font, fill=WHITE)
    draw.text((text_x, title_y + th + 86), "Турниры · Рейтинги · Команды",
              font=sub_font, fill=SOFT_GRAY)

    canvas.convert("RGB").save(ROOT / "feature_v2_dark.png", "PNG", optimize=True)


# ---------- v3: diagonal red/black split ----------
def v3_split():
    canvas = Image.new("RGBA", (W, H), NEAR_BLACK)
    draw = ImageDraw.Draw(canvas)

    # Red parallelogram covering left ~46% with a diagonal edge
    red_poly = [(0, 0), (490, 0), (430, H), (0, H)]
    draw.polygon(red_poly, fill=RED)

    # Logo centered on the red side
    logo = fit(load_logo(), 340)
    lw, lh = logo.size
    # Approximate center of the red region at y=H/2 is x ≈ 230
    red_center_x = 215
    logo_x = red_center_x - lw // 2
    logo_y = (H - lh) // 2
    canvas.paste(logo, (logo_x, logo_y), logo)

    # Right side text on black
    title_font = ImageFont.truetype(FONT_BLACK, 100)
    slogan_font = ImageFont.truetype(FONT_BOLD, 32)
    sub_font = ImageFont.truetype(FONT_LIGHT, 24)

    text_x = 540
    title = "iLeader"
    tb = draw.textbbox((0, 0), title, font=title_font)
    th = tb[3] - tb[1]
    title_y = 170
    draw.text((text_x, title_y), title, font=title_font, fill=WHITE)
    draw.text((text_x, title_y + th + 30), "Спортивная платформа",
              font=slogan_font, fill=WHITE)
    draw.text((text_x, title_y + th + 78), "Турниры · Рейтинги · Команды",
              font=sub_font, fill=SOFT_GRAY)

    canvas.convert("RGB").save(ROOT / "feature_v3_split.png", "PNG", optimize=True)


if __name__ == "__main__":
    v1_light()
    v2_dark()
    v3_split()
    print("done")
