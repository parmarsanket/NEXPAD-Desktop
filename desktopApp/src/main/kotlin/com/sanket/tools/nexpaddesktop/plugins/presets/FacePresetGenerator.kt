package com.sanket.tools.nexpaddesktop.plugins.presets

import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle

internal object FacePresetGenerator {

    internal fun facePresetXboxElite(
        control: String,
        label: String,
        accent: String,
        glow: String,
        name: String,
        seed: Int
    ): String {
        val motion = retroMotion("0.68", "440", "0.94")
        val selector = ".nexpad-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent-core: $accent;
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 96px;
  height: 96px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.85),
    0 0 24px var(--accent-glow),
    inset 0 2px 3px rgba(255, 255, 255, 0.15),
    inset 0 -6px 12px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector:active {
  transform: scale(var(--press-scale));
}
$selector .glow-ring {
  position: absolute;
  left: 4px;
  top: 4px;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  box-shadow: 0 0 26px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .socket-well {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, #0c0e12 0%, #050608 100%);
  box-shadow:
    inset 0 6px 12px rgba(0, 0, 0, 0.95),
    inset 0 -2px 4px rgba(255, 255, 255, 0.08);
}
$selector .face-plate {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 40% 30%, rgba(255, 255, 255, 0.18) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, #16241d 0%, #0c1410 55%, #050806 100%);
  box-shadow:
    0 4px 10px rgba(0, 0, 0, 0.75),
    inset 0 2px 3px rgba(255, 255, 255, 0.45),
    inset 0 -5px 10px rgba(0, 0, 0, 0.90);
}
$selector .specular-arc {
  position: absolute;
  left: 21px;
  top: 14px;
  width: 54px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.75) 0%, rgba(255, 255, 255, 0.15) 50%, transparent 100%);
  pointer-events: none;
}
$selector .caustic-glow {
  position: absolute;
  left: 24px;
  top: 62px;
  width: 48px;
  height: 18px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 100%, var(--accent-glow) 0%, transparent 70%);
  pointer-events: none;
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 31px;
  width: 96px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  font-size: 34px;
  font-weight: 900;
  color: var(--accent-core);
  text-shadow:
    0 4px 10px rgba(0, 0, 0, 0.95),
    0 0 14px var(--accent-glow),
    0 0 28px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-codename="Xbox Elite" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="specular-arc"></div>
    <div class="caustic-glow"></div>
    <span class="btn-label">$label</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun facePresetArcade(
        control: String,
        label: String,
        accent: String,
        glow: String,
        name: String,
        seed: Int
    ): String {
        val motion = retroMotion("0.65", "460", "0.93")
        val selector = ".nexpad-btn"
        val active = centeredPress(selector)
        val domeColors = when (accent.uppercase()) {
            "#00FFA3", "#4ADE80", "#10B981" -> Pair("#4ade80", "#064e3b")
            "#FF2A6D", "#FF3366", "#F43F5E", "#EF4444", "#DC2626" -> Pair("#f87171", "#7f1d1d")
            "#00E5FF", "#00B0FF", "#0284C7" -> Pair("#38bdf8", "#082f49")
            "#FFD600", "#FFCC00", "#F59E0B" -> Pair("#fde047", "#713f12")
            else -> Pair("#f87171", "#7f1d1d")
        }

        val css = """
$motion
:root {
  --accent-core: $accent;
  --accent: $accent;
  --accent-glow: $glow;
  --dome-light: ${domeColors.first};
  --dome-dark: ${domeColors.second};
}
$selector {
  position: relative;
  width: 96px;
  height: 96px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.85),
    0 0 22px var(--accent-glow),
    inset 0 2px 2px rgba(255, 255, 255, 0.25),
    inset 0 -5px 10px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector:active {
  transform: scale(var(--press-scale));
}
$selector .glow-ring {
  position: absolute;
  left: 4px;
  top: 4px;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  box-shadow: 0 0 24px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .socket-well {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, #181818 0%, #060606 100%);
  box-shadow: inset 0 5px 10px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 11px;
  top: 11px;
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: radial-gradient(circle at 38% 28%, var(--dome-light) 0%, var(--accent-core) 50%, var(--dome-dark) 100%);
  box-shadow:
    0 4px 14px rgba(0, 0, 0, 0.70),
    inset 0 3px 4px rgba(255, 255, 255, 0.55),
    inset 0 -6px 12px rgba(0, 0, 0, 0.70);
}
$selector .arcade-sheen {
  position: absolute;
  left: 21px;
  top: 14px;
  width: 54px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.80) 0%, rgba(255, 255, 255, 0.20) 50%, transparent 100%);
  pointer-events: none;
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 31px;
  width: 96px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  font-size: 34px;
  font-weight: 900;
  color: #ffffff;
  text-shadow:
    0 2px 5px rgba(0, 0, 0, 0.75),
    0 -1px 1px rgba(0, 0, 0, 0.40);
}
$active
""".trimIndent()

        val body = """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-codename="Candy Cab Arcade" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="arcade-sheen"></div>
    <span class="btn-label">$label</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun facePresetCyberpunk(
        control: String,
        label: String,
        accent: String,
        glow: String,
        name: String,
        seed: Int
    ): String {
        val motion = retroMotion("0.68", "440", "0.94")
        val selector = ".nexpad-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent-core: $accent;
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 96px;
  height: 96px;
  padding: 0;
  border: 1.5px solid var(--accent-glow);
  outline: none;
  border-radius: 20px;
  background: linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.90),
    0 0 25px var(--accent-glow),
    inset 0 1px 2px rgba(255, 255, 255, 0.15),
    inset 0 -4px 8px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  font-family: 'Courier New', Courier, monospace;
  cursor: pointer;
  overflow: visible;
}
$selector:active {
  transform: scale(var(--press-scale));
}
$selector .glow-ring {
  position: absolute;
  left: 4px;
  top: 4px;
  width: 88px;
  height: 88px;
  border-radius: 18px;
  box-shadow: 0 0 24px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .face-plate {
  position: absolute;
  left: 9px;
  top: 9px;
  width: 78px;
  height: 78px;
  border-radius: 14px;
  border: 1px solid rgba(0, 229, 255, 0.35);
  background:
    linear-gradient(180deg, rgba(6, 182, 212, 0.10) 0%, transparent 50%),
    linear-gradient(145deg, #131720 0%, #090c12 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.15),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
}
$selector .cyber-grid {
  position: absolute;
  left: 17px;
  top: 17px;
  width: 62px;
  height: 62px;
  border: 1px dashed rgba(6, 182, 212, 0.22);
  border-radius: 10px;
  pointer-events: none;
}
$selector .button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 96px;
  height: 96px;
  pointer-events: none;
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 31px;
  width: 96px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  font-size: 34px;
  font-weight: 900;
  color: var(--accent-core);
  text-shadow:
    0 0 8px var(--accent-core),
    0 0 20px var(--accent-glow),
    0 0 36px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-codename="Cyberpunk" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cyber-grid"></div>
    <svg class="button-emblem" viewBox="0 0 96 96" xmlns="http://www.w3.org/2000/svg">
      <path d="M 16 28 L 16 16 L 28 16" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
      <path d="M 80 68 L 80 80 L 68 80" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
    </svg>
    <span class="btn-label">$label</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun facePresetSatin(
        control: String,
        label: String,
        accent: String,
        glow: String,
        name: String,
        seed: Int
    ): String {
        val motion = retroMotion("0.70", "420", "0.95")
        val selector = ".nexpad-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent-core: $accent;
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 96px;
  height: 96px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.80),
    0 0 18px rgba(148, 163, 184, 0.20),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.20),
    inset 0 -5px 10px rgba(0, 0, 0, 0.90);
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector:active {
  transform: scale(var(--press-scale));
}
$selector .glow-ring {
  position: absolute;
  left: 4px;
  top: 4px;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(148, 163, 184, 0.22);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .socket-well {
  position: absolute;
  left: 7px;
  top: 7px;
  width: 82px;
  height: 82px;
  border-radius: 50%;
  background: #080a0e;
  box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 11px;
  top: 11px;
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.65),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.22),
    inset 0 -4px 8px rgba(0, 0, 0, 0.70);
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 31px;
  width: 96px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  font-size: 34px;
  font-weight: 700;
  color: #e2e8f0;
  letter-spacing: -0.5px;
  text-shadow:
    0 -1px 1px rgba(0, 0, 0, 0.85),
    0 1px 1px rgba(255, 255, 255, 0.20);
}
$active
""".trimIndent()

        val body = """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-codename="Satin DualSense" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <span class="btn-label">$label</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun facePreset(
        control: String,
        label: String,
        accent: String,
        glow: String,
        name: String,
        seed: Int,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String = when (style) {
        NxprcPresetStyle.XBOX_ELITE -> facePresetXboxElite(control, label, accent, glow, name, seed)
        NxprcPresetStyle.ARCADE -> facePresetArcade(control, label, accent, glow, name, seed)
        NxprcPresetStyle.CYBERPUNK -> facePresetCyberpunk(control, label, accent, glow, name, seed)
        NxprcPresetStyle.SATIN -> facePresetSatin(control, label, accent, glow, name, seed)
    }
}
