package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpad.nxprc.NxprcPackager

/**
 * Intelligent HTML / CSS / SVG to .nxprc Converter Facade.
 * Delegates compilation to the shared multiplatform NxprcPackager in :protocol.
 */
object NxprcHtmlCssConverter {

    /**
     * Converts raw HTML/CSS/SVG text into an NxprcDocument via shared :protocol engine.
     */
    fun convert(
        source: String,
        id: String,
        name: String,
        category: String = "BUTTON",
        defaultControl: String = "A"
    ): NxprcDocument {
        return NxprcPackager.compile(
            html = source,
            id = id,
            name = name,
            category = category,
            defaultControl = defaultControl
        )
    }

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
    justify-content: space-between;
    padding: 12px 0;
    box-sizing: border-box;
    position: relative;
  }
  .trigger-btn::before {
    content: "";
    position: absolute;
    top: 45%;
    width: 44px;
    height: 4px;
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.18);
    box-shadow: 0 8px 0 rgba(255, 255, 255, 0.12), 0 16px 0 rgba(255, 255, 255, 0.08);
  }
  .trigger-label {
    font-size: 26px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 2px 4px rgba(0,0,0,0.8), 0 0 12px var(--accent);
  }
  .trigger-sub {
    font-size: 11px;
    font-weight: 700;
    color: var(--accent);
    letter-spacing: 1px;
  }
  .trigger-btn:active {
    transform: scaleY(0.94) translateY(4px);
  }
</style>
</head>
<body>
  <button class="trigger-btn" data-control="RT" data-category="TRIGGER" data-name="Tactile Trigger RT">
    <span class="trigger-label">RT</span>
    <span class="trigger-sub">PULL</span>
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
    border-radius: 50%;
    background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%);
    border: 3px solid #3d4657;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7), inset 0 3px 6px rgba(255, 255, 255, 0.25), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 20px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .stick-btn::before {
    content: "";
    position: absolute;
    width: 66px;
    height: 66px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #1a1e26 0%, #0d0f14 100%);
    box-shadow: inset 0 0 10px rgba(0,0,0,0.9), 0 0 0 2px rgba(255, 255, 255, 0.12);
  }
  .stick-btn::after {
    content: "";
    position: absolute;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 2px dashed rgba(74, 222, 128, 0.5);
  }
  .stick-label {
    font-size: 22px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
    z-index: 5;
  }
  .stick-btn:active {
    transform: scale(0.92);
  }
</style>
</head>
<body>
  <button class="stick-btn" data-control="LS" data-category="JOYSTICK" data-name="Analog Stick LS">
    <span class="stick-label">L3</span>
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
    justify-content: space-between;
    padding: 12px 0;
    box-sizing: border-box;
    position: relative;
  }
  .trigger-btn::before {
    content: "";
    position: absolute;
    top: 45%;
    width: 44px;
    height: 4px;
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.18);
    box-shadow: 0 8px 0 rgba(255, 255, 255, 0.12), 0 16px 0 rgba(255, 255, 255, 0.08);
  }
  .trigger-label {
    font-size: 26px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 2px 4px rgba(0,0,0,0.8), 0 0 12px var(--accent);
  }
  .trigger-sub {
    font-size: 11px;
    font-weight: 700;
    color: var(--accent);
    letter-spacing: 1px;
  }
  .trigger-btn:active {
    transform: scaleY(0.94) translateY(4px);
  }
</style>
</head>
<body>
  <button class="trigger-btn" data-control="LT" data-category="TRIGGER" data-name="Tactile Trigger LT">
    <span class="trigger-label">LT</span>
    <span class="trigger-sub">BRAKE</span>
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
    border-radius: 50%;
    background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%);
    border: 3px solid #3d4657;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7), inset 0 3px 6px rgba(255, 255, 255, 0.25), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 20px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .stick-btn::before {
    content: "";
    position: absolute;
    width: 66px;
    height: 66px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #1a1e26 0%, #0d0f14 100%);
    box-shadow: inset 0 0 10px rgba(0,0,0,0.9), 0 0 0 2px rgba(255, 255, 255, 0.12);
  }
  .stick-btn::after {
    content: "";
    position: absolute;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 2px dashed rgba(0, 240, 255, 0.5);
  }
  .stick-label {
    font-size: 22px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
    z-index: 5;
  }
  .stick-btn:active {
    transform: scale(0.92);
  }
</style>
</head>
<body>
  <button class="stick-btn" data-control="RS" data-category="JOYSTICK" data-name="Analog Stick RS">
    <span class="stick-label">R3</span>
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
     * Generates an in-depth, specialized AI Prompt tailored specifically to the target button type.
     * Delegates to dedicated generators for ABXY, D-PAD, TRIGGERS, BUMPERS, JOYSTICKS, and SYSTEM buttons.
     */
    fun generateAiPrompt(
        control: String,
        category: String,
        widthDp: Int,
        heightDp: Int
    ): String {
        return when (category.uppercase()) {
            "TRIGGER" -> generateTriggerPrompt(control, widthDp, heightDp)
            "BUMPER" -> generateBumperPrompt(control, widthDp, heightDp)
            "DPAD" -> generateDpadPrompt(control, widthDp, heightDp)
            "JOYSTICK" -> generateStickPrompt(control, widthDp, heightDp)
            "SYSTEM" -> generateSystemPrompt(control, widthDp, heightDp)
            else -> generateAbxyPrompt(control, widthDp, heightDp)
        }
    }

    private fun genAiHeader(): String = """
# NEXPAD 10/10 VIRTUAL CONTROLLER COMPONENT SPECIFICATION
**Engineered & Validated for Frontier Generative AI Models & NXPRC 10/10 Protocol Engine:**
- OpenAI ChatGPT (GPT-4o, GPT-4, o1, o3-mini)
- Anthropic Claude (Claude 3.5 Sonnet, Claude 3.7 Sonnet)
- Google Gemini (Gemini 2.5 Flash / Pro, Gemini 2.0 Flash, Gemini 1.5 Pro)
- DeepSeek (DeepSeek-V3, DeepSeek-R1)
- xAI Grok (Grok 2, Grok 3)
- Or any modern frontier LLM with HTML/CSS/SVG code generation capabilities
""".trimIndent()

private fun engineBoundaries(rootClass: String): String = """
### STRICT NEXPAD COMPILER CONTRACT — FOLLOW THIS EXACTLY:
1. **Single compiled component**: `<body>` must contain exactly one root `<button class="$rootClass" data-control="..." data-category="..." data-name="...">`. Keep every visual child inside it. The compiler selects this button and does not render a general web page.
2. **Portable self-contained document**: Include one `<style>` block, one root button, and no external dependencies (no external `<link>`, `@import`, remote font files, or external web scripts). System fonts only.
3. **Paint & vector primitives (10/10 Protocol Engine)**:
   - Fills: `background`/`background-color`, multi-stop `linear-gradient`, `radial-gradient`, and `conic-gradient`.
   - Modern CSS Colors: hex (`#rrggbbaa`), `rgb()`, `rgba()`, `hsl()`, `hwb()`, `oklch()`, and `color(display-p3 ...)`.
   - Borders & Outlines: explicit `border` (both `solid` and `dashed` are fully supported), `border-radius` (uniform or 4-corner), and `outline`.
   - Multi-tier Box Shadows: multiple inset and outset shadows (`box-shadow: 0 8px 24px rgba(0,0,0,0.65), inset 0 2px 4px rgba(255,255,255,0.4)`).
   - Optical Filters: Use GPU `filter: blur()`, `brightness()`, `contrast()`, `saturate()`, `hue-rotate()`, or SVG `<filter>` graphs for optical effects. Do not use `backdrop-filter` or `mix-blend-mode`.
   - SVG Graphics & Filter Nodes: Embedded `<svg>` with `<path d="...">`, `<circle>`, `<rect>`, `<polygon>`, `<g>`, and `<filter id="...">` graphs (`<feGaussianBlur>`, `<feColorMatrix>`, `<feDropShadow>`, `<feBlend>`) are fully compiled to native Compose vector and filter graphs.
4. **Safe geometry & shapes**: Use `px` dimensions for the root and visual children. Use `border-radius` or `clip-path: polygon(...)` for circles, capsules, stars, diamonds, hexagons, handmade, asymmetric, and organic silhouettes. Preserve the user's requested shape, proportions, and aesthetic.
5. **Layout & flexbox**:
   - Relative root with absolute layered children: Set `position: relative` on the root. Set `position: absolute`, `left`, `top`, `width`, and `height` on decorative children as needed. Use `z-index` for layer ordering.
   - Flexbox layouts: Fully supported for alignment, flow, and grouped components. Use `display: flex`, `flex-direction: row | column`, `flex-wrap: wrap | nowrap`, `gap: ...px`, `row-gap`, `column-gap`, `justify-content`, and `align-items`.
6. **Typographic auto-wrapping & text**:
   - Text must be real DOM text: Put labels, legends, and decorative symbols in actual `<span>`/`<div>` text nodes. Multi-label layouts (such as 'LT' + 'BRAKE' or directional markers) are fully supported.
   - Text formatting: Supports `font-size`, `font-weight`, `letter-spacing`, `line-height`, `text-shadow`, `text-align`, and multi-line wrapping via `white-space: normal | pre-line` and explicit newlines. Do not use pseudo-element text with icons or emoji; pseudo-elements `::before`/`::after` may use `content: ""` only for painted layers.
7. **Tactile micro-physics & animation**:
   - Physical spring calibration: Declare spring physics custom properties in `:root`: `--spring-damping: 0.68;`, `--spring-stiffness: 440;`, `--press-scale: 0.92;`. These calibrate physical tactile button weight, dampening, and spring return speed on Android touch HUDs.
   - Interaction state: Always define `.$rootClass:active { transform: scale(...) translateY(...); }` using the exact root class.
   - Continuous timeline animations: CSS `@keyframes` with `animation: ... infinite` are compiled to hardware-accelerated Compose transition animators. Do not use `@media`, `@supports`, `:hover`, `:focus`, or `:focus-visible` (these are browser page-state features).
8. **Creative freedom**: `data-category` is metadata, not a shape instruction. It does not force a circle, cross, capsule, paddle, ring, gimbal, or any other silhouette. Preserve the user's requested shape, proportions, color palette, and visual language—even when they differ from the category.
9. **Self-check before output**: Confirm the document has exactly one compiled button with class "$rootClass", explicit px geometry, real DOM text labels, valid active transform with tactile spring properties, and supported paint properties.
10. **Output contract**: Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block.
""".trimIndent()

    private fun generateAbxyPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val (colorName, hexCode, rgbGlow, coreGrad) = when (control.uppercase()) {
            "X" -> Quadruple("Vibrant Sapphire Blue", "#00B0FF", "rgba(0, 176, 255, 0.6)", "linear-gradient(145deg, #0284c7 0%, #0369a1 50%, #0c4a6e 100%)")
            "Y" -> Quadruple("Radiant Solar Yellow", "#FFCC00", "rgba(255, 204, 0, 0.6)", "linear-gradient(145deg, #eab308 0%, #ca8a04 50%, #713f12 100%)")
            "B" -> Quadruple("Vibrant Crimson Red", "#FF3366", "rgba(255, 51, 102, 0.6)", "linear-gradient(145deg, #f43f5e 0%, #e11d48 50%, #881337 100%)")
            else -> Quadruple("Vibrant Emerald Green", "#4ADE80", "rgba(74, 222, 128, 0.6)", "linear-gradient(145deg, #10b981 0%, #059669 50%, #047857 100%)")
        }

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Face Action Button for NEXPAD.

### TARGET COMPONENT: ABXY FACE BUTTON
- **Button Key**: $control (Standard Gamepad Face Button)
- **Category**: BUTTON
- **Target Dimensions**: width: ${widthDp}px; height: ${heightDp}px; (fixed canvas size; choose any silhouette)
- **Standard Color Profile**: $colorName (Accent: $hexCode, Glow: $rgbGlow)
- **Recommended Core**: $coreGrad
- **Design Intent (OPTIONAL INSPIRATION)**: Momentary tactile action feedback with readable labeling, layered depth, directional lighting, and physical spring depression. The silhouette is completely yours.

### NEXPAD COMPILER ARCHITECTURE & LAYER TRANSLATION:
The NEXPAD engine converts your HTML/CSS/SVG into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-name="Action $control">`)**:
   - `border-radius: 50%`: Compiled to native `CanvasLayer.BoxLayer` with oval/circular geometry (or use `clip-path: polygon(...)` for custom faceted geometries).
   - `background`: Stack multiple `radial-gradient` layers:
     - Top-left specular highlight: `radial-gradient(circle at 28% 20%, rgba(255,255,255,0.8) 0%, transparent 35%)`
     - Bottom-right occlusion shadow: `radial-gradient(circle at 72% 80%, rgba(0,0,0,0.4) 0%, transparent 60%)`
     - Main chromatic core: Multi-stop gradient for your button color ($hexCode).
   - **Tactile Spring Micro-Physics**: Configure in `:root`:
     `--spring-damping: 0.68; --spring-stiffness: 440; --press-scale: 0.92;`
2. **Multi-Tier Box Shadows**:
   - Outset: `box-shadow: 0 8px 24px rgba(0,0,0,0.65), 0 0 0 3px rgba(20,22,30,0.9), 0 0 20px var(--accent-glow);` (creates physical socket elevation and neon ambient halo).
   - Inset: `box-shadow: inset 0 2px 4px rgba(255,255,255,0.4), inset 0 -6px 12px rgba(0,0,0,0.7);` (creates 3D spherical bevel rim and recessed socket well).
3. **Pseudo-Elements & SVG Layers**:
   - `::before`: Inner recessed core or metallic chamfered bezel ring (`conic-gradient` supported).
   - `::after`: Translucent elliptical gloss reflection arc (`radial-gradient(ellipse at 50% 30%, rgba(255,255,255,0.7) 0%, transparent 70%)` rotated by -12deg).
   - Embedded `<svg>`: Full support for vector iconography, paths, and SVG `<filter>` graphs (`<feGaussianBlur>`, `<feColorMatrix>`).
4. **Center Typography Glyph & Auto-Wrapping**:
   - `<span class="btn-label">$control</span>`: Font size 34-42px, weight 900.
   - Multi-layer `text-shadow`: `0 1px 0 rgba(255,255,255,0.8), 0 -1px 0 rgba(0,0,0,0.9), 0 3px 6px rgba(0,0,0,0.75), 0 0 12px var(--accent-core);` (renders as 3D extruded tactile letter).
   - Auto-wrapping support: Supports `white-space: pre-line` or `normal`, with `line-height` and explicit newlines.
5. **Tactile Active Physics**:
   - `.nexpad-btn:active { transform: scale(0.93) translateY(3px); }` (compiles into native Compose spring physics).

${engineBoundaries("nexpad-btn")}

### OPTIONAL STARTER TEMPLATE (REFERENCE ONLY):
Use this only to understand the expected document structure. Design freely, but do not copy any property that conflicts with the STRICT NEXPAD COMPILER CONTRACT above. Rebuild the geometry, colors, layers, and visual language with the supported subset:
```html
${getReferenceTemplate(control, "BUTTON")}
```

### USER CUSTOMIZATION REQUEST:
[Describe your desired visual aesthetic here, e.g. "Cyberpunk 2077 neon with dark carbon fiber", "Xbox Elite gunmetal brushed aluminum with glow", "PlayStation 5 frosted smoked glass with micro-dot texture", or "Retro arcade microswitch"]

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateDpadPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val arrowGlyph = when (control.uppercase()) {
            "DOWN" -> "▼"
            "LEFT" -> "◀"
            "RIGHT" -> "▶"
            "DPAD" -> "❖"
            else -> "▲"
        }

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller D-Pad Component for NEXPAD.

### TARGET COMPONENT: DIRECTIONAL PAD (D-PAD)
- **Button Key**: $control (${if (control.uppercase() == "DPAD") "Unified 4-Way Cross Pad" else "Directional Arrow Button"})
- **Category**: DPAD
- **Target Dimensions**: width: ${widthDp}px; height: ${heightDp}px;
- **Directional Glyph**: $arrowGlyph
- **Design Intent (OPTIONAL INSPIRATION)**: Preserve directional meaning for $control with clear visual feedback and tactile actuation. A cross-pad, arrow, wedge, star, organic form, or any other silhouette is valid; do not force a conventional D-pad shape.

### NEXPAD COMPILER ARCHITECTURE & LAYER TRANSLATION:
The NEXPAD engine converts your HTML/CSS/SVG into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="dpad-btn" data-control="$control" data-category="DPAD" data-name="D-Pad $control">`)**:
   - **Tactile Spring Micro-Physics**: Configure in `:root`:
     `--spring-damping: 0.72; --spring-stiffness: 480; --press-scale: 0.94;`
   ${if (control.uppercase() == "DPAD") """
   - `border-radius: 28px`: Outer tactile cross housing (or `clip-path: polygon(...)` for a 12-point faceted cross).
   - `background`: Deep radial gradient with directional arm shading.
   - Central Pivot: Use `::before` to create a circular recessed pivot well (`width: 44px; height: 44px; border-radius: 50%`) with an inset drop shadow simulating the central rocker pivot.
   - Direction Markers: Crisp vector/font glyphs, SVG directional arrows, or markings for UP, DOWN, LEFT, RIGHT.
   """ else """
   - `border-radius: 18px`: Directional wedge/button housing.
   - `background`: Directional linear gradient sloped along the direction of travel ($control) from raised outer rim to recessed inner base.
   - Arrow Glyph: Directional indicator (<span class="dpad-arrow">$arrowGlyph</span>) or embedded `<svg>` chevron with neon glow and drop shadow.
   """}
2. **Multi-Tier Box Shadows**:
   - Outset: `box-shadow: 0 10px 24px rgba(0,0,0,0.65), 0 0 0 2px rgba(35,40,55,0.8), 0 0 20px var(--accent-glow);`
   - Inset: `box-shadow: inset 0 2px 4px rgba(255,255,255,0.25), inset 0 -5px 10px rgba(0,0,0,0.7);`
3. **Tactile Active Physics**:
   - `.dpad-btn:active { transform: ${if (control.uppercase() == "DPAD") "scale(0.95)" else "scale(0.92) translateY(2px)"}; }`

${engineBoundaries("dpad-btn")}

### OPTIONAL STARTER TEMPLATE (REFERENCE ONLY):
Use this only to understand the expected document structure. Design freely, but do not copy any property that conflicts with the STRICT NEXPAD COMPILER CONTRACT above. Rebuild the geometry, colors, layers, and visual language with the supported subset:
```html
${getReferenceTemplate(control, "DPAD")}
```

### USER CUSTOMIZATION REQUEST:
[Describe your desired visual aesthetic here, e.g. "Stealth matte black with cyan directional laser engravings", "Retro Nintendo Game Boy matte dark gray cross", "Cyberpunk high-contrast hazard chevron styling", or "Mechanical arcade microswitch aesthetic"]

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateTriggerPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val subLabel = if (control.uppercase() == "LT") "BRAKE" else "PULL"

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Analog Trigger for NEXPAD.

### TARGET COMPONENT: ANALOG PULL TRIGGER ($control)
- **Button Key**: $control (${if (control.uppercase() == "LT") "Left Trigger / Brake / Aim" else "Right Trigger / Throttle / Fire"})
- **Category**: TRIGGER
- **Target Dimensions**: width: ${widthDp}px; height: ${heightDp}px; (fixed canvas size; choose any silhouette)
- **Labels**: Primary "$control" with sub-label "$subLabel"
- **Design Intent (OPTIONAL INSPIRATION)**: Represent analog pull, pressure, and release with clear travel feedback. A paddle, wedge, ring, vertical bar, star, or any original silhouette is valid.

### NEXPAD COMPILER ARCHITECTURE & LAYER TRANSLATION:
The NEXPAD engine converts your HTML/CSS/SVG into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-name="Trigger $control">`)**:
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 20px;` (ergonomic vertical capsule or angular wedge).
   - `background: linear-gradient(180deg, #282e3d 0%, #151822 45%, #0a0c10 100%)`: Simulates the curved rake angle of the trigger paddle receding into the gamepad shell.
   - **Tactile Spring Micro-Physics**: Configure in `:root`:
     `--spring-damping: 0.65; --spring-stiffness: 380; --press-scale: 0.94;`
2. **Traction Grip Ribs via Flexbox or `::before`**:
   - Grouped grip ribs: Use Flexbox (`display: flex`, `flex-direction: column`, `gap: 6px`) or `::before` with multi-tier `box-shadow` for horizontal friction ridges:
     `background: rgba(255,255,255,0.18); box-shadow: 0 8px 0 rgba(255,255,255,0.12), 0 16px 0 rgba(255,255,255,0.08);`
3. **Multi-Tier Box Shadows**:
   - Outset: `box-shadow: 0 10px 24px rgba(0,0,0,0.65), 0 0 18px var(--accent-glow);`
   - Inset: `box-shadow: inset 0 2px 4px rgba(255,255,255,0.3), inset 0 -8px 16px rgba(0,0,0,0.8);` (deep vertical pull socket well).
4. **Stacked Typography & Typographic Wrapping**:
   - Vertical flex column with real DOM text: `<span class="trigger-label">$control</span>` (font-size 26px, weight 900) + `<span class="trigger-sub">$subLabel</span>` (font-size 10px, letter-spacing 1.5px).
   - Multi-line wrapping and `line-height` are natively calculated by the protocol engine.
5. **Tactile Active Travel Physics**:
   - `.trigger-btn:active { transform: scaleY(0.94) translateY(4px); }` (simulates physical downward paddle pull stroke).

${engineBoundaries("trigger-btn")}

### OPTIONAL STARTER TEMPLATE (REFERENCE ONLY):
Use this only to understand the expected document structure. Design freely, but do not copy any property that conflicts with the STRICT NEXPAD COMPILER CONTRACT above. Rebuild the geometry, colors, layers, and visual language with the supported subset:
```html
${getReferenceTemplate(control, "TRIGGER")}
```

### USER CUSTOMIZATION REQUEST:
[Describe your desired visual aesthetic here, e.g. "Carbon fiber racing trigger with Brembo red accents", "Cyberpunk neon magenta with digital pressure gauge gradient", "Military tactical grip with grooved stippling", or "Aerospace titanium with laser-etched telemetry"]

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateBumperPrompt(control: String, widthDp: Int, heightDp: Int): String = """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Shoulder Bumper for NEXPAD.

### TARGET COMPONENT: SHOULDER BUMPER SWITCH ($control)
- **Button Key**: $control (${if (control.uppercase() == "LB") "Left Bumper / Secondary Weapon" else "Right Bumper / Primary Weapon"})
- **Category**: BUMPER
- **Target Dimensions**: width: ${widthDp}px; height: ${heightDp}px; (fixed canvas size; choose any silhouette)
- **Design Intent (OPTIONAL INSPIRATION)**: Represent a shallow shoulder click with clear press feedback. A capsule, tile, shard, star, handmade polygon, or any original silhouette is valid.

### NEXPAD COMPILER ARCHITECTURE & LAYER TRANSLATION:
The NEXPAD engine converts your HTML/CSS/SVG into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="bumper-btn" data-control="$control" data-category="BUMPER" data-name="Bumper $control">`)**:
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 18px;` (wide horizontal capsule or curved wedge).
   - `background: linear-gradient(180deg, #2b3240 0%, #161a22 65%, #0b0d12 100%)`: Convex curvature across the horizontal shoulder.
   - **Tactile Spring Micro-Physics**: Configure in `:root`:
     `--spring-damping: 0.75; --spring-stiffness: 520; --press-scale: 0.96;`
2. **Horizontal Specular Sheen via `::after` or SVG**:
   - Positioned across the upper third (`top: 10%; left: 12%; width: 76%; height: 35%; border-radius: 10px;`):
     `background: radial-gradient(ellipse at 50% 30%, rgba(255,255,255,0.45) 0%, transparent 75%);`
3. **Multi-Tier Box Shadows**:
   - Outset: `box-shadow: 0 8px 20px rgba(0,0,0,0.6), 0 0 16px var(--accent-glow);`
   - Inset: `box-shadow: inset 0 2px 4px rgba(255,255,255,0.35), inset 0 -4px 8px rgba(0,0,0,0.7);`
4. **Typography & Layout**:
   - `<span class="bumper-label">$control</span>`: Font size 24px, weight 900, with horizontal specular highlight and dark drop shadow.
   - Flexbox centering: `display: flex; align-items: center; justify-content: center;`
5. **Tactile Active Click Physics**:
   - `.bumper-btn:active { transform: scale(0.96) translateY(2px); }` (simulates shallow micro-switch click).

${engineBoundaries("bumper-btn")}

### OPTIONAL STARTER TEMPLATE (REFERENCE ONLY):
Use this only to understand the expected document structure. Design freely, but do not copy any property that conflicts with the STRICT NEXPAD COMPILER CONTRACT above. Rebuild the geometry, colors, layers, and visual language with the supported subset:
```html
${getReferenceTemplate(control, "BUMPER")}
```

### USER CUSTOMIZATION REQUEST:
[Describe your desired visual aesthetic here, e.g. "Brushed gunmetal aluminum with electric blue edge lighting", "Matte stealth carbon weave with subtle bevel", "Sci-fi spacecraft thruster bumper with glowing vents", or "Clean minimal Xbox Series style"]

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()

    private fun generateStickPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val clickLabel = if (control.uppercase() == "RS") "R3" else "L3"

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Thumbstick Component for NEXPAD.

### TARGET COMPONENT: ANALOG THUMBSTICK ($control)
- **Button Key**: $control ($clickLabel Click)
- **Category**: JOYSTICK
- **Target Dimensions**: width: ${widthDp}px; height: ${heightDp}px; (fixed canvas size; choose any silhouette)
- **Design Intent (OPTIONAL INSPIRATION)**: Represent analog movement and $clickLabel click actuation with readable state feedback. A gimbal, ring, square, abstract mark, organic form, or any original silhouette is valid.

### NEXPAD COMPILER ARCHITECTURE & LAYER TRANSLATION:
The NEXPAD engine converts your HTML/CSS/SVG into native GPU Compose Canvas draw layers (.nxprc format):
1. **Outer Gimbal Housing (`<button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-name="Stick $control">`)**:
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 50%;`
   - `background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%)`
   - Inset deep well shadow: `box-shadow: inset 0 -8px 16px rgba(0,0,0,0.85), inset 0 3px 6px rgba(255,255,255,0.25);`
   - **Tactile Spring Micro-Physics**: Configure in `:root`:
     `--spring-damping: 0.70; --spring-stiffness: 420; --press-scale: 0.92;`
2. **Inner Concave Thumb Dome via `::before`**:
   - Centered circular dome (`width: 66px; height: 66px; border-radius: 50%`):
     `background: radial-gradient(circle at 50% 50%, #1a1e26 0%, #0d0f14 100%)`
     `box-shadow: inset 0 0 10px rgba(0,0,0,0.9), 0 0 0 2px rgba(255,255,255,0.12);`
3. **Concentric Knurled Grip Rings via `::after` or SVG**:
   - Concentric dashed/knurled ring (`width: 44px; height: 44px; border-radius: 50%; border: 2px dashed rgba(74, 222, 128, 0.5);`) or SVG radial tick pattern providing physical thumb grip traction.
4. **Stick Click Typography**:
   - `<span class="stick-label">$clickLabel</span>`: Font size 22px, weight 900, with neon ambient backlighting.
5. **Tactile Active Press Physics**:
   - `.stick-btn:active { transform: scale(0.92); }` (simulates physical thumbstick button depression).

${engineBoundaries("stick-btn")}

### OPTIONAL STARTER TEMPLATE (REFERENCE ONLY):
Use this only to understand the expected document structure. Design freely, but do not copy any property that conflicts with the STRICT NEXPAD COMPILER CONTRACT above. Rebuild the geometry, colors, layers, and visual language with the supported subset:
```html
${getReferenceTemplate(control, "JOYSTICK")}
```

### USER CUSTOMIZATION REQUEST:
[Describe your desired visual aesthetic here, e.g. "Contoured tactical rubber dome with neon green grip ring", "Xbox Elite magnetic swappable metal stick with cross-hatch grip", "PlayStation DualSense two-tone concave dome", or "Arcade flight-sim gimbal joystick"]

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateSystemPrompt(control: String, widthDp: Int, heightDp: Int): String = """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller System/Utility Button for NEXPAD.

### TARGET COMPONENT: SYSTEM / UTILITY BUTTON ($control)
- **Button Key**: $control (${when(control.uppercase()) { "MENU" -> "Menu / Pause / Start"; "VIEW" -> "View / Back / Select"; else -> "Home / Guide / Nexus" }})
- **Category**: SYSTEM
- **Target Dimensions**: width: ${widthDp}px; height: ${heightDp}px;
- **Design Intent (OPTIONAL INSPIRATION)**: Compact utility control with clear iconography and tactile click feedback. A sphere, pill, tile, emblem, star, or any original silhouette is valid.

### NEXPAD COMPILER ARCHITECTURE & LAYER TRANSLATION:
The NEXPAD engine converts your HTML/CSS/SVG into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="system-btn" data-control="$control" data-category="SYSTEM" data-name="System $control">`)**:
   - **Tactile Spring Micro-Physics**: Configure in `:root`:
     `--spring-damping: 0.78; --spring-stiffness: 500; --press-scale: 0.92;`
   ${if (control.uppercase() == "HOME") """
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 50%;`
   - Multi-tiered radial ambient lighting with glowing nexus emblem and silver chamfered bezel.
   """ else """
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 14px;`
   - Radial dark gradient: `background: radial-gradient(circle at 50% 30%, #242833 0%, #101217 100%);`
   - Inset bevel shadows: `box-shadow: inset 0 1px 3px rgba(255,255,255,0.25), inset 0 -3px 6px rgba(0,0,0,0.7);`
   """}
2. **Iconography & Grouped Elements**:
   ${when (control.uppercase()) {
       "MENU" -> "- 3-line horizontal hamburger pause bars (`<div class=\"burger-bar\"></div>` with `width: 22px; height: 3px; border-radius: 1.5px; background: #E0E0E0;`) using flexbox vertical column (`display: flex; flex-direction: column; gap: 4px;`)."
       "VIEW" -> "- Overlapping dual-rectangle back/select icons (`<span class=\"view-icon\">⧉</span>` or embedded `<svg>`)."
       else -> "- Central nexus/guide logo (`<span class=\"home-symbol\">⨂</span>` or embedded `<svg>`)."
   }}
3. **Tactile Active Click Physics**:
   - `.system-btn:active { transform: scale(0.92) translateY(2px); }`

${engineBoundaries("system-btn")}

### OPTIONAL STARTER TEMPLATE (REFERENCE ONLY):
Use this only to understand the expected document structure. Design freely, but do not copy any property that conflicts with the STRICT NEXPAD COMPILER CONTRACT above. Rebuild the geometry, colors, layers, and visual language with the supported subset:
```html
${getReferenceTemplate(control, "SYSTEM")}
```

### USER CUSTOMIZATION REQUEST:
[Describe your desired visual aesthetic here, e.g. "Minimalist matte dark pill with illuminated icon", "Cyberpunk neon yellow utility toggle", "Xbox Series glass guide button with white LED backlighting", or "Brushed steel flush console switch"]

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
