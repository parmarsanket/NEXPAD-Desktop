package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey

/*
 * NXPRC starter library
 *
 * Visual direction: RETRO HARDWARE / REALISTIC MATERIALS
 *
 * These documents intentionally use the small, portable NXPRC vocabulary:
 * gradients, borders, radii, shadows, pseudo-elements, one root animation,
 * and one centered press transform. The browser preview, desktop canvas, and
 * Android canvas therefore receive the same physical design language.
 */

private fun dpadGlyph(control: String): String = when (control.uppercase()) {
    ControlKey.DOWN.key -> "▼"
    ControlKey.LEFT.key -> "◀"
    ControlKey.RIGHT.key -> "▶"
    ControlKey.DPAD.key -> "▲"
    else -> "▲"
}

private fun htmlDocument(css: String, body: String): String = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
$css
</style>
</head>
<body>
$body
</body>
</html>
""".trimIndent()

private fun retroMotion(
    damping: String = "0.78",
    stiffness: String = "420",
    pressScale: String = "0.94"
): String = """
:root {
  --spring-damping: $damping;
  --spring-stiffness: $stiffness;
  --press-scale: $pressScale;
}
@keyframes retro-idle {
  0%, 100% { opacity: 0.97; }
  50% { opacity: 1; }
}
""".trimIndent()

private fun centeredPress(selector: String): String = """
$selector:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
  animation: none;
}
""".trimIndent()

private fun hardwareButton(
    rootClass: String,
    labelClass: String,
    control: String,
    category: String,
    width: Int,
    height: Int,
    radius: String,
    accent: String,
    glow: String,
    name: String,
    label: String,
    innerHtml: String = """<span class="$labelClass">$label</span>""",
    motion: String = retroMotion(),
    extraCss: String = ""
): String {
    val selector = ".$rootClass"
    val dimensions = "width: " + width + "px; height: " + height + "px;"
    val active = centeredPress(selector)
    val css = """
$motion
:root {
  --accent-core: $accent;
  --accent-glow: $glow;
}
$selector {
  position: relative;
  $dimensions
  box-sizing: border-box;
  border: 2px solid #C8BDAA;
  border-radius: $radius;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  outline: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #F3EBDD;
  background:
    linear-gradient(145deg, #E6DECF 0%, #A8A093 35%, #575852 68%, #242724 100%);
  box-shadow:
    0 3px 0 #171916,
    0 10px 18px rgba(0, 0, 0, 0.70),
    0 0 12px $glow,
    inset 0 2px 1px rgba(255, 255, 255, 0.48),
    inset 0 -7px 12px rgba(0, 0, 0, 0.72);
  animation: retro-idle 3600ms ease-in-out infinite;
}
$selector::before {
  content: "";
  position: absolute;
  left: 5%; top: 5%; width: 90%; height: 90%;
  box-sizing: border-box;
  border: 1px solid rgba(255, 248, 232, 0.26);
  border-radius: $radius;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.32),
    inset 0 -2px 3px rgba(0, 0, 0, 0.54);
  pointer-events: none;
  z-index: 1;
}
.$labelClass {
  position: relative;
  z-index: 5;
  color: #FFF8E8;
  font-size: 24px;
  font-weight: 900;
  line-height: 1;
  letter-spacing: 1px;
  text-shadow:
    0 1px 0 rgba(255, 255, 255, 0.58),
    0 2px 0 rgba(0, 0, 0, 0.72),
    0 3px 5px rgba(0, 0, 0, 0.72);
}
$extraCss
$active
""".trimIndent()
    return htmlDocument(
        css,
        """
  <button class="$rootClass" data-control="$control" data-category="$category" data-name="$name">
    $innerHtml
  </button>
        """.trimIndent()
    )
}

private fun faceMaterial(accent: String): Triple<String, String, String> = when (accent.uppercase()) {
    "#FF2A6D" -> Triple("#E3C5BD", "#A96463", "#4A2324")
    "#00E5FF" -> Triple("#D9E8E8", "#7EABB0", "#2B4D53")
    "#FFD600" -> Triple("#F2E2AA", "#B9903F", "#554014")
    "#3F8FE0" -> Triple("#D8E6EF", "#6D8FA7", "#263C4D")
    "#E6474E" -> Triple("#E7D2CB", "#A76364", "#4A2527")
    "#E055B8" -> Triple("#EDD6E6", "#B782A6", "#55344A")
    "#3FD25A" -> Triple("#E0EBD8", "#83A56D", "#3D5734")
    else -> Triple("#E1EBDD", "#8EA18B", "#425A46")
}

private fun facePreset(
    control: String,
    label: String,
    accent: String,
    glow: String,
    name: String,
    seed: Int
): String {
    val material = faceMaterial(accent)
    val materialTop = material.first
    val materialMid = material.second
    val materialDeep = material.third
    val css = """
.nexpad-btn {
  border-color: #1F2937;
  background:
    radial-gradient(circle at 45% 35%, $glow 0%, transparent 60%),
    radial-gradient(circle at 50% 50%, #252925 0%, #111411 65%, #050705 100%);
  box-shadow:
    0 14px 30px rgba(0, 0, 0, 0.85),
    0 0 0 3px rgba(17, 24, 39, 0.95),
    0 0 24px $glow,
    inset 0 2px 4px rgba(255, 255, 255, 0.25),
    inset 0 -6px 14px rgba(0, 0, 0, 0.90);
}
.nexpad-btn::before {
  left: 0; top: 0; width: 100%; height: 100%;
  border: 0;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
    conic-gradient(from 180deg at 50% 50%, #17202A, #2B3A4A, #131A22, #384D63, #17202A, #2B3A4A, #131A22);
  box-shadow:
    inset 0 3px 6px rgba(255, 255, 255, 0.22),
    inset 0 -6px 14px rgba(0, 0, 0, 0.85);
  z-index: 1;
}
.nexpad-btn::after {
  content: "";
  position: absolute;
  left: 14%; top: 6%; width: 72%; height: 40%;
  box-sizing: border-box;
  border-top: 1px solid rgba(255, 255, 255, 0.46);
  border-radius: 50%;
  background: transparent;
  transform: rotate(-10deg);
  pointer-events: none;
  z-index: 4;
}
/* The seed editor recognizes this tactile core token during rerolls. */
.nexpad-btn .btn-core {
  background: linear-gradient(145deg, #221217 0%, #12060a 40%, #060203 100%);
}
.nexpad-btn .face-plate {
  position: relative;
  left: auto; top: auto;
  width: 68px; height: 68px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid $accent;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, $glow 0%, transparent 40%),
    radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.70) 0%, transparent 55%),
    linear-gradient(145deg, $materialTop 0%, $materialMid 45%, $materialDeep 100%);
  box-shadow:
    inset 0 2px 5px rgba(255, 255, 255, 0.35),
    inset 0 -5px 10px rgba(0, 0, 0, 0.85);
  z-index: 3;
}
.nexpad-btn .btn-label {
  position: relative;
  z-index: 5;
  font-size: 34px;
  font-weight: 900;
  color: #FFFDF5;
  text-shadow:
    0 1px 0 rgba(255, 255, 255, 0.88),
    0 2px 0 rgba(0, 0, 0, 0.64),
    0 3px 5px rgba(0, 0, 0, 0.72);
}
""".trimIndent()
    return hardwareButton(
        rootClass = "nexpad-btn",
        labelClass = "btn-label",
        control = control,
        category = "BUTTON",
        width = 96,
        height = 96,
        radius = "50%",
        accent = accent,
        glow = glow,
        name = name,
        label = label,
        innerHtml = """
    <div class="face-plate btn-core" data-layer-role="detail">
      <span class="btn-label">$label</span>
    </div>
        """.trimIndent(),
        motion = retroMotion("0.70", "440", "0.93"),
        extraCss = css
    ).replace(
        """data-name="$name">""",
        """data-codename="Retro Foundry" data-name="$name" data-seed="$seed">"""
    )
}

private fun triggerPreset(control: String): String {
    val right = control.uppercase() == "RT"
    val accent = if (right) "#B65C52" else "#A57A3A"
    val glow = if (right) "rgba(182, 92, 82, 0.34)" else "rgba(165, 122, 58, 0.34)"
    return hardwareButton(
        rootClass = "trigger-btn",
        labelClass = "trigger-label",
        control = control,
        category = "TRIGGER",
        width = 74,
        height = 112,
        radius = "22px 22px 28px 28px",
        accent = accent,
        glow = glow,
        name = "Retro Foundry Trigger $control",
        label = control,
        innerHtml = """
    <span class="trigger-label">$control</span>
    <div class="trigger-well" data-layer-role="base">
      <div class="trigger-fill"></div>
      <div class="trigger-rib trigger-rib-1"></div>
      <div class="trigger-rib trigger-rib-2"></div>
      <div class="trigger-rib trigger-rib-3"></div>
    </div>
        """.trimIndent(),
        motion = retroMotion("0.76", "500", "0.95"),
        extraCss = """
.trigger-label {
  position: absolute;
  left: 0; top: 14px; width: 100%;
  font-size: 23px;
  letter-spacing: 2px;
  color: #F5E7C9;
}
.trigger-well {
  position: absolute;
  left: 11px; top: 54px; width: 48px; height: 43px;
  box-sizing: border-box;
  border: 2px solid #242723;
  border-radius: 12px 12px 17px 17px;
  background: linear-gradient(160deg, #4B4B43 0%, #242722 55%, #11130F 100%);
  box-shadow:
    inset 0 4px 6px rgba(0, 0, 0, 0.84),
    inset 0 -2px 1px rgba(255, 255, 255, 0.14);
  z-index: 4;
}
.trigger-fill {
  position: absolute;
  left: 5px; bottom: 5px; width: 34px; height: 15px;
  border-radius: 0 0 7px 7px;
  background: linear-gradient(180deg, #D3B77A 0%, $accent 100%);
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.46), 0 0 6px $glow;
}
.trigger-rib {
  position: absolute;
  left: 8px; width: 30px; height: 3px;
  border-radius: 2px;
  background: rgba(241, 225, 191, 0.44);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.86);
}
.trigger-rib-1 { top: 8px; }
.trigger-rib-2 { top: 18px; }
.trigger-rib-3 { top: 28px; }
        """.trimIndent()
    )
}

private fun bumperPreset(control: String): String {
    val right = control.uppercase() == "RB"
    val accent = if (right) "#8E6DAA" else "#6B927C"
    val glow = if (right) "rgba(142, 109, 170, 0.28)" else "rgba(107, 146, 124, 0.28)"
    return hardwareButton(
        rootClass = "bumper-btn",
        labelClass = "bumper-label",
        control = control,
        category = "BUMPER",
        width = 120,
        height = 52,
        radius = "17px",
        accent = accent,
        glow = glow,
        name = "Retro Foundry Bumper $control",
        label = control,
        innerHtml = """
    <div class="bumper-seam" data-layer-role="detail"></div>
    <span class="bumper-label">$control</span>
        """.trimIndent(),
        motion = retroMotion("0.75", "520", "0.95"),
        extraCss = """
.bumper-btn::before {
  left: 12%; top: 10%; width: 76%; height: 35%;
  border: 1px solid rgba(255, 248, 232, 0.42);
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.30) 0%, transparent 80%);
}
.bumper-label {
  font-size: 22px;
  letter-spacing: 2px;
  color: #F3E6C9;
}
.bumper-seam {
  position: absolute;
  left: 16px; bottom: 5px; width: 84px; height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, transparent 0%, $accent 50%, transparent 100%);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.72);
  z-index: 4;
}
        """.trimIndent()
    )
}

private fun dpadDirectionPreset(control: String): String {
    val glyph = dpadGlyph(control)
    val accent = when (control.uppercase()) {
        "DOWN", "RIGHT" -> "#A27C48"
        else -> "#6D8A82"
    }
    val glow = if (control.uppercase() in setOf("DOWN", "RIGHT")) {
        "rgba(162, 124, 72, 0.26)"
    } else {
        "rgba(109, 138, 130, 0.26)"
    }
    return hardwareButton(
        rootClass = "dpad-btn",
        labelClass = "dpad-glyph",
        control = control,
        category = "DPAD",
        width = 88,
        height = 88,
        radius = "22px",
        accent = accent,
        glow = glow,
        name = "Retro Foundry D-Pad $control",
        label = glyph,
        motion = retroMotion("0.72", "460", "0.95"),
        extraCss = """
.dpad-btn {
  border-color: #5F6259;
  background:
    radial-gradient(circle at 34% 24%, rgba(255, 255, 255, 0.22) 0%, transparent 34%),
    linear-gradient(145deg, #50534B 0%, #2E312D 58%, #171A17 100%);
  box-shadow:
    0 3px 0 #0F110F,
    0 9px 14px rgba(0, 0, 0, 0.76),
    inset 0 2px 2px rgba(255, 255, 255, 0.24),
    inset 0 -6px 9px rgba(0, 0, 0, 0.72);
}
.dpad-glyph {
  font-size: 32px;
  color: #F3E7C9;
  text-shadow: 0 2px 0 #11130F, 0 0 5px $glow;
}
        """.trimIndent()
    )
}

private fun dpadCrossPreset(): String {
    val motion = retroMotion("0.72", "460", "0.95")
    val css = """
$motion
:root {
  --accent-core: #8F7045;
  --accent-glow: rgba(143, 112, 69, 0.30);
}
.dpad-cross {
  position: relative;
  width: 160px; height: 160px;
  box-sizing: border-box;
  border: 2px solid #8F8877;
  border-radius: 50%;
  padding: 0;
  overflow: hidden;
  background:
    radial-gradient(circle at 34% 22%, rgba(255, 255, 255, 0.20) 0%, transparent 32%),
    linear-gradient(145deg, #B6AD9D 0%, #706D63 48%, #292C27 100%);
  box-shadow:
    0 4px 0 #11130F,
    0 16px 25px rgba(0, 0, 0, 0.76),
    0 0 10px rgba(143, 112, 69, 0.24),
    inset 0 7px 10px rgba(255, 255, 255, 0.16),
    inset 0 -7px 13px rgba(0, 0, 0, 0.78);
  animation: retro-idle 3300ms ease-in-out infinite;
}
.dpad-socket-trim {
  position: absolute;
  left: 8px; top: 8px; width: 140px; height: 140px;
  box-sizing: border-box;
  border: 2px solid rgba(35, 38, 33, 0.56);
  border-radius: 50%;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.32);
}
.dpad-arm-v, .dpad-arm-h {
  position: absolute;
  box-sizing: border-box;
  border: 2px solid #151815;
  border-radius: 13px;
  background:
    linear-gradient(145deg, #555A50 0%, #30352F 48%, #171A17 100%);
  box-shadow:
    0 5px 8px rgba(0, 0, 0, 0.66),
    inset 0 2px 1px rgba(255, 255, 255, 0.18),
    inset 0 -4px 6px rgba(0, 0, 0, 0.76);
}
.dpad-arm-v { left: 54px; top: 10px; width: 52px; height: 140px; }
.dpad-arm-h { left: 10px; top: 54px; width: 140px; height: 52px; }
.dpad-arm {
  position: absolute;
  box-sizing: border-box;
  border: 1px solid #77796D;
  border-radius: 9px;
  background: linear-gradient(145deg, #666A5E 0%, #3A4037 62%, #1C201C 100%);
  box-shadow:
    inset 0 2px 1px rgba(255, 255, 255, 0.24),
    inset 0 -3px 4px rgba(0, 0, 0, 0.74);
  z-index: 3;
}
.dpad-arm-top { left: 56px; top: 12px; width: 48px; height: 44px; }
.dpad-arm-bottom { left: 56px; top: 104px; width: 48px; height: 44px; }
.dpad-arm-left { left: 12px; top: 56px; width: 44px; height: 48px; }
.dpad-arm-right { left: 104px; top: 56px; width: 44px; height: 48px; }
.dpad-pivot {
  position: absolute;
  left: 55px; top: 55px; width: 50px; height: 50px;
  box-sizing: border-box;
  border: 2px solid #171A17;
  border-radius: 50%;
  background:
    radial-gradient(circle at 36% 28%, #6D7165 0%, #30362F 46%, #10130F 100%);
  box-shadow:
    inset 0 5px 8px rgba(0, 0, 0, 0.88),
    inset 0 -1px 1px rgba(255, 255, 255, 0.24);
  z-index: 4;
}
.dpad-arrow {
  position: absolute;
  z-index: 5;
  width: 28px; height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 900;
  color: #F2E4C5;
  text-shadow: 0 2px 0 #11130F, 0 0 4px rgba(255, 226, 170, 0.30);
}
.dpad-up { left: 66px; top: 18px; }
.dpad-right { left: 116px; top: 66px; }
.dpad-down { left: 66px; top: 114px; }
.dpad-left { left: 16px; top: 66px; }
.dpad-cross:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
  animation: none;
}
""".trimIndent()
    return htmlDocument(
        css,
        """
  <button class="dpad-btn dpad-cross" data-control="DPAD" data-category="DPAD" data-name="Retro Foundry Cross Pad">
    <div class="dpad-socket-trim"></div>
    <div class="dpad-arm-v"></div>
    <div class="dpad-arm-h"></div>
    <div class="dpad-arm dpad-arm-top"></div>
    <div class="dpad-arm dpad-arm-bottom"></div>
    <div class="dpad-arm dpad-arm-left"></div>
    <div class="dpad-arm dpad-arm-right"></div>
    <div class="dpad-pivot"></div>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
        """.trimIndent()
    )
}

private fun joystickPreset(control: String): String {
    val right = control.uppercase() == "RS"
    val accent = if (right) "#9B6F61" else "#6C8C78"
    val glow = if (right) "rgba(155, 111, 97, 0.30)" else "rgba(108, 140, 120, 0.30)"
    val motion = retroMotion("0.70", "420", "0.94")
    val css = """
$motion
:root {
  --accent-core: $accent;
  --accent-glow: $glow;
}
.stick-btn {
  position: relative;
  width: 150px; height: 150px;
  border: 0;
  padding: 0;
  background: transparent;
  animation: retro-idle 3500ms ease-in-out infinite;
}
.stick-base {
  position: absolute;
  left: 0; top: 0; width: 150px; height: 150px;
  box-sizing: border-box;
  border: 3px solid #6B6B60;
  border-radius: 50%;
  background:
    radial-gradient(circle at 32% 23%, rgba(255, 255, 255, 0.18) 0%, transparent 30%),
    radial-gradient(circle at 50% 52%, #51564C 0%, #2B3029 58%, #10130F 100%);
  box-shadow:
    0 4px 0 #0D100D,
    0 16px 25px rgba(0, 0, 0, 0.80),
    0 0 12px $glow,
    inset 0 4px 5px rgba(255, 255, 255, 0.24),
    inset 0 -10px 18px rgba(0, 0, 0, 0.84);
  z-index: 1;
}
.socket-well {
  position: absolute;
  left: 13px; top: 13px; width: 118px; height: 118px;
  box-sizing: border-box;
  border: 2px solid #1A1D18;
  border-radius: 50%;
  background: radial-gradient(circle at 44% 34%, #343A32 0%, #151914 68%, #070907 100%);
  box-shadow: inset 0 7px 11px rgba(0, 0, 0, 0.94);
}
.axis-ring {
  position: absolute;
  left: 26px; top: 26px; width: 96px; height: 96px;
  box-sizing: border-box;
  border: 2px dashed rgba(188, 165, 111, 0.50);
  border-radius: 50%;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.76);
}
.stick-cap {
  position: absolute;
  left: 26px; top: 26px; width: 98px; height: 98px;
  box-sizing: border-box;
  border: 2px solid #B9AA8B;
  border-radius: 50%;
  background:
    radial-gradient(circle at 31% 22%, rgba(255, 255, 255, 0.28) 0%, transparent 30%),
    linear-gradient(145deg, #B6A88B 0%, #6E6757 46%, #292B25 100%);
  box-shadow:
    0 5px 8px rgba(0, 0, 0, 0.78),
    0 0 7px $glow,
    inset 0 3px 2px rgba(255, 255, 255, 0.38),
    inset 0 -8px 13px rgba(0, 0, 0, 0.78);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}
.knurled-ring {
  position: absolute;
  left: 15px; top: 15px; width: 64px; height: 64px;
  box-sizing: border-box;
  border: 2px dashed $accent;
  border-radius: 50%;
  opacity: 0.76;
}
.cap-gloss {
  position: absolute;
  left: 20px; top: 9px; width: 56px; height: 17px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.34) 0%, transparent 78%);
}
.stick-label {
  position: relative;
  z-index: 4;
  font-size: 23px;
  font-weight: 900;
  color: #FFF3D5;
  letter-spacing: 1px;
  text-shadow: 0 2px 0 #191B16, 0 3px 5px rgba(0, 0, 0, 0.76);
}
.stick-btn:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
  animation: none;
}
""".trimIndent()
    return htmlDocument(
        css,
        """
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-name="Retro Foundry Analog Stick $control">
    <div class="stick-base" data-layer-role="base">
      <div class="socket-well" data-layer-role="base"></div>
      <div class="axis-ring" data-layer-role="base"></div>
    </div>
    <div class="stick-cap" data-layer-role="thumb-cap">
      <div class="knurled-ring" data-layer-role="thumb-cap"></div>
      <div class="cap-gloss" data-layer-role="thumb-cap"></div>
      <span class="stick-label">$control</span>
    </div>
  </button>
        """.trimIndent()
    )
}

private fun stickButtonPreset(control: String): String {
    val right = control.uppercase() == "RSB"
    val accent = if (right) "#9B6F61" else "#6C8C78"
    val glow = if (right) "rgba(155, 111, 97, 0.28)" else "rgba(108, 140, 120, 0.28)"
    return hardwareButton(
        rootClass = "stick-btn-ctl",
        labelClass = "stick-btn-label",
        control = control,
        category = "BUTTON",
        width = 70,
        height = 70,
        radius = "50%",
        accent = accent,
        glow = glow,
        name = "Retro Foundry Stick Click $control",
        label = control,
        innerHtml = """
    <div class="stick-btn-ring" data-layer-role="detail"></div>
    <div class="stick-btn-dish">
      <span class="stick-btn-label">$control</span>
    </div>
        """.trimIndent(),
        motion = retroMotion("0.74", "440", "0.94"),
        extraCss = """
.stick-btn-ctl {
  border-color: #9A8C73;
  background:
    radial-gradient(circle at 32% 22%, rgba(255, 255, 255, 0.30) 0%, transparent 34%),
    linear-gradient(145deg, #7E806F 0%, #494B41 55%, #1C201B 100%);
}
.stick-btn-ring {
  position: absolute;
  left: 7px; top: 7px; width: 52px; height: 52px;
  box-sizing: border-box;
  border: 2px dashed $accent;
  border-radius: 50%;
  opacity: 0.74;
}
.stick-btn-dish {
  position: relative;
  width: 42px; height: 42px;
  border: 2px solid #B9AA8B;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 34% 24%, #A99C80 0%, #4C4D42 62%, #1A1D18 100%);
  box-shadow:
    0 2px 0 rgba(0, 0, 0, 0.62),
    inset 0 2px 2px rgba(255, 255, 255, 0.38),
    inset 0 -5px 7px rgba(0, 0, 0, 0.72);
  z-index: 4;
}
.stick-btn-label { font-size: 13px; color: #FFF0CC; }
        """.trimIndent()
    )
}

private fun touchpadPreset(control: String): String {
    val right = control.uppercase() == "RTP"
    val accent = if (right) "#9B6F61" else "#6C8C78"
    val glow = if (right) "rgba(155, 111, 97, 0.25)" else "rgba(108, 140, 120, 0.25)"
    val title = if (right) "Touch Look • RTP" else "Touch Move • LTP"
    val touchId = "touch_" + control.lowercase()
    val motion = retroMotion("0.80", "500", "0.98")
    val css = """
$motion
:root {
  --accent-core: $accent;
  --accent-glow: $glow;
}
.touchpad-ctl {
  position: relative;
  width: 180px; height: 180px;
  box-sizing: border-box;
  border: 3px solid #9B917D;
  border-radius: 25px;
  padding: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.22) 0%, transparent 30%),
    linear-gradient(145deg, #8E8D80 0%, #4C5049 56%, #1A1D19 100%);
  box-shadow:
    0 4px 0 #11130F,
    0 16px 26px rgba(0, 0, 0, 0.78),
    0 0 10px $glow,
    inset 0 3px 2px rgba(255, 255, 255, 0.34),
    inset 0 -10px 17px rgba(0, 0, 0, 0.82);
  animation: retro-idle 3800ms ease-in-out infinite;
}
.touchpad-surface {
  position: absolute;
  left: 14px; top: 14px; width: 148px; height: 148px;
  box-sizing: border-box;
  border: 2px solid #B8AF9A;
  border-radius: 17px;
  background:
    radial-gradient(circle at 48% 30%, rgba(255, 255, 255, 0.18) 0%, transparent 48%),
    linear-gradient(160deg, #62675D 0%, #333933 58%, #171A17 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.22),
    inset 0 -8px 12px rgba(0, 0, 0, 0.78);
  z-index: 1;
}
.touchpad-scanline {
  position: absolute;
  left: 30px; top: 88px; width: 120px; height: 1px;
  background: linear-gradient(90deg, transparent, #C0A66A, transparent);
  opacity: 0.52;
  z-index: 2;
}
.touchpad-title, .touchpad-sub {
  position: relative;
  z-index: 5;
  color: #F6E8C7;
  text-shadow: 0 2px 0 #151815, 0 0 4px $glow;
}
.touchpad-title {
  margin-top: 8px;
  font-size: 15px;
  font-weight: 900;
  letter-spacing: 1.2px;
}
.touchpad-sub {
  margin-bottom: 8px;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 1.1px;
  color: #D8B978;
}
.touchpad-ctl:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
  animation: none;
}
""".trimIndent()
    return htmlDocument(
        css,
        """
  <button class="touchpad-ctl" data-id="$touchId" data-control="$control" data-category="TOUCHPAD" data-name="Touchpad $control">
    <span class="touchpad-title">$title</span>
    <div class="touchpad-surface"></div>
    <div class="touchpad-scanline" data-layer-role="detail"></div>
    <span class="touchpad-sub">2.0X BALLISTICS</span>
  </button>
        """.trimIndent()
    )
}

private fun systemMenuPreset(): String {
    val motion = retroMotion("0.80", "500", "0.93")
    val css = """
$motion
.system-btn {
  position: relative;
  width: 64px; height: 42px;
  box-sizing: border-box;
  border: 2px solid #B8AA8B;
  border-radius: 12px;
  padding: 0;
  overflow: hidden;
  display: block;
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.28) 0%, transparent 34%),
    linear-gradient(145deg, #8E8D80 0%, #4A4D45 58%, #1A1D19 100%);
  box-shadow:
    0 3px 0 #11130F,
    0 11px 16px rgba(0, 0, 0, 0.74),
    inset 0 2px 2px rgba(255, 255, 255, 0.36),
    inset 0 -7px 11px rgba(0, 0, 0, 0.76);
  animation: retro-idle 3600ms ease-in-out infinite;
}
.burger-bar {
  position: absolute;
  left: 20px; width: 24px; height: 3px;
  border-radius: 2px;
  background: linear-gradient(180deg, #F6E5BD 0%, #B39255 100%);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.84);
  z-index: 4;
}
.burger-bar-1 { top: 9px; }
.burger-bar-2 { top: 19px; }
.burger-bar-3 { top: 29px; }
.system-btn:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
  animation: none;
}
""".trimIndent()
    return htmlDocument(
        css,
        """
  <button class="system-btn" data-control="MENU" data-category="SYSTEM" data-name="Retro Foundry Menu">
    <div class="burger-bar burger-bar-1"></div>
    <div class="burger-bar burger-bar-2"></div>
    <div class="burger-bar burger-bar-3"></div>
  </button>
        """.trimIndent()
    )
}

private fun macroPreset(control: String): String {
    val safeControl = control.trim().ifBlank { "M1" }.uppercase()
    return hardwareButton(
        rootClass = "macro-paddle",
        labelClass = "macro-label",
        control = safeControl,
        category = "MACRO",
        width = 72,
        height = 72,
        radius = "18px",
        accent = "#B38A4B",
        glow = "rgba(179, 138, 75, 0.28)",
        name = "Retro Foundry Macro Paddle $safeControl",
        label = safeControl,
        innerHtml = """
    <span class="macro-bolt">⚡</span>
    <span class="macro-label">$safeControl</span>
        """.trimIndent(),
        motion = retroMotion("0.76", "460", "0.94"),
        extraCss = """
.macro-paddle {
  border-color: #B8AA8B;
  background:
    radial-gradient(circle at 28% 20%, rgba(255, 255, 255, 0.28) 0%, transparent 32%),
    linear-gradient(145deg, #9C8E73 0%, #554D3F 55%, #211F19 100%);
}
.macro-bolt {
  position: absolute;
  left: 17px; top: 14px;
  z-index: 5;
  font-size: 16px;
  color: #F4D18A;
  text-shadow: 0 1px 0 #171916, 0 0 4px rgba(179, 138, 75, 0.40);
}
.macro-label {
  position: relative;
  z-index: 5;
  margin-left: 16px;
  font-size: 19px;
  color: #FFF0C9;
}
        """.trimIndent()
    )
}

private fun syntaxDocument(
    rootClass: String,
    selector: String,
    width: Int,
    height: Int,
    body: String,
    extraCss: String = ""
): String {
    val dimensions = "width: " + width + "px; height: " + height + "px;"
    val css = """
:root {
  --spring-damping: 0.72;
  --spring-stiffness: 480;
  --press-scale: 0.94;
}
.$rootClass {
  position: relative;
  $dimensions
  box-sizing: border-box;
  background: transparent;
  border: 1px solid currentColor;
}
$extraCss
$selector:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
}
""".trimIndent()
    return htmlDocument(css, body)
}

object NxprcPresets {
    val PRESET_ULTRA_NEXPAD_A get() = PRESET_NEO_TACTILE_A

    val PRESET_CYBER_REACTOR = hardwareButton(
        rootClass = "reactor-button",
        labelClass = "reactor-button-label",
        control = "A",
        category = "BUTTON",
        width = 92,
        height = 92,
        radius = "16px",
        accent = "#7E9E84",
        glow = "rgba(126, 158, 132, 0.28)",
        name = "Cyber Reactor A",
        label = "A",
        extraCss = """
.reactor-button {
  border-color: #B8AA8B;
  background:
    radial-gradient(circle at 28% 20%, rgba(255, 255, 255, 0.25) 0%, transparent 32%),
    linear-gradient(145deg, #8B8E80 0%, #4D5149 55%, #1C211B 100%);
}
.reactor-button-label { font-size: 30px; color: #F2E6C8; }
        """.trimIndent()
    )

    val PRESET_CRIMSON_OCTA = hardwareButton(
        rootClass = "crimson-octa",
        labelClass = "crimson-octa-label",
        control = "B",
        category = "BUTTON",
        width = 94,
        height = 94,
        radius = "16px",
        accent = "#A96463",
        glow = "rgba(169, 100, 99, 0.26)",
        name = "Retro Octagon B",
        label = "B",
        extraCss = """
.crimson-octa {
  clip-path: polygon(29% 0%, 71% 0%, 100% 29%, 100% 71%, 71% 100%, 29% 100%, 0% 71%, 0% 29%);
  border-color: #B8AA8B;
}
.crimson-octa-label { font-size: 30px; color: #F4D9CC; }
        """.trimIndent()
    )

    val PRESET_SPEED_TURBO = hardwareButton(
        rootClass = "speed-turbo",
        labelClass = "turbo-label",
        control = "Y",
        category = "BUTTON",
        width = 104,
        height = 74,
        radius = "20px",
        accent = "#B9903F",
        glow = "rgba(185, 144, 63, 0.28)",
        name = "Retro Turbo Y",
        label = "Y",
        innerHtml = """
    <span class="turbo-label">Y</span>
    <span class="turbo-sub">TURBO</span>
        """.trimIndent(),
        extraCss = """
.turbo-label { font-size: 30px; color: #F7E1A4; }
.turbo-sub {
  position: absolute;
  bottom: 12px;
  font-size: 9px;
  font-weight: 900;
  letter-spacing: 2px;
  color: #D4B46A;
}
        """.trimIndent()
    )

    val PRESET_NEO_TACTILE_A =
        facePreset("A", "A", "#00FFA3", "rgba(0, 255, 163, 0.42)", "Retro Foundry A", 16166)
    val PRESET_NEO_TACTILE_B =
        facePreset("B", "B", "#FF2A6D", "rgba(255, 42, 109, 0.36)", "Retro Foundry B", 24892)
    val PRESET_NEO_TACTILE_X =
        facePreset("X", "X", "#00E5FF", "rgba(0, 229, 255, 0.38)", "Retro Foundry X", 38120)
    val PRESET_NEO_TACTILE_Y =
        facePreset("Y", "Y", "#FFD600", "rgba(255, 214, 0, 0.40)", "Retro Foundry Y", 49551)

    val PRESET_NEO_TACTILE_PS_CROSS =
        facePreset("A", "✕", "#3F8FE0", "rgba(63, 143, 224, 0.34)", "Retro Foundry Cross", 16166)
    val PRESET_NEO_TACTILE_PS_CIRCLE =
        facePreset("B", "○", "#E6474E", "rgba(230, 71, 78, 0.34)", "Retro Foundry Circle", 24892)
    val PRESET_NEO_TACTILE_PS_SQUARE =
        facePreset("X", "□", "#E055B8", "rgba(224, 85, 184, 0.34)", "Retro Foundry Square", 38120)
    val PRESET_NEO_TACTILE_PS_TRIANGLE =
        facePreset("Y", "△", "#3FD25A", "rgba(63, 210, 90, 0.34)", "Retro Foundry Triangle", 49551)

    val PRESET_DPAD_UP = dpadDirectionPreset("UP")
    val PRESET_DPAD_DOWN = dpadDirectionPreset("DOWN")
    val PRESET_DPAD_LEFT = dpadDirectionPreset("LEFT")
    val PRESET_DPAD_RIGHT = dpadDirectionPreset("RIGHT")
    val PRESET_DPAD_CROSS = dpadCrossPreset()

    val PRESET_TRIGGER_LT = triggerPreset("LT")
    val PRESET_TRIGGER_RT = triggerPreset("RT")
    val PRESET_BUMPER_LB = bumperPreset("LB")
    val PRESET_BUMPER_RB = bumperPreset("RB")

    val PRESET_THUMBSTICK_LS = joystickPreset("LS")
    val PRESET_THUMBSTICK_RS = joystickPreset("RS")
    val PRESET_STICK_BUTTON_LSB = stickButtonPreset("LSB")
    val PRESET_STICK_BUTTON_RSB = stickButtonPreset("RSB")

    val PRESET_TOUCHPAD_LTP = touchpadPreset("LTP")
    val PRESET_TOUCHPAD_RTP = touchpadPreset("RTP")

    val PRESET_SYSTEM_MENU = systemMenuPreset()
    val PRESET_SYSTEM_VIEW = hardwareButton(
        rootClass = "system-btn",
        labelClass = "system-symbol",
        control = "VIEW",
        category = "SYSTEM",
        width = 64,
        height = 42,
        radius = "12px",
        accent = "#A78B5A",
        glow = "rgba(167, 139, 90, 0.24)",
        name = "Retro Foundry View",
        label = "VIEW",
        extraCss = ".system-symbol { font-size: 12px; letter-spacing: 1px; color: #F2DFB6; }"
    )
    val PRESET_SYSTEM_HOME = hardwareButton(
        rootClass = "system-btn",
        labelClass = "system-symbol",
        control = "HOME",
        category = "SYSTEM",
        width = 70,
        height = 70,
        radius = "50%",
        accent = "#A78B5A",
        glow = "rgba(167, 139, 90, 0.24)",
        name = "Retro Foundry Home",
        label = "⌂",
        extraCss = ".system-symbol { font-size: 28px; color: #F2DFB6; }"
    )
    val PRESET_SYSTEM_SHARE = hardwareButton(
        rootClass = "system-btn",
        labelClass = "system-symbol",
        control = "SHARE",
        category = "SYSTEM",
        width = 70,
        height = 70,
        radius = "50%",
        accent = "#8F7A5C",
        glow = "rgba(143, 122, 92, 0.24)",
        name = "Retro Foundry Share",
        label = "⇪",
        extraCss = ".system-symbol { font-size: 27px; color: #F2DFB6; }"
    )

    fun createPresetMacroPaddle(control: String = "M1"): String = macroPreset(control)

    val PRESET_MACRO_M1 get() = createPresetMacroPaddle("M1")
    val PRESET_MACRO_M2 get() = createPresetMacroPaddle("M2")
    val PRESET_MACRO_M3 get() = createPresetMacroPaddle("M3")
    val PRESET_MACRO_M4 get() = createPresetMacroPaddle("M4")

    fun getReferenceTemplate(control: String): String = getReferenceTemplateInternal(control, "BUTTON")

    fun getReferenceTemplate(control: String, category: String): String =
        getReferenceTemplateInternal(control, category)

    fun getReferenceTemplate(control: String, category: String, consoleMode: ConsoleMode): String =
        getReferenceTemplateInternal(control, category, consoleMode)

    private fun getReferenceTemplateInternal(
        control: String,
        category: String,
        consoleMode: ConsoleMode = ConsoleMode.XBOX
    ): String {
        val ctrl = ControlKey.fromIdentifier(control)
        val catType = ctrl?.categoryType
            ?: CategoryType.fromIdentifier(category)
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
                CategoryType.TRIGGERS ->
                    if (ctrl == ControlKey.LT) PRESET_TRIGGER_LT else PRESET_TRIGGER_RT
                CategoryType.BUMPERS ->
                    if (ctrl == ControlKey.LB) PRESET_BUMPER_LB else PRESET_BUMPER_RB
                CategoryType.STICKS -> when (ctrl) {
                    ControlKey.RS -> PRESET_THUMBSTICK_RS
                    ControlKey.LSB -> PRESET_STICK_BUTTON_LSB
                    ControlKey.RSB -> PRESET_STICK_BUTTON_RSB
                    ControlKey.LTP -> PRESET_TOUCHPAD_LTP
                    ControlKey.RTP -> PRESET_TOUCHPAD_RTP
                    else -> PRESET_THUMBSTICK_LS
                }
                CategoryType.SYSTEM -> when (ctrl) {
                    ControlKey.BACK -> PRESET_SYSTEM_VIEW
                    ControlKey.GUIDE -> PRESET_SYSTEM_HOME
                    ControlKey.SHARE -> PRESET_SYSTEM_SHARE
                    else -> PRESET_SYSTEM_MENU
                }
                CategoryType.MACROS -> when (ctrl) {
                    ControlKey.M2 -> createPresetMacroPaddle("M2")
                    ControlKey.M3 -> createPresetMacroPaddle("M3")
                    ControlKey.M4 -> createPresetMacroPaddle("M4")
                    else -> createPresetMacroPaddle(ctrl?.key ?: "M1")
                }
                CategoryType.ABXY -> getReferenceTemplateInternal(control, "BUTTON", consoleMode)
            }
        }

        if (consoleMode == ConsoleMode.PLAYSTATION) {
            return when (ctrl) {
                ControlKey.A -> PRESET_NEO_TACTILE_PS_CROSS
                ControlKey.B -> PRESET_NEO_TACTILE_PS_CIRCLE
                ControlKey.X -> PRESET_NEO_TACTILE_PS_SQUARE
                ControlKey.Y -> PRESET_NEO_TACTILE_PS_TRIANGLE
                else -> PRESET_NEO_TACTILE_PS_CROSS
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
            ControlKey.SHARE -> PRESET_SYSTEM_SHARE
            ControlKey.M1 -> createPresetMacroPaddle("M1")
            ControlKey.M2 -> createPresetMacroPaddle("M2")
            ControlKey.M3 -> createPresetMacroPaddle("M3")
            ControlKey.M4 -> createPresetMacroPaddle("M4")
            else -> PRESET_NEO_TACTILE_A
        }
    }

    fun getSyntaxSkeleton(control: String, category: String, widthDp: Int, heightDp: Int): String {
        val ctrl = ControlKey.fromIdentifier(control)
        val upper = control.uppercase()
        val glyph = dpadGlyph(control)
        val catType = CategoryType.fromIdentifier(category)
            ?: ctrl?.categoryType
            ?: CategoryManager.findCategoryForControl(control)?.type

        if (ctrl == ControlKey.LTP || ctrl == ControlKey.RTP ||
            (ctrl?.componentType == ComponentType.TOUCHPAD && ctrl.categoryType == CategoryType.STICKS)
        ) {
            val id = "touch_" + control.lowercase()
            return syntaxDocument(
                "touchpad-ctl",
                ".touchpad-ctl",
                widthDp,
                heightDp,
                """
  <button class="touchpad-ctl" data-id="$id" data-control="$control" data-category="TOUCHPAD" data-name="Touchpad $control">
    <span class="touchpad-title">$control</span>
  </button>
                """.trimIndent()
            )
        }

        if (ctrl == ControlKey.LSB || ctrl == ControlKey.RSB ||
            (ctrl?.componentType == ComponentType.BUTTON && ctrl.categoryType == CategoryType.STICKS)
        ) {
            return syntaxDocument(
                "stick-btn-ctl",
                ".stick-btn-ctl",
                widthDp,
                heightDp,
                """
  <button class="stick-btn-ctl" data-control="$control" data-category="BUTTON" data-name="Stick Button $control">
    <span class="stick-btn-label">$control</span>
  </button>
                """.trimIndent()
            )
        }

        return when (catType) {
            CategoryType.STICKS -> syntaxDocument(
                "stick-btn",
                ".stick-btn",
                widthDp,
                heightDp,
                """
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-name="Analog Stick $control">
    <div class="stick-base"></div>
    <div class="stick-cap">
      <span class="stick-label">$control</span>
    </div>
  </button>
                """.trimIndent(),
                """
.stick-base, .stick-cap { position: absolute; border: 1px solid currentColor; border-radius: 50%; }
.stick-base { left: 0; top: 0; width: 100%; height: 100%; }
.stick-cap { left: 17%; top: 17%; width: 66%; height: 66%; }
.stick-label { position: relative; }
                """.trimIndent()
            )
            CategoryType.TRIGGERS -> syntaxDocument(
                "trigger-btn",
                ".trigger-btn",
                widthDp,
                heightDp,
                """
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-name="Trigger $control">
    <span class="trigger-label">$control</span>
  </button>
                """.trimIndent()
            )
            CategoryType.BUMPERS -> syntaxDocument(
                "bumper-btn",
                ".bumper-btn",
                widthDp,
                heightDp,
                """
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-name="Bumper $control">
    <span class="bumper-label">$control</span>
  </button>
                """.trimIndent()
            )
            CategoryType.DPAD -> {
                if (upper == ControlKey.DPAD.key) {
                    syntaxDocument(
                        "dpad-btn",
                        ".dpad-cross",
                        widthDp,
                        heightDp,
                        """
  <button class="dpad-btn dpad-cross" data-control="DPAD" data-category="DPAD" data-name="D-Pad Cross">
    <div class="dpad-pivot"></div>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
                        """.trimIndent(),
                        ".dpad-pivot { position: absolute; }"
                    )
                } else {
                    syntaxDocument(
                        "dpad-btn",
                        ".dpad-btn",
                        widthDp,
                        heightDp,
                        """
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-name="D-Pad $control">
    <span class="dpad-glyph">$glyph</span>
  </button>
                        """.trimIndent()
                    )
                }
            }
            CategoryType.SYSTEM, CategoryType.MACROS -> {
                val macroCategory = if (catType == CategoryType.MACROS) "MACRO" else "SYSTEM"
                syntaxDocument(
                    "system-btn",
                    ".system-btn",
                    widthDp,
                    heightDp,
                    """
  <button class="system-btn" data-control="$control" data-category="$macroCategory" data-name="System $control">
    <span class="system-label">$control</span>
  </button>
                    """.trimIndent()
                )
            }
            CategoryType.ABXY, null -> syntaxDocument(
                "nexpad-btn",
                ".nexpad-btn",
                widthDp,
                heightDp,
                """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-name="Action $control">
    <span class="btn-label">$control</span>
  </button>
                """.trimIndent()
            )
        }
    }
}
