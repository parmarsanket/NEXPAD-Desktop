package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey

private fun dpadGlyph(control: String): String = when (control.uppercase()) {
    ControlKey.DOWN.key  -> "▼"
    ControlKey.LEFT.key  -> "◀"
    ControlKey.RIGHT.key -> "▶"
    ControlKey.DPAD.key  -> "❖"
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
    --core-glow: rgba(0, 255, 163, 0.6);
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
      radial-gradient(circle at 50% 50%, #06120c 0%, #020604 70%, #000000 100%);
    border: 2px solid #1a3828;
    box-shadow:
      0 10px 24px rgba(0, 0, 0, 0.8),
      0 0 0 3px #0a1610,
      0 0 20px var(--core-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.3),
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
    border: 1.5px dashed rgba(0, 255, 163, 0.45);
    background: radial-gradient(circle at 35% 25%, rgba(0, 255, 163, 0.15) 0%, transparent 60%);
  }
  .button-a::after {
    content: "";
    position: absolute;
    top: 8%;
    left: 16%;
    width: 68%;
    height: 36%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.75) 0%, transparent 75%);
    transform: rotate(-12deg);
  }
  .button-a .reactor-core {
    width: 58px;
    height: 58px;
    border-radius: 50%;
    background: radial-gradient(circle at 35% 30%, #15803d 0%, #064e3b 50%, #022c22 100%);
    border: 1.5px solid #22c55e;
    box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.4), inset 0 -4px 8px rgba(0, 0, 0, 0.85), 0 0 14px var(--core-glow);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .button-a span {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 10px #00FFA3, 0 2px 4px rgba(0, 0, 0, 0.95), 0 1px 0 rgba(255, 255, 255, 0.8);
    z-index: 5;
  }
  .button-a:active {
    transform: scale(0.93) translateY(2px);
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
    --accent-glow: rgba(255, 0, 85, 0.55);
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
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.8), inset 0 2px 5px rgba(255, 255, 255, 0.4), inset 0 -6px 12px rgba(0, 0, 0, 0.9), 0 0 22px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .crimson-octa::before {
    content: "";
    position: absolute;
    inset: 5px;
    clip-path: polygon(30% 0%, 70% 0%, 100% 30%, 100% 70%, 70% 100%, 30% 100%, 0% 70%, 0% 30%);
    border: 1.5px solid rgba(255, 255, 255, 0.25);
  }
  .crimson-octa::after {
    content: "";
    position: absolute;
    top: 6px;
    left: 22px;
    width: 48px;
    height: 18px;
    border-radius: 9px;
    background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.6) 0%, transparent 80%);
  }
  .octa-label {
    font-size: 34px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 12px #FF0055, 0 2px 4px rgba(0, 0, 0, 0.95), 0 1px 0 rgba(255, 255, 255, 0.8);
    z-index: 5;
  }
  .crimson-octa:active {
    transform: scale(0.92) translateY(2px);
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
    --accent-glow: rgba(255, 184, 0, 0.55);
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
      radial-gradient(circle at 35% 25%, #382e18 0%, #171206 65%, #080602 100%);
    border: 2px solid #5a4722;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.8), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -6px 12px rgba(0, 0, 0, 0.9), 0 0 20px var(--accent-glow);
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
    border: 1px dashed rgba(255, 184, 0, 0.35);
  }
  .turbo-label {
    font-size: 30px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 10px #FFB800, 0 2px 4px rgba(0, 0, 0, 0.95), 0 1px 0 rgba(255, 255, 255, 0.8);
    z-index: 5;
  }
  .turbo-sub {
    font-size: 9px;
    font-weight: 900;
    letter-spacing: 1.5px;
    color: var(--accent);
    text-shadow: 0 0 6px var(--accent);
    z-index: 5;
  }
  .speed-turbo:active {
    transform: scale(0.91) translateY(2px);
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
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.28) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #0d1a13 0%, #06100a 65%, #020503 100%);
    border: 2px solid #1c3d2a;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.8),
      0 0 0 3px rgba(18, 40, 26, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.32),
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
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.2) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #173322, #326343, #122418, #478c5e, #173322, #326343, #122418);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.8);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.85) 0%, rgba(255, 255, 255, 0.2) 42%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1.5px solid rgba(255, 255, 255, 0.55);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.55) 0%, transparent 35%),
      radial-gradient(circle at 68% 75%, rgba(0, 0, 0, 0.55) 0%, transparent 50%),
      linear-gradient(145deg, #10b981 0%, #059669 35%, #047857 70%, #064e3b 100%);
    border: 1.5px solid rgba(0, 255, 163, 0.75);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.75);
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
    transform: scale(0.93) translateY(3px);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Neo Tactile A">
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
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.28) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #220b12 0%, #120408 65%, #050102 100%);
    border: 2px solid #4a1524;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.8),
      0 0 0 3px rgba(60, 15, 25, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.32),
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
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.2) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #3b101c, #63182c, #260810, #8c2944, #3b101c, #63182c, #260810);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.8);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.85) 0%, rgba(255, 255, 255, 0.2) 42%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1.5px solid rgba(255, 255, 255, 0.55);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.55) 0%, transparent 35%),
      radial-gradient(circle at 68% 75%, rgba(0, 0, 0, 0.55) 0%, transparent 50%),
      linear-gradient(145deg, #f43f5e 0%, #e11d48 35%, #be123c 70%, #881337 100%);
    border: 1.5px solid rgba(255, 42, 109, 0.75);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.75);
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
    transform: scale(0.93) translateY(3px);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="B" data-category="BUTTON" data-name="Neo Tactile B">
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
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.28) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #0b1a24 0%, #050f16 65%, #010406 100%);
    border: 2px solid #19384d;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.8),
      0 0 0 3px rgba(15, 38, 55, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.32),
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
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.2) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #102638, #1c4b6e, #0c1c2b, #256a9e, #102638, #1c4b6e, #0c1c2b);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.8);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.85) 0%, rgba(255, 255, 255, 0.2) 42%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1.5px solid rgba(255, 255, 255, 0.55);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.55) 0%, transparent 35%),
      radial-gradient(circle at 68% 75%, rgba(0, 0, 0, 0.55) 0%, transparent 50%),
      linear-gradient(145deg, #06b6d4 0%, #0284c7 35%, #0369a1 70%, #0c4a6e 100%);
    border: 1.5px solid rgba(0, 229, 255, 0.75);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.75);
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
    transform: scale(0.93) translateY(3px);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="X" data-category="BUTTON" data-name="Neo Tactile X">
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
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.28) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.7) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #241c09 0%, #140e03 65%, #050301 100%);
    border: 2px solid #523f14;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.8),
      0 0 0 3px rgba(60, 45, 12, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.32),
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
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.2) 0%, transparent 45%),
      conic-gradient(from 180deg at 50% 50%, #362a0c, #695116, #241a05, #94721c, #362a0c, #695116, #241a05);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.8);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 14%;
    width: 72%;
    height: 40%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 25%, rgba(255, 255, 255, 0.85) 0%, rgba(255, 255, 255, 0.2) 42%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1.5px solid rgba(255, 255, 255, 0.55);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.55) 0%, transparent 35%),
      radial-gradient(circle at 68% 75%, rgba(0, 0, 0, 0.55) 0%, transparent 50%),
      linear-gradient(145deg, #f59e0b 0%, #d97706 35%, #b45309 70%, #78350f 100%);
    border: 1.5px solid rgba(255, 214, 0, 0.75);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.75);
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
    transform: scale(0.93) translateY(3px);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="Y" data-category="BUTTON" data-name="Neo Tactile Y">
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
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.92;
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 50% 20%, rgba(0, 240, 255, 0.22) 0%, transparent 55%),
      linear-gradient(180deg, #262d3a 0%, #151821 60%, #080a0e 100%);
    border: 2px solid #384254;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -5px 10px rgba(0, 0, 0, 0.85), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    top: 8px;
    left: 18px;
    width: 40px;
    height: 3px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 8%;
    left: 14%;
    width: 72%;
    height: 36%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.5) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.95), 0 -1px 0 rgba(255,255,255,0.7);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(0.92) translateY(2px);
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
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.92;
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 50% 80%, rgba(0, 240, 255, 0.22) 0%, transparent 55%),
      linear-gradient(0deg, #262d3a 0%, #151821 60%, #080a0e 100%);
    border: 2px solid #384254;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -5px 10px rgba(0, 0, 0, 0.85), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    bottom: 8px;
    left: 18px;
    width: 40px;
    height: 3px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    bottom: 8%;
    left: 14%;
    width: 72%;
    height: 36%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 70%, rgba(255, 255, 255, 0.5) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.95), 0 -1px 0 rgba(255,255,255,0.7);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(0.92) translateY(2px);
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
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.92;
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 20% 50%, rgba(0, 240, 255, 0.22) 0%, transparent 55%),
      linear-gradient(90deg, #262d3a 0%, #151821 60%, #080a0e 100%);
    border: 2px solid #384254;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -5px 10px rgba(0, 0, 0, 0.85), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    left: 8px;
    top: 18px;
    width: 3px;
    height: 40px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 14%;
    left: 8%;
    width: 36%;
    height: 72%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 30% 50%, rgba(255, 255, 255, 0.5) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.95), 0 -1px 0 rgba(255,255,255,0.7);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(0.92) translateY(2px);
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
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.92;
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: 
      radial-gradient(circle at 80% 50%, rgba(0, 240, 255, 0.22) 0%, transparent 55%),
      linear-gradient(270deg, #262d3a 0%, #151821 60%, #080a0e 100%);
    border: 2px solid #384254;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -5px 10px rgba(0, 0, 0, 0.85), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    right: 8px;
    top: 18px;
    width: 3px;
    height: 40px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 14%;
    right: 8%;
    width: 36%;
    height: 72%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 70% 50%, rgba(255, 255, 255, 0.5) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.95), 0 -1px 0 rgba(255,255,255,0.7);
    z-index: 5;
  }
  .dpad-btn:active {
    transform: scale(0.92) translateY(2px);
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
    --cross-size: 144px;
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.4);
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.95;
  }
  .dpad-cross {
    width: var(--cross-size);
    height: var(--cross-size);
    border-radius: 28px;
    background: 
      radial-gradient(circle at 50% 50%, #1e2430 0%, #101319 65%, #06070a 100%);
    border: 2px solid #343d4e;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.8), 
      inset 0 2px 5px rgba(255, 255, 255, 0.28), 
      inset 0 -6px 14px rgba(0, 0, 0, 0.9), 
      0 0 24px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-cross::before {
    content: "";
    position: absolute;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: radial-gradient(circle at 45% 45%, #262e3d 0%, #0b0d12 100%);
    box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.95), 0 1px 2px rgba(255, 255, 255, 0.25);
    border: 1.5px solid rgba(0, 240, 255, 0.35);
  }
  .dpad-cross::after {
    content: "";
    position: absolute;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    border: 1.5px dashed rgba(0, 240, 255, 0.35);
  }
  .cross-center {
    font-size: 22px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.9);
    z-index: 5;
  }
  .dpad-cross:active {
    transform: scale(0.95);
  }
</style>
</head>
<body>
  <button class="dpad-btn dpad-cross" data-control="DPAD" data-category="DPAD" data-name="Tactile Cross Pad">
    <span class="cross-center">❖</span>
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
    --spring-damping: 0.65;
    --spring-stiffness: 380;
    --press-scale: 0.94;
  }
  .trigger-btn {
    width: 74px;
    height: 112px;
    border-radius: 20px;
    background: linear-gradient(180deg, #262d3a 0%, #141720 45%, #080a0e 100%);
    border: 2px solid #384254;
    box-shadow: 0 12px 26px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.32), inset 0 -8px 16px rgba(0, 0, 0, 0.85), 0 0 18px var(--accent-glow);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 18px;
    box-sizing: border-box;
    position: relative;
  }
  .trigger-btn::before {
    content: "";
    position: absolute;
    top: 55%;
    width: 44px;
    height: 4px;
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.2);
    box-shadow: 0 8px 0 rgba(255, 255, 255, 0.14), 0 16px 0 rgba(255, 255, 255, 0.08);
  }
  .trigger-label {
    font-size: 28px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 2px 4px rgba(0,0,0,0.9), 0 0 12px var(--accent);
  }
  .trigger-btn:active {
    transform: scaleY(0.94) translateY(4px);
  }
</style>
</head>
<body>
  <button class="trigger-btn" data-control="LT" data-category="TRIGGER" data-name="Tactile Trigger LT">
    <span class="trigger-label">LT</span>
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
    --spring-damping: 0.65;
    --spring-stiffness: 380;
    --press-scale: 0.94;
  }
  .trigger-btn {
    width: 74px;
    height: 112px;
    border-radius: 20px;
    background: linear-gradient(180deg, #262d3a 0%, #141720 45%, #080a0e 100%);
    border: 2px solid #384254;
    box-shadow: 0 12px 26px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.32), inset 0 -8px 16px rgba(0, 0, 0, 0.85), 0 0 18px var(--accent-glow);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 18px;
    box-sizing: border-box;
    position: relative;
  }
  .trigger-btn::before {
    content: "";
    position: absolute;
    top: 55%;
    width: 44px;
    height: 4px;
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.2);
    box-shadow: 0 8px 0 rgba(255, 255, 255, 0.14), 0 16px 0 rgba(255, 255, 255, 0.08);
  }
  .trigger-label {
    font-size: 28px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 2px 4px rgba(0,0,0,0.9), 0 0 12px var(--accent);
  }
  .trigger-btn:active {
    transform: scaleY(0.94) translateY(4px);
  }
</style>
</head>
<body>
  <button class="trigger-btn" data-control="RT" data-category="TRIGGER" data-name="Tactile Trigger RT">
    <span class="trigger-label">RT</span>
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
    --accent-glow: rgba(0, 240, 255, 0.4);
    --spring-damping: 0.75;
    --spring-stiffness: 520;
    --press-scale: 0.95;
  }
  .bumper-btn {
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background: linear-gradient(180deg, #262e3c 0%, #151822 60%, #080a0e 100%);
    border: 2px solid #384357;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.7), inset 0 2px 4px rgba(255, 255, 255, 0.32), inset 0 -5px 10px rgba(0, 0, 0, 0.8), 0 0 18px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .bumper-btn::after {
    content: "";
    position: absolute;
    top: 10%;
    left: 12%;
    width: 76%;
    height: 35%;
    border-radius: 10px;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.45) 0%, transparent 75%);
  }
  .bumper-label {
    font-size: 24px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7), 0 -1px 0 rgba(0, 0, 0, 0.95), 0 0 12px var(--accent);
  }
  .bumper-btn:active {
    transform: scale(0.95) translateY(2px);
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
    --accent-glow: rgba(0, 240, 255, 0.4);
    --spring-damping: 0.75;
    --spring-stiffness: 520;
    --press-scale: 0.95;
  }
  .bumper-btn {
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background: linear-gradient(180deg, #262e3c 0%, #151822 60%, #080a0e 100%);
    border: 2px solid #384357;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.7), inset 0 2px 4px rgba(255, 255, 255, 0.32), inset 0 -5px 10px rgba(0, 0, 0, 0.8), 0 0 18px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .bumper-btn::after {
    content: "";
    position: absolute;
    top: 10%;
    left: 12%;
    width: 76%;
    height: 35%;
    border-radius: 10px;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.45) 0%, transparent 75%);
  }
  .bumper-label {
    font-size: 24px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7), 0 -1px 0 rgba(0, 0, 0, 0.95), 0 0 12px var(--accent);
  }
  .bumper-btn:active {
    transform: scale(0.95) translateY(2px);
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
    --accent: #4ADE80;
    --accent-glow: rgba(74, 222, 128, 0.3);
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
    background: radial-gradient(circle at 45% 40%, #262d3a 0%, #12151d 65%, #06070a 100%);
    border: 3px solid #384355;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.75), inset 0 3px 6px rgba(255, 255, 255, 0.28), inset 0 -8px 16px rgba(0, 0, 0, 0.85), 0 0 20px var(--accent-glow);
    box-sizing: border-box;
  }
  .stick-cap {
    position: absolute;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #181c24 0%, #0a0c10 100%);
    box-shadow: inset 0 0 10px rgba(0,0,0,0.95), 0 0 0 2px rgba(255, 255, 255, 0.14);
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
    border: 2px dashed rgba(74, 222, 128, 0.55);
    box-sizing: border-box;
  }
  .stick-label {
    font-size: 20px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
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
    --accent: #00B0FF;
    --accent-glow: rgba(0, 176, 255, 0.4);
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
    background: radial-gradient(circle at 45% 40%, #262d3a 0%, #12151d 65%, #06070a 100%);
    border: 3px solid #384355;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.75), inset 0 3px 6px rgba(255, 255, 255, 0.28), inset 0 -8px 16px rgba(0, 0, 0, 0.85), 0 0 20px var(--accent-glow);
    box-sizing: border-box;
  }
  .stick-cap {
    position: absolute;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #181c24 0%, #0a0c10 100%);
    box-shadow: inset 0 0 10px rgba(0,0,0,0.95), 0 0 0 2px rgba(255, 255, 255, 0.14);
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
    border: 2px dashed rgba(0, 176, 255, 0.55);
    box-sizing: border-box;
  }
  .stick-label {
    font-size: 20px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
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
    --accent-glow: rgba(0, 229, 255, 0.45);
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.90;
  }
  .stick-btn-ctl {
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 35% 35%, #2a2f3b 0%, #101217 100%);
    border: 2px solid #384254;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.7), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -6px 12px rgba(0, 0, 0, 0.85), 0 0 18px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    box-sizing: border-box;
  }
  .stick-btn-ctl::before {
    content: "";
    position: absolute;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    border: 1.5px dashed rgba(0, 229, 255, 0.45);
    box-sizing: border-box;
  }
  .stick-btn-dish {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #20242e 0%, #0d0e12 100%);
    box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.95), 0 0 0 1px rgba(255, 255, 255, 0.18);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .stick-btn-label {
    font-size: 14px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
    letter-spacing: 0.5px;
  }
  .stick-btn-ctl:active {
    transform: scale(0.90) translateY(2px);
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
    --accent-glow: rgba(255, 0, 127, 0.45);
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.90;
  }
  .stick-btn-ctl {
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 35% 35%, #2a2f3b 0%, #101217 100%);
    border: 2px solid #384254;
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.7), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -6px 12px rgba(0, 0, 0, 0.85), 0 0 18px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    box-sizing: border-box;
  }
  .stick-btn-ctl::before {
    content: "";
    position: absolute;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    border: 1.5px dashed rgba(255, 0, 127, 0.45);
    box-sizing: border-box;
  }
  .stick-btn-dish {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #20242e 0%, #0d0e12 100%);
    box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.95), 0 0 0 1px rgba(255, 255, 255, 0.18);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .stick-btn-label {
    font-size: 14px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
    letter-spacing: 0.5px;
  }
  .stick-btn-ctl:active {
    transform: scale(0.90) translateY(2px);
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
    --accent-glow: rgba(0, 229, 255, 0.35);
  }
  .touchpad-ctl {
    width: var(--pad-size);
    height: var(--pad-size);
    border-radius: 26px;
    background: radial-gradient(circle at 40% 40%, #20232a 0%, #111317 60%, #07080a 100%);
    border: 2px solid #333a47;
    box-shadow: 0 12px 26px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.18), inset 0 -6px 14px rgba(0, 0, 0, 0.85), 0 0 18px var(--accent-glow);
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
    border: 1px solid rgba(255, 255, 255, 0.07);
    background: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.02) 0%, transparent 80%);
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.35);
    pointer-events: none;
  }
  .touchpad-title {
    font-size: 10px;
    font-weight: 900;
    color: var(--accent);
    letter-spacing: 1.2px;
    text-transform: uppercase;
    z-index: 2;
  }
  .touchpad-sub {
    font-size: 9px;
    color: rgba(255, 255, 255, 0.45);
    letter-spacing: 0.8px;
    z-index: 2;
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
    --accent-glow: rgba(255, 0, 127, 0.35);
  }
  .touchpad-ctl {
    width: var(--pad-size);
    height: var(--pad-size);
    border-radius: 26px;
    background: radial-gradient(circle at 40% 40%, #20232a 0%, #111317 60%, #07080a 100%);
    border: 2px solid #333a47;
    box-shadow: 0 12px 26px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.18), inset 0 -6px 14px rgba(0, 0, 0, 0.85), 0 0 18px var(--accent-glow);
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
    border: 1px solid rgba(255, 255, 255, 0.07);
    background: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.02) 0%, transparent 80%);
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.35);
    pointer-events: none;
  }
  .touchpad-title {
    font-size: 10px;
    font-weight: 900;
    color: var(--accent);
    letter-spacing: 1.2px;
    text-transform: uppercase;
    z-index: 2;
  }
  .touchpad-sub {
    font-size: 9px;
    color: rgba(255, 255, 255, 0.45);
    letter-spacing: 0.8px;
    z-index: 2;
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
    background: radial-gradient(circle at 50% 30%, #252b37 0%, #101217 100%);
    border: 1.5px solid #363e4f;
    box-shadow: 0 8px 18px rgba(0, 0, 0, 0.65), inset 0 1px 3px rgba(255, 255, 255, 0.32), inset 0 -3px 6px rgba(0, 0, 0, 0.8);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
  .burger-bar {
    width: 22px;
    height: 3px;
    border-radius: 1.5px;
    background: #FFFFFF;
    box-shadow: 0 1px 2px rgba(0,0,0,0.85), 0 0 6px rgba(255,255,255,0.45);
  }
  .system-btn:active {
    transform: scale(0.92) translateY(2px);
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
    background: radial-gradient(circle at 50% 30%, #252b37 0%, #101217 100%);
    border: 1.5px solid #363e4f;
    box-shadow: 0 8px 18px rgba(0, 0, 0, 0.65), inset 0 1px 3px rgba(255, 255, 255, 0.32), inset 0 -3px 6px rgba(0, 0, 0, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .view-icon {
    font-size: 22px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 10px rgba(0, 240, 255, 0.65), 0 1px 2px rgba(0,0,0,0.95);
  }
  .system-btn:active {
    transform: scale(0.92) translateY(2px);
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
    background: radial-gradient(circle at 50% 35%, #2a303f 0%, #12151d 70%, #050608 100%);
    border: 2px solid #455064;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.8), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -6px 12px rgba(0, 0, 0, 0.85), 0 0 22px rgba(255, 255, 255, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .home-symbol {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 14px rgba(255, 255, 255, 0.9), 0 0 22px rgba(0, 240, 255, 0.55), 0 2px 4px rgba(0,0,0,0.95);
  }
  .system-home-btn:active {
    transform: scale(0.93) translateY(2px);
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
        val catType = CategoryType.fromIdentifier(category)
            ?: ctrl?.categoryType
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
                CategoryType.SYSTEM, CategoryType.MACROS -> when (ctrl) {
                    ControlKey.BACK -> PRESET_SYSTEM_VIEW
                    ControlKey.GUIDE -> PRESET_SYSTEM_HOME
                    else -> PRESET_SYSTEM_MENU
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
  .touchpad-ctl {
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    border-radius: 26px;
    position: relative;
    box-sizing: border-box;
    /* Pure flat, stationary laptop trackpad surface (2.0x default ballistics). */
    /* Strictly NO center button, NO center dot, NO movable ring, and NO tap-to-click. */
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
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    position: relative;
    border-radius: 50%;
    /* Visually design the knurled rim, recessed thumb dish, and label here */
  }
  .stick-btn-ctl:active {
    transform: scale(0.90) translateY(2px);
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
    --press-scale: 0.92;
  }
  .stick-btn {
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    position: relative;
    background: transparent;
    border: none;
    padding: 0;
    outline: none;
  }
  /* Stationary Gimbal Base (remains at 0, 0) */
  .stick-base {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    /* Visually design the stationary socket, bezel, and directional tick markers here */
  }
  /* Movable Analog Thumb Cap (translates on thumb drag) */
  .stick-cap {
    position: absolute;
    left: ${'$'}{(widthDp * 0.18).toInt()}px;
    top: ${'$'}{(heightDp * 0.18).toInt()}px;
    width: ${'$'}{(widthDp * 0.64).toInt()}px;
    height: ${'$'}{(heightDp * 0.64).toInt()}px;
    /* Visually design the thumb dish, knurled traction rings, vector art, and label here */
  }
  .stick-btn:active .stick-cap {
    transform: scale(0.92);
  }
</style>
</head>
<body>
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-name="Analog Stick $control">
    <div class="stick-base">
      <!-- Stationary socket layers: bezel, well cavity, directional ticks -->
    </div>
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
    --spring-damping: 0.65;
    --spring-stiffness: 380;
    --press-scale: 0.94;
  }
  .trigger-btn {
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    position: relative;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 18px;
    /* Visually design the trigger paddle body, curvature gradient, bevels, and shadows here */
  }
  .trigger-label {
    font-size: 28px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    /* Visually design the label typography and embossed shadows here */
  }
  .trigger-btn:active {
    transform: scaleY(0.94) translateY(4px);
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
  }
  .bumper-btn {
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    position: relative;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    /* Visually design the shoulder lever rocker, curvature specular highlight, and socket recess here */
  }
  .bumper-label {
    font-size: 24px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    /* Visually design the bumper label typography and embossed shadows here */
  }
  .bumper-btn:active {
    transform: scale(0.96) translateY(2px);
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

            CategoryType.DPAD -> """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: ${'$'}{if (control.uppercase() == ControlKey.DPAD.key) "0.95" else "0.92"};
  }
  .dpad-btn {
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    position: relative;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    /* Visually design the directional pad geometry, rocker pivot well, and shading here */
  }
  .dpad-glyph {
    font-size: 28px;
    font-weight: 900;
    /* Visually design the directional indicator (or embedded SVG chevron/arrow) here */
  }
  .dpad-btn:active {
    transform: ${'$'}{if (control.uppercase() == ControlKey.DPAD.key) "scale(0.95)" else "scale(0.92) translateY(2px)"};
  }
</style>
</head>
<body>
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-name="D-Pad $control">
    <span class="dpad-glyph">${'$'}{dpadGlyph(control)}</span>
  </button>
</body>
</html>
            """.trimIndent()

            CategoryType.SYSTEM, CategoryType.MACROS -> """
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
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    position: relative;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    /* Visually design the low-profile utility switch body, socket bevels, and lighting here */
  }
  .system-btn:active {
    transform: scale(0.92) translateY(2px);
  }
</style>
</head>
<body>
  <button class="system-btn" data-control="$control" data-category="SYSTEM" data-name="System $control">
    <!-- Visually design vector iconography (e.g. flex hamburger bars, overlapping windows, or emblem) here -->
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
    --press-scale: 0.92;
  }
  .nexpad-btn {
    width: ${'$'}{widthDp}px;
    height: ${'$'}{heightDp}px;
    position: relative;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    /* Visually design the face button silhouette, physical material, depth, and socket recess here */
  }
  /* Optional: embedded SVG emblem for complex characters or icons */
  .btn-emblem {
    width: ${'$'}{(widthDp * 0.58).toInt()}px;
    height: ${'$'}{(heightDp * 0.58).toInt()}px;
    position: absolute;
    pointer-events: none;
  }
  .btn-label {
    font-size: 34px;
    font-weight: 900;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    /* Visually design the extruded 3D typography and text shadows here */
  }
  .nexpad-btn:active {
    transform: scale(0.93) translateY(3px);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-name="Action $control">
    <!-- If designing a character, hero emblem, or custom icon, embed an <svg class="btn-emblem" viewBox="0 0 100 100"><path d="..."/></svg> here -->
    <span class="btn-label">$control</span>
  </button>
</body>
</html>
            """.trimIndent()
        }
    }
}
