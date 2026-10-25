package com.sanket.tools.nexpaddesktop.plugins.presets

import com.sanket.tools.nexpad.category.ControlKey

internal fun dpadGlyph(control: String): String = when (control.uppercase()) {
    ControlKey.DOWN.key -> "▼"
    ControlKey.LEFT.key -> "◀"
    ControlKey.RIGHT.key -> "▶"
    ControlKey.DPAD.key -> "▲"
    else -> "▲"
}

internal fun htmlDocument(css: String, body: String): String = """
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

internal fun retroMotion(
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

internal fun centeredPress(selector: String): String = """
$selector:active {
  transform: scale(var(--press-scale));
  transform-origin: 50% 50%;
}
""".trimIndent()

internal data class ElementStyleConfig(
    val chassisBg: String,
    val chassisShadow: String,
    val facePlateBg: String,
    val accentColor: String,
    val accentShadow: String,
    val accentBorder: String
)

internal fun syntaxDocument(
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
