package com.sanket.tools.nexpaddesktop.ui.designer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
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
import org.jetbrains.skia.ClipMode as SkClipMode
import org.jetbrains.skia.FilterBlurMode as SkFilterBlurMode
import org.jetbrains.skia.MaskFilter as SkMaskFilter
import org.jetbrains.skia.Paint as SkPaint
import org.jetbrains.skia.RRect as SkRRect
import org.jetbrains.skia.Rect as SkRect

/**
 * Professional 3-zone gaming speed-to-distance transfer function.
 * Calibrated Sweet Spots:
 * - Noise Gate: < 8 dp/s -> 0.0 (anti-jitter)
 * - Precision Zone: 8..120 dp/s -> 0.16..0.25 (anti-deadzone floor)
 * - Linear Tracking Zone: 120..400 dp/s -> 0.25..0.50 (GOLDEN SWEET SPOT: 400 dp/s = 0.50 half deflection)
 * - Exponential Acceleration Zone: 400..1200 dp/s -> 0.50..1.00 (Smooth flick curve to full 1.00 deflection)
 * - Saturation Zone: > 1200 dp/s -> 1.00 (clamped maximum)
 */
internal fun calculateGamingStickMagnitude(speedDpPerSec: Float, sensitivity: Float = 1.0f): Float {
    val effSpeed = speedDpPerSec * sensitivity.coerceAtLeast(0.1f)
    return when {
        effSpeed < 8f -> 0f
        effSpeed <= 120f -> 0.16f + 0.09f * ((effSpeed - 8f) / 112f)
        effSpeed <= 400f -> 0.25f + 0.25f * ((effSpeed - 120f) / 280f)
        effSpeed <= 1200f -> {
            val norm = (effSpeed - 400f) / 800f
            0.50f + 0.50f * norm.toDouble().pow(1.35).toFloat()
        }
        else -> 1.0f
    }
}

private fun createNxprcColorFilter(filter: FilterDef): ColorFilter? {
    val brightness = filter.brightness.coerceAtLeast(0f)
    val saturation = filter.saturation.coerceAtLeast(0f)
    if (brightness == 1f && saturation == 1f) return null

    val inv = 1f - saturation
    val r = 0.213f * inv
    val g = 0.715f * inv
    val b = 0.072f * inv
    return ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        brightness * (r + saturation), brightness * g, brightness * b, 0f, 0f,
        brightness * r, brightness * (g + saturation), brightness * b, 0f, 0f,
        brightness * r, brightness * g, brightness * (b + saturation), 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )))
}

private fun createHueRotateColorMatrix(degrees: Float): FloatArray {
    val rad = (degrees % 360f) * (Math.PI / 180.0).toFloat()
    val cosVal = kotlin.math.cos(rad)
    val sinVal = kotlin.math.sin(rad)

    val lumR = 0.213f
    val lumG = 0.715f
    val lumB = 0.072f

    val m00 = lumR + cosVal * (1.0f - lumR) + sinVal * (-lumR)
    val m01 = lumG + cosVal * (-lumG) + sinVal * (-lumG)
    val m02 = lumB + cosVal * (-lumB) + sinVal * (1.0f - lumB)

    val m10 = lumR + cosVal * (-lumR) + sinVal * (0.143f)
    val m11 = lumG + cosVal * (1.0f - lumG) + sinVal * (0.140f)
    val m12 = lumB + cosVal * (-lumB) + sinVal * (-0.283f)

    val m20 = lumR + cosVal * (-lumR) + sinVal * (-(1.0f - lumR))
    val m21 = lumG + cosVal * (-lumG) + sinVal * (lumG)
    val m22 = lumB + cosVal * (1.0f - lumB) + sinVal * (lumB)

    return floatArrayOf(
        m00, m01, m02, 0f, 0f,
        m10, m11, m12, 0f, 0f,
        m20, m21, m22, 0f, 0f,
        0f,  0f,  0f,  1f, 0f
    )
}

internal fun evaluateAnimationTrack(track: AnimationTrack, progress: Float): Float {
    if (track.keyframes.isEmpty()) return 0f
    if (track.keyframes.size == 1) return track.keyframes[0].value

    val p = progress.coerceIn(0f, 1f)
    val sorted = track.keyframes

    val afterIdx = sorted.indexOfFirst { it.fraction >= p }
    if (afterIdx <= 0) {
        return if (afterIdx == 0) sorted[0].value else sorted.last().value
    }

    val before = sorted[afterIdx - 1]
    val after = sorted[afterIdx]

    val span = after.fraction - before.fraction
    if (span <= 0.00001f) return before.value

    val localFraction = ((p - before.fraction) / span).coerceIn(0f, 1f)

    val easedT = when (track.easing.uppercase()) {
        "EASE_IN_OUT" -> localFraction * localFraction * (3f - 2f * localFraction)
        "EASE_IN" -> localFraction * localFraction
        "EASE_OUT" -> localFraction * (2f - localFraction)
        else -> localFraction
    }

    return before.value + (after.value - before.value) * easedT
}

@Composable
fun NxprcCanvasPreview(
    document: NxprcDocument,
    modifier: Modifier = Modifier,
    sizeDp: Int = 140,
    activeLayersOnly: List<CanvasLayer>? = null,
    backgroundColor: Color = Color.Transparent,
    onStickDeflection: ((Float, Float) -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val isStick = document.manifest.category.equals("JOYSTICK", ignoreCase = true) ||
            document.manifest.category.equals("TOUCHPAD", ignoreCase = true) ||
            document.manifest.defaultControl.uppercase() in listOf(
                com.sanket.tools.nexpad.category.ControlKey.LS.key,
                com.sanket.tools.nexpad.category.ControlKey.RS.key,
                com.sanket.tools.nexpad.category.ControlKey.LTP.key,
                com.sanket.tools.nexpad.category.ControlKey.RTP.key
            )
    val isCameraMode = document.manifest.category.equals("TOUCHPAD", ignoreCase = true) ||
            document.manifest.defaultControl.equals(com.sanket.tools.nexpad.category.ControlKey.RTP.key, ignoreCase = true) ||
            document.manifest.defaultControl.equals(com.sanket.tools.nexpad.category.ControlKey.LTP.key, ignoreCase = true) ||
            document.manifest.defaultControl.equals(com.sanket.tools.nexpad.category.ControlKey.RS.key, ignoreCase = true)
    val isTouchpad = document.manifest.category.equals("TOUCHPAD", ignoreCase = true) ||
            document.manifest.defaultControl.equals(com.sanket.tools.nexpad.category.ControlKey.LTP.key, ignoreCase = true) ||
            document.manifest.defaultControl.equals(com.sanket.tools.nexpad.category.ControlKey.RTP.key, ignoreCase = true)

    val thumbOffsetX = remember { Animatable(0f) }
    val thumbOffsetY = remember { Animatable(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current.density

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

    // Infinite transitions for idle animations — only active when needed
    val needsPulse = document.animations.idleType == "PULSE"
    val needsRotation = document.animations.idleType == "ROTATE"
    val needsRgbCycle = document.animations.idleType == "RGB_CYCLE"
    val infiniteTransition = rememberInfiniteTransition()

    val pulseAlpha = if (needsPulse) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(document.animations.idleDurationMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    } else 0.8f

    val rotateAngle = if (needsRotation) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ).value
    } else 0f

    val rgbHueAngle = if (needsRgbCycle) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(document.animations.idleDurationMs.coerceAtLeast(1000), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ).value
    } else 0f

    // Dynamic Universal Timeline Track Sampling
    val hasDynamicTracks = document.animations.tracks.isNotEmpty()
    val trackDurationMs = document.animations.tracks.firstOrNull()?.durationMs ?: document.animations.idleDurationMs

    val timelineProgress = if (hasDynamicTracks) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(trackDurationMs.coerceAtLeast(200), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ).value
    } else 0f

    val trackScale = if (hasDynamicTracks) {
        document.animations.tracks.firstOrNull { it.property == AnimatedProperty.SCALE }
            ?.let { evaluateAnimationTrack(it, timelineProgress) } ?: 1f
    } else 1f

    val trackRotation = if (hasDynamicTracks) {
        document.animations.tracks.firstOrNull { it.property == AnimatedProperty.ROTATION }
            ?.let { evaluateAnimationTrack(it, timelineProgress) } ?: 0f
    } else 0f

    val trackOpacity = if (hasDynamicTracks) {
        document.animations.tracks.firstOrNull { it.property == AnimatedProperty.OPACITY }
            ?.let { evaluateAnimationTrack(it, timelineProgress) } ?: 1f
    } else 1f

    val trackTranslateX = if (hasDynamicTracks) {
        document.animations.tracks.firstOrNull { it.property == AnimatedProperty.TRANSLATE_X }
            ?.let { evaluateAnimationTrack(it, timelineProgress) } ?: 0f
    } else 0f

    val trackTranslateY = if (hasDynamicTracks) {
        document.animations.tracks.firstOrNull { it.property == AnimatedProperty.TRANSLATE_Y }
            ?.let { evaluateAnimationTrack(it, timelineProgress) } ?: 0f
    } else 0f

    val trackHueAngle = if (hasDynamicTracks) {
        document.animations.tracks.firstOrNull { it.property == AnimatedProperty.HUE_ROTATE }
            ?.let { evaluateAnimationTrack(it, timelineProgress) } ?: 0f
    } else 0f

    val activeHue = if (hasDynamicTracks && trackHueAngle != 0f) {
        trackHueAngle
    } else if (needsRgbCycle) {
        rgbHueAngle
    } else 0f

    val rgbFilter = if (activeHue != 0f) {
        ColorFilter.colorMatrix(ColorMatrix(createHueRotateColorMatrix(activeHue)))
    } else null

    val gestureModifier = if (isStick) {
        Modifier.pointerInput(document.manifest.id, isCameraMode, density) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val maxRadius = sizeDp * 0.28f * density
                val deadzoneRadius = 4f * density
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val startTime = System.currentTimeMillis()
                var previousTouchX = down.position.x
                var previousTouchY = down.position.y
                var previousTimeMs = startTime
                var currentStickX = 0f
                var currentStickY = 0f
                var lastSpeed = 0f
                var decayJob: kotlinx.coroutines.Job? = null

                if (isCameraMode) {
                    onStickDeflection?.invoke(0f, 0f)
                } else {
                    fun updateDeflection(pos: Offset) {
                        val vecX = pos.x - centerX
                        val vecY = pos.y - centerY
                        val dist = hypot(vecX, vecY)
                        val (clampedX, clampedY) = if (dist > maxRadius) {
                            val angle = atan2(vecY, vecX)
                            Pair(cos(angle) * maxRadius, sin(angle) * maxRadius)
                        } else {
                            Pair(vecX, vecY)
                        }
                        coroutineScope.launch {
                            thumbOffsetX.snapTo(clampedX)
                            thumbOffsetY.snapTo(clampedY)
                        }
                        val normX = if (dist < deadzoneRadius) 0f else (clampedX / maxRadius).coerceIn(-1f, 1f)
                        val normY = if (dist < deadzoneRadius) 0f else (-clampedY / maxRadius).coerceIn(-1f, 1f)
                        onStickDeflection?.invoke(normX, normY)
                    }
                    updateDeflection(down.position)
                }

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) break
                    change.consume()

                    val currentTouchX = change.position.x
                    val currentTouchY = change.position.y
                    val deltaX = currentTouchX - previousTouchX
                    val deltaY = currentTouchY - previousTouchY
                    previousTouchX = currentTouchX
                    previousTouchY = currentTouchY

                    if (isCameraMode) {
                        var finalDeltaX = deltaX
                        var finalDeltaY = deltaY
                        val absX = kotlin.math.abs(deltaX)
                        val absY = kotlin.math.abs(deltaY)
                        if (absX > 3.0f * absY) {
                            finalDeltaY *= 0.5f // Suppress vertical wobble during horizontal turns
                        } else if (absY > 3.0f * absX) {
                            finalDeltaX *= 0.5f // Suppress horizontal wobble during vertical looks
                        }

                        val distPx = hypot(finalDeltaX, finalDeltaY)
                        val distDp = distPx / density

                        if (distDp > 0.15f) {
                            val currentTimeMs = System.currentTimeMillis()
                            val dtSec = ((currentTimeMs - previousTimeMs).coerceAtLeast(1L)) / 1000f
                            previousTimeMs = currentTimeMs
                            val speedDpPerSec = distDp / dtSec
                            lastSpeed = speedDpPerSec

                            val stickMagnitude = calculateGamingStickMagnitude(speedDpPerSec, 1.0f)

                            if (stickMagnitude > 0f) {
                                val dirX = finalDeltaX / distPx
                                val dirY = finalDeltaY / distPx

                                val targetStickX = (dirX * stickMagnitude).coerceIn(-1f, 1f)
                                val targetStickY = (-dirY * stickMagnitude).coerceIn(-1f, 1f)

                                currentStickX = 0.70f * targetStickX + 0.30f * currentStickX
                                currentStickY = 0.70f * targetStickY + 0.30f * currentStickY

                                onStickDeflection?.invoke(currentStickX, currentStickY)

                                // Visual knob deflection actively moves down when dragging down
                                val clampedX = (currentStickX * maxRadius).coerceIn(-maxRadius, maxRadius)
                                val clampedY = (-currentStickY * maxRadius).coerceIn(-maxRadius, maxRadius)
                                coroutineScope.launch {
                                    thumbOffsetX.snapTo(clampedX)
                                    thumbOffsetY.snapTo(clampedY)
                                }

                                decayJob?.cancel()
                                decayJob = coroutineScope.launch {
                                    kotlinx.coroutines.delay(40)
                                    currentStickX *= 0.3f
                                    currentStickY *= 0.3f
                                    onStickDeflection?.invoke(currentStickX, currentStickY)
                                    launch { thumbOffsetX.snapTo(currentStickX * maxRadius) }
                                    launch { thumbOffsetY.snapTo(-currentStickY * maxRadius) }
                                    kotlinx.coroutines.delay(30)
                                    currentStickX = 0f
                                    currentStickY = 0f
                                    onStickDeflection?.invoke(0f, 0f)
                                    launch { thumbOffsetX.animateTo(0f, spring(stiffness = 1000f, dampingRatio = 0.65f)) }
                                    launch { thumbOffsetY.animateTo(0f, spring(stiffness = 1000f, dampingRatio = 0.65f)) }
                                    lastSpeed = 0f
                                }
                            }
                        }
                    } else {
                        val vecX = change.position.x - centerX
                        val vecY = change.position.y - centerY
                        val dist = hypot(vecX, vecY)
                        val (clampedX, clampedY) = if (dist > maxRadius) {
                            val angle = atan2(vecY, vecX)
                            Pair(cos(angle) * maxRadius, sin(angle) * maxRadius)
                        } else {
                            Pair(vecX, vecY)
                        }
                        coroutineScope.launch {
                            thumbOffsetX.snapTo(clampedX)
                            thumbOffsetY.snapTo(clampedY)
                        }
                        val normX = if (dist < deadzoneRadius) 0f else (clampedX / maxRadius).coerceIn(-1f, 1f)
                        val normY = if (dist < deadzoneRadius) 0f else (-clampedY / maxRadius).coerceIn(-1f, 1f)
                        onStickDeflection?.invoke(normX, normY)
                    }
                }
                isPressed = false
                decayJob?.cancel()
                if (isCameraMode && lastSpeed > 400f) {
                    val coastSteps = ((lastSpeed / 200f).toInt()).coerceIn(3, 7)
                    coroutineScope.launch {
                        var coastX = currentStickX
                        var coastY = currentStickY
                        repeat(coastSteps) {
                            coastX *= 0.68f
                            coastY *= 0.68f
                            onStickDeflection?.invoke(coastX, coastY)
                            launch { thumbOffsetX.snapTo(coastX * maxRadius) }
                            launch { thumbOffsetY.snapTo(-coastY * maxRadius) }
                            kotlinx.coroutines.delay(16)
                        }
                        launch { thumbOffsetX.animateTo(0f, spring(stiffness = document.animations.joystickSpringTension, dampingRatio = 0.65f)) }
                        launch { thumbOffsetY.animateTo(0f, spring(stiffness = document.animations.joystickSpringTension, dampingRatio = 0.65f)) }
                        onStickDeflection?.invoke(0f, 0f)
                    }
                } else {
                    coroutineScope.launch {
                        launch { thumbOffsetX.animateTo(0f, spring(stiffness = document.animations.joystickSpringTension, dampingRatio = 0.65f)) }
                        launch { thumbOffsetY.animateTo(0f, spring(stiffness = document.animations.joystickSpringTension, dampingRatio = 0.65f)) }
                    }
                    onStickDeflection?.invoke(0f, 0f)
                }
            }
        }
    } else {
        Modifier.pointerInput(document.manifest.id) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
    }

    val isTwoStageStick = isStick && !isTouchpad && document.canvas.capLayerIndices.isNotEmpty()
    val capIndicesSet = remember(document) { if (isTouchpad) emptySet() else document.canvas.capLayerIndices.toSet() }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .then(gestureModifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .graphicsLayer {
                    val finalScale = scaleAnim * trackScale
                    scaleX = if (isTwoStageStick) 1f else finalScale
                    scaleY = if (isTwoStageStick) 1f else finalScale
                    rotationZ = if (hasDynamicTracks) trackRotation else 0f
                    alpha = if (hasDynamicTracks) trackOpacity.coerceIn(0f, 1f) else 1f
                    translationX = (if (isStick && !isTwoStageStick && !isTouchpad) thumbOffsetX.value else 0f) + (trackTranslateX * density)
                    translationY = (if (isStick && !isTwoStageStick && !isTouchpad) thumbOffsetY.value else 0f) + (pressOffsetYAnim + trackTranslateY) * density
                    if (rgbFilter != null) {
                        colorFilter = rgbFilter
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(sizeDp.dp).background(backgroundColor)) {
                // Render the document viewBox at its real aspect ratio. The
                // previous 90% constant made a 102px HTML button become 126px
                // in a 140px preview and made parity comparisons misleading.
                val viewBoxW = document.canvas.viewBoxWidth.coerceAtLeast(1f)
                val viewBoxH = document.canvas.viewBoxHeight.coerceAtLeast(1f)
                val viewScale = minOf(size.width / viewBoxW, size.height / viewBoxH)
                val buttonW = viewBoxW * viewScale
                val buttonH = viewBoxH * viewScale
                val buttonLeft = (size.width - buttonW) / 2f
                val buttonTop = (size.height - buttonH) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                val pxPerUnit = viewScale
                val scaleRatio = (pxPerUnit / density).coerceAtLeast(0.01f)

                val primaryBox = document.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().firstOrNull()
                val primaryShape = document.canvas.layers.filterIsInstance<CanvasLayer.GradientShape>().firstOrNull()
                val rootShapeType = (primaryBox?.shapeType?.uppercase() ?: primaryShape?.shapeType?.uppercase() ?: "ROUNDED_RECT")
                val rootIsOval = rootShapeType == "OVAL"
                val rootIsPolygon = rootShapeType == "POLYGON" || rootShapeType == "PATH" || (primaryBox?.pathData?.isNotBlank() == true)
                val rootTl = (primaryBox?.cornerRadiusTopLeft ?: primaryShape?.cornerRadius ?: 14f) * pxPerUnit
                val rootTr = (primaryBox?.cornerRadiusTopRight ?: primaryShape?.cornerRadius ?: 14f) * pxPerUnit
                val rootBr = (primaryBox?.cornerRadiusBottomRight ?: primaryShape?.cornerRadius ?: 14f) * pxPerUnit
                val rootBl = (primaryBox?.cornerRadiusBottomLeft ?: primaryShape?.cornerRadius ?: 14f) * pxPerUnit

                val rootEffectiveSides = when {
                    (primaryBox?.polygonSides ?: 0) >= 3 -> primaryBox!!.polygonSides
                    rootShapeType == "HEXAGON" -> 6
                    rootShapeType == "OCTAGON" -> 8
                    else -> 0
                }
                val rootClipShape = Path().apply {
                    if (primaryBox != null && primaryBox.pathData.isNotBlank()) {
                        addPath(buildScaledPath(primaryBox.pathData, Rect(buttonLeft, buttonTop, buttonLeft + buttonW, buttonTop + buttonH)))
                    } else if (rootEffectiveSides >= 3) {
                        addPath(buildRegularPolygonPath(rootEffectiveSides, Rect(buttonLeft, buttonTop, buttonLeft + buttonW, buttonTop + buttonH)))
                    } else if (rootIsOval) {
                        addOval(Rect(buttonLeft, buttonTop, buttonLeft + buttonW, buttonTop + buttonH))
                    } else {
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                rect = Rect(buttonLeft, buttonTop, buttonLeft + buttonW, buttonTop + buttonH),
                                topLeft = CornerRadius(rootTl, rootTl),
                                topRight = CornerRadius(rootTr, rootTr),
                                bottomRight = CornerRadius(rootBr, rootBr),
                                bottomLeft = CornerRadius(rootBl, rootBl)
                            )
                        )
                    }
                }

                val layersToRender = activeLayersOnly ?: document.canvas.layers
                val finalScale = scaleAnim * trackScale
                val drawSingleLayer: androidx.compose.ui.graphics.drawscope.DrawScope.(CanvasLayer) -> Unit = { layer ->
                    when (layer) {
                        is CanvasLayer.BoxLayer -> {
                            val transform = layer.effectiveTransform
                            val effects = layer.effectiveEffects
                            val boxWidth = buttonW * layer.widthRatio
                            val boxHeight = buttonH * layer.heightRatio
                            val boxLeft = buttonLeft + transform.offsetXRatio * buttonW
                            val boxTop = buttonTop + transform.offsetYRatio * buttonH

                            val pivot = Offset(boxLeft + boxWidth * transform.originXRatio, boxTop + boxHeight * transform.originYRatio)
                            val rotAngle = if (transform.isRotating && document.animations.idleType == "ROTATE") rotateAngle else transform.rotationDegrees

                            val isOval = layer.shapeType.uppercase() == "OVAL"
                            val isPolygon = layer.shapeType.uppercase() == "POLYGON" || layer.shapeType.uppercase() == "PATH" || layer.pathData.isNotBlank()
                            val layerAlpha = effects.opacity.coerceIn(0f, 1f)
                            val tl = layer.cornerRadiusTopLeft * pxPerUnit
                            val tr = layer.cornerRadiusTopRight * pxPerUnit
                            val br = layer.cornerRadiusBottomRight * pxPerUnit
                            val bl = layer.cornerRadiusBottomLeft * pxPerUnit
                            val hasVariableCorners = !isOval && !isPolygon && (tr != tl || br != tl || bl != tl)

                            val polygonPath = if (layer.pathData.isNotBlank()) {
                                buildScaledPath(layer.pathData, Rect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight))
                            } else if (layer.polygonSides > 2) {
                                buildRegularPolygonPath(layer.polygonSides, Rect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight))
                            } else {
                                Path()
                            }

                            val variablePath = Path().apply {
                                addRoundRect(
                                    androidx.compose.ui.geometry.RoundRect(
                                        rect = Rect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight),
                                        topLeft = CornerRadius(tl, tl),
                                        topRight = CornerRadius(tr, tr),
                                        bottomRight = CornerRadius(br, br),
                                        bottomLeft = CornerRadius(bl, bl)
                                    )
                                )
                            }

                            val needsOffscreen = effects.compositingStrategy == com.sanket.tools.nexpad.nxprc.CompositingStrategy.OFFSCREEN ||
                                (layerAlpha < 1.0f && (layer.fills.size > 1 || layer.boxShadows.isNotEmpty()))
                            val subAlpha = if (needsOffscreen) 1.0f else layerAlpha

                            val drawBox: () -> Unit = {
                                withTransform({
                                    if (rotAngle != 0f) rotate(rotAngle, pivot = pivot)
                                    if (transform.scaleX != 1f || transform.scaleY != 1f) scale(scaleX = transform.scaleX, scaleY = transform.scaleY, pivot = pivot)
                                }) {
                                    val ovalClipPath = Path().apply {
                                        addOval(Rect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight))
                                    }

                                    val elementRRect = if (isOval) {
                                        SkRRect.makeOvalXYWH(boxLeft, boxTop, boxWidth, boxHeight)
                                    } else if (hasVariableCorners) {
                                        SkRRect.makeComplexXYWH(boxLeft, boxTop, boxWidth, boxHeight, floatArrayOf(tl, tl, tr, tr, br, br, bl, bl))
                                    } else {
                                        SkRRect.makeXYWH(boxLeft, boxTop, boxWidth, boxHeight, tl)
                                    }

                                    // 1. Outset box shadows (drawn bottom-to-top per CSS spec)
                                    layer.boxShadows.filter { !it.isInset }.reversed().forEach { shadow ->
                                        val shadowOffset = Offset(shadow.offsetX * pxPerUnit, shadow.offsetY * pxPerUnit)
                                        val sColor = Color(shadow.color)
                                        val spreadPx = shadow.spreadRadius * pxPerUnit
                                        val blurPx = shadow.blurRadius * pxPerUnit
                                        val effAlpha = (sColor.alpha * subAlpha).coerceIn(0f, 1f)
                                        if (effAlpha <= 0.001f) return@forEach
                                        val shadowColorArgb = sColor.copy(alpha = effAlpha).toArgb()

                                        val sLeft = boxLeft + shadowOffset.x - spreadPx
                                        val sTop = boxTop + shadowOffset.y - spreadPx
                                        val sWidth = (boxWidth + spreadPx * 2f).coerceAtLeast(0f)
                                        val sHeight = (boxHeight + spreadPx * 2f).coerceAtLeast(0f)

                                        val shadowRRect = if (isOval) {
                                            SkRRect.makeOvalXYWH(sLeft, sTop, sWidth, sHeight)
                                        } else if (hasVariableCorners) {
                                            val sTl = (tl + spreadPx).coerceAtLeast(0f)
                                            val sTr = (tr + spreadPx).coerceAtLeast(0f)
                                            val sBr = (br + spreadPx).coerceAtLeast(0f)
                                            val sBl = (bl + spreadPx).coerceAtLeast(0f)
                                            SkRRect.makeComplexXYWH(sLeft, sTop, sWidth, sHeight, floatArrayOf(sTl, sTl, sTr, sTr, sBr, sBr, sBl, sBl))
                                        } else {
                                            val sRadius = (tl + spreadPx).coerceAtLeast(0f)
                                            SkRRect.makeXYWH(sLeft, sTop, sWidth, sHeight, sRadius)
                                        }

                                        val skCanvas = drawContext.canvas.skiaCanvas
                                        val skPaint = SkPaint().apply {
                                            color = shadowColorArgb
                                            if (blurPx > 0f) {
                                                maskFilter = SkMaskFilter.makeBlur(
                                                    SkFilterBlurMode.NORMAL,
                                                    (blurPx / 2f).coerceAtLeast(0.5f)
                                                )
                                            }
                                        }
                                        skCanvas.save()
                                        try {
                                            if (!isPolygon) {
                                                skCanvas.clipRRect(elementRRect, SkClipMode.DIFFERENCE, true)
                                                skCanvas.drawRRect(shadowRRect, skPaint)
                                            } else {
                                                skCanvas.drawRRect(shadowRRect, skPaint)
                                            }
                                        } finally {
                                            skCanvas.restore()
                                            skPaint.close()
                                        }
                                    }

                                    // 2. Main surface fills (stacked bottom-to-top per CSS painter's algorithm)
                                    val allBrushes = if (layer.fills.isNotEmpty()) {
                                        layer.fills.map { createBrush(it, Size(boxWidth, boxHeight), Offset(boxLeft, boxTop)) }
                                    } else {
                                        listOf(createBrush(layer.fill, Size(boxWidth, boxHeight), Offset(boxLeft, boxTop)))
                                    }

                                    allBrushes.forEach { b ->
                                        if (isPolygon && isOval) {
                                            clipPath(ovalClipPath) {
                                                drawPath(polygonPath, brush = b, alpha = subAlpha)
                                            }
                                        } else if (isPolygon && hasVariableCorners) {
                                            clipPath(variablePath) {
                                                drawPath(polygonPath, brush = b, alpha = subAlpha)
                                            }
                                        } else if (isPolygon) {
                                            drawPath(polygonPath, brush = b, alpha = subAlpha)
                                        } else if (isOval) {
                                            drawOval(
                                                brush = b,
                                                topLeft = Offset(boxLeft, boxTop),
                                                size = Size(boxWidth, boxHeight),
                                                alpha = subAlpha
                                            )
                                        } else if (hasVariableCorners) {
                                            drawPath(variablePath, brush = b, alpha = subAlpha)
                                        } else {
                                            drawRoundRect(
                                                brush = b,
                                                topLeft = Offset(boxLeft, boxTop),
                                                size = Size(boxWidth, boxHeight),
                                                cornerRadius = CornerRadius(tl, tl),
                                                alpha = subAlpha
                                            )
                                        }
                                    }

                                    // 3. Stroke / Border
                                    layer.stroke?.let { st ->
                                        val stColor = Color(st.color)
                                        val stWidth = st.width * pxPerUnit
                                        val strokeStyle = if (st.isDashed) {
                                            Stroke(width = stWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * pxPerUnit, 6f * pxPerUnit), 0f))
                                        } else {
                                            Stroke(width = stWidth)
                                        }
                                        if (isPolygon && isOval) {
                                            clipPath(ovalClipPath) {
                                                drawPath(polygonPath, color = stColor.copy(alpha = stColor.alpha * subAlpha), style = strokeStyle)
                                            }
                                        } else if (isPolygon && hasVariableCorners) {
                                            clipPath(variablePath) {
                                                drawPath(polygonPath, color = stColor.copy(alpha = stColor.alpha * subAlpha), style = strokeStyle)
                                            }
                                        } else if (isPolygon) {
                                            drawPath(polygonPath, color = stColor.copy(alpha = stColor.alpha * subAlpha), style = strokeStyle)
                                        } else if (isOval) {
                                            if (st.isTopOnly) {
                                                drawArc(
                                                    color = stColor.copy(alpha = stColor.alpha * subAlpha),
                                                    startAngle = 180f,
                                                    sweepAngle = 180f,
                                                    useCenter = false,
                                                    topLeft = Offset(boxLeft, boxTop),
                                                    size = Size(boxWidth, boxHeight),
                                                    style = strokeStyle
                                                )
                                            } else {
                                                drawOval(
                                                    color = stColor.copy(alpha = stColor.alpha * subAlpha),
                                                    topLeft = Offset(boxLeft, boxTop),
                                                    size = Size(boxWidth, boxHeight),
                                                    style = strokeStyle
                                                )
                                            }
                                        } else if (hasVariableCorners) {
                                            drawPath(variablePath, color = stColor.copy(alpha = stColor.alpha * subAlpha), style = strokeStyle)
                                        } else {
                                            drawRoundRect(
                                                color = stColor.copy(alpha = stColor.alpha * subAlpha),
                                                topLeft = Offset(boxLeft, boxTop),
                                                size = Size(boxWidth, boxHeight),
                                                cornerRadius = CornerRadius(tl, tl),
                                                style = strokeStyle
                                            )
                                        }
                                    }

                                    // 4. Inset box shadows
                                    val insets = layer.boxShadows.filter { it.isInset }
                                    if (insets.isNotEmpty()) {
                                        insets.forEach { shadow ->
                                            val inColor = Color(shadow.color)
                                            val inAlpha = (inColor.alpha * subAlpha).coerceIn(0f, 1f)
                                            if (inAlpha <= 0.001f) return@forEach
                                            val shadowColorArgb = inColor.copy(alpha = inAlpha).toArgb()

                                            val sOffset = Offset(shadow.offsetX * pxPerUnit, shadow.offsetY * pxPerUnit)
                                            val spreadPx = shadow.spreadRadius * pxPerUnit
                                            val blurPx = shadow.blurRadius * pxPerUnit

                                            val hLeft = boxLeft + sOffset.x + spreadPx
                                            val hTop = boxTop + sOffset.y + spreadPx
                                            val hWidth = (boxWidth - spreadPx * 2f).coerceAtLeast(0f)
                                            val hHeight = (boxHeight - spreadPx * 2f).coerceAtLeast(0f)

                                            val margin = blurPx * 3f + kotlin.math.abs(sOffset.x) + kotlin.math.abs(sOffset.y) + 32f
                                            val insetPath = Path().apply {
                                                fillType = PathFillType.EvenOdd
                                                addRect(Rect(boxLeft - margin, boxTop - margin, boxLeft + boxWidth + margin, boxTop + boxHeight + margin))
                                                if (isOval) {
                                                    addOval(Rect(hLeft, hTop, hLeft + hWidth, hTop + hHeight))
                                                } else if (hasVariableCorners) {
                                                    val hTl = (tl - spreadPx).coerceAtLeast(0f)
                                                    val hTr = (tr - spreadPx).coerceAtLeast(0f)
                                                    val hBr = (br - spreadPx).coerceAtLeast(0f)
                                                    val hBl = (bl - spreadPx).coerceAtLeast(0f)
                                                    addRoundRect(
                                                        androidx.compose.ui.geometry.RoundRect(
                                                            rect = Rect(hLeft, hTop, hLeft + hWidth, hTop + hHeight),
                                                            topLeft = CornerRadius(hTl, hTl),
                                                            topRight = CornerRadius(hTr, hTr),
                                                            bottomRight = CornerRadius(hBr, hBr),
                                                            bottomLeft = CornerRadius(hBl, hBl)
                                                        )
                                                    )
                                                } else {
                                                    val hRadius = (tl - spreadPx).coerceAtLeast(0f)
                                                    addRoundRect(
                                                        androidx.compose.ui.geometry.RoundRect(
                                                            rect = Rect(hLeft, hTop, hLeft + hWidth, hTop + hHeight),
                                                            cornerRadius = CornerRadius(hRadius, hRadius)
                                                        )
                                                    )
                                                }
                                            }

                                            val skCanvas = drawContext.canvas.skiaCanvas
                                            val skPaint = SkPaint().apply {
                                                color = shadowColorArgb
                                                if (blurPx > 0f) {
                                                    maskFilter = SkMaskFilter.makeBlur(
                                                        SkFilterBlurMode.NORMAL,
                                                        (blurPx / 2f).coerceAtLeast(0.5f)
                                                    )
                                                }
                                            }
                                            skCanvas.save()
                                            try {
                                                skCanvas.clipRRect(elementRRect, SkClipMode.INTERSECT, true)
                                                skCanvas.drawPath(insetPath.asSkiaPath(), skPaint)
                                            } finally {
                                                skCanvas.restore()
                                                skPaint.close()
                                            }
                                        }
                                    }
                                }
                            }

                            val drawFilteredBox: () -> Unit = {
                                val colorFilter = createNxprcColorFilter(effects.filter)
                                if (colorFilter == null && !needsOffscreen) {
                                    drawBox()
                                } else {
                                    val paint = Paint().apply {
                                        if (colorFilter != null) this.colorFilter = colorFilter
                                        if (needsOffscreen) this.alpha = layerAlpha
                                    }
                                    val outsets = effects.layerOutsets
                                    val saveRect = Rect(
                                        boxLeft - outsets.left * pxPerUnit - 20f,
                                        boxTop - outsets.top * pxPerUnit - 20f,
                                        boxLeft + boxWidth + outsets.right * pxPerUnit + 20f,
                                        boxTop + boxHeight + outsets.bottom * pxPerUnit + 20f
                                    )
                                    drawContext.canvas.saveLayer(saveRect, paint)
                                    drawBox()
                                    drawContext.canvas.restore()
                                }
                            }

                            val isRootLayer = layer == document.canvas.layers.firstOrNull() || (layer.widthRatio >= 1.0f && layer.heightRatio >= 1.0f)
                            if (!isRootLayer && (layer.clipToBounds || document.canvas.clipToBounds)) {
                                clipPath(rootClipShape) {
                                    drawFilteredBox()
                                }
                            } else {
                                drawFilteredBox()
                            }
                        }
                        is CanvasLayer.BezelSocket -> {
                            val baseRadius = minOf(buttonW, buttonH) / 2f
                            // 1. Soft bottom drop shadow
                            drawCircle(
                                color = Color(layer.shadowColor),
                                radius = baseRadius * 0.98f,
                                center = Offset(centerOffset.x, centerOffset.y + baseRadius * 0.05f)
                            )
                            // 2. Solid bezel well
                            drawCircle(
                                color = Color(layer.outerBezelColor),
                                radius = baseRadius * 0.98f,
                                center = centerOffset
                            )
                            // 3. Bezel rim stroke
                            drawCircle(
                                color = Color(layer.outerBevelStroke),
                                radius = baseRadius * 0.98f,
                                center = centerOffset,
                                style = Stroke(width = 2f * pxPerUnit)
                            )
                        }
                        is CanvasLayer.GlowRing -> {
                            val alpha = if (layer.pulseEnabled && document.animations.idleType == "PULSE") pulseAlpha else 0.8f
                            val glowColor = Color(layer.glowColor)
                            val buttonRadius = minOf(buttonW, buttonH) / 2f
                            val blurPx = (layer.blurRadius * pxPerUnit).coerceAtLeast(8f * scaleRatio)
                            val totalRadius = (buttonRadius + blurPx).coerceAtMost(size.minDimension / 2f)
                            val innerRatio = (buttonRadius / totalRadius).coerceIn(0.1f, 0.85f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0.0f to glowColor.copy(alpha = alpha * 0.35f),
                                        innerRatio to glowColor.copy(alpha = alpha * 0.28f),
                                        (innerRatio + (1f - innerRatio) * 0.5f) to glowColor.copy(alpha = alpha * 0.10f),
                                        1.0f to Color.Transparent
                                    ),
                                    center = centerOffset,
                                    radius = totalRadius
                                ),
                                radius = totalRadius,
                                center = centerOffset
                            )
                        }
                        is CanvasLayer.GradientShape -> {
                            val transform = layer.effectiveTransform
                            val effects = layer.effectiveEffects
                            val shapeW = buttonW * layer.widthRatio
                            val shapeH = buttonH * layer.heightRatio
                            val shapeLeft = buttonLeft + buttonW * transform.offsetXRatio
                            val shapeTop = buttonTop + buttonH * transform.offsetYRatio
                            val shapeSize = Size(shapeW, shapeH)
                            val brush = createBrush(layer.fill, shapeSize, Offset(shapeLeft, shapeTop))
                            val cornerRadiusPx = layer.cornerRadius * pxPerUnit
                            val shapeAlpha = effects.opacity.coerceIn(0f, 1f)

                            val hasTransform = transform.hasTransform
                            val pivot = Offset(
                                shapeLeft + transform.originXRatio * shapeW,
                                shapeTop + transform.originYRatio * shapeH
                            )

                            val drawShape: () -> Unit = {
                                val shapeType = layer.shapeType.uppercase()
                                val effectiveSides = when {
                                    shapeType == "HEXAGON" -> 6
                                    shapeType == "OCTAGON" -> 8
                                    shapeType == "POLYGON" -> 6
                                    else -> 0
                                }
                                when {
                                    effectiveSides >= 3 -> {
                                        val polyPath = buildRegularPolygonPath(effectiveSides, Rect(shapeLeft, shapeTop, shapeLeft + shapeW, shapeTop + shapeH))
                                        drawPath(polyPath, brush = brush, alpha = shapeAlpha)
                                        layer.stroke?.let { st ->
                                            val stColor = Color(st.color)
                                            val strokeStyle = if (st.isDashed) Stroke(width = st.width * pxPerUnit, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * pxPerUnit, 6f * pxPerUnit), 0f)) else Stroke(width = st.width * pxPerUnit)
                                            drawPath(polyPath, color = stColor.copy(alpha = stColor.alpha * shapeAlpha), style = strokeStyle)
                                        }
                                    }
                                    shapeType == "OVAL" -> {
                                        drawOval(
                                            brush = brush,
                                            topLeft = Offset(shapeLeft, shapeTop),
                                            size = shapeSize,
                                            alpha = shapeAlpha
                                        )
                                        layer.stroke?.let { st ->
                                            val stColor = Color(st.color)
                                            val strokeStyle = if (st.isDashed) Stroke(width = st.width * pxPerUnit, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * pxPerUnit, 6f * pxPerUnit), 0f)) else Stroke(width = st.width * pxPerUnit)
                                            if (st.isTopOnly) {
                                                drawArc(
                                                    color = stColor.copy(alpha = stColor.alpha * shapeAlpha),
                                                    startAngle = 180f,
                                                    sweepAngle = 180f,
                                                    useCenter = false,
                                                    topLeft = Offset(shapeLeft, shapeTop),
                                                    size = shapeSize,
                                                    style = strokeStyle
                                                )
                                            } else {
                                                drawOval(
                                                    color = stColor.copy(alpha = stColor.alpha * shapeAlpha),
                                                    topLeft = Offset(shapeLeft, shapeTop),
                                                    size = shapeSize,
                                                    style = strokeStyle
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        drawRoundRect(
                                            brush = brush,
                                            topLeft = Offset(shapeLeft, shapeTop),
                                            size = shapeSize,
                                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                            alpha = shapeAlpha
                                        )
                                        layer.stroke?.let { st ->
                                            val stColor = Color(st.color)
                                            val strokeStyle = if (st.isDashed) Stroke(width = st.width * pxPerUnit, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * pxPerUnit, 6f * pxPerUnit), 0f)) else Stroke(width = st.width * pxPerUnit)
                                            drawRoundRect(
                                                color = stColor.copy(alpha = stColor.alpha * shapeAlpha),
                                                topLeft = Offset(shapeLeft, shapeTop),
                                                size = shapeSize,
                                                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                                style = strokeStyle
                                            )
                                        }
                                    }
                                }
                            }

                            val drawFilteredShape: () -> Unit = {
                                val colorFilter = createNxprcColorFilter(effects.filter)
                                if (colorFilter == null) {
                                    drawShape()
                                } else {
                                    val paint = Paint().apply { this.colorFilter = colorFilter }
                                    drawContext.canvas.saveLayer(
                                        Rect(shapeLeft, shapeTop, shapeLeft + shapeW, shapeTop + shapeH),
                                        paint
                                    )
                                    drawShape()
                                    drawContext.canvas.restore()
                                }
                            }

                            if (hasTransform) {
                                withTransform({
                                    scale(transform.scaleX, transform.scaleY, pivot = pivot)
                                    rotate(transform.rotationDegrees, pivot = pivot)
                                }) {
                                    drawFilteredShape()
                                }
                            } else {
                                drawFilteredShape()
                            }
                        }
                        is CanvasLayer.InnerShadow -> {
                            val elementRRect = if (rootIsOval) {
                                SkRRect.makeOvalXYWH(buttonLeft, buttonTop, buttonW, buttonH)
                            } else {
                                SkRRect.makeXYWH(buttonLeft, buttonTop, buttonW, buttonH, rootTl)
                            }

                            val shadows = listOf(
                                Triple(Color(layer.highlightColor), Offset(0f, layer.strokeWidth * pxPerUnit * 0.6f), layer.strokeWidth * pxPerUnit * 1.0f),
                                Triple(Color(layer.shadowColor), Offset(0f, -layer.strokeWidth * pxPerUnit * 1.4f), layer.strokeWidth * pxPerUnit * 2.0f)
                            )

                            val skCanvas = drawContext.canvas.skiaCanvas
                            shadows.forEach { (color, sOffset, blurPx) ->
                                val alpha = color.alpha
                                if (alpha <= 0.001f) return@forEach
                                val shadowColorArgb = color.toArgb()

                                val hLeft = buttonLeft + sOffset.x
                                val hTop = buttonTop + sOffset.y
                                val hWidth = buttonW
                                val hHeight = buttonH

                                val holeRRect = if (rootIsOval) {
                                    SkRRect.makeOvalXYWH(hLeft, hTop, hWidth, hHeight)
                                } else {
                                    SkRRect.makeXYWH(hLeft, hTop, hWidth, hHeight, rootTl)
                                }

                                val margin = blurPx * 3f + kotlin.math.abs(sOffset.y) + 32f
                                val outerRect = SkRect.makeLTRB(buttonLeft - margin, buttonTop - margin, buttonLeft + buttonW + margin, buttonTop + buttonH + margin)

                                val skPaint = SkPaint().apply {
                                    this.color = shadowColorArgb
                                    if (blurPx > 0f) {
                                        maskFilter = SkMaskFilter.makeBlur(
                                            SkFilterBlurMode.NORMAL,
                                            (blurPx / 2f).coerceAtLeast(0.5f)
                                        )
                                    }
                                }
                                skCanvas.save()
                                try {
                                    skCanvas.clipRRect(elementRRect, SkClipMode.INTERSECT, true)
                                    skCanvas.clipRRect(holeRRect, SkClipMode.DIFFERENCE, true)
                                    skCanvas.drawRect(outerRect, skPaint)
                                } finally {
                                    skCanvas.restore()
                                    skPaint.close()
                                }
                            }
                        }
                        is CanvasLayer.GlossReflection -> {
                            val glossW = buttonW * layer.widthRatio
                            val glossH = buttonH * layer.heightRatio
                            val glossLeft = buttonLeft + buttonW * layer.offsetXRatio
                            val glossTop = buttonTop + buttonH * layer.offsetYRatio
                            val glossCenter = Offset(glossLeft + glossW / 2f, glossTop + glossH / 2f)

                            val drawGloss: () -> Unit = {
                                rotate(layer.rotationDegrees, pivot = glossCenter) {
                                    val blurSpread = layer.blurRadius * pxPerUnit
                                    val effectiveRadius = (glossW / 2f + blurSpread).coerceAtLeast(1f)
                                    val glossBrush = Brush.radialGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.55f * layer.alpha),
                                            Color.White.copy(alpha = 0.25f * layer.alpha),
                                            Color.White.copy(alpha = 0.05f * layer.alpha),
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
                                clipPath(rootClipShape) {
                                    drawGloss()
                                }
                            } else {
                                drawGloss()
                            }
                        }
                        is CanvasLayer.VectorPath -> {
                            val targetRect = Rect(
                                buttonLeft + buttonW * layer.offsetXRatio,
                                buttonTop + buttonH * layer.offsetYRatio,
                                buttonLeft + buttonW * (layer.offsetXRatio + layer.scale),
                                buttonTop + buttonH * (layer.offsetYRatio + layer.scale)
                            )
                            val brush = createBrush(layer.fill, targetRect.size, targetRect.topLeft)
                            val angle = if (layer.isRotating && document.animations.idleType == "ROTATE") rotateAngle else layer.rotationDegrees
                            val vectorPath = if (layer.pathData.isNotBlank()) {
                                buildScaledPath(layer.pathData, targetRect)
                            } else null

                            rotate(angle, pivot = centerOffset) {
                                if (vectorPath != null) {
                                    drawPath(vectorPath, brush = brush)
                                    layer.stroke?.let { st ->
                                        drawPath(vectorPath, color = Color(st.color), style = Stroke(width = st.width * pxPerUnit))
                                    }
                                } else {
                                    drawCircle(
                                        brush = brush,
                                        radius = size.minDimension / 2f * 0.75f
                                    )
                                    layer.stroke?.let { st ->
                                        drawCircle(
                                            color = Color(st.color),
                                            radius = size.minDimension / 2f * 0.75f,
                                            style = Stroke(width = st.width * pxPerUnit)
                                        )
                                    }
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

                layersToRender.forEachIndexed { layerIdx, layer ->
                    val origIdx = if (activeLayersOnly != null) document.canvas.layers.indexOf(layer) else layerIdx
                    val isCap = isTwoStageStick && origIdx != -1 && origIdx in capIndicesSet
                    if (isCap) {
                        withTransform({
                            translate(left = thumbOffsetX.value, top = thumbOffsetY.value)
                            scale(scaleX = finalScale, scaleY = finalScale, pivot = centerOffset)
                        }) {
                            drawSingleLayer(layer)
                        }
                    } else {
                        drawSingleLayer(layer)
                    }
                }
            }

        // Center text glyph or multi-text layers with embossed 3D lighting and tactile synchronization
        val effectiveLayers = activeLayersOnly ?: document.canvas.layers
        val glyph = remember(document, effectiveLayers) { effectiveLayers.filterIsInstance<CanvasLayer.CenterGlyph>().firstOrNull() }
        val textLayers = remember(document, effectiveLayers) { effectiveLayers.filterIsInstance<CanvasLayer.TextLayer>() }
        val viewBox = document.canvas.viewBoxWidth.coerceAtLeast(1f)
        val viewBoxH = document.canvas.viewBoxHeight.coerceAtLeast(1f)
        val viewScale = minOf(sizeDp.toFloat() / viewBox, sizeDp.toFloat() / viewBoxH)
        val buttonW = viewBox * viewScale
        val buttonH = viewBoxH * viewScale
        val scaleFactor = viewScale
        val rootBox = remember(document) { document.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().firstOrNull() }

        if (textLayers.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(sizeDp.dp)
                    .graphicsLayer {
                        rotationZ = rootBox?.effectiveTransform?.rotationDegrees ?: 0f
                    }
            ) {
                textLayers.forEach { tl ->
                    val fontSp = (tl.fontSizeSp * scaleFactor).sp
                    val fontWeight = if (tl.fontWeight >= 900) FontWeight.Black else if (tl.fontWeight >= 700) FontWeight.Bold else FontWeight.Normal
                    val origIdx = document.canvas.layers.indexOf(tl)
                    val isLayerCap = isTwoStageStick && origIdx != -1 && origIdx in capIndicesSet
                    val stickShiftX = if (isLayerCap) (thumbOffsetX.value / density).dp else 0.dp
                    val stickShiftY = if (isLayerCap) (thumbOffsetY.value / density).dp else 0.dp
                    val offX = (tl.offsetXRatio * buttonW).dp + stickShiftX
                    val offY = (tl.offsetYRatio * buttonH).dp + stickShiftY

                    if (tl.textShadows.isNotEmpty()) {
                        tl.textShadows.forEach { ts ->
                            Text(
                                text = tl.text,
                                color = Color(ts.color),
                                fontSize = fontSp,
                                fontWeight = fontWeight,
                                modifier = Modifier.offset(
                                    x = offX + (ts.offsetX * scaleFactor).dp,
                                    y = offY + (ts.offsetY * scaleFactor).dp
                                )
                            )
                        }
                    }
                    Text(
                        text = tl.text,
                        color = Color(tl.textColor),
                        fontSize = fontSp,
                        fontWeight = fontWeight,
                        modifier = Modifier.offset(x = offX, y = offY)
                    )
                }
            }
        } else if (glyph != null) {
            val centerText = glyph?.text ?: document.manifest.defaultControl
            val textColor = glyph?.textColor ?: 0xFFF5F5F5L
            val baseFontSp = glyph?.fontSizeSp ?: (viewBox * 0.32f)
            val fontSp = (baseFontSp * scaleFactor).sp
            val fontWeight = FontWeight.Bold

            val stickShiftX = if (isTwoStageStick) (thumbOffsetX.value / density).dp else 0.dp
            val stickShiftY = if (isTwoStageStick) (thumbOffsetY.value / density).dp else 0.dp
            val offX = ((glyph?.offsetXRatio ?: 0f) * buttonW).dp + stickShiftX
            val offY = ((glyph?.offsetYRatio ?: 0f) * buttonH).dp + stickShiftY

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = offX, y = offY)
                    .graphicsLayer {
                        rotationZ = rootBox?.effectiveTransform?.rotationDegrees ?: 0f
                        if (isTwoStageStick) {
                            val finalScale = scaleAnim * trackScale
                            scaleX = finalScale
                            scaleY = finalScale
                        }
                    }
            ) {
                val extraShadows = glyph?.textShadows ?: emptyList()
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
}
}

private fun sampleGradientColor(colors: List<Long>, stops: List<Float>, pos: Float): Color {
    if (colors.isEmpty()) return Color.Transparent
    if (colors.size == 1) return Color(colors[0])
    val clamped = pos.coerceIn(0f, 1f)
    var idx = 0
    while (idx < stops.size - 1 && stops[idx + 1] < clamped) {
        idx++
    }
    if (idx >= stops.size - 1) return Color(colors.last())
    val s0 = stops[idx]
    val s1 = stops[idx + 1]
    val range = (s1 - s0).coerceAtLeast(0.0001f)
    val t = ((clamped - s0) / range).coerceIn(0f, 1f)
    val c1 = Color(colors[idx])
    val c2 = Color(colors[idx + 1])
    return Color(
        red = c1.red + (c2.red - c1.red) * t,
        green = c1.green + (c2.green - c1.green) * t,
        blue = c1.blue + (c2.blue - c1.blue) * t,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * t
    )
}

private fun createBrush(fill: FillBrush, size: Size, topLeft: Offset = Offset.Zero): Brush {
    return when (fill) {
        is FillBrush.Solid -> SolidColor(Color(fill.color))
        is FillBrush.LinearGradient -> {
            val angleRad = Math.toRadians((fill.angleDegrees - 90.0))
            val cx = topLeft.x + size.width / 2f
            val cy = topLeft.y + size.height / 2f
            val halfLen = Math.hypot(size.width.toDouble(), size.height.toDouble()).toFloat() / 2f
            val cos = Math.cos(angleRad).toFloat()
            val sin = Math.sin(angleRad).toFloat()
            val start = Offset(cx - cos * halfLen, cy - sin * halfLen)
            val end = Offset(cx + cos * halfLen, cy + sin * halfLen)

            if (fill.stops.isNotEmpty() && fill.stops.size == fill.colors.size) {
                val colorStops = fill.colors.indices.map { i ->
                    fill.stops[i] to Color(fill.colors[i])
                }.toTypedArray()
                Brush.linearGradient(colorStops = colorStops, start = start, end = end)
            } else {
                Brush.linearGradient(fill.colors.map { Color(it) }, start = start, end = end)
            }
        }
        is FillBrush.RadialGradient -> {
            val cx = topLeft.x + size.width * fill.centerXRatio
            val cy = topLeft.y + size.height * fill.centerYRatio
            // GradientParser already folds CSS background-size into radiusRatio.
            // Applying another multiplier made radial layers too broad and
            // washed out the dark violet edge compared with the browser.
            val maxR = size.minDimension * fill.radiusRatio
            if (fill.stops.isNotEmpty() && fill.stops.size == fill.colors.size) {
                val colorStops = fill.colors.indices.map { i ->
                    fill.stops[i] to Color(fill.colors[i])
                }.toTypedArray()
                Brush.radialGradient(
                    colorStops = colorStops,
                    center = Offset(cx, cy),
                    radius = maxR.coerceAtLeast(1f)
                )
            } else {
                Brush.radialGradient(
                    colors = fill.colors.map { Color(it) },
                    center = Offset(cx, cy),
                    radius = maxR.coerceAtLeast(1f)
                )
            }
        }
        is FillBrush.SweepGradient -> {
            val cx = topLeft.x + size.width * fill.centerXRatio
            val cy = topLeft.y + size.height * fill.centerYRatio
            if (fill.colors.size >= 2) {
                // In CSS conic-gradient, 0deg points North (12 o'clock / -Y).
                // Compose Brush.sweepGradient starts at East (3 o'clock / +X).
                // Therefore: standard_angle = css_angle - 90deg.
                val startPos = (((fill.startAngleDegrees - 90f) % 360f + 360f) % 360f) / 360f
                val n = fill.colors.size
                val rawStops = if (fill.stops.size == n) fill.stops else List(n) { it.toFloat() / (n - 1) }
                val samples = 72
                val sampleStops = FloatArray(samples + 1) { it.toFloat() / samples }
                val colorStops = sampleStops.map { s ->
                    val origPos = (s - startPos + 1.0f) % 1.0f
                    s to sampleGradientColor(fill.colors, rawStops, origPos)
                }.toTypedArray()
                Brush.sweepGradient(
                    colorStops = colorStops,
                    center = Offset(cx, cy)
                )
            } else if (fill.colors.isNotEmpty()) {
                Brush.sweepGradient(
                    colors = fill.colors.map { Color(it) },
                    center = Offset(cx, cy)
                )
            } else {
                SolidColor(Color.Transparent)
            }
        }
    }
}

internal fun buildScaledPath(svgData: String, targetRect: Rect): Path {
    val trimmed = svgData.trim()
    if (trimmed.isBlank()) return Path()

    // 1. Skia SVG path parsing with bounds-based transformation
    try {
        val skiaPath = org.jetbrains.skia.Path.makeFromSVGString(trimmed)
        if (skiaPath != null) {
            val bounds = skiaPath.bounds
            val composePath = skiaPath.asComposePath()
            if (bounds.width > 0.001f && bounds.height > 0.001f) {
                val isNormalized100 = bounds.left >= -5f && bounds.top >= -5f && bounds.right <= 105f && bounds.bottom <= 105f
                val matrix = Matrix().apply {
                    if (isNormalized100) {
                        translate(x = targetRect.left, y = targetRect.top)
                        scale(x = targetRect.width / 100f, y = targetRect.height / 100f)
                    } else {
                        translate(x = targetRect.left, y = targetRect.top)
                        scale(x = targetRect.width / bounds.width, y = targetRect.height / bounds.height)
                        translate(x = -bounds.left, y = -bounds.top)
                    }
                }
                composePath.transform(matrix)
                return composePath
            }
        }
    } catch (_: Throwable) {
        // Fall back to manual token parser below
    }

    // 2. Percentage tokenizer fallback (supports M, L, Z normalized 0..100)
    val path = Path()
    val tokens = trimmed.split(java.util.regex.Pattern.compile("[,\\s]+")).filter { it.isNotEmpty() }
    var i = 0
    while (i < tokens.size) {
        val tok = tokens[i].uppercase()
        when (tok) {
            "M" -> {
                if (i + 2 < tokens.size) {
                    val x = targetRect.left + ((tokens[i + 1].toFloatOrNull() ?: 0f) / 100f) * targetRect.width
                    val y = targetRect.top + ((tokens[i + 2].toFloatOrNull() ?: 0f) / 100f) * targetRect.height
                    path.moveTo(x, y)
                    i += 3
                } else i++
            }
            "L" -> {
                if (i + 2 < tokens.size) {
                    val x = targetRect.left + ((tokens[i + 1].toFloatOrNull() ?: 0f) / 100f) * targetRect.width
                    val y = targetRect.top + ((tokens[i + 2].toFloatOrNull() ?: 0f) / 100f) * targetRect.height
                    path.lineTo(x, y)
                    i += 3
                } else i++
            }
            "Z" -> {
                path.close()
                i++
            }
            else -> i++
        }
    }
    return path
}

internal fun buildRegularPolygonPath(sides: Int, targetRect: Rect): Path {
    val path = Path()
    if (sides < 3) return path
    val cx = targetRect.center.x
    val cy = targetRect.center.y
    val rx = targetRect.width / 2f
    val ry = targetRect.height / 2f
    val angleStep = (2.0 * Math.PI / sides)
    val startAngle = -Math.PI / 2.0 // start at top

    for (i in 0 until sides) {
        val a = startAngle + i * angleStep
        val x = (cx + rx * Math.cos(a)).toFloat()
        val y = (cy + ry * Math.sin(a)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
