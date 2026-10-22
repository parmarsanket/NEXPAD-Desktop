package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey

enum class NxprcPresetStyle(
    val id: String,
    val displayName: String,
    val badge: String,
    val description: String
) {
    XBOX_ELITE("xbox_elite", "Xbox Elite Crystal", "Pro Luxury", "Clear resin dome with 3D floating neon glyph"),
    ARCADE("arcade", "Candy Cab Arcade", "Retro Arcade", "Sanwa-style glossy convex dome with crisp white label"),
    CYBERPUNK("cyberpunk", "Cyberpunk Mecha", "Sci-Fi HUD", "Chamfered titanium squircle with glowing HUD glyph"),
    SATIN("satin", "Satin DualSense", "Minimalist", "Frosted velvet matte cap with minimalist typography")
}

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


private fun facePresetXboxElite(
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
    radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.35) 0%, transparent 45%),
    linear-gradient(180deg, #18231e 0%, #0f1613 55%, #080c0a 100%);
  box-shadow:
    0 4px 10px rgba(0, 0, 0, 0.70),
    inset 0 1.5px 3px rgba(255, 255, 255, 0.40),
    inset 0 -4px 8px rgba(0, 0, 0, 0.80);
}
$selector .specular-arc {
  position: absolute;
  left: 22px;
  top: 16px;
  width: 52px;
  height: 24px;
  border-radius: 50%;
  border-top: 2px solid rgba(255, 255, 255, 0.70);
  transform: rotate(-10deg);
  pointer-events: none;
}
$selector .caustic-glow {
  position: absolute;
  left: 24px;
  top: 66px;
  width: 48px;
  height: 16px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 50%, var(--accent-glow) 0%, transparent 70%);
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

private fun facePresetArcade(
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
        "#00FFA3", "#4ADE80", "#10B981" -> Pair("#4ade80", "#065f46")
        "#FF2A6D", "#FF3366", "#F43F5E", "#EF4444" -> Pair("#f87171", "#7f1d1d")
        "#00E5FF", "#00B0FF", "#0284C7" -> Pair("#38bdf8", "#0c4a6e")
        "#FFD600", "#FFCC00", "#F59E0B" -> Pair("#fde047", "#78350f")
        else -> Pair("#cbd5e1", "#334155")
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
  background: linear-gradient(180deg, #2e2e2e 0%, #151515 50%, #0a0a0a 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.80),
    0 0 22px var(--accent-glow),
    inset 0 2px 2px rgba(255, 255, 255, 0.25),
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
  background: radial-gradient(circle at 45% 40%, #1f1f1f 0%, #0d0d0d 100%);
  box-shadow: inset 0 5px 8px rgba(0, 0, 0, 0.95);
}
$selector .face-plate {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 25%, var(--dome-light) 0%, var(--accent-core) 45%, var(--dome-dark) 100%);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.65),
    inset 0 3px 4px rgba(255, 255, 255, 0.50),
    inset 0 -5px 10px rgba(0, 0, 0, 0.65);
}
$selector .arcade-sheen {
  position: absolute;
  left: 20px;
  top: 16px;
  width: 56px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.45) 0%, rgba(255, 255, 255, 0) 100%);
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
    0 2px 4px rgba(0, 0, 0, 0.65),
    0 -1px 1px rgba(0, 0, 0, 0.45);
}
$active
""".trimIndent()

    val body = """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-codename="Candy Cab" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="socket-well"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="arcade-sheen"></div>
    <span class="btn-label">$label</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun facePresetCyberpunk(
    control: String,
    label: String,
    accent: String,
    glow: String,
    name: String,
    seed: Int
): String {
    val motion = retroMotion("0.60", "480", "0.94")
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
  background: linear-gradient(145deg, #1b2028 0%, #0e1117 60%, #07090c 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.90),
    0 0 25px var(--accent-glow),
    inset 0 1px 2px rgba(255, 255, 255, 0.15),
    inset 0 -4px 8px rgba(0, 0, 0, 0.80);
  transform-origin: 50% 50%;
  font-family: "Courier New", Courier, monospace;
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
  box-shadow: 0 0 26px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
$selector .face-plate {
  position: absolute;
  left: 10px;
  top: 10px;
  width: 76px;
  height: 76px;
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(6, 182, 212, 0.08) 0%, transparent 60%),
    linear-gradient(145deg, #151922 0%, #0b0d13 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.12),
    inset 0 -3px 6px rgba(0, 0, 0, 0.70);
}
$selector .cyber-grid {
  position: absolute;
  left: 16px;
  top: 16px;
  width: 64px;
  height: 64px;
  border: 1px dashed rgba(6, 182, 212, 0.25);
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
    0 0 10px var(--accent-core),
    0 0 24px var(--accent-glow),
    0 0 42px var(--accent-glow);
}
$active
""".trimIndent()

    val body = """
  <button class="nexpad-btn" data-control="$control" data-category="BUTTON" data-codename="Cyberpunk" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
    <div class="glow-ring"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cyber-grid"></div>
    <svg class="button-emblem" viewBox="0 0 96 96" xmlns="http://www.w3.org/2000/svg">
      <path d="M 14 20 L 14 14 L 20 14" stroke="$accent" stroke-width="2" fill="none"/>
      <path d="M 82 76 L 82 82 L 76 82" stroke="$accent" stroke-width="2" fill="none"/>
    </svg>
    <span class="btn-label">$label</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun facePresetSatin(
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
  background: linear-gradient(160deg, #242831 0%, #15181f 60%, #0d0f14 100%);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.75),
    0 0 20px rgba(148, 163, 184, 0.20),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.18),
    inset 0 -5px 10px rgba(0, 0, 0, 0.85);
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
  box-shadow: 0 0 22px rgba(148, 163, 184, 0.25);
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
  background: radial-gradient(circle at 45% 40%, #1c1f26 0%, #0c0e12 100%);
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.90);
}
$selector .face-plate {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 30%, #323744 0%, #1e222b 60%, #13151b 100%);
  box-shadow:
    0 4px 10px rgba(0, 0, 0, 0.50),
    inset 0 1.5px 2px rgba(255, 255, 255, 0.25),
    inset 0 -3px 6px rgba(0, 0, 0, 0.60);
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
    0 2px 4px rgba(0, 0, 0, 0.65),
    0 -1px 1px rgba(255, 255, 255, 0.25);
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

private fun facePreset(
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


private data class StyleTheme(
    val chassisBg: String,
    val chassisShadow: String,
    val facePlateBg: String,
    val facePlateShadow: String,
    val textColor: String,
    val textShadow: String,
    val accentBorder: String = "none",
    val fontFam: String = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    val barColor: String = "#ffffff",
    val barShadow: String = "none"
)

private fun resolveStyleTheme(style: NxprcPresetStyle, accent: String, glow: String): StyleTheme = when (style) {
    NxprcPresetStyle.XBOX_ELITE -> StyleTheme(
        chassisBg = "radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%)",
        chassisShadow = "0 8px 22px rgba(0, 0, 0, 0.85), 0 0 22px $glow, inset 0 2px 3px rgba(255, 255, 255, 0.15), inset 0 -6px 12px rgba(0, 0, 0, 0.95)",
        facePlateBg = "linear-gradient(180deg, rgba(255, 255, 255, 0.15) 0%, transparent 35%), linear-gradient(160deg, #1c232d 0%, #11151c 55%, #080a0e 100%)",
        facePlateShadow = "inset 0 1.5px 3px rgba(255, 255, 255, 0.25), inset 0 -6px 10px rgba(0, 0, 0, 0.80)",
        textColor = accent,
        textShadow = "0 2px 8px rgba(0, 0, 0, 0.95), 0 0 14px $glow",
        accentBorder = "none",
        fontFam = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
        barColor = accent,
        barShadow = "0 0 8px $glow"
    )
    NxprcPresetStyle.ARCADE -> StyleTheme(
        chassisBg = "linear-gradient(180deg, #2c2c2c 0%, #181818 50%, #0d0d0d 100%)",
        chassisShadow = "0 8px 22px rgba(0, 0, 0, 0.80), 0 0 20px $glow, inset 0 2px 2px rgba(255, 255, 255, 0.30), inset 0 -5px 10px rgba(0, 0, 0, 0.90)",
        facePlateBg = "radial-gradient(circle at 35% 25%, rgba(255, 255, 255, 0.50) 0%, $accent 48%, #064e3b 100%)",
        facePlateShadow = "0 4px 12px rgba(0, 0, 0, 0.65), inset 0 3px 4px rgba(255, 255, 255, 0.55), inset 0 -5px 10px rgba(0, 0, 0, 0.65)",
        textColor = "#ffffff",
        textShadow = "0 2px 4px rgba(0, 0, 0, 0.70), 0 -1px 1px rgba(0, 0, 0, 0.50)",
        accentBorder = "none",
        fontFam = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
        barColor = "#ffffff",
        barShadow = "0 1px 3px rgba(0, 0, 0, 0.70)"
    )
    NxprcPresetStyle.CYBERPUNK -> StyleTheme(
        chassisBg = "linear-gradient(145deg, #1b2028 0%, #0e1117 60%, #07090c 100%)",
        chassisShadow = "0 8px 24px rgba(0, 0, 0, 0.90), 0 0 25px $glow, inset 0 1px 2px rgba(255, 255, 255, 0.15), inset 0 -4px 8px rgba(0, 0, 0, 0.80)",
        facePlateBg = "linear-gradient(180deg, rgba(6, 182, 212, 0.12) 0%, transparent 60%), linear-gradient(145deg, #151922 0%, #0b0d13 100%)",
        facePlateShadow = "inset 0 1px 1px rgba(255, 255, 255, 0.18), inset 0 -4px 8px rgba(0, 0, 0, 0.85)",
        textColor = accent,
        textShadow = "0 0 10px $accent, 0 0 24px $glow, 0 0 42px $glow",
        accentBorder = "1.5px solid $glow",
        fontFam = "'Courier New', Courier, monospace",
        barColor = accent,
        barShadow = "0 0 10px $accent"
    )
    NxprcPresetStyle.SATIN -> StyleTheme(
        chassisBg = "linear-gradient(160deg, #242831 0%, #15181f 60%, #0d0f14 100%)",
        chassisShadow = "0 8px 22px rgba(0, 0, 0, 0.75), 0 0 18px rgba(148, 163, 184, 0.20), inset 0 1.5px 2px rgba(255, 255, 255, 0.18), inset 0 -5px 10px rgba(0, 0, 0, 0.85)",
        facePlateBg = "radial-gradient(circle at 40% 30%, #353b49 0%, #20242e 60%, #13161c 100%)",
        facePlateShadow = "0 4px 10px rgba(0, 0, 0, 0.50), inset 0 1.5px 2px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.65)",
        textColor = "#e2e8f0",
        textShadow = "0 2px 4px rgba(0, 0, 0, 0.65), 0 -1px 1px rgba(255, 255, 255, 0.25)",
        accentBorder = "none",
        fontFam = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
        barColor = "#e2e8f0",
        barShadow = "0 1px 2px rgba(0, 0, 0, 0.50)"
    )
}

private fun triggerPreset(
    control: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val right = control.uppercase() == "RT"
    val accent = if (right) "#DC2626" else "#10B981"
    val glow = if (right) "rgba(220, 38, 38, 0.55)" else "rgba(16, 185, 129, 0.55)"
    val seed = if (right) 63400 else 62900
    val name = "${style.displayName} Trigger $control #$seed"
    val motion = retroMotion("0.68", "440", "0.95")
    val active = centeredPress(".trigger-btn")
    val theme = resolveStyleTheme(style, accent, glow)

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
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 22px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
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
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
}
$active
""".trimIndent()

    val body = """
  <button class="trigger-btn" data-control="$control" data-category="TRIGGER" data-codename="${style.displayName}" data-name="$name" data-seed="$seed" id="rc.${control.lowercase()}_$seed">
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

private fun bumperPreset(
    control: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val right = control.uppercase() == "RB"
    val accent = if (right) "#DC2626" else "#10B981"
    val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(16, 185, 129, 0.45)"
    val motion = retroMotion("0.72", "480", "0.95")
    val active = centeredPress(".bumper-btn")
    val theme = resolveStyleTheme(style, accent, glow)

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
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 17px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
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
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
}
$active
""".trimIndent()

    val body = """
  <button class="bumper-btn" data-control="$control" data-category="BUMPER" data-codename="${style.displayName}" data-name="${style.displayName} Bumper $control" data-seed="48200" id="rc.${control.lowercase()}_48200">
    <span class="bumper-label">$control</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun dpadDirectionPreset(
    control: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val glyph = dpadGlyph(control)
    val motion = retroMotion("0.68", "460", "0.94")
    val active = centeredPress(".dpad-btn")
    val accent = when (control.uppercase()) {
        "DOWN", "RIGHT" -> "#DC2626"
        else -> "#10B981"
    }
    val glow = "rgba(16, 185, 129, 0.45)"
    val theme = resolveStyleTheme(style, accent, glow)

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.dpad-btn {
  position: relative;
  width: 88px;
  height: 88px;
  padding: 0;
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 18px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
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
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
}
$active
""".trimIndent()

    val body = """
  <button class="dpad-btn" data-control="$control" data-category="DPAD" data-codename="${style.displayName}" data-name="${style.displayName} D-Pad $control" data-seed="33100" id="rc.dpad_${control.lowercase()}">
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

private fun dpadCrossPreset(
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val motion = retroMotion("0.68", "460", "0.95")
    val active = centeredPress(".dpad-cross")
    val accent = "#10B981"
    val glow = "rgba(16, 185, 129, 0.45)"
    val theme = resolveStyleTheme(style, accent, glow)

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.dpad-cross {
  position: relative;
  width: 160px;
  height: 160px;
  padding: 0;
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 36px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
  cursor: pointer;
  overflow: visible;
}
.glow-ring {
  position: absolute;
  left: 6px;
  top: 6px;
  width: 148px;
  height: 148px;
  border-radius: 32px;
  box-shadow: 0 0 24px var(--accent-glow);
  animation: pulse-aura 2.4s ease-in-out infinite;
  pointer-events: none;
}
.face-plate {
  position: absolute;
  left: 10px;
  top: 10px;
  width: 140px;
  height: 140px;
  border-radius: 28px;
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
}
.cross-center {
  position: absolute;
  left: 56px;
  top: 56px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #15191f 0%, #080a0e 100%);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.90);
}
.button-emblem {
  position: absolute;
  left: 0;
  top: 0;
  width: 160px;
  height: 160px;
  pointer-events: none;
}
.dpad-arrow {
  position: absolute;
  font-size: 22px;
  font-weight: 900;
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
  z-index: 5;
}
.dpad-up    { left: 69px; top: 18px; }
.dpad-right { right: 18px; top: 69px; }
.dpad-down  { left: 69px; bottom: 18px; }
.dpad-left  { left: 18px; top: 69px; }
$active
""".trimIndent()

    val body = """
  <button class="dpad-cross" data-control="DPAD" data-category="DPAD" data-codename="${style.displayName}" data-name="${style.displayName} D-Pad Cross" data-seed="31000" id="rc.dpad_cross">
    <div class="glow-ring"></div>
    <div class="face-plate" data-layer-role="base"></div>
    <div class="cross-center" data-layer-role="detail"></div>
    <svg class="button-emblem" viewBox="0 0 160 160" xmlns="http://www.w3.org/2000/svg">
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

private fun joystickPreset(
    control: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val right = control.uppercase() == "RS"
    val accent = if (right) "#DC2626" else "#10B981"
    val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(16, 185, 129, 0.45)"
    val motion = retroMotion("0.68", "440", "0.94")
    val active = centeredPress(".stick-btn")
    val theme = resolveStyleTheme(style, accent, glow)

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
  font-family: ${theme.fontFam};
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
  border: ${theme.accentBorder};
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  z-index: 1;
}
.socket-well {
  position: absolute;
  left: 12px;
  top: 12px;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: radial-gradient(circle at 45% 40%, #15191f 0%, #0b0d11 65%, #050608 100%);
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
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
  opacity: 0.65;
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
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
}
$active
""".trimIndent()

    val body = """
  <button class="stick-btn" data-control="$control" data-category="JOYSTICK" data-codename="${style.displayName}" data-name="${style.displayName} Analog Stick $control" data-seed="51200" id="rc.stick_${control.lowercase()}">
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

private fun stickButtonPreset(
    control: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val right = control.uppercase() == "RSB"
    val accent = if (right) "#DC2626" else "#10B981"
    val glow = if (right) "rgba(220, 38, 38, 0.45)" else "rgba(16, 185, 129, 0.45)"
    val motion = retroMotion("0.68", "460", "0.93")
    val active = centeredPress(".stick-btn-ctl")
    val theme = resolveStyleTheme(style, accent, glow)

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
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 50%;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
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
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
}
$active
""".trimIndent()

    val body = """
  <button class="stick-btn-ctl" data-control="$control" data-category="BUTTON" data-codename="${style.displayName}" data-name="${style.displayName} Click $control" data-seed="21900" id="rc.click_${control.lowercase()}">
    <div class="face-plate" data-layer-role="base">
      <div class="grip-ribs" data-layer-role="detail"></div>
      <span class="stick-btn-label">$control</span>
    </div>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun touchpadPreset(
    control: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val right = control.uppercase() == "RTP"
    val accent = if (right) "#DC2626" else "#10B981"
    val glow = if (right) "rgba(220, 38, 38, 0.40)" else "rgba(16, 185, 129, 0.40)"
    val title = if (right) "Touch Look • RTP" else "Touch Move • LTP"
    val touchId = "touch_" + control.lowercase()
    val motion = retroMotion("0.75", "500", "0.98")
    val active = centeredPress(".touchpad-ctl")
    val theme = resolveStyleTheme(style, accent, glow)

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
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 22px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
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
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
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
    <svg class="button-emblem" viewBox="0 0 180 180" xmlns="http://www.w3.org/2000/svg">
      <!-- 4 corner machine screws -->
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

private fun systemPreset(
    control: String,
    label: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val motion = retroMotion("0.75", "500", "0.93")
    val active = centeredPress(".system-btn")
    val accent = "#10B981"
    val glow = "rgba(16, 185, 129, 0.40)"
    val theme = resolveStyleTheme(style, accent, glow)

    val css = """
$motion
.system-btn {
  position: relative;
  width: 64px;
  height: 42px;
  padding: 0;
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 12px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
}
.btn-label {
  position: absolute;
  left: 0;
  top: 6px;
  width: 64px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
}
$active
""".trimIndent()

    val body = """
  <button class="system-btn" data-control="$control" data-category="SYSTEM" data-codename="${style.displayName}" data-name="${style.displayName} $control" data-seed="11400" id="rc.sys_${control.lowercase()}">
    <div class="face-plate" data-layer-role="base"></div>
    <span class="btn-label">$label</span>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun systemMenuPreset(
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val motion = retroMotion("0.75", "500", "0.93")
    val active = centeredPress(".system-btn")
    val accent = "#10B981"
    val glow = "rgba(16, 185, 129, 0.40)"
    val theme = resolveStyleTheme(style, accent, glow)

    val css = """
$motion
.system-btn {
  position: relative;
  width: 64px;
  height: 42px;
  padding: 0;
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 12px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
}
.burger-bar {
  position: absolute;
  left: 17px;
  width: 20px;
  height: 3px;
  border-radius: 2px;
  background: ${theme.barColor};
  box-shadow: ${theme.barShadow};
  z-index: 4;
}
.burger-bar-1 { top: 7px; }
.burger-bar-2 { top: 14px; }
.burger-bar-3 { top: 21px; }
$active
""".trimIndent()

    val body = """
  <button class="system-btn" data-control="MENU" data-category="SYSTEM" data-codename="${style.displayName}" data-name="${style.displayName} Menu" data-seed="11400" id="rc.sys_menu">
    <div class="face-plate" data-layer-role="base">
      <div class="burger-bar burger-bar-1" data-layer-role="detail"></div>
      <div class="burger-bar burger-bar-2" data-layer-role="detail"></div>
      <div class="burger-bar burger-bar-3" data-layer-role="detail"></div>
    </div>
  </button>
    """.trimIndent()

    return htmlDocument(css, body)
}

private fun systemViewPreset(style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE): String = systemPreset("BACK", "⧉", style)
private fun systemHomePreset(style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE): String = systemPreset("GUIDE", "⌂", style)
private fun systemSharePreset(style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE): String = systemPreset("SHARE", "⇪", style)

private fun macroPreset(
    control: String,
    style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
): String {
    val safeControl = control.trim().ifBlank { "M1" }.uppercase()
    val motion = retroMotion("0.70", "460", "0.94")
    val active = centeredPress(".macro-paddle")
    val accent = "#10B981"
    val glow = "rgba(16, 185, 129, 0.45)"
    val theme = resolveStyleTheme(style, accent, glow)

    val css = """
$motion
:root {
  --accent: $accent;
  --accent-glow: $glow;
}
.macro-paddle {
  position: relative;
  width: 72px;
  height: 72px;
  padding: 0;
  border: ${theme.accentBorder};
  outline: none;
  border-radius: 16px;
  background: ${theme.chassisBg};
  box-shadow: ${theme.chassisShadow};
  transform-origin: 50% 50%;
  font-family: ${theme.fontFam};
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
  background: ${theme.facePlateBg};
  box-shadow: ${theme.facePlateShadow};
}
.macro-bolt {
  position: absolute;
  left: 14px;
  top: 18px;
  z-index: 5;
  font-size: 18px;
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
}
.macro-label {
  position: relative;
  z-index: 5;
  margin-left: 16px;
  font-size: 20px;
  font-weight: 900;
  letter-spacing: 1px;
  color: ${theme.textColor};
  text-shadow: ${theme.textShadow};
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
  <button class="macro-paddle" data-control="$safeControl" data-category="MACRO" data-codename="${style.displayName}" data-name="${style.displayName} Macro $safeControl" id="rc.macro_${safeControl.lowercase()}">
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

    // --- Xbox Elite Crystal Resin Presets ---
    val PRESET_XBOX_ELITE_A = facePreset("A", "A", "#00FFA3", "rgba(0, 255, 163, 0.42)", "Xbox Elite A", 16166, NxprcPresetStyle.XBOX_ELITE)
    val PRESET_XBOX_ELITE_B = facePreset("B", "B", "#FF2A6D", "rgba(255, 42, 109, 0.36)", "Xbox Elite B", 24892, NxprcPresetStyle.XBOX_ELITE)
    val PRESET_XBOX_ELITE_X = facePreset("X", "X", "#00E5FF", "rgba(0, 229, 255, 0.38)", "Xbox Elite X", 38120, NxprcPresetStyle.XBOX_ELITE)
    val PRESET_XBOX_ELITE_Y = facePreset("Y", "Y", "#FFD600", "rgba(255, 214, 0, 0.40)", "Xbox Elite Y", 49551, NxprcPresetStyle.XBOX_ELITE)
    val PRESET_XBOX_ELITE_PS_CROSS = facePreset("A", "✕", "#3F8FE0", "rgba(63, 143, 224, 0.34)", "Xbox Elite Cross", 16166, NxprcPresetStyle.XBOX_ELITE)
    val PRESET_XBOX_ELITE_PS_CIRCLE = facePreset("B", "○", "#E6474E", "rgba(230, 71, 78, 0.34)", "Xbox Elite Circle", 24892, NxprcPresetStyle.XBOX_ELITE)
    val PRESET_XBOX_ELITE_PS_SQUARE = facePreset("X", "□", "#E055B8", "rgba(224, 85, 184, 0.34)", "Xbox Elite Square", 38120, NxprcPresetStyle.XBOX_ELITE)
    val PRESET_XBOX_ELITE_PS_TRIANGLE = facePreset("Y", "△", "#3FD25A", "rgba(63, 210, 90, 0.34)", "Xbox Elite Triangle", 49551, NxprcPresetStyle.XBOX_ELITE)

    // --- Candy Cab Arcade Presets ---
    val PRESET_ARCADE_A = facePreset("A", "A", "#00FFA3", "rgba(0, 255, 163, 0.42)", "Candy Cab A", 16166, NxprcPresetStyle.ARCADE)
    val PRESET_ARCADE_B = facePreset("B", "B", "#FF2A6D", "rgba(255, 42, 109, 0.36)", "Candy Cab B", 24892, NxprcPresetStyle.ARCADE)
    val PRESET_ARCADE_X = facePreset("X", "X", "#00E5FF", "rgba(0, 229, 255, 0.38)", "Candy Cab X", 38120, NxprcPresetStyle.ARCADE)
    val PRESET_ARCADE_Y = facePreset("Y", "Y", "#FFD600", "rgba(255, 214, 0, 0.40)", "Candy Cab Y", 49551, NxprcPresetStyle.ARCADE)
    val PRESET_ARCADE_PS_CROSS = facePreset("A", "✕", "#3F8FE0", "rgba(63, 143, 224, 0.34)", "Candy Cab Cross", 16166, NxprcPresetStyle.ARCADE)
    val PRESET_ARCADE_PS_CIRCLE = facePreset("B", "○", "#E6474E", "rgba(230, 71, 78, 0.34)", "Candy Cab Circle", 24892, NxprcPresetStyle.ARCADE)
    val PRESET_ARCADE_PS_SQUARE = facePreset("X", "□", "#E055B8", "rgba(224, 85, 184, 0.34)", "Candy Cab Square", 38120, NxprcPresetStyle.ARCADE)
    val PRESET_ARCADE_PS_TRIANGLE = facePreset("Y", "△", "#3FD25A", "rgba(63, 210, 90, 0.34)", "Candy Cab Triangle", 49551, NxprcPresetStyle.ARCADE)

    // --- Cyberpunk Mecha Presets ---
    val PRESET_CYBERPUNK_A = facePreset("A", "A", "#00FFA3", "rgba(0, 255, 163, 0.42)", "Cyberpunk A", 16166, NxprcPresetStyle.CYBERPUNK)
    val PRESET_CYBERPUNK_B = facePreset("B", "B", "#FF2A6D", "rgba(255, 42, 109, 0.36)", "Cyberpunk B", 24892, NxprcPresetStyle.CYBERPUNK)
    val PRESET_CYBERPUNK_X = facePreset("X", "X", "#00E5FF", "rgba(0, 229, 255, 0.38)", "Cyberpunk X", 38120, NxprcPresetStyle.CYBERPUNK)
    val PRESET_CYBERPUNK_Y = facePreset("Y", "Y", "#FFD600", "rgba(255, 214, 0, 0.40)", "Cyberpunk Y", 49551, NxprcPresetStyle.CYBERPUNK)
    val PRESET_CYBERPUNK_PS_CROSS = facePreset("A", "✕", "#3F8FE0", "rgba(63, 143, 224, 0.34)", "Cyberpunk Cross", 16166, NxprcPresetStyle.CYBERPUNK)
    val PRESET_CYBERPUNK_PS_CIRCLE = facePreset("B", "○", "#E6474E", "rgba(230, 71, 78, 0.34)", "Cyberpunk Circle", 24892, NxprcPresetStyle.CYBERPUNK)
    val PRESET_CYBERPUNK_PS_SQUARE = facePreset("X", "□", "#E055B8", "rgba(224, 85, 184, 0.34)", "Cyberpunk Square", 38120, NxprcPresetStyle.CYBERPUNK)
    val PRESET_CYBERPUNK_PS_TRIANGLE = facePreset("Y", "△", "#3FD25A", "rgba(63, 210, 90, 0.34)", "Cyberpunk Triangle", 49551, NxprcPresetStyle.CYBERPUNK)

    // --- Satin DualSense Presets ---
    val PRESET_SATIN_A = facePreset("A", "A", "#00FFA3", "rgba(0, 255, 163, 0.42)", "Satin DualSense A", 16166, NxprcPresetStyle.SATIN)
    val PRESET_SATIN_B = facePreset("B", "B", "#FF2A6D", "rgba(255, 42, 109, 0.36)", "Satin DualSense B", 24892, NxprcPresetStyle.SATIN)
    val PRESET_SATIN_X = facePreset("X", "X", "#00E5FF", "rgba(0, 229, 255, 0.38)", "Satin DualSense X", 38120, NxprcPresetStyle.SATIN)
    val PRESET_SATIN_Y = facePreset("Y", "Y", "#FFD600", "rgba(255, 214, 0, 0.40)", "Satin DualSense Y", 49551, NxprcPresetStyle.SATIN)
    val PRESET_SATIN_PS_CROSS = facePreset("A", "✕", "#3F8FE0", "rgba(63, 143, 224, 0.34)", "Satin DualSense Cross", 16166, NxprcPresetStyle.SATIN)
    val PRESET_SATIN_PS_CIRCLE = facePreset("B", "○", "#E6474E", "rgba(230, 71, 78, 0.34)", "Satin DualSense Circle", 24892, NxprcPresetStyle.SATIN)
    val PRESET_SATIN_PS_SQUARE = facePreset("X", "□", "#E055B8", "rgba(224, 85, 184, 0.34)", "Satin DualSense Square", 38120, NxprcPresetStyle.SATIN)
    val PRESET_SATIN_PS_TRIANGLE = facePreset("Y", "△", "#3FD25A", "rgba(63, 210, 90, 0.34)", "Satin DualSense Triangle", 49551, NxprcPresetStyle.SATIN)

    // --- Canonical aliases defaulting to Xbox Elite Crystal ---
    val PRESET_NEO_TACTILE_A get() = PRESET_XBOX_ELITE_A
    val PRESET_NEO_TACTILE_B get() = PRESET_XBOX_ELITE_B
    val PRESET_NEO_TACTILE_X get() = PRESET_XBOX_ELITE_X
    val PRESET_NEO_TACTILE_Y get() = PRESET_XBOX_ELITE_Y

    val PRESET_NEO_TACTILE_PS_CROSS get() = PRESET_XBOX_ELITE_PS_CROSS
    val PRESET_NEO_TACTILE_PS_CIRCLE get() = PRESET_XBOX_ELITE_PS_CIRCLE
    val PRESET_NEO_TACTILE_PS_SQUARE get() = PRESET_XBOX_ELITE_PS_SQUARE
    val PRESET_NEO_TACTILE_PS_TRIANGLE get() = PRESET_XBOX_ELITE_PS_TRIANGLE

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

    fun getReferenceTemplate(control: String): String = getReferenceTemplateInternal(control, "BUTTON", ConsoleMode.XBOX, NxprcPresetStyle.XBOX_ELITE)

    fun getReferenceTemplate(control: String, category: String): String =
        getReferenceTemplateInternal(control, category, ConsoleMode.XBOX, NxprcPresetStyle.XBOX_ELITE)

    fun getReferenceTemplate(control: String, category: String, consoleMode: ConsoleMode): String =
        getReferenceTemplateInternal(control, category, consoleMode, NxprcPresetStyle.XBOX_ELITE)

    fun getReferenceTemplate(control: String, category: String, style: NxprcPresetStyle): String =
        getReferenceTemplateInternal(control, category, ConsoleMode.XBOX, style)

    fun getReferenceTemplate(control: String, category: String, consoleMode: ConsoleMode, style: NxprcPresetStyle): String =
        getReferenceTemplateInternal(control, category, consoleMode, style)

    private fun getReferenceTemplateInternal(
        control: String,
        category: String,
        consoleMode: ConsoleMode = ConsoleMode.XBOX,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val ctrl = ControlKey.fromIdentifier(control)
        val catType = ctrl?.categoryType
            ?: CategoryType.fromIdentifier(category)
            ?: CategoryManager.findCategoryForControl(control)?.type

        if (catType != null && catType != CategoryType.ABXY) {
            return when (catType) {
                CategoryType.DPAD -> when (ctrl) {
                    ControlKey.DOWN -> dpadDirectionPreset("DOWN", style)
                    ControlKey.LEFT -> dpadDirectionPreset("LEFT", style)
                    ControlKey.RIGHT -> dpadDirectionPreset("RIGHT", style)
                    ControlKey.DPAD -> dpadCrossPreset(style)
                    else -> dpadDirectionPreset("UP", style)
                }
                CategoryType.TRIGGERS ->
                    if (ctrl == ControlKey.LT) triggerPreset("LT", style) else triggerPreset("RT", style)
                CategoryType.BUMPERS ->
                    if (ctrl == ControlKey.LB) bumperPreset("LB", style) else bumperPreset("RB", style)
                CategoryType.STICKS -> when (ctrl) {
                    ControlKey.RS -> joystickPreset("RS", style)
                    ControlKey.LSB -> stickButtonPreset("LSB", style)
                    ControlKey.RSB -> stickButtonPreset("RSB", style)
                    ControlKey.LTP -> touchpadPreset("LTP", style)
                    ControlKey.RTP -> touchpadPreset("RTP", style)
                    else -> joystickPreset("LS", style)
                }
                CategoryType.SYSTEM -> when (ctrl) {
                    ControlKey.BACK -> systemViewPreset(style)
                    ControlKey.GUIDE -> systemHomePreset(style)
                    ControlKey.SHARE -> systemSharePreset(style)
                    else -> systemMenuPreset(style)
                }
                CategoryType.MACROS -> when (ctrl) {
                    ControlKey.M2 -> macroPreset("M2", style)
                    ControlKey.M3 -> macroPreset("M3", style)
                    ControlKey.M4 -> macroPreset("M4", style)
                    else -> macroPreset(ctrl?.key ?: "M1", style)
                }
                CategoryType.ABXY -> getReferenceTemplateInternal(control, "BUTTON", consoleMode, style)
            }
        }

        if (ctrl in listOf(ControlKey.A, ControlKey.B, ControlKey.X, ControlKey.Y)) {
            val isPs = consoleMode == ConsoleMode.PLAYSTATION
            return when (style) {
                NxprcPresetStyle.XBOX_ELITE -> when (ctrl) {
                    ControlKey.A -> if (isPs) PRESET_XBOX_ELITE_PS_CROSS else PRESET_XBOX_ELITE_A
                    ControlKey.B -> if (isPs) PRESET_XBOX_ELITE_PS_CIRCLE else PRESET_XBOX_ELITE_B
                    ControlKey.X -> if (isPs) PRESET_XBOX_ELITE_PS_SQUARE else PRESET_XBOX_ELITE_X
                    ControlKey.Y -> if (isPs) PRESET_XBOX_ELITE_PS_TRIANGLE else PRESET_XBOX_ELITE_Y
                    else -> PRESET_XBOX_ELITE_A
                }
                NxprcPresetStyle.ARCADE -> when (ctrl) {
                    ControlKey.A -> if (isPs) PRESET_ARCADE_PS_CROSS else PRESET_ARCADE_A
                    ControlKey.B -> if (isPs) PRESET_ARCADE_PS_CIRCLE else PRESET_ARCADE_B
                    ControlKey.X -> if (isPs) PRESET_ARCADE_PS_SQUARE else PRESET_ARCADE_X
                    ControlKey.Y -> if (isPs) PRESET_ARCADE_PS_TRIANGLE else PRESET_ARCADE_Y
                    else -> PRESET_ARCADE_A
                }
                NxprcPresetStyle.CYBERPUNK -> when (ctrl) {
                    ControlKey.A -> if (isPs) PRESET_CYBERPUNK_PS_CROSS else PRESET_CYBERPUNK_A
                    ControlKey.B -> if (isPs) PRESET_CYBERPUNK_PS_CIRCLE else PRESET_CYBERPUNK_B
                    ControlKey.X -> if (isPs) PRESET_CYBERPUNK_PS_SQUARE else PRESET_CYBERPUNK_X
                    ControlKey.Y -> if (isPs) PRESET_CYBERPUNK_PS_TRIANGLE else PRESET_CYBERPUNK_Y
                    else -> PRESET_CYBERPUNK_A
                }
                NxprcPresetStyle.SATIN -> when (ctrl) {
                    ControlKey.A -> if (isPs) PRESET_SATIN_PS_CROSS else PRESET_SATIN_A
                    ControlKey.B -> if (isPs) PRESET_SATIN_PS_CIRCLE else PRESET_SATIN_B
                    ControlKey.X -> if (isPs) PRESET_SATIN_PS_SQUARE else PRESET_SATIN_X
                    ControlKey.Y -> if (isPs) PRESET_SATIN_PS_TRIANGLE else PRESET_SATIN_Y
                    else -> PRESET_SATIN_A
                }
            }
        }

        return when (ctrl) {
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
