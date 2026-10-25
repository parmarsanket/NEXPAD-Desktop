package com.sanket.tools.nexpaddesktop.plugins.presets

import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle

internal object JoystickPresetGenerator {

    internal fun joystickPresetXboxElite(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.68", "440", "0.94")
        val selector = ".stick-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 150px;
  height: 150px;
  padding: 0;
  border: none;
  background: transparent;
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector .stick-base {
  position: absolute;
  left: 0;
  top: 0;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.85),
    0 0 22px var(--accent-glow),
    inset 0 2px 3px rgba(255, 255, 255, 0.15),
    inset 0 -6px 12px rgba(0, 0, 0, 0.95);
  z-index: 1;
}
$selector .socket-well {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, #0c0e12 0%, #050608 100%);
  box-shadow: inset 0 6px 12px rgba(0, 0, 0, 0.95);
}
$selector .stick-cap {
  position: absolute;
  left: 26px;
  top: 26px;
  width: 98px;
  height: 98px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 40% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
    linear-gradient(160deg, #1c232d 0%, #11151c 55%, #080a0e 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.80),
    inset 0 1.5px 3px rgba(255, 255, 255, 0.30),
    inset 0 -5px 10px rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
$selector .knurled-ring {
  position: absolute;
  left: 16px;
  top: 16px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px dashed var(--accent);
  opacity: 0.70;
}
$selector .cap-gloss {
  position: absolute;
  left: 22px;
  top: 10px;
  width: 54px;
  height: 20px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.50) 0%, transparent 75%);
}
$selector .stick-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--accent);
  text-shadow:
    0 3px 8px rgba(0, 0, 0, 0.95),
    0 0 14px var(--accent-glow);
}
$active
""".trimIndent()

        val body = """
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-codename="Xbox Elite" data-name="Xbox Elite Analog Stick $control" data-seed="51200" id="rc.stick_${control.lowercase()}">
    <div class="stick-base" data-layer-role="base">
      <div class="socket-well" data-layer-role="base"></div>
    </div>
    <div class="stick-cap" data-layer-role="thumb-cap">
      <div class="knurled-ring" data-layer-role="thumb-cap"></div>
      <div class="cap-gloss" data-layer-role="thumb-cap"></div>
      <span class="stick-label">$control</span>
    </div>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun joystickPresetArcade(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.65", "460", "0.93")
        val selector = ".stick-btn"
        val active = centeredPress(selector)
        val right = control.uppercase() == "RS"
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
  width: 150px;
  height: 150px;
  padding: 0;
  border: none;
  background: transparent;
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector .stick-base {
  position: absolute;
  left: 0;
  top: 0;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.85),
    0 0 20px var(--accent-glow),
    inset 0 2px 2px rgba(255, 255, 255, 0.25),
    inset 0 -5px 10px rgba(0, 0, 0, 0.95);
  z-index: 1;
}
$selector .socket-well {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, #181818 0%, #060606 100%);
  box-shadow: inset 0 5px 10px rgba(0, 0, 0, 0.95);
}
$selector .stick-cap {
  position: absolute;
  left: 26px;
  top: 26px;
  width: 98px;
  height: 98px;
  border-radius: 50%;
  background: radial-gradient(circle at 38% 28%, var(--dome-light) 0%, var(--accent) 50%, var(--dome-dark) 100%);
  box-shadow:
    0 4px 14px rgba(0, 0, 0, 0.70),
    inset 0 3px 4px rgba(255, 255, 255, 0.55),
    inset 0 -6px 12px rgba(0, 0, 0, 0.70);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
$selector .knurled-ring {
  position: absolute;
  left: 16px;
  top: 16px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px dashed #ffffff;
  opacity: 0.55;
}
$selector .cap-gloss {
  position: absolute;
  left: 22px;
  top: 10px;
  width: 54px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.80) 0%, transparent 100%);
}
$selector .stick-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 1px;
  color: #ffffff;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.75),
    0 -1px 1px rgba(0, 0, 0, 0.40);
}
$active
""".trimIndent()

        val body = """
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-codename="Candy Cab Arcade" data-name="Candy Cab Arcade Analog Stick $control" data-seed="51200" id="rc.stick_${control.lowercase()}">
    <div class="stick-base" data-layer-role="base">
      <div class="socket-well" data-layer-role="base"></div>
    </div>
    <div class="stick-cap" data-layer-role="thumb-cap">
      <div class="knurled-ring" data-layer-role="thumb-cap"></div>
      <div class="cap-gloss" data-layer-role="thumb-cap"></div>
      <span class="stick-label">$control</span>
    </div>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun joystickPresetCyberpunk(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.68", "440", "0.94")
        val selector = ".stick-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 150px;
  height: 150px;
  padding: 0;
  border: none;
  background: transparent;
  transform-origin: 50% 50%;
  font-family: 'Courier New', Courier, monospace;
  cursor: pointer;
  overflow: visible;
}
$selector .stick-base {
  position: absolute;
  left: 0;
  top: 0;
  width: 150px;
  height: 150px;
  border-radius: 28px;
  border: 1.5px solid var(--accent-glow);
  background: linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.90),
    0 0 25px var(--accent-glow),
    inset 0 1px 2px rgba(255, 255, 255, 0.15);
  z-index: 1;
}
$selector .socket-well {
  position: absolute;
  left: 10px;
  top: 10px;
  width: 130px;
  height: 130px;
  border-radius: 22px;
  background: #090c12;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.85);
}
$selector .stick-cap {
  position: absolute;
  left: 26px;
  top: 26px;
  width: 98px;
  height: 98px;
  border-radius: 50%;
  border: 1px solid rgba(0, 229, 255, 0.35);
  background:
    linear-gradient(180deg, rgba(6, 182, 212, 0.10) 0%, transparent 60%),
    linear-gradient(145deg, #131720 0%, #090c12 100%);
  box-shadow:
    0 4px 10px rgba(0, 0, 0, 0.80),
    inset 0 1px 1px rgba(255, 255, 255, 0.15),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
$selector .knurled-ring {
  position: absolute;
  left: 16px;
  top: 16px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px dashed var(--accent);
  opacity: 0.65;
}
$selector .cap-gloss {
  position: absolute;
  left: 22px;
  top: 10px;
  width: 54px;
  height: 20px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(6, 182, 212, 0.40) 0%, transparent 75%);
}
$selector .stick-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
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
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-codename="Cyberpunk" data-name="Cyberpunk Analog Stick $control" data-seed="51200" id="rc.stick_${control.lowercase()}">
    <div class="stick-base" data-layer-role="base">
      <div class="socket-well" data-layer-role="base"></div>
    </div>
    <div class="stick-cap" data-layer-role="thumb-cap">
      <div class="knurled-ring" data-layer-role="thumb-cap"></div>
      <div class="cap-gloss" data-layer-role="thumb-cap"></div>
      <span class="stick-label">$control</span>
    </div>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun joystickPresetSatin(control: String, accent: String, glow: String): String {
        val motion = retroMotion("0.70", "420", "0.95")
        val selector = ".stick-btn"
        val active = centeredPress(selector)

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 150px;
  height: 150px;
  padding: 0;
  border: none;
  background: transparent;
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
}
$selector .stick-base {
  position: absolute;
  left: 0;
  top: 0;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.80),
    0 0 18px rgba(148, 163, 184, 0.20),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.20),
    inset 0 -5px 10px rgba(0, 0, 0, 0.90);
  z-index: 1;
}
$selector .socket-well {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: #080a0e;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.95);
}
$selector .stick-cap {
  position: absolute;
  left: 26px;
  top: 26px;
  width: 98px;
  height: 98px;
  border-radius: 50%;
  background: radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.65),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.22),
    inset 0 -4px 8px rgba(0, 0, 0, 0.70);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
$selector .knurled-ring {
  position: absolute;
  left: 16px;
  top: 16px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px dashed #94a3b8;
  opacity: 0.40;
}
$selector .cap-gloss {
  position: absolute;
  left: 22px;
  top: 10px;
  width: 54px;
  height: 20px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.25) 0%, transparent 75%);
}
$selector .stick-label {
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
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-codename="Satin DualSense" data-name="Satin DualSense Analog Stick $control" data-seed="51200" id="rc.stick_${control.lowercase()}">
    <div class="stick-base" data-layer-role="base">
      <div class="socket-well" data-layer-role="base"></div>
    </div>
    <div class="stick-cap" data-layer-role="thumb-cap">
      <div class="knurled-ring" data-layer-role="thumb-cap"></div>
      <div class="cap-gloss" data-layer-role="thumb-cap"></div>
      <span class="stick-label">$control</span>
    </div>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun joystickPreset(
        control: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val right = control.uppercase() == "RS"
        val accent = if (right) "#DC2626" else "#10B981"
        val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(16, 185, 129, 0.45)"
        return when (style) {
            NxprcPresetStyle.XBOX_ELITE -> joystickPresetXboxElite(control, accent, glow)
            NxprcPresetStyle.ARCADE -> joystickPresetArcade(control, accent, glow)
            NxprcPresetStyle.CYBERPUNK -> joystickPresetCyberpunk(control, accent, glow)
            NxprcPresetStyle.SATIN -> joystickPresetSatin(control, accent, glow)
        }
    }

    internal fun stickButtonPreset(
        control: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val right = control.uppercase() == "RSB"
        val accent = if (right) "#DC2626" else "#10B981"
        val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(16, 185, 129, 0.45)"
        val motion = retroMotion("0.68", "460", "0.93")
        val selector = ".stick-btn-ctl"
        val active = centeredPress(selector)

        val css = when (style) {
            NxprcPresetStyle.XBOX_ELITE -> """
$motion
:root { --accent: $accent; --accent-glow: $glow; }
$selector {
  position: relative; width: 70px; height: 70px; padding: 0; border: none; outline: none; border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.85), inset 0 2px 3px rgba(255, 255, 255, 0.15), inset 0 -4px 8px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; cursor: pointer; overflow: visible; display: flex; align-items: center; justify-content: center;
}
$selector .face-plate {
  position: relative; width: 52px; height: 52px; border-radius: 50%;
  background: radial-gradient(circle at 40% 30%, rgba(255, 255, 255, 0.18) 0%, transparent 50%), radial-gradient(circle at 50% 50%, #16241d 0%, #0c1410 55%, #050806 100%);
  box-shadow: inset 0 1.5px 2px rgba(255, 255, 255, 0.35), inset 0 -3px 6px rgba(0, 0, 0, 0.85); display: flex; align-items: center; justify-content: center;
}
$selector .stick-btn-label { position: relative; z-index: 5; font-size: 15px; font-weight: 900; letter-spacing: 1px; color: var(--accent); text-shadow: 0 2px 6px rgba(0, 0, 0, 0.95), 0 0 10px var(--accent-glow); }
$active
""".trimIndent()

            NxprcPresetStyle.ARCADE -> """
$motion
:root { --accent: $accent; --accent-glow: $glow; }
$selector {
  position: relative; width: 70px; height: 70px; padding: 0; border: none; outline: none; border-radius: 50%;
  background: linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.85), inset 0 2px 2px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.95);
  transform-origin: 50% 50%; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; cursor: pointer; overflow: visible; display: flex; align-items: center; justify-content: center;
}
$selector .face-plate {
  position: relative; width: 52px; height: 52px; border-radius: 50%;
  background: radial-gradient(circle at 38% 28%, #38bdf8 0%, #0284c7 50%, #082f49 100%);
  box-shadow: inset 0 2px 3px rgba(255, 255, 255, 0.55), inset 0 -4px 8px rgba(0, 0, 0, 0.70); display: flex; align-items: center; justify-content: center;
}
$selector .stick-btn-label { position: relative; z-index: 5; font-size: 15px; font-weight: 900; letter-spacing: 1px; color: #ffffff; text-shadow: 0 1px 3px rgba(0, 0, 0, 0.75); }
$active
""".trimIndent()

            NxprcPresetStyle.CYBERPUNK -> """
$motion
:root { --accent: $accent; --accent-glow: $glow; }
$selector {
  position: relative; width: 70px; height: 70px; padding: 0; border: 1.5px solid var(--accent-glow); outline: none; border-radius: 16px;
  background: linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.90), 0 0 18px var(--accent-glow);
  transform-origin: 50% 50%; font-family: 'Courier New', Courier, monospace; cursor: pointer; overflow: visible; display: flex; align-items: center; justify-content: center;
}
$selector .face-plate {
  position: relative; width: 52px; height: 52px; border-radius: 12px; border: 1px solid rgba(0, 229, 255, 0.35);
  background: linear-gradient(145deg, #131720 0%, #090c12 100%);
  display: flex; align-items: center; justify-content: center;
}
$selector .stick-btn-label { position: relative; z-index: 5; font-size: 15px; font-weight: 900; letter-spacing: 1px; color: var(--accent); text-shadow: 0 0 8px var(--accent), 0 0 20px var(--accent-glow); }
$active
""".trimIndent()

            NxprcPresetStyle.SATIN -> """
$motion
:root { --accent: $accent; --accent-glow: $glow; }
$selector {
  position: relative; width: 70px; height: 70px; padding: 0; border: none; outline: none; border-radius: 50%;
  background: linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.80), inset 0 1.5px 2px rgba(255, 255, 255, 0.20), inset 0 -4px 8px rgba(0, 0, 0, 0.90);
  transform-origin: 50% 50%; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; cursor: pointer; overflow: visible; display: flex; align-items: center; justify-content: center;
}
$selector .face-plate {
  position: relative; width: 52px; height: 52px; border-radius: 50%;
  background: radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%);
  box-shadow: inset 0 1.5px 2px rgba(255, 255, 255, 0.22), inset 0 -3px 6px rgba(0, 0, 0, 0.70); display: flex; align-items: center; justify-content: center;
}
$selector .stick-btn-label { position: relative; z-index: 5; font-size: 15px; font-weight: 700; letter-spacing: 1px; color: #e2e8f0; text-shadow: 0 -1px 1px rgba(0, 0, 0, 0.85); }
$active
""".trimIndent()
        }

        val body = """
  <button class="stick-btn-ctl" data-control="$control" data-category="BUTTON" data-codename="${style.displayName}" data-name="${style.displayName} Click $control" data-seed="21900" id="rc.click_${control.lowercase()}">
    <div class="face-plate" data-layer-role="base">
      <span class="stick-btn-label">$control</span>
    </div>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }

    internal fun touchpadPreset(
        control: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val right = control.uppercase() == "RTP"
        val accent = if (right) "#DC2626" else "#10B981"
        val glow = if (right) "rgba(220, 38, 38, 0.40)" else "rgba(16, 185, 129, 0.40)"
        val title = if (right) "Touch Look • RTP" else "Touch Move • LTP"
        val touchId = "touch_" + control.lowercase()
        val motion = retroMotion("0.75", "500", "0.98")
        val selector = ".touchpad-ctl"
        val active = centeredPress(selector)

        val config = when (style) {
            NxprcPresetStyle.XBOX_ELITE -> ElementStyleConfig(
                chassisBg = "radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%)",
                chassisShadow = "0 14px 32px rgba(0, 0, 0, 0.85), 0 0 24px $glow, inset 0 2px 3px rgba(255, 255, 255, 0.15), inset 0 -6px 12px rgba(0, 0, 0, 0.95)",
                facePlateBg = "linear-gradient(160deg, #1c232d 0%, #11151c 55%, #080a0e 100%)",
                accentColor = accent,
                accentShadow = "0 2px 8px rgba(0, 0, 0, 0.95), 0 0 14px $glow",
                accentBorder = "none"
            )
            NxprcPresetStyle.ARCADE -> ElementStyleConfig(
                chassisBg = "linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%)",
                chassisShadow = "0 14px 32px rgba(0, 0, 0, 0.85), inset 0 2px 2px rgba(255, 255, 255, 0.25), inset 0 -6px 12px rgba(0, 0, 0, 0.95)",
                facePlateBg = "radial-gradient(circle at 38% 28%, #4ade80 0%, #10b981 50%, #064e3b 100%)",
                accentColor = "#ffffff",
                accentShadow = "0 2px 4px rgba(0, 0, 0, 0.70)",
                accentBorder = "none"
            )
            NxprcPresetStyle.CYBERPUNK -> ElementStyleConfig(
                chassisBg = "linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%)",
                chassisShadow = "0 14px 32px rgba(0, 0, 0, 0.90), 0 0 25px $glow",
                facePlateBg = "linear-gradient(145deg, #131720 0%, #090c12 100%)",
                accentColor = accent,
                accentShadow = "0 0 10px $accent, 0 0 24px $glow",
                accentBorder = "1.5px solid $glow"
            )
            NxprcPresetStyle.SATIN -> ElementStyleConfig(
                chassisBg = "linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%)",
                chassisShadow = "0 14px 32px rgba(0, 0, 0, 0.80), inset 0 1.5px 2px rgba(255, 255, 255, 0.20), inset 0 -6px 12px rgba(0, 0, 0, 0.90)",
                facePlateBg = "radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%)",
                accentColor = "#e2e8f0",
                accentShadow = "0 -1px 1px rgba(0, 0, 0, 0.85), 0 1px 1px rgba(255, 255, 255, 0.20)",
                accentBorder = "none"
            )
        }

        val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  width: 180px;
  height: 180px;
  padding: 12px;
  box-sizing: border-box;
  border: ${config.accentBorder};
  outline: none;
  border-radius: 22px;
  background: ${config.chassisBg};
  box-shadow: ${config.chassisShadow};
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  cursor: pointer;
  overflow: visible;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}
.touchpad-surface {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 156px;
  height: 156px;
  border-radius: 16px;
  background: ${config.facePlateBg};
  z-index: 1;
}
.touchpad-scanline {
  position: absolute;
  left: 20px;
  top: 90px;
  width: 140px;
  height: 1px;
  background: linear-gradient(90deg, transparent, $accent, transparent);
  opacity: 0.65;
  z-index: 2;
}
.touchpad-title, .touchpad-sub {
  position: relative;
  z-index: 5;
  color: ${config.accentColor};
  text-shadow: ${config.accentShadow};
}
.touchpad-title {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 900;
  letter-spacing: 1.5px;
}
.touchpad-sub {
  margin-bottom: 6px;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 1px;
  color: $accent;
}
$active
""".trimIndent()

        val body = """
  <button class="touchpad-ctl" data-id="$touchId" data-control="$control" data-category="TOUCHPAD" data-name="${style.displayName} Touchpad $control" data-seed="71500" id="rc.touch_${control.lowercase()}">
    <div class="touchpad-surface" data-layer-role="base"></div>
    <div class="touchpad-scanline" data-layer-role="detail"></div>
    <span class="touchpad-title">$title</span>
    <span class="touchpad-sub">2.0X BALLISTICS</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }
}
