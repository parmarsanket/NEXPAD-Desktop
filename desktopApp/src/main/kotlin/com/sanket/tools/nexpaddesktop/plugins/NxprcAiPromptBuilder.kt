package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey
import com.sanket.tools.nexpad.model.NexpadKeys

/**
 * Creative exploration freedom level for AI component generation.
 */
enum class Creativity {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Visual layering, vector detail, and mechanical density target.
 */
enum class Complexity {
    AUTO,
    SIMPLE,
    DETAILED,
    EXTREME
}

/**
 * Thematic interpretation fidelity mode for character/brand/aesthetic themes.
 */
enum class Fidelity {
    FAITHFUL,
    INSPIRED,
    ABSTRACT
}

/**
 * Detail concentration and visual balance target for component geometry.
 */
enum class VisualDensity {
    AUTO,
    CLEAN,
    BALANCED,
    DENSE
}

/**
 * Parameterized design options for NEXPAD generative AI component creation.
 * Empowers calling editors and automated generators with fine-grained aesthetic control.
 */
data class AiDesignOptions(
    val creativity: Creativity = Creativity.HIGH,
    val complexity: Complexity = Complexity.AUTO,
    val fidelity: Fidelity = Fidelity.INSPIRED,
    val visualDensity: VisualDensity = VisualDensity.AUTO,

    val style: String? = null,
    val color: String? = null,
    val shape: String? = null,
    val material: String? = null,
    val lighting: String? = null,
    val texture: String? = null,
    val emblem: String? = null,
    val label: String? = null,
    val tactilePhysics: String? = null,

    val specialInstructions: String? = null,
    val userRequest: String = "",
    val includeSyntaxSkeleton: Boolean = false
)

/**
 * High-performance, modular AI Prompt Builder for NEXPAD Virtual Controller Components.
 * Generates compact, token-efficient generation protocols engineered for frontier and compact LLMs alike.
 */
object NxprcAiPromptBuilder {

    /**
     * Builds a comprehensive, parameter-driven AI prompt tailored specifically to the target button type.
     */
    fun buildPrompt(
        control: String,
        category: String,
        widthDp: Int,
        heightDp: Int,
        options: AiDesignOptions = AiDesignOptions()
    ): String {
        val ctrl = ControlKey.fromIdentifier(control)
        if (ctrl == ControlKey.LTP || ctrl == ControlKey.RTP ||
            (ctrl?.componentType == ComponentType.TOUCHPAD && ctrl.categoryType == CategoryType.STICKS)) {
            return generateTouchpadPrompt(control, category, widthDp, heightDp, options)
        }

        if (ctrl == ControlKey.LSB || ctrl == ControlKey.RSB ||
            (ctrl?.componentType == ComponentType.BUTTON && ctrl.categoryType == CategoryType.STICKS)) {
            return generateStickButtonPrompt(control, category, widthDp, heightDp, options)
        }

        val catType = CategoryType.fromIdentifier(category)
            ?: CategoryManager.findCategoryForControl(control)?.type
        return when (catType) {
            CategoryType.TRIGGERS -> generateTriggerPrompt(control, category, widthDp, heightDp, options)
            CategoryType.BUMPERS -> generateBumperPrompt(control, category, widthDp, heightDp, options)
            CategoryType.DPAD -> generateDpadPrompt(control, category, widthDp, heightDp, options)
            CategoryType.STICKS -> generateStickPrompt(control, category, widthDp, heightDp, options)
            CategoryType.SYSTEM,
            CategoryType.MACROS -> generateSystemPrompt(control, category, widthDp, heightDp, options)
            CategoryType.ABXY,
            null -> generateAbxyPrompt(control, category, widthDp, heightDp, options)
        }
    }

    /**
     * Generates a targeted, surgical repair prompt for the Generate -> Compile -> Repair loop.
     * Instructs the AI model to fix specific compiler diagnostics while strictly preserving the visual artwork.
     */
    fun buildRepairPrompt(
        previousHtml: String,
        warnings: List<String> = emptyList(),
        errors: List<String> = emptyList(),
        control: String? = null,
        category: String? = null,
        options: AiDesignOptions = AiDesignOptions()
    ): String {
        val paramsSection = StringBuilder()
        if (options.creativity != Creativity.HIGH || options.complexity != Complexity.AUTO ||
            options.fidelity != Fidelity.INSPIRED || options.visualDensity != VisualDensity.AUTO ||
            !options.style.isNullOrBlank() || !options.color.isNullOrBlank() ||
            !options.tactilePhysics.isNullOrBlank() ||
            !options.specialInstructions.isNullOrBlank() || options.userRequest.isNotBlank()
        ) {
            paramsSection.append("### TARGET DESIGN CONSTRAINTS (PRESERVE THESE IN REPAIR):\n")
            paramsSection.append(renderDesignParameters(options).removePrefix("### USER DESIGN PARAMETERS & PREFERENCES:\n"))
            if (options.userRequest.isNotBlank()) {
                paramsSection.append("- **Original User Request**: ${options.userRequest}\n")
            }
            paramsSection.append("\n")
        }

        return """
# NEXPAD COMPONENT COMPILER REPAIR PROTOCOL
You are an expert gamepad UI/UX and CSS/SVG compiler engineer repairing an AI-generated NEXPAD controller component.

### COMPILER DIAGNOSTICS:
${if (errors.isNotEmpty()) "ERRORS (CRITICAL):\n" + errors.joinToString("\n") { "- $it" } else "No fatal compilation errors."}
${if (warnings.isNotEmpty()) "WARNINGS (ATTENTION NEEDED):\n" + warnings.joinToString("\n") { "- $it" } else "No compiler warnings."}

### TARGET COMPONENT IDENTITY:
- **Control Key**: ${control?.ifBlank { "Unspecified" } ?: "Unspecified"}
- **Category**: ${category?.ifBlank { "BUTTON" } ?: "BUTTON"}

${paramsSection}### PREVIOUS CODE:
```html
$previousHtml
```

### SURGICAL REPAIR CONTRACT:
1. **Preserve Visual Design**: Do NOT redesign the component or alter its requested theme, artistic concept, color palette, or vector artwork.
2. **Fix Compiler Diagnostics**: Resolve ONLY the specific errors and warnings reported above (e.g., ensure single root `<button>`, valid explicit px bounds, valid SVG viewBox, remove unsupported CSS like `@media`/`@keyframes`/`mix-blend-mode`).
3. **Dual-Engine Alignment**: Ensure complex graphics use embedded `<svg class="button-emblem" viewBox="...">` vector paths, and glowing halos use underlying CSS spans rather than SVG filter graphs (`feGaussianBlur`).
4. **Authoritative Output**: Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include markdown conversation or explanations.
""".trimIndent()
    }

    private fun genAiHeader(): String = """
# NEXPAD COMPONENT GENERATION PROTOCOL
**Protocol Standard: NXPRC 10/10 Vector Engine Architecture**

## CORE RULES (QUICK SUMMARY FOR ALL MODELS):
1. Build ONE virtual controller component inside a single `<button>` element.
2. Follow the user's visual request first — user customization always wins within compiler boundaries.
3. Keep the component self-contained: one `<style>` block, system fonts, zero external assets.
4. Use only supported HTML/CSS/SVG primitives (no unsupported web page APIs).
5. Preserve the component's interaction meaning (category semantics), not a mandatory shape.
6. Make the design visually coherent with physically believable depth and lighting.
7. Use creativity when details are unspecified — never default to a generic circle unless requested or natural.
8. Design with restraint: avoid visual clutter; prefer the minimum number of layers required to achieve the requested aesthetic clearly; do not remove meaningful visual detail merely to reduce layer count.
9. Ensure the label/icon remains clearly readable with strong contrast in an unrotated DOM text node.
10. Return ONLY the complete, self-contained HTML/CSS inside one code block.
11. NEXPAD supports dual button labeling styles (Xbox: A, B, X, Y, LB, RB, LT, RT, LSB, RSB vs PlayStation: ✕, ○, □, △, L1, R1, L2, R2, L3, R3) and dynamically translates standard controller labels at runtime while preserving custom action text (e.g. ATTACK, DASH, JUMP).
""".trimIndent()

    private fun engineBoundaries(rootClass: String): String = """
### SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION
When instructions conflict, resolve them in this strict order of authority:
1. **Non-Negotiable Compiler Safety** [GLOBAL-REQUIRED] (Single button root, px bounds, DOM text, self-contained document, no external assets or scripts).
2. **User's Explicit Customization** [USER-OVERRIDE] (Highest design authority — user's artistic style, shape, palette, and theme always supersede defaults).
3. **Component Semantics** [COMPONENT-REQUIRED] (Preserve interaction meaning: tappable, directional, analog, etc.).
4. **Accessibility & Readability** [GLOBAL-REQUIRED] (High-contrast label legibility, touch target visibility).
5. **Design Quality Principles** [RECOMMENDED] (Physical coherence, balanced hierarchy, believable depth).
6. **Category Defaults** [RECOMMENDED] (Color palette suggestions, default glyphs used when user specifies none).
7. **Optional Inspiration** [OPTIONAL] (Theme suggestions, optional decorative flair).
8. **Starter-Template Examples [NON-BINDING SYNTAX REFERENCE]** (Syntax structure only — never copy its geometry, proportions, colors, materials, layer count, visual hierarchy, or silhouette unless those properties are independently required by the component contract or explicitly requested by the user).

> **The Golden Rule**: The user's visual and artistic instructions always win over defaults and recommendations, provided they remain compatible with the required compiler contract and component semantics.
> **Conflict Rule**: User instructions always take precedence over optional recommendations or category defaults.

### SECTION 2 — RULE CLASSIFICATION HIERARCHY
- **[GLOBAL-REQUIRED]**: Platform/engine constraints. Violation causes compiler rejection.
- **[COMPONENT-REQUIRED]**: Required for this component's interaction model (e.g. active feedback, control key).
- **[USER-OVERRIDE]**: User's explicit aesthetic requests. Highest design authority within compiler boundaries.
- **[RECOMMENDED]**: Proven design patterns for quality, depth, and touch affordance.
- **[OPTIONAL]**: Primitives and effects (SVG paths, conic gradients, filter nodes) to use when they enhance the requested aesthetic.
- **[NON-BINDING SYNTAX REFERENCE]**: Architectural syntax example only.

### SECTION 3 — USER CREATIVE AUTHORITY & FREE-HAND MODE
**Strict on Code, Free on Design:**
```
Compiler Contract:    STRICT  (Single button, valid CSS/SVG primitives, explicit bounds)
User Visual Concept:  FREE    (Theme, character, style, geometry completely replace defaults)
AI Artistic Choice:   FREE    (Infer unspecified lighting, materials, palette, vector details)
Unsupported Details:  ADAPT   (Map impossible requests to nearest compilable representation)
Output Format:        STRICT  (Single ```html ... ``` block, zero markdown conversational text)
```
- **Free-Hand Rule**: Missing visual parameters are invitations for creative decisions, not missing information that must be filled using default style. Infer shape, palette, material, lighting, composition, texture and emblem treatment from user's concept. Do not ask for missing design parameters. Do not simplify meaningful artwork unless necessary.

### SECTION 4 — INTERPRETATION & FIDELITY MODES
- **FAITHFUL**: Preserve recognizable motifs and visual relationships.
- **INSPIRED**: Create an original design strongly influenced by them.
- **ABSTRACT**: Extract only the essential visual language.

### SECTION 5 — HARD COMPILER CONTRACT & STRICT BOUNDARIES
1. **Single compiled component [GLOBAL-REQUIRED]**: `<body>` must contain exactly one root `<button class="$rootClass" data-control="..." data-category="..." data-name="...">`. Keep every visual child inside it.
2. **Portable self-contained document [GLOBAL-REQUIRED]**: Include one `<style>` block, one root button, zero external assets, no `@import`, no `<link>`, no external fonts or scripts. System fonts only.
3. **Explicit geometry & positioning [GLOBAL-REQUIRED]**: Root component dimensions MUST use explicit `px` bounds (`position: relative`). Set `position: absolute`, `left`, `top`, `width`, and `height` on decorative children when deterministic layered artwork is desired. Flexbox (`display: flex`, `gap`, `justify-content`, `align-items`) MAY be used where flow/alignment is more appropriate. Dynamic `calc()` and `aspect-ratio` are supported on children.
4. **Arbitrary polygon shapes & free geometry [GLOBAL-REQUIRED]**: Use `border-radius` or `clip-path: polygon(...)` for circles, capsules, stars, diamonds, hexagons, octagons, and organic silhouettes. `data-category` is metadata, not a shape instruction. Preserve the user's requested shape.
5. **Physical 3D Layer Hierarchy & Recommended Layering [RECOMMENDED / ANTI-OCCLUSION REQUIRED]**:
   - Recommended Layering Pattern:
     - 0..2: Chassis housing, outer ring, ambient glow, recessed socket well
     - 3..4: Main keycap face plate / center body surface (opaque base)
     - 5..7: Tactile grips, ridges, secondary accents & optical halo glow
     - 8..9: Embedded `<svg class="button-emblem">` vector emblem, insignia, SVG icons
     - 10+: Center typography letterform & specular gloss reflections (::before/::after)
   ⚠️ The Hard Requirement: Opaque surface plates MUST sit underneath vector artwork and typography. In HTML DOM, declare face plate FIRST, embedded `<svg>` SECOND, and label `<span>` THIRD. Opaque surfaces must not unintentionally occlude required artwork or text.
6. **Text must be real DOM text without rotation [GLOBAL-REQUIRED]**: Labels and markings in unrotated `<span>` (`NO TEXT ROTATION`). When iconography is needed, use SVG/vector graphics; do not add text only because the component is a button.
7. **Stable CSS only [GLOBAL-REQUIRED]**: Do not use `@media`, `@supports`, `:hover`, `:focus`, or `@keyframes`. Press feedback uses `.$rootClass:active` with spring micro-physics.
8. **Optical filters [GLOBAL-REQUIRED]**: GPU `filter: blur()`, `brightness()`, `contrast()`, `saturate()`, `hue-rotate()`. Do not use `backdrop-filter` or `mix-blend-mode`.
9. **Tactile active interaction [COMPONENT-REQUIRED]**: Always define `.$rootClass:active { transform: scale(...) translateY(...); }`.
10. **Tactile spring micro-physics [COMPONENT-REQUIRED]**: Component MUST declare spring variables in `:root`:
    `--spring-damping: <number>;`, `--spring-stiffness: <number>;`, `--press-scale: <number>;`
    Use user-specified tactile physics when provided; otherwise use category defaults (e.g. Bumpers: 0.75 / 520 / 0.96; Stick Buttons: 0.72 / 480 / 0.90; Face/Dpad/System: 0.68 / 440 / 0.92). If neither is specified, use global defaults: `--spring-damping: 0.68; --spring-stiffness: 440; --press-scale: 0.92;`.

### SECTION 6 — COMPILER CAPABILITIES — WHAT PRIMITIVES ARE BEST FOR:
#### ✅ FULLY SUPPORTED:
- `radial-gradient`: Spherical/concave shading, highlights, ambient glow.
- `linear-gradient`: Rake angles, light slopes, horizontal sheens, bevels.
- `conic-gradient`: Brushed metallic bezels, segmented rotary dials, sheen rings.
- `box-shadow`: Outset socket shadows, inset bevel rims, recessed well depths.
- `border-radius`: Circles, capsules, squircles, rounded rects.
- `clip-path: polygon(...)`: Stars, hexagons, diamonds, shields, custom silhouettes.
- `filter: blur(Npx)`, `filter: brightness(N)`, `filter: contrast(N)`: Single-function GPU filters.
- Embedded `<svg>` & Vector Nodes: `<path d="...">`, `<circle>`, `<polygon>`, `<g>`. Supports `<defs>` gradient paint servers (`linearGradient`, `radialGradient`).
- Vector Emblem Glow Rule: Vector layers compile into GPU Skia paths; do NOT rely on SVG `<filter>` graphs (`feGaussianBlur`, `feDropShadow`) on vector paths for glow. Place an underlying HTML/CSS `<span class="emblem-ambient">` with `filter: blur(4px)` or `box-shadow` underneath the `<svg>` to cast a luminous ambient aura!
- SVG Transforms & Group Matrices: Native support for `<g transform="translate(x, y) rotate(deg)">` and direct `<path transform="...">`.
- `calc()` and `aspect-ratio`: Dynamic child dimensions.
- Flexbox: Flow and alignment (`display: flex`, `gap`, `justify-content`, `align-items`).

#### ❌ NOT SUPPORTED:
- `mix-blend-mode`, `backdrop-filter`, `@keyframes`, `transition:`, `mask`, `display: grid`.

### SECTION 7 — ARCHITECTURAL PATTERN: HTML/CSS BUTTON SHELL + EMBEDDED SVG VECTOR EMBLEM
When the user requests a character, hero, creature, vehicle, weapon, insignia, or intricate graphic:

⚠️ FORBIDDEN ANTI-PATTERN (NEVER DO THIS): Never build complex character faces, vehicle contours, or intricate emblems out of dozens of nested HTML <div> shapes or CSS clip-paths. They are brittle and hard to maintain.

✅ MANDATORY DUAL-ENGINE ARCHITECTURE (ALWAYS DO THIS):
1. The Outer HTML/CSS Button Shell (<button class="$rootClass" ...>):
   - Handles the 3D physical tactile housing, surface material, perimeter bevel, specular curvature arc (`::after`), recessed socket shadows (`box-shadow`), and tactile active spring micro-physics (`--spring-damping: 0.68; --spring-stiffness: 440;` with `.$rootClass:active`).
2. The Embedded `<svg class="button-emblem" viewBox="0 0 100 100">` Vector Emblem:
   - Embedded directly inside the `<button>`.
   - Uses clean SVG vector paths (`<path d="...">`, `<polygon points="...">`, `<circle>`, `<ellipse>`, `<line>`) to draw the exact character, emblem, or insignia.
   - Sized appropriately to sit centered or docked on the button face (e.g., `width: 50px; height: 50px; position: absolute;` or flexbox child).
   - Supports `<defs>` gradient paint servers (`<linearGradient id="...">`, `<radialGradient id="...">` with `<stop>`) or solid vector fills and strokes.
   - ⚠️ **VECTOR GLOW PATTERN**: Do NOT rely on SVG `<filter>` graphs (`<feGaussianBlur>`, `<feColorMatrix>`) on vector paths for glow. Instead, place an underlying HTML/CSS `<span class="emblem-ambient">` with `filter: blur(4px)` or `box-shadow: 0 0 16px var(--accent-glow)` underneath the `<svg>` to cast a luminous ambient aura!
   - ✅ **SVG TRANSFORMS & ROTATIONS**: You can freely use `<g transform="translate(x,y) rotate(deg)">` or direct `<path transform="...">` to distribute or rotate shapes (such as radial petals, emblems, gear teeth, or insignia rays) around a center point, or bake coordinates directly. Both are fully compiled.
3. The High-Contrast Control Typography (<span class="btn-label">):
   - Real DOM text for the gamepad key ensuring instantaneous legibility during high-speed gaming.
   - ⚠️ **NO TEXT ROTATION**: Keep labels, sub-labels, and hardware text markings in straight, unrotated `<span>` elements (avoid `transform: rotate(...)` on text nodes to ensure 100% crisp subpixel font rasterization on mobile displays).

### SECTION 8 — DESIGN QUALITY CRITERIA & CONDITIONAL RESTRAINT
**DESIGN QUALITY CRITERIA**:
1. *Recognizability*: Instantly identifiable key identity during gameplay.
2. *Legibility*: High contrast label readable at small handheld touch scales.
3. *Touch Affordance*: Visually communicates pressability, depth, and tactile actuation.
4. *Visual Hierarchy*: Primary glyph stands out above decorative bezels and ambient halos.
5. *Material Coherence*: Shading, highlights, and borders reflect a consistent material.
6. *Appropriate Depth*: Multi-tier inset/outset shadows creating realistic tactile socket recess.

**DESIGN RESTRAINT & VISUAL BALANCE**:
- Use the minimum number of layers necessary to express the user's concept clearly; do not remove meaningful visual detail merely to reduce layer count.
- Avoid unnecessary glow or excessive shadows that visually compete with the button label.

### SECTION 9 — ADAPTATION RULES (HANDLING IMPOSSIBLE REQUESTS)
When a user request exceeds compiler limits or platform capabilities:
1. Preserve the user's primary visual, structural, and thematic intent.
2. Adapt unsupported or impractical details to the nearest supported CSS/SVG representation.
3. Do not abandon the concept.
4. Do not explain limitations or output conversational excuses.
5. Return the best compilable implementation.

### SECTION 10 — VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT)
Self-check before output:
- [ ] Compiler Safety: Exactly one root `<button class="$rootClass"` with matching `data-control`, `data-category`, and `data-name`.
- [ ] Real DOM Text: Real DOM text labels with strong contrast and readable font size in unrotated `<span>` (`Text must be real DOM text`).
- [ ] Explicit Coordinates: Explicit px dimensions on root and layered children (`Set position: absolute, left, top, width, and height`).
- [ ] Tactile Physics: Valid active state `.$rootClass:active` with spring micro-physics (`--spring-damping`, `--spring-stiffness`) in `:root`.
- [ ] Clean Engine Profile: No forbidden properties (`Do not use @media`, no external fonts, no external scripts, no `mix-blend-mode`).
- [ ] Restraint & Coherence: Preserves the user's requested shape and applies appropriate design restraint without stripping meaningful detail.
- [ ] Complex Graphics Architecture: If a character, emblem, or complex graphic is requested, uses an embedded `<svg class="button-emblem" viewBox="0 0 100 100">` vector element with clean `<path d="...">` rather than brittle CSS `<div>` hacks. For vector glow, use an underlying CSS `<span>` with `filter: blur()` or `box-shadow` (no SVG `<filter>` graphs).
- [ ] Geometry & Dimensions: If inset child rings or padded inner layers are used, prefer `calc()` (e.g. `width: calc(100% - 16px)`) and `aspect-ratio: 1`.
- [ ] Vector Emblem Glow: Did you use an underlying CSS `<span>` with `filter: blur()` or `box-shadow` instead of an SVG `<filter>` graph (`feGaussianBlur`) for emblem glow?
- [ ] Typography: Are all text labels and markings (`<span>`) kept straight without `transform: rotate(...)` (unrotated)?

### SECTION 11 — AUTHORITATIVE OUTPUT CONTRACT
To ensure reliable programmatic compilation, return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text outside it.
""".trimIndent()

    private fun renderDesignParameters(options: AiDesignOptions): String {
        val sb = StringBuilder()
        sb.append("### USER DESIGN PARAMETERS & PREFERENCES:\n")
        val creativityDesc = when (options.creativity) {
            Creativity.LOW -> "Low (conservative interpretation of the user's requested concept; avoid unnecessary reinterpretation)"
            Creativity.MEDIUM -> "Medium (balanced interpretation with moderate artistic exploration)"
            Creativity.HIGH -> "High (bold reinterpretation, unusual geometry, materials, and visual treatment while preserving the user's concept)"
        }
        val complexityDesc = when (options.complexity) {
            Complexity.AUTO -> "Auto (infer appropriate construction complexity from the concept)"
            Complexity.SIMPLE -> "Simple (use a small number of meaningful visual layers and simple geometry)"
            Complexity.DETAILED -> "Detailed (use multiple meaningful layers, material transitions, secondary detailing, and moderately complex SVG geometry)"
            Complexity.EXTREME -> "Extreme (use the full supported CSS/SVG expressive range when useful, including intricate vector geometry and layered surface treatment; do not add meaningless decoration just to increase complexity)"
        }
        val fidelityDesc = when (options.fidelity) {
            Fidelity.FAITHFUL -> "Faithful (preserve recognizable motifs and visual relationships)"
            Fidelity.INSPIRED -> "Inspired (create an original design strongly influenced by them)"
            Fidelity.ABSTRACT -> "Abstract (extract only the essential visual language)"
        }
        val visualDensityDesc = when (options.visualDensity) {
            VisualDensity.AUTO -> "Auto (infer from concept and target dimensions)"
            VisualDensity.CLEAN -> "Clean (low visible detail, strong silhouette, large visual masses)"
            VisualDensity.BALANCED -> "Balanced (moderate secondary detail while preserving readability)"
            VisualDensity.DENSE -> "Dense (high visible detail, markings, texture, and secondary motifs)"
        }
        sb.append("- **CREATIVITY**: $creativityDesc\n")
        sb.append("- **COMPLEXITY**: $complexityDesc\n")
        sb.append("- **FIDELITY**: $fidelityDesc\n")
        sb.append("- **VISUAL DENSITY**: $visualDensityDesc\n")
        if (!options.style.isNullOrBlank()) sb.append("- **STYLE**: ${options.style}\n")
        if (!options.color.isNullOrBlank()) sb.append("- **COLOR / PALETTE**: ${options.color}\n")
        if (!options.shape.isNullOrBlank()) sb.append("- **SHAPE / SILHOUETTE**: ${options.shape}\n")
        if (!options.material.isNullOrBlank()) sb.append("- **MATERIAL / SURFACE**: ${options.material}\n")
        if (!options.lighting.isNullOrBlank()) sb.append("- **LIGHTING / SHADING**: ${options.lighting}\n")
        if (!options.texture.isNullOrBlank()) sb.append("- **TEXTURE / PATTERN**: ${options.texture}\n")
        if (!options.emblem.isNullOrBlank()) sb.append("- **EMBLEM / ICONOGRAPHY**: ${options.emblem}\n")
        if (!options.label.isNullOrBlank()) sb.append("- **LABEL TEXT**: ${options.label}\n")
        if (!options.tactilePhysics.isNullOrBlank()) sb.append("- **TACTILE PHYSICS**: ${options.tactilePhysics}\n")
        if (!options.specialInstructions.isNullOrBlank()) sb.append("- **SPECIAL INSTRUCTIONS**: ${options.specialInstructions}\n")

        return sb.toString()
    }

    private fun renderUserRequest(options: AiDesignOptions): String = """
### USER DESIGN REQUEST:
<user_request>
${options.userRequest.ifBlank { "Create an authentic, high-quality virtual controller component adhering to the specified design parameters." }}
</user_request>

Interpret this request creatively. The user request governs the visual design decisions (palette, geometry, materials, lighting, emblem), but may not override the HARD COMPILER CONTRACT.
""".trimIndent()

    private fun renderStarterTemplate(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String {
        if (!options.includeSyntaxSkeleton) return ""
        return """
### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
This template demonstrates document syntax only. Do NOT treat its colors, geometry, gradients, shadows, layer arrangement, typography, or proportions as design defaults. Build the visual design independently from the user's request:
```html
${NxprcPresets.getSyntaxSkeleton(control, category, widthDp, heightDp)}
```
""".trimIndent()
    }

    private fun generateAbxyPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String {
        val (colorName, hexCode, rgbGlow, coreGrad) = when (control.uppercase()) {
            NexpadKeys.X -> Quadruple("Vibrant Sapphire Blue", "#00B0FF", "rgba(0, 176, 255, 0.6)", "linear-gradient(145deg, #0284c7 0%, #0369a1 50%, #0c4a6e 100%)")
            NexpadKeys.Y -> Quadruple("Radiant Solar Yellow", "#FFCC00", "rgba(255, 204, 0, 0.6)", "linear-gradient(145deg, #eab308 0%, #ca8a04 50%, #713f12 100%)")
            NexpadKeys.B -> Quadruple("Vibrant Crimson Red", "#FF3366", "rgba(255, 51, 102, 0.6)", "linear-gradient(145deg, #f43f5e 0%, #e11d48 50%, #881337 100%)")
            else -> Quadruple("Vibrant Emerald Green", "#4ADE80", "rgba(74, 222, 128, 0.6)", "linear-gradient(145deg, #10b981 0%, #059669 50%, #047857 100%)")
        }

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Face Action Button for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (Standard Gamepad Face Button)
- **Category [GLOBAL-REQUIRED]**: $category
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)
- **Standard Color Profile [RECOMMENDED]**: $colorName (Accent: $hexCode, Glow: $rgbGlow)
- **Standard Core [RECOMMENDED]**: $coreGrad

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Momentary discrete user actuation with tactile depression and instant spring release.
- **Visual Affordance [RECOMMENDED]**: Prominent elevation, tactile socket well, clear pressability, high-contrast center label.
- **Optional Visual Language [OPTIONAL]**: Multi-stop radial gradients, specular highlight arcs, metallic chamfer rings, neon edge halos.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. The silhouette is completely yours: circle, hexagon, rounded rect, diamond, shield, or organic silhouette. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Matte Polycarbonate Body & Optical Depth**: Rich dual-cast molding — deep chassis base tones (`#14171e`, `#1c202a`, `#08090c`) with perimeter chamfer highlights, NOT flat monochrome or pure `#000`.
2. **Physical Contact Shadows & Recessed Socket**: Elevated dome seated inside subtle socket well (`box-shadow: 0 8px 24px rgba(0,0,0,0.65), inset 0 2px 4px rgba(255,255,255,0.4), inset 0 -6px 12px rgba(0,0,0,0.7)`).
3. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested.
4. **Legible High-Contrast Letterform**: Prominent center glyph ($control) with multi-stop 3D text shadow.

${engineBoundaries("nexpad-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Cyberpunk 2077 / Glassmorphism / Brushed Gunmetal / Retro Arcade / Minimal Flat / Anime Mecha / Custom]
- **COLOR / ACCENT**: [e.g. Neon cyan & dark obsidian / Crimson & carbon / Custom palette (Default: $hexCode)]
- **SHAPE / SILHOUETTE**: [e.g. Faceted octagon / Smooth capsule / Organic shield / Asymmetric shard (Default: Circular)]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for character art, hero logos, vehicle silhouettes, or intricate crests]
- **LABEL / GLYPH**: [e.g. "$control" / Custom text / SVG icon emblem (Default: "$control")]
- **MATERIAL / TEXTURE**: [e.g. Matte polycarbonate / Anodized aluminum / Smoked translucent glass / Stippled rubber]
- **LIGHTING & DEPTH**: [e.g. Top-left specular directional / Under-glow neon edge / Deep recessed socket]
- **TACTILE PHYSICS**: [e.g. Snappy micro-switch / Heavy spring depression / Soft fluid damping]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

${renderDesignParameters(options)}

${renderStarterTemplate(control, category, widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateDpadPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String {
        val arrowGlyph = when (control.uppercase()) {
            NexpadKeys.DOWN -> "▼"
            NexpadKeys.LEFT -> "◀"
            NexpadKeys.RIGHT -> "▶"
            NexpadKeys.DPAD -> "❖"
            else -> "▲"
        }

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller D-Pad Component for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${if (control.uppercase() == NexpadKeys.DPAD) "Unified 4-Way Cross Pad" else "Directional Arrow Button"})
- **Category [GLOBAL-REQUIRED]**: $category
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px;
- **Directional Glyph [RECOMMENDED]**: $arrowGlyph

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Directional navigation with crisp actuation along cardinal or diagonal axes.
- **Visual Affordance [RECOMMENDED]**: Clear directional affordance, central rocker pivot affordance, distinct directional touch zones.
- **Optional Visual Language [OPTIONAL]**: Recessed pivot well, laser-etched chevron markings, sloped directional gradients, tactile nubs.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. Cross, wedge, arrow, star, disc, or organic form are all valid. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Textured Matte ABS Plastic**: Deep chassis body tones (`#14171e`, `#1c202a`, `#08090c`) with subtle perimeter bevels, NOT flat grey or pure `#000`.
2. **Central Rocker Pivot Mechanics**: Authentic console D-pads rock around a central spherical pivot. When designing a 4-way cross or dish, include a recessed central pivot well (`::before` circular indent) simulating the physical rocker mechanism. When designing an individual directional button, slope the gradient along the direction of travel to communicate tactile inward tilt.
3. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic directional pads prioritize tactile finger purchase, molded cardinal bevels, and crisp physical contact shadows.
4. **High-Contrast Cardinal Directional Affordance**: Crisp directional indicators (arrow glyph $arrowGlyph, chevron, or vector path) with high contrast against the dark textured housing.

${engineBoundaries("dpad-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Stealth Matte Black / Cyberpunk High-Contrast Hazard / Retro Game Boy / Clean Minimal]
- **COLOR / ACCENT**: [e.g. Electric Cyan / Neon Amber / Stealth Dark / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. 12-point faceted cross / Segmented arrows / Radial disc / Wedge]
- **DIRECTIONAL MARKINGS**: [e.g. Laser-etched arrows / Glowing chevrons / Raised tactile nubs]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for custom center emblems, directional arrows, or intricate crests]
- **MATERIAL / TEXTURE**: [e.g. Textured ABS plastic / Brushed gunmetal / Rubberized grip]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

${renderDesignParameters(options)}

${renderStarterTemplate(control, category, widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateTriggerPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String {
        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Analog Trigger for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${if (control.uppercase() == NexpadKeys.LT) "Left Trigger" else "Right Trigger"})
- **Category [GLOBAL-REQUIRED]**: $category
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)
- **Labels [RECOMMENDED]**: Primary "$control"

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Analog progressive pull, pressure, and travel communication (throttle, brake, aim, fire).
- **Visual Affordance [RECOMMENDED]**: Analog Travel Affordance — progressive travel, depth, directional pull cues, active travel displacement (`scaleY(0.94) translateY(4px)`).
- **Optional Visual Language [OPTIONAL]**: Horizontal friction ribs, stippling, curved rake paddle angle, digital pressure telemetry.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. Ergonomic curved paddle, angular wedge, minimal capsule, or custom silhouette. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Progressive Analog Travel Mechanics**: Authentic analog triggers communicate progressive depth and travel within the bounding box (${widthDp}px x ${heightDp}px) with a gradient receding into the controller housing cavity, communicating analog travel and finger placement.
2. **Molded Traction Ribs**: Physical molded horizontal friction ridges (via Flexbox column or `::before` layered shadows) providing authentic fingertip grip for throttling, braking, or aiming.
3. **High-Contrast Clean Typography**: Prominent primary key indicator ("$control", font-size 26-30px, weight 900). Keep the typography clean and authentic to real console gamepads without artificial secondary sub-labels.
4. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic triggers focus on ergonomic paddle curvature, molded grip traction, and deep socket shadow wells.

${engineBoundaries("trigger-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Carbon Fiber Racing / Brembo Red Performance / Cyberpunk Neon Telemetry / Tactical Military]
- **COLOR / ACCENT**: [e.g. Racing Red / Neon Magenta / Titanium Gray / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. Ergonomic curved paddle / Angular wedge / Modern capsule / Asymmetric blade / Custom contour]
- **TRACTION GRIP**: [e.g. Horizontal rubberized ribs / Stippled texture / Slotted heat vents]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for weapon branding, team logos, vehicle silhouettes, or telemetry graphics]
- **LABELS**: [e.g. "$control" / Custom text / Icon only (Default: "$control")]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

${renderDesignParameters(options)}

${renderStarterTemplate(control, category, widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateBumperPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String = """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Shoulder Bumper for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${if (control.uppercase() == NexpadKeys.LB) "Left Bumper" else "Right Bumper"})
- **Category [GLOBAL-REQUIRED]**: $category
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Shallow tactile shoulder lever/rocker actuation with crisp microswitch click feedback.
- **Visual Affordance [RECOMMENDED]**: Physical shoulder lever profile seated in a chassis housing socket or seam, specular sheen highlight, shallow press displacement.
- **Optional Visual Language [OPTIONAL]**: Specular sheen arc, brushed metallic texture, chamfered housing seam, tactile ridge.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. The silhouette is completely yours: curved shoulder lever, angular stealth wedge, faceted cyber wing, horizontal blade, or organic contour. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Physical Shoulder Lever/Rocker Architecture**: Authentic gamepad bumpers are physical shoulder levers seated directly in a recessed chassis housing seam or socket on the controller shell, rather than floating abstract pills. The lever surface catches ambient light along its top shoulder contour.
2. **Convex Curvature Specular Sheen**: Specular highlight arc communicating convex molded polycarbonate catching studio light.
3. **Microswitch Click Actuation**: Unlike analog triggers, shoulder bumpers use crisp tactile microswitches with shallow travel displacement (`scale(0.96) translateY(2px)`) and snappy spring return (`--spring-damping: 0.75; --spring-stiffness: 520; --press-scale: 0.96;`).
4. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic bumpers feature clean industrial dark tones (`#2c3342` to `#0c0e13`), chassis seam contact shadows, and crisp high-contrast labels.

${engineBoundaries("bumper-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Brushed Gunmetal Aluminum / Matte Stealth Carbon / Sci-Fi Thruster / Minimalist]
- **COLOR / ACCENT**: [e.g. Electric Blue / Cyberpunk Yellow / Gunmetal / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. Ergonomic curved shoulder / Angled stealth wedge / Faceted cyber wing / Horizontal blade / Custom contour]
- **FINISH & SHEEN**: [e.g. Horizontal specular arc / Frosted matte / Edge illumination]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for faction crests, wing markings, hero logos, or intricate insignias]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

${renderDesignParameters(options)}

${renderStarterTemplate(control, category, widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()

    private fun generateStickPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String {
        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Analog Thumbstick Component for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Control Key [COMPONENT-REQUIRED]**: $control (Analog Thumbstick - NO center click button)
- **Category [GLOBAL-REQUIRED]**: $category
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Continuous 360-degree analog navigation. IMPORTANT: In NEXPAD, Analog Joysticks have NO center button or click mechanism. Stick click is strictly separated into standalone LSB/RSB buttons to prevent accidental clicks while dragging.
- **Visual Affordance [RECOMMENDED]**: Outer gimbal socket well, concave thumb dome, concentric knurled grip texture for traction.
- **Optional Visual Language [OPTIONAL]**: Knurled dashed rings, radial tick marks, cross-hatch metal, rubberized stippling, cardinal directional markers (▲, ▼, ◀, ▶).
- **Geometry [USER-OVERRIDE]**: Circle geometry is natural and authentic for physical joystick gimbal, socket, and thumb cap. Do not make it look like a flat circular web button. `data-category` is metadata, not a shape instruction. Gimbal ring, dish, square housing, or stylized silhouette: preserve the user's requested shape.

### JOYSTICK TWO-ZONE PHYSICAL MECHANISM:
In physical gamepads (Xbox, PlayStation) and mobile gaming (CoD Mobile, Genshin, PUBG), an analog stick consists of TWO distinct physical parts:
1. **Stationary Gimbal Base (The Fixed Socket Housing)**:
   - Must remain 100% stationary at (0, 0) — never translates during thumb drag.
   - Contains: Outer bezel rim, deep recessed spherical socket well (`box-shadow: inset ...`), directional tick marks, axis lines, and directional markers (▲, ▼, ◀, ▶).
   - Use container `<div class="stick-base">` or element classes containing: `base`, `socket`, `bezel`, `outer-ring`, `ticks`, `marker`.
2. **Movable Analog Thumb Cap (The Inner Dome)**:
   - Sized at approximately 55%–65% of the base diameter (~${(widthDp * 0.58).toInt()}px to ${(widthDp * 0.65).toInt()}px) to provide mechanical clearance inside the socket.
   - **Only this part translates (x, y)** when the player drags their thumb, and springs back to center on release!
   - Contains: Concave thumb dish, knurled traction grip rings, custom vector emblems/graphics, or center $control marking (<span class="stick-label">$control</span>, NO center click button).
   - **MANDATORY DOM PLACEMENT**: Put ALL cap elements (dome background, knurled rings, graphics, label) inside `<div class="stick-cap">` or element classes containing: `stick-cap`, `thumb`, `grip`, `core`, `stick-label`. Never attach thumb cap elements directly to the root `<button>` or use `.stick-btn::before`/`::after` for the moving cap, as that causes the cap to freeze to the stationary socket!
3. **Continuous Analog Navigation (NO Center Button)**:
   - **360° Analog Deflection**: Handled dynamically at runtime by NEXPAD's touch vector engine with spring return physics when dragged. **Do not write JavaScript, CSS transitions/animations, or hover/pointer events for analog movement.**
   - **Zero Center Button Interference**: In NEXPAD, thumbsticks do NOT actuate L3/R3 on click or press. Stick click is strictly isolated in dedicated standalone LSB/RSB buttons.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Matte Charcoal & Polycarbonate Plastic**: Base chassis tones `#14171e`, `#1c202a`, `#08090c` with subtle surface specular rim highlights, NOT flat grey or pure `#000`.
2. **Physical Material Contrast**: The outer gimbal socket is a deep, recessed cavity (`box-shadow: inset 0 -8px 16px rgba(0,0,0,0.85)`). The inner thumb cap is textured molded rubber/elastomer with knurled traction rings or micro-ribs.
3. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic gamepads feature clean micro-textures, matte finishes, and crisp physical contact shadows.
4. **Mechanical Clearance & Proportions**: The thumb cap diameter must be approximately 55%–65% of the total socket diameter (~${(widthDp * 0.60).toInt()}px for ${widthDp}px socket) to provide authentic travel clearance inside the housing well. A 1:1 cap-to-socket ratio looks like a broken button, not an analog stick!

${engineBoundaries("stick-btn")}

### VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT):
Before outputting, verify your component against this checklist:
- [ ] Two-Zone DOM Structure: Stationary base enclosed in `<div class="stick-base">`, movable cap enclosed in `<div class="stick-cap">`.
- [ ] Mechanical Clearance: Thumb cap diameter is ~55%–65% of socket diameter (~${(widthDp * 0.60).toInt()}px) for realistic travel clearance.
- [ ] No Frozen Cap Elements: Knurled rings, traction ridges, emblems, and label are placed INSIDE `<div class="stick-cap">`.
- [ ] NO Center Click Button: The analog stick has NO center click button or L3/R3 marking. It is labeled "$control" in an unrotated `<span>` (or clean vector/directional art). Stick click is handled separately by LSB/RSB.
- [ ] Complex Graphics Architecture: If a character, emblem, or complex graphic is requested on the thumb cap, uses an embedded `<svg>` vector element inside `<div class="stick-cap">` with clean `<path d="...">` rather than brittle CSS `<div>` hacks. For vector glow, use an underlying CSS `<span>` (no SVG `<filter>` graphs).
- [ ] Console Realism: Authentic industrial materials (matte charcoal, rubberized dish, physical shadows) rather than unsolicited neon glow.
- [ ] No Scripts or Page CSS: Zero JavaScript, zero CSS keyframes animations, zero hover/pointer event handlers.
- [ ] Compiler Safety: Exactly one root `<button class="stick-btn">` element; all px dimensions explicit.

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Tactical Rubber Dome / Xbox Elite Magnetic Swappable / DualSense Two-Tone / Arcade Flight-Sim]
- **COLOR / ACCENT**: [e.g. Neon Emerald Green / Cyberpunk Cyan / Stealth Black / Custom palette]
- **THUMB DOME**: [e.g. Deep concave dish / Convex textured dome / Cross-hatch metallic surface]
- **KNURLING & TRACTION**: [e.g. Concentric dashed rings / Radial tick marks / Diamond knurl texture]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) placed inside `<div class="stick-cap">` for anime icons, hero crests, or custom emblems]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

${renderDesignParameters(options)}

${renderStarterTemplate(control, category, widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateStickButtonPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String {
        val clickLabel = if (control.uppercase() == NexpadKeys.RSB || control.uppercase() == "RSB") "Right Stick Click (RSB / R3)" else "Left Stick Click (LSB / L3)"

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Stick Click Button Component for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control ($clickLabel)
- **Category [GLOBAL-REQUIRED]**: $category (Specialized Thumbstick Click Button under Sticks category, compiled as BUTTON)
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Instant tactile thumbstick cap depression / axial click ($clickLabel action). Unlike continuous 360° analog sticks, this is a dedicated digital button for reliable, rapid stick clicks during high-intensity gameplay.
- **Visual Affordance [RECOMMENDED]**: Circular thumbstick cap profile with knurled perimeter grip ring, concave thumb dish, radial lighting, and tactile spring micro-physics.
- **Optional Visual Language [OPTIONAL]**: Dashed traction ring, radial tick notches, rubberized stippling, edge illumination.
- **Geometry [USER-OVERRIDE]**: Circular geometry authentic to console thumbsticks is recommended, but user's requested style or custom contour always takes precedence.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Single-Button Tactile Architecture**: Unlike analog joysticks which require a stationary base + moving cap two-zone split, this dedicated stick button is a single unified button (`<button class="stick-btn-ctl" data-control="$control" data-category="BUTTON">`). The entire cap depresses with spring return physics.
2. **Textured Thumbstick Cap Dish**: A recessed center dish with knurled perimeter rim communicating molded rubber/elastomer thumb grip.
3. **Restrained Detailing & Tactile Lighting**: Clean dark polycarbonate tones (`#333333` to `#141414`) with subtle accent glow and crisp high-contrast label.
4. **Tactile Spring Micro-Physics**: Configure in `:root`:
   `--spring-damping: 0.72; --spring-stiffness: 480; --press-scale: 0.90;`

${engineBoundaries("stick-btn-ctl")}

### VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT):
Before outputting, verify your component against this checklist:
- [ ] No Two-Zone Analog Split: Dedicated digital button (<button class="stick-btn-ctl" data-control="$control" data-category="BUTTON">) with single unified surface (no base/socket split).
- [ ] Compiler Safety: Exactly one root `<button class="stick-btn-ctl" data-control="$control" data-category="BUTTON">`.
- [ ] Complex Graphics Architecture: If a character, emblem, or complex graphic is requested, uses an embedded `<svg class="button-emblem" viewBox="0 0 100 100">` vector element with clean `<path d="...">` rather than brittle CSS `<div>` hacks. For vector glow, use an underlying CSS `<span>` (no SVG `<filter>` graphs).
- [ ] Tactile Physics: Valid :active state with `--spring-damping` and `--spring-stiffness`.

### USER CUSTOMIZATION SCHEMA:
- **STYLE**: [e.g. Tactical Thumbstick / Xbox Elite Swappable Cap / Cyberpunk Neon / Stealth Carbon]
- **COLOR / ACCENT**: [e.g. Cyan / Magenta / Emerald / Amber / Custom palette]
- **TRACTION GRIP**: [e.g. Dashed knurled ring / Radial ticks / Stippled texture]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for custom cap insignia]
- **LABELS**: [e.g. "$control" / Custom glyph (Default: "$control")]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

${renderDesignParameters(options)}

${renderStarterTemplate(control, "BUTTON", widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateTouchpadPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String {
        val isLeft = control.equals("LTP", ignoreCase = true)
        val padRole = if (isLeft) "Left Touch Movement Pad (Floating Dynamic-Center Stick)" else "Right Touch Camera Look Pad (Free-Look Swipe Trackpad)"
        val interactionDesc = if (isLeft) {
            "Continuous 360° character locomotion via touch drag. Touching anywhere establishes a dynamic anchor pivot; dragging directs walking/sprinting with laptop-touchpad speed-to-distance transfer (2.0x default sensitivity)."
        } else {
            "Free-look camera panning via touch swipe deltas. Dragging converts instantaneous finger velocity into right-stick camera deflection with immediate stop when stationary and 2.0x default gaming ballistics."
        }

        return """
${genAiHeader()}

### TARGET COMPONENT IDENTITY:
- **Canonical Control Key [GLOBAL-REQUIRED]**: $control
- **Component Role**: $padRole
- **Category [GLOBAL-REQUIRED]**: $category (Touchpad Surface under Sticks category)
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: $interactionDesc IMPORTANT: Touchpads are pure flat, stationary laptop-style trackpad surfaces. Strictly NO center button, NO center dot, NO movable ring, and NO tap-to-click mechanism. Stick click is strictly separated into standalone LSB/RSB buttons to prevent accidental sprint or melee triggers during camera panning or movement. The touchpad operates as a pure continuous speed-to-distance surface.
- **Visual Affordance [RECOMMENDED]**: Expansive rounded-rectangular trackpad surface with deep matte texture, smooth glass touch feel, subtle peripheral bevel/frame, and high-contrast technical typography. Strictly NO center button or dot, and NO joystick-style circular ring.
- **Optional Visual Language [OPTIONAL]**: Subtle corner alignment marks, inset glass framing, ambient edge illumination, carbon-fiber stippling.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM (STEAM DECK):
1. **Single-Surface Trackpad Architecture**: One expansive root `<button class="touchpad-ctl" data-id="touch_${control.lowercase()}" data-control="$control" data-category="$category" data-name="Touchpad $control">`.
2. **Textured Recessed Dish**: Deep carbon/polycarbonate matte finish with inset drop shadow and smooth laser-etched touch feel.
3. **Stationary Trackpad Surface**: Subtle inner boundary frame (`<div class="touchpad-surface"></div>`) or corner alignment marks. Completely stationary surface with NO movable ring, NO sliding thumb cap, and NO center dot.
4. **Header and Subtext Markings**: Technical monospace typography denoting touch mode (e.g. "${if (isLeft) "Touch Move • LTP" else "Touch Look • RTP"}") and ballistics ("2.0X BALLISTICS"). Do NOT include stick click or tap click text.

${engineBoundaries("touchpad-ctl")}

${renderDesignParameters(options)}

${renderStarterTemplate(control, category, widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateSystemPrompt(control: String, category: String, widthDp: Int, heightDp: Int, options: AiDesignOptions): String = """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller System/Utility Button for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${when(control.uppercase()) { NexpadKeys.MENU, NexpadKeys.START -> "Menu / Pause / Start"; NexpadKeys.VIEW, NexpadKeys.BACK -> "View / Back / Select"; else -> "Home / Guide / Nexus" }})
- **Category [GLOBAL-REQUIRED]**: $category
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px;

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Secondary console utility actions (menu, pause, guide, view, options).
- **Visual Affordance [RECOMMENDED]**: Compact, flush or low-profile footprint, immediate iconography recognition, subtle tactile click.
- **Optional Visual Language [OPTIONAL]**: Flexbox hamburger pause bars, overlapping dual rectangles, glowing nexus guide emblem.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. Pill, sphere, tile, emblem, or custom form are all valid. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Flush Low-Profile Utility Ergonomics**: System buttons (MENU, VIEW, HOME, SHARE) on authentic gamepads are secondary utility controls. They feature a compact, flush or slightly recessed profile to prevent accidental presses during intense gameplay.
2. **Crisp Authentic Iconography**:
   - `MENU`: 3 horizontal hamburger bars with clean vertical flexbox column spacing (`gap: 4px`), rounded pill ends, and clean white/silver contrast.
   - `VIEW`: Overlapping dual windows/rectangles symbol (`⧉`) or vector path.
   - `HOME` / `GUIDE`: Central nexus orb / emblem with subtle radial glow and chamfered bezel ring.
3. **Zero Text Collision on Graphic Buttons**: Iconographic system buttons (such as MENU hamburger bars or VIEW windows) must NEVER have automatic text stamped over their icons. The icon itself is the visual identity.
4. **Restrained Lighting & Tactile Click**: Subtle recessed socket well (`box-shadow: inset 0 1px 3px rgba(255,255,255,0.25), inset 0 -3px 6px rgba(0,0,0,0.75)`), matte chassis darks, and shallow tactile micro-travel (`scale(0.92) translateY(2px)`).

${engineBoundaries("system-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Minimalist Matte Dark Pill / Cyberpunk Neon Toggle / Xbox Series Glass Guide / Brushed Steel Switch]
- **COLOR / ACCENT**: [e.g. Subtle Cool White / Neon Yellow / Amber / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. Compact pill / Circular guide emblem / Rounded tile]
- **ICONOGRAPHY**: [e.g. Hamburger bars / Dual overlapping rectangles / Nexus sphere emblem]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for custom guide logos, nexus crests, or game symbols]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

${renderDesignParameters(options)}

${renderStarterTemplate(control, category, widthDp, heightDp, options)}

${renderUserRequest(options)}

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
