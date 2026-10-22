package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey

/*
 * NXPRC Starter Component Library
 *
 * Visual Direction: WARM REALISTIC-RETRO HARDWARE
 *
 * Industrial design philosophy inspired by Braun (Dieter Rams), Teenage Engineering,
 * and vintage 1980s studio electronics:
 * - Molded warm polymer chassis with directional bevels & specular highlights
 * - Recessed socket wells with machined countersinks & contact ambient occlusion
 * - Textured rubber grip ribs and knurled traction bezels
 * - Countersunk brass slotted machine screws & hex bolts
 * - Painted warm cream resin faceplates with high-contrast debossed typography
 * - Segmented illuminated optical bar gauges and ambient status indicators
 * - Authentic Xbox and PlayStation ABXY color matrices
 *
 * Strictly adheres to NXPRC compiler contracts (zero CSS warnings, zero translateY jitter,
 * spring micro-physics, inline vector SVG detailing).
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
<meta charset="utf-8">
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
    damping: String = "0.68",
    stiffness: String = "440",
    pressScale: String = "0.93"
): String = """
:root {
  --spring-damping: $damping;
  --spring-stiffness: $stiffness;
  --press-scale: $pressScale;
  --cream: #eadfc2;
  --cream-dark: #c9b78d;
  --ink: #261307;
  --screw-brass: #8a7448;
}
@keyframes pulse-aura {
  0%, 100% { opacity: 0.45; }
  50% { opacity: 0.95; }
}
@keyframes glow-pulse {
  0%, 100% { opacity: 0.55; }
  50% { opacity: 1; }
}
@keyframes spin-dial {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
""".trimIndent()

private fun centeredPress(selector: String): String = """
$selector:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
}
""".trimIndent()

private fun faceMaterial(accent: String): Triple<String, String, String> = when (accent.uppercase()) {
    "#00FFA3", "#4ADE80", "#10B981" -> Triple("#E6F5EC", "#B0DAC0", "#3E664E") // Emerald
    "#FF2A6D", "#FF3366", "#F43F5E" -> Triple("#FDE8EE", "#EDA3B6", "#73243A") // Crimson Rose
    "#00E5FF", "#00B0FF", "#0284C7" -> Triple("#E2F4FA", "#93D0E8", "#1E5470") // Cyan Azure
    "#FFD600", "#FFCC00", "#F59E0B" -> Triple("#FFF5D8", "#ECC875", "#6D4C13") // Solar Amber
    "#3F8FE0" -> Triple("#E1EEFB", "#92BCE8", "#1C4E7E") // PS Blue
    "#E6474E" -> Triple("#FCE7E8", "#ECA0A5", "#732025") // PS Red
    "#E055B8" -> Triple("#FCE7F6", "#EDA5DC", "#732060") // PS Pink
    "#3FD25A" -> Triple("#E4F8E7", "#98E0A6", "#1F682F") // PS Green
    else -> Triple("#F3E9CD", "#D8C7A3", "#5C4625")      // Classic Cream
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
    val matTop = material.first
    val matMid = material.second
    val matDeep = material.third
    val motion = retroMotion("0.68", "440", "0.93")
    val selector = ".nexpad-btn"
    val active = centeredPress(selector)

    val css = """
$motion
:root {
  --accent-core: $accent;
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
  background: linear-gradient(160deg, #3d2b1d 0%, #20150d 55%, #0f0a06 100%);
  box-shadow:
    0 14px 28px rgba(0, 0, 0, 0.85),
    0 0 0 3px rgba(28, 18, 12, 0.95),
    0 0 24px $glow,
    inset 0 2px 4px rgba(255, 220, 180, 0.25),
    inset 0 -6px 14px rgba(0, 0, 0, 0.90);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
$selector::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  border: 0;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.15) 0%, transparent 45%),
    conic-gradient(from 180deg at 50% 50%, #2a1b12, #473223, #1c110b, #59402f, #2a1b12, #473223, #1c110b);
  box-shadow:
    inset 0 3px 6px rgba(255, 255, 255, 0.22),
    inset 0 -6px 14px rgba(0, 0, 0, 0.85);
  pointer-events: none;
  z-index: 1;
}
$selector::after {
  content: "";
  position: absolute;
  left: 14%;
  top: 6%;
  width: 72%;
  height: 40%;
  box-sizing: border-box;
  border-top: 1px solid rgba(255, 255, 255, 0.46);
  border-radius: 50%;
  background: transparent;
  transform: rotate(-10deg);
  pointer-events: none;
  z-index: 4;
}
/* The seed editor recognizes this tactile core token during rerolls. */
$selector .btn-core {
  background: linear-gradient(145deg, #221217 0%, #12060a 40%, #060203 100%);
}
$selector .face-plate {
  position: relative;
  width: 68px;
  height: 68px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid $accent;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, $glow 0%, transparent 40%),
    radial-gradient(circle at 65% 75%, rgba(0, 0, 0, 0.70) 0%, transparent 55%),
    linear-gradient(145deg, $matTop 0%, $matMid 45%, $matDeep 100%);
  box-shadow:
    inset 0 2px 5px rgba(255, 255, 255, 0.35),
    inset 0 -5px 10px rgba(0, 0, 0, 0.85);
  z-index: 3;
}
$selector .btn-label {
  position: relative;
  z-index: 5;
  font-size: 34px;
  font-weight: 900;
  line-height: 1;
  letter-spacing: 1px;
  color: #FFFDF5;
  text-shadow:
    0 1px 0 rgba(255, 255, 255, 0.88),
    0 -1px 0 rgba(0, 0, 0, 0.85),
    0 2px 0 rgba(0, 0, 0, 0.64),
    0 3px 5px rgba(0, 0, 0, 0.72);
}
$active
""".trimIndent()

    val body = """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-codename="Retro Foundry" data-name="$name" data-seed="$seed">
    <div class="face-plate btn-core" data-layer-role="detail">
      <span class="btn-label">$label</span>
    </div>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun triggerPreset(control: String): String {
    val right = control.uppercase() == "RT"
    val accent = if (right) "#DC2626" else "#F97316"
    val glow = if (right) "rgba(220, 38, 38, 0.65)" else "rgba(249, 115, 22, 0.65)"
    val seed = if (right) 63400 else 62900
    val name = "Tactile Trigger $control #$seed"
    val motion = retroMotion("0.68", "440", "0.95")
    val active = centeredPress(".trigger-btn")

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.trigger-btn {
  position: relative;
  width: 110px;
  height: 140px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 22px;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.65),
    0 0 26px var(--accent-glow),
    inset 0 2px 3px rgba(255, 200, 140, 0.25),
    inset 0 -4px 8px rgba(0, 0, 0, 0.7);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
}
.glow-ring {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 98px;
  height: 128px;
  border-radius: 22px;
  box-shadow: 0 0 30px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
.face-plate {
  position: absolute;
  left: 8px;
  top: 8px;
  width: 94px;
  height: 124px;
  border-radius: 16px 16px 24px 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0) 30%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.7),
    inset 0 -6px 10px rgba(90, 55, 20, 0.45),
    inset 3px 0 5px rgba(90, 55, 20, 0.2),
    inset -3px 0 5px rgba(90, 55, 20, 0.2);
}
.grip-ribs {
  position: absolute;
  left: 28px;
  top: 16px;
  width: 54px;
  height: 30px;
  border-radius: 8px;
  background: repeating-linear-gradient(
    180deg,
    rgba(59, 29, 10, 0.55) 0px,
    rgba(59, 29, 10, 0.55) 2px,
    rgba(255, 255, 255, 0.5) 2px,
    rgba(255, 255, 255, 0.5) 3px,
    transparent 3px,
    transparent 6px
  );
  box-shadow: inset 0 2px 4px rgba(59, 29, 10, 0.5);
}
.retro-stripes {
  position: absolute;
  left: 8px;
  top: 98px;
  width: 94px;
  height: 18px;
  border-radius: 0 0 20px 20px;
  background: linear-gradient(
    180deg,
    #f97316 0%,
    #f97316 33%,
    #dc2626 33%,
    #dc2626 66%,
    #7c2d12 66%,
    #7c2d12 100%
  );
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35);
}
.gauge-left,
.gauge-right {
  position: absolute;
  top: 16px;
  width: 6px;
  height: 74px;
  border-radius: 3px;
  background: repeating-linear-gradient(
    180deg,
    $accent 0px,
    $accent 6px,
    #3b1d0a 6px,
    #3b1d0a 9px
  );
  box-shadow: 0 0 8px var(--accent-glow);
  animation: glow-pulse 1.6s ease-in-out infinite;
}
.gauge-left { left: 16px; }
.gauge-right { left: 88px; }
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 110px;
  height: 140px;
  pointer-events: none;
}
.dial {
  position: absolute;
  left: 46px;
  top: 118px;
  width: 18px;
  height: 18px;
  transform-origin: 50% 50%;
  animation: spin-dial 6s linear infinite;
}
.btn-label {
  position: absolute;
  left: 0;
  top: 47px;
  width: 110px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  font-size: 28px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--ink);
  text-shadow: 2px 2px 0 var(--accent);
}
$active
""".trimIndent()

    val body = """
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-codename="Tactile Trigger" data-name="$name" data-seed="$seed">
    <div class="glow-ring"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="grip-ribs" data-layer-role="detail"></div>
    <div class="retro-stripes" data-layer-role="detail"></div>
    <div class="gauge-left" data-layer-role="detail"></div>
    <div class="gauge-right" data-layer-role="detail"></div>
    <svg class="button-emblem" viewBox="0 0 110 140" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="wingGrad_$control" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stop-color="#7c2d12"/>
          <stop offset="1" stop-color="$accent"/>
        </linearGradient>
      </defs>
      <!-- flanged grip wings -->
      <polygon points="8,52 1,60 1,84 8,92" fill="url(#wingGrad_$control)"/>
      <polygon points="102,52 109,60 109,84 102,92" fill="url(#wingGrad_$control)"/>
      <!-- pull chevrons -->
      <path d="M40 84 L55 91 L70 84" fill="none" stroke="#3b1d0a" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M40 79 L55 86 L70 79" fill="none" stroke="$accent" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <!-- corner screws -->
      <circle cx="16" cy="12" r="2.5" fill="#8a7448"/>
      <line x1="14.5" y1="12" x2="17.5" y2="12" stroke="#3b1d0a" stroke-width="1"/>
      <circle cx="94" cy="12" r="2.5" fill="#8a7448"/>
      <line x1="92.5" y1="12" x2="95.5" y2="12" stroke="#3b1d0a" stroke-width="1"/>
      <circle cx="16" cy="124" r="2.5" fill="#8a7448"/>
      <line x1="14.5" y1="124" x2="17.5" y2="124" stroke="#3b1d0a" stroke-width="1"/>
      <circle cx="94" cy="124" r="2.5" fill="#8a7448"/>
      <line x1="92.5" y1="124" x2="95.5" y2="124" stroke="#3b1d0a" stroke-width="1"/>
    </svg>
    <svg class="dial" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
      <circle cx="10" cy="10" r="8" fill="#2a1408" stroke="$accent" stroke-width="1"/>
      <path d="M10 2 L11.5 8.5 L18 10 L11.5 11.5 L10 18 L8.5 11.5 L2 10 L8.5 8.5 Z" fill="$accent"/>
      <circle cx="10" cy="10" r="2" fill="#eadfc2"/>
    </svg>
    <span class="btn-label">$control</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun bumperPreset(control: String): String {
    val right = control.uppercase() == "RB"
    val accent = if (right) "#DC2626" else "#F97316"
    val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(249, 115, 22, 0.45)"
    val motion = retroMotion("0.72", "480", "0.95")
    val active = centeredPress(".bumper-btn")

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.bumper-btn {
  position: relative;
  width: 120px;
  height: 52px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 17px;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.65),
    0 0 18px var(--accent-glow),
    inset 0 2px 2px rgba(255, 200, 140, 0.30),
    inset 0 -4px 6px rgba(0, 0, 0, 0.75);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bumper-btn::before {
  content: "";
  position: absolute;
  left: 12%;
  top: 10%;
  width: 76%;
  height: 35%;
  border: 1px solid rgba(255, 248, 232, 0.42);
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.30) 0%, transparent 80%);
  pointer-events: none;
  z-index: 3;
}
.bumper-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 2px;
  color: var(--cream);
  text-shadow: 1px 1px 0 $accent;
}
$active
""".trimIndent()

    val body = """
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-codename="Tactile Bumper" data-name="Tactile Bumper $control" data-seed="48200">
    <span class="bumper-label">$control</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun dpadDirectionPreset(control: String): String {
    val glyph = dpadGlyph(control)
    val motion = retroMotion("0.68", "460", "0.94")
    val active = centeredPress(".dpad-btn")
    val accent = when (control.uppercase()) {
        "DOWN", "RIGHT" -> "#DC2626"
        else -> "#F97316"
    }

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: rgba(249, 115, 22, 0.45);
}
.dpad-btn {
  position: relative;
  width: 88px;
  height: 88px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 18px;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 7px 18px rgba(0, 0, 0, 0.70),
    0 0 16px var(--accent-glow),
    inset 0 2px 2px rgba(255, 200, 140, 0.30),
    inset 0 -5px 8px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.glow-ring {
  position: absolute;
  left: 4px;
  top: 4px;
  width: 80px;
  height: 80px;
  border-radius: 16px;
  box-shadow: 0 0 20px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
.face-plate {
  position: absolute;
  left: 7px;
  top: 7px;
  width: 74px;
  height: 74px;
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0) 35%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.70),
    inset 0 -5px 8px rgba(90, 55, 20, 0.45);
}
.grip-ribs {
  position: absolute;
  left: 17px;
  top: 17px;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 1px dashed rgba(59, 29, 10, 0.40);
}
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 88px;
  height: 88px;
  pointer-events: none;
}
.dpad-glyph {
  position: relative;
  z-index: 5;
  font-size: 34px;
  color: var(--ink);
  text-shadow: 1px 1px 0 $accent, 0 2px 4px rgba(0, 0, 0, 0.40);
}
$active
""".trimIndent()

    val body = """
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-codename="Tactile D-Pad" data-name="Tactile D-Pad $control" data-seed="33100">
    <div class="glow-ring"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="grip-ribs" data-layer-role="detail"></div>
    <svg class="button-emblem" viewBox="0 0 88 88" xmlns="http://www.w3.org/2000/svg">
      <circle cx="12" cy="12" r="2.5" fill="#8a7448"/>
      <line x1="10.5" y1="12" x2="13.5" y2="12" stroke="#261307" stroke-width="1"/>
      <circle cx="76" cy="12" r="2.5" fill="#8a7448"/>
      <line x1="74.5" y1="12" x2="77.5" y2="12" stroke="#261307" stroke-width="1"/>
      <circle cx="12" cy="76" r="2.5" fill="#8a7448"/>
      <line x1="10.5" y1="76" x2="13.5" y2="76" stroke="#261307" stroke-width="1"/>
      <circle cx="76" cy="76" r="2.5" fill="#8a7448"/>
      <line x1="74.5" y1="76" x2="77.5" y2="76" stroke="#261307" stroke-width="1"/>
    </svg>
    <span class="dpad-glyph">$glyph</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun dpadCrossPreset(): String {
    val motion = retroMotion("0.68", "460", "0.95")
    val active = centeredPress(".dpad-cross")

    val css = """
$motion
:root {
  --accent: #F97316;
  --accent-glow: rgba(249, 115, 22, 0.45);
}
.dpad-cross {
  position: relative;
  width: 160px;
  height: 160px;
  box-sizing: border-box;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.80),
    0 0 24px var(--accent-glow),
    inset 0 3px 3px rgba(255, 200, 140, 0.30),
    inset 0 -6px 12px rgba(0, 0, 0, 0.85);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
}
.dpad-socket-trim {
  position: absolute;
  left: 8px;
  top: 8px;
  width: 144px;
  height: 144px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #2b1f16 0%, #160e08 65%, #0a0603 100%);
  box-shadow:
    inset 0 5px 10px rgba(0, 0, 0, 0.92),
    inset 0 -2px 3px rgba(255, 255, 255, 0.12);
  pointer-events: none;
}
.dpad-arm-v, .dpad-arm-h {
  position: absolute;
  border-radius: 12px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0) 30%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.70),
    inset 0 -4px 6px rgba(90, 55, 20, 0.45),
    0 4px 8px rgba(0, 0, 0, 0.50);
}
.dpad-arm-v { left: 56px; top: 12px; width: 48px; height: 136px; }
.dpad-arm-h { left: 12px; top: 56px; width: 136px; height: 48px; }
.dpad-pivot {
  position: absolute;
  left: 58px;
  top: 58px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #2a1408 0%, #140803 70%, #050201 100%);
  box-shadow:
    inset 0 4px 8px rgba(0, 0, 0, 0.90),
    0 2px 4px rgba(255, 255, 255, 0.20);
  z-index: 4;
}
.dpad-arrow {
  position: absolute;
  z-index: 5;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 900;
  color: var(--ink);
  text-shadow: 1px 1px 0 var(--accent);
}
.dpad-up { left: 66px; top: 18px; }
.dpad-right { left: 116px; top: 66px; }
.dpad-down { left: 66px; top: 114px; }
.dpad-left { left: 16px; top: 66px; }
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 160px;
  height: 160px;
  pointer-events: none;
  z-index: 2;
}
$active
""".trimIndent()

    val body = """
  <button class="dpad-btn dpad-cross" data-control="DPAD" data-category="DPAD" data-codename="Tactile D-Pad Cross" data-name="Tactile D-Pad Cross" data-seed="33500">
    <div class="dpad-socket-trim"></div>
    <div class="dpad-arm-v" data-layer-role="base"></div>
    <div class="dpad-arm-h" data-layer-role="base"></div>
    <div class="dpad-pivot" data-layer-role="detail"></div>
    <svg class="button-emblem" viewBox="0 0 160 160" xmlns="http://www.w3.org/2000/svg">
      <!-- brass corner screws in 4 quadrant wells -->
      <circle cx="24" cy="24" r="3" fill="#8a7448"/>
      <line x1="22" y1="24" x2="26" y2="24" stroke="#261307" stroke-width="1"/>
      <circle cx="136" cy="24" r="3" fill="#8a7448"/>
      <line x1="134" y1="24" x2="138" y2="24" stroke="#261307" stroke-width="1"/>
      <circle cx="24" cy="136" r="3" fill="#8a7448"/>
      <line x1="22" y1="136" x2="26" y2="136" stroke="#261307" stroke-width="1"/>
      <circle cx="136" cy="136" r="3" fill="#8a7448"/>
      <line x1="134" y1="136" x2="138" y2="136" stroke="#261307" stroke-width="1"/>
    </svg>
    <span class="dpad-arrow dpad-up">▲</span>
    <span class="dpad-arrow dpad-right">▶</span>
    <span class="dpad-arrow dpad-down">▼</span>
    <span class="dpad-arrow dpad-left">◀</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun joystickPreset(control: String): String {
    val right = control.uppercase() == "RS"
    val accent = if (right) "#DC2626" else "#F97316"
    val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(249, 115, 22, 0.45)"
    val motion = retroMotion("0.68", "440", "0.94")
    val active = centeredPress(".stick-btn")

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.stick-btn {
  position: relative;
  width: 150px;
  height: 150px;
  padding: 0;
  border: none;
  background: transparent;
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
}
.stick-base {
  position: absolute;
  left: 0;
  top: 0;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.80),
    0 0 20px var(--accent-glow),
    inset 0 3px 4px rgba(255, 200, 140, 0.25),
    inset 0 -8px 14px rgba(0, 0, 0, 0.85);
  z-index: 1;
}
.socket-well {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #2a1408 0%, #150903 65%, #060201 100%);
  box-shadow:
    inset 0 6px 12px rgba(0, 0, 0, 0.95),
    inset 0 -2px 3px rgba(255, 255, 255, 0.12);
}
.axis-ring {
  position: absolute;
  left: 26px;
  top: 26px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 1px dashed rgba(249, 115, 22, 0.35);
}
.stick-cap {
  position: absolute;
  left: 26px;
  top: 26px;
  width: 98px;
  height: 98px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.35) 0%, transparent 40%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.70),
    inset 0 -6px 10px rgba(90, 55, 20, 0.50),
    0 6px 14px rgba(0, 0, 0, 0.70);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
.knurled-ring {
  position: absolute;
  left: 16px;
  top: 16px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px dashed $accent;
  opacity: 0.55;
}
.cap-gloss {
  position: absolute;
  left: 22px;
  top: 10px;
  width: 54px;
  height: 20px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.50) 0%, transparent 75%);
}
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 150px;
  height: 150px;
  pointer-events: none;
  z-index: 2;
}
.stick-label {
  position: relative;
  z-index: 5;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--ink);
  text-shadow: 1px 1px 0 $accent;
}
$active
""".trimIndent()

    val body = """
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-codename="Tactile Stick" data-name="Tactile Analog Stick $control" data-seed="51200">
    <div class="stick-base" data-layer-role="base">
      <div class="socket-well" data-layer-role="base"></div>
      <div class="axis-ring" data-layer-role="base"></div>
    </div>
    <svg class="button-emblem" viewBox="0 0 150 150" xmlns="http://www.w3.org/2000/svg">
      <circle cx="16" cy="16" r="3" fill="#8a7448"/>
      <line x1="14" y1="16" x2="18" y2="16" stroke="#261307" stroke-width="1"/>
      <circle cx="134" cy="16" r="3" fill="#8a7448"/>
      <line x1="132" y1="16" x2="136" y2="16" stroke="#261307" stroke-width="1"/>
      <circle cx="16" cy="134" r="3" fill="#8a7448"/>
      <line x1="14" y1="134" x2="18" y2="134" stroke="#261307" stroke-width="1"/>
      <circle cx="134" cy="134" r="3" fill="#8a7448"/>
      <line x1="132" y1="134" x2="136" y2="134" stroke="#261307" stroke-width="1"/>
    </svg>
    <div class="stick-cap" data-layer-role="thumb-cap">
      <div class="knurled-ring" data-layer-role="thumb-cap"></div>
      <div class="cap-gloss" data-layer-role="thumb-cap"></div>
      <span class="stick-label">$control</span>
    </div>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun stickButtonPreset(control: String): String {
    val right = control.uppercase() == "RSB"
    val accent = if (right) "#DC2626" else "#F97316"
    val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(249, 115, 22, 0.45)"
    val motion = retroMotion("0.68", "460", "0.93")
    val active = centeredPress(".stick-btn-ctl")

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.stick-btn-ctl {
  position: relative;
  width: 70px;
  height: 70px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 6px 14px rgba(0, 0, 0, 0.70),
    0 0 16px var(--accent-glow),
    inset 0 2px 2px rgba(255, 200, 140, 0.30),
    inset 0 -4px 7px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.35) 0%, transparent 40%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.70),
    inset 0 -4px 6px rgba(90, 55, 20, 0.50);
  display: flex;
  align-items: center;
  justify-content: center;
}
.grip-ribs {
  position: absolute;
  left: 8px;
  top: 8px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px dashed $accent;
  opacity: 0.60;
}
.stick-btn-label {
  position: relative;
  z-index: 5;
  font-size: 15px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--ink);
  text-shadow: 1px 1px 0 $accent;
}
$active
""".trimIndent()

    val body = """
  <button class="stick-btn-ctl" data-control="$control" data-category="BUTTON" data-codename="Tactile Click" data-name="Tactile Click $control" data-seed="21900">
    <div class="face-plate" data-layer-role="base">
      <div class="grip-ribs" data-layer-role="detail"></div>
      <span class="stick-btn-label">$control</span>
    </div>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun touchpadPreset(control: String): String {
    val right = control.uppercase() == "RTP"
    val accent = if (right) "#DC2626" else "#F97316"
    val glow = if (right) "rgba(220, 38, 38, 0.40)" else "rgba(249, 115, 22, 0.40)"
    val title = if (right) "Touch Look • RTP" else "Touch Move • LTP"
    val touchId = "touch_" + control.lowercase()
    val motion = retroMotion("0.75", "500", "0.98")
    val active = centeredPress(".touchpad-ctl")

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.touchpad-ctl {
  position: relative;
  width: 180px;
  height: 180px;
  padding: 12px;
  box-sizing: border-box;
  border: none;
  outline: none;
  border-radius: 22px;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.75),
    0 0 20px var(--accent-glow),
    inset 0 3px 3px rgba(255, 200, 140, 0.25),
    inset 0 -6px 10px rgba(0, 0, 0, 0.85);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
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
  background:
    radial-gradient(circle at 45% 30%, rgba(255, 255, 255, 0.20) 0%, transparent 45%),
    linear-gradient(160deg, #2b1d14 0%, #160e09 55%, #0d0704 100%);
  box-shadow:
    inset 0 3px 6px rgba(0, 0, 0, 0.90),
    inset 0 -1px 2px rgba(255, 255, 255, 0.15);
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
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 180px;
  height: 180px;
  pointer-events: none;
  z-index: 3;
}
.touchpad-title, .touchpad-sub {
  position: relative;
  z-index: 5;
  color: var(--cream);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.80);
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
  <button class="touchpad-ctl" data-id="$touchId" data-control="$control" data-category="TOUCHPAD" data-name="Tactile Touchpad $control" data-seed="71500">
    <div class="touchpad-surface" data-layer-role="base"></div>
    <div class="touchpad-scanline" data-layer-role="detail"></div>
    <svg class="button-emblem" viewBox="0 0 180 180" xmlns="http://www.w3.org/2000/svg">
      <!-- 4 corner brass machine screws -->
      <circle cx="20" cy="20" r="3" fill="#8a7448"/>
      <line x1="18" y1="20" x2="22" y2="20" stroke="#261307" stroke-width="1"/>
      <circle cx="160" cy="20" r="3" fill="#8a7448"/>
      <line x1="158" y1="20" x2="162" y2="20" stroke="#261307" stroke-width="1"/>
      <circle cx="20" cy="160" r="3" fill="#8a7448"/>
      <line x1="18" y1="160" x2="22" y2="160" stroke="#261307" stroke-width="1"/>
      <circle cx="160" cy="160" r="3" fill="#8a7448"/>
      <line x1="158" y1="160" x2="162" y2="160" stroke="#261307" stroke-width="1"/>
    </svg>
    <span class="touchpad-title">$title</span>
    <span class="touchpad-sub">2.0X BALLISTICS</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun systemMenuPreset(): String {
    val motion = retroMotion("0.75", "500", "0.93")
    val active = centeredPress(".system-btn")

    val css = """
$motion
.system-btn {
  position: relative;
  width: 64px;
  height: 42px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 12px;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 5px 12px rgba(0, 0, 0, 0.70),
    0 0 12px rgba(249, 115, 22, 0.40),
    inset 0 2px 2px rgba(255, 200, 140, 0.25),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  cursor: pointer;
  overflow: visible;
  display: block;
}
.face-plate {
  position: absolute;
  left: 5px;
  top: 5px;
  width: 54px;
  height: 32px;
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.30) 0%, transparent 40%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 1px 2px rgba(255, 255, 255, 0.70),
    inset 0 -3px 5px rgba(90, 55, 20, 0.45);
}
.burger-bar {
  position: absolute;
  left: 17px;
  width: 20px;
  height: 3px;
  border-radius: 2px;
  background: #3b1d0a;
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.40);
  z-index: 4;
}
.burger-bar-1 { top: 7px; }
.burger-bar-2 { top: 14px; }
.burger-bar-3 { top: 21px; }
$active
""".trimIndent()

    val body = """
  <button class="system-btn" data-control="MENU" data-category="SYSTEM" data-codename="Tactile Menu" data-name="Tactile Menu" data-seed="11400">
    <div class="face-plate" data-layer-role="base">
      <div class="burger-bar burger-bar-1" data-layer-role="detail"></div>
      <div class="burger-bar burger-bar-2" data-layer-role="detail"></div>
      <div class="burger-bar burger-bar-3" data-layer-role="detail"></div>
    </div>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun macroPreset(control: String): String {
    val safeControl = control.trim().ifBlank { "M1" }.uppercase()
    val motion = retroMotion("0.70", "460", "0.94")
    val active = centeredPress(".macro-paddle")

    val css = """
$motion
:root {
  --accent: #F97316;
  --accent-glow: rgba(249, 115, 22, 0.45);
}
.macro-paddle {
  position: relative;
  width: 72px;
  height: 72px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 16px;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 6px 14px rgba(0, 0, 0, 0.70),
    0 0 16px var(--accent-glow),
    inset 0 2px 2px rgba(255, 200, 140, 0.30),
    inset 0 -4px 7px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0) 35%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.70),
    inset 0 -4px 6px rgba(90, 55, 20, 0.45);
}
.macro-bolt {
  position: absolute;
  left: 14px;
  top: 18px;
  z-index: 5;
  font-size: 18px;
  color: var(--accent);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.60);
}
.macro-label {
  position: relative;
  z-index: 5;
  margin-left: 16px;
  font-size: 20px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--ink);
  text-shadow: 1px 1px 0 var(--accent);
}
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 72px;
  height: 72px;
  pointer-events: none;
  z-index: 3;
}
$active
""".trimIndent()

    val body = """
  <button class="macro-paddle" data-control="$safeControl" data-category="MACRO" data-codename="Tactile Macro" data-name="Tactile Macro $safeControl">
    <div class="face-plate" data-layer-role="base"></div>
    <svg class="button-emblem" viewBox="0 0 72 72" xmlns="http://www.w3.org/2000/svg">
      <circle cx="10" cy="10" r="2" fill="#8a7448"/>
      <circle cx="62" cy="10" r="2" fill="#8a7448"/>
      <circle cx="10" cy="62" r="2" fill="#8a7448"/>
      <circle cx="62" cy="62" r="2" fill="#8a7448"/>
    </svg>
    <span class="macro-bolt">⚡</span>
    <span class="macro-label">$safeControl</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
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
  --spring-damping: 0.68;
  --spring-stiffness: 440;
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

    val PRESET_CYBER_REACTOR = htmlDocument(
        """
${retroMotion("0.68", "440", "0.93")}
:root {
  --accent-core: #10B981;
  --accent-glow: rgba(16, 185, 129, 0.45);
}
.reactor-button {
  position: relative;
  width: 94px;
  height: 94px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: linear-gradient(160deg, #3d2b1d 0%, #20150d 55%, #0f0a06 100%);
  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.75),
    0 0 24px var(--accent-glow),
    inset 0 2px 3px rgba(255, 220, 180, 0.30),
    inset 0 -5px 10px rgba(0, 0, 0, 0.85);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: relative;
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.40) 0%, transparent 45%),
    linear-gradient(150deg, #E6F5EC 0%, #B0DAC0 45%, #3E664E 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.75),
    inset 0 -5px 9px rgba(38, 19, 7, 0.60),
    0 4px 10px rgba(0, 0, 0, 0.60);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
.reactor-button-label {
  position: relative;
  z-index: 5;
  font-size: 32px;
  font-weight: 900;
  color: var(--ink);
  text-shadow: 1px 1px 0 var(--accent-core);
}
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 94px;
  height: 94px;
  pointer-events: none;
  z-index: 2;
}
${centeredPress(".reactor-button")}
        """.trimIndent(),
        """
  <button class="reactor-button" data-control="A" data-category="BUTTON" data-codename="Cyber Reactor" data-name="Cyber Reactor A" data-seed="19900">
    <svg class="button-emblem" viewBox="0 0 94 94" xmlns="http://www.w3.org/2000/svg">
      <circle cx="14" cy="14" r="2.5" fill="#8a7448"/>
      <circle cx="80" cy="14" r="2.5" fill="#8a7448"/>
      <circle cx="14" cy="80" r="2.5" fill="#8a7448"/>
      <circle cx="80" cy="80" r="2.5" fill="#8a7448"/>
    </svg>
    <div class="face-plate" data-layer-role="base">
      <span class="reactor-button-label">A</span>
    </div>
  </button>
        """.trimIndent()
    )

    val PRESET_CRIMSON_OCTA = htmlDocument(
        """
${retroMotion("0.68", "440", "0.93")}
:root {
  --accent-core: #F43F5E;
  --accent-glow: rgba(244, 63, 94, 0.45);
}
.crimson-octa {
  position: relative;
  width: 94px;
  height: 94px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 20px;
  clip-path: polygon(29% 0%, 71% 0%, 100% 29%, 100% 71%, 71% 100%, 29% 100%, 0% 71%, 0% 29%);
  background: linear-gradient(160deg, #3d2b1d 0%, #20150d 55%, #0f0a06 100%);
  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.75),
    0 0 24px var(--accent-glow),
    inset 0 2px 3px rgba(255, 220, 180, 0.30),
    inset 0 -5px 10px rgba(0, 0, 0, 0.85);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: relative;
  width: 66px;
  height: 66px;
  clip-path: polygon(29% 0%, 71% 0%, 100% 29%, 100% 71%, 71% 100%, 29% 100%, 0% 71%, 0% 29%);
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.40) 0%, transparent 45%),
    linear-gradient(150deg, #FDE8EE 0%, #EDA3B6 45%, #73243A 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
.crimson-octa-label {
  position: relative;
  z-index: 5;
  font-size: 32px;
  font-weight: 900;
  color: var(--ink);
  text-shadow: 1px 1px 0 var(--accent-core);
}
${centeredPress(".crimson-octa")}
        """.trimIndent(),
        """
  <button class="crimson-octa" data-control="B" data-category="BUTTON" data-codename="Tactile Octagon" data-name="Retro Octagon B" data-seed="28800">
    <div class="face-plate" data-layer-role="base">
      <span class="crimson-octa-label">B</span>
    </div>
  </button>
        """.trimIndent()
    )

    val PRESET_SPEED_TURBO = htmlDocument(
        """
${retroMotion("0.68", "440", "0.93")}
:root {
  --accent-core: #F59E0B;
  --accent-glow: rgba(245, 158, 11, 0.45);
}
.speed-turbo {
  position: relative;
  width: 104px;
  height: 74px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 20px;
  background: linear-gradient(160deg, #3d2b1d 0%, #20150d 55%, #0f0a06 100%);
  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.75),
    0 0 24px var(--accent-glow),
    inset 0 2px 3px rgba(255, 220, 180, 0.30),
    inset 0 -5px 10px rgba(0, 0, 0, 0.85);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
  cursor: pointer;
  overflow: visible;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: relative;
  width: 88px;
  height: 58px;
  border-radius: 16px;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.40) 0%, transparent 45%),
    linear-gradient(150deg, #FFF5D8 0%, #ECC875 45%, #6D4C13 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.75),
    inset 0 -4px 8px rgba(38, 19, 7, 0.60);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
.turbo-label {
  position: relative;
  z-index: 5;
  font-size: 28px;
  font-weight: 900;
  color: var(--ink);
  text-shadow: 1px 1px 0 var(--accent-core);
}
.turbo-sub {
  position: relative;
  z-index: 5;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 2px;
  color: var(--ink);
  opacity: 0.85;
}
${centeredPress(".speed-turbo")}
        """.trimIndent(),
        """
  <button class="speed-turbo" data-control="Y" data-category="BUTTON" data-codename="Speed Turbo" data-name="Retro Turbo Y" data-seed="39900">
    <div class="face-plate" data-layer-role="base">
      <span class="turbo-label">Y</span>
      <span class="turbo-sub">TURBO</span>
    </div>
  </button>
        """.trimIndent()
    )

    val PRESET_NEO_TACTILE_A =
        facePreset("A", "A", "#00FFA3", "rgba(0, 255, 163, 0.42)", "Neo Tactile A", 16166)
    val PRESET_NEO_TACTILE_B =
        facePreset("B", "B", "#FF2A6D", "rgba(255, 42, 109, 0.36)", "Neo Tactile B", 24892)
    val PRESET_NEO_TACTILE_X =
        facePreset("X", "X", "#00E5FF", "rgba(0, 229, 255, 0.38)", "Neo Tactile X", 38120)
    val PRESET_NEO_TACTILE_Y =
        facePreset("Y", "Y", "#FFD600", "rgba(255, 214, 0, 0.40)", "Neo Tactile Y", 49551)

    val PRESET_NEO_TACTILE_PS_CROSS =
        facePreset("A", "✕", "#3F8FE0", "rgba(63, 143, 224, 0.34)", "Neo Tactile Cross", 16166)
    val PRESET_NEO_TACTILE_PS_CIRCLE =
        facePreset("B", "○", "#E6474E", "rgba(230, 71, 78, 0.34)", "Neo Tactile Circle", 24892)
    val PRESET_NEO_TACTILE_PS_SQUARE =
        facePreset("X", "□", "#E055B8", "rgba(224, 85, 184, 0.34)", "Neo Tactile Square", 38120)
    val PRESET_NEO_TACTILE_PS_TRIANGLE =
        facePreset("Y", "△", "#3FD25A", "rgba(63, 210, 90, 0.34)", "Neo Tactile Triangle", 49551)

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
    val PRESET_SYSTEM_VIEW = htmlDocument(
        """
${retroMotion("0.75", "500", "0.93")}
.system-btn {
  position: relative;
  width: 64px;
  height: 42px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 12px;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 5px 12px rgba(0, 0, 0, 0.70),
    0 0 12px rgba(249, 115, 22, 0.40),
    inset 0 2px 2px rgba(255, 200, 140, 0.25),
    inset 0 -3px 6px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: relative;
  width: 54px;
  height: 32px;
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.30) 0%, transparent 40%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 1px 2px rgba(255, 255, 255, 0.70),
    inset 0 -3px 5px rgba(90, 55, 20, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}
.system-symbol {
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--ink);
}
${centeredPress(".system-btn")}
        """.trimIndent(),
        """
  <button class="system-btn" data-control="VIEW" data-category="SYSTEM" data-codename="Tactile View" data-name="Retro Foundry View" data-seed="11500">
    <div class="face-plate" data-layer-role="base">
      <span class="system-symbol">VIEW</span>
    </div>
  </button>
        """.trimIndent()
    )

    val PRESET_SYSTEM_HOME = htmlDocument(
        """
${retroMotion("0.75", "500", "0.93")}
:root {
  --accent: #F97316;
  --accent-glow: rgba(249, 115, 22, 0.45);
}
.system-btn {
  position: relative;
  width: 70px;
  height: 70px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.70),
    0 0 18px var(--accent-glow),
    inset 0 2px 2px rgba(255, 200, 140, 0.30),
    inset 0 -4px 8px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.40) 0%, transparent 45%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.70),
    inset 0 -4px 7px rgba(90, 55, 20, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}
.system-symbol {
  font-size: 28px;
  color: var(--ink);
  text-shadow: 1px 1px 0 var(--accent);
}
${centeredPress(".system-btn")}
        """.trimIndent(),
        """
  <button class="system-btn" data-control="HOME" data-category="SYSTEM" data-codename="Tactile Home" data-name="Retro Foundry Home" data-seed="11600">
    <div class="face-plate" data-layer-role="base">
      <span class="system-symbol">⌂</span>
    </div>
  </button>
        """.trimIndent()
    )

    val PRESET_SYSTEM_SHARE = htmlDocument(
        """
${retroMotion("0.75", "500", "0.93")}
:root {
  --accent: #F97316;
  --accent-glow: rgba(249, 115, 22, 0.45);
}
.system-btn {
  position: relative;
  width: 70px;
  height: 70px;
  padding: 0;
  border: none;
  outline: none;
  border-radius: 50%;
  background: linear-gradient(160deg, #4a2a14 0%, #2a1408 55%, #140803 100%);
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.70),
    0 0 18px var(--accent-glow),
    inset 0 2px 2px rgba(255, 200, 140, 0.30),
    inset 0 -4px 8px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  cursor: pointer;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-plate {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.40) 0%, transparent 45%),
    linear-gradient(160deg, #f3e9cd 0%, var(--cream) 45%, var(--cream-dark) 100%);
  box-shadow:
    inset 0 2px 2px rgba(255, 255, 255, 0.70),
    inset 0 -4px 7px rgba(90, 55, 20, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}
.system-symbol {
  font-size: 26px;
  color: var(--ink);
  text-shadow: 1px 1px 0 var(--accent);
}
${centeredPress(".system-btn")}
        """.trimIndent(),
        """
  <button class="system-btn" data-control="SHARE" data-category="SYSTEM" data-codename="Tactile Share" data-name="Retro Foundry Share" data-seed="11700">
    <div class="face-plate" data-layer-role="base">
      <span class="system-symbol">⇪</span>
    </div>
  </button>
        """.trimIndent()
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
    <span class="btn-label">$control</span>
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
    <span class="btn-label">$control</span>
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
