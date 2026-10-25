package com.sanket.tools.nexpaddesktop.plugins.presets

import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle

internal object DpadPresetGenerator {

    internal fun dpadDirectionPresetXboxElite(control: String, glyph: String, accent: String, glow: String): String {
        val motion = retroMotion("0.68", "460", "0.94")
        val selector = ".dpad-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 88px;
  height: 88px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 18px;
  background: radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.85),
    0 0 20px var(--accent-glow),
    inset 0 2px 3px rgba(255, 255, 255, 0.15),
    inset 0 -5px 10px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
$selector .socket-well {
  position: absolute;
  left: 5px;
  top: 5px;
  width: 78px;
  height: 78px;
  border-radius: 15px;
  background: radial-gradient(circle at 50% 50%, #0c0e12 0%, #050608 100%);
  box-shadow: inset 0 5px 10px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 9px;
  top: 9px;
  width: 70px;
  height: 70px;
  border-radius: 12px;
  background:
    radial-gradient(circle at 40% 30%, rgba(255, 255, 255, 0.18) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, #16241d 0%, #0c1410 55%, #050806 100%);
  box-shadow:
    0 3px 8px rgba(0, 0, 0, 0.75),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.40),
    inset 0 -4px 8px rgba(0, 0, 0, 0.85);
}
$selector .specular-arc {
  position: absolute;
  left: 17px;
  top: 12px;
  width: 54px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.70) 0%, transparent 100%);
  pointer-events: none;
}
$selector .dpad-glyph {
  position: relative;
  z-index: 5;
  font-size: 34px;
  color: var(--accent);
  text-shadow:
    0 3px 8px rgba(0, 0, 0, 0.95),
    0 0 14px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-codename="Xbox Elite" data-name="Xbox Elite D-Pad $control" data-seed="33100" id="rc.dpad_${control.lowercase()}">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="specular-arc"></div>
    <span class="dpad-glyph">$glyph</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadDirectionPresetArcade(control: String, glyph: String, accent: String, glow: String): String {
        val motion = retroMotion("0.65", "460", "0.93")
        val selector = ".dpad-btn"
        val active = centeredPress(selector)
        val domeColors = Pair("#38bdf8", "#082f49")

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
  --dome-light: ${domeColors.first};
  --dome-dark: ${domeColors.second};
}
$selector {
  position: relative;
  width: 88px;
  height: 88px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 18px;
  background: linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.85),
    0 0 20px var(--accent-glow),
    inset 0 2px 2px rgba(255, 255, 255, 0.25),
    inset 0 -5px 10px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
$selector .socket-well {
  position: absolute;
  left: 5px;
  top: 5px;
  width: 78px;
  height: 78px;
  border-radius: 15px;
  background: radial-gradient(circle at 50% 50%, #181818 0%, #060606 100%);
  box-shadow: inset 0 5px 10px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 9px;
  top: 9px;
  width: 70px;
  height: 70px;
  border-radius: 12px;
  background: radial-gradient(circle at 38% 28%, var(--dome-light) 0%, #0284c7 50%, var(--dome-dark) 100%);
  box-shadow:
    0 3px 10px rgba(0, 0, 0, 0.70),
    inset 0 2.5px 3px rgba(255, 255, 255, 0.55),
    inset 0 -5px 10px rgba(0, 0, 0, 0.70);
}
$selector .arcade-sheen {
  position: absolute;
  left: 17px;
  top: 12px;
  width: 54px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.80) 0%, rgba(255, 255, 255, 0.20) 50%, transparent 100%);
  pointer-events: none;
}
$selector .dpad-glyph {
  position: relative;
  z-index: 5;
  font-size: 34px;
  color: #ffffff;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.75),
    0 -1px 1px rgba(0, 0, 0, 0.40);
}
$active
""".trimIndent()

        val body = """
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-codename="Candy Cab Arcade" data-name="Candy Cab Arcade D-Pad $control" data-seed="33100" id="rc.dpad_${control.lowercase()}">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="arcade-sheen"></div>
    <span class="dpad-glyph">$glyph</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadDirectionPresetCyberpunk(control: String, glyph: String, accent: String, glow: String): String {
        val motion = retroMotion("0.68", "440", "0.94")
        val selector = ".dpad-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 88px;
  height: 88px;
  padding: 0;
  border: 1.5px solid var(--accent-glow);
  outline: none;
  border-radius: 18px;
  background: linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.90),
    0 0 22px var(--accent-glow),
    inset 0 1px 2px rgba(255, 255, 255, 0.15),
    inset 0 -4px 8px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  font-family: 'Courier New', Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
$selector .face-plate {
  position: absolute;
  left: 7px;
  top: 7px;
  width: 74px;
  height: 74px;
  border-radius: 12px;
  border: 1px solid rgba(0, 229, 255, 0.35);
  background:
    linear-gradient(180deg, rgba(6, 182, 212, 0.10) 0%, transparent 50%),
    linear-gradient(145deg, #131720 0%, #090c12 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.15),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
}
$selector .button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 88px;
  height: 88px;
  pointer-events: none;
}
$selector .dpad-glyph {
  position: relative;
  z-index: 5;
  font-size: 34px;
  color: var(--accent);
  text-shadow:
    0 0 8px var(--accent),
    0 0 20px var(--accent-glow),
    0 0 36px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-codename="Cyberpunk" data-name="Cyberpunk D-Pad $control" data-seed="33100" id="rc.dpad_${control.lowercase()}">
    <div class="face-plate" data-layer-role="base"></div>
    <svg class="button-emblem" viewBox="0 0 88 88" xmlns="http://www.w3.org/2000/svg">
      <path d="M 14 24 L 14 14 L 24 14" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
      <path d="M 74 64 L 74 74 L 64 74" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
    </svg>
    <span class="dpad-glyph">$glyph</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadDirectionPresetSatin(control: String, glyph: String, accent: String, glow: String): String {
        val motion = retroMotion("0.70", "420", "0.95")
        val selector = ".dpad-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 88px;
  height: 88px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 18px;
  background: linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.80),
    0 0 16px rgba(148, 163, 184, 0.20),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.20),
    inset 0 -5px 10px rgba(0, 0, 0, 0.90);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
$selector .socket-well {
  position: absolute;
  left: 5px;
  top: 5px;
  width: 78px;
  height: 78px;
  border-radius: 15px;
  background: #080a0e;
  box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 9px;
  top: 9px;
  width: 70px;
  height: 70px;
  border-radius: 12px;
  background: radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%);
  box-shadow:
    0 3px 8px rgba(0, 0, 0, 0.65),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.22),
    inset 0 -4px 8px rgba(0, 0, 0, 0.70);
}
$selector .dpad-glyph {
  position: relative;
  z-index: 5;
  font-size: 34px;
  color: #e2e8f0;
  text-shadow:
    0 -1px 1px rgba(0, 0, 0, 0.85),
    0 1px 1px rgba(255, 255, 255, 0.20);
}
$active
""".trimIndent()

        val body = """
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-codename="Satin DualSense" data-name="Satin DualSense D-Pad $control" data-seed="33100" id="rc.dpad_${control.lowercase()}">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <span class="dpad-glyph">$glyph</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadDirectionPreset(
        control: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val glyph = dpadGlyph(control)
        val (accent, glow) = when (control.uppercase()) {
            "DOWN", "RIGHT" -> Pair("#DC2626", "rgba(220, 38, 38, 0.45)")
            else -> Pair("#10B981", "rgba(16, 185, 129, 0.45)")
        }
        return when (style) {
            NxprcPresetStyle.XBOX_ELITE -> dpadDirectionPresetXboxElite(control, glyph, accent, glow)
            NxprcPresetStyle.ARCADE -> dpadDirectionPresetArcade(control, glyph, accent, glow)
            NxprcPresetStyle.CYBERPUNK -> dpadDirectionPresetCyberpunk(control, glyph, accent, glow)
            NxprcPresetStyle.SATIN -> dpadDirectionPresetSatin(control, glyph, accent, glow)
        }
    }

    internal fun dpadCrossPresetXboxElite(): String {
        val motion = retroMotion("0.68", "460", "0.95")
        val selector = ".dpad-cross"
        val active = centeredPress(selector)
        val accent = "#10B981"
        val glow = "rgba(16, 185, 129, 0.45)"

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 160px;
  height: 160px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 36px;
  background: radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%);
  box-shadow:
    0 14px 32px rgba(0, 0, 0, 0.85),
    0 0 24px var(--accent-glow),
    inset 0 2px 3px rgba(255, 255, 255, 0.15),
    inset 0 -6px 14px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector .glow-ring {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 148px;
  height: 148px;
  border-radius: 32px;
  box-shadow: 0 0 26px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .socket-well {
  position: absolute;
  left: 8px;
  top: 8px;
  width: 144px;
  height: 144px;
  border-radius: 30px;
  background: radial-gradient(circle at 50% 50%, #0c0e12 0%, #050608 100%);
  box-shadow: inset 0 6px 14px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 136px;
  height: 136px;
  border-radius: 26px;
  background:
    radial-gradient(circle at 40% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, #16241d 0%, #0c1410 55%, #050806 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.75),
    inset 0 2px 3px rgba(255, 255, 255, 0.35),
    inset 0 -5px 10px rgba(0, 0, 0, 0.90);
}
$selector .cross-center {
  position: absolute;
  left: 56px;
  top: 56px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #1a2029 0%, #0a0d12 100%);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.90);
}
$selector .dpad-arrow {
  position: absolute;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  font-size: 22px;
  font-weight: 900;
  color: var(--accent);
  text-shadow:
    0 3px 8px rgba(0, 0, 0, 0.95),
    0 0 14px var(--accent-glow);
  z-index: 5;
}
$selector .dpad-up    { left: 66px; top: 16px; }
$selector .dpad-right { left: 116px; top: 66px; }
$selector .dpad-down  { left: 66px; top: 116px; }
$selector .dpad-left  { left: 16px; top: 66px; }
$active
""".trimIndent()

        val body = """
  <button class="dpad-cross" data-control="DPAD" data-category="DPAD" data-codename="Xbox Elite" data-name="Xbox Elite D-Pad Cross" data-seed="31000" id="rc.dpad_cross">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cross-center" data-layer-role="detail"></div>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadCrossPresetArcade(): String {
        val motion = retroMotion("0.65", "460", "0.93")
        val selector = ".dpad-cross"
        val active = centeredPress(selector)
        val accent = "#00FFA3"
        val glow = "rgba(0, 255, 163, 0.42)"
        val domeLight = "#4ade80"
        val domeDark = "#064e3b"

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
  --dome-light: $domeLight;
  --dome-dark: $domeDark;
}
$selector {
  position: relative;
  width: 160px;
  height: 160px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 36px;
  background: linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%);
  box-shadow:
    0 14px 32px rgba(0, 0, 0, 0.85),
    0 0 22px var(--accent-glow),
    inset 0 2px 2px rgba(255, 255, 255, 0.25),
    inset 0 -6px 12px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector .socket-well {
  position: absolute;
  left: 8px;
  top: 8px;
  width: 144px;
  height: 144px;
  border-radius: 30px;
  background: radial-gradient(circle at 50% 50%, #181818 0%, #060606 100%);
  box-shadow: inset 0 6px 12px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 136px;
  height: 136px;
  border-radius: 26px;
  background: radial-gradient(circle at 38% 28%, var(--dome-light) 0%, #10b981 50%, var(--dome-dark) 100%);
  box-shadow:
    0 4px 14px rgba(0, 0, 0, 0.70),
    inset 0 3px 4px rgba(255, 255, 255, 0.55),
    inset 0 -6px 12px rgba(0, 0, 0, 0.70);
}
$selector .cross-center {
  position: absolute;
  left: 56px;
  top: 56px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: radial-gradient(circle at 38% 28%, #1f2937 0%, #111827 100%);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.80);
}
$selector .dpad-arrow {
  position: absolute;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  font-size: 22px;
  font-weight: 900;
  color: #ffffff;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.75),
    0 -1px 1px rgba(0, 0, 0, 0.40);
  z-index: 5;
}
$selector .dpad-up    { left: 66px; top: 16px; }
$selector .dpad-right { left: 116px; top: 66px; }
$selector .dpad-down  { left: 66px; top: 116px; }
$selector .dpad-left  { left: 16px; top: 66px; }
$active
""".trimIndent()

        val body = """
  <button class="dpad-cross" data-control="DPAD" data-category="DPAD" data-codename="Candy Cab Arcade" data-name="Candy Cab Arcade D-Pad Cross" data-seed="31000" id="rc.dpad_cross">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cross-center" data-layer-role="detail"></div>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadCrossPresetCyberpunk(): String {
        val motion = retroMotion("0.68", "440", "0.94")
        val selector = ".dpad-cross"
        val active = centeredPress(selector)
        val accent = "#00FFA3"
        val glow = "rgba(0, 255, 163, 0.42)"

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 160px;
  height: 160px;
  padding: 0;
  border: 1.5px solid var(--accent-glow);
  outline: none;
  border-radius: 36px;
  background: linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%);
  box-shadow:
    0 14px 32px rgba(0, 0, 0, 0.90),
    0 0 25px var(--accent-glow),
    inset 0 1px 2px rgba(255, 255, 255, 0.15),
    inset 0 -4px 8px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  font-family: 'Courier New', Courier, monospace;
  cursor: pointer;
  overflow: visible;
}
$selector .face-plate {
  position: absolute;
  left: 10px;
  top: 10px;
  width: 140px;
  height: 140px;
  border-radius: 28px;
  border: 1px solid rgba(0, 229, 255, 0.35);
  background:
    linear-gradient(180deg, rgba(6, 182, 212, 0.10) 0%, transparent 50%),
    linear-gradient(145deg, #131720 0%, #090c12 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.15),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
}
$selector .cross-center {
  position: absolute;
  left: 56px;
  top: 56px;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: 1px dashed rgba(0, 229, 255, 0.35);
}
$selector .button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 160px;
  height: 160px;
  pointer-events: none;
}
$selector .dpad-arrow {
  position: absolute;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  font-size: 22px;
  font-weight: 900;
  color: var(--accent);
  text-shadow:
    0 0 8px var(--accent),
    0 0 20px var(--accent-glow),
    0 0 36px var(--accent-glow);
  z-index: 5;
}
$selector .dpad-up    { left: 66px; top: 16px; }
$selector .dpad-right { left: 116px; top: 66px; }
$selector .dpad-down  { left: 66px; top: 116px; }
$selector .dpad-left  { left: 16px; top: 66px; }
$active
""".trimIndent()

        val body = """
  <button class="dpad-cross" data-control="DPAD" data-category="DPAD" data-codename="Cyberpunk" data-name="Cyberpunk D-Pad Cross" data-seed="31000" id="rc.dpad_cross">
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cross-center" data-layer-role="detail"></div>
    <svg class="button-emblem" viewBox="0 0 160 160" xmlns="http://www.w3.org/2000/svg">
      <path d="M 20 34 L 20 20 L 34 20" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
      <path d="M 140 126 L 140 140 L 126 140" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
      <circle cx="80" cy="80" r="14" stroke="$accent" stroke-width="1.5" fill="none" stroke-dasharray="3,3"/>
    </svg>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadCrossPresetSatin(): String {
        val motion = retroMotion("0.70", "420", "0.95")
        val selector = ".dpad-cross"
        val active = centeredPress(selector)
        val accent = "#00FFA3"
        val glow = "rgba(148, 163, 184, 0.20)"

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 160px;
  height: 160px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 36px;
  background: linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%);
  box-shadow:
    0 14px 32px rgba(0, 0, 0, 0.80),
    0 0 18px rgba(148, 163, 184, 0.20),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.20),
    inset 0 -5px 10px rgba(0, 0, 0, 0.90);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector .socket-well {
  position: absolute;
  left: 8px;
  top: 8px;
  width: 144px;
  height: 144px;
  border-radius: 30px;
  background: #080a0e;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 136px;
  height: 136px;
  border-radius: 26px;
  background: radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.65),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.22),
    inset 0 -4px 8px rgba(0, 0, 0, 0.70);
}
$selector .cross-center {
  position: absolute;
  left: 56px;
  top: 56px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #222630 0%, #101217 100%);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.90);
}
$selector .dpad-arrow {
  position: absolute;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: #e2e8f0;
  text-shadow:
    0 -1px 1px rgba(0, 0, 0, 0.85),
    0 1px 1px rgba(255, 255, 255, 0.20);
  z-index: 5;
}
$selector .dpad-up    { left: 66px; top: 16px; }
$selector .dpad-right { left: 116px; top: 66px; }
$selector .dpad-down  { left: 66px; top: 116px; }
$selector .dpad-left  { left: 16px; top: 66px; }
$active
""".trimIndent()

        val body = """
  <button class="dpad-cross" data-control="DPAD" data-category="DPAD" data-codename="Satin DualSense" data-name="Satin DualSense D-Pad Cross" data-seed="31000" id="rc.dpad_cross">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cross-center" data-layer-role="detail"></div>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun dpadCrossPreset(
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String = when (style) {
        NxprcPresetStyle.XBOX_ELITE -> dpadCrossPresetXboxElite()
        NxprcPresetStyle.ARCADE -> dpadCrossPresetArcade()
        NxprcPresetStyle.CYBERPUNK -> dpadCrossPresetCyberpunk()
        NxprcPresetStyle.SATIN -> dpadCrossPresetSatin()
    }
}
