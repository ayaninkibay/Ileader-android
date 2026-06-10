"""Final clean feature graphic 1024x500. Pure dark, logo left, text right."""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

ROOT = Path(__file__).parent
LOGO = ROOT.parent / "app" / "src" / "main" / "res" / "drawable" / "logo_light.png"

W, H = 1024, 500
BG = (10, 10, 12, 255)
WHITE = (255, 255, 255, 255)
GRAY = (155, 155, 165, 255)

FONT_BLACK = "C:/Windows/Fonts/arialbd.ttf"
FONT_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_REG = "C:/Windows/Fonts/segoeui.ttf"


def fit(img, target_h):
    w, h = img.size
    s = target_h / h
    return img.resize((int(w * s), int(h * s)), Image.LANCZOS)


canvas = Image.new("RGBA", (W, H), BG)
draw = ImageDraw.Draw(canvas)

logo = Image.open(LOGO).convert("RGBA")
logo = logo.crop(logo.getbbox())
logo = fit(logo, 360)
lw, lh = logo.size
canvas.paste(logo, (90, (H - lh) // 2), logo)

title_font = ImageFont.truetype(FONT_BLACK, 108)
slogan_font = ImageFont.truetype(FONT_BOLD, 36)
sub_font = ImageFont.truetype(FONT_REG, 26)

text_x = 540

title = "iLeader"
tb = draw.textbbox((0, 0), title, font=title_font)
th = tb[3] - tb[1]
title_y = 165
draw.text((text_x, title_y), title, font=title_font, fill=WHITE)

draw.text((text_x, title_y + th + 38), "Спортивная платформа",
          font=slogan_font, fill=WHITE)
draw.text((text_x, title_y + th + 92), "Турниры · Рейтинги · Команды",
          font=sub_font, fill=GRAY)

out = ROOT / "feature_graphic_1024x500.png"
canvas.convert("RGB").save(out, "PNG", optimize=True)
print(f"written: {out} ({out.stat().st_size // 1024} KB)")
