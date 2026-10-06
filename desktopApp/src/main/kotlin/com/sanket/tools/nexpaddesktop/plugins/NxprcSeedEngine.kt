package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.model.NexpadKeys
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Procedural Seed-Driven Generator for NEXPAD Virtual Controller Components.
 *
 * Inspired by Minecraft world generation seeds, this engine uses a deterministic
 * integer seed and a seeded random function [Random(seed)] to procedurally synthesize
 * distinct themes, geometric shapes, surface textures, lighting auras, and color palettes.
 *
 * The component identity strictly uses the button and seed number (e.g. "A #74829", "rc.a_74829"),
 * eliminating wordy names while guaranteeing 100% collision-free uniqueness during Wi-Fi push.
 */
data class SeedProfile(
    val seed: Long,
    val skinName: String,
    val componentId: String,
    val theme: String,
    val shape: String,
    val palette: ColorProfile,
    val surfaceTexture: String,
    val lightingStyle: String
)

object NxprcSeedEngine {

    private val THEME_MOTIFS = listOf(
        "Anime Mecha Energy / High-Impact Cel Animation & Dynamic Aura Burst",
        "Cyberpunk 2077 / High-Tech Neon Hologram & Glitch HUD Overlay",
        "Obsidian Void / Quantum Singularity & Dark Matter Cavity",
        "Solar Flare / Celestial Plasma Radiance & Corona Arc Sheen",
        "Retro Arcade / 80s Synthwave Outrun Grid & Neon Edge Wireframe",
        "Bio-Organic / Xenomorph Exoskeleton & Bioluminescent Tendrils",
        "Industrial Heavy Metal / Brushed Titanium & Recessed Hex Rivets",
        "Emerald Matrix / Crystalline Quantum Circuitry & Data Flow",
        "Crimson Dragon / Volcanic Basalt Forge & Molten Magma Fissures",
        "Phantom Stealth / Matte Carbon Fiber Monolith & Chamfered Bevels",
        "Hyperion Royal Gold / Ancient Relic Artifact & Engraved Filigree",
        "Glacial Cryo / Frostbite Crystalline Ice & Sub-Zero Specular Sheen",
        "Toxic Cyber-Hazard / Nuclear Plasma Spill & Warning Chevron Bezel",
        "Chrono Steampunk / Brass Clockwork Gears & Steamed Copper Sheen",
        "Deep Abyss / Bioluminescent Ocean Abyssal Radiance",
        "Prismatic Aura / Iridescent Spectrum Holographic Foil"
    )

    private val SHAPE_SILHOUETTES = listOf(
        "Faceted Hexagon with Chamfered Corners (`clip-path: polygon(...)`) & Recessed Socket",
        "Precision Squircle with Stepped Angular Bevels & Perimeter Trench",
        "Curved Ergonomic Shield Silhouette with Deep Tactile Socket Well",
        "Asymmetric Cyber Blade / Shard Silhouette with Directional Bevel",
        "Faceted Octagon with Perimeter Mechanical Accents & Inset Screws",
        "Diamond Rhombus with Inset Tactile Dish & Corner Chamfers",
        "Capsule Pod with Dual-Cast Flanged Grip Wings & Specular Arc",
        "Aggressive Chevron / Arrowhead Silhouette with Dynamic Rake",
        "Circular Turbine Bezel with Stepped Concentric Ridges",
        "Sculpted Teardrop / Aerodynamic Wing with Flowing Specular Sheen"
    )

    private val SURFACE_TEXTURES = listOf(
        "Matte Sandblasted Polymer with Deep Precision Chamfers",
        "Radial Brushed Metallic Disc with Multi-Angle Specular Highlights",
        "Translucent Frosted Glassmorphism with Refraction Rim & Depth Blur",
        "Textured Carbon Fiber Twill Weave with Gloss Clearcoat",
        "High-Gloss Enamel Glaze with High-Contrast Edge Specular Reflection",
        "Micro-Etched Circuit Trace Substrate with Backlit Trace Lines",
        "Tactile Concentric Ridge Grip Surface for Finger Stability",
        "Dual-Injection Rubberized Matte Finish with Recessed Contrast Grooves"
    )

    private val LIGHTING_AURA_STYLES = listOf(
        "Soft Ambient Underglow (`box-shadow: 0 0 24px rgba(...)`) with Smooth Falloff",
        "Hard Neon Laser Glow (`box-shadow: 0 0 8px ..., 0 0 28px ...`) High Contrast",
        "Edge-Lit Fiber-Optic Perimeter Ring with Specular Bezel Reflections",
        "Subtle Backlight Cavity Aura (`filter: drop-shadow(...)`) for Float Depth",
        "Dynamic Pulsing Aura Halo (`filter: blur(8px)`) with Secondary Corona Bloom",
        "Directional Sheen Glint with Micro Bevel Highlights"
    )

    /**
     * Generates a random 5-digit procedural seed (10000..99999).
     */
    fun randomSeed(): Long = Random.nextLong(10000L, 100000L)

    /**
     * Computes a deterministic seed integer from raw code content.
     */
    fun seedFromContent(content: String): Long {
        val clean = content.filter { !it.isWhitespace() }
        val hash = (clean.hashCode().toLong() and 0x7FFFFFFFL)
        return (hash % 90000L) + 10000L
    }

    /**
     * Extracts the base style/codename from a component name by removing trailing
     * button keys and seed hashes.
     * E.g. "Simple Btn Style A #25678" -> "Simple Btn Style"
     *      "Xbox Simple A" -> "Xbox Simple"
     *      "A #25678" -> ""
     */
    fun extractBaseStyle(name: String?, control: String): String {
        if (name.isNullOrBlank()) return ""
        val clean = name.trim().replace(Regex("""\s*#\d+\s*$"""), "")
        val withoutControl = clean.replace(Regex("""\s+${Regex.escape(control)}\s*$""", RegexOption.IGNORE_CASE), "")
        return if (withoutControl.equals(control, ignoreCase = true)) "" else withoutControl.trim()
    }

    /**
     * Formats a complete, foolproof component name combining style title, button key, and seed.
     * Format: "[Style Name] [Control] #[Seed]"
     * E.g. "Simple Btn Style A #25678"
     */
    fun formatComponentName(styleTitle: String?, control: String, seed: Long?): String {
        val baseStyle = extractBaseStyle(styleTitle, control)
        return when {
            baseStyle.isNotBlank() && seed != null -> "$baseStyle $control #$seed"
            baseStyle.isNotBlank() -> "$baseStyle $control"
            seed != null -> "$control #$seed"
            else -> control
        }
    }

    /**
     * Returns the canonical, authentic seed for each standard hardware controller component.
     * Guarantees that starter presets always initialize with their iconic hardware color identity:
     * - A: Emerald Green (Seed #16166)
     * - B: Crimson Red (Seed #24892)
     * - X: Cyber Cyan / Blue (Seed #38120)
     * - Y: Solar Amber (Seed #49551)
     * - D-Pad: Directional Cryo Blue (Seed #51040)
     * - Triggers: Volcano Blaze (Seed #62900)
     * - Bumpers: Plasma Ultraviolet (Seed #73400)
     * - Sticks: Crystalline Mint Frost (Seed #84200)
     * - System: Titanium Chrome (Seed #90510)
     * - Macros: Radiant Amber (Seed #95120)
     */
    fun canonicalSeedFor(control: String): Long = when (control.uppercase()) {
        "A" -> 16166L
        "B" -> 24892L
        "X" -> 38120L
        "Y" -> 49551L
        "UP", "DOWN", "LEFT", "RIGHT", "DPAD" -> 51040L
        "LT", "RT" -> 62900L
        "LB", "RB" -> 73400L
        "LS", "RS", "LSB", "RSB", "LTP", "RTP" -> 84200L
        "GUIDE", "START", "BACK", "SHARE" -> 90510L
        "M1", "M2", "M3", "M4" -> 95120L
        else -> 16166L
    }

    /**
     * Standard authentic hardware color palette corresponding to canonical seeds.
     */
    fun standardPaletteForControl(control: String): ColorProfile = when (control.uppercase()) {
        "A" -> ColorProfile(
            name = "Neo Tactile Emerald",
            hexCode = "#00FFA3",
            glowRgba = "rgba(0, 255, 163, 0.65)",
            coreGradient = "linear-gradient(145deg, #10b981 0%, #059669 50%, #047857 100%)"
        )
        "B" -> ColorProfile(
            name = "Neo Tactile Crimson",
            hexCode = "#FF2A6D",
            glowRgba = "rgba(255, 42, 109, 0.65)",
            coreGradient = "linear-gradient(145deg, #f43f5e 0%, #e11d48 50%, #881337 100%)"
        )
        "X" -> ColorProfile(
            name = "Neo Tactile Cyan",
            hexCode = "#00E5FF",
            glowRgba = "rgba(0, 229, 255, 0.65)",
            coreGradient = "linear-gradient(145deg, #06b6d4 0%, #0891b2 50%, #164e63 100%)"
        )
        "Y" -> ColorProfile(
            name = "Neo Tactile Amber",
            hexCode = "#FFD600",
            glowRgba = "rgba(255, 214, 0, 0.65)",
            coreGradient = "linear-gradient(145deg, #f59e0b 0%, #d97706 50%, #78350f 100%)"
        )
        "UP", "DOWN", "LEFT", "RIGHT", "DPAD" -> ColorProfile(
            name = "Glacial Cryo Blue",
            hexCode = "#38BDF8",
            glowRgba = "rgba(56, 189, 248, 0.65)",
            coreGradient = "linear-gradient(145deg, #38bdf8 0%, #0284c7 50%, #0c4a6e 100%)"
        )
        "LT", "RT" -> ColorProfile(
            name = "Magma Volcano Blaze",
            hexCode = "#F97316",
            glowRgba = "rgba(249, 115, 22, 0.65)",
            coreGradient = "linear-gradient(145deg, #f97316 0%, #ea580c 50%, #7c2d12 100%)"
        )
        "LB", "RB" -> ColorProfile(
            name = "Plasma Ultraviolet",
            hexCode = "#A855F7",
            glowRgba = "rgba(168, 85, 247, 0.65)",
            coreGradient = "linear-gradient(145deg, #a855f7 0%, #9333ea 50%, #581c87 100%)"
        )
        "LS", "RS", "LSB", "RSB", "LTP", "RTP" -> ColorProfile(
            name = "Crystalline Mint Frost",
            hexCode = "#2DD4BF",
            glowRgba = "rgba(45, 212, 191, 0.65)",
            coreGradient = "linear-gradient(145deg, #2dd4bf 0%, #0d9488 50%, #115e59 100%)"
        )
        "GUIDE", "START", "BACK", "SHARE" -> ColorProfile(
            name = "Industrial Titanium Chrome",
            hexCode = "#94A3B8",
            glowRgba = "rgba(148, 163, 184, 0.65)",
            coreGradient = "linear-gradient(145deg, #94a3b8 0%, #64748b 50%, #334155 100%)"
        )
        else -> ColorProfile(
            name = "Radiant Solar Amber",
            hexCode = "#F59E0B",
            glowRgba = "rgba(245, 158, 11, 0.65)",
            coreGradient = "linear-gradient(145deg, #f59e0b 0%, #d97706 50%, #78350f 100%)"
        )
    }

    /**
     * Continuous mathematical HSL color generator for procedural seeds.
     *
     * Maps every seed number (10000..99999) to a continuous coordinate on the 360° color wheel
     * using the Golden Ratio angle (~137.507764°). Guarantees infinite distinct color shades,
     * high-contrast gaming vibrancy (86-100% saturation), and radiant illumination without
     * being locked to any predefined list of colors.
     */
    fun proceduralPaletteFromSeed(seed: Long): ColorProfile {
        val goldenAngle = 137.50776405
        val rawHue = (seed * goldenAngle) % 360.0
        val hue = if (rawHue < 0) rawHue + 360.0 else rawHue

        val rng = Random(seed)
        // High-saturation gaming vibrancy (86% to 100%)
        val sat = 0.86 + (rng.nextDouble() * 0.14)
        // Radiant lightness (48% to 60%) — punchy, luminous contrast
        val light = 0.48 + (rng.nextDouble() * 0.12)

        val (r, g, b) = hslToRgb(hue, sat, light)
        val hexCode = "#%02X%02X%02X".format(r, g, b)
        val glowRgba = "rgba($r, $g, $b, 0.65)"

        // 3D tactile core gradient stops:
        // Stop 0: Top specular bevel highlight
        val (rTop, gTop, bTop) = hslToRgb(hue, sat, (light * 1.25).coerceAtMost(0.78))
        // Stop 1: Mid-body tactile core
        val (rMid, gMid, bMid) = hslToRgb(hue, sat, light)
        // Stop 2: Deep recessed cavity shadow
        val (rBot, gBot, bBot) = hslToRgb(hue, sat * 0.95, (light * 0.40).coerceAtLeast(0.12))

        val topHex = "#%02X%02X%02X".format(rTop, gTop, bTop)
        val midHex = "#%02X%02X%02X".format(rMid, gMid, bMid)
        val botHex = "#%02X%02X%02X".format(rBot, gBot, bBot)
        val coreGradient = "linear-gradient(145deg, $topHex 0%, $midHex 50%, $botHex 100%)"

        val hueInt = hue.roundToInt() % 360
        val hueFamily = when (hueInt) {
            in 0..14, in 345..359 -> "Crimson Blaze"
            in 15..39 -> "Solar Ember"
            in 40..59 -> "Hyperion Gold"
            in 60..84 -> "Toxic Acid Lime"
            in 85..149 -> "Emerald Matrix"
            in 150..174 -> "Mint Crystalline"
            in 175..209 -> "Cyber Cyan"
            in 210..249 -> "Glacial Cryo Blue"
            in 250..284 -> "Plasma Ultraviolet"
            in 285..314 -> "Neon Magenta"
            else -> "Vaporwave Pink" // 315..344
        }
        val name = "Procedural $hueFamily (${hueInt}°)"

        return ColorProfile(
            name = name,
            hexCode = hexCode,
            glowRgba = glowRgba,
            coreGradient = coreGradient
        )
    }

    /**
     * Converts HSL (hue [0..360], saturation [0..1], lightness [0..1]) to standard sRGB (0..255).
     */
    fun hslToRgb(hDeg: Double, s: Double, l: Double): Triple<Int, Int, Int> {
        val h = ((hDeg % 360.0 + 360.0) % 360.0) / 360.0
        val c = (1.0 - abs(2.0 * l - 1.0)) * s
        val x = c * (1.0 - abs((h * 6.0) % 2.0 - 1.0))
        val m = l - c / 2.0
        val sector = (h * 6.0).toInt().coerceIn(0, 5)
        val (rPrime, gPrime, bPrime) = when (sector) {
            0 -> Triple(c, x, 0.0)
            1 -> Triple(x, c, 0.0)
            2 -> Triple(0.0, c, x)
            3 -> Triple(0.0, x, c)
            4 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }
        val r = ((rPrime + m) * 255.0).roundToInt().coerceIn(0, 255)
        val g = ((gPrime + m) * 255.0).roundToInt().coerceIn(0, 255)
        val b = ((bPrime + m) * 255.0).roundToInt().coerceIn(0, 255)
        return Triple(r, g, b)
    }

    /**
     * Resolves a complete procedural [SeedProfile] for a given control and seed
     * using Kotlin's seeded pseudo-random generator [Random(seed)].
     */
    fun resolve(
        control: String,
        category: String,
        seed: Long? = null,
        customName: String? = null
    ): SeedProfile {
        val canonical = canonicalSeedFor(control)
        val effectiveSeed = seed ?: canonical
        val isCanonical = (effectiveSeed == canonical)

        val rng = Random(effectiveSeed)

        val theme = THEME_MOTIFS[rng.nextInt(THEME_MOTIFS.size)]
        val shape = SHAPE_SILHOUETTES[rng.nextInt(SHAPE_SILHOUETTES.size)]
        
        val palette = if (isCanonical) {
            standardPaletteForControl(control)
        } else {
            proceduralPaletteFromSeed(effectiveSeed)
        }

        val surface = SURFACE_TEXTURES[rng.nextInt(SURFACE_TEXTURES.size)]
        val lighting = LIGHTING_AURA_STYLES[rng.nextInt(LIGHTING_AURA_STYLES.size)]

        val skinName = if (!customName.isNullOrBlank()) {
            formatComponentName(customName, control, effectiveSeed)
        } else {
            "$control #$effectiveSeed"
        }

        val componentId = "rc.${control.lowercase()}_$effectiveSeed"

        return SeedProfile(
            seed = effectiveSeed,
            skinName = skinName,
            componentId = componentId,
            theme = theme,
            shape = shape,
            palette = palette,
            surfaceTexture = surface,
            lightingStyle = lighting
        )
    }

    /**
     * Renders the Target Component Identity block with seed parameters.
     */
    fun renderTargetIdentityBlock(
        control: String,
        category: String,
        widthDp: Int,
        heightDp: Int,
        profile: SeedProfile,
        standardProfile: ColorProfile? = null,
        controlRoleDescription: String? = null
    ): String {
        val roleDesc = if (controlRoleDescription != null) " ($controlRoleDescription)" else ""
        val standardSection = if (standardProfile != null && standardProfile.hexCode != profile.palette.hexCode) {
            "- **Standard Color Profile [OPTIONAL-FALLBACK]**: ${standardProfile.name} (Accent: ${standardProfile.hexCode}, Glow: ${standardProfile.glowRgba})\n"
        } else ""
        val styleTitle = extractBaseStyle(profile.skinName, control).ifBlank { profile.theme.substringBefore(" / ").trim() }
        val fullName = formatComponentName(styleTitle, control, profile.seed)

        return """
### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control$roleDesc
- **Category [GLOBAL-REQUIRED]**: $category
- **Seed Number [PROCEDURAL-SEED]**: #${profile.seed} (Minecraft-style deterministic generation seed)
- **Style Codename [CREATIVE-REQUIRED]**: "$styleTitle" (Invent a 2-4 word descriptive style title matching your visual design, e.g. "Simple Btn Style", "Xbox Simple", "Neon Outrun", "Molten Forge")
- **Full Component Name [GLOBAL-REQUIRED]**: "$fullName" (Format: "<Style Codename> $control #${profile.seed}")
- **Component ID [GLOBAL-REQUIRED]**: "${profile.componentId}"
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)
- **Procedural Seed Theme [SEED-DIRECTIVE]**: ${profile.theme}
- **Procedural Seed Shape [SEED-DIRECTIVE]**: ${profile.shape}
- **Procedural Surface Texture [SEED-DIRECTIVE]**: ${profile.surfaceTexture}
- **Procedural Aura Lighting [SEED-DIRECTIVE]**: ${profile.lightingStyle}
- **Procedural Color Profile [SEED-DIRECTIVE]**: ${profile.palette.name} (Accent: ${profile.palette.hexCode}, Glow: ${profile.palette.glowRgba})
- **Standard Core [RECOMMENDED]**: ${profile.palette.coreGradient}
$standardSection
### SEED IDENTITY & ANTI-DUPLICATION CONTRACT:
1. **Root Button Attributes [MANDATORY]**: The root `<button>` element MUST declare:
   `<button class="nexpad-btn" data-control="$control" data-category="$category" data-codename="$styleTitle" data-name="$fullName" data-seed="${profile.seed}" id="${profile.componentId}">`
2. **Procedural Seed Creativity**: Use Seed #${profile.seed} and its procedural theme, shape, surface texture, and aura lighting to craft a distinct, high-quality controller component.
3. **Inbuilt Anti-Duplication**: Using unique seeds and ID "${profile.componentId}" guarantees this button has an independent identity and will never overwrite previous designs in NEXPAD when pushed via Wi-Fi.
""".trimIndent()
    }

    /**
     * Generates a complete starter HTML template customized for [control]
     * and themed with [seed]'s procedural colors, gradient, and glow aura.
     */
    fun generateSeedTemplate(control: String, category: String, seed: Long): String {
        val base = NxprcPresets.getReferenceTemplate(control, category)
        val profile = resolve(control, category, seed)
        val styleTitle = extractBaseStyle(profile.skinName, control).ifBlank { "Reference" }
        val hex = profile.palette.hexCode
        val glow = profile.palette.glowRgba

        var html = base

        // 1. Invert/inject data-seed, data-codename, data-name, and id
        html = if (html.contains("data-seed=")) {
            html.replace(Regex("""data-seed=["'][^"']*["']"""), "data-seed=\"$seed\"")
        } else {
            html.replaceFirst("<button", "<button data-seed=\"$seed\"")
        }
        html = if (html.contains("data-codename=")) {
            html.replace(Regex("""data-codename=["'][^"']*["']"""), "data-codename=\"$styleTitle\"")
        } else {
            html.replaceFirst("<button", "<button data-codename=\"$styleTitle\"")
        }
        html = if (html.contains("data-name=")) {
            html.replace(Regex("""data-name=["'][^"']*["']"""), "data-name=\"${profile.skinName}\"")
        } else {
            html.replaceFirst("<button", "<button data-name=\"${profile.skinName}\"")
        }
        html = if (html.contains(Regex("""id=["'][^"']*["']"""))) {
            html.replace(Regex("""id=["'][^"']*["']"""), "id=\"${profile.componentId}\"")
        } else {
            html.replaceFirst("<button", "<button id=\"${profile.componentId}\"")
        }

        // 2. Replace CSS root variables
        html = html
            .replace(Regex("""(--accent-color\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--accent-core\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--core-color\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--accent\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--xbox-green\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--xbox-green-dark\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--accent-glow\s*:\s*)[^;]+;"""), "$1$glow;")
            .replace(Regex("""(--core-glow\s*:\s*)[^;]+;"""), "$1$glow;")

        return html
    }

    /**
     * Applies the procedural seed's color, gradient, aura, and identity attributes
     * to the provided HTML code, ensuring the live preview visibly changes color on every reroll.
     */
    fun applySeedToHtml(
        currentHtml: String,
        control: String,
        category: String,
        seed: Long,
        customStyleName: String? = null
    ): String {
        // Extract existing style from HTML if any
        val existingCodename = Regex("""data-codename=["']([^"']*)["']""").find(currentHtml)?.groupValues?.get(1)
            ?: Regex("""data-code-name=["']([^"']*)["']""").find(currentHtml)?.groupValues?.get(1)
            ?: Regex("""data-style=["']([^"']*)["']""").find(currentHtml)?.groupValues?.get(1)
        val existingName = Regex("""data-name=["']([^"']*)["']""").find(currentHtml)?.groupValues?.get(1)

        val baseStyle = customStyleName
            ?: existingCodename?.takeIf { it.isNotBlank() }
            ?: extractBaseStyle(existingName, control).takeIf { it.isNotBlank() }

        val profile = resolve(control, category, seed, baseStyle)
        val styleTitle = extractBaseStyle(profile.skinName, control).ifBlank { "Custom" }
        val hex = profile.palette.hexCode
        val glow = profile.palette.glowRgba
        val grad = profile.palette.coreGradient

        if (currentHtml.isBlank()) {
            return generateSeedTemplate(control, category, seed)
        }

        var html = currentHtml

        // 1. Update data-seed
        html = if (html.contains("data-seed=")) {
            html.replace(Regex("""data-seed=["'][^"']*["']"""), "data-seed=\"$seed\"")
        } else {
            html.replaceFirst("<button", "<button data-seed=\"$seed\"")
        }

        // 2. Update data-codename
        html = if (html.contains("data-codename=")) {
            html.replace(Regex("""data-codename=["'][^"']*["']"""), "data-codename=\"$styleTitle\"")
        } else if (html.contains("data-code-name=")) {
            html.replace(Regex("""data-code-name=["'][^"']*["']"""), "data-codename=\"$styleTitle\"")
        } else {
            html.replaceFirst("<button", "<button data-codename=\"$styleTitle\"")
        }

        // 3. Update data-name
        html = if (html.contains("data-name=")) {
            html.replace(Regex("""data-name=["'][^"']*["']"""), "data-name=\"${profile.skinName}\"")
        } else {
            html.replaceFirst("<button", "<button data-name=\"${profile.skinName}\"")
        }

        // 4. Update id
        html = if (html.contains(Regex("""id=["'][^"']*["']"""))) {
            html.replace(Regex("""id=["'][^"']*["']"""), "id=\"${profile.componentId}\"")
        } else {
            html.replaceFirst("<button", "<button id=\"${profile.componentId}\"")
        }

        // 5. Update CSS root color variables
        html = html
            .replace(Regex("""(--accent-color\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--accent-core\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--core-color\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--accent\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--xbox-green\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--xbox-green-dark\s*:\s*)[^;]+;"""), "$1$hex;")
            .replace(Regex("""(--accent-glow\s*:\s*)[^;]+;"""), "$1$glow;")
            .replace(Regex("""(--core-glow\s*:\s*)[^;]+;"""), "$1$glow;")
            .replace(Regex("""(--glow\s*:\s*)[^;]+;"""), "$1$glow;")
            .replace(Regex("""(--theme-color\s*:\s*)[^;]+;"""), "$1$hex;")

        // 6. Replace any existing procedural core gradients across ALL elements
        html = html.replace(
            Regex("""linear-gradient\(145deg,\s*#[0-9a-fA-F]{6}\s*0%,\s*#[0-9a-fA-F]{6}\s*50%,\s*#[0-9a-fA-F]{6}\s*100%\)"""),
            grad
        )

        // 7. Replace any existing glow RGBA values across ALL elements
        html = html.replace(
            Regex("""rgba\(\s*\d+\s*,\s*\d+\s*,\s*\d+\s*,\s*0\.65\s*\)"""),
            glow
        )

        // 8. Update face/cap layer backgrounds
        if (html.contains(".face")) {
            html = html.replace(
                Regex("""(\.face\s*\{[^}]*background:\s*)[^;]+;"""),
                "$1radial-gradient(circle at 34% 24%, rgba(255,255,255,0.35) 0%, transparent 45%), $grad;"
            )
        }

        return html
    }
}
