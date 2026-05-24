import os
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageColor

# Define Canvas and Mockup Dimensions
CANVAS_WIDTH = 1200
CANVAS_HEIGHT = 2160

MOCKUP_WIDTH = 760
MOCKUP_HEIGHT = 1520
MOCKUP_X = (CANVAS_WIDTH - MOCKUP_WIDTH) // 2
MOCKUP_Y = 540
BEZEL_THICKNESS = 16
ROUNDED_RADIUS = 50

# Font settings
FONT_PATH = "/System/Library/Fonts/HelveticaNeue.ttc"
FONT_HEADING_SIZE = 58
FONT_SUBHEADING_SIZE = 34

def create_diagonal_gradient(width, height, color_start, color_end):
    """Creates a beautiful diagonal gradient image."""
    base = Image.new("RGBA", (width, height))
    draw = ImageDraw.Draw(base)
    
    c1 = ImageColor.getrgb(color_start)
    c2 = ImageColor.getrgb(color_end)
    
    # Render line-by-line diagonal gradient
    for y in range(height):
        # Blend factor based on diagonal position (x + y)
        factor = y / height
        r = int(c1[0] + (c2[0] - c1[0]) * factor)
        g = int(c1[1] + (c2[1] - c1[1]) * factor)
        b = int(c1[2] + (c2[2] - c1[2]) * factor)
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))
        
    # Apply a subtle diagonal shift
    gradient = Image.new("RGBA", (width, height))
    for x in range(width):
        factor = x / width
        r_offset = int((c2[0] - c1[0]) * factor * 0.2)
        g_offset = int((c2[1] - c1[1]) * factor * 0.2)
        b_offset = int((c2[2] - c1[2]) * factor * 0.2)
        
        # We blend the columns to make it diagonal
        col = base.crop((x, 0, x + 1, height))
        gradient.paste(col, (x, 0))
        
    return gradient

def get_rounded_mask(width, height, radius):
    """Generates a high-quality anti-aliased rounded rectangle mask."""
    mask = Image.new("L", (width * 4, height * 4), 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle(
        [(0, 0), (width * 4, height * 4)],
        radius=radius * 4,
        fill=255
    )
    return mask.resize((width, height), Image.Resampling.LANCZOS)

def draw_soft_shadow(canvas, x, y, width, height, radius, blur_radius=35):
    """Draws a beautiful, photorealistic soft drop shadow behind the device."""
    # Create shadow canvas slightly larger than device to prevent clipping
    pad = blur_radius * 3
    shadow_w = width + pad * 2
    shadow_h = height + pad * 2
    
    shadow_img = Image.new("RGBA", (shadow_w, shadow_h), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_img)
    
    # Draw dark shadow shape with opacity
    s_draw.rounded_rectangle(
        [(pad, pad), (pad + width, pad + height)],
        radius=radius,
        fill=(0, 0, 0, 110)
    )
    
    # Blur the shadow for premium soft appearance
    blurred_shadow = shadow_img.filter(ImageFilter.GaussianBlur(blur_radius))
    
    # Paste on the canvas
    canvas.paste(blurred_shadow, (x - pad, y - pad), blurred_shadow)

def add_device_mockup(canvas, screenshot_path, x, y, width, height, radius, bezel_thickness):
    """Composite the screenshot inside a premium, custom phone frame."""
    if not os.path.exists(screenshot_path):
        print(f"Error: Screenshot {screenshot_path} not found.")
        return False
        
    # 1. Draw the soft drop shadow first
    draw_soft_shadow(canvas, x, y, width, height, radius, blur_radius=40)
    
    # 2. Draw outer phone bezel (matte charcoal / metal dark)
    bezel_color = "#1e293b" # Slate 800
    bezel_accent = "#475569" # Slate 600
    
    # Bezel shadow / highlight
    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle(
        [(x, y), (x + width, y + height)],
        radius=radius,
        fill=bezel_color,
        outline=bezel_accent,
        width=3
    )
    
    # 3. Load and crop the screenshot to fit inner screen
    inner_w = width - bezel_thickness * 2
    inner_h = height - bezel_thickness * 2
    inner_x = x + bezel_thickness
    inner_y = y + bezel_thickness
    inner_radius = radius - bezel_thickness
    
    screenshot = Image.open(screenshot_path).convert("RGBA")
    
    # Resize screenshot maintaining aspect ratio or crop center
    screen_ratio = inner_w / inner_h
    shot_w, shot_h = screenshot.size
    shot_ratio = shot_w / shot_h
    
    if abs(screen_ratio - shot_ratio) > 0.05:
        # Needs cropping
        if shot_ratio > screen_ratio:
            # Screenshot is wider
            new_w = int(shot_h * screen_ratio)
            left = (shot_w - new_w) // 2
            screenshot = screenshot.crop((left, 0, left + new_w, shot_h))
        else:
            # Screenshot is taller
            new_h = int(shot_w / screen_ratio)
            top = (shot_h - new_h) // 2
            screenshot = screenshot.crop((0, top, shot_w, top + new_h))
            
    resized_screenshot = screenshot.resize((inner_w, inner_h), Image.Resampling.LANCZOS)
    
    # Apply rounded mask to inner screen
    mask = get_rounded_mask(inner_w, inner_h, inner_radius)
    canvas.paste(resized_screenshot, (inner_x, inner_y), mask)
    
    # 4. Draw premium highlight rim inside bezel
    draw.rounded_rectangle(
        [(inner_x, inner_y), (inner_x + inner_w, inner_y + inner_h)],
        radius=inner_radius,
        fill=None,
        outline="#ffffff15",
        width=2
    )
    
    # 5. Draw modern "Dynamic Island" at the top center of the screen
    island_w = 160
    island_h = 32
    island_x = x + (width - island_w) // 2
    island_y = inner_y + 18
    draw.rounded_rectangle(
        [(island_x, island_y), (island_x + island_w, island_y + island_h)],
        radius=16,
        fill="#000000e0"
    )
    # Highlight on Dynamic Island
    draw.rounded_rectangle(
        [(island_x, island_y), (island_x + island_w, island_y + island_h)],
        radius=16,
        fill=None,
        outline="#ffffff10",
        width=1
    )
    
    return True

def wrap_text(text, font, max_width):
    """Wraps text line-by-line so that it fits within max_width."""
    words = text.split()
    lines = []
    current_line = []
    
    for word in words:
        current_line.append(word)
        # Check text width of current line
        line_str = " ".join(current_line)
        # Pillow 10+ uses getlength or getbbox
        if font.getlength(line_str) > max_width:
            current_line.pop()
            lines.append(" ".join(current_line))
            current_line = [word]
            
    if current_line:
        lines.append(" ".join(current_line))
    return lines

def add_typography(canvas, headline, subheadline):
    """Render high-end Vietnamese typography on the screenshot header."""
    draw = ImageDraw.Draw(canvas)
    
    # Load fonts (try index 0 for Bold, 1 for Regular/Medium)
    try:
        font_h = ImageFont.truetype(FONT_PATH, FONT_HEADING_SIZE, index=1) # Bold
    except Exception:
        font_h = ImageFont.load_default()
        
    try:
        font_sub = ImageFont.truetype(FONT_PATH, FONT_SUBHEADING_SIZE, index=0) # Regular
    except Exception:
        font_sub = ImageFont.load_default()
        
    # Wrap and Draw Headline
    headline_lines = wrap_text(headline, font_h, CANVAS_WIDTH - 120)
    current_y = 120
    
    for line in headline_lines:
        w = font_h.getlength(line)
        x = (CANVAS_WIDTH - w) // 2
        # Text shadow for subtle premium contrast
        draw.text((x + 2, current_y + 2), line, font=font_h, fill="#00000040")
        draw.text((x, current_y), line, font=font_h, fill="#ffffff")
        current_y += FONT_HEADING_SIZE + 12
        
    current_y += 10 # spacing between head and subhead
    
    # Wrap and Draw Subheadline
    sub_lines = wrap_text(subheadline, font_sub, CANVAS_WIDTH - 160)
    for line in sub_lines:
        w = font_sub.getlength(line)
        x = (CANVAS_WIDTH - w) // 2
        draw.text((x + 1, current_y + 1), line, font=font_sub, fill="#00000030")
        draw.text((x, current_y), line, font=font_sub, fill="#cbd5e1") # Soft cool gray
        current_y += FONT_SUBHEADING_SIZE + 8

def main():
    print("Starting Play Store Screenshots generation...")
    
    # Make sure we have output directory
    output_dir = "/Users/sonle/Desktop/App/AndroidBaseArchitechture/Screenshot"
    os.makedirs(output_dir, exist_ok=True)
    
    # Define Slide configurations
    slides = [
        {
            "id": 1,
            "filename": "PlayStore_1_Home.png",
            "screenshot": os.path.join(output_dir, "Screenshot_20260522-101733.png"),
            "color_start": "#1e1b4b", # Dark indigo
            "color_end": "#0f172a",   # Dark Slate
            "headline": "DIGITIZE & ORGANIZE INSTANTLY",
            "subheadline": "Manage all your vital office, legal, and personal documents in a single structured workspace."
        },
        {
            "id": 2,
            "filename": "PlayStore_2_Camera.png",
            "screenshot": os.path.join(output_dir, "Screenshot_20260522-101805.png"),
            "color_start": "#064e3b", # Emerald
            "color_end": "#022c22",   # Dark Forest Green
            "headline": "AI-POWERED AUTO CAPTURE",
            "subheadline": "Point, scan, and align. Real-time edge detection ensures perfectly cropped documents instantly."
        },
        {
            "id": 3,
            "filename": "PlayStore_3_Review.png",
            "screenshot": os.path.join(output_dir, "Screenshot_20260522-101841.png"),
            "color_start": "#3b0764", # Deep Purple
            "color_end": "#0f172a",   # Dark Slate
            "headline": "FLEXIBLE MULTI-PAGE BATCH",
            "subheadline": "Review, rotate, and reorder pages effortlessly to compile comprehensive digital sheets."
        },
        {
            "id": 4,
            "filename": "PlayStore_4_Filter.png",
            "screenshot": os.path.join(output_dir, "Screenshot_20260522-101851.png"),
            "color_start": "#1e3a8a", # Cobalt Blue
            "color_end": "#0f172a",   # Dark Slate
            "headline": "ULTRA-CLEAR MAGIC FILTERS",
            "subheadline": "Maximize legibility. Revive low-contrast or faded text with advanced contrast and sharpening."
        },
        {
            "id": 5,
            "filename": "PlayStore_5_Success.png",
            "screenshot": os.path.join(output_dir, "Screenshot_20260522-101713.png"),
            "color_start": "#581c87", # Lavender / Purple
            "color_end": "#0f172a",   # Dark Slate
            "headline": "SECURE PDF EXPORT & SHARE",
            "subheadline": "Generate optimized, professional PDF files and share via Email or clouds in a single tap."
        }
    ]
    
    for slide in slides:
        print(f"Generating Slide {slide['id']}: {slide['filename']}...")
        
        # Create gradient canvas
        canvas = create_diagonal_gradient(
            CANVAS_WIDTH,
            CANVAS_HEIGHT,
            slide["color_start"],
            slide["color_end"]
        )
        
        # Add phone mockup with the screenshot
        success = add_device_mockup(
            canvas,
            slide["screenshot"],
            MOCKUP_X,
            MOCKUP_Y,
            MOCKUP_WIDTH,
            MOCKUP_HEIGHT,
            ROUNDED_RADIUS,
            BEZEL_THICKNESS
        )
        
        if success:
            # Add typography overlays
            add_typography(canvas, slide["headline"], slide["subheadline"])
            
            # Save the final masterpiece
            output_path = os.path.join(output_dir, slide["filename"])
            canvas.save(output_path, "PNG")
            print(f"Successfully saved {slide['filename']}")
        else:
            print(f"Skipping {slide['filename']} due to errors.")
            
    print("All Play Store Screenshots generated successfully!")

if __name__ == "__main__":
    main()
