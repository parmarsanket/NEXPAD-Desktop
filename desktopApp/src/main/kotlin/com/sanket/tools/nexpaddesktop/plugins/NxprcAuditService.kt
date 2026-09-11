package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.nxprc.*
import java.awt.*
import java.awt.geom.*
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.Raster
import java.io.File
import javax.imageio.ImageIO

/**
 * NxprcAuditService provides headless browser capture, native raster rendering,
 * visual parity score computation, and layer metadata inspection for NEXPAD Desktop.
 */
object NxprcAuditService {

    /**
     * Auto-discovers Google Chrome executable on Windows, macOS, or Linux.
     */
    fun findChromePath(): String? {
        val candidates = mutableListOf<String>()

        val os = System.getProperty("os.name", "").lowercase()
        if (os.contains("win")) {
            candidates.add("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")
            candidates.add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe")
            val localAppData = System.getenv("LOCALAPPDATA")
            if (!localAppData.isNullOrBlank()) {
                candidates.add("$localAppData\\Google\\Chrome\\Application\\chrome.exe")
            }
            val programFiles = System.getenv("ProgramFiles")
            if (!programFiles.isNullOrBlank()) {
                candidates.add("$programFiles\\Google\\Chrome\\Application\\chrome.exe")
            }
        } else if (os.contains("mac")) {
            candidates.add("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
        } else {
            candidates.add("/usr/bin/google-chrome")
            candidates.add("/usr/bin/chromium-browser")
            candidates.add("/usr/bin/chromium")
        }

        return candidates.firstOrNull { File(it).exists() }
    }

    /**
     * Captures true browser rendering via Headless Google Chrome.
     */
    fun captureChromeScreenshot(html: String, width: Int = 400, height: Int = 400): BufferedImage? {
        val chromePath = findChromePath() ?: return null

        return try {
            val tempDir = File(System.getProperty("java.io.tmpdir"), "nexpad_audit").apply { mkdirs() }
            val htmlFile = File(tempDir, "preview_audit_${System.currentTimeMillis()}.html")
            val outFile = File(tempDir, "chrome_audit_${System.currentTimeMillis()}.png")

            val styledHtml = wrapHtmlForPreview(html, width, height)
            htmlFile.writeText(styledHtml)

            val fileUrl = htmlFile.toURI().toString()
            val cmd = arrayOf(
                "cmd.exe", "/c",
                "start", "/wait", "\"\"",
                "\"$chromePath\"",
                "--headless=new",
                "--disable-gpu",
                "--window-size=$width,$height",
                "--screenshot=\"${outFile.absolutePath}\"",
                "\"$fileUrl\""
            )

            val proc = ProcessBuilder(*cmd).start()
            proc.waitFor()

            val resultImg = if (outFile.exists() && outFile.length() > 0) {
                ImageIO.read(outFile)
            } else null

            // Cleanup temp files
            htmlFile.delete()
            outFile.delete()

            resultImg
        } catch (_: Exception) {
            null
        }
    }

    private fun wrapHtmlForPreview(html: String, width: Int, height: Int): String {
        return if (html.contains("<body>", ignoreCase = true)) {
            html.replace(
                Regex("<body>", RegexOption.IGNORE_CASE),
                """<body style="margin:0; padding:0; background-color:#0B0E14; width:${width}px; height:${height}px; display:flex; align-items:center; justify-content:center; overflow:hidden;">"""
            )
        } else {
            """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="margin:0; padding:0; background-color:#0B0E14; width:${width}px; height:${height}px; display:flex; align-items:center; justify-content:center; overflow:hidden;">
            $html
            </body>
            </html>
            """.trimIndent()
        }
    }

    /**
     * Crops image symmetrically anchored around the image center so directional drop shadows
     * never pull the button off-center.
     */
    fun cropToButtonContentSymmetric(img: BufferedImage): BufferedImage {
        var minX = img.width
        var maxX = 0
        var minY = img.height
        var maxY = 0
        val bgR = 0x0B
        val bgG = 0x0E
        val bgB = 0x14

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val rgb = img.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                val diff = Math.abs(r - bgR) + Math.abs(g - bgG) + Math.abs(b - bgB)
                if (diff > 25) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        if (maxX > minX && maxY > minY) {
            val pad = 12
            val cx = img.width / 2
            val cy = img.height / 2
            val halfW = maxOf(Math.abs(cx - minX), Math.abs(maxX - cx)) + pad
            val halfH = maxOf(Math.abs(cy - minY), Math.abs(maxY - cy)) + pad
            val cropX = (cx - halfW).coerceAtLeast(0)
            val cropY = (cy - halfH).coerceAtLeast(0)
            val cropW = (halfW * 2).coerceAtMost(img.width - cropX)
            val cropH = (halfH * 2).coerceAtMost(img.height - cropY)
            return img.getSubimage(cropX, cropY, cropW, cropH)
        }
        return img
    }

    /**
     * Computes pixel-by-pixel Euclidean RGB visual parity percentage (0% to 100%).
     */
    fun computeVisualParity(imgA: BufferedImage, imgB: BufferedImage): Double {
        val croppedA = cropToButtonContentSymmetric(imgA)
        val croppedB = cropToButtonContentSymmetric(imgB)

        val compSize = 200
        val scaledA = BufferedImage(compSize, compSize, BufferedImage.TYPE_INT_RGB)
        val gA = scaledA.createGraphics()
        gA.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        gA.drawImage(croppedA, 0, 0, compSize, compSize, null)
        gA.dispose()

        val scaledB = BufferedImage(compSize, compSize, BufferedImage.TYPE_INT_RGB)
        val gB = scaledB.createGraphics()
        gB.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        gB.drawImage(croppedB, 0, 0, compSize, compSize, null)
        gB.dispose()

        var totalSimilarity = 0.0
        val maxDist = Math.sqrt(3.0 * 255.0 * 255.0)

        for (y in 0 until compSize) {
            for (x in 0 until compSize) {
                val rgbA = scaledA.getRGB(x, y)
                val rgbB = scaledB.getRGB(x, y)

                val rA = (rgbA shr 16) and 0xFF
                val gAVal = (rgbA shr 8) and 0xFF
                val bA = rgbA and 0xFF

                val rB = (rgbB shr 16) and 0xFF
                val gBVal = (rgbB shr 8) and 0xFF
                val bB = rgbB and 0xFF

                val dr = (rA - rB).toDouble()
                val dg = (gAVal - gBVal).toDouble()
                val db = (bA - bB).toDouble()

                val dist = Math.sqrt(dr * dr + dg * dg + db * db)
                val sim = 1.0 - (dist / maxDist)
                totalSimilarity += sim
            }
        }

        return (totalSimilarity / (compSize * compSize)) * 100.0
    }

    /**
     * Renders an NXPRC Document (or specific layers) into a BufferedImage using AWT Skia simulation.
     */
    fun renderNxprcToImage(
        doc: NxprcDocument,
        width: Int,
        height: Int,
        activeLayersOnly: List<CanvasLayer>? = null
    ): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = img.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        g2.color = Color(0x0B, 0x0E, 0x14)
        g2.fillRect(0, 0, width, height)

        val viewBoxW = doc.canvas.viewBoxWidth.coerceAtLeast(1f)
        val viewBoxH = doc.canvas.viewBoxHeight.coerceAtLeast(1f)
        val maxTargetDim = 280f
        val viewScale = minOf(maxTargetDim / viewBoxW, maxTargetDim / viewBoxH)
        val btnW = viewBoxW * viewScale
        val btnH = viewBoxH * viewScale
        val btnLeft = (width - btnW) / 2f
        val btnTop = (height - btnH) / 2f
        val density = viewScale

        val primaryBox = doc.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().firstOrNull()
        val isRootOval = primaryBox?.shapeType?.uppercase() == "OVAL"
        val rootCornerArc = (primaryBox?.cornerRadiusTopLeft ?: 14f) * density * 2f
        val rootClipShape: Shape = if (isRootOval) {
            Ellipse2D.Float(btnLeft, btnTop, btnW, btnH)
        } else {
            RoundRectangle2D.Float(btnLeft, btnTop, btnW, btnH, rootCornerArc, rootCornerArc)
        }

        val layersToRender = activeLayersOnly ?: doc.canvas.layers
        layersToRender.forEach { layer ->
            val gLayer = g2.create() as Graphics2D
            gLayer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            gLayer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            try {
                when (layer) {
                    is CanvasLayer.GlowRing -> {
                        val r = ((layer.glowColor shr 16) and 0xFF).toInt()
                        val g = ((layer.glowColor shr 8) and 0xFF).toInt()
                        val b = (layer.glowColor and 0xFF).toInt()
                        val btnRad = minOf(btnW, btnH) / 2f
                        val blurSpread = (layer.blurRadius * density).coerceAtLeast(8f)
                        val totalRadius = (btnRad + blurSpread).coerceAtLeast(10f)
                        val innerFrac = (btnRad / totalRadius).coerceIn(0.1f, 0.85f)
                        val fractions = floatArrayOf(0f, innerFrac, (innerFrac + (1f - innerFrac) * 0.5f).coerceAtMost(0.95f), 1f)
                        val colors = arrayOf(
                            Color(r, g, b, 90),
                            Color(r, g, b, 75),
                            Color(r, g, b, 25),
                            Color(r, g, b, 0)
                        )
                        gLayer.paint = RadialGradientPaint(width / 2f, height / 2f, totalRadius, fractions, colors)
                        gLayer.fillOval((width / 2f - totalRadius).toInt(), (height / 2f - totalRadius).toInt(), (totalRadius * 2).toInt(), (totalRadius * 2).toInt())
                    }
                    is CanvasLayer.BoxLayer -> {
                        val boxW = btnW * layer.widthRatio
                        val boxH = btnH * layer.heightRatio
                        val boxX = btnLeft + btnW * layer.offsetXRatio
                        val boxY = btnTop + btnH * layer.offsetYRatio
                        val tl = layer.cornerRadiusTopLeft * density
                        val tr = layer.cornerRadiusTopRight * density
                        val br = layer.cornerRadiusBottomRight * density
                        val bl = layer.cornerRadiusBottomLeft * density
                        val isOval = layer.shapeType.uppercase() == "OVAL"

                        val isRoot = layer == doc.canvas.layers.firstOrNull() || (layer.widthRatio >= 1.0f && layer.heightRatio >= 1.0f)
                        if (!isRoot && (layer.clipToBounds || doc.canvas.clipToBounds)) {
                            gLayer.clip(rootClipShape)
                        }

                        val pivotX = boxX + boxW * layer.originXRatio
                        val pivotY = boxY + boxH * layer.originYRatio
                        gLayer.translate(pivotX.toDouble(), pivotY.toDouble())
                        if (layer.rotationDegrees != 0f) gLayer.rotate(Math.toRadians(layer.rotationDegrees.toDouble()))
                        if (layer.scaleX != 1f || layer.scaleY != 1f) gLayer.scale(layer.scaleX.toDouble(), layer.scaleY.toDouble())
                        gLayer.translate(-pivotX.toDouble(), -pivotY.toDouble())

                        // Outset Shadows
                        layer.boxShadows.filter { !it.isInset }.forEach { shadow ->
                            val sp = shadow.spreadRadius * density
                            val sx = shadow.offsetX * density
                            val sy = shadow.offsetY * density
                            val alpha = (((shadow.color shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                            val c = Color(
                                ((shadow.color shr 16) and 0xFF).toInt(),
                                ((shadow.color shr 8) and 0xFF).toInt(),
                                (shadow.color and 0xFF).toInt(),
                                alpha
                            )
                            gLayer.color = c
                            val sShape = getBoxShape(boxX + sx - sp, boxY + sy - sp, boxW + sp * 2, boxH + sp * 2, tl + sp, tr + sp, br + sp, bl + sp, isOval)
                            gLayer.fill(sShape)
                        }

                        // Fills
                        val fills = if (layer.fills.isNotEmpty()) layer.fills else listOf(layer.fill)
                        fills.forEach { fill ->
                            when (fill) {
                                is FillBrush.Solid -> {
                                    val alpha = (((fill.color shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                                    gLayer.color = Color(
                                        ((fill.color shr 16) and 0xFF).toInt(),
                                        ((fill.color shr 8) and 0xFF).toInt(),
                                        (fill.color and 0xFF).toInt(),
                                        alpha
                                    )
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval)
                                    gLayer.fill(shape)
                                }
                                is FillBrush.LinearGradient -> {
                                    val angleRad = Math.toRadians((fill.angleDegrees - 90.0))
                                    val gcx = boxX + boxW / 2f
                                    val gcy = boxY + boxH / 2f
                                    val r = Math.hypot(boxW.toDouble(), boxH.toDouble()).toFloat() / 2f
                                    val cos = Math.cos(angleRad).toFloat()
                                    val sin = Math.sin(angleRad).toFloat()
                                    val x1 = gcx - cos * r
                                    val y1 = gcy - sin * r
                                    val x2 = gcx + cos * r
                                    val y2 = gcy + sin * r
                                    val fractions = fill.stops.takeIf { it.size == fill.colors.size && it.size >= 2 }?.toFloatArray()
                                        ?: FloatArray(fill.colors.size) { it.toFloat() / (fill.colors.size - 1).coerceAtLeast(1) }
                                    val colors = fill.colors.map { col ->
                                        val alpha = (((col shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                                        Color(
                                            ((col shr 16) and 0xFF).toInt(),
                                            ((col shr 8) and 0xFF).toInt(),
                                            (col and 0xFF).toInt(),
                                            alpha
                                        )
                                    }.toTypedArray()
                                    gLayer.paint = LinearGradientPaint(x1, y1, x2, y2, fractions, colors)
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval)
                                    gLayer.fill(shape)
                                }
                                is FillBrush.RadialGradient -> {
                                    val gcx = boxX + boxW * fill.centerXRatio
                                    val gcy = boxY + boxH * fill.centerYRatio
                                    val gradRad = (boxW * fill.radiusRatio).coerceAtLeast(1f)
                                    val fractions = fill.stops.takeIf { it.size == fill.colors.size && it.size >= 2 }?.toFloatArray()
                                        ?: FloatArray(fill.colors.size) { it.toFloat() / (fill.colors.size - 1).coerceAtLeast(1) }
                                    val colors = fill.colors.map { col ->
                                        val alpha = (((col shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                                        Color(
                                            ((col shr 16) and 0xFF).toInt(),
                                            ((col shr 8) and 0xFF).toInt(),
                                            (col and 0xFF).toInt(),
                                            alpha
                                        )
                                    }.toTypedArray()

                                    val xform = if (fill.aspectRatio != 1.0f && fill.aspectRatio > 0f) {
                                        AffineTransform().apply {
                                            translate(gcx.toDouble(), gcy.toDouble())
                                            scale(1.0, (1.0f / fill.aspectRatio).toDouble())
                                            translate(-gcx.toDouble(), -gcy.toDouble())
                                        }
                                    } else AffineTransform()

                                    gLayer.paint = RadialGradientPaint(Point2D.Float(gcx, gcy), gradRad, Point2D.Float(gcx, gcy), fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, xform)
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval)
                                    gLayer.fill(shape)
                                }
                                is FillBrush.SweepGradient -> {
                                    val gcx = boxX + boxW / 2f
                                    val gcy = boxY + boxH / 2f
                                    val colors = fill.colors.map { col ->
                                        val alpha = (((col shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                                        Color(
                                            ((col shr 16) and 0xFF).toInt(),
                                            ((col shr 8) and 0xFF).toInt(),
                                            (col and 0xFF).toInt(),
                                            alpha
                                        )
                                    }.toTypedArray()
                                    val fractions = fill.stops.takeIf { it.size == fill.colors.size && it.size >= 2 }?.toFloatArray()
                                        ?: FloatArray(fill.colors.size) { it.toFloat() / (fill.colors.size - 1).coerceAtLeast(1) }
                                    val startAngleRad = Math.toRadians((fill.startAngleDegrees).toDouble()).toFloat()
                                    gLayer.paint = ConicGradientPaint(gcx, gcy, startAngleRad, colors, fractions)
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval)
                                    gLayer.fill(shape)
                                }
                            }
                        }

                        // Inset Shadows
                        layer.boxShadows.filter { it.isInset }.forEach { shadow ->
                            val sp = shadow.spreadRadius * density
                            val sx = shadow.offsetX * density
                            val sy = shadow.offsetY * density
                            val alpha = (((shadow.color shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                            val c = Color(
                                ((shadow.color shr 16) and 0xFF).toInt(),
                                ((shadow.color shr 8) and 0xFF).toInt(),
                                (shadow.color and 0xFF).toInt(),
                                alpha
                            )
                            gLayer.color = c
                            val strokeW = (sp * 1.5f).coerceAtLeast(2f)
                            gLayer.stroke = BasicStroke(strokeW)
                            val shape = getBoxShape(boxX + sx + strokeW / 2, boxY + sy + strokeW / 2, boxW - strokeW, boxH - strokeW, tl, tr, br, bl, isOval)
                            gLayer.draw(shape)
                        }

                        // Border Stroke
                        layer.stroke?.let { stroke ->
                            val sAlpha = (((stroke.color shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                            gLayer.color = Color(
                                ((stroke.color shr 16) and 0xFF).toInt(),
                                ((stroke.color shr 8) and 0xFF).toInt(),
                                (stroke.color and 0xFF).toInt(),
                                sAlpha
                            )
                            val strokeW = stroke.width * density
                            val strokeObj = if (stroke.dashWidth > 0f && stroke.dashGap > 0f) {
                                BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, floatArrayOf(stroke.dashWidth * density, stroke.dashGap * density), 0f)
                            } else {
                                BasicStroke(strokeW)
                            }
                            gLayer.stroke = strokeObj
                            val shape = getBoxShape(boxX + strokeW / 2, boxY + strokeW / 2, boxW - strokeW, boxH - strokeW, (tl - strokeW / 2).coerceAtLeast(0f), (tr - strokeW / 2).coerceAtLeast(0f), (br - strokeW / 2).coerceAtLeast(0f), (bl - strokeW / 2).coerceAtLeast(0f), isOval)
                            gLayer.draw(shape)
                        }
                    }
                    is CanvasLayer.CenterGlyph -> {
                        val text = layer.text ?: doc.manifest.defaultControl
                        val fontSize = (layer.fontSizeSp * density * 0.95f).toInt().coerceAtLeast(14)
                        gLayer.font = Font("SansSerif", Font.BOLD, fontSize)
                        val fm = gLayer.fontMetrics
                        val tw = fm.stringWidth(text)
                        val th = fm.ascent

                        val gx = (btnLeft + btnW / 2f - tw / 2f) + (layer.offsetXRatio * btnW)
                        val gy = (btnTop + btnH / 2f + th / 2f - 2) + (layer.offsetYRatio * btnH)

                        val alpha = ((layer.textColor shr 24) and 0xFF).toInt().coerceIn(0, 255)
                        gLayer.color = Color(
                            ((layer.textColor shr 16) and 0xFF).toInt(),
                            ((layer.textColor shr 8) and 0xFF).toInt(),
                            (layer.textColor and 0xFF).toInt(),
                            alpha
                        )
                        gLayer.drawString(text, gx.toInt(), gy.toInt())
                    }
                    else -> {}
                }
            } finally {
                gLayer.dispose()
            }
        }

        g2.dispose()
        return img
    }

    private fun getBoxShape(x: Float, y: Float, w: Float, h: Float, tl: Float, tr: Float, br: Float, bl: Float, isOval: Boolean): Shape {
        return if (isOval) {
            Ellipse2D.Float(x, y, w, h)
        } else {
            val path = Path2D.Float()
            path.moveTo(x + tl, y)
            path.lineTo(x + w - tr, y)
            path.quadTo(x + w, y, x + w, y + tr)
            path.lineTo(x + w, y + h - br)
            path.quadTo(x + w, y + h, x + w - br, y + h)
            path.lineTo(x + bl, y + h)
            path.quadTo(x, y + h, x, y + h - bl)
            path.lineTo(x, y + tl)
            path.quadTo(x, y, x + tl, y)
            path.closePath()
            path
        }
    }

    /**
     * Generates a descriptive title for a layer for the audit grid.
     */
    fun describeLayer(index: Int, layer: CanvasLayer, doc: NxprcDocument): String {
        return when (layer) {
            is CanvasLayer.CenterGlyph -> "L$index: Letter '${layer.text ?: doc.manifest.defaultControl}'"
            is CanvasLayer.TextLayer -> "L$index: Label '${layer.text}'"
            is CanvasLayer.GlowRing -> "L$index: Outer Glow Ring"
            is CanvasLayer.InnerShadow -> "L$index: Inner Bevel Shadow"
            is CanvasLayer.GlossReflection -> "L$index: Specular Arc Reflection"
            is CanvasLayer.BezelSocket -> "L$index: Molded Bezel Socket"
            is CanvasLayer.GradientShape -> "L$index: Gradient Fill Shape"
            is CanvasLayer.BoxLayer -> {
                when {
                    index == 0 || (layer.widthRatio >= 0.95f && layer.heightRatio >= 0.95f) -> "L$index: Root Socket"
                    layer.fills.any { it is FillBrush.SweepGradient } -> "L$index: Molded Conic Ring"
                    index == 1 && layer.widthRatio >= 0.85f -> "L$index: Molded Socket Ring"
                    layer.widthRatio in 0.65f..0.85f && layer.boxShadows.size >= 3 -> "L$index: Primary Keycap"
                    layer.rotationDegrees != 0f && layer.heightRatio < 0.4f -> "L$index: Top Shell Gloss"
                    layer.offsetYRatio > 0.5f && layer.heightRatio < 0.3f -> "L$index: Lower Curved Shadow"
                    layer.widthRatio in 0.4f..0.6f && layer.boxShadows.any { it.isInset } -> "L$index: Light Core Diffusion"
                    layer.stroke != null && layer.widthRatio in 0.4f..0.6f -> "L$index: Inner Mounting Rim"
                    layer.widthRatio < 0.35f && layer.heightRatio < 0.15f -> "L$index: Specular Highlight"
                    else -> "L$index: BoxLayer (${(layer.widthRatio * 100).toInt()}%)"
                }
            }
            else -> "L$index: ${layer::class.simpleName}"
        }
    }

    private class ConicGradientPaint(
        private val cx: Float,
        private val cy: Float,
        private val startAngleRad: Float,
        private val colors: Array<Color>,
        private val fractions: FloatArray
    ) : Paint {
        override fun createContext(
            cm: ColorModel?,
            deviceBounds: Rectangle,
            userBounds: Rectangle2D,
            xform: AffineTransform,
            hints: RenderingHints
        ): PaintContext = ConicPaintContext(cx, cy, startAngleRad, colors, fractions, xform)

        override fun getTransparency(): Int = Transparency.TRANSLUCENT

        private class ConicPaintContext(
            private val cx: Float,
            private val cy: Float,
            private val startAngleRad: Float,
            private val colors: Array<Color>,
            private val fractions: FloatArray,
            private val xform: AffineTransform
        ) : PaintContext {
            private val invXform = try { xform.createInverse() } catch (_: Exception) { AffineTransform() }

            override fun dispose() {}
            override fun getColorModel(): ColorModel = ColorModel.getRGBdefault()

            override fun getRaster(x: Int, y: Int, w: Int, h: Int): Raster {
                val raster = getColorModel().createCompatibleWritableRaster(w, h)
                val data = IntArray(w * h)
                val ptSrc = Point2D.Float()
                val ptDst = Point2D.Float()
                val twoPi = (2.0 * Math.PI).toFloat()

                for (j in 0 until h) {
                    for (i in 0 until w) {
                        ptSrc.setLocation((x + i).toFloat(), (y + j).toFloat())
                        invXform.transform(ptSrc, ptDst)

                        val dx = ptDst.x - cx
                        val dy = ptDst.y - cy
                        var angle = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                        if (angle < 0f) angle += twoPi
                        var relAngle = angle - startAngleRad
                        while (relAngle < 0f) relAngle += twoPi
                        while (relAngle >= twoPi) relAngle -= twoPi
                        val fraction = relAngle / twoPi

                        var idx = 0
                        while (idx < fractions.size - 1 && fractions[idx + 1] < fraction) {
                            idx++
                        }
                        val c: Color = if (idx >= fractions.size - 1) {
                            colors.last()
                        } else {
                            val f0 = fractions[idx]
                            val f1 = fractions[idx + 1]
                            val t = if (f1 > f0) (fraction - f0) / (f1 - f0) else 0f
                            val c0 = colors[idx]
                            val c1 = colors[idx + 1]
                            Color(
                                (c0.red + t * (c1.red - c0.red)).toInt().coerceIn(0, 255),
                                (c0.green + t * (c1.green - c0.green)).toInt().coerceIn(0, 255),
                                (c0.blue + t * (c1.blue - c0.blue)).toInt().coerceIn(0, 255),
                                (c0.alpha + t * (c1.alpha - c0.alpha)).toInt().coerceIn(0, 255)
                            )
                        }
                        data[j * w + i] = (c.alpha shl 24) or (c.red shl 16) or (c.green shl 8) or c.blue
                    }
                }
                raster.setDataElements(0, 0, w, h, data)
                return raster
            }
        }
    }
}
