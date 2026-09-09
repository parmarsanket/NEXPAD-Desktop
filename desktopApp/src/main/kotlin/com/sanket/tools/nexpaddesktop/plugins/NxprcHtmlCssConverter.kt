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

.a-button {
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

.a-button::before {
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

.a-button::after {
    content: "";
    position: absolute;
    left: 14%;
    top: 7%;
    width: 58%;
    height: 29%;
    border-radius: 50%;
    transform: rotate(-17deg) scaleY(0.92);
    background: radial-gradient(ellipse at 32% 28%, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.52) 18%, rgba(255,255,255,0.16) 43%, transparent 76%);
    filter: blur(1.2px);
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
    filter: blur(2px);
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
    filter: blur(1px);
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

.a-button:active {
    opacity: 0.93;
    transform: translateY(2px) scale(0.95);
}
</style>
</head>
<body>
    <div class="a-button" data-id="rc.ultra_a" data-name="Ultra A Button" data-control="A" data-category="BUTTON">
        <div class="a-inner-ring"></div>
        <div class="a-reflection"></div>
        <div class="a-highlight"></div>
        <span class="a-label">A</span>
    </div>
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
    --btn-size: 88px;
    --accent-glow: rgba(0, 240, 255, 0.6);
    --accent-core: #00e5ff;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: radial-gradient(circle at 50% 50%, #151821 0%, #0d0e14 70%, #050608 100%);
    border: 2px solid #2a2e3d;
    box-shadow: 
      0 10px 24px rgba(0, 0, 0, 0.65),
      0 0 0 3px rgba(20, 22, 30, 0.9),
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
      conic-gradient(from 180deg at 50% 50%, #1a1d26, #2d3242, #14161f, #2d3242, #1a1d26);
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
      linear-gradient(145deg, #0052cc 0%, #002b80 50%, #001440 100%);
    border: 1.5px solid rgba(0, 229, 255, 0.5);
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
  <div class="nexpad-btn">
    <div class="btn-core">
      <span class="btn-label">A</span>
    </div>
  </div>
</body>
</html>
""".trimIndent()
}
