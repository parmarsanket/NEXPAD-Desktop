package com.sanket.tools.nexpaddesktop.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

val PlusJakartaSans = FontFamily(
    Font(resource = "fonts/PlusJakartaSans-Regular.ttf", weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(resource = "fonts/PlusJakartaSans-Medium.ttf", weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(resource = "fonts/PlusJakartaSans-SemiBold.ttf", weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(resource = "fonts/PlusJakartaSans-Bold.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(resource = "fonts/PlusJakartaSans-ExtraBold.ttf", weight = FontWeight.ExtraBold, style = FontStyle.Normal),
    Font(resource = "fonts/PlusJakartaSans-ExtraBold.ttf", weight = FontWeight.Black, style = FontStyle.Normal)
)

val SpaceGrotesk = FontFamily(
    Font(resource = "fonts/SpaceGrotesk-Medium.ttf", weight = FontWeight.Medium, style = FontStyle.Normal)
)

val NexpadTypography = Typography(
    // Large Screen / Brand Display
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.02).sp
    ),
    // Screen Main Title ("CONTROLLER & MOTION")
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.8.sp
    ),
    // Hero Telemetry Readout ("184.5 °/s", "+28 °") - Tabular numbers prevent 60Hz jitter!
    displaySmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    ),
    // Card Titles ("Xbox 360", "DualShock 4")
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    // Section Headers ("Gyro Precision Aiming", "Analog Sticks Base Sensitivity")
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp
    ),
    // Sub-group Headers & Live Value Readouts ("Right Stick", "+12.4 °/s")
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontFeatureSettings = "tnum"
    ),
    // Primary Explanatory Copy
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    // Monospace Data Chips & Mini Badges (DSU, Active, Rate)
    labelMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.4.sp
    )
)

/** Backward-compatible alias for [NexpadTypography] */
val Typography = NexpadTypography
