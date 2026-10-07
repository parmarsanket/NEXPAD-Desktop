package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey

private fun dpadGlyph(control: String): String = when (control.uppercase()) {
    ControlKey.DOWN.key  -> "▼"
    ControlKey.LEFT.key  -> "◀"
    ControlKey.RIGHT.key -> "▶"
    ControlKey.DPAD.key  -> "▲"
    else                 -> "▲"
}

/**
 * Pre-built hardware button templates and syntax skeletons for the NXPRC ecosystem.
 * Extracted from NxprcHtmlCssConverter to maintain modularity and token efficiency.
 */
object NxprcPresets {
    /** Pre-built HTML/CSS templates for instant testing */
    val PRESET_ULTRA_NEXPAD_A get() = PRESET_NEO_TACTILE_A

    val PRESET_CYBER_REACTOR = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --core-color: #00FFA3;
    --core-glow: rgba(0, 255, 163, 0.65);
    --spring-damping: 0.70;
    --spring-stiffness: 460;
    --press-scale: 0.93;
  }
  .button-a {
    position: relative;
    width: 88px;
    height: 88px;
    border-radius: 50%;
    background:
      radial-gradient(circle at 35% 25%, rgba(0, 255, 163, 0.22) 0%, transparent 50%),
      radial-gradient(circle at 50% 50%, #06180f 0%, #020a06 70%, #000000 100%);
    border: 2px solid #1a4a30;
    box-shadow:
      0 14px 30px rgba(0, 0, 0, 0.85),
      0 0 0 3px #0a1f14,
      0 0 28px var(--core-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.38),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .button-a::before {
    content: "";
    position: absolute;
    inset: 6px;
    border-radius: 50%;
    border: 1.5px dashed rgba(0, 255, 163, 0.6);
    background: radial-gradient(circle at 35% 25%, rgba(0, 255, 163, 0.22) 0%, transparent 60%);
  }
  .button-a::after {
    content: "";
    position: absolute;
    top: 8%;
    left: 16%;
    width: 68%;
    height: 36%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.85) 0%, transparent 75%);
    transform: rotate(-12deg);
    border-top: 1.5px solid rgba(255, 255, 255, 0.55);
  }
  .button-a .reactor-core {
    width: 58px;
    height: 58px;
    border-radius: 50%;
    background: radial-gradient(circle at 35% 30%, #16a34a 0%, #065f46 50%, #022c22 100%);
    border: 1.5px solid #22c55e;
    box-shadow:
      inset 0 2px 5px rgba(255, 255, 255, 0.5),
      inset 0 -4px 10px rgba(0, 0, 0, 0.85),
      0 0 18px var(--core-glow);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .button-a span {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 14px #00FFA3,
      0 2px 4px rgba(0, 0, 0, 0.95),
      0 1px 0 rgba(255, 255, 255, 0.9);
    z-index: 5;
  }
  .button-a:active {
    transform: scale(0.93);
  }
</style>
</head>
<body>
  <button class="button-a" data-control="A" data-category="BUTTON" data-name="Cyber Reactor A">
    <div class="reactor-core">
      <span>A</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_CRIMSON_OCTA = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #FF0055;
    --accent-glow: rgba(255, 0, 85, 0.65);
    --spring-damping: 0.68;
    --spring-stiffness: 480;
    --press-scale: 0.92;
  }
  .crimson-octa {
    position: relative;
    width: 92px;
    height: 92px;
    clip-path: polygon(30% 0%, 70% 0%, 100% 30%, 100% 70%, 70% 100%, 30% 100%, 0% 70%, 0% 30%);
    background: radial-gradient(circle at 35% 25%, #e11d48 0%, #9f1239 45%, #3f0713 85%, #180206 100%);
    box-shadow:
      0 14px 30px rgba(0, 0, 0, 0.85),
      inset 0 2px 5px rgba(255, 255, 255, 0.5),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9),
      0 0 26px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .crimson-octa::before {
    content: "";
    position: absolute;
    inset: 5px;
    clip-path: polygon(30% 0%, 70% 0%, 100% 30%, 100% 70%, 70% 100%, 30% 100%, 0% 70%, 0% 30%);
    border: 1.5px solid rgba(255, 255, 255, 0.35);
  }
  .crimson-octa::after {
    content: "";
    position: absolute;
    top: 6px;
    left: 22px;
    width: 48px;
    height: 18px;
    border-radius: 9px;
    background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.7) 0%, transparent 80%);
  }
  .octa-label {
    font-size: 34px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 16px #FF0055,
      0 2px 4px rgba(0, 0, 0, 0.95),
      0 1px 0 rgba(255, 255, 255, 0.9);
    z-index: 5;
  }
  .crimson-octa:active {
    transform: scale(0.92);
  }
</style>
</head>
<body>
  <button class="crimson-octa" data-control="B" data-category="BUTTON" data-name="Crimson Octa B">
    <span class="octa-label">B</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_SPEED_TURBO = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #FFB800;
    --accent-glow: rgba(255, 184, 0, 0.65);
    --spring-damping: 0.72;
    --spring-stiffness: 500;
    --press-scale: 0.91;
  }
  .speed-turbo {
    position: relative;
    width: 90px;
    height: 90px;
    border-radius: 22px;
    background:
      radial-gradient(circle at 35% 25%, #4a3c1b 0%, #1c1507 65%, #080602 100%);
    border: 2px solid #735a26;
    box-shadow:
      0 14px 30px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(26, 20, 7, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.38),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9),
      0 0 24px var(--accent-glow);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
  .speed-turbo::before {
    content: "";
    position: absolute;
    inset: 4px;
    border-radius: 18px;
    border: 1px dashed rgba(255, 184, 0, 0.45);
  }
  .turbo-label {
    font-size: 30px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 14px #FFB800,
      0 2px 4px rgba(0, 0, 0, 0.95),
      0 1px 0 rgba(255, 255, 255, 0.9);
    z-index: 5;
  }
  .turbo-sub {
    font-size: 9px;
    font-weight: 900;
    letter-spacing: 1.5px;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
    z-index: 5;
  }
  .speed-turbo:active {
    transform: scale(0.91);
  }
</style>
</head>
<body>
  <button class="speed-turbo" data-control="Y" data-category="BUTTON" data-name="Speed Turbo Y">
    <span class="turbo-label">Y</span>
    <span class="turbo-sub">TURBO</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_NEO_TACTILE_A = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 96px;
    --accent-glow: rgba(0, 255, 163, 0.65);
    --accent-core: #00FFA3;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.93;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 45% 35%, rgba(0, 255, 163, 0.12) 0%, transparent 60%),
      radial-gradient(circle at 50% 50%, #151b22 0%, #0b0e13 65%, #030406 100%);
    border: 2px solid #1f2937;
    box-shadow: 
      0 14px 30px rgba(0, 0, 0, 0.85),
      0 0 0 3px rgba(17, 24, 39, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn::before {
    content: "";
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #17202a, #2b3a4a, #131a22, #384d63, #17202a, #2b3a4a, #131a22);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.22),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0.08) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.15);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(0, 255, 163, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      linear-gradient(145deg, #111822 0%, #0a0e14 40%, #040608 100%);
    border: 1.5px solid var(--accent-core);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.35),
      inset 0 -5px 10px rgba(0, 0, 0, 0.85);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.9),
      0 0 14px var(--accent-core);
  }

  .nexpad-btn:active {
    transform: scale(0.93);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-codename="Neo Tactile" data-name="Neo Tactile A" data-seed="16166">
    <div class="btn-core">
      <span class="btn-label">A</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_NEO_TACTILE_B = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 96px;
    --accent-glow: rgba(255, 42, 109, 0.65);
    --accent-core: #FF2A6D;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.93;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 45% 35%, rgba(255, 42, 109, 0.12) 0%, transparent 60%),
      radial-gradient(circle at 50% 50%, #1f1418 0%, #10080c 65%, #050203 100%);
    border: 2px solid #381a24;
    box-shadow: 
      0 14px 30px rgba(0, 0, 0, 0.85),
      0 0 0 3px rgba(35, 15, 22, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn::before {
    content: "";
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #2a141b, #4a1f2c, #1a0a10, #612739, #2a141b, #4a1f2c, #1a0a10);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.22),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0.08) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.15);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 42, 109, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      linear-gradient(145deg, #221217 0%, #12060a 40%, #060203 100%);
    border: 1.5px solid var(--accent-core);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.35),
      inset 0 -5px 10px rgba(0, 0, 0, 0.85);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.9),
      0 0 14px var(--accent-core);
  }

  .nexpad-btn:active {
    transform: scale(0.93);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="B" data-category="BUTTON" data-codename="Neo Tactile" data-name="Neo Tactile B" data-seed="24892">
    <div class="btn-core">
      <span class="btn-label">B</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_NEO_TACTILE_X = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 96px;
    --accent-glow: rgba(0, 229, 255, 0.65);
    --accent-core: #00E5FF;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.93;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 45% 35%, rgba(0, 229, 255, 0.12) 0%, transparent 60%),
      radial-gradient(circle at 50% 50%, #111a24 0%, #060e16 65%, #010408 100%);
    border: 2px solid #163147;
    box-shadow: 
      0 14px 30px rgba(0, 0, 0, 0.85),
      0 0 0 3px rgba(12, 30, 45, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn::before {
    content: "";
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #112638, #1c4b6e, #0c1c2b, #256a9e, #112638, #1c4b6e, #0c1c2b);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.22),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0.08) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.15);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(0, 229, 255, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      linear-gradient(145deg, #0d1b28 0%, #050e18 40%, #020509 100%);
    border: 1.5px solid var(--accent-core);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.35),
      inset 0 -5px 10px rgba(0, 0, 0, 0.85);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.9),
      0 0 14px var(--accent-core);
  }

  .nexpad-btn:active {
    transform: scale(0.93);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="X" data-category="BUTTON" data-codename="Neo Tactile" data-name="Neo Tactile X" data-seed="38120">
    <div class="btn-core">
      <span class="btn-label">X</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_NEO_TACTILE_Y = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 96px;
    --accent-glow: rgba(255, 214, 0, 0.65);
    --accent-core: #FFD600;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.93;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 45% 35%, rgba(255, 214, 0, 0.12) 0%, transparent 60%),
      radial-gradient(circle at 50% 50%, #1f1a10 0%, #110e06 65%, #050401 100%);
    border: 2px solid #3d3319;
    box-shadow: 
      0 14px 30px rgba(0, 0, 0, 0.85),
      0 0 0 3px rgba(36, 29, 10, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn::before {
    content: "";
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #291f0c, #4a3814, #1c1406, #634d19, #291f0c, #4a3814, #1c1406);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.22),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0.08) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.15);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 214, 0, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      linear-gradient(145deg, #241c0a 0%, #120e03 40%, #050401 100%);
    border: 1.5px solid var(--accent-core);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.35),
      inset 0 -5px 10px rgba(0, 0, 0, 0.85);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.9),
      0 0 14px var(--accent-core);
  }

  .nexpad-btn:active {
    transform: scale(0.93);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="Y" data-category="BUTTON" data-codename="Neo Tactile" data-name="Neo Tactile Y" data-seed="49551">
    <div class="btn-core">
      <span class="btn-label">Y</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_DPAD_UP = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --dpad-size: 76px;
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.5);
    --spring-damping: 0.68;
    --spring-stiffness: 460;
    --press-scale: 0.92;
  }
  .dpad-btn {
    position: relative;
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 50% 20%, rgba(0, 240, 255, 0.28) 0%, transparent 60%),
      linear-gradient(180deg, #2b3342 0%, #161a23 55%, #080a0e 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.35),
      inset 0 -6px 12px rgba(0, 0, 0, 0.92);
    display: flex;
    align-items: center;
    justify-content: center;
    transform-origin: 50% 50%;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    top: 8px;
    left: 16px;
    width: 44px;
    height: 3.5px;
    border-radius: 2px;
    background: linear-gradient(90deg, transparent 0%, var(--accent) 50%, transparent 100%);
    box-shadow: 0 0 10px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 8%;
    left: 14%;
    width: 72%;
    height: 36%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.40) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 18px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="dpad-btn" data-control="UP" data-category="DPAD" data-name="D-Pad Up">
    <span class="dpad-arrow">▲</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_DPAD_DOWN = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --dpad-size: 76px;
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.5);
    --spring-damping: 0.68;
    --spring-stiffness: 460;
    --press-scale: 0.92;
  }
  .dpad-btn {
    position: relative;
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 50% 80%, rgba(0, 240, 255, 0.28) 0%, transparent 60%),
      linear-gradient(0deg, #2b3342 0%, #161a23 55%, #080a0e 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.35),
      inset 0 -6px 12px rgba(0, 0, 0, 0.92);
    display: flex;
    align-items: center;
    justify-content: center;
    transform-origin: 50% 50%;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    bottom: 8px;
    left: 16px;
    width: 44px;
    height: 3.5px;
    border-radius: 2px;
    background: linear-gradient(90deg, transparent 0%, var(--accent) 50%, transparent 100%);
    box-shadow: 0 0 10px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    bottom: 8%;
    left: 14%;
    width: 72%;
    height: 36%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 70%, rgba(255, 255, 255, 0.40) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 18px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="dpad-btn" data-control="DOWN" data-category="DPAD" data-name="D-Pad Down">
    <span class="dpad-arrow">▼</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_DPAD_LEFT = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --dpad-size: 76px;
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.5);
    --spring-damping: 0.68;
    --spring-stiffness: 460;
    --press-scale: 0.92;
  }
  .dpad-btn {
    position: relative;
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 20% 50%, rgba(0, 240, 255, 0.28) 0%, transparent 60%),
      linear-gradient(90deg, #2b3342 0%, #161a23 55%, #080a0e 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.35),
      inset 0 -6px 12px rgba(0, 0, 0, 0.92);
    display: flex;
    align-items: center;
    justify-content: center;
    transform-origin: 50% 50%;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    left: 8px;
    top: 16px;
    width: 3.5px;
    height: 44px;
    border-radius: 2px;
    background: linear-gradient(180deg, transparent 0%, var(--accent) 50%, transparent 100%);
    box-shadow: 0 0 10px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 14%;
    left: 8%;
    width: 36%;
    height: 72%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 30% 50%, rgba(255, 255, 255, 0.40) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 18px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="dpad-btn" data-control="LEFT" data-category="DPAD" data-name="D-Pad Left">
    <span class="dpad-arrow">◀</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_DPAD_RIGHT = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --dpad-size: 76px;
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.5);
    --spring-damping: 0.68;
    --spring-stiffness: 460;
    --press-scale: 0.92;
  }
  .dpad-btn {
    position: relative;
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 80% 50%, rgba(0, 240, 255, 0.28) 0%, transparent 60%),
      linear-gradient(270deg, #2b3342 0%, #161a23 55%, #080a0e 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.35),
      inset 0 -6px 12px rgba(0, 0, 0, 0.92);
    display: flex;
    align-items: center;
    justify-content: center;
    transform-origin: 50% 50%;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    right: 8px;
    top: 16px;
    width: 3.5px;
    height: 44px;
    border-radius: 2px;
    background: linear-gradient(180deg, transparent 0%, var(--accent) 50%, transparent 100%);
    box-shadow: 0 0 10px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 14%;
    right: 8%;
    width: 36%;
    height: 72%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 70% 50%, rgba(255, 255, 255, 0.40) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 18px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="dpad-btn" data-control="RIGHT" data-category="DPAD" data-name="D-Pad Right">
    <span class="dpad-arrow">▶</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_DPAD_CROSS = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --cross-size: 140px;
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.45);
    --spring-damping: 0.68;
    --spring-stiffness: 460;
    --press-scale: 0.95;
  }

  * { box-sizing: border-box; margin: 0; padding: 0; }

  html, body {
    width: 140px;
    height: 140px;
    background: transparent;
  }

  /* 0: Root controller button — Recessed circular chassis socket well in gamepad body */
  .dpad-cross {
    position: relative;
    width: var(--cross-size);
    height: var(--cross-size);
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #161b24 0%, #0d1017 65%, #05070a 100%);
    border: 2px solid #2a3342;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.85),
      0 0 0 3px rgba(10, 13, 18, 0.95),
      inset 0 6px 14px rgba(0, 0, 0, 0.95),
      inset 0 -2px 5px rgba(255, 255, 255, 0.08),
      0 0 24px var(--accent-glow);
    display: block;
    cursor: pointer;
    outline: none;
    padding: 0;
    transform-origin: 50% 50%;
    -webkit-tap-highlight-color: transparent;
  }

  .dpad-cross > * { position: absolute; }

  /* 1: Recessed circular socket cavity trim ring */
  .dpad-socket-trim {
    inset: 4px;
    border-radius: 50%;
    border: 1.5px dashed rgba(0, 240, 255, 0.3);
    pointer-events: none;
  }

  /* 2: Vertical physical cross arm bar */
  .dpad-arm-v {
    left: 47px;
    top: 8px;
    width: 46px;
    height: 124px;
    border-radius: 10px;
    background: linear-gradient(180deg, #2c3545 0%, #1a202a 35%, #12161e 65%, #222a38 100%);
    border: 1.5px solid rgba(255, 255, 255, 0.18);
    box-shadow:
      0 6px 14px rgba(0, 0, 0, 0.75),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -4px 8px rgba(0, 0, 0, 0.85);
  }

  /* 3: Horizontal physical cross arm bar */
  .dpad-arm-h {
    left: 8px;
    top: 47px;
    width: 124px;
    height: 46px;
    border-radius: 10px;
    background: linear-gradient(90deg, #2c3545 0%, #1a202a 35%, #12161e 65%, #222a38 100%);
    border: 1.5px solid rgba(255, 255, 255, 0.18);
    box-shadow:
      0 6px 14px rgba(0, 0, 0, 0.75),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -4px 8px rgba(0, 0, 0, 0.85);
  }

  /* 4: Tactile directional arm button face plates (UP, DOWN, LEFT, RIGHT) */
  .dpad-arm {
    box-sizing: border-box;
    z-index: 3;
  }
  .dpad-arm-top {
    top: 10px;
    left: 49px;
    width: 42px;
    height: 38px;
    border-radius: 8px 8px 3px 3px;
    background: linear-gradient(180deg, #384357 0%, #1c222c 100%);
    box-shadow: inset 0 2px 3px rgba(255, 255, 255, 0.3), inset 0 -2px 4px rgba(0, 0, 0, 0.6);
  }
  .dpad-arm-bottom {
    bottom: 10px;
    left: 49px;
    width: 42px;
    height: 38px;
    border-radius: 3px 3px 8px 8px;
    background: linear-gradient(0deg, #384357 0%, #1c222c 100%);
    box-shadow: inset 0 -2px 3px rgba(255, 255, 255, 0.2), inset 0 2px 4px rgba(0, 0, 0, 0.6);
  }
  .dpad-arm-left {
    left: 10px;
    top: 49px;
    width: 38px;
    height: 42px;
    border-radius: 8px 3px 3px 8px;
    background: linear-gradient(90deg, #384357 0%, #1c222c 100%);
    box-shadow: inset 2px 0 3px rgba(255, 255, 255, 0.3), inset -2px 0 4px rgba(0, 0, 0, 0.6);
  }
  .dpad-arm-right {
    right: 10px;
    top: 49px;
    width: 38px;
    height: 42px;
    border-radius: 3px 8px 8px 3px;
    background: linear-gradient(270deg, #384357 0%, #1c222c 100%);
    box-shadow: inset -2px 0 3px rgba(255, 255, 255, 0.3), inset 2px 0 4px rgba(0, 0, 0, 0.6);
  }

  /* 5: Central concave thumb rest cup / rocker pivot */
  .dpad-pivot {
    left: 48px;
    top: 48px;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: radial-gradient(circle at 45% 45%, #080a0e 0%, #141923 60%, #1e2531 100%);
    box-shadow:
      inset 0 3px 6px rgba(0, 0, 0, 0.95),
      0 1px 2px rgba(255, 255, 255, 0.2);
    border: 1.5px solid rgba(0, 240, 255, 0.45);
    z-index: 4;
  }

  /* 6: Cardinal directional chevrons / arrows */
  .dpad-arrow {
    position: absolute;
    font-size: 18px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 12px var(--accent), 0 0 6px var(--accent), 0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    user-select: none;
  }
  .dpad-up    { top: 15px; left: 56px; }
  .dpad-down  { bottom: 15px; left: 56px; }
  .dpad-left  { left: 15px; top: 56px; }
  .dpad-right { right: 15px; top: 56px; }

  /* In-place uniform scale kinematics (zero displacement) */
  .dpad-cross:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="dpad-btn dpad-cross" data-control="DPAD" data-category="DPAD" data-name="Tactile Cross Pad">
    <div class="dpad-socket-trim"></div>
    <div class="dpad-arm-v"></div>
    <div class="dpad-arm-h"></div>
    <div class="dpad-arm dpad-arm-top"></div>
    <div class="dpad-arm dpad-arm-bottom"></div>
    <div class="dpad-arm dpad-arm-left"></div>
    <div class="dpad-arm dpad-arm-right"></div>
    <div class="dpad-pivot"></div>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_TRIGGER_LT = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.5);
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.95;
  }
  .trigger-btn {
    position: relative;
    width: 74px;
    height: 112px;
    border-radius: 20px 20px 28px 28px;
    background:
      radial-gradient(circle at 50% 25%, rgba(255, 255, 255, 0.16) 0%, transparent 60%),
      linear-gradient(180deg, #2a3140 0%, #151923 45%, #080a0e 100%);
    border: 2px solid #3e4b60;
    box-shadow:
      0 0 24px var(--accent-glow),
      0 14px 28px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.35),
      inset 0 -8px 16px rgba(0, 0, 0, 0.92);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 16px;
    box-sizing: border-box;
    transform-origin: 50% 50%;
  }
  .trigger-btn::after {
    content: "";
    position: absolute;
    top: 6px;
    left: 12px;
    width: 50px;
    height: 18px;
    border-radius: 9px;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.45) 0%, transparent 75%);
  }
  .trigger-well {
    position: absolute;
    bottom: 14px;
    width: 48px;
    height: 42px;
    border-radius: 10px;
    background: #0c0f16;
    border: 1.5px solid #283344;
    box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.95);
    overflow: hidden;
  }
  .trigger-fill {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 18px;
    border-radius: 0 0 8px 8px;
    background: linear-gradient(180deg, var(--accent) 0%, rgba(0, 240, 255, 0.3) 100%);
    box-shadow: 0 0 10px var(--accent);
  }
  .trigger-rib-1 {
    position: absolute;
    top: 8px;
    left: 7px;
    width: 34px;
    height: 3px;
    border-radius: 1.5px;
    background: rgba(255, 255, 255, 0.35);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
    z-index: 2;
  }
  .trigger-rib-2 {
    position: absolute;
    top: 18px;
    left: 7px;
    width: 34px;
    height: 3px;
    border-radius: 1.5px;
    background: rgba(255, 255, 255, 0.35);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
    z-index: 2;
  }
  .trigger-rib-3 {
    position: absolute;
    top: 28px;
    left: 7px;
    width: 34px;
    height: 3px;
    border-radius: 1.5px;
    background: rgba(255, 255, 255, 0.35);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
    z-index: 2;
  }
  .trigger-label {
    font-size: 26px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 16px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .trigger-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="trigger-btn" data-control="LT" data-category="TRIGGER" data-name="Tactile Trigger LT">
    <span class="trigger-label">LT</span>
    <div class="trigger-well">
      <div class="trigger-fill"></div>
      <div class="trigger-rib-1"></div>
      <div class="trigger-rib-2"></div>
      <div class="trigger-rib-3"></div>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_TRIGGER_RT = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #FF3366;
    --accent-glow: rgba(255, 51, 102, 0.5);
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.95;
  }
  .trigger-btn {
    position: relative;
    width: 74px;
    height: 112px;
    border-radius: 20px 20px 28px 28px;
    background:
      radial-gradient(circle at 50% 25%, rgba(255, 255, 255, 0.16) 0%, transparent 60%),
      linear-gradient(180deg, #2a3140 0%, #151923 45%, #080a0e 100%);
    border: 2px solid #3e4b60;
    box-shadow:
      0 0 24px var(--accent-glow),
      0 14px 28px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.35),
      inset 0 -8px 16px rgba(0, 0, 0, 0.92);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 16px;
    box-sizing: border-box;
    transform-origin: 50% 50%;
  }
  .trigger-btn::after {
    content: "";
    position: absolute;
    top: 6px;
    left: 12px;
    width: 50px;
    height: 18px;
    border-radius: 9px;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.45) 0%, transparent 75%);
  }
  .trigger-well {
    position: absolute;
    bottom: 14px;
    width: 48px;
    height: 42px;
    border-radius: 10px;
    background: #0c0f16;
    border: 1.5px solid #283344;
    box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.95);
    overflow: hidden;
  }
  .trigger-fill {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 18px;
    border-radius: 0 0 8px 8px;
    background: linear-gradient(180deg, var(--accent) 0%, rgba(255, 51, 102, 0.3) 100%);
    box-shadow: 0 0 10px var(--accent);
  }
  .trigger-rib-1 {
    position: absolute;
    top: 8px;
    left: 7px;
    width: 34px;
    height: 3px;
    border-radius: 1.5px;
    background: rgba(255, 255, 255, 0.35);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
    z-index: 2;
  }
  .trigger-rib-2 {
    position: absolute;
    top: 18px;
    left: 7px;
    width: 34px;
    height: 3px;
    border-radius: 1.5px;
    background: rgba(255, 255, 255, 0.35);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
    z-index: 2;
  }
  .trigger-rib-3 {
    position: absolute;
    top: 28px;
    left: 7px;
    width: 34px;
    height: 3px;
    border-radius: 1.5px;
    background: rgba(255, 255, 255, 0.35);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.8);
    z-index: 2;
  }
  .trigger-label {
    font-size: 26px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 16px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .trigger-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="trigger-btn" data-control="RT" data-category="TRIGGER" data-name="Tactile Trigger RT">
    <span class="trigger-label">RT</span>
    <div class="trigger-well">
      <div class="trigger-fill"></div>
      <div class="trigger-rib-1"></div>
      <div class="trigger-rib-2"></div>
      <div class="trigger-rib-3"></div>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_BUMPER_LB = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.45);
    --spring-damping: 0.75;
    --spring-stiffness: 520;
    --press-scale: 0.95;
  }
  .bumper-btn {
    position: relative;
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background:
      radial-gradient(circle at 50% 15%, rgba(255, 255, 255, 0.16) 0%, transparent 55%),
      linear-gradient(180deg, #2e3646 0%, #171b25 55%, #080a0e 100%);
    border: 2px solid #3e4b60;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.8),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.32),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .bumper-btn::before {
    content: "";
    position: absolute;
    bottom: 0;
    left: 15%;
    width: 70%;
    height: 3px;
    border-radius: 2px 2px 0 0;
    background: linear-gradient(90deg, transparent 0%, var(--accent) 50%, transparent 100%);
    opacity: 0.7;
    box-shadow: 0 0 6px var(--accent);
  }
  .bumper-btn::after {
    content: "";
    position: absolute;
    top: 10%;
    left: 12%;
    width: 76%;
    height: 35%;
    border-radius: 10px;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.45) 0%, transparent 75%);
  }
  .bumper-label {
    font-size: 24px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 14px var(--accent),
      0 0 8px var(--accent),
      0 2px 5px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .bumper-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="bumper-btn" data-control="LB" data-category="BUMPER" data-name="Shoulder Bumper LB">
    <span class="bumper-label">LB</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_BUMPER_RB = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.45);
    --spring-damping: 0.75;
    --spring-stiffness: 520;
    --press-scale: 0.95;
  }
  .bumper-btn {
    position: relative;
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background:
      radial-gradient(circle at 50% 15%, rgba(255, 255, 255, 0.16) 0%, transparent 55%),
      linear-gradient(180deg, #2e3646 0%, #171b25 55%, #080a0e 100%);
    border: 2px solid #3e4b60;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.8),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.32),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .bumper-btn::before {
    content: "";
    position: absolute;
    bottom: 0;
    left: 15%;
    width: 70%;
    height: 3px;
    border-radius: 2px 2px 0 0;
    background: linear-gradient(90deg, transparent 0%, var(--accent) 50%, transparent 100%);
    opacity: 0.7;
    box-shadow: 0 0 6px var(--accent);
  }
  .bumper-btn::after {
    content: "";
    position: absolute;
    top: 10%;
    left: 12%;
    width: 76%;
    height: 35%;
    border-radius: 10px;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.45) 0%, transparent 75%);
  }
  .bumper-label {
    font-size: 24px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 14px var(--accent),
      0 0 8px var(--accent),
      0 2px 5px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .bumper-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="bumper-btn" data-control="RB" data-category="BUMPER" data-name="Shoulder Bumper RB">
    <span class="bumper-label">RB</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_THUMBSTICK_LS = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --stick-size: 104px;
    --accent: #00E5FF;
    --accent-glow: rgba(0, 229, 255, 0.5);
    --spring-damping: 0.70;
    --spring-stiffness: 420;
    --press-scale: 0.92;
  }
  .stick-btn {
    width: var(--stick-size);
    height: var(--stick-size);
    position: relative;
    background: transparent;
    border: none;
    padding: 0;
    outline: none;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .stick-base {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background:
      radial-gradient(circle at 45% 40%, #2e3544 0%, #151822 65%, #07080c 100%);
    border: 3px solid #3e4b60;
    box-shadow:
      0 0 24px var(--accent-glow),
      0 14px 28px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 3px 6px rgba(255, 255, 255, 0.3),
      inset 0 -8px 18px rgba(0, 0, 0, 0.92);
    box-sizing: border-box;
  }
  .stick-cap {
    position: absolute;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background:
      radial-gradient(circle at 48% 42%, #282f3c 0%, #13161f 65%, #080a0e 100%);
    border: 1.5px solid rgba(255, 255, 255, 0.18);
    box-shadow:
      inset 0 0 14px rgba(0, 0, 0, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.22),
      0 4px 10px rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .knurled-ring {
    position: absolute;
    width: 46px;
    height: 46px;
    border-radius: 50%;
    border: 2px dashed var(--accent);
    box-shadow: 0 0 8px var(--accent-glow);
    opacity: 0.8;
    box-sizing: border-box;
  }
  .stick-label {
    font-size: 20px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 16px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .stick-btn:active .stick-cap {
    transform: scale(0.92);
  }
</style>
</head>
<body>
  <button class="stick-btn" data-control="LS" data-category="JOYSTICK" data-name="Analog Stick LS">
    <div class="stick-base"></div>
    <div class="stick-cap">
      <div class="knurled-ring"></div>
      <span class="stick-label">LS</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_THUMBSTICK_RS = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --stick-size: 104px;
    --accent: #FF007F;
    --accent-glow: rgba(255, 0, 127, 0.5);
    --spring-damping: 0.70;
    --spring-stiffness: 420;
    --press-scale: 0.92;
  }
  .stick-btn {
    width: var(--stick-size);
    height: var(--stick-size);
    position: relative;
    background: transparent;
    border: none;
    padding: 0;
    outline: none;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .stick-base {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background:
      radial-gradient(circle at 45% 40%, #2e3544 0%, #151822 65%, #07080c 100%);
    border: 3px solid #3c485c;
    box-shadow:
      0 0 24px var(--accent-glow),
      0 14px 28px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 3px 6px rgba(255, 255, 255, 0.3),
      inset 0 -8px 18px rgba(0, 0, 0, 0.92);
    box-sizing: border-box;
  }
  .stick-cap {
    position: absolute;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background:
      radial-gradient(circle at 48% 42%, #282f3c 0%, #13161f 65%, #080a0e 100%);
    border: 1.5px solid rgba(255, 255, 255, 0.18);
    box-shadow:
      inset 0 0 14px rgba(0, 0, 0, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.22),
      0 4px 10px rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .knurled-ring {
    position: absolute;
    width: 46px;
    height: 46px;
    border-radius: 50%;
    border: 2px dashed var(--accent);
    box-shadow: 0 0 8px var(--accent-glow);
    opacity: 0.8;
    box-sizing: border-box;
  }
  .stick-label {
    font-size: 20px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 16px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .stick-btn:active .stick-cap {
    transform: scale(0.92);
  }
</style>
</head>
<body>
  <button class="stick-btn" data-control="RS" data-category="JOYSTICK" data-name="Analog Stick RS">
    <div class="stick-base"></div>
    <div class="stick-cap">
      <div class="knurled-ring"></div>
      <span class="stick-label">RS</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_STICK_BUTTON_LSB = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 70px;
    --accent: #00E5FF;
    --accent-glow: rgba(0, 229, 255, 0.5);
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.90;
  }
  .stick-btn-ctl {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 35% 35%, #2d3342 0%, #101217 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.8),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.35),
      inset 0 -6px 12px rgba(0, 0, 0, 0.88);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .stick-btn-ctl::before {
    content: "";
    position: absolute;
    left: 7px;
    top: 7px;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    border: 1.5px dashed var(--accent);
    box-shadow: 0 0 8px var(--accent-glow);
    opacity: 0.8;
    box-sizing: border-box;
  }
  .stick-btn-dish {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #222631 0%, #0d0e12 100%);
    border: 1px solid rgba(255, 255, 255, 0.18);
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.95), 0 0 10px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .stick-btn-label {
    font-size: 15px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 12px var(--accent), 0 1px 3px rgba(0, 0, 0, 0.95);
    letter-spacing: 0.5px;
  }
  .stick-btn-ctl:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="stick-btn-ctl" data-control="LSB" data-category="BUTTON" data-name="Stick Button LSB">
    <div class="stick-btn-dish">
      <span class="stick-btn-label">LSB</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_STICK_BUTTON_RSB = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 70px;
    --accent: #FF007F;
    --accent-glow: rgba(255, 0, 127, 0.5);
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.90;
  }
  .stick-btn-ctl {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 35% 35%, #2d3342 0%, #101217 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 22px var(--accent-glow),
      0 12px 24px rgba(0, 0, 0, 0.8),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.35),
      inset 0 -6px 12px rgba(0, 0, 0, 0.88);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .stick-btn-ctl::before {
    content: "";
    position: absolute;
    left: 7px;
    top: 7px;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    border: 1.5px dashed var(--accent);
    box-shadow: 0 0 8px var(--accent-glow);
    opacity: 0.8;
    box-sizing: border-box;
  }
  .stick-btn-dish {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #222631 0%, #0d0e12 100%);
    border: 1px solid rgba(255, 255, 255, 0.18);
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.95), 0 0 10px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .stick-btn-label {
    font-size: 15px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 12px var(--accent), 0 1px 3px rgba(0, 0, 0, 0.95);
    letter-spacing: 0.5px;
  }
  .stick-btn-ctl:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="stick-btn-ctl" data-control="RSB" data-category="BUTTON" data-name="Stick Button RSB">
    <div class="stick-btn-dish">
      <span class="stick-btn-label">RSB</span>
    </div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_TOUCHPAD_LTP = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --pad-size: 180px;
    --accent: #00E5FF;
    --accent-glow: rgba(0, 229, 255, 0.45);
  }
  .touchpad-ctl {
    width: var(--pad-size);
    height: var(--pad-size);
    border-radius: 26px;
    background: radial-gradient(circle at 45% 40%, #262c38 0%, #13161f 60%, #07080b 100%);
    border: 2.5px solid #384254;
    box-shadow:
      0 0 24px var(--accent-glow),
      0 14px 28px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    padding: 14px;
    position: relative;
    box-sizing: border-box;
    overflow: hidden;
  }
  .touchpad-surface {
    position: absolute;
    inset: 14px;
    border-radius: 18px;
    border: 1.5px solid rgba(255, 255, 255, 0.16);
    background: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.06) 0%, transparent 75%);
    box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.7), inset 0 0 10px var(--accent-glow);
    pointer-events: none;
  }
  .touchpad-title {
    font-size: 15px;
    font-weight: 900;
    color: #FFFFFF;
    letter-spacing: 1.4px;
    text-transform: uppercase;
    text-shadow:
      0 0 16px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .touchpad-sub {
    font-size: 12px;
    font-weight: 800;
    color: #FFFFFF;
    letter-spacing: 1.2px;
    text-shadow:
      0 0 12px var(--accent),
      0 1px 3px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
</style>
</head>
<body>
  <button class="touchpad-ctl" data-id="touch_ltp" data-control="LTP" data-category="TOUCHPAD" data-name="Touchpad LTP">
    <span class="touchpad-title">Touch Move • LTP</span>
    <div class="touchpad-surface"></div>
    <span class="touchpad-sub">2.0X BALLISTICS</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_TOUCHPAD_RTP = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --pad-size: 180px;
    --accent: #FF007F;
    --accent-glow: rgba(255, 0, 127, 0.45);
  }
  .touchpad-ctl {
    width: var(--pad-size);
    height: var(--pad-size);
    border-radius: 26px;
    background: radial-gradient(circle at 45% 40%, #262c38 0%, #13161f 60%, #07080b 100%);
    border: 2.5px solid #384254;
    box-shadow:
      0 0 24px var(--accent-glow),
      0 14px 28px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.9);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    padding: 14px;
    position: relative;
    box-sizing: border-box;
    overflow: hidden;
  }
  .touchpad-surface {
    position: absolute;
    inset: 14px;
    border-radius: 18px;
    border: 1.5px solid rgba(255, 255, 255, 0.16);
    background: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.06) 0%, transparent 75%);
    box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.7), inset 0 0 10px var(--accent-glow);
    pointer-events: none;
  }
  .touchpad-title {
    font-size: 15px;
    font-weight: 900;
    color: #FFFFFF;
    letter-spacing: 1.4px;
    text-transform: uppercase;
    text-shadow:
      0 0 16px var(--accent),
      0 0 8px var(--accent),
      0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .touchpad-sub {
    font-size: 12px;
    font-weight: 800;
    color: #FFFFFF;
    letter-spacing: 1.2px;
    text-shadow:
      0 0 12px var(--accent),
      0 1px 3px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
</style>
</head>
<body>
  <button class="touchpad-ctl" data-id="touch_rtp" data-control="RTP" data-category="TOUCHPAD" data-name="Touchpad RTP">
    <span class="touchpad-title">Touch Look • RTP</span>
    <div class="touchpad-surface"></div>
    <span class="touchpad-sub">2.0X BALLISTICS</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_SYSTEM_MENU = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.78;
    --spring-stiffness: 500;
    --press-scale: 0.92;
  }
  .system-btn {
    width: 64px;
    height: 42px;
    border-radius: 14px;
    background: radial-gradient(circle at 50% 30%, #282f3c 0%, #111318 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 18px rgba(0, 240, 255, 0.25),
      0 10px 20px rgba(0, 0, 0, 0.75),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 1.5px 3px rgba(255, 255, 255, 0.35),
      inset 0 -4px 8px rgba(0, 0, 0, 0.85);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
  .burger-bar {
    width: 24px;
    height: 3.5px;
    border-radius: 2px;
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.85) 0%, #FFFFFF 50%, rgba(255, 255, 255, 0.85) 100%);
    box-shadow: 0 0 8px rgba(0, 240, 255, 0.5), 0 1px 2px rgba(0, 0, 0, 0.9);
  }
  .system-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="system-btn" data-control="MENU" data-category="SYSTEM" data-name="System Menu">
    <div class="burger-bar"></div>
    <div class="burger-bar"></div>
    <div class="burger-bar"></div>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_SYSTEM_VIEW = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.78;
    --spring-stiffness: 500;
    --press-scale: 0.92;
  }
  .system-btn {
    width: 64px;
    height: 42px;
    border-radius: 14px;
    background: radial-gradient(circle at 50% 30%, #282f3c 0%, #111318 100%);
    border: 2px solid #3c485c;
    box-shadow:
      0 0 18px rgba(0, 240, 255, 0.25),
      0 10px 20px rgba(0, 0, 0, 0.75),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 1.5px 3px rgba(255, 255, 255, 0.35),
      inset 0 -4px 8px rgba(0, 0, 0, 0.85);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .view-icon {
    font-size: 22px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 14px rgba(0, 240, 255, 0.85), 0 1px 3px rgba(0, 0, 0, 0.95);
  }
  .system-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="system-btn" data-control="VIEW" data-category="SYSTEM" data-name="System View">
    <span class="view-icon">⧉</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_SYSTEM_HOME = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.78;
    --spring-stiffness: 500;
    --press-scale: 0.93;
  }
  .system-home-btn {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 35%, #2c3445 0%, #12151d 70%, #06070a 100%);
    border: 2px solid #455268;
    box-shadow:
      0 0 28px rgba(0, 240, 255, 0.65),
      0 12px 26px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.38),
      inset 0 -6px 12px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .home-symbol {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow:
      0 0 20px rgba(0, 240, 255, 0.95),
      0 0 10px rgba(255, 255, 255, 0.9),
      0 2px 4px rgba(0, 0, 0, 0.95);
  }
  .system-home-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="system-btn system-home-btn" data-control="HOME" data-category="SYSTEM" data-name="System Home">
    <span class="home-symbol">⨂</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_SYSTEM_SHARE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.78;
    --spring-stiffness: 500;
    --press-scale: 0.93;
  }
  .system-share-btn {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 35%, #2c3445 0%, #12151d 70%, #06070a 100%);
    border: 2px solid #455268;
    box-shadow:
      0 0 24px rgba(0, 240, 255, 0.45),
      0 12px 26px rgba(0, 0, 0, 0.85),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.38),
      inset 0 -6px 12px rgba(0, 0, 0, 0.88);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .share-symbol {
    font-size: 28px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 16px rgba(0, 240, 255, 0.9), 0 2px 4px rgba(0, 0, 0, 0.95);
  }
  .system-share-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="system-btn system-share-btn" data-control="SHARE" data-category="SYSTEM" data-name="System Share">
    <span class="share-symbol">⇪</span>
  </button>
</body>
</html>
""".trimIndent()

    fun createPresetMacroPaddle(control: String = "M1"): String = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.94;
    --macro-accent: #F59E0B;
  }
  .macro-paddle {
    position: relative;
    width: 72px;
    height: 72px;
    border-radius: 16px;
    background: radial-gradient(circle at 40% 30%, #262c3a 0%, #12161f 65%, #080a0f 100%);
    border: 2px solid #404c62;
    box-shadow:
      0 0 24px rgba(245, 158, 11, 0.45),
      0 12px 24px rgba(0, 0, 0, 0.8),
      0 0 0 2px rgba(18, 22, 30, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.3),
      inset 0 -6px 12px rgba(0, 0, 0, 0.88);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
  }
  .macro-paddle::before {
    content: "";
    position: absolute;
    inset: 5px;
    border-radius: 12px;
    border: 1.5px dashed rgba(245, 158, 11, 0.55);
    box-shadow: inset 0 0 8px rgba(245, 158, 11, 0.2);
  }
  .macro-bolt {
    font-size: 15px;
    color: var(--macro-accent);
    text-shadow: 0 0 12px rgba(245, 158, 11, 0.85);
  }
  .macro-label {
    font-size: 20px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 12px rgba(245, 158, 11, 0.9), 0 2px 4px rgba(0, 0, 0, 0.95);
  }
  .macro-paddle:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="macro-paddle" data-control="$control" data-category="MACRO" data-name="Paddle $control">
    <span class="macro-bolt">⚡</span>
    <span class="macro-label">$control</span>
  </button>
</body>
</html>
""".trimIndent()

    val PRESET_MACRO_M1 get() = createPresetMacroPaddle("M1")
    val PRESET_MACRO_M2 get() = createPresetMacroPaddle("M2")
    val PRESET_MACRO_M3 get() = createPresetMacroPaddle("M3")
    val PRESET_MACRO_M4 get() = createPresetMacroPaddle("M4")

    /**
     * Returns the optional reference template (document structure guide only) for any controller button key.
     * Templates are REFERENCE ONLY — do not treat them as 100% NXPRC-compliant HTML.
     * Some legacy templates may contain properties unsupported by the NXPRC compiler (e.g. filter: blur()).
     * Always follow the STRICT NEXPAD COMPILER CONTRACT defined in [generateAiPrompt].
     */
    fun getReferenceTemplate(control: String): String = getReferenceTemplateInternal(control, "BUTTON")

    /** Returns the category-specific reference template (structure guide only) used by the desktop studio AI prompt. */
    fun getReferenceTemplate(control: String, category: String): String = getReferenceTemplateInternal(control, category)

    private fun getReferenceTemplateInternal(control: String, category: String): String {
        val ctrl = ControlKey.fromIdentifier(control)
        val catType = ctrl?.categoryType
            ?: CategoryType.fromIdentifier(category)
            ?: CategoryManager.findCategoryForControl(control)?.type

        if (catType != null && catType != CategoryType.ABXY) {
            return when (catType) {
                CategoryType.DPAD -> when (ctrl) {
                    ControlKey.DOWN -> PRESET_DPAD_DOWN
                    ControlKey.LEFT -> PRESET_DPAD_LEFT
                    ControlKey.RIGHT -> PRESET_DPAD_RIGHT
                    ControlKey.DPAD -> PRESET_DPAD_CROSS
                    else -> PRESET_DPAD_UP
                }
                CategoryType.TRIGGERS -> if (ctrl == ControlKey.LT) PRESET_TRIGGER_LT else PRESET_TRIGGER_RT
                CategoryType.BUMPERS -> if (ctrl == ControlKey.LB) PRESET_BUMPER_LB else PRESET_BUMPER_RB
                CategoryType.STICKS -> when (ctrl) {
                    ControlKey.RS -> PRESET_THUMBSTICK_RS
                    ControlKey.LSB -> PRESET_STICK_BUTTON_LSB
                    ControlKey.RSB -> PRESET_STICK_BUTTON_RSB
                    ControlKey.LTP -> PRESET_TOUCHPAD_LTP
                    ControlKey.RTP -> PRESET_TOUCHPAD_RTP
                    else -> PRESET_THUMBSTICK_LS
                }
                CategoryType.SYSTEM -> when (ctrl) {
                    ControlKey.BACK -> PRESET_SYSTEM_VIEW
                    ControlKey.GUIDE -> PRESET_SYSTEM_HOME
                    ControlKey.SHARE -> PRESET_SYSTEM_SHARE
                    else -> PRESET_SYSTEM_MENU
                }
                CategoryType.MACROS -> when (ctrl) {
                    ControlKey.M2 -> createPresetMacroPaddle("M2")
                    ControlKey.M3 -> createPresetMacroPaddle("M3")
                    ControlKey.M4 -> createPresetMacroPaddle("M4")
                    else -> createPresetMacroPaddle(ctrl?.key ?: "M1")
                }
                CategoryType.ABXY -> getReferenceTemplateInternal(control, "BUTTON")
            }
        }

        return when (ctrl) {
            ControlKey.A -> PRESET_NEO_TACTILE_A
            ControlKey.B -> PRESET_NEO_TACTILE_B
            ControlKey.X -> PRESET_NEO_TACTILE_X
            ControlKey.Y -> PRESET_NEO_TACTILE_Y
            ControlKey.UP -> PRESET_DPAD_UP
            ControlKey.DOWN -> PRESET_DPAD_DOWN
            ControlKey.LEFT -> PRESET_DPAD_LEFT
            ControlKey.RIGHT -> PRESET_DPAD_RIGHT
            ControlKey.DPAD -> PRESET_DPAD_CROSS
            ControlKey.LT -> PRESET_TRIGGER_LT
            ControlKey.RT -> PRESET_TRIGGER_RT
            ControlKey.LB -> PRESET_BUMPER_LB
            ControlKey.RB -> PRESET_BUMPER_RB
            ControlKey.LS -> PRESET_THUMBSTICK_LS
            ControlKey.RS -> PRESET_THUMBSTICK_RS
            ControlKey.LSB -> PRESET_STICK_BUTTON_LSB
            ControlKey.RSB -> PRESET_STICK_BUTTON_RSB
            ControlKey.LTP -> PRESET_TOUCHPAD_LTP
            ControlKey.RTP -> PRESET_TOUCHPAD_RTP
            ControlKey.START -> PRESET_SYSTEM_MENU
            ControlKey.BACK -> PRESET_SYSTEM_VIEW
            ControlKey.GUIDE -> PRESET_SYSTEM_HOME
            ControlKey.SHARE -> PRESET_SYSTEM_SHARE
            ControlKey.M1 -> createPresetMacroPaddle("M1")
            ControlKey.M2 -> createPresetMacroPaddle("M2")
            ControlKey.M3 -> createPresetMacroPaddle("M3")
            ControlKey.M4 -> createPresetMacroPaddle("M4")
            else -> PRESET_NEO_TACTILE_A
        }
    }

    /**
     * Returns an unstyled, non-binding syntax skeleton illustrating the minimal compiler contract
     * for a given category. Intentionally free of pre-baked colors, gradients, and border-radii
     * to eliminate visual imitation bias in generative AI models.
     */
    fun getSyntaxSkeleton(control: String, category: String, widthDp: Int, heightDp: Int): String {
        val ctrl = ControlKey.fromIdentifier(control)
        if (ctrl == ControlKey.LTP || ctrl == ControlKey.RTP ||
            (ctrl?.componentType == ComponentType.TOUCHPAD && ctrl.categoryType == CategoryType.STICKS)) {
            return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --pad-w: ${widthDp}px;
    --pad-h: ${heightDp}px;
    --spring-damping: 0.78;
    --spring-stiffness: 500;
    --press-scale: 0.98;
  }
  .touchpad-ctl {
    width: var(--pad-w);
    height: var(--pad-h);
    position: relative;
    border-radius: 26px;
    box-sizing: border-box;
    background: radial-gradient(circle at 50% 45%, #181c24 0%, #0d1016 70%, #06080b 100%);
    border: 2px solid #232a36;
    box-shadow: 
      0 10px 24px rgba(0, 0, 0, 0.75),
      inset 0 2px 4px rgba(255, 255, 255, 0.22),
      inset 0 -6px 12px rgba(0, 0, 0, 0.85);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    /* Pure flat, stationary laptop trackpad surface (2.0x default ballistics). */
    /* Strictly NO center button, NO center dot, NO movable ring, and NO tap-to-click. */
  }
  .touchpad-title {
    font-size: 20px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #94A3B8;
    letter-spacing: 1.5px;
  }
  .touchpad-ctl:active {
    transform: scale(0.98);
  }
</style>
</head>
<body>
  <button class="touchpad-ctl" data-id="touch_${control.lowercase()}" data-control="$control" data-category="$category" data-name="Touchpad $control">
    <span class="touchpad-title">$control</span>
  </button>
</body>
</html>
""".trimIndent()
        }

        if (ctrl == ControlKey.LSB || ctrl == ControlKey.RSB ||
            (ctrl?.componentType == ComponentType.BUTTON && ctrl.categoryType == CategoryType.STICKS)) {
            return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.90;
  }
  .stick-btn-ctl {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    border-radius: 50%;
    box-sizing: border-box;
    background: radial-gradient(circle at 45% 40%, #20242e 0%, #12151d 65%, #08090c 100%);
    border: 2px solid #2e3544;
    box-shadow: 
      0 8px 20px rgba(0, 0, 0, 0.8),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 12px rgba(0, 0, 0, 0.85);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .stick-btn-ctl::before {
    content: "";
    position: absolute;
    width: ${(widthDp * 0.70).toInt()}px;
    height: ${(heightDp * 0.70).toInt()}px;
    border-radius: 50%;
    border: 2px dashed rgba(255, 255, 255, 0.22);
    box-sizing: border-box;
  }
  .stick-btn-label {
    font-size: 22px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.9);
    z-index: 5;
  }
  .stick-btn-ctl:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="stick-btn-ctl" data-control="$control" data-category="BUTTON" data-name="Stick Button $control">
    <span class="stick-btn-label">$control</span>
  </button>
</body>
</html>
""".trimIndent()
        }

        val catType = CategoryType.fromIdentifier(category)
            ?: ControlKey.fromIdentifier(control)?.categoryType
            ?: CategoryManager.findCategoryForControl(control)?.type

        return when (catType) {
            CategoryType.STICKS -> """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.70;
    --spring-stiffness: 420;
    --press-scale: 0.94;
    --glow: ${if (control.uppercase() == "RS") "#FF007F" else "#00E5FF"};
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  .stick-btn {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    background: transparent;
    border: none;
    padding: 0;
    outline: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  /* Stationary Gimbal Base (remains at 0, 0) */
  .stick-base {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: radial-gradient(circle at 48% 42%, #262c38 0%, #12161f 65%, #06080b 100%);
    border: 3px solid #323a4a;
    box-shadow: 
      0 0 24px rgba(0, 229, 255, 0.18),
      0 14px 32px rgba(0, 0, 0, 0.88),
      inset 0 3px 6px rgba(255, 255, 255, 0.28),
      inset 0 -8px 18px rgba(0, 0, 0, 0.92);
  }
  /* Movable Analog Thumb Cap (translates on thumb drag) */
  .stick-cap {
    position: absolute;
    left: ${(widthDp * 0.17).toInt()}px;
    top: ${(heightDp * 0.17).toInt()}px;
    width: ${(widthDp * 0.66).toInt()}px;
    height: ${(heightDp * 0.66).toInt()}px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 45%, #222834 0%, #10141b 70%, #08090d 100%);
    border: 2.5px solid #3a4558;
    box-shadow: 
      0 4px 12px rgba(0, 0, 0, 0.70),
      inset 0 0 12px rgba(0, 0, 0, 0.95),
      inset 0 2px 4px rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }
  /* Concave Grip Ring Knurling */
  .stick-cap::before {
    content: "";
    position: absolute;
    width: ${(widthDp * 0.46).toInt()}px;
    height: ${(heightDp * 0.46).toInt()}px;
    border-radius: 50%;
    border: 2px dashed rgba(255, 255, 255, 0.28);
    box-sizing: border-box;
    pointer-events: none;
  }
  /* Top Specular Crescent Sheen */
  .stick-cap::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 20%;
    width: 60%;
    height: 32%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 20%, rgba(255, 255, 255, 0.42) 0%, transparent 75%);
    pointer-events: none;
  }
  .stick-label {
    font-size: 22px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 14px var(--glow), 0 2px 6px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .stick-btn:active .stick-cap {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-name="Analog Stick $control">
    <div class="stick-base"></div>
    <div class="stick-cap">
      <!-- Movable thumb cap layers: 360° analog stick navigation with NO center button -->
      <span class="stick-label">$control</span>
    </div>
  </button>
</body>
</html>
            """.trimIndent()

            CategoryType.TRIGGERS -> """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.95;
    --glow: ${if (control.uppercase() == "RT") "#E055B8" else "#00E5FF"};
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  .trigger-btn {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    border-radius: 20px 20px 32px 32px;
    background: linear-gradient(180deg, #282f3c 0%, #151922 60%, #090c10 100%);
    border: 2px solid #353f50;
    box-shadow: 
      0 0 24px rgba(0, 229, 255, 0.20),
      0 14px 30px rgba(0, 0, 0, 0.85),
      inset 0 3px 6px rgba(255, 255, 255, 0.32),
      inset 0 -8px 18px rgba(0, 0, 0, 0.92);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 18px;
    cursor: pointer;
    outline: none;
    overflow: hidden;
    transform-origin: 50% 50%;
  }
  /* Tactile Index Finger Friction Ridge */
  .trigger-btn::after {
    content: "";
    position: absolute;
    bottom: 14%;
    width: 64%;
    height: 5px;
    border-radius: 3px;
    background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.35) 50%, transparent 100%);
    box-shadow: 0 0 8px var(--glow);
  }
  /* Top Specular Crest Reflection */
  .trigger-btn::before {
    content: "";
    position: absolute;
    top: 4px;
    left: 15%;
    width: 70%;
    height: 16px;
    border-radius: 10px;
    background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.40) 0%, transparent 80%);
    pointer-events: none;
  }
  .trigger-label {
    font-size: 30px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 14px var(--glow), 0 2px 6px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .trigger-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-name="Trigger $control">
    <span class="trigger-label">$control</span>
  </button>
</body>
</html>
            """.trimIndent()

            CategoryType.BUMPERS -> """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.75;
    --spring-stiffness: 520;
    --press-scale: 0.96;
    --glow: ${if (control.uppercase() == "LB") "#A97CF0" else "#00E5FF"};
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  .bumper-btn {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    border-radius: ${if (control.uppercase() == "LB") "12px 28px 14px 12px" else if (control.uppercase() == "RB") "28px 12px 12px 14px" else "20px"};
    background: linear-gradient(180deg, #282e3b 0%, #151922 65%, #090c10 100%);
    border: 2px solid #363e4f;
    box-shadow: 
      0 0 22px rgba(169, 124, 240, 0.22),
      0 12px 26px rgba(0, 0, 0, 0.80),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.32),
      inset 0 -6px 14px rgba(0, 0, 0, 0.90);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    outline: none;
    overflow: hidden;
  }
  /* Tactile Microswitch Click Seam Accent */
  .bumper-btn::after {
    content: "";
    position: absolute;
    bottom: 0;
    left: 15%;
    width: 70%;
    height: 3px;
    border-radius: 2px 2px 0 0;
    background: linear-gradient(90deg, transparent 0%, var(--glow) 50%, transparent 100%);
    opacity: 0.65;
  }
  /* Top Specular Sheen Arc */
  .bumper-btn::before {
    content: "";
    position: absolute;
    top: 2px;
    left: 8%;
    width: 84%;
    height: 14px;
    border-radius: 10px;
    background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.35) 0%, transparent 80%);
    pointer-events: none;
  }
  .bumper-label {
    font-size: 24px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 12px var(--glow), 0 2px 5px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .bumper-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-name="Bumper $control">
    <span class="bumper-label">$control</span>
  </button>
</body>
</html>
            """.trimIndent()

            CategoryType.DPAD -> if (control.uppercase() == ControlKey.DPAD.key) {
                """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.68;
    --spring-stiffness: 460;
    --press-scale: 0.95;
    --accent: #00F0FF;
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  .dpad-cross {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #161b24 0%, #0d1017 65%, #05070a 100%);
    border: 2px solid #2a3342;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.85),
      0 0 0 3px rgba(10, 13, 18, 0.95),
      inset 0 6px 14px rgba(0, 0, 0, 0.95),
      inset 0 -2px 5px rgba(255, 255, 255, 0.08);
    display: block;
    cursor: pointer;
    outline: none;
    padding: 0;
    transform-origin: 50% 50%;
  }
  .dpad-cross > * { position: absolute; }
  .dpad-socket-trim {
    inset: 4px;
    border-radius: 50%;
    border: 1.5px dashed rgba(0, 240, 255, 0.3);
    pointer-events: none;
  }
  .dpad-arm-v {
    left: ${(widthDp * 0.335).toInt()}px;
    top: ${(heightDp * 0.06).toInt()}px;
    width: ${(widthDp * 0.33).toInt()}px;
    height: ${(heightDp * 0.88).toInt()}px;
    border-radius: 10px;
    background: linear-gradient(180deg, #2c3545 0%, #1a202a 35%, #12161e 65%, #222a38 100%);
    border: 1.5px solid rgba(255, 255, 255, 0.18);
    box-shadow:
      0 6px 14px rgba(0, 0, 0, 0.75),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -4px 8px rgba(0, 0, 0, 0.85);
  }
  .dpad-arm-h {
    left: ${(widthDp * 0.06).toInt()}px;
    top: ${(heightDp * 0.335).toInt()}px;
    width: ${(widthDp * 0.88).toInt()}px;
    height: ${(heightDp * 0.33).toInt()}px;
    border-radius: 10px;
    background: linear-gradient(90deg, #2c3545 0%, #1a202a 35%, #12161e 65%, #222a38 100%);
    border: 1.5px solid rgba(255, 255, 255, 0.18);
    box-shadow:
      0 6px 14px rgba(0, 0, 0, 0.75),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -4px 8px rgba(0, 0, 0, 0.85);
  }
  .dpad-arm {
    box-sizing: border-box;
    z-index: 3;
  }
  .dpad-arm-top {
    top: ${(heightDp * 0.07).toInt()}px;
    left: ${(widthDp * 0.35).toInt()}px;
    width: ${(widthDp * 0.30).toInt()}px;
    height: ${(heightDp * 0.27).toInt()}px;
    border-radius: 8px 8px 3px 3px;
    background: linear-gradient(180deg, #384357 0%, #1c222c 100%);
    box-shadow: inset 0 2px 3px rgba(255, 255, 255, 0.3), inset 0 -2px 4px rgba(0, 0, 0, 0.6);
  }
  .dpad-arm-bottom {
    bottom: ${(heightDp * 0.07).toInt()}px;
    left: ${(widthDp * 0.35).toInt()}px;
    width: ${(widthDp * 0.30).toInt()}px;
    height: ${(heightDp * 0.27).toInt()}px;
    border-radius: 3px 3px 8px 8px;
    background: linear-gradient(0deg, #384357 0%, #1c222c 100%);
    box-shadow: inset 0 -2px 3px rgba(255, 255, 255, 0.2), inset 0 2px 4px rgba(0, 0, 0, 0.6);
  }
  .dpad-arm-left {
    left: ${(widthDp * 0.07).toInt()}px;
    top: ${(heightDp * 0.35).toInt()}px;
    width: ${(widthDp * 0.27).toInt()}px;
    height: ${(heightDp * 0.30).toInt()}px;
    border-radius: 8px 3px 3px 8px;
    background: linear-gradient(90deg, #384357 0%, #1c222c 100%);
    box-shadow: inset 2px 0 3px rgba(255, 255, 255, 0.3), inset -2px 0 4px rgba(0, 0, 0, 0.6);
  }
  .dpad-arm-right {
    right: ${(widthDp * 0.07).toInt()}px;
    top: ${(heightDp * 0.35).toInt()}px;
    width: ${(widthDp * 0.27).toInt()}px;
    height: ${(heightDp * 0.30).toInt()}px;
    border-radius: 3px 8px 8px 3px;
    background: linear-gradient(270deg, #384357 0%, #1c222c 100%);
    box-shadow: inset -2px 0 3px rgba(255, 255, 255, 0.3), inset 2px 0 4px rgba(0, 0, 0, 0.6);
  }
  .dpad-pivot {
    position: absolute;
    width: ${(widthDp * 0.32).toInt()}px;
    height: ${(heightDp * 0.32).toInt()}px;
    left: ${(widthDp * 0.34).toInt()}px;
    top: ${(heightDp * 0.34).toInt()}px;
    border-radius: 50%;
    background: radial-gradient(circle at 45% 45%, #080a0e 0%, #141923 60%, #1e2531 100%);
    border: 1.5px solid rgba(0, 240, 255, 0.45);
    box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.95), 0 1px 2px rgba(255, 255, 255, 0.2);
    z-index: 4;
  }
  .dpad-arrow {
    position: absolute;
    font-size: 18px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0, 0, 0, 0.95);
    z-index: 5;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    user-select: none;
  }
  .dpad-up    { top: ${(heightDp * 0.10).toInt()}px; left: ${(widthDp * 0.40).toInt()}px; }
  .dpad-down  { bottom: ${(heightDp * 0.10).toInt()}px; left: ${(widthDp * 0.40).toInt()}px; }
  .dpad-left  { left: ${(widthDp * 0.10).toInt()}px; top: ${(heightDp * 0.40).toInt()}px; }
  .dpad-right { right: ${(widthDp * 0.10).toInt()}px; top: ${(heightDp * 0.40).toInt()}px; }
  .dpad-cross:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="dpad-btn dpad-cross" data-control="DPAD" data-category="DPAD" data-name="D-Pad $control">
    <div class="dpad-socket-trim"></div>
    <div class="dpad-arm-v"></div>
    <div class="dpad-arm-h"></div>
    <div class="dpad-arm dpad-arm-top"></div>
    <div class="dpad-arm dpad-arm-bottom"></div>
    <div class="dpad-arm dpad-arm-left"></div>
    <div class="dpad-arm dpad-arm-right"></div>
    <div class="dpad-pivot"></div>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
</body>
</html>
                """.trimIndent()
            } else {
                """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.68;
    --spring-stiffness: 460;
    --press-scale: 0.94;
    --glow: #00E5FF;
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  .dpad-btn {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    border-radius: 20px;
    background: radial-gradient(circle at 45% 40%, #242935 0%, #12161f 65%, #080a0e 100%);
    border: 2px solid #323b4c;
    box-shadow: 
      0 0 20px rgba(0, 229, 255, 0.20),
      0 10px 24px rgba(0, 0, 0, 0.85),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.30),
      inset 0 -6px 14px rgba(0, 0, 0, 0.90);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    outline: none;
    overflow: hidden;
    transform-origin: 50% 50%;
  }
  /* Directional Inward Slope Reflection */
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 15%;
    width: 70%;
    height: 34%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 20%, rgba(255, 255, 255, 0.40) 0%, transparent 75%);
    pointer-events: none;
  }
  .dpad-glyph {
    font-size: 30px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 14px var(--glow), 0 2px 5px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-name="D-Pad $control">
    <span class="dpad-glyph">${dpadGlyph(control)}</span>
  </button>
</body>
</html>
                """.trimIndent()
            }

            CategoryType.SYSTEM, CategoryType.MACROS -> """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.78;
    --spring-stiffness: 500;
    --press-scale: 0.94;
    --glow: #00E5FF;
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  .system-btn {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    border-radius: 20px;
    background: radial-gradient(circle at 48% 40%, #262c38 0%, #131720 65%, #080a0e 100%);
    border: 2px solid #323b4c;
    box-shadow: 
      0 0 18px rgba(0, 229, 255, 0.18),
      0 10px 24px rgba(0, 0, 0, 0.80),
      inset 0 2px 4px rgba(255, 255, 255, 0.28),
      inset 0 -6px 12px rgba(0, 0, 0, 0.90);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    outline: none;
    overflow: hidden;
  }
  /* Top Optical Crescent Reflection */
  .system-btn::after {
    content: "";
    position: absolute;
    top: 4%;
    left: 15%;
    width: 70%;
    height: 32%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 20%, rgba(255, 255, 255, 0.38) 0%, transparent 75%);
    pointer-events: none;
  }
  .system-label {
    font-size: 20px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 12px var(--glow), 0 2px 5px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .system-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="system-btn" data-control="$control" data-category="${if (catType == CategoryType.MACROS) "MACROS" else "SYSTEM"}" data-name="System $control">
    <span class="system-label">$control</span>
  </button>
</body>
</html>
            """.trimIndent()

            CategoryType.ABXY, null -> """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.94;
    --glow: ${
        when (control.uppercase()) {
            "A" -> "#4ADE80"
            "B" -> "#FF3366"
            "X" -> "#00B0FF"
            "Y" -> "#FFCC00"
            else -> "#00E5FF"
        }
    };
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  .nexpad-btn {
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    border-radius: 50%;
    background: radial-gradient(circle at 46% 40%, #282f3c 0%, #141720 65%, #080a0e 100%);
    border: 2.5px solid #343f52;
    box-shadow: 
      0 0 24px rgba(0, 229, 255, 0.22),
      0 12px 28px rgba(0, 0, 0, 0.85),
      inset 0 2.5px 5px rgba(255, 255, 255, 0.35),
      inset 0 -7px 15px rgba(0, 0, 0, 0.94);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    outline: none;
    overflow: hidden;
  }
  /* Optical Glass Crescent Highlight */
  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 18%;
    width: 64%;
    height: 36%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 20%, rgba(255, 255, 255, 0.55) 0%, transparent 75%);
    transform: rotate(-8deg);
    pointer-events: none;
  }
  /* Subtle Internal Luminous Accent Trim */
  .nexpad-btn::before {
    content: "";
    position: absolute;
    inset: 4px;
    border-radius: 50%;
    border: 1px solid rgba(255, 255, 255, 0.12);
    box-shadow: inset 0 0 8px var(--glow);
    opacity: 0.50;
    pointer-events: none;
  }
  .btn-emblem {
    width: ${(widthDp * 0.58).toInt()}px;
    height: ${(heightDp * 0.58).toInt()}px;
    position: absolute;
    pointer-events: none;
  }
  .btn-label {
    font-size: 34px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #FFFFFF;
    text-shadow: 0 0 16px var(--glow), 0 2px 6px rgba(0, 0, 0, 0.95);
    z-index: 5;
  }
  .nexpad-btn:active {
    transform: scale(var(--press-scale));
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-name="Action $control">
    <span class="btn-label">$control</span>
  </button>
</body>
</html>
            """.trimIndent()
        }
    }
}
