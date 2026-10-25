package com.sanket.tools.nexpaddesktop.plugins.presets

import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle

internal object TriggerPresetGenerator {

    internal fun triggerPresetXboxElite(control: String, accent: String, glow: String, right: Boolean): String {
        val motion = retroMotion("0.68", "440", "0.95")
        val selector = ".trigger-btn"
        val active = centeredPress(selector)
        val seed = if (right) 63400 else 62900
        val name = "Xbox Elite Trigger $control #$seed"

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 110px;
  height: 140px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 22px;
  background: radial-gradient(circle at 45% 35%, #242a34 0%, #11141a 65%, #06080a 100%);
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
$selector .glow-ring {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 98px;
  height: 128px;
  border-radius: 20px;
  box-shadow: 0 0 26px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .socket-well {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 98px;
  height: 128px;
  border-radius: 18px 18px 24px 24px;
  background: radial-gradient(circle at 50% 50%, #0d0f14 0%, #050608 100%);
  box-shadow:
    inset 0 6px 14px rgba(0, 0, 0, 0.95),
    inset 0 -2px 4px rgba(255, 255, 255, 0.08);
}
$selector .face-plate {
  position: absolute;
  left: 11px;
  top: 11px;
  width: 88px;
  height: 118px;
  border-radius: 14px 14px 20px 20px;
  background: linear-gradient(180deg, #222834 0%, #141720 55%, #0a0c10 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.80),
    inset 0 2px 3px rgba(255, 255, 255, 0.25),
    inset 0 -5px 10px rgba(0, 0, 0, 0.90);
}
$selector .specular-arc {
  position: absolute;
  left: 20px;
  top: 15px;
  width: 70px;
  height: 26px;
  border-radius: 14px 14px 50% 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.70) 0%, rgba(255, 255, 255, 0.12) 60%, transparent 100%);
  pointer-events: none;
}
$selector .elite-grip {
  position: absolute;
  left: 20px;
  top: 76px;
  width: 70px;
  height: 42px;
  border-radius: 8px 8px 14px 14px;
  background: repeating-linear-gradient(
    45deg,
    rgba(0, 0, 0, 0.45) 0px,
    rgba(0, 0, 0, 0.45) 2px,
    transparent 2px,
    transparent 6px
  );
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.85);
}
$selector .caustic-glow {
  position: absolute;
  left: 26px;
  top: 114px;
  width: 58px;
  height: 10px;
  border-radius: 5px;
  background: radial-gradient(ellipse at 50% 100%, var(--accent-glow) 0%, transparent 70%);
  pointer-events: none;
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 36px;
  width: 110px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  font-size: 32px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--accent);
  text-shadow:
    0 4px 10px rgba(0, 0, 0, 0.95),
    0 0 14px var(--accent-glow),
    0 0 28px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-codename="Xbox Elite" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="specular-arc"></div>
    <div class="elite-grip"></div>
    <div class="caustic-glow"></div>
    <span class="btn-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun triggerPresetArcade(control: String, accent: String, glow: String, right: Boolean): String {
        val motion = retroMotion("0.65", "460", "0.93")
        val selector = ".trigger-btn"
        val active = centeredPress(selector)
        val seed = if (right) 63400 else 62900
        val name = "Candy Cab Arcade Trigger $control #$seed"
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
  width: 110px;
  height: 140px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 22px;
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
$selector .glow-ring {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 98px;
  height: 128px;
  border-radius: 20px;
  box-shadow: 0 0 24px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .socket-well {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 98px;
  height: 128px;
  border-radius: 18px;
  background: radial-gradient(circle at 50% 50%, #181818 0%, #060606 100%);
  box-shadow: inset 0 5px 10px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 10px;
  top: 10px;
  width: 90px;
  height: 120px;
  border-radius: 14px 14px 20px 20px;
  background: radial-gradient(ellipse at 40% 30%, var(--dome-light) 0%, var(--accent) 50%, var(--dome-dark) 100%);
  box-shadow:
    0 4px 14px rgba(0, 0, 0, 0.70),
    inset 0 3px 4px rgba(255, 255, 255, 0.55),
    inset 0 -6px 12px rgba(0, 0, 0, 0.70);
}
$selector .arcade-sheen {
  position: absolute;
  left: 20px;
  top: 14px;
  width: 70px;
  height: 32px;
  border-radius: 14px 14px 50% 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.80) 0%, rgba(255, 255, 255, 0.20) 50%, transparent 100%);
  pointer-events: none;
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 48px;
  width: 110px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  font-size: 34px;
  font-weight: 900;
  letter-spacing: 1px;
  color: #ffffff;
  text-shadow:
    0 2px 5px rgba(0, 0, 0, 0.75),
    0 -1px 1px rgba(0, 0, 0, 0.40);
}
$active
""".trimIndent()

        val body = """
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-codename="Candy Cab Arcade" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="arcade-sheen"></div>
    <span class="btn-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun triggerPresetCyberpunk(control: String, accent: String, glow: String, right: Boolean): String {
        val motion = retroMotion("0.68", "440", "0.94")
        val selector = ".trigger-btn"
        val active = centeredPress(selector)
        val seed = if (right) 63400 else 62900
        val name = "Cyberpunk Trigger $control #$seed"

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 110px;
  height: 140px;
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
$selector .glow-ring {
  position: absolute;
  left: 4px;
  top: 4px;
  width: 102px;
  height: 132px;
  border-radius: 18px;
  box-shadow: 0 0 24px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .face-plate {
  position: absolute;
  left: 8px;
  top: 8px;
  width: 94px;
  height: 124px;
  border-radius: 14px;
  border: 1px solid rgba(0, 229, 255, 0.35);
  background:
    linear-gradient(180deg, rgba(6, 182, 212, 0.10) 0%, transparent 50%),
    linear-gradient(145deg, #131720 0%, #090c12 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.15),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
}
$selector .cyber-telemetry {
  position: absolute;
  left: 14px;
  top: 20px;
  width: 6px;
  height: 96px;
  border-radius: 3px;
  background: repeating-linear-gradient(
    180deg,
    var(--accent) 0px,
    var(--accent) 4px,
    transparent 4px,
    transparent 10px
  );
  box-shadow: 0 0 8px var(--accent-glow);
}
$selector .button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 110px;
  height: 140px;
  pointer-events: none;
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 46px;
  width: 110px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  font-size: 32px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--accent);
  text-shadow:
    0 0 8px var(--accent),
    0 0 20px var(--accent-glow),
    0 0 36px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-codename="Cyberpunk" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cyber-telemetry"></div>
    <svg class="button-emblem" viewBox="0 0 110 140" xmlns="http://www.w3.org/2000/svg">
      <path d="M 16 26 L 16 16 L 26 16" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
      <path d="M 94 114 L 94 124 L 84 124" stroke="$accent" stroke-width="2.5" fill="none" stroke-linecap="round"/>
    </svg>
    <span class="btn-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun triggerPresetSatin(control: String, accent: String, glow: String, right: Boolean): String {
        val motion = retroMotion("0.70", "420", "0.95")
        val selector = ".trigger-btn"
        val active = centeredPress(selector)
        val seed = if (right) 63400 else 62900
        val name = "Satin DualSense Trigger $control #$seed"

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 110px;
  height: 140px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 22px;
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
$selector .glow-ring {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 98px;
  height: 128px;
  border-radius: 20px;
  box-shadow: 0 0 20px rgba(148, 163, 184, 0.22);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .socket-well {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 98px;
  height: 128px;
  border-radius: 20px 20px 26px 26px;
  background: #080a0e;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 10px;
  top: 10px;
  width: 90px;
  height: 120px;
  border-radius: 16px 16px 22px 22px;
  background: radial-gradient(ellipse at 45% 35%, #383e4c 0%, #222630 55%, #13161c 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.65),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.22),
    inset 0 -4px 8px rgba(0, 0, 0, 0.70);
}
$selector .btn-label {
  position: absolute;
  left: 0;
  top: 48px;
  width: 110px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #e2e8f0;
  text-shadow:
    0 -1px 1px rgba(0, 0, 0, 0.85),
    0 1px 1px rgba(255, 255, 255, 0.20);
}
$active
""".trimIndent()

        val body = """
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-codename="Satin DualSense" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <span class="btn-label">$control</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun triggerPreset(
        control: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val right = control.uppercase() == "RT"
        val accent = if (right) "#DC2626" else "#10B981"
        val glow = if (right) "rgba(220, 38, 38, 0.55)" else "rgba(16, 185, 129, 0.55)"
        return when (style) {
            NxprcPresetStyle.XBOX_ELITE -> triggerPresetXboxElite(control, accent, glow, right)
            NxprcPresetStyle.ARCADE -> triggerPresetArcade(control, accent, glow, right)
            NxprcPresetStyle.CYBERPUNK -> triggerPresetCyberpunk(control, accent, glow, right)
            NxprcPresetStyle.SATIN -> triggerPresetSatin(control, accent, glow, right)
        }
    }
}
