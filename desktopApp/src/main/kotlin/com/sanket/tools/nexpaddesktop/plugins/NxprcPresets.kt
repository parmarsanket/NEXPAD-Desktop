package com.sanket.tools.nexpaddesktop.plugins

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
        .button-a {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: radial-gradient(
                circle at 32% 24%,
                #d9ffd9 0%,
                #9be99b 18%,
                #52b952 48%,
                #238423 78%,
                #155c15 100%
            );
            border: 3px solid #292a30;
            box-shadow:
                0 2px 4px rgba(255,255,255,0.18) inset,
                0 -7px 14px rgba(0,0,0,0.45) inset,
                0 0 0 2px rgba(255,255,255,0.08),
                0 0 0 5px rgba(0,0,0,0.30),
                0 8px 14px rgba(0,0,0,0.45);
        }
        .button-a::before {
            content: "";
            position: absolute;
            width: 55%;
            height: 32%;
            top: 7%;
            left: 14%;
            border-radius: 50%;
            background: radial-gradient(ellipse at center, rgba(255,255,255,0.65) 0%, rgba(255,255,255,0.22) 40%, transparent 75%);
            transform: rotate(-18deg);
        }
        .button-a::after {
            content: "";
            position: absolute;
            inset: 5px;
            border-radius: 50%;
            box-shadow: inset 0 0 8px rgba(0,0,0,0.45), inset 0 2px 4px rgba(255,255,255,0.18);
        }
        .button-a span {
            font-size: 39px;
            font-weight: 900;
            color: #f5f5f5;
            text-shadow: 0 3px 2px rgba(0,0,0,0.45), 0 1px 0 rgba(255,255,255,0.7);
        }
        .button-a:active {
            transform: scale(0.94) translateY(2px);
        }
    </style>
</head>
<body>
    <button class="button-a" data-control="A" data-category="BUTTON" data-name="Cyber Reactor A"><span>A</span></button>
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
    --accent-glow: rgba(255, 0, 85, 0.6);
  }
  .crimson-octa {
    position: relative;
    width: 96px;
    height: 96px;
    clip-path: polygon(30% 0%, 70% 0%, 100% 30%, 100% 70%, 70% 100%, 30% 100%, 0% 70%, 0% 30%);
    background: radial-gradient(circle at 35% 30%, #ff1766 0%, #990033 50%, #20000a 100%);
    border: 2px solid var(--accent);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.7), 0 0 20px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .crimson-octa span {
    font-size: 38px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8), 0 0 12px var(--accent);
  }
  .crimson-octa:active {
    transform: scale(0.93) translateY(2px);
  }
</style>
</head>
<body>
  <button class="crimson-octa" data-control="B" data-category="BUTTON" data-name="Crimson Octagon">
    <span>B</span>
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
    --accent: #FFCC00;
    --accent-glow: rgba(255, 204, 0, 0.6);
  }
  .speed-turbo {
    position: relative;
    width: 96px;
    height: 96px;
    border-radius: 20px;
    background: linear-gradient(135deg, #ffdb4d 0%, #cc9900 45%, #2a2000 100%);
    border: 2px solid var(--accent);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.65), 0 0 20px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .speed-turbo span {
    font-size: 38px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8), 0 0 12px var(--accent);
  }
  .speed-turbo:active {
    transform: scale(0.93) translateY(2px);
  }
</style>
</head>
<body>
  <button class="speed-turbo" data-control="X" data-category="BUTTON" data-name="Speed Turbo X">
    <span>X</span>
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
    --accent-glow: rgba(74, 222, 128, 0.7);
    --accent-core: #4ade80;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.65) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #0e1c14 0%, #06100a 65%, #020603 100%);
    border: 2px solid #1c3d28;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.75),
      0 0 0 3px rgba(18, 40, 26, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
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
      linear-gradient(145deg, #34d399 0%, #10b981 35%, #059669 70%, #064e3b 100%);
    border: 1.5px solid rgba(74, 222, 128, 0.65);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.8),
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
    --accent-glow: rgba(255, 51, 102, 0.7);
    --accent-core: #ff3366;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.65) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #220c13 0%, #120509 65%, #050102 100%);
    border: 2px solid #4a1926;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.75),
      0 0 0 3px rgba(45, 14, 22, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
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
      conic-gradient(from 180deg at 50% 50%, #38161f, #70263a, #240e14, #99334e, #38161f, #70263a, #240e14);
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
      linear-gradient(145deg, #fb7185 0%, #f43f5e 35%, #e11d48 70%, #881337 100%);
    border: 1.5px solid rgba(255, 51, 102, 0.65);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.8),
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
    --accent-glow: rgba(0, 176, 255, 0.7);
    --accent-core: #00b0ff;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.65) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #0c1824 0%, #050d14 65%, #010406 100%);
    border: 2px solid #1a364f;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.75),
      0 0 0 3px rgba(14, 30, 48, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
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
      conic-gradient(from 180deg at 50% 50%, #14293d, #29547d, #0d1b29, #3d7cb8, #14293d, #29547d, #0d1b29);
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
      linear-gradient(145deg, #38bdf8 0%, #0ea5e9 35%, #0284c7 70%, #0c4a6e 100%);
    border: 1.5px solid rgba(0, 176, 255, 0.65);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.8),
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
    --accent-glow: rgba(255, 204, 0, 0.7);
    --accent-core: #ffcc00;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background:
      radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.25) 0%, transparent 40%),
      radial-gradient(circle at 68% 78%, rgba(0, 0, 0, 0.65) 0%, transparent 55%),
      radial-gradient(circle at 50% 50%, #201a08 0%, #110d04 65%, #040301 100%);
    border: 2px solid #473a14;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.75),
      0 0 0 3px rgba(45, 36, 11, 0.95),
      0 0 24px var(--accent-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.25),
      inset 0 -6px 14px rgba(0, 0, 0, 0.85);
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
      conic-gradient(from 180deg at 50% 50%, #3d3110, #7d6521, #29200b, #b89531, #3d3110, #7d6521, #29200b);
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
      linear-gradient(145deg, #fde047 0%, #eab308 35%, #ca8a04 70%, #713f12 100%);
    border: 1.5px solid rgba(255, 204, 0, 0.65);
    box-shadow: 
      inset 0 2px 5px rgba(255, 255, 255, 0.45),
      inset 0 -5px 10px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.95),
      0 -1px 0 rgba(0, 0, 0, 0.95),
      0 3px 8px rgba(0, 0, 0, 0.8),
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
    --dpad-size: 80px;
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
      radial-gradient(circle at 50% 20%, rgba(0, 240, 255, 0.18) 0%, transparent 55%),
      linear-gradient(180deg, #2a3140 0%, #161a22 60%, #0a0c10 100%);
    border: 2px solid #3d475c;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.65), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.75), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    top: 9px;
    left: 18px;
    width: 44px;
    height: 3px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
    opacity: 0.85;
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 8%;
    left: 14%;
    width: 72%;
    height: 36%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.45) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.9), 0 -1px 0 rgba(255,255,255,0.6);
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

    val PRESET_DPAD_CROSS = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --cross-size: 140px;
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.35);
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: 0.95;
  }
  .dpad-cross {
    width: var(--cross-size);
    height: var(--cross-size);
    border-radius: 28px;
    background: 
      radial-gradient(circle at 50% 50%, #222834 0%, #12151c 65%, #08090d 100%);
    border: 2px solid #363f52;
    box-shadow: 
      0 12px 28px rgba(0, 0, 0, 0.75), 
      inset 0 2px 5px rgba(255, 255, 255, 0.25), 
      inset 0 -6px 14px rgba(0, 0, 0, 0.85), 
      0 0 24px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-cross::before {
    content: "";
    position: absolute;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: radial-gradient(circle at 45% 45%, #2a3140 0%, #0d0f14 100%);
    box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.9), 0 1px 2px rgba(255, 255, 255, 0.2);
    border: 1.5px solid rgba(0, 240, 255, 0.3);
  }
  .dpad-cross::after {
    content: "";
    position: absolute;
    width: 96px;
    height: 96px;
    border-radius: 50%;
    border: 1.5px dashed rgba(0, 240, 255, 0.35);
  }
  .cross-center {
    font-size: 20px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 10px var(--accent), 0 2px 4px rgba(0,0,0,0.8);
    z-index: 5;
  }
  .dpad-cross:active {
    transform: scale(0.95);
  }
</style>
</head>
<body>
  <button class="dpad-cross" data-control="DPAD" data-category="DPAD" data-name="Tactile Cross Pad">
    <span class="cross-center">❖</span>
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
    width: 72px;
    height: 110px;
    border-radius: 20px;
    background: linear-gradient(180deg, #282f3d 0%, #161922 45%, #0a0c10 100%);
    border: 2px solid #3d4659;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.65), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 18px var(--accent-glow);
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
    background: rgba(255, 255, 255, 0.18);
    box-shadow: 0 8px 0 rgba(255, 255, 255, 0.12), 0 16px 0 rgba(255, 255, 255, 0.08);
  }
  .trigger-label {
    font-size: 28px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 2px 4px rgba(0,0,0,0.8), 0 0 12px var(--accent);
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

    val PRESET_BUMPER_RB = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.35);
    --spring-damping: 0.75;
    --spring-stiffness: 520;
    --press-scale: 0.96;
  }
  .bumper-btn {
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background: linear-gradient(180deg, #2c3342 0%, #171a23 60%, #0c0e13 100%);
    border: 2px solid #3d475c;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.7), 0 0 16px var(--accent-glow);
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
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7), 0 -1px 0 rgba(0, 0, 0, 0.9), 0 0 10px var(--accent);
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
    --stick-size: 100px;
    --accent: #4ADE80;
    --accent-glow: rgba(74, 222, 128, 0.25);
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
    background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%);
    border: 3px solid #3d4657;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7), inset 0 3px 6px rgba(255, 255, 255, 0.25), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 20px var(--accent-glow);
    box-sizing: border-box;
  }
  .stick-cap {
    position: absolute;
    width: 66px;
    height: 66px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #1a1e26 0%, #0d0f14 100%);
    box-shadow: inset 0 0 10px rgba(0,0,0,0.9), 0 0 0 2px rgba(255, 255, 255, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .knurled-ring {
    position: absolute;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 2px dashed rgba(74, 222, 128, 0.5);
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
      <span class="stick-label">L3</span>
    </div>
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
    height: 44px;
    border-radius: 14px;
    background: radial-gradient(circle at 50% 30%, #29303e 0%, #12151d 100%);
    border: 1.5px solid #3d475c;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.6), inset 0 1px 3px rgba(255, 255, 255, 0.3), inset 0 -3px 6px rgba(0, 0, 0, 0.75);
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
    box-shadow: 0 1px 2px rgba(0,0,0,0.8), 0 0 4px rgba(255,255,255,0.4);
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

    val PRESET_DPAD_DOWN = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --dpad-size: 80px;
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
      radial-gradient(circle at 50% 80%, rgba(0, 240, 255, 0.18) 0%, transparent 55%),
      linear-gradient(0deg, #2a3140 0%, #161a22 60%, #0a0c10 100%);
    border: 2px solid #3d475c;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.65), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.75), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    bottom: 9px;
    left: 18px;
    width: 44px;
    height: 3px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
    opacity: 0.85;
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    bottom: 8%;
    left: 14%;
    width: 72%;
    height: 36%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 70%, rgba(255, 255, 255, 0.45) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.9), 0 -1px 0 rgba(255,255,255,0.6);
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
    --dpad-size: 80px;
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
      radial-gradient(circle at 20% 50%, rgba(0, 240, 255, 0.18) 0%, transparent 55%),
      linear-gradient(90deg, #2a3140 0%, #161a22 60%, #0a0c10 100%);
    border: 2px solid #3d475c;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.65), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.75), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    left: 9px;
    top: 18px;
    width: 3px;
    height: 44px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
    opacity: 0.85;
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 14%;
    left: 8%;
    width: 36%;
    height: 72%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 30% 50%, rgba(255, 255, 255, 0.45) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.9), 0 -1px 0 rgba(255,255,255,0.6);
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
    --dpad-size: 80px;
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
      radial-gradient(circle at 80% 50%, rgba(0, 240, 255, 0.18) 0%, transparent 55%),
      linear-gradient(270deg, #2a3140 0%, #161a22 60%, #0a0c10 100%);
    border: 2px solid #3d475c;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.65), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.75), 0 0 16px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::before {
    content: "";
    position: absolute;
    right: 9px;
    top: 18px;
    width: 3px;
    height: 44px;
    border-radius: 1.5px;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
    opacity: 0.85;
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 14%;
    right: 8%;
    width: 36%;
    height: 72%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 70% 50%, rgba(255, 255, 255, 0.45) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.9), 0 -1px 0 rgba(255,255,255,0.6);
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

    val PRESET_TRIGGER_LT = """
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
    width: 72px;
    height: 110px;
    border-radius: 20px;
    background: linear-gradient(180deg, #282f3d 0%, #161922 45%, #0a0c10 100%);
    border: 2px solid #3d4659;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.65), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 18px var(--accent-glow);
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
    background: rgba(255, 255, 255, 0.18);
    box-shadow: 0 8px 0 rgba(255, 255, 255, 0.12), 0 16px 0 rgba(255, 255, 255, 0.08);
  }
  .trigger-label {
    font-size: 28px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 2px 4px rgba(0,0,0,0.8), 0 0 12px var(--accent);
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

    val PRESET_BUMPER_LB = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --accent: #00F0FF;
    --accent-glow: rgba(0, 240, 255, 0.35);
    --spring-damping: 0.75;
    --spring-stiffness: 520;
    --press-scale: 0.96;
  }
  .bumper-btn {
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background: linear-gradient(180deg, #2c3342 0%, #171a23 60%, #0c0e13 100%);
    border: 2px solid #3d475c;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.7), 0 0 16px var(--accent-glow);
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
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7), 0 -1px 0 rgba(0, 0, 0, 0.9), 0 0 10px var(--accent);
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

    val PRESET_THUMBSTICK_RS = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --stick-size: 100px;
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
    background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%);
    border: 3px solid #3d4657;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7), inset 0 3px 6px rgba(255, 255, 255, 0.25), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 20px var(--accent-glow);
    box-sizing: border-box;
  }
  .stick-cap {
    position: absolute;
    width: 66px;
    height: 66px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #1a1e26 0%, #0d0f14 100%);
    box-shadow: inset 0 0 10px rgba(0,0,0,0.9), 0 0 0 2px rgba(255, 255, 255, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  .knurled-ring {
    position: absolute;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 2px dashed rgba(0, 176, 255, 0.5);
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
      <span class="stick-label">R3</span>
    </div>
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
    height: 44px;
    border-radius: 14px;
    background: radial-gradient(circle at 50% 30%, #29303e 0%, #12151d 100%);
    border: 1.5px solid #3d475c;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.6), inset 0 1px 3px rgba(255, 255, 255, 0.3), inset 0 -3px 6px rgba(0, 0, 0, 0.75);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .view-icon {
    font-size: 22px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 8px rgba(0, 240, 255, 0.6), 0 1px 2px rgba(0,0,0,0.9);
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
    background: radial-gradient(circle at 50% 35%, #303748 0%, #151922 70%, #07090d 100%);
    border: 2px solid #4f5b72;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.75), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -6px 12px rgba(0, 0, 0, 0.8), 0 0 20px rgba(255, 255, 255, 0.35);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .home-symbol {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 14px rgba(255, 255, 255, 0.85), 0 0 22px rgba(0, 240, 255, 0.5), 0 2px 4px rgba(0,0,0,0.9);
  }
  .system-home-btn:active {
    transform: scale(0.93) translateY(2px);
  }
</style>
</head>
<body>
  <button class="system-home-btn" data-control="HOME" data-category="SYSTEM" data-name="System Home">
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
        if (category.uppercase() != "BUTTON") {
            return when (category.uppercase()) {
                "DPAD" -> when (control.uppercase()) {
                    "DOWN" -> PRESET_DPAD_DOWN
                    "LEFT" -> PRESET_DPAD_LEFT
                    "RIGHT" -> PRESET_DPAD_RIGHT
                    "DPAD" -> PRESET_DPAD_CROSS
                    else -> PRESET_DPAD_UP
                }
                "TRIGGER" -> if (control.uppercase() == "LT") PRESET_TRIGGER_LT else PRESET_TRIGGER_RT
                "BUMPER" -> if (control.uppercase() == "LB") PRESET_BUMPER_LB else PRESET_BUMPER_RB
                "JOYSTICK" -> if (control.uppercase() == "RS") PRESET_THUMBSTICK_RS else PRESET_THUMBSTICK_LS
                "SYSTEM" -> when (control.uppercase()) {
                    "VIEW" -> PRESET_SYSTEM_VIEW
                    "HOME" -> PRESET_SYSTEM_HOME
                    else -> PRESET_SYSTEM_MENU
                }
                else -> getReferenceTemplateInternal(control, "BUTTON")
            }
        }

        return when (control.uppercase()) {
            "A" -> PRESET_NEO_TACTILE_A
            "B" -> PRESET_NEO_TACTILE_B
            "X" -> PRESET_NEO_TACTILE_X
            "Y" -> PRESET_NEO_TACTILE_Y
            "UP" -> PRESET_DPAD_UP
            "DOWN" -> PRESET_DPAD_DOWN
            "LEFT" -> PRESET_DPAD_LEFT
            "RIGHT" -> PRESET_DPAD_RIGHT
            "DPAD" -> PRESET_DPAD_CROSS
            "LT" -> PRESET_TRIGGER_LT
            "RT" -> PRESET_TRIGGER_RT
            "LB" -> PRESET_BUMPER_LB
            "RB" -> PRESET_BUMPER_RB
            "LS" -> PRESET_THUMBSTICK_LS
            "RS" -> PRESET_THUMBSTICK_RS
            "MENU" -> PRESET_SYSTEM_MENU
            "VIEW" -> PRESET_SYSTEM_VIEW
            "HOME" -> PRESET_SYSTEM_HOME
            else -> PRESET_NEO_TACTILE_A
        }
    }

    /**
     * Returns an unstyled, non-binding syntax skeleton illustrating the minimal compiler contract
     * for a given category. Intentionally free of pre-baked colors, gradients, and border-radii
     * to eliminate visual imitation bias in generative AI models.
     */
    fun getSyntaxSkeleton(control: String, category: String, widthDp: Int, heightDp: Int): String {
        return when (category.uppercase()) {
            "JOYSTICK" -> """
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
    width: ${widthDp}px;
    height: ${heightDp}px;
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
    left: ${(widthDp * 0.18).toInt()}px;
    top: ${(heightDp * 0.18).toInt()}px;
    width: ${(widthDp * 0.64).toInt()}px;
    height: ${(heightDp * 0.64).toInt()}px;
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
      <!-- Stationary socket layers -->
    </div>
    <div class="stick-cap">
      <!-- Movable thumb cap layers -->
      <span class="stick-label">${if (control.uppercase() == "RS") "R3" else "L3"}</span>
    </div>
  </button>
</body>
</html>
            """.trimIndent()

            "TRIGGER" -> """
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
    width: ${widthDp}px;
    height: ${heightDp}px;
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

            "BUMPER" -> """
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
    width: ${widthDp}px;
    height: ${heightDp}px;
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

            "DPAD" -> """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.72;
    --spring-stiffness: 480;
    --press-scale: ${if (control.uppercase() == "DPAD") "0.95" else "0.92"};
  }
  .dpad-btn {
    width: ${widthDp}px;
    height: ${heightDp}px;
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
    transform: ${if (control.uppercase() == "DPAD") "scale(0.95)" else "scale(0.92) translateY(2px)"};
  }
</style>
</head>
<body>
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-name="D-Pad $control">
    <span class="dpad-glyph">${when (control.uppercase()) { "DOWN" -> "▼"; "LEFT" -> "◀"; "RIGHT" -> "▶"; "DPAD" -> "❖"; else -> "▲" }}</span>
  </button>
</body>
</html>
            """.trimIndent()

            "SYSTEM" -> """
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
    width: ${widthDp}px;
    height: ${heightDp}px;
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

            else -> """
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
    width: ${widthDp}px;
    height: ${heightDp}px;
    position: relative;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    /* Visually design the face button silhouette, physical material, depth, and socket recess here */
  }
  /* Optional: embedded SVG emblem for complex characters or icons */
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
