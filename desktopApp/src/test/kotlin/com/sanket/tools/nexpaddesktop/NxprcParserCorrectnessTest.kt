package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.engine.css.CssCascadeResolver
import com.sanket.tools.nexpad.nxprc.engine.css.CssTokenizer
import com.sanket.tools.nexpad.nxprc.engine.dom.HtmlDomParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NxprcParserCorrectnessTest {
    @Test
    fun ignoresDoctypeCommentsAndKeepsVoidTagsFromCorruptingTheTree() {
        val parsed = HtmlDomParser.parse("""
            <!DOCTYPE html><!-- comment -->
            <button class="nexpad-btn" data-control="A" data-category="BUTTON">
                <img src="x"><span>A</span><source src="x"><span>B</span>
            </button>
        """.trimIndent())
        val button = parsed.root.findByTag("button").single()

        assertEquals(listOf("img", "span", "source", "span"), button.children.map { it.tag })
        assertTrue(button.textContent.isBlank())
    }

    @Test
    fun supportsCompoundClassesChildSelectorsAndImportantValues() {
        val parsed = HtmlDomParser.parse("""
            <style>
              .parent .child { opacity: .2; }
              .nexpad-btn.primary { color: #00ff00 !important; }
              .parent > .child { opacity: .5 !important; }
            </style>
            <button class="nexpad-btn primary"><span class="parent"><span class="child">A</span></span></button>
        """.trimIndent())
        val stylesheet = CssTokenizer.parse(parsed.embeddedCss)
        val button = parsed.root.findByTag("button").single()
        val parent = button.children.single()
        val child = parent.children.single()

        assertEquals("#00ff00", CssCascadeResolver.computeStyle(button, stylesheet).base["color"])
        assertEquals(".5", CssCascadeResolver.computeStyle(child, stylesheet).base["opacity"])
    }

    @Test
    fun inspectUserTriggerHtml() {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>NEXPAD Neo Tactile LT Trigger</title>

<style>
:root {
  --trigger-size-w: 110px;
  --trigger-size-h: 140px;

  --body-0: #050607;
  --body-1: #0c0f12;
  --body-2: #171b20;
  --body-3: #282f36;
  --body-4: #384149;

  --accent: #48d8ff;
  --accent-soft: rgba(72,216,255,0.24);

  --spring-damping: 0.68;
  --spring-stiffness: 440;
  --press-scale: 0.94;
}

* {
  box-sizing: border-box;
}

html,
body {
  margin: 0;
  width: 100%;
  height: 100%;

  background:
    radial-gradient(
      ellipse at 50% 34%,
      #3b4147 0%,
      #1a1e22 46%,
      #080a0c 100%
    );

  display: flex;
  align-items: center;
  justify-content: center;

  font-family:
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    Roboto,
    Arial,
    sans-serif;
}

.trigger-btn {
  position: relative;

  width: 110px;
  height: 140px;

  padding: 0;
  border: 0;
  margin: 0;

  border-radius: 30px;

  background:
    radial-gradient(
      ellipse at 27% 12%,
      rgba(255,255,255,0.09) 0%,
      transparent 34%
    ),
    radial-gradient(
      ellipse at 72% 88%,
      rgba(0,0,0,0.80) 0%,
      transparent 58%
    ),
    linear-gradient(
      150deg,
      var(--body-4) 0%,
      var(--body-3) 26%,
      var(--body-2) 57%,
      var(--body-0) 100%
    );

  border: 2px solid rgba(2,3,4,0.96);

  box-shadow:
    0 11px 20px rgba(0,0,0,0.78),
    0 2px 3px rgba(255,255,255,0.05),
    inset 0 3px 4px rgba(255,255,255,0.08),
    inset 0 -12px 18px rgba(0,0,0,0.86);

  overflow: hidden;

  transform-origin: 50% 82%;
  z-index: 0;
}

/* Molded outer socket */
.trigger-btn::before {
  content: "";

  position: absolute;
  left: 6px;
  top: 6px;

  width: 98px;
  height: 128px;

  border-radius: 25px;

  background:
    radial-gradient(
      ellipse at 50% 7%,
      rgba(255,255,255,0.055),
      transparent 31%
    ),
    linear-gradient(
      154deg,
      #343b42 0%,
      #242a30 38%,
      #12161a 100%
    );

  box-shadow:
    inset 0 2px 3px rgba(255,255,255,0.08),
    inset 0 -9px 13px rgba(0,0,0,0.84),
    0 3px 6px rgba(0,0,0,0.42);

  z-index: 1;
}

/* Socket well */
.trigger-channel {
  position: absolute;

  left: 13px;
  top: 11px;

  width: 84px;
  height: 118px;

  border-radius: 23px;

  background:
    radial-gradient(
      ellipse at 50% 15%,
      rgba(255,255,255,0.065),
      transparent 30%
    ),
    radial-gradient(
      ellipse at 50% 82%,
      rgba(0,0,0,0.48),
      transparent 68%
    ),
    linear-gradient(
      157deg,
      #252b31 0%,
      #171b20 48%,
      #080b0d 100%
    );

  box-shadow:
    inset 0 4px 7px rgba(0,0,0,0.72),
    inset 0 -4px 7px rgba(255,255,255,0.035),
    0 2px 3px rgba(0,0,0,0.52);

  z-index: 2;
}

/* Main ergonomic paddle */
.trigger-surface {
  position: absolute;

  left: 19px;
  top: 15px;

  width: 72px;
  height: 108px;

  border-radius: 21px 21px 24px 24px;

  background:
    radial-gradient(
      ellipse at 31% 8%,
      rgba(255,255,255,0.17) 0%,
      transparent 27%
    ),
    radial-gradient(
      ellipse at 70% 88%,
      rgba(0,0,0,0.62) 0%,
      transparent 57%
    ),
    linear-gradient(
      160deg,
      #454d55 0%,
      #333a41 25%,
      #20262b 55%,
      #101418 100%
    );

  border: 1px solid rgba(0,0,0,0.9);

  box-shadow:
    0 3px 5px rgba(0,0,0,0.55),
    inset 0 3px 4px rgba(255,255,255,0.105),
    inset 2px 0 3px rgba(255,255,255,0.025),
    inset -3px 0 5px rgba(0,0,0,0.36),
    inset 0 -11px 15px rgba(0,0,0,0.76);

  transform-origin: 50% 88%;

  z-index: 3;
}

/* Curved central depression */
.trigger-surface::before {
  content: "";

  position: absolute;

  left: 7px;
  top: 8px;

  width: 58px;
  height: 92px;

  border-radius: 18px;

  background:
    radial-gradient(
      ellipse at 50% 16%,
      rgba(255,255,255,0.045),
      transparent 34%
    ),
    linear-gradient(
      163deg,
      rgba(0,0,0,0.06),
      rgba(0,0,0,0.38)
    );

  box-shadow:
    inset 0 3px 5px rgba(0,0,0,0.30),
    inset 0 -3px 5px rgba(255,255,255,0.025);

  z-index: 4;
}

/* Physical traction ribs */
.trigger-grips {
  position: absolute;

  left: 27px;
  top: 46px;

  width: 56px;
  height: 48px;

  background:
    repeating-linear-gradient(
      to bottom,
      rgba(255,255,255,0.105) 0px,
      rgba(255,255,255,0.105) 2px,
      rgba(0,0,0,0.18) 2px,
      rgba(0,0,0,0.18) 5px,
      transparent 5px,
      transparent 9px
    );

  border-radius: 12px;

  opacity: 0.72;

  box-shadow:
    inset 0 1px 1px rgba(255,255,255,0.035),
    0 1px 1px rgba(0,0,0,0.22);

  z-index: 5;
}

/* Upper optical accent */
.trigger-light {
  position: absolute;

  left: 31px;
  top: 25px;

  width: 48px;
  height: 3px;

  border-radius: 50%;

  background:
    radial-gradient(
      ellipse,
      var(--accent) 0%,
      rgba(72,216,255,0.42) 42%,
      transparent 82%
    );

  box-shadow:
    0 0 7px var(--accent-soft),
    0 0 13px rgba(72,216,255,0.10);

  opacity: 0.68;

  z-index: 6;
}

/* Lower finger-contact cavity */
.trigger-bottom {
  position: absolute;

  left: 29px;
  bottom: 23px;

  width: 52px;
  height: 12px;

  border-radius: 50%;

  background:
    radial-gradient(
      ellipse,
      rgba(0,0,0,0.62) 0%,
      rgba(0,0,0,0.27) 48%,
      transparent 78%
    );

  box-shadow:
    0 -1px 2px rgba(255,255,255,0.025);

  z-index: 6;
}

/* Small machined side markers */
.trigger-mark {
  position: absolute;

  left: 16px;
  top: 47px;

  width: 3px;
  height: 33px;

  border-radius: 2px;

  background:
    linear-gradient(
      to bottom,
      transparent,
      rgba(255,255,255,0.14) 25%,
      rgba(255,255,255,0.14) 75%,
      transparent
    );

  opacity: 0.45;

  z-index: 4;
}

/* Real DOM label */
.trigger-label {
  position: absolute;

  left: 0;
  bottom: 16px;

  width: 110px;

  text-align: center;

  font-size: 27px;
  font-weight: 900;

  line-height: 30px;
  letter-spacing: 1px;

  color: #e8edf1;

  text-shadow:
    0 1px 1px rgba(0,0,0,0.92),
    0 -1px 0 rgba(255,255,255,0.08);

  z-index: 8;
}

/* Active tactile compression */
.trigger-btn:active {
  transform:
    scale(var(--press-scale))
    translateY(4px);

  box-shadow:
    0 5px 10px rgba(0,0,0,0.80),
    inset 0 5px 8px rgba(0,0,0,0.80),
    inset 0 -5px 9px rgba(255,255,255,0.025);
}

.trigger-btn:active .trigger-surface {
  transform:
    scaleY(0.985)
    translateY(3px);

  box-shadow:
    0 2px 3px rgba(0,0,0,0.55),
    inset 0 5px 7px rgba(0,0,0,0.42),
    inset 0 -6px 9px rgba(0,0,0,0.78);
}

.trigger-btn:active .trigger-light {
  opacity: 1;
  filter: brightness(1.22);
}

.trigger-btn:active .trigger-grips {
  filter: brightness(0.86);
}
</style>
</head>

<body>

<button
  class="trigger-btn"
  data-control="LT"
  data-category="TRIGGER"
  data-name="Neo Tactile Left Trigger"
>
  <div class="trigger-channel"></div>

  <div class="trigger-surface"></div>

  <div class="trigger-grips"></div>

  <div class="trigger-mark"></div>

  <div class="trigger-light"></div>

  <div class="trigger-bottom"></div>

  <span class="trigger-label">LT</span>
</button>

</body>
</html>
        """.trimIndent()

        println("=== TESTING CONVERSION ===")
        val normalized = com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter.normalizeAiHtml(html)
        println("NORMALIZED HTML LENGTH: ${normalized.length}")

        val result = com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter.convertWithWarnings(
            source = html,
            id = "trigger-neo",
            name = "Neo Tactile Left Trigger",
            category = "TRIGGER",
            defaultControl = "LT"
        )

        println("WARNINGS count = ${result.warnings.size}")
        result.warnings.forEach { println("  WARNING: ${it.code}: ${it.message}") }

        val doc = result.document
        println("DOC canvas size = ${doc.canvas.viewBoxWidth} x ${doc.canvas.viewBoxHeight}")
        println("LAYERS count = ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("Layer #$i [${layer::class.simpleName}]: $layer")
        }
    }
}

