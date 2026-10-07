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
    fun captureChromeScreenshot(html: String, width: Int = 400, height: Int = 400, scale: Float = 1.0f): BufferedImage? {
        val chromePath = findChromePath() ?: return null

        return try {
            val tempDir = File(System.getProperty("java.io.tmpdir"), "nexpad_audit").apply { mkdirs() }
            val htmlFile = File(tempDir, "preview_audit_${System.currentTimeMillis()}.html")
            val outFile = File(tempDir, "chrome_audit_${System.currentTimeMillis()}.png")

            val styledHtml = wrapHtmlForPreview(html, width, height, scale)
            htmlFile.writeText(styledHtml)

            val fileUrl = htmlFile.toURI().toString()
            val cmd = arrayOf(
                chromePath,
                "--headless=new",
                "--disable-gpu",
                "--window-size=$width,$height",
                "--screenshot=${outFile.absolutePath}",
                fileUrl
            )

            val proc = ProcessBuilder(*cmd).start()
            val completed = proc.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)
            if (!completed) {
                proc.destroyForcibly()
            }

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

    private fun wrapHtmlForPreview(html: String, width: Int, height: Int, scale: Float = 1.0f): String {
        val zoomStyle = if (scale != 1.0f) "zoom: ${scale};" else ""
        val bodyW = if (scale != 1.0f) (width / scale).toInt() else width
        val bodyH = if (scale != 1.0f) (height / scale).toInt() else height
        return if (html.contains("<body>", ignoreCase = true)) {
            html.replace(
                Regex("<body>", RegexOption.IGNORE_CASE),
                """<body style="margin:0; padding:0; background-color:#0B0E14; width:${bodyW}px; height:${bodyH}px; display:flex; align-items:center; justify-content:center; overflow:hidden; $zoomStyle">"""
            )
        } else {
            """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="margin:0; padding:0; background-color:#0B0E14; width:${bodyW}px; height:${bodyH}px; display:flex; align-items:center; justify-content:center; overflow:hidden; $zoomStyle">
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
        activeLayersOnly: List<CanvasLayer>? = null,
        isPressed: Boolean = false
    ): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = img.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        g2.color = Color(0x0B, 0x0E, 0x14)
        g2.fillRect(0, 0, width, height)

        if (isPressed) {
            val scale = doc.manifest.springPhysics.pressedScale
            val cx = width / 2.0
            val cy = height / 2.0
            g2.translate(cx, cy)
            g2.scale(scale.toDouble(), scale.toDouble())
            g2.translate(-cx, -cy)
        }

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
        val primaryGrad = doc.canvas.layers.filterIsInstance<CanvasLayer.GradientShape>().firstOrNull()
        val rootShapeType = primaryBox?.shapeType?.uppercase() ?: primaryGrad?.shapeType?.uppercase() ?: "ROUNDED_RECT"
        val isRootOval = rootShapeType == "OVAL"
        val rootCornerArc = (primaryBox?.cornerRadiusTopLeft ?: primaryGrad?.cornerRadius ?: 14f) * density * 2f
        val rootPathData = primaryBox?.pathData ?: ""
        val rootPolySides = primaryBox?.polygonSides ?: 0
        val rootEffectiveSides = when {
            rootPolySides >= 3 -> rootPolySides
            rootShapeType == "HEXAGON" -> 6
            rootShapeType == "OCTAGON" -> 8
            else -> 0
        }
        val rootClipShape: Shape = when {
            rootPathData.isNotBlank() -> skiaPathToAwtShape(rootPathData, btnLeft, btnTop, btnW, btnH) ?: parsePathDataToShape(rootPathData, btnLeft, btnTop, btnW, btnH)
            rootEffectiveSides >= 3 -> buildPolygonShape(rootEffectiveSides, btnLeft, btnTop, btnW, btnH)
            isRootOval -> Ellipse2D.Float(btnLeft, btnTop, btnW, btnH)
            else -> RoundRectangle2D.Float(btnLeft, btnTop, btnW, btnH, rootCornerArc, rootCornerArc)
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
                        val rawAlpha = (((layer.glowColor shr 24) and 0xFF).toFloat() / 255f).coerceIn(0.01f, 1f)
                        val btnRad = minOf(btnW, btnH) / 2f
                        val blurSpread = (layer.blurRadius * density).coerceAtLeast(8f)
                        val totalRadius = (btnRad + blurSpread).coerceAtLeast(10f)
                        val innerFrac = (btnRad / totalRadius).coerceIn(0.1f, 0.85f)
                        val fractions = floatArrayOf(0f, innerFrac, (innerFrac + (1f - innerFrac) * 0.5f).coerceAtMost(0.95f), 1f)
                        val a90 = (90 * rawAlpha).toInt().coerceIn(0, 255)
                        val a75 = (75 * rawAlpha).toInt().coerceIn(0, 255)
                        val a25 = (25 * rawAlpha).toInt().coerceIn(0, 255)
                        val colors = arrayOf(
                            Color(r, g, b, a90),
                            Color(r, g, b, a75),
                            Color(r, g, b, a25),
                            Color(r, g, b, 0)
                        )
                        val centerX = btnLeft + btnW / 2f
                        val centerY = btnTop + btnH / 2f
                        if (isRootOval) {
                            gLayer.paint = RadialGradientPaint(centerX, centerY, totalRadius, fractions, colors)
                            gLayer.fillOval((centerX - totalRadius).toInt(), (centerY - totalRadius).toInt(), (totalRadius * 2).toInt(), (totalRadius * 2).toInt())
                        } else {
                            val sImg = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                            val sg = sImg.createGraphics()
                            sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                            sg.color = Color(r, g, b, (120 * rawAlpha).toInt().coerceIn(0, 255))
                            val blurHalf = blurSpread * 0.4f
                            val sShape = getBoxShape(
                                btnLeft - blurHalf,
                                btnTop - blurHalf,
                                btnW + blurHalf * 2f,
                                btnH + blurHalf * 2f,
                                rootCornerArc / 2f + blurHalf,
                                rootCornerArc / 2f + blurHalf,
                                rootCornerArc / 2f + blurHalf,
                                rootCornerArc / 2f + blurHalf,
                                false,
                                rootPathData,
                                rootShapeType,
                                rootPolySides
                            )
                            sg.fill(sShape)
                            sg.dispose()
                            val blurred = gaussianBlurRgba(sImg, blurSpread / 2f)
                            gLayer.drawImage(blurred, 0, 0, null)
                        }
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

                        val elementShape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval, layer.pathData, layer.shapeType, layer.polygonSides)

                        // 1. Outset Shadows (drawn bottom-to-top per CSS spec)
                        val glowRing = doc.canvas.layers.filterIsInstance<CanvasLayer.GlowRing>().firstOrNull()
                        layer.boxShadows.filter { !it.isInset }.reversed().forEach { shadow ->
                            if (glowRing != null && shadow.color == glowRing.glowColor && shadow.blurRadius == glowRing.blurRadius) {
                                return@forEach
                            }
                            val sp = shadow.spreadRadius * density
                            val sx = shadow.offsetX * density
                            val sy = shadow.offsetY * density
                            val alpha = (((shadow.color shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                            if (alpha <= 0) return@forEach
                            val c = Color(
                                ((shadow.color shr 16) and 0xFF).toInt(),
                                ((shadow.color shr 8) and 0xFF).toInt(),
                                (shadow.color and 0xFF).toInt(),
                                alpha
                            )
                            val blur = shadow.blurRadius * density
                            val sShape = getBoxShape(boxX + sx - sp, boxY + sy - sp, boxW + sp * 2, boxH + sp * 2, tl + sp, tr + sp, br + sp, bl + sp, isOval, layer.pathData, layer.shapeType, layer.polygonSides)

                            if (blur > 0.5f) {
                                val sImg = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                                val sg = sImg.createGraphics()
                                sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                                sg.transform = gLayer.transform
                                sg.color = c
                                sg.fill(sShape)
                                sg.dispose()

                                val blurred = gaussianBlurRgba(sImg, blur / 2f)
                                val gOut = g2.create() as Graphics2D
                                try {
                                    val fullArea = Area(Rectangle(0, 0, width, height))
                                    fullArea.subtract(Area(gLayer.transform.createTransformedShape(elementShape)))
                                    gOut.clip(fullArea)
                                    gOut.drawImage(blurred, 0, 0, null)
                                } finally {
                                    gOut.dispose()
                                }
                            } else {
                                val gOut = gLayer.create() as Graphics2D
                                try {
                                    val fullArea = Area(Rectangle(0, 0, width, height))
                                    fullArea.subtract(Area(elementShape))
                                    gOut.clip(fullArea)
                                    gOut.color = c
                                    gOut.fill(sShape)
                                } finally {
                                    gOut.dispose()
                                }
                            }
                        }

                        // 2. Fills (stacked bottom-to-top per CSS painter's algorithm)
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
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval, layer.pathData, layer.shapeType, layer.polygonSides)
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
                                    val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, layer.opacity)
                                    gLayer.paint = LinearGradientPaint(x1, y1, x2, y2, fractions, colors)
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval, layer.pathData, layer.shapeType, layer.polygonSides)
                                    gLayer.fill(shape)
                                }
                                is FillBrush.RadialGradient -> {
                                    val gcx = boxX + boxW * fill.centerXRatio
                                    val gcy = boxY + boxH * fill.centerYRatio
                                    val radius = (Math.min(boxW, boxH) * fill.radiusRatio * 1.5f).coerceAtLeast(1f)
                                    val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, layer.opacity)

                                    val xform = if (fill.aspectRatio != 1.0f && fill.aspectRatio > 0f) {
                                        AffineTransform().apply {
                                            translate(gcx.toDouble(), gcy.toDouble())
                                            scale(1.0, (1.0f / fill.aspectRatio).toDouble())
                                            translate(-gcx.toDouble(), -gcy.toDouble())
                                        }
                                    } else AffineTransform()

                                    gLayer.paint = RadialGradientPaint(Point2D.Float(gcx, gcy), radius, Point2D.Float(gcx, gcy), fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, xform)
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval, layer.pathData, layer.shapeType, layer.polygonSides)
                                    gLayer.fill(shape)
                                }
                                is FillBrush.SweepGradient -> {
                                    val gcx = boxX + boxW * fill.centerXRatio
                                    val gcy = boxY + boxH * fill.centerYRatio
                                    val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, layer.opacity)
                                    val startAngleRad = Math.toRadians((fill.startAngleDegrees - 90.0)).toFloat()
                                    gLayer.paint = ConicGradientPaint(gcx, gcy, startAngleRad, colors, fractions)
                                    val shape = getBoxShape(boxX, boxY, boxW, boxH, tl, tr, br, bl, isOval, layer.pathData, layer.shapeType, layer.polygonSides)
                                    gLayer.fill(shape)
                                }
                            }
                        }

                        // 3. Border Stroke (supports dashed stroke & top-only specular arc)
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
                            } else if (stroke.isDashed) {
                                BasicStroke(strokeW, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(8f * density, 6f * density), 0f)
                            } else {
                                BasicStroke(strokeW)
                            }
                            gLayer.stroke = strokeObj
                            if (isOval && stroke.isTopOnly) {
                                gLayer.draw(Arc2D.Float(boxX, boxY, boxW, boxH, 0f, 180f, Arc2D.OPEN))
                            } else {
                                val shape = getBoxShape(boxX + strokeW / 2, boxY + strokeW / 2, boxW - strokeW, boxH - strokeW, (tl - strokeW / 2).coerceAtLeast(0f), (tr - strokeW / 2).coerceAtLeast(0f), (br - strokeW / 2).coerceAtLeast(0f), (bl - strokeW / 2).coerceAtLeast(0f), isOval, layer.pathData, layer.shapeType, layer.polygonSides)
                                gLayer.draw(shape)
                            }
                        }

                        // 4. Inset Shadows (with Gaussian blur & hole difference clipping)
                        val insets = layer.boxShadows.filter { it.isInset }
                        if (insets.isNotEmpty()) {
                            val elemTransformed = gLayer.transform.createTransformedShape(elementShape)

                            insets.forEach { shadow ->
                                val alpha = (((shadow.color shr 24) and 0xFF) * layer.opacity).toInt().coerceIn(0, 255)
                                if (alpha <= 0) return@forEach
                                val sc = Color(
                                    ((shadow.color shr 16) and 0xFF).toInt(),
                                    ((shadow.color shr 8) and 0xFF).toInt(),
                                    (shadow.color and 0xFF).toInt(),
                                    alpha
                                )
                                val blur = shadow.blurRadius * density
                                val sp = shadow.spreadRadius * density
                                val sx = shadow.offsetX * density
                                val sy = shadow.offsetY * density

                                val hLeft = boxX + sx + sp
                                val hTop = boxY + sy + sp
                                val hW = (boxW - sp * 2f).coerceAtLeast(0f)
                                val hH = (boxH - sp * 2f).coerceAtLeast(0f)
                                val hTl = (tl - sp).coerceAtLeast(0f)
                                val hTr = (tr - sp).coerceAtLeast(0f)
                                val hBr = (br - sp).coerceAtLeast(0f)
                                val hBl = (bl - sp).coerceAtLeast(0f)
                                val holeShape = getBoxShape(hLeft, hTop, hW, hH, hTl, hTr, hBr, hBl, isOval, layer.pathData, layer.shapeType, layer.polygonSides)

                                if (blur > 0.5f) {
                                    val sImg = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                                    val sg = sImg.createGraphics()
                                    sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                                    val outerMargin = blur * 3f + Math.abs(sx) + Math.abs(sy) + 32f
                                    val outerRect = Rectangle2D.Float(boxX - outerMargin, boxY - outerMargin, boxW + outerMargin * 2f, boxH + outerMargin * 2f)
                                    val maskArea = Area(outerRect)
                                    maskArea.subtract(Area(holeShape))
                                    sg.transform = gLayer.transform
                                    sg.color = sc
                                    sg.fill(maskArea)
                                    sg.dispose()

                                    val blurred = gaussianBlurRgba(sImg, blur / 2f)
                                    val gOut = g2.create() as Graphics2D
                                    try {
                                        gOut.clip(elemTransformed)
                                        gOut.drawImage(blurred, 0, 0, null)
                                    } finally {
                                        gOut.dispose()
                                    }
                                } else {
                                    val gOut = gLayer.create() as Graphics2D
                                    try {
                                        gOut.clip(elementShape)
                                        val outerMargin = 32f
                                        val outerRect = Rectangle2D.Float(boxX - outerMargin, boxY - outerMargin, boxW + outerMargin * 2f, boxH + outerMargin * 2f)
                                        val maskArea = Area(outerRect)
                                        maskArea.subtract(Area(holeShape))
                                        gOut.color = sc
                                        gOut.fill(maskArea)
                                    } finally {
                                        gOut.dispose()
                                    }
                                }
                            }
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
                    is CanvasLayer.TextLayer -> {
                        val fontScale = density
                        val fontSize = (layer.fontSizeSp * fontScale).toInt()
                        val fontStyle = if (layer.fontWeight >= 700) Font.BOLD else Font.PLAIN
                        gLayer.font = Font("SansSerif", fontStyle, fontSize)
                        val fontMetrics = gLayer.fontMetrics
                        val text = layer.text
                        val textW = fontMetrics.stringWidth(text)
                        val textH = fontMetrics.ascent - fontMetrics.descent
                        val tx = (btnLeft + btnW * 0.5f + layer.offsetXRatio * btnW - textW / 2f).toInt()
                        val ty = (btnTop + btnH * 0.5f + layer.offsetYRatio * btnH + textH / 2f).toInt()

                        layer.textShadows.forEach { ts ->
                            val alpha = ((ts.color shr 24) and 0xFF).toInt()
                            val sc = Color(
                                ((ts.color shr 16) and 0xFF).toInt(),
                                ((ts.color shr 8) and 0xFF).toInt(),
                                (ts.color and 0xFF).toInt(),
                                alpha
                            )
                            gLayer.color = sc
                            gLayer.drawString(text, (tx + ts.offsetX * fontScale).toInt(), (ty + ts.offsetY * fontScale).toInt())
                        }

                        val textColor = layer.textColor.toInt()
                        gLayer.color = Color(
                            (textColor shr 16) and 0xFF,
                            (textColor shr 8) and 0xFF,
                            textColor and 0xFF
                        )
                        gLayer.drawString(text, tx, ty)
                    }
                    is CanvasLayer.GradientShape -> {
                        val shapeW = btnW * layer.widthRatio
                        val shapeH = btnH * layer.heightRatio
                        val shapeLeft = btnLeft + btnW * layer.effectiveTransform.offsetXRatio
                        val shapeTop = btnTop + btnH * layer.effectiveTransform.offsetYRatio
                        val cornerRadius = layer.cornerRadius * density
                        val shapeType = layer.shapeType.uppercase()
                        val isOval = shapeType == "OVAL"
                        val shapeAlpha = layer.effectiveEffects.opacity.coerceIn(0f, 1f)

                        val pivotX = shapeLeft + shapeW * layer.effectiveTransform.originXRatio
                        val pivotY = shapeTop + shapeH * layer.effectiveTransform.originYRatio
                        gLayer.translate(pivotX.toDouble(), pivotY.toDouble())
                        if (layer.effectiveTransform.rotationDegrees != 0f) gLayer.rotate(Math.toRadians(layer.effectiveTransform.rotationDegrees.toDouble()))
                        if (layer.effectiveTransform.scaleX != 1f || layer.effectiveTransform.scaleY != 1f) gLayer.scale(layer.effectiveTransform.scaleX.toDouble(), layer.effectiveTransform.scaleY.toDouble())
                        gLayer.translate(-pivotX.toDouble(), -pivotY.toDouble())

                        val effectiveSides = when {
                            shapeType == "HEXAGON" -> 6
                            shapeType == "OCTAGON" -> 8
                            shapeType == "POLYGON" -> 6
                            else -> 0
                        }
                        val shape = when {
                            effectiveSides >= 3 -> buildPolygonShape(effectiveSides, shapeLeft, shapeTop, shapeW, shapeH)
                            isOval -> Ellipse2D.Float(shapeLeft, shapeTop, shapeW, shapeH)
                            else -> RoundRectangle2D.Float(shapeLeft, shapeTop, shapeW, shapeH, cornerRadius * 2f, cornerRadius * 2f)
                        }

                        // Fill
                        when (val fill = layer.fill) {
                            is FillBrush.Solid -> {
                                val alpha = (((fill.color shr 24) and 0xFF) * shapeAlpha).toInt().coerceIn(0, 255)
                                gLayer.color = Color(((fill.color shr 16) and 0xFF).toInt(), ((fill.color shr 8) and 0xFF).toInt(), (fill.color and 0xFF).toInt(), alpha)
                            }
                            is FillBrush.LinearGradient -> {
                                val angleRad = Math.toRadians((fill.angleDegrees - 90.0))
                                val gcx = shapeLeft + shapeW / 2f
                                val gcy = shapeTop + shapeH / 2f
                                val r = Math.hypot(shapeW.toDouble(), shapeH.toDouble()).toFloat() / 2f
                                val cos = Math.cos(angleRad).toFloat()
                                val sin = Math.sin(angleRad).toFloat()
                                val x1 = gcx - cos * r
                                val y1 = gcy - sin * r
                                val x2 = gcx + cos * r
                                val y2 = gcy + sin * r
                                val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, shapeAlpha)
                                gLayer.paint = LinearGradientPaint(x1, y1, x2, y2, fractions, colors)
                            }
                            is FillBrush.RadialGradient -> {
                                val gcx = shapeLeft + shapeW * fill.centerXRatio
                                val gcy = shapeTop + shapeH * fill.centerYRatio
                                val radius = (Math.min(shapeW, shapeH) * fill.radiusRatio * 1.5f).coerceAtLeast(1f)
                                val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, shapeAlpha)
                                gLayer.paint = RadialGradientPaint(gcx, gcy, radius, fractions, colors)
                            }
                            is FillBrush.SweepGradient -> {
                                val gcx = shapeLeft + shapeW * fill.centerXRatio
                                val gcy = shapeTop + shapeH * fill.centerYRatio
                                val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, shapeAlpha)
                                val startAngleRad = Math.toRadians((fill.startAngleDegrees - 90.0)).toFloat()
                                gLayer.paint = ConicGradientPaint(gcx, gcy, startAngleRad, colors, fractions)
                            }
                        }
                        gLayer.fill(shape)

                        // Stroke
                        layer.stroke?.let { st ->
                            val alpha = (((st.color shr 24) and 0xFF) * shapeAlpha).toInt().coerceIn(0, 255)
                            gLayer.color = Color(((st.color shr 16) and 0xFF).toInt(), ((st.color shr 8) and 0xFF).toInt(), (st.color and 0xFF).toInt(), alpha)
                            if (st.isDashed) {
                                gLayer.stroke = BasicStroke(st.width * density, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(8f * density, 6f * density), 0f)
                            } else {
                                gLayer.stroke = BasicStroke(st.width * density)
                            }
                            if (isOval && st.isTopOnly) {
                                gLayer.draw(Arc2D.Float(shapeLeft, shapeTop, shapeW, shapeH, 0f, 180f, Arc2D.OPEN))
                            } else {
                                gLayer.draw(shape)
                            }
                        }
                    }
                    is CanvasLayer.BezelSocket -> {
                        val baseRadius = minOf(btnW, btnH) / 2f
                        val cx = btnLeft + btnW / 2f
                        val cy = btnTop + btnH / 2f
                        // 1. Soft bottom drop shadow
                        val sAlpha = ((layer.shadowColor shr 24) and 0xFF).toInt()
                        gLayer.color = Color(((layer.shadowColor shr 16) and 0xFF).toInt(), ((layer.shadowColor shr 8) and 0xFF).toInt(), (layer.shadowColor and 0xFF).toInt(), sAlpha)
                        gLayer.fill(Ellipse2D.Float(cx - baseRadius * 0.98f, cy - baseRadius * 0.98f + baseRadius * 0.05f, baseRadius * 1.96f, baseRadius * 1.96f))
                        // 2. Solid bezel well
                        val bAlpha = ((layer.outerBezelColor shr 24) and 0xFF).toInt()
                        gLayer.color = Color(((layer.outerBezelColor shr 16) and 0xFF).toInt(), ((layer.outerBezelColor shr 8) and 0xFF).toInt(), (layer.outerBezelColor and 0xFF).toInt(), bAlpha)
                        gLayer.fill(Ellipse2D.Float(cx - baseRadius * 0.98f, cy - baseRadius * 0.98f, baseRadius * 1.96f, baseRadius * 1.96f))
                        // 3. Bezel rim stroke
                        val rAlpha = ((layer.outerBevelStroke shr 24) and 0xFF).toInt()
                        gLayer.color = Color(((layer.outerBevelStroke shr 16) and 0xFF).toInt(), ((layer.outerBevelStroke shr 8) and 0xFF).toInt(), (layer.outerBevelStroke and 0xFF).toInt(), rAlpha)
                        gLayer.stroke = BasicStroke(2f * density)
                        gLayer.draw(Ellipse2D.Float(cx - baseRadius * 0.98f, cy - baseRadius * 0.98f, baseRadius * 1.96f, baseRadius * 1.96f))
                    }
                    is CanvasLayer.InnerShadow -> {
                        val shadows = listOf(
                            Triple(layer.highlightColor, 0f, layer.strokeWidth * density * 0.6f),
                            Triple(layer.shadowColor, 0f, -layer.strokeWidth * density * 1.4f)
                        )
                        shadows.forEach { (colorLong, sx, sy) ->
                            val alpha = ((colorLong shr 24) and 0xFF).toInt()
                            if (alpha <= 0) return@forEach
                            val sc = Color(
                                ((colorLong shr 16) and 0xFF).toInt(),
                                ((colorLong shr 8) and 0xFF).toInt(),
                                (colorLong and 0xFF).toInt(),
                                alpha
                            )
                            val hLeft = btnLeft + sx
                            val hTop = btnTop + sy
                            val holeShape = if (isRootOval) {
                                Ellipse2D.Float(hLeft, hTop, btnW, btnH)
                            } else {
                                RoundRectangle2D.Float(hLeft, hTop, btnW, btnH, rootCornerArc, rootCornerArc)
                            }
                            val gOut = gLayer.create() as Graphics2D
                            try {
                                gOut.clip(rootClipShape)
                                val outerMargin = 32f
                                val outerRect = Rectangle2D.Float(btnLeft - outerMargin, btnTop - outerMargin, btnW + outerMargin * 2f, btnH + outerMargin * 2f)
                                val maskArea = Area(outerRect)
                                maskArea.subtract(Area(holeShape))
                                gOut.color = sc
                                gOut.fill(maskArea)
                            } finally {
                                gOut.dispose()
                            }
                        }
                    }
                    is CanvasLayer.GlossReflection -> {
                        val glossW = btnW * layer.widthRatio
                        val glossH = btnH * layer.heightRatio
                        val glossLeft = btnLeft + btnW * layer.offsetXRatio
                        val glossTop = btnTop + btnH * layer.offsetYRatio
                        val glossCx = glossLeft + glossW / 2f
                        val glossCy = glossTop + glossH / 2f
                        if (doc.canvas.clipToBounds) {
                            gLayer.clip(rootClipShape)
                        }

                        gLayer.translate(glossCx.toDouble(), glossCy.toDouble())
                        if (layer.rotationDegrees != 0f) gLayer.rotate(Math.toRadians(layer.rotationDegrees.toDouble()))
                        gLayer.translate(-glossCx.toDouble(), -glossCy.toDouble())

                        val blurSpread = layer.blurRadius * density
                        val radius = (glossW / 2f + blurSpread).coerceAtLeast(1f)
                        val colors = arrayOf(
                            Color(255, 255, 255, (255 * 0.55f * layer.alpha).toInt().coerceIn(0, 255)),
                            Color(255, 255, 255, (255 * 0.25f * layer.alpha).toInt().coerceIn(0, 255)),
                            Color(255, 255, 255, (255 * 0.05f * layer.alpha).toInt().coerceIn(0, 255)),
                            Color(255, 255, 255, 0)
                        )
                        val fractions = floatArrayOf(0f, 0.35f, 0.70f, 1.0f)
                        gLayer.paint = RadialGradientPaint(glossCx, glossCy, radius, fractions, colors)
                        gLayer.fill(Ellipse2D.Float(glossLeft - blurSpread * 0.5f, glossTop - blurSpread * 0.5f, glossW + blurSpread, glossH + blurSpread))
                    }
                    is CanvasLayer.VectorPath -> {
                        val svgBoxW = btnW * layer.scale
                        val svgBoxH = btnH * layer.scale
                        val svgBoxX = btnLeft + btnW * layer.offsetXRatio
                        val svgBoxY = btnTop + btnH * layer.offsetYRatio
                        val shape = skiaPathToAwtShape(layer.pathData, svgBoxX, svgBoxY, svgBoxW, svgBoxH)
                        if (shape != null) {
                            if (layer.rotationDegrees != 0f) {
                                val cx = svgBoxX + svgBoxW / 2.0
                                val cy = svgBoxY + svgBoxH / 2.0
                                gLayer.translate(cx, cy)
                                gLayer.rotate(Math.toRadians(layer.rotationDegrees.toDouble()))
                                gLayer.translate(-cx, -cy)
                            }
                            // Fill
                            when (val fill = layer.fill) {
                                is FillBrush.Solid -> {
                                    val alpha = ((fill.color shr 24) and 0xFF).toInt()
                                    if (alpha > 0) {
                                        gLayer.color = Color(((fill.color shr 16) and 0xFF).toInt(), ((fill.color shr 8) and 0xFF).toInt(), (fill.color and 0xFF).toInt(), alpha)
                                        gLayer.fill(shape)
                                    }
                                }
                                is FillBrush.LinearGradient -> {
                                    val angleRad = Math.toRadians((fill.angleDegrees - 90.0))
                                    val gcx = svgBoxX + svgBoxW / 2f
                                    val gcy = svgBoxY + svgBoxH / 2f
                                    val r = Math.hypot(svgBoxW.toDouble(), svgBoxH.toDouble()).toFloat() / 2f
                                    val cos = Math.cos(angleRad).toFloat()
                                    val sin = Math.sin(angleRad).toFloat()
                                    val x1 = gcx - cos * r
                                    val y1 = gcy - sin * r
                                    val x2 = gcx + cos * r
                                    val y2 = gcy + sin * r
                                    val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, 1.0f)
                                    gLayer.paint = LinearGradientPaint(x1, y1, x2, y2, fractions, colors)
                                    gLayer.fill(shape)
                                }
                                is FillBrush.RadialGradient -> {
                                    val gcx = svgBoxX + svgBoxW * fill.centerXRatio
                                    val gcy = svgBoxY + svgBoxH * fill.centerYRatio
                                    val gradRad = (svgBoxW * fill.radiusRatio).coerceAtLeast(1f)
                                    val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, 1.0f)
                                    gLayer.paint = RadialGradientPaint(gcx, gcy, gradRad, fractions, colors)
                                    gLayer.fill(shape)
                                }
                                is FillBrush.SweepGradient -> {
                                    val gcx = svgBoxX + svgBoxW / 2f
                                    val gcy = svgBoxY + svgBoxH / 2f
                                    val (fractions, colors) = sanitizeFractionsAndColors(fill.stops, fill.colors, 1.0f)
                                    val startAngleRad = Math.toRadians(fill.startAngleDegrees.toDouble()).toFloat()
                                    gLayer.paint = ConicGradientPaint(gcx, gcy, startAngleRad, colors, fractions)
                                    gLayer.fill(shape)
                                }
                            }

                            // Stroke
                            layer.stroke?.let { st ->
                                val alpha = ((st.color shr 24) and 0xFF).toInt()
                                if (alpha > 0) {
                                    gLayer.color = Color(((st.color shr 16) and 0xFF).toInt(), ((st.color shr 8) and 0xFF).toInt(), (st.color and 0xFF).toInt(), alpha)
                                    val strokeW = (st.width * density).coerceAtLeast(0.5f)
                                    gLayer.stroke = BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                                    gLayer.draw(shape)
                                }
                            }
                        }
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

    fun buildPolygonShape(sides: Int, x: Float, y: Float, w: Float, h: Float): Shape {
        val path = Path2D.Float()
        val cx = x + w / 2f
        val cy = y + h / 2f
        val rx = w / 2f
        val ry = h / 2f
        val angleStep = (2.0 * Math.PI / sides)
        val startAngle = -Math.PI / 2.0
        for (i in 0 until sides) {
            val a = startAngle + i * angleStep
            val px = (cx + rx * Math.cos(a)).toFloat()
            val py = (cy + ry * Math.sin(a)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.closePath()
        return path
    }

    fun skiaPathToAwtShape(svgData: String, boxX: Float, boxY: Float, boxW: Float, boxH: Float): Shape? {
        val trimmed = svgData.trim()
        if (trimmed.isBlank()) return null
        val skiaPath = try { org.jetbrains.skia.Path.makeFromSVGString(trimmed) } catch (_: Throwable) { null } ?: return null
        val bounds = skiaPath.bounds
        val isNormalized100 = bounds.left >= -5f && bounds.top >= -5f && bounds.right <= 105f && bounds.bottom <= 105f
        fun toScreenX(px: Float): Float = if (isNormalized100) boxX + (px / 100f) * boxW else boxX + ((px - bounds.left) / bounds.width.coerceAtLeast(0.001f)) * boxW
        fun toScreenY(py: Float): Float = if (isNormalized100) boxY + (py / 100f) * boxH else boxY + ((py - bounds.top) / bounds.height.coerceAtLeast(0.001f)) * boxH

        val path2d = Path2D.Float()
        val iter = skiaPath.iterator()
        while (iter.hasNext()) {
            val seg = iter.next() ?: continue
            when (seg.verb) {
                org.jetbrains.skia.PathVerb.MOVE -> {
                    val p0 = seg.p0
                    if (p0 != null) path2d.moveTo(toScreenX(p0.x), toScreenY(p0.y))
                }
                org.jetbrains.skia.PathVerb.LINE -> {
                    val p1 = seg.p1
                    if (p1 != null) path2d.lineTo(toScreenX(p1.x), toScreenY(p1.y))
                }
                org.jetbrains.skia.PathVerb.QUAD -> {
                    val p1 = seg.p1
                    val p2 = seg.p2
                    if (p1 != null && p2 != null) path2d.quadTo(toScreenX(p1.x), toScreenY(p1.y), toScreenX(p2.x), toScreenY(p2.y))
                }
                org.jetbrains.skia.PathVerb.CUBIC -> {
                    val p1 = seg.p1
                    val p2 = seg.p2
                    val p3 = seg.p3
                    if (p1 != null && p2 != null && p3 != null) path2d.curveTo(toScreenX(p1.x), toScreenY(p1.y), toScreenX(p2.x), toScreenY(p2.y), toScreenX(p3.x), toScreenY(p3.y))
                }
                org.jetbrains.skia.PathVerb.CLOSE -> path2d.closePath()
                else -> {}
            }
        }
        return path2d
    }

    fun getBoxShape(
        x: Float, y: Float, w: Float, h: Float,
        tl: Float, tr: Float, br: Float, bl: Float,
        isOval: Boolean,
        pathData: String = "",
        shapeType: String = "",
        polygonSides: Int = 0
    ): Shape {
        val st = shapeType.uppercase()
        val effectiveSides = when {
            polygonSides >= 3 -> polygonSides
            st == "HEXAGON" -> 6
            st == "OCTAGON" -> 8
            else -> 0
        }
        val baseShape: Shape = when {
            pathData.isNotBlank() -> skiaPathToAwtShape(pathData, x, y, w, h) ?: parsePathDataToShape(pathData, x, y, w, h)
            effectiveSides >= 3 -> buildPolygonShape(effectiveSides, x, y, w, h)
            isOval || st == "OVAL" -> Ellipse2D.Float(x, y, w, h)
            else -> {
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

        if (pathData.isNotBlank() && isOval) {
            val area = java.awt.geom.Area(baseShape)
            area.intersect(java.awt.geom.Area(Ellipse2D.Float(x, y, w, h)))
            return area
        } else if (pathData.isNotBlank() && (tl > 0f || tr > 0f || br > 0f || bl > 0f)) {
            val outerPath = Path2D.Float().apply {
                moveTo(x + tl, y)
                lineTo(x + w - tr, y)
                quadTo(x + w, y, x + w, y + tr)
                lineTo(x + w, y + h - br)
                quadTo(x + w, y + h, x + w - br, y + h)
                lineTo(x + bl, y + h)
                quadTo(x, y + h, x, y + h - bl)
                lineTo(x, y + tl)
                quadTo(x, y, x + tl, y)
                closePath()
            }
            val area = java.awt.geom.Area(baseShape)
            area.intersect(java.awt.geom.Area(outerPath))
            return area
        }

        return baseShape
    }

    fun parsePathDataToShape(pathData: String, boxX: Float, boxY: Float, boxW: Float, boxH: Float): Shape {
        val path = Path2D.Float()
        val tokens = pathData.trim().split(java.util.regex.Pattern.compile("[,\\s]+")).filter { it.isNotEmpty() }
        var i = 0
        var curX = 0f
        var curY = 0f

        var isNormalized = true
        for (idx in tokens.indices) {
            val f = tokens[idx].toFloatOrNull()
            if (f != null && (f < -1f || f > 105f)) {
                isNormalized = false
                break
            }
        }

        fun toScreenX(px: Float): Float = if (isNormalized) boxX + (px / 100f) * boxW else boxX + px
        fun toScreenY(py: Float): Float = if (isNormalized) boxY + (py / 100f) * boxH else boxY + py

        while (i < tokens.size) {
            val cmd = tokens[i].uppercase()
            when (cmd) {
                "M" -> {
                    if (i + 2 < tokens.size) {
                        val px = tokens[i + 1].toFloatOrNull() ?: 0f
                        val py = tokens[i + 2].toFloatOrNull() ?: 0f
                        curX = toScreenX(px)
                        curY = toScreenY(py)
                        path.moveTo(curX, curY)
                        i += 3
                    } else i++
                }
                "L" -> {
                    if (i + 2 < tokens.size) {
                        val px = tokens[i + 1].toFloatOrNull() ?: 0f
                        val py = tokens[i + 2].toFloatOrNull() ?: 0f
                        curX = toScreenX(px)
                        curY = toScreenY(py)
                        path.lineTo(curX, curY)
                        i += 3
                    } else i++
                }
                "C" -> {
                    if (i + 6 < tokens.size) {
                        val x1 = toScreenX(tokens[i + 1].toFloatOrNull() ?: 0f)
                        val y1 = toScreenY(tokens[i + 2].toFloatOrNull() ?: 0f)
                        val x2 = toScreenX(tokens[i + 3].toFloatOrNull() ?: 0f)
                        val y2 = toScreenY(tokens[i + 4].toFloatOrNull() ?: 0f)
                        curX = toScreenX(tokens[i + 5].toFloatOrNull() ?: 0f)
                        curY = toScreenY(tokens[i + 6].toFloatOrNull() ?: 0f)
                        path.curveTo(x1, y1, x2, y2, curX, curY)
                        i += 7
                    } else i++
                }
                "Q" -> {
                    if (i + 4 < tokens.size) {
                        val x1 = toScreenX(tokens[i + 1].toFloatOrNull() ?: 0f)
                        val y1 = toScreenY(tokens[i + 2].toFloatOrNull() ?: 0f)
                        curX = toScreenX(tokens[i + 3].toFloatOrNull() ?: 0f)
                        curY = toScreenY(tokens[i + 4].toFloatOrNull() ?: 0f)
                        path.quadTo(x1, y1, curX, curY)
                        i += 5
                    } else i++
                }
                "Z" -> {
                    path.closePath()
                    i++
                }
                else -> i++
            }
        }
        return path
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
    fun gaussianBlurRgba(src: BufferedImage, radius: Float): BufferedImage {
        val r = radius.toInt().coerceAtLeast(1)
        val w = src.width
        val h = src.height
        val srcPixels = IntArray(w * h)
        src.getRGB(0, 0, w, h, srcPixels, 0, w)
        val dstPixels = IntArray(w * h)

        fun boxBlurPass(input: IntArray, output: IntArray) {
            val temp = IntArray(w * h)
            val div = 2 * r + 1
            for (y in 0 until h) {
                var aSum = 0
                var rSum = 0
                var gSum = 0
                var bSum = 0
                val rowStart = y * w
                for (i in -r..r) {
                    val x = i.coerceIn(0, w - 1)
                    val c = input[rowStart + x]
                    aSum += (c ushr 24) and 0xFF
                    rSum += (c ushr 16) and 0xFF
                    gSum += (c ushr 8) and 0xFF
                    bSum += c and 0xFF
                }
                for (x in 0 until w) {
                    temp[rowStart + x] = ((aSum / div) shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val prevX = (x - r).coerceIn(0, w - 1)
                    val nextX = (x + r + 1).coerceIn(0, w - 1)
                    val cPrev = input[rowStart + prevX]
                    val cNext = input[rowStart + nextX]
                    aSum += ((cNext ushr 24) and 0xFF) - ((cPrev ushr 24) and 0xFF)
                    rSum += ((cNext ushr 16) and 0xFF) - ((cPrev ushr 16) and 0xFF)
                    gSum += ((cNext ushr 8) and 0xFF) - ((cPrev ushr 8) and 0xFF)
                    bSum += (cNext and 0xFF) - (cPrev and 0xFF)
                }
            }
            for (x in 0 until w) {
                var aSum = 0
                var rSum = 0
                var gSum = 0
                var bSum = 0
                for (i in -r..r) {
                    val y = i.coerceIn(0, h - 1)
                    val c = temp[y * w + x]
                    aSum += (c ushr 24) and 0xFF
                    rSum += (c ushr 16) and 0xFF
                    gSum += (c ushr 8) and 0xFF
                    bSum += c and 0xFF
                }
                for (y in 0 until h) {
                    output[y * w + x] = ((aSum / div) shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val prevY = (y - r).coerceIn(0, h - 1)
                    val nextY = (y + r + 1).coerceIn(0, h - 1)
                    val cPrev = temp[prevY * w + x]
                    val cNext = temp[nextY * w + x]
                    aSum += ((cNext ushr 24) and 0xFF) - ((cPrev ushr 24) and 0xFF)
                    rSum += ((cNext ushr 16) and 0xFF) - ((cPrev ushr 16) and 0xFF)
                    gSum += ((cNext ushr 8) and 0xFF) - ((cPrev ushr 8) and 0xFF)
                    bSum += (cNext and 0xFF) - (cPrev and 0xFF)
                }
            }
        }

        val intermediate = IntArray(w * h)
        boxBlurPass(srcPixels, intermediate)
        boxBlurPass(intermediate, dstPixels)

        val res = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        res.setRGB(0, 0, w, h, dstPixels, 0, w)
        return res
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

    /**
     * Sanitizes stops and colors for Java AWT LinearGradientPaint and RadialGradientPaint:
     * 1. Guarantees stops starts with 0.0f
     * 2. Guarantees stops ends with 1.0f
     * 3. Guarantees strictly increasing order (fractions[i] > fractions[i-1]) so
     *    IllegalArgumentException: Keyframe fractions must be increasing is never thrown.
     */
    private fun sanitizeFractionsAndColors(
        rawStops: List<Float>?,
        rawColors: List<Long>,
        layerOpacity: Float
    ): Pair<FloatArray, Array<Color>> {
        if (rawColors.isEmpty()) {
            return Pair(floatArrayOf(0f, 1f), arrayOf(Color(0, 0, 0, 0), Color(0, 0, 0, 0)))
        }

        val colorsList = rawColors.map { col ->
            val alpha = (((col shr 24) and 0xFF) * layerOpacity).toInt().coerceIn(0, 255)
            Color(
                ((col shr 16) and 0xFF).toInt(),
                ((col shr 8) and 0xFF).toInt(),
                (col and 0xFF).toInt(),
                alpha
            )
        }.toMutableList()

        val stopsList = (rawStops?.takeIf { it.size == rawColors.size && it.size >= 2 }
            ?: List(rawColors.size) { it.toFloat() / (rawColors.size - 1).coerceAtLeast(1) }).toMutableList()

        // 1. Ensure starts at 0f (required by java.awt.MultipleGradientPaint)
        if (stopsList.first() > 0.0001f) {
            stopsList.add(0, 0f)
            colorsList.add(0, colorsList.first())
        } else {
            stopsList[0] = 0f
        }

        // 2. Ensure ends at 1f (required by java.awt.MultipleGradientPaint)
        if (stopsList.last() < 0.9999f) {
            stopsList.add(1f)
            colorsList.add(colorsList.last())
        } else {
            stopsList[stopsList.lastIndex] = 1f
        }

        // 3. Ensure strictly increasing (required by java.awt.MultipleGradientPaint: fractions[i] > fractions[i-1])
        val epsilon = 0.0002f
        for (i in 1 until stopsList.size) {
            if (stopsList[i] <= stopsList[i - 1]) {
                stopsList[i] = (stopsList[i - 1] + epsilon).coerceAtMost(1f)
            }
        }
        for (i in stopsList.size - 2 downTo 0) {
            if (stopsList[i] >= stopsList[i + 1]) {
                stopsList[i] = (stopsList[i + 1] - epsilon).coerceAtLeast(0f)
            }
        }

        return Pair(stopsList.toFloatArray(), colorsList.toTypedArray())
    }
}
