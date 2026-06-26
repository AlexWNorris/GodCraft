import sys
from PIL import Image

def main():
    try:
        img = Image.open('models/enchanment_icons.png').convert("RGBA")
    except Exception as e:
        print(f"Error opening image: {e}")
        return

    cols = 8
    rows = 5
    cell_w = img.width / cols
    cell_h = img.height / rows

    bg_color = img.getpixel((0,0))

    # New atlas image 512x320 (8 columns * 64, 5 rows * 64)
    icon_w, icon_h = 64, 64
    atlas = Image.new("RGBA", (cols * icon_w, rows * icon_h), (0,0,0,0))

    for r in range(rows):
        for c in range(cols):
            x_center = int(c * cell_w + cell_w / 2)
            y_start = int(r * cell_h) + 12  # Adjust if needed
            
            box = (x_center - icon_w // 2, y_start, x_center + icon_w // 2, y_start + icon_h)
            icon = img.crop(box)
            
            # Make background transparent
            icon_data = icon.getdata()
            new_data = [(0,0,0,0) if px == bg_color else px for px in icon_data]
            icon.putdata(new_data)
            
            atlas.paste(icon, (c * icon_w, r * icon_h))

    atlas.save("src/main/resources/assets/godcraft/textures/gui/enchantment_icons.png")
    print("Generated enchantment_icons.png atlas")

if __name__ == '__main__':
    main()
