package com.sanket.tools.nexpaddesktop.plugins.presets

import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle

internal object SystemPresetGenerator {

    internal fun systemMenuPreset(
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val motion = retroMotion("0.75", "500", "0.93")
        val selector = ".system-btn"
        val active = centeredPress(selector)
        val accent = "#10B981"
        val glow = "rgba(16, 185, 129, 0.40)"

        val config = when (style) {
            NxprcPresetStyle.XBOX_ELITE -> ElementStyleConfig(
                chassisBg = "radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.85), 0 0 18px $glow, inset 0 1.5px 2px rgba(255, 255, 255, 0.15), inset 0 -4px 8px rgba(0, 0, 0, 0.95)",
                facePlateBg = "linear-gradient(160deg, #1c232d 0%, #11151c 55%, #080a0e 100%)",
                accentColor = accent,
                accentShadow = "0 0 8px $glow",
                accentBorder = "none"
            )
            NxprcPresetStyle.ARCADE -> ElementStyleConfig(
                chassisBg = "linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.85), inset 0 2px 2px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.95)",
                facePlateBg = "radial-gradient(circle at 38% 28%, #4ade80 0%, #10b981 50%, #064e3b 100%)",
                accentColor = "#ffffff",
                accentShadow = "0 1px 3px rgba(0, 0, 0, 0.70)",
                accentBorder = "none"
            )
            NxprcPresetStyle.CYBERPUNK -> ElementStyleConfig(
                chassisBg = "linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.90), 0 0 20px $glow",
                facePlateBg = "linear-gradient(145deg, #131720 0%, #090c12 100%)",
                accentColor = accent,
                accentShadow = "0 0 10px $accent",
                accentBorder = "1.5px solid $glow"
            )
            NxprcPresetStyle.SATIN -> ElementStyleConfig(
                chassisBg = "linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.80), inset 0 1.5px 2px rgba(255, 255, 255, 0.20), inset 0 -4px 8px rgba(0, 0, 0, 0.90)",
                facePlateBg = "radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%)",
                accentColor = "#e2e8f0",
                accentShadow = "0 1px 2px rgba(0, 0, 0, 0.50)",
                accentBorder = "none"
            )
        }

        val css = """
$motion
$selector {
  position: relative;
  width: 64px;
  height: 42px;
  padding: 0;
  border: ${config.accentBorder};
  outline: none;
  border-radius: 12px;
  background: ${config.chassisBg};
  box-shadow: ${config.chassisShadow};
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
  background: ${config.facePlateBg};
}
.burger-bar {
  position: absolute;
  left: 17px;
  width: 20px;
  height: 3px;
  border-radius: 2px;
  background: ${config.accentColor};
  box-shadow: ${config.accentShadow};
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

    internal fun systemPreset(
        control: String,
        label: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val motion = retroMotion("0.75", "500", "0.93")
        val selector = ".system-btn"
        val active = centeredPress(selector)
        val accent = "#10B981"
        val glow = "rgba(16, 185, 129, 0.40)"

        val config = when (style) {
            NxprcPresetStyle.XBOX_ELITE -> ElementStyleConfig(
                chassisBg = "radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.85), 0 0 18px $glow, inset 0 1.5px 2px rgba(255, 255, 255, 0.15), inset 0 -4px 8px rgba(0, 0, 0, 0.95)",
                facePlateBg = "linear-gradient(160deg, #1c232d 0%, #11151c 55%, #080a0e 100%)",
                accentColor = accent,
                accentShadow = "0 2px 8px rgba(0, 0, 0, 0.95), 0 0 12px $glow",
                accentBorder = "none"
            )
            NxprcPresetStyle.ARCADE -> ElementStyleConfig(
                chassisBg = "linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.85), inset 0 2px 2px rgba(255, 255, 255, 0.25), inset 0 -4px 8px rgba(0, 0, 0, 0.95)",
                facePlateBg = "radial-gradient(circle at 38% 28%, #4ade80 0%, #10b981 50%, #064e3b 100%)",
                accentColor = "#ffffff",
                accentShadow = "0 1px 3px rgba(0, 0, 0, 0.70)",
                accentBorder = "none"
            )
            NxprcPresetStyle.CYBERPUNK -> ElementStyleConfig(
                chassisBg = "linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.90), 0 0 20px $glow",
                facePlateBg = "linear-gradient(145deg, #131720 0%, #090c12 100%)",
                accentColor = accent,
                accentShadow = "0 0 10px $accent, 0 0 24px $glow",
                accentBorder = "1.5px solid $glow"
            )
            NxprcPresetStyle.SATIN -> ElementStyleConfig(
                chassisBg = "linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%)",
                chassisShadow = "0 8px 20px rgba(0, 0, 0, 0.80), inset 0 1.5px 2px rgba(255, 255, 255, 0.20), inset 0 -4px 8px rgba(0, 0, 0, 0.90)",
                facePlateBg = "radial-gradient(circle at 42% 32%, #383e4c 0%, #222630 55%, #13161c 100%)",
                accentColor = "#e2e8f0",
                accentShadow = "0 -1px 1px rgba(0, 0, 0, 0.85), 0 1px 1px rgba(255, 255, 255, 0.20)",
                accentBorder = "none"
            )
        }

        val css = """
$motion
$selector {
  position: relative;
  width: 64px;
  height: 42px;
  padding: 0;
  border: ${config.accentBorder};
  outline: none;
  border-radius: 12px;
  background: ${config.chassisBg};
  box-shadow: ${config.chassisShadow};
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
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
  background: ${config.facePlateBg};
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
  color: ${config.accentColor};
  text-shadow: ${config.accentShadow};
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

    internal fun systemViewPreset(style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE): String = systemPreset("BACK", "⧉", style)
    internal fun systemHomePreset(style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE): String = systemPreset("GUIDE", "⌂", style)
    internal fun systemSharePreset(style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE): String = systemPreset("SHARE", "⇪", style)

    internal fun macroPreset(
        control: String,
        style: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE
    ): String {
        val safeControl = control.trim().ifBlank { "M1" }.uppercase()
        val motion = retroMotion("0.70", "460", "0.94")
        val selector = ".macro-paddle"
        val active = centeredPress(selector)
        val accent = "#10B981"
        val glow = "rgba(16, 185, 129, 0.45)"

        val config = when (style) {
            NxprcPresetStyle.XBOX_ELITE -> ElementStyleConfig(
                chassisBg = "radial-gradient(circle at 45% 40%, #20242b 0%, #101217 70%, #06080a 100%)",
                chassisShadow = "0 10px 24px rgba(0, 0, 0, 0.85), 0 0 20px $glow, inset 0 2px 3px rgba(255, 255, 255, 0.15), inset 0 -5px 10px rgba(0, 0, 0, 0.95)",
                facePlateBg = "linear-gradient(160deg, #1c232d 0%, #11151c 55%, #080a0e 100%)",
                accentColor = accent,
                accentShadow = "0 2px 8px rgba(0, 0, 0, 0.95), 0 0 14px $glow",
                accentBorder = "none"
            )
            NxprcPresetStyle.ARCADE -> ElementStyleConfig(
                chassisBg = "linear-gradient(180deg, #363636 0%, #1a1a1a 50%, #0a0a0a 100%)",
                chassisShadow = "0 10px 24px rgba(0, 0, 0, 0.85), inset 0 2px 2px rgba(255, 255, 255, 0.25), inset 0 -5px 10px rgba(0, 0, 0, 0.95)",
                facePlateBg = "radial-gradient(circle at 38% 28%, #4ade80 0%, #10b981 50%, #064e3b 100%)",
                accentColor = "#ffffff",
                accentShadow = "0 2px 4px rgba(0, 0, 0, 0.70)",
                accentBorder = "none"
            )
            NxprcPresetStyle.CYBERPUNK -> ElementStyleConfig(
                chassisBg = "linear-gradient(145deg, #181d26 0%, #0e1117 60%, #07090c 100%)",
                chassisShadow = "0 10px 24px rgba(0, 0, 0, 0.90), 0 0 22px $glow",
                facePlateBg = "linear-gradient(145deg, #131720 0%, #090c12 100%)",
                accentColor = accent,
                accentShadow = "0 0 10px $accent, 0 0 24px $glow",
                accentBorder = "1.5px solid $glow"
            )
            NxprcPresetStyle.SATIN -> ElementStyleConfig(
                chassisBg = "linear-gradient(160deg, #282d38 0%, #161921 60%, #0d0f14 100%)",
                chassisShadow = "0 10px 24px rgba(0, 0, 0, 0.80), inset 0 1.5px 2px rgba(255, 255, 255, 0.20), inset 0 -5px 10px rgba(0, 0, 0, 0.90)",
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
  width: 72px;
  height: 72px;
  padding: 0;
  border: ${config.accentBorder};
  outline: none;
  border-radius: 16px;
  background: ${config.chassisBg};
  box-shadow: ${config.chassisShadow};
  transform-origin: 50% 50%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
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
  background: ${config.facePlateBg};
}
.macro-bolt {
  position: absolute;
  left: 14px;
  top: 18px;
  z-index: 5;
  font-size: 18px;
  color: ${config.accentColor};
  text-shadow: ${config.accentShadow};
}
.macro-label {
  position: relative;
  z-index: 5;
  margin-left: 16px;
  font-size: 20px;
  font-weight: 900;
  letter-spacing: 1px;
  color: ${config.accentColor};
  text-shadow: ${config.accentShadow};
}
$active
""".trimIndent()

        val body = """
  <button class="macro-paddle" data-control="$safeControl" data-category="MACRO" data-codename="${style.displayName}" data-name="${style.displayName} Macro $safeControl" id="rc.macro_${safeControl.lowercase()}">
    <div class="face-plate" data-layer-role="base"></div>
    <span class="macro-bolt">⚡</span>
    <span class="macro-label">$safeControl</span>
  </button>
        """.trimIndent()

        return htmlDocument(css, body)
    }
}
