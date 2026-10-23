package com.sanket.tools.nexpaddesktop.plugins.presets

import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle

internal object BumperPresetGenerator {

    internal fun bumperPresetXboxElite(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.72", "480", "0.95")
        val selector = ".bumper-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 120px;
  height: 52px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 17px;
  background: radial-gradient(ellipse at 50% 30%, #242a34 0%, #11141a 70%, #06080a 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.85),
    0 0 20px var(--accent-glow),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.20),
    inset 0 -4px 8px rgba(0, 0, 0, 0.90);
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
  left: 4px;
  top: 4px;
  width: 112px;
  height: 44px;
  border-radius: 14px;
  background: #0c0e12;
  box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 108px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(180deg, #242a34 0%, #141720 60%, #0a0c10 100%);
  box-shadow:
    0 2px 6px rgba(0, 0, 0, 0.70),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.30),
    inset 0 -3px 6px rgba(0, 0, 0, 0.85);
}
$selector .bumper-sheen {
  position: absolute;
  left: 12%;
  top: 10%;
  width: 76%;
  height: 35%;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.50) 0%, transparent 80%);
  pointer-events: none;
}
$selector .bumper-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 2px;
  color: var(--accent);
  text-shadow:
    0 3px 8px rgba(0, 0, 0, 0.95),
    0 0 14px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-codename="Xbox Elite" data-name="Xbox Elite Bumper $control" data-seed="48200" id="rc.${control.lowercase()}_48200">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="bumper-sheen"></div>
    <span class="bumper-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun bumperPresetArcade(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.70", "480", "0.94")
        val selector = ".bumper-btn"
        val active = centeredPress(selector)
        val right = control.uppercase() == "RB"
        val domeLight = if (right) "#f87171" else "#4ade80"
        val domeDark = if (right) "#7f1d1d" else "#064e3b"

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
  width: 120px;
  height: 52px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 17px;
  background: linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.85),
    0 0 20px var(--accent-glow),
    inset 0 2px 2px rgba(255, 255, 255, 0.25),
    inset 0 -4px 8px rgba(0, 0, 0, 0.95);
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
  left: 4px;
  top: 4px;
  width: 112px;
  height: 44px;
  border-radius: 14px;
  background: radial-gradient(circle at 50% 50%, #181818 0%, #060606 100%);
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 108px;
  height: 40px;
  border-radius: 12px;
  background: radial-gradient(ellipse at 40% 30%, var(--dome-light) 0%, var(--accent) 50%, var(--dome-dark) 100%);
  box-shadow:
    0 3px 10px rgba(0, 0, 0, 0.70),
    inset 0 2px 3px rgba(255, 255, 255, 0.55),
    inset 0 -4px 8px rgba(0, 0, 0, 0.70);
}
$selector .arcade-sheen {
  position: absolute;
  left: 14px;
  top: 8px;
  width: 92px;
  height: 16px;
  border-radius: 8px 8px 50% 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.80) 0%, rgba(255, 255, 255, 0.20) 50%, transparent 100%);
  pointer-events: none;
}
$selector .bumper-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 2px;
  color: #ffffff;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.75),
    0 -1px 1px rgba(0, 0, 0, 0.40);
}
$active
""".trimIndent()

        val body = """
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-codename="Candy Cab Arcade" data-name="Candy Cab Arcade Bumper $control" data-seed="48200" id="rc.${control.lowercase()}_48200">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="arcade-sheen"></div>
    <span class="bumper-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun bumperPresetCyberpunk(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.70", "460", "0.94")
        val selector = ".bumper-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 120px;
  height: 52px;
  padding: 0;
  border: 1.5px solid var(--accent-glow);
  outline: none;
  border-radius: 17px;
  background: linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.90),
    0 0 22px var(--accent-glow),
    inset 0 1px 2px rgba(255, 255, 255, 0.15),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
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
  left: 5px;
  top: 5px;
  width: 110px;
  height: 42px;
  border-radius: 12px;
  border: 1px solid rgba(0, 229, 255, 0.35);
  background:
    linear-gradient(180deg, rgba(6, 182, 212, 0.10) 0%, transparent 60%),
    linear-gradient(145deg, #131720 0%, #090c12 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.15),
    inset 0 -2px 4px rgba(0, 0, 0, 0.80);
}
$selector .button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 120px;
  height: 52px;
  pointer-events: none;
}
$selector .bumper-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 2px;
  color: var(--accent);
  text-shadow:
    0 0 8px var(--accent),
    0 0 20px var(--accent-glow),
    0 0 36px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-codename="Cyberpunk" data-name="Cyberpunk Bumper $control" data-seed="48200" id="rc.${control.lowercase()}_48200">
    <div class="face-plate" data-layer-role="base"></div>
    <svg class="button-emblem" viewBox="0 0 120 52" xmlns="http://www.w3.org/2000/svg">
      <path d="M 12 18 L 12 10 L 22 10" stroke="$accent" stroke-width="2" fill="none" stroke-linecap="round"/>
      <path d="M 108 34 L 108 42 L 98 42" stroke="$accent" stroke-width="2" fill="none" stroke-linecap="round"/>
    </svg>
    <span class="bumper-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun bumperPresetSatin(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.72", "440", "0.95")
        val selector = ".bumper-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 120px;
  height: 52px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 17px;
  background: linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.80),
    0 0 16px rgba(148, 163, 184, 0.20),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.20),
    inset 0 -4px 8px rgba(0, 0, 0, 0.90);
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
  left: 4px;
  top: 4px;
  width: 112px;
  height: 44px;
  border-radius: 14px;
  background: #080a0e;
  box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 108px;
  height: 40px;
  border-radius: 12px;
  background: radial-gradient(ellipse at 45% 35%, #383e4c 0%, #222630 55%, #13161c 100%);
  box-shadow:
    0 2px 6px rgba(0, 0, 0, 0.65),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.22),
    inset 0 -3px 6px rgba(0, 0, 0, 0.70);
}
$selector .bumper-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #e2e8f0;
  text-shadow:
    0 -1px 1px rgba(0, 0, 0, 0.85),
    0 1px 1px rgba(255, 255, 255, 0.20);
}
$active
""".trimIndent()

        val body = """
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-codename="Satin DualSense" data-name="Satin DualSense Bumper $control" data-seed="48200" id="rc.${control.lowercase()}_48200">
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <span class="bumper-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun bumperPreset(
        control: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val right = control.uppercase() == "RB"
        val accent = if (right) "#DC2626" else "#10B981"
        val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(16, 185, 129, 0.45)"
        return when (style) {
            NxprcPresetStyle.XBOX_ELITE -> bumperPresetXboxElite(control, accent, glow)
            NxprcPresetStyle.ARCADE -> bumperPresetArcade(control, accent, glow)
            NxprcPresetStyle.CYBERPUNK -> bumperPresetCyberpunk(control, accent, glow)
            NxprcPresetStyle.SATIN -> bumperPresetSatin(control, accent, glow)
        }
    }
}
