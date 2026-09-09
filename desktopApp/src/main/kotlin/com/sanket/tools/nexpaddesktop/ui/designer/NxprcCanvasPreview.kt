package com.sanket.tools.nexpaddesktop.ui.designer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.nxprc.*

@Composable
fun NxprcCanvasPreview(
    document: NxprcDocument,
    modifier: Modifier = Modifier,
    sizeDp: Int = 140
) {
    var isPressed by remember { mutableStateOf(false) }

    // Spring scale on touch press
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) document.animations.pressScale else 1f,
        animationSpec = spring(
            dampingRatio = document.animations.springDamping,
            stiffness = document.animations.springStiffness
        )
    )

    // Spring translateY on touch press
    val pressOffsetYAnim by animateFloatAsState(
        targetValue = if (isPressed) document.animations.pressOffsetY else 0f,
        animationSpec = spring(
            dampingRatio = document.animations.springDamping,
            stiffness = document.animations.springStiffness
        )
    )

    // Infinite transitions for idle animations
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(document.animations.idleDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .pointerInput(document.manifest.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            scale(scaleAnim) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f + pressOffsetYAnim * density)

                document.canvas.layers.forEach { layer ->
                    when (layer) {
                        is CanvasLayer.BoxLayer -> {
                            val pivot = Offset(size.width * layer.originXRatio, size.height * layer.originYRatio)
                            val rotAngle = if (layer.isRotating && document.animations.idleType == "ROTATE") rotateAngle else layer.rotationDegrees

                            withTransform({
                                translate(left = layer.offsetXRatio * size.width, top = layer.offsetYRatio * size.height)
                                rotate(rotAngle, pivot = pivot)
                                scale(scaleX = layer.scaleX, scaleY = layer.scaleY, pivot = pivot)
                            }) {
                                val baseRadius = size.minDimension / 2f
                                val shapeRadius = baseRadius * 0.90f
                                val layerAlpha = layer.opacity.coerceIn(0f, 1f)
                                val cornerRadiusPx = layer.cornerRadiusTopLeft * density

                                // 1. Outset box shadows
                                layer.boxShadows.filter { !it.isInset }.forEach { shadow ->
                                    val shadowOffset = Offset(shadow.offsetX * density, shadow.offsetY * density)
                                    val sColor = Color(shadow.color)
                                    if (layer.shapeType.uppercase() == "OVAL") {
                                        val shadowRadius = (shapeRadius + shadow.spreadRadius * density).coerceAtLeast(0f)
                                        drawCircle(
                                            color = sColor.copy(alpha = sColor.alpha * layerAlpha),
                                            radius = shadowRadius,
                                            center = Offset(centerOffset.x + shadowOffset.x, centerOffset.y + shadowOffset.y)
                                        )
                                    } else {
                                        val spreadPx = shadow.spreadRadius * density
                                        drawRoundRect(
                                            color = sColor.copy(alpha = sColor.alpha * layerAlpha),
                                            topLeft = Offset(centerOffset.x - shapeRadius + shadowOffset.x - spreadPx, centerOffset.y - shapeRadius + shadowOffset.y - spreadPx),
                                            size = Size((shapeRadius + spreadPx) * 2f, (shapeRadius + spreadPx) * 2f),
                                            cornerRadius = CornerRadius(cornerRadiusPx + spreadPx, cornerRadiusPx + spreadPx)
                                        )
                                    }
                                }
                                // 2. Main surface
                                val allBrushes = if (layer.fills.isNotEmpty()) {
                                    layer.fills.map { createBrush(it, size) }
                                } else {
                                    listOf(createBrush(layer.fill, size))
                                }

                                allBrushes.forEach { b ->
                                    if (layer.shapeType.uppercase() == "OVAL") {
                                        drawCircle(brush = b, radius = shapeRadius, center = centerOffset, alpha = layerAlpha)
                                    } else {
                                        drawRoundRect(
                                            brush = b,
                                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                            alpha = layerAlpha
                                        )
                                    }
                                }

                                layer.stroke?.let { st ->
                                    val stColor = Color(st.color)
                                    if (layer.shapeType.uppercase() == "OVAL") {
                                        drawCircle(
                                            color = stColor.copy(alpha = stColor.alpha * layerAlpha),
                                            radius = shapeRadius,
                                            center = centerOffset,
                                            style = Stroke(width = st.width * density)
                                        )
                                    } else {
                                        drawRoundRect(
                                            color = stColor.copy(alpha = stColor.alpha * layerAlpha),
                                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                            style = Stroke(width = st.width * density)
                                        )
                                    }
                                }
                                // 3. Inset box shadows (smooth clipped perimeter rim)
                                val insets = layer.boxShadows.filter { it.isInset }
                                if (insets.isNotEmpty()) {
                                    val shapeClipPath = Path().apply {
                                        if (layer.shapeType.uppercase() == "OVAL") {
                                            addOval(Rect(centerOffset.x - shapeRadius, centerOffset.y - shapeRadius, centerOffset.x + shapeRadius, centerOffset.y + shapeRadius))
                                        } else {
                                            addRoundRect(
                                                androidx.compose.ui.geometry.RoundRect(
                                                    left = centerOffset.x - shapeRadius,
                                                    top = centerOffset.y - shapeRadius,
                                                    right = centerOffset.x + shapeRadius,
                                                    bottom = centerOffset.y + shapeRadius,
                                                    radiusX = cornerRadiusPx,
                                                    radiusY = cornerRadiusPx
                                                )
                                            )
                                        }
                                    }
                                    clipPath(shapeClipPath) {
                                        insets.forEach { shadow ->
                                            val inColor = Color(shadow.color)
                                            val sOffset = Offset(shadow.offsetX * density, shadow.offsetY * density)
                                            val strokeW = (shadow.blurRadius.takeIf { it > 0f } ?: 3.5f) * density
                                            if (layer.shapeType.uppercase() == "OVAL") {
                                                drawCircle(
                                                    color = inColor.copy(alpha = inColor.alpha * layerAlpha),
                                                    radius = shapeRadius,
                                                    center = Offset(centerOffset.x + sOffset.x, centerOffset.y + sOffset.y),
                                                    style = Stroke(width = strokeW * 1.5f)
                                                )
                                            } else {
                                                drawRoundRect(
                                                    color = inColor.copy(alpha = inColor.alpha * layerAlpha),
                                                    topLeft = Offset(centerOffset.x - shapeRadius + sOffset.x, centerOffset.y - shapeRadius + sOffset.y),
                                                    size = Size(shapeRadius * 2f, shapeRadius * 2f),
                                                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                                    style = Stroke(width = strokeW * 1.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is CanvasLayer.BezelSocket -> {
                            val baseRadius = size.minDimension / 2f
                            // 1. Soft bottom drop shadow
                            drawCircle(
                                color = Color(layer.shadowColor),
                                radius = baseRadius * 0.96f,
                                center = Offset(centerOffset.x, centerOffset.y + baseRadius * 0.08f)
                            )
                            // 2. Solid bezel well
                            drawCircle(
                                color = Color(layer.outerBezelColor),
                                radius = baseRadius * 0.96f,
                                center = centerOffset
                            )
                            // 3. Bezel rim stroke
                            drawCircle(
                                color = Color(layer.outerBevelStroke),
                                radius = baseRadius * 0.96f,
                                center = centerOffset,
                                style = Stroke(width = 2.5f * density)
                            )
                        }
                        is CanvasLayer.GlowRing -> {
                            val alpha = if (layer.pulseEnabled && document.animations.idleType == "PULSE") pulseAlpha else 0.8f
                            val glowColor = Color(layer.glowColor).copy(alpha = alpha * 0.4f)
                            drawCircle(
                                color = glowColor,
                                radius = size.minDimension / 2f * 0.95f
                            )
                        }
                        is CanvasLayer.GradientShape -> {
                            val brush = createBrush(layer.fill, size)
                            val cornerRadiusPx = layer.cornerRadius * density
                            val shapeRadius = size.minDimension / 2f * 0.88f
                            val shapeAlpha = layer.opacity.coerceIn(0f, 1f)

                            val hasTransform = layer.rotationDegrees != 0f || layer.scaleX != 1f || layer.scaleY != 1f || layer.offsetXRatio != 0f || layer.offsetYRatio != 0f
                            val pivot = Offset(layer.originXRatio * size.width, layer.originYRatio * size.height)

                            val drawShape: () -> Unit = {
                                if (layer.shapeType.uppercase() == "OVAL") {
                                    drawCircle(
                                        brush = brush,
                                        radius = shapeRadius,
                                        center = centerOffset,
                                        alpha = shapeAlpha
                                    )
                                    layer.stroke?.let { st ->
                                        val stColor = Color(st.color)
                                        drawCircle(
                                            color = stColor.copy(alpha = stColor.alpha * shapeAlpha),
                                            radius = shapeRadius,
                                            center = centerOffset,
                                            style = Stroke(width = st.width * density)
                                        )
                                    }
                                } else {
                                    drawRoundRect(
                                        brush = brush,
                                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                        alpha = shapeAlpha
                                    )
                                    layer.stroke?.let { st ->
                                        val stColor = Color(st.color)
                                        drawRoundRect(
                                            color = stColor.copy(alpha = stColor.alpha * shapeAlpha),
                                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                            style = Stroke(width = st.width * density)
                                        )
                                    }
                                }
                            }

                            if (hasTransform) {
                                withTransform({
                                    translate(layer.offsetXRatio * size.width, layer.offsetYRatio * size.height)
                                    scale(layer.scaleX, layer.scaleY, pivot = pivot)
                                    rotate(layer.rotationDegrees, pivot = pivot)
                                }) {
                                    drawShape()
                                }
                            } else {
                                drawShape()
                            }
                        }
                        is CanvasLayer.InnerShadow -> {
                            val arcRadius = size.minDimension / 2f * 0.86f
                            val arcTopLeft = Offset(centerOffset.x - arcRadius, centerOffset.y - arcRadius)
                            val arcSize = Size(arcRadius * 2f, arcRadius * 2f)

                            // Top highlight rim
                            drawArc(
                                color = Color(layer.highlightColor),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = layer.strokeWidth * density)
                            )
                            // Bottom dark curved shadow
                            drawArc(
                                color = Color(layer.shadowColor),
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = layer.strokeWidth * density)
                            )
                        }
                        is CanvasLayer.GlossReflection -> {
                            val drawGloss: () -> Unit = {
                                val glossW = size.width * layer.widthRatio
                                val glossH = size.height * layer.heightRatio
                                val glossLeft = size.width * layer.offsetXRatio
                                val glossTop = size.height * layer.offsetYRatio
                                val glossCenter = Offset(glossLeft + glossW / 2f, glossTop + glossH / 2f)

                                rotate(layer.rotationDegrees, pivot = glossCenter) {
                                    val blurSpread = layer.blurRadius * density
                                    val effectiveRadius = (glossW / 2f + blurSpread).coerceAtLeast(1f)
                                    val glossBrush = Brush.radialGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.35f * layer.alpha),
                                            Color.White.copy(alpha = 0.12f * layer.alpha),
                                            Color.White.copy(alpha = 0.02f * layer.alpha),
                                            Color.Transparent
                                        ),
                                        center = glossCenter,
                                        radius = effectiveRadius
                                    )
                                    drawOval(
                                        brush = glossBrush,
                                        topLeft = Offset(glossLeft - blurSpread * 0.5f, glossTop - blurSpread * 0.5f),
                                        size = Size(glossW + blurSpread, glossH + blurSpread)
                                    )
                                }
                            }

                            if (document.canvas.clipToBounds) {
                                val clipPath = Path().apply {
                                    val r = size.minDimension / 2f * 0.88f
                                    val isOval = document.canvas.layers.filterIsInstance<CanvasLayer.GradientShape>().firstOrNull()?.shapeType?.uppercase() == "OVAL" ||
                                            document.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().firstOrNull()?.shapeType?.uppercase() == "OVAL"
                                    if (isOval) {
                                        addOval(Rect(centerOffset.x - r, centerOffset.y - r, centerOffset.x + r, centerOffset.y + r))
                                    } else {
                                        val cr = (document.canvas.layers.filterIsInstance<CanvasLayer.GradientShape>().firstOrNull()?.cornerRadius
                                            ?: document.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().firstOrNull()?.cornerRadiusTopLeft
                                            ?: 14f) * density
                                        addRoundRect(
                                            androidx.compose.ui.geometry.RoundRect(
                                                left = centerOffset.x - r,
                                                top = centerOffset.y - r,
                                                right = centerOffset.x + r,
                                                bottom = centerOffset.y + r,
                                                radiusX = cr,
                                                radiusY = cr
                                            )
                                        )
                                    }
                                }
                                clipPath(clipPath) {
                                    drawGloss()
                                }
                            } else {
                                drawGloss()
                            }
                        }
                        is CanvasLayer.VectorPath -> {
                            val brush = createBrush(layer.fill, size)
                            val angle = if (layer.isRotating && document.animations.idleType == "ROTATE") rotateAngle else layer.rotationDegrees
                            rotate(angle, pivot = centerOffset) {
                                drawCircle(
                                    brush = brush,
                                    radius = size.minDimension / 2f * 0.75f
                                )
                                layer.stroke?.let { st ->
                                    drawCircle(
                                        color = Color(st.color),
                                        radius = size.minDimension / 2f * 0.75f,
                                        style = Stroke(width = st.width * density)
                                    )
                                }
                            }
                        }
                        is CanvasLayer.CenterGlyph -> {
                            // Text glyph rendered as overlay Box below
                        }
                        is CanvasLayer.TextLayer -> {
                            // Text layer rendered as overlay Box below
                        }
                    }
                }
            }
        }

        // Center text glyph with embossed 3D lighting and tactile synchronization
        val glyph = document.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().firstOrNull()
        val textLayer = document.canvas.layers.filterIsInstance<CanvasLayer.TextLayer>().firstOrNull()
        val centerText = glyph?.text ?: textLayer?.text ?: document.manifest.defaultControl
        val textColor = glyph?.textColor ?: textLayer?.textColor ?: 0xFFF5F5F5L

        val viewBox = document.canvas.viewBoxWidth.coerceAtLeast(1f)
        val scaleFactor = sizeDp.toFloat() / viewBox
        val baseFontSp = glyph?.fontSizeSp ?: textLayer?.fontSizeSp ?: (viewBox * 0.32f)
        val fontSp = (baseFontSp * scaleFactor).sp
        val fontWeight = if (textLayer != null && textLayer.fontWeight >= 900) FontWeight.Black else FontWeight.Bold

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
                translationY = pressOffsetYAnim * density
            }
        ) {
            val extraShadows = glyph?.textShadows ?: textLayer?.textShadows ?: emptyList()
            if (extraShadows.isNotEmpty()) {
                extraShadows.forEach { ts ->
                    Text(
                        text = centerText,
                        color = Color(ts.color),
                        fontSize = fontSp,
                        fontWeight = fontWeight,
                        modifier = Modifier.offset(
                            x = (ts.offsetX * scaleFactor).dp,
                            y = (ts.offsetY * scaleFactor).dp
                        )
                    )
                }
            } else {
                glyph?.shadowColor?.let { sc ->
                    Text(
                        text = centerText,
                        color = Color(sc),
                        fontSize = fontSp,
                        fontWeight = fontWeight,
                        modifier = Modifier.offset(y = (glyph.shadowOffsetY * scaleFactor).dp)
                    )
                }
                glyph?.highlightColor?.let { hc ->
                    Text(
                        text = centerText,
                        color = Color(hc),
                        fontSize = fontSp,
                        fontWeight = fontWeight,
                        modifier = Modifier.offset(y = (-1f * scaleFactor).dp)
                    )
                }
            }
            // Foreground text
            Text(
                text = centerText,
                color = Color(textColor),
                fontSize = fontSp,
                fontWeight = fontWeight
            )
        }
    }
}

private fun createBrush(fill: FillBrush, size: Size): Brush {
    return when (fill) {
        is FillBrush.Solid -> SolidColor(Color(fill.color))
        is FillBrush.LinearGradient -> {
            if (fill.stops.isNotEmpty() && fill.stops.size == fill.colors.size) {
                val colorStops = fill.colors.indices.map { i ->
                    fill.stops[i] to Color(fill.colors[i])
                }.toTypedArray()
                Brush.linearGradient(colorStops = colorStops)
            } else {
                Brush.linearGradient(fill.colors.map { Color(it) })
            }
        }
        is FillBrush.RadialGradient -> {
            val cx = size.width * fill.centerXRatio
            val cy = size.height * fill.centerYRatio
            val maxR = size.minDimension * fill.radiusRatio * 1.6f
            if (fill.stops.isNotEmpty() && fill.stops.size == fill.colors.size) {
                val colorStops = fill.colors.indices.map { i ->
                    fill.stops[i] to Color(fill.colors[i])
                }.toTypedArray()
                Brush.radialGradient(
                    colorStops = colorStops,
                    center = Offset(cx, cy),
                    radius = maxR
                )
            } else {
                Brush.radialGradient(
                    colors = fill.colors.map { Color(it) },
                    center = Offset(cx, cy),
                    radius = maxR
                )
            }
        }
        is FillBrush.SweepGradient -> {
            val cx = size.width * fill.centerXRatio
            val cy = size.height * fill.centerYRatio
            Brush.sweepGradient(
                colors = fill.colors.map { Color(it) },
                center = Offset(cx, cy)
            )
        }
    }
}

private fun isDarkColor(color: Long): Boolean {
    val r = (color shr 16) and 0xFF
    val g = (color shr 8) and 0xFF
    val b = color and 0xFF
    val brightness = (r * 299 + g * 587 + b * 114) / 1000
    return brightness < 50
}
