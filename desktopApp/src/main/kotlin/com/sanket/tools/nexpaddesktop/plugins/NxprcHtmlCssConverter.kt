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
    val PRESET_ULTRA_NEXPAD_A = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
:root {
    --button-size: 96px;
    --a-light: #d8ffdf;
    --a-main: #65d67e;
    --a-mid: #32a94d;
    --a-dark: #125d29;
}

.nexpad-btn {
    width: var(--button-size);
    height: var(--button-size);
    position: relative;
    display: block;
    border-radius: 50%;
    overflow: hidden;
    box-sizing: border-box;
    opacity: 0.96;
    transform-origin: 30% 70%;
    transform: translate(0px, 0px) rotate(2deg) scaleX(1) scaleY(1);
    background:
        radial-gradient(circle at 26% 18%, rgba(255,255,255,0.85) 0%, rgba(255,255,255,0.32) 10%, transparent 30%),
        radial-gradient(circle at 72% 78%, rgba(0,0,0,0.30) 0%, transparent 55%),
        radial-gradient(circle at 48% 44%, var(--a-light) 0%, var(--a-main) 34%, var(--a-mid) 68%, var(--a-dark) 100%),
        linear-gradient(145deg, rgba(255,255,255,0.12), rgba(0,0,0,0.10));
    box-shadow:
        0px 2px 3px rgba(255,255,255,0.18),
        0px 5px 8px rgba(0,0,0,0.28),
        0px 12px 20px rgba(0,0,0,0.34),
        0px 22px 34px rgba(0,0,0,0.20),
        inset 0px 2px 3px rgba(255,255,255,0.36),
        inset 2px 0px 6px rgba(255,255,255,0.10),
        inset -3px 0px 8px rgba(0,0,0,0.12),
        inset 0px -9px 15px rgba(0,0,0,0.32);
}

.nexpad-btn::before {
    content: "";
    position: absolute;
    left: 5%;
    top: 5%;
    width: 90%;
    height: 90%;
    border-radius: 50%;
    transform-origin: 35% 30%;
    transform: rotate(-7deg);
    background:
        radial-gradient(ellipse at 27% 16%, rgba(255,255,255,0.58) 0%, rgba(255,255,255,0.18) 21%, transparent 48%),
        radial-gradient(ellipse at 55% 105%, rgba(0,0,0,0.30) 0%, transparent 62%),
        radial-gradient(circle at 50% 45%, rgba(255,255,255,0.08), transparent 70%);
    box-shadow:
        inset 0px 2px 4px rgba(255,255,255,0.26),
        inset 2px 0px 5px rgba(255,255,255,0.07),
        inset -2px 0px 6px rgba(0,0,0,0.10),
        inset 0px -6px 10px rgba(0,0,0,0.18);
    opacity: 0.88;
}

.nexpad-btn::after {
    content: "";
    position: absolute;
    left: 14%;
    top: 7%;
    width: 58%;
    height: 29%;
    border-radius: 50%;
    transform: rotate(-17deg) scaleY(0.92);
    background: radial-gradient(ellipse at 32% 28%, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.52) 18%, rgba(255,255,255,0.16) 43%, transparent 76%);
    opacity: 0.88;
}

.a-inner-ring {
    position: absolute;
    left: 9px;
    top: 9px;
    width: 78px;
    height: 78px;
    border-radius: 50%;
    border-top: 1px solid rgba(255,255,255,0.42);
    border-left: 1px solid rgba(255,255,255,0.18);
    border-right: 1px solid rgba(255,255,255,0.07);
    border-bottom: 1px solid rgba(0,0,0,0.20);
    box-shadow: inset 0px 1px 3px rgba(255,255,255,0.16), inset 0px -2px 5px rgba(0,0,0,0.13);
    opacity: 0.9;
}

.a-reflection {
    position: absolute;
    right: 12%;
    bottom: 15%;
    width: 33%;
    height: 11%;
    border-radius: 50%;
    transform: rotate(-20deg) scaleY(0.9);
    background: radial-gradient(ellipse at center, rgba(255,255,255,0.20) 0%, rgba(255,255,255,0.06) 45%, transparent 75%);
    opacity: 0.75;
}

.a-highlight {
    position: absolute;
    left: 29%;
    top: 14%;
    width: 15%;
    height: 5%;
    border-radius: 50%;
    background: radial-gradient(ellipse, rgba(255,255,255,0.78), rgba(255,255,255,0.20) 45%, transparent 75%);
    transform: rotate(-8deg);
    opacity: 0.78;
}

.a-label {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
    font-size: 44px;
    font-weight: 900;
    color: rgba(255,255,255,0.97);
    opacity: 0.98;
    text-shadow:
        0px -1px 0px rgba(255,255,255,0.90),
        0px 1px 0px rgba(255,255,255,0.35),
        0px 2px 2px rgba(0,0,0,0.26),
        0px 4px 5px rgba(0,0,0,0.28),
        0px 7px 10px rgba(0,0,0,0.18);
}

.nexpad-btn:active {
    opacity: 0.93;
    transform: translateY(2px) scale(0.95);
}
</style>
</head>
<body>
    <button class="nexpad-btn" data-control="A" data-name="Ultra A Button" data-category="BUTTON">
        <div class="a-inner-ring"></div>
        <div class="a-reflection"></div>
        <div class="a-highlight"></div>
        <span class="a-label">A</span>
    </button>
</body>
</html>
""".trimIndent()

    val PRESET_CYBER_REACTOR = """
<!DOCTYPE html>
<html lang="en">
<head>
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
    <div class="button-a"><span>A</span></div>
</body>
</html>
""".trimIndent()

    @Deprecated(
        message = "PRESET_CRIMSON_OCTA uses inline <svg> (banned by NXPRC rule 2) and is missing required data-control/data-category/data-name attributes. Do not pass to NxprcPackager.compile().",
        level = DeprecationLevel.WARNING
    )
    val PRESET_CRIMSON_OCTA = """
<button class="crimson-octa">
  <svg viewBox="0 0 100 100">
    <path d="M30 10 L70 10 L90 30 L90 70 L70 90 L30 90 L10 70 L10 30 Z" />
  </svg>
  <span>B</span>
</button>
<style>
.crimson-octa {
  background: radial-gradient(circle at 35% 30%, #FF0055, #660022, #140208);
  border: 2px solid #FF0055;
  box-shadow: 0 0 18px #FF0055;
  color: #FFFFFF;
}
</style>
""".trimIndent()

    @Deprecated(
        message = "PRESET_SPEED_TURBO uses inline <svg> which is banned by NXPRC rule 2. Do not pass to NxprcPackager.compile().",
        level = DeprecationLevel.WARNING
    )
    val PRESET_SPEED_TURBO = """
<button class="speed-turbo">
  <svg viewBox="0 0 100 100">
    <path d="M50 5 L60 40 L95 40 L65 65 L75 95 L50 75 L25 95 L35 65 L5 40 L40 40 Z" />
  </svg>
  <span>X</span>
</button>
<style>
.speed-turbo {
  background: linear-gradient(135deg, #FFCC00, #996600, #221A00);
  border: 2px solid #FFCC00;
  box-shadow: 0 0 22px #FFCC00;
  color: #FFFFFF;
}
</style>
""".trimIndent()

    val PRESET_NEO_TACTILE_A = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 96px;
    --accent-glow: rgba(74, 222, 128, 0.6);
    --accent-core: #4ade80;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #111a14 0%, #0a110c 70%, #040805 100%);
    border: 2px solid #233829;
    box-shadow: 
      0 10px 24px rgba(0, 0, 0, 0.65),
      0 0 0 3px rgba(18, 30, 22, 0.9),
      0 0 20px var(--accent-glow);
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
      conic-gradient(from 180deg at 50% 50%, #17261b, #294230, #131f16, #294230, #17261b);
    box-shadow: 
      inset 0 3px 6px rgba(255, 255, 255, 0.2),
      inset 0 -6px 12px rgba(0, 0, 0, 0.7);
  }

  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 15%;
    width: 70%;
    height: 38%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.75) 0%, rgba(255, 255, 255, 0.15) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.4);
  }

  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 38% 30%, rgba(255, 255, 255, 0.4) 0%, transparent 35%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.45) 0%, transparent 50%),
      linear-gradient(145deg, #10b981 0%, #059669 50%, #047857 100%);
    border: 1.5px solid rgba(74, 222, 128, 0.5);
    box-shadow: 
      inset 0 2px 4px rgba(255, 255, 255, 0.35),
      inset 0 -4px 8px rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 
      0 1px 0 rgba(255, 255, 255, 0.8),
      0 -1px 0 rgba(0, 0, 0, 0.9),
      0 3px 6px rgba(0, 0, 0, 0.75),
      0 0 12px var(--accent-core);
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
    --accent-glow: rgba(255, 51, 102, 0.6);
    --accent-core: #ff3366;
  }
  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #201115 0%, #120a0d 70%, #080405 100%);
    border: 2px solid #3d232a;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.65), 0 0 0 3px rgba(32, 17, 21, 0.9), 0 0 20px var(--accent-glow);
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
      conic-gradient(from 180deg at 50% 50%, #2b171c, #4a2831, #1f1014, #4a2831, #2b171c);
    box-shadow: inset 0 3px 6px rgba(255, 255, 255, 0.2), inset 0 -6px 12px rgba(0, 0, 0, 0.7);
  }
  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 15%;
    width: 70%;
    height: 38%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.75) 0%, rgba(255, 255, 255, 0.15) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.4);
  }
  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 38% 30%, rgba(255, 255, 255, 0.4) 0%, transparent 35%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.45) 0%, transparent 50%),
      linear-gradient(145deg, #f43f5e 0%, #e11d48 50%, #881337 100%);
    border: 1.5px solid rgba(255, 51, 102, 0.5);
    box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -4px 8px rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8), 0 -1px 0 rgba(0, 0, 0, 0.9), 0 3px 6px rgba(0, 0, 0, 0.75), 0 0 12px var(--accent-core);
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
    --accent-glow: rgba(0, 176, 255, 0.6);
    --accent-core: #00b0ff;
  }
  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #101622 0%, #090e17 70%, #04060b 100%);
    border: 2px solid #22324a;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.65), 0 0 0 3px rgba(16, 24, 38, 0.9), 0 0 20px var(--accent-glow);
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
      conic-gradient(from 180deg at 50% 50%, #162438, #263e61, #121c2c, #263e61, #162438);
    box-shadow: inset 0 3px 6px rgba(255, 255, 255, 0.2), inset 0 -6px 12px rgba(0, 0, 0, 0.7);
  }
  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 15%;
    width: 70%;
    height: 38%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.75) 0%, rgba(255, 255, 255, 0.15) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.4);
  }
  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 38% 30%, rgba(255, 255, 255, 0.4) 0%, transparent 35%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.45) 0%, transparent 50%),
      linear-gradient(145deg, #0284c7 0%, #0369a1 50%, #0c4a6e 100%);
    border: 1.5px solid rgba(0, 176, 255, 0.5);
    box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -4px 8px rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8), 0 -1px 0 rgba(0, 0, 0, 0.9), 0 3px 6px rgba(0, 0, 0, 0.75), 0 0 12px var(--accent-core);
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
    --accent-glow: rgba(255, 204, 0, 0.6);
    --accent-core: #ffcc00;
  }
  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #1e1a10 0%, #121008 70%, #060503 100%);
    border: 2px solid #3d3525;
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.65), 0 0 0 3px rgba(30, 26, 18, 0.9), 0 0 20px var(--accent-glow);
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
      conic-gradient(from 180deg at 50% 50%, #2b2517, #4a3e26, #1f1a10, #4a3e26, #2b2517);
    box-shadow: inset 0 3px 6px rgba(255, 255, 255, 0.2), inset 0 -6px 12px rgba(0, 0, 0, 0.7);
  }
  .nexpad-btn::after {
    content: "";
    position: absolute;
    top: 6%;
    left: 15%;
    width: 70%;
    height: 38%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.75) 0%, rgba(255, 255, 255, 0.15) 45%, transparent 75%);
    transform: rotate(-10deg);
    border-top: 1px solid rgba(255, 255, 255, 0.4);
  }
  .nexpad-btn .btn-core {
    position: relative;
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: 
      radial-gradient(circle at 38% 30%, rgba(255, 255, 255, 0.4) 0%, transparent 35%),
      radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.45) 0%, transparent 50%),
      linear-gradient(145deg, #eab308 0%, #ca8a04 50%, #713f12 100%);
    border: 1.5px solid rgba(255, 204, 0, 0.5);
    box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -4px 8px rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .nexpad-btn .btn-label {
    font-size: 34px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8), 0 -1px 0 rgba(0, 0, 0, 0.9), 0 3px 6px rgba(0, 0, 0, 0.75), 0 0 12px var(--accent-core);
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
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: radial-gradient(circle at 50% 25%, #252a36 0%, #13161c 70%, #0a0b0e 100%);
    border: 2px solid #333a4a;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    top: 8%;
    left: 15%;
    width: 70%;
    height: 35%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.4) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.8);
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
  }
  .dpad-cross {
    width: var(--cross-size);
    height: var(--cross-size);
    border-radius: 28px;
    background: radial-gradient(circle at 50% 50%, #1e222b 0%, #11141a 75%, #08090c 100%);
    border: 2px solid #2d3342;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7), inset 0 2px 6px rgba(255, 255, 255, 0.2), inset 0 -6px 14px rgba(0, 0, 0, 0.8), 0 0 24px rgba(0, 240, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-cross::before {
    content: "";
    position: absolute;
    width: 46px;
    height: 46px;
    border-radius: 50%;
    background: radial-gradient(circle at 45% 45%, #2a303d 0%, #0d0f14 100%);
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.8), 0 1px 2px rgba(255, 255, 255, 0.15);
  }
  .cross-center {
    font-size: 18px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 8px var(--accent);
    z-index: 5;
  }
  .dpad-cross:active {
    transform: scale(0.95);
  }
</style>
</head>
<body>
  <button class="dpad-cross" data-control="DPAD" data-category="DPAD" data-name="Cross Pad">
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
  }
  .trigger-btn {
    width: 72px;
    height: 110px;
    border-radius: 20px;
    background: linear-gradient(180deg, #242936 0%, #151820 40%, #0a0c10 100%);
    border: 2px solid #363d4f;
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
  }
  .bumper-btn {
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background: linear-gradient(180deg, #2a303d 0%, #171a22 60%, #0c0e12 100%);
    border: 2px solid #3a4254;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.7), 0 0 16px rgba(0, 240, 255, 0.35);
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
  }
  .stick-btn {
    width: var(--stick-size);
    height: var(--stick-size);
    border-radius: 50%;
    background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%);
    border: 3px solid #3d4657;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7), inset 0 3px 6px rgba(255, 255, 255, 0.25), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 20px rgba(74, 222, 128, 0.25);
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
  .system-btn {
    width: 64px;
    height: 44px;
    border-radius: 14px;
    background: radial-gradient(circle at 50% 30%, #242833 0%, #101217 100%);
    border: 1.5px solid #363c4c;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.55), inset 0 1px 3px rgba(255, 255, 255, 0.25), inset 0 -3px 6px rgba(0, 0, 0, 0.7);
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
    background: #E0E0E0;
    box-shadow: 0 1px 2px rgba(0,0,0,0.6);
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
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: radial-gradient(circle at 50% 75%, #252a36 0%, #13161c 70%, #0a0b0e 100%);
    border: 2px solid #333a4a;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-btn::after {
    content: "";
    position: absolute;
    bottom: 8%;
    left: 15%;
    width: 70%;
    height: 35%;
    border-radius: 12px;
    background: radial-gradient(ellipse at 50% 70%, rgba(255, 255, 255, 0.4) 0%, transparent 70%);
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.8);
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
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: radial-gradient(circle at 25% 50%, #252a36 0%, #13161c 70%, #0a0b0e 100%);
    border: 2px solid #333a4a;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.8);
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
  }
  .dpad-btn {
    width: var(--dpad-size);
    height: var(--dpad-size);
    border-radius: 18px;
    background: radial-gradient(circle at 75% 50%, #252a36 0%, #13161c 70%, #0a0b0e 100%);
    border: 2px solid #333a4a;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .dpad-arrow {
    font-size: 32px;
    font-weight: 900;
    color: var(--accent);
    text-shadow: 0 0 12px var(--accent), 0 2px 4px rgba(0,0,0,0.8);
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
  }
  .trigger-btn {
    width: 72px;
    height: 110px;
    border-radius: 20px;
    background: linear-gradient(180deg, #242936 0%, #151820 40%, #0a0c10 100%);
    border: 2px solid #363d4f;
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
  }
  .bumper-btn {
    width: 120px;
    height: 52px;
    border-radius: 18px;
    background: linear-gradient(180deg, #2a303d 0%, #171a22 60%, #0c0e12 100%);
    border: 2px solid #3a4254;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.3), inset 0 -4px 8px rgba(0, 0, 0, 0.7), 0 0 16px rgba(0, 240, 255, 0.35);
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
    --accent: #4ADE80;
  }
  .stick-btn {
    width: var(--stick-size);
    height: var(--stick-size);
    border-radius: 50%;
    background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%);
    border: 3px solid #3d4657;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7), inset 0 3px 6px rgba(255, 255, 255, 0.25), inset 0 -8px 16px rgba(0, 0, 0, 0.8), 0 0 20px rgba(74, 222, 128, 0.25);
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
  .system-btn {
    width: 64px;
    height: 44px;
    border-radius: 14px;
    background: radial-gradient(circle at 50% 30%, #242833 0%, #101217 100%);
    border: 1.5px solid #363c4c;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.55), inset 0 1px 3px rgba(255, 255, 255, 0.25), inset 0 -3px 6px rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .view-icon {
    font-size: 20px;
    color: #E0E0E0;
    text-shadow: 0 1px 2px rgba(0,0,0,0.8);
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
  .system-home-btn {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 35%, #2c3342 0%, #131720 70%, #07090d 100%);
    border: 2px solid #4a5568;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.7), inset 0 2px 4px rgba(255, 255, 255, 0.35), inset 0 -6px 12px rgba(0, 0, 0, 0.8), 0 0 20px rgba(255, 255, 255, 0.35);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }
  .home-symbol {
    font-size: 32px;
    font-weight: 900;
    color: #FFFFFF;
    text-shadow: 0 0 14px rgba(255, 255, 255, 0.85), 0 2px 4px rgba(0,0,0,0.9);
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
# NEXPAD VIRTUAL CONTROLLER COMPONENT SPECIFICATION
**Engineered & Tested for Frontier Generative AI Models:**
- OpenAI ChatGPT (GPT-4o, GPT-4, o1, o3-mini)
- Anthropic Claude (Claude 3.5 Sonnet, Claude 3.7 Sonnet)
- Google Gemini (Gemini 2.0 Flash / Pro, Gemini 1.5 Pro)
- DeepSeek (DeepSeek-V3, DeepSeek-R1)
- xAI Grok (Grok 2, Grok 3)
- Or any modern frontier LLM with HTML/CSS code generation capabilities
""".trimIndent()

private fun engineBoundaries(rootClass: String): String = """
### STRICT NEXPAD COMPILER CONTRACT — FOLLOW THIS EXACTLY:
1. **Single compiled component**: `<body>` must contain exactly one root `<button class="$rootClass" data-control="..." data-category="..." data-name="...">`. Keep every visual child inside it. The compiler selects this button and does not render a general web page.
2. **Portable document**: Include one `<style>` block, one root button, and no JavaScript, `<canvas>`, `<iframe>`, `<img>`, `<svg>`, `<link>`, `@import`, external fonts, or external assets. Use system fonts only.
3. **Use the supported paint primitives**: `background`/`background-color`, `linear-gradient`, `radial-gradient`, `conic-gradient`, explicit `border` (both `solid` and `dashed` are fully supported), `border-radius`, `box-shadow` (including multiple inset/outset shadows), `opacity`, `transform`, `transform-origin`, `overflow: hidden`, and `clip-path: polygon(...)`.
4. **Safe geometry**: Use `px` dimensions for the root and visual children. Use `border-radius` or `clip-path: polygon(...)` for circles, capsules, stars, diamonds, hexagons, handmade, asymmetric, and organic silhouettes. Do not use CSS masks, `path()` Bézier geometry, `filter: blur()`, `backdrop-filter`, `mix-blend-mode`, 3D transforms, or layout-dependent geometry.
5. **Explicit layers**: Set `position: relative` on the root. Set `position: absolute`, `left`, `top`, `width`, and `height` on every decorative child. Use `z-index` only for simple layer ordering. For grouped items (like menu bars, grip ribs, or stacked labels), Flexbox is fully supported: use `display: flex`, `flex-direction: row` or `column`, `gap: ...px`, `padding`, `justify-content`, and `align-items`. Do not use CSS grid, float, or multi-column layout.
6. **Text must be real DOM text**: Put labels and decorative symbols in actual `<span>`/`<div>` text nodes. Multi-label layouts (such as 'LT' + 'BRAKE' or directional markers) are fully supported with flexbox alignment. Do not use `content: 'A'`, generated text icons, icon fonts, emoji, or pseudo-element text; pseudo-elements may use `content: ""` only for painted shapes.
7. **Stable CSS only**: Do not use `@media`, `@supports`, `@keyframes`, `animation`, `transition`, `:hover`, `:focus`, `:focus-visible`, or `!important`. These are browser/page-state features and are not reliable in the NXPRC static button preview. Use `$rootClass:active` only for press feedback.
8. **Interaction**: Always define `$rootClass:active { transform: scale(...) translateY(...); }` using the exact root class. Do not put the press transform on a child unless the design specifically requires it.
9. **Colors and gradients**: Use explicit hex/rgb/rgba colors, CSS variables declared in `:root`, and explicit gradient stops. Never rely on `currentColor`, inherited colors, `color-mix()`, system theme colors, or uninitialized variables. Keep all colors inside the component so the desktop and Android previews match.
10. **Creative freedom**: `data-category` is metadata, not a shape instruction. It does not force a circle, cross, capsule, paddle, ring, gimbal, or any other silhouette. Preserve the user's requested shape, proportions, color palette, and visual language—even when they differ from the category.
11. **Self-check before output**: Confirm the document has exactly one compiled button, explicit px geometry, no unsupported features listed above, real text labels, a valid active rule, and only supported paint properties.
12. **Output validity**: Return one complete HTML document inside one `html` code block and no explanation outside it.
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
The NEXPAD engine converts your HTML/CSS into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-name="Action $control">`)**:
   - `border-radius: 50%`: Compiled to native `CanvasLayer.BoxLayer` with oval/circular geometry.
   - `background`: Stack multiple `radial-gradient` layers:
     - Top-left specular highlight: `radial-gradient(circle at 28% 20%, rgba(255,255,255,0.8) 0%, transparent 35%)`
     - Bottom-right occlusion shadow: `radial-gradient(circle at 72% 80%, rgba(0,0,0,0.4) 0%, transparent 60%)`
     - Main chromatic core: Multi-stop gradient for your button color ($hexCode).
2. **Multi-Tier Box Shadows**:
   - Outset: `box-shadow: 0 8px 24px rgba(0,0,0,0.65), 0 0 0 3px rgba(20,22,30,0.9), 0 0 20px var(--accent-glow);` (creates physical socket elevation and neon ambient halo).
   - Inset: `box-shadow: inset 0 2px 4px rgba(255,255,255,0.4), inset 0 -6px 12px rgba(0,0,0,0.7);` (creates 3D spherical bevel rim and recessed socket well).
3. **Pseudo-Elements**:
   - `::before`: Inner recessed core or metallic chamfered bezel ring (`conic-gradient` supported).
   - `::after`: Translucent elliptical gloss reflection arc (`radial-gradient(ellipse at 50% 30%, rgba(255,255,255,0.7) 0%, transparent 70%)` rotated by -12deg).
4. **Center Typography Glyph**:
   - `<span class="btn-label">$control</span>`: Font size 34-42px, weight 900.
   - Multi-layer `text-shadow`: `0 1px 0 rgba(255,255,255,0.8), 0 -1px 0 rgba(0,0,0,0.9), 0 3px 6px rgba(0,0,0,0.75), 0 0 12px var(--accent-core);` (renders as 3D extruded tactile letter).
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
The NEXPAD engine converts your HTML/CSS into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="dpad-btn" data-control="$control" data-category="DPAD" data-name="D-Pad $control">`)**:
   ${if (control.uppercase() == "DPAD") """
   - `border-radius: 28px`: Outer tactile cross housing.
   - `background`: Deep radial gradient with directional arm shading.
   - Central Pivot: Use `::before` to create a circular recessed pivot well (`width: 44px; height: 44px; border-radius: 50%`) with an inset drop shadow simulating the central rocker pivot.
   - Direction Markers: Crisp vector/font glyphs or markings for UP, DOWN, LEFT, RIGHT.
   """ else """
   - `border-radius: 18px`: Directional wedge/button housing.
   - `background`: Directional linear gradient sloped along the direction of travel ($control) from raised outer rim to recessed inner base.
   - Arrow Glyph: Directional indicator (<span class="dpad-arrow">$arrowGlyph</span>) with neon glow and drop shadow.
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
The NEXPAD engine converts your HTML/CSS into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-name="Trigger $control">`)**:
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 20px;` (ergonomic vertical capsule).
   - `background: linear-gradient(180deg, #282e3d 0%, #151822 45%, #0a0c10 100%)`: Simulates the curved rake angle of the trigger paddle receding into the gamepad shell.
2. **Traction Grip Ribs via `::before`**:
   - Use `::before` positioned at 45% top height with `box-shadow` to generate horizontal tactile grip ribs for thumb/finger friction:
     `background: rgba(255,255,255,0.18); box-shadow: 0 8px 0 rgba(255,255,255,0.12), 0 16px 0 rgba(255,255,255,0.08);`
3. **Multi-Tier Box Shadows**:
   - Outset: `box-shadow: 0 10px 24px rgba(0,0,0,0.65), 0 0 18px var(--accent-glow);`
   - Inset: `box-shadow: inset 0 2px 4px rgba(255,255,255,0.3), inset 0 -8px 16px rgba(0,0,0,0.8);` (deep vertical pull socket well).
4. **Stacked Typography**:
   - Vertical flex column: `<span class="trigger-label">$control</span>` (font-size 26px, weight 900) + `<span class="trigger-sub">$subLabel</span>` (font-size 10px, letter-spacing 1.5px).
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
The NEXPAD engine converts your HTML/CSS into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="bumper-btn" data-control="$control" data-category="BUMPER" data-name="Bumper $control">`)**:
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 18px;` (wide horizontal capsule).
   - `background: linear-gradient(180deg, #2b3240 0%, #161a22 65%, #0b0d12 100%)`: Convex curvature across the horizontal shoulder.
2. **Horizontal Specular Sheen via `::after`**:
   - Positioned across the upper third (`top: 10%; left: 12%; width: 76%; height: 35%; border-radius: 10px;`):
     `background: radial-gradient(ellipse at 50% 30%, rgba(255,255,255,0.45) 0%, transparent 75%);`
3. **Multi-Tier Box Shadows**:
   - Outset: `box-shadow: 0 8px 20px rgba(0,0,0,0.6), 0 0 16px var(--accent-glow);`
   - Inset: `box-shadow: inset 0 2px 4px rgba(255,255,255,0.35), inset 0 -4px 8px rgba(0,0,0,0.7);`
4. **Typography**:
   - `<span class="bumper-label">$control</span>`: Font size 24px, weight 900, with horizontal specular highlight and dark drop shadow.
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
The NEXPAD engine converts your HTML/CSS into native GPU Compose Canvas draw layers (.nxprc format):
1. **Outer Gimbal Housing (`<button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-name="Stick $control">`)**:
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 50%;`
   - `background: radial-gradient(circle at 45% 40%, #2b313d 0%, #14171e 65%, #08090c 100%)`
   - Inset deep well shadow: `box-shadow: inset 0 -8px 16px rgba(0,0,0,0.85), inset 0 3px 6px rgba(255,255,255,0.25);`
2. **Inner Concave Thumb Dome via `::before`**:
   - Centered circular dome (`width: 66px; height: 66px; border-radius: 50%`):
     `background: radial-gradient(circle at 50% 50%, #1a1e26 0%, #0d0f14 100%)`
     `box-shadow: inset 0 0 10px rgba(0,0,0,0.9), 0 0 0 2px rgba(255,255,255,0.12);`
3. **Concentric Knurled Grip Rings via `::after`**:
   - Concentric dashed/knurled ring (`width: 44px; height: 44px; border-radius: 50%; border: 2px dashed rgba(74, 222, 128, 0.5);`) providing physical thumb grip texture.
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
The NEXPAD engine converts your HTML/CSS into native GPU Compose Canvas draw layers (.nxprc format):
1. **Root Button Tag (`<button class="system-btn" data-control="$control" data-category="SYSTEM" data-name="System $control">`)**:
   ${if (control.uppercase() == "HOME") """
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 50%;`
   - Multi-tiered radial ambient lighting with glowing nexus emblem and silver chamfered bezel.
   """ else """
   - `width: ${widthDp}px; height: ${heightDp}px; border-radius: 14px;`
   - Radial dark gradient: `background: radial-gradient(circle at 50% 30%, #242833 0%, #101217 100%);`
   - Inset bevel shadows: `box-shadow: inset 0 1px 3px rgba(255,255,255,0.25), inset 0 -3px 6px rgba(0,0,0,0.7);`
   """}
2. **Iconography**:
   ${when (control.uppercase()) {
       "MENU" -> "- 3-line horizontal hamburger pause bars (`<div class=\"burger-bar\"></div>` with `width: 22px; height: 3px; border-radius: 1.5px; background: #E0E0E0;`)."
       "VIEW" -> "- Overlapping dual-rectangle back/select icons (`<span class=\"view-icon\">⧉</span>`)."
       else -> "- Central nexus/guide logo (`<span class=\"home-symbol\">⨂</span>`)."
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
