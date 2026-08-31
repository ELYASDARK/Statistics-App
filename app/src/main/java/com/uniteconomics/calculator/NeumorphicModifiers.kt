package com.uniteconomics.calculator

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

/**
 * Neumorphic Color Design Tokens for Modifiers matching luminous_fintech/DESIGN.md:
 * Light Surface: #E0E5EC
 * Light Dark Shadow: #A3B1C6 (alpha 0.6)
 * Light Light Shadow: #FFFFFF (alpha 0.8)
 * Dark Surface: #171C21 / #1E232B
 * Dark Dark Shadow: #0C0F13 (alpha 0.8)
 * Dark Light Shadow: #242B35 (alpha 0.5)
 */
val NeuColorLightSurface = Color(0xFFE0E5EC)
val NeuColorLightDarkShadow = Color(0xFFA3B1C6).copy(alpha = 0.50f)
val NeuColorLightLightShadow = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val NeuColorLightGradLight = Color(0xFFF5F8FC)
val NeuColorLightGradDark = Color(0xFFCAD1DC)

val NeuColorDarkSurface = Color(0xFF171C21)
val NeuColorDarkSurfaceCard = Color(0xFF1E232B)
val NeuColorDarkDarkShadow = Color(0xFF0C0F13).copy(alpha = 0.85f)
val NeuColorDarkLightShadow = Color.Transparent
val NeuColorDarkGradLight = Color(0xFF1E232B)
val NeuColorDarkGradDark = Color(0xFF1E232B)

/**
 * Helper to convert Shape to corner radius float in pixels.
 */
fun Shape.toCornerRadiusPx(
    size: Size,
    density: Density,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr
): Float {
    val outline = this.createOutline(size, layoutDirection, density)
    return when (outline) {
        is Outline.Rounded -> outline.roundRect.topLeftCornerRadius.x
        is Outline.Rectangle -> 0f
        else -> 16f * density.density
    }
}

/**
 * Neumorphic Flat modifier: Extruded surface with top-left light shadow and bottom-right dark shadow.
 */
fun Modifier.neuFlat(
    cornerShape: Shape,
    isDark: Boolean? = null,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = composed {
    val dark = isDark ?: isSystemInDarkTheme()
    this.neuFlat(
        lightShadowColor = if (dark) NeuColorDarkLightShadow else NeuColorLightLightShadow,
        darkShadowColor = if (dark) NeuColorDarkDarkShadow else NeuColorLightDarkShadow,
        backgroundColor = if (dark) NeuColorDarkSurfaceCard else NeuColorLightSurface,
        cornerShape = cornerShape,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuFlat(
    isDark: Boolean? = null,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = composed {
    val dark = isDark ?: isSystemInDarkTheme()
    this.neuFlat(
        cornerShape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
        isDark = dark,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuFlat(
    lightShadowColor: Color,
    darkShadowColor: Color,
    backgroundColor: Color,
    cornerRadius: Dp = 16.dp,
    cornerShape: Shape? = null,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = this.drawWithCache {
    val cornerRadiusPx = cornerShape?.toCornerRadiusPx(size, this) ?: cornerRadius.toPx()
    val elevationPx = elevation.toPx()
    val blurRadiusPx = blurRadius.toPx()

    val darkPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = darkShadowColor.toArgb()
        if (blurRadiusPx > 0f) {
            maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    val hasLightShadow = lightShadowColor != Color.Transparent && lightShadowColor.alpha > 0f
    val lightPaint = if (hasLightShadow) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = lightShadowColor.toArgb()
            if (blurRadiusPx > 0f) {
                maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
            }
        }
    } else null

    val bgPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRoundRect(
                elevationPx,
                elevationPx,
                size.width + elevationPx,
                size.height + elevationPx,
                cornerRadiusPx,
                cornerRadiusPx,
                darkPaint
            )
            if (lightPaint != null) {
                canvas.nativeCanvas.drawRoundRect(
                    -elevationPx,
                    -elevationPx,
                    size.width - elevationPx,
                    size.height - elevationPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    lightPaint
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                cornerRadiusPx,
                cornerRadiusPx,
                bgPaint
            )
        }
    }
}

/**
 * Neumorphic Pressed modifier: Inset well surface with inner shadows.
 */
fun Modifier.neuPressed(
    cornerShape: Shape,
    isDark: Boolean? = null,
    elevation: Dp = 4.dp,
    blurRadius: Dp = 6.dp
): Modifier = composed {
    val dark = isDark ?: isSystemInDarkTheme()
    this.neuPressed(
        lightShadowColor = if (dark) NeuColorDarkLightShadow else NeuColorLightLightShadow,
        darkShadowColor = if (dark) NeuColorDarkDarkShadow else NeuColorLightDarkShadow,
        backgroundColor = if (dark) NeuColorDarkSurfaceCard else NeuColorLightSurface,
        cornerShape = cornerShape,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuPressed(
    isDark: Boolean? = null,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 4.dp,
    blurRadius: Dp = 6.dp
): Modifier = composed {
    this.neuPressed(
        cornerShape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
        isDark = isDark,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuPressed(
    lightShadowColor: Color,
    darkShadowColor: Color,
    backgroundColor: Color,
    cornerRadius: Dp = 16.dp,
    cornerShape: Shape? = null,
    elevation: Dp = 4.dp,
    blurRadius: Dp = 6.dp
): Modifier = this.drawWithCache {
    val cornerRadiusPx = cornerShape?.toCornerRadiusPx(size, this) ?: cornerRadius.toPx()
    val elevationPx = elevation.toPx()
    val blurRadiusPx = blurRadius.toPx()

    val bgPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
    }

    val darkInnerPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = darkShadowColor.toArgb()
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = elevationPx * 2f
        if (blurRadiusPx > 0f) {
            maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    val hasLightShadow = lightShadowColor != Color.Transparent && lightShadowColor.alpha > 0f
    val lightInnerPaint = if (hasLightShadow) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = lightShadowColor.toArgb()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = elevationPx * 2f
            if (blurRadiusPx > 0f) {
                maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
            }
        }
    } else null

    val clipPath = android.graphics.Path().apply {
        addRoundRect(
            0f, 0f, size.width, size.height,
            cornerRadiusPx, cornerRadiusPx,
            android.graphics.Path.Direction.CW
        )
    }

    onDrawWithContent {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                cornerRadiusPx,
                cornerRadiusPx,
                bgPaint
            )

            canvas.save()
            canvas.nativeCanvas.clipPath(clipPath)

            canvas.nativeCanvas.drawRoundRect(
                elevationPx,
                elevationPx,
                size.width + elevationPx,
                size.height + elevationPx,
                cornerRadiusPx,
                cornerRadiusPx,
                darkInnerPaint
            )

            if (lightInnerPaint != null) {
                canvas.nativeCanvas.drawRoundRect(
                    -elevationPx,
                    -elevationPx,
                    size.width - elevationPx,
                    size.height - elevationPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    lightInnerPaint
                )
            }

            canvas.restore()
        }

        drawContent()
    }
}

/**
 * Neumorphic Convex modifier: Linear gradient 145deg from light to dark.
 */
fun Modifier.neuConvex(
    cornerShape: Shape,
    isDark: Boolean? = null,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = composed {
    val dark = isDark ?: isSystemInDarkTheme()
    this.neuConvex(
        lightShadowColor = if (dark) NeuColorDarkLightShadow else NeuColorLightLightShadow,
        darkShadowColor = if (dark) NeuColorDarkDarkShadow else NeuColorLightDarkShadow,
        backgroundColor = if (dark) NeuColorDarkSurfaceCard else NeuColorLightSurface,
        lightGradientColor = if (dark) NeuColorDarkGradLight else NeuColorLightGradLight,
        darkGradientColor = if (dark) NeuColorDarkGradDark else NeuColorLightGradDark,
        cornerShape = cornerShape,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuConvex(
    isDark: Boolean? = null,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = composed {
    this.neuConvex(
        cornerShape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
        isDark = isDark,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuConvex(
    lightShadowColor: Color,
    darkShadowColor: Color,
    backgroundColor: Color,
    lightGradientColor: Color = Color.Unspecified,
    darkGradientColor: Color = Color.Unspecified,
    cornerRadius: Dp = 16.dp,
    cornerShape: Shape? = null,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = this.drawWithCache {
    val cornerRadiusPx = cornerShape?.toCornerRadiusPx(size, this) ?: cornerRadius.toPx()
    val elevationPx = elevation.toPx()
    val blurRadiusPx = blurRadius.toPx()

    val isDarkBg = backgroundColor.luminance() < 0.5f
    val resolvedLightGrad = if (lightGradientColor != Color.Unspecified) lightGradientColor
                            else if (isDarkBg) NeuColorDarkGradLight else NeuColorLightGradLight
    val resolvedDarkGrad = if (darkGradientColor != Color.Unspecified) darkGradientColor
                           else if (isDarkBg) NeuColorDarkGradDark else NeuColorLightGradDark

    val darkPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = darkShadowColor.toArgb()
        if (blurRadiusPx > 0f) {
            maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    val hasLightShadow = lightShadowColor != Color.Transparent && lightShadowColor.alpha > 0f
    val lightPaint = if (hasLightShadow) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = lightShadowColor.toArgb()
            if (blurRadiusPx > 0f) {
                maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
            }
        }
    } else null

    val baseBgPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
    }
    val gradientShader = android.graphics.LinearGradient(
        0f, 0f,
        size.width, size.height,
        resolvedLightGrad.toArgb(),
        resolvedDarkGrad.toArgb(),
        android.graphics.Shader.TileMode.CLAMP
    )
    val bgPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        shader = gradientShader
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRoundRect(
                elevationPx,
                elevationPx,
                size.width + elevationPx,
                size.height + elevationPx,
                cornerRadiusPx,
                cornerRadiusPx,
                darkPaint
            )

            if (lightPaint != null) {
                canvas.nativeCanvas.drawRoundRect(
                    -elevationPx,
                    -elevationPx,
                    size.width - elevationPx,
                    size.height - elevationPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    lightPaint
                )
            }

            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                cornerRadiusPx,
                cornerRadiusPx,
                bgPaint
            )
        }
    }
}

/**
 * Neumorphic Concave modifier: Linear gradient 145deg from dark to light.
 */
fun Modifier.neuConcave(
    cornerShape: Shape,
    isDark: Boolean? = null,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = composed {
    val dark = isDark ?: isSystemInDarkTheme()
    this.neuConcave(
        lightShadowColor = if (dark) NeuColorDarkLightShadow else NeuColorLightLightShadow,
        darkShadowColor = if (dark) NeuColorDarkDarkShadow else NeuColorLightDarkShadow,
        backgroundColor = if (dark) NeuColorDarkSurfaceCard else NeuColorLightSurface,
        lightGradientColor = if (dark) NeuColorDarkGradLight else NeuColorLightGradLight,
        darkGradientColor = if (dark) NeuColorDarkGradDark else NeuColorLightGradDark,
        cornerShape = cornerShape,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuConcave(
    isDark: Boolean? = null,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = composed {
    this.neuConcave(
        cornerShape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
        isDark = isDark,
        elevation = elevation,
        blurRadius = blurRadius
    )
}

fun Modifier.neuConcave(
    lightShadowColor: Color,
    darkShadowColor: Color,
    backgroundColor: Color,
    lightGradientColor: Color = Color.Unspecified,
    darkGradientColor: Color = Color.Unspecified,
    cornerRadius: Dp = 16.dp,
    cornerShape: Shape? = null,
    elevation: Dp = 6.dp,
    blurRadius: Dp = 10.dp
): Modifier = this.drawWithCache {
    val cornerRadiusPx = cornerShape?.toCornerRadiusPx(size, this) ?: cornerRadius.toPx()
    val elevationPx = elevation.toPx()
    val blurRadiusPx = blurRadius.toPx()

    val isDarkBg = backgroundColor.luminance() < 0.5f
    val resolvedLightGrad = if (lightGradientColor != Color.Unspecified) lightGradientColor
                            else if (isDarkBg) NeuColorDarkGradLight else NeuColorLightGradLight
    val resolvedDarkGrad = if (darkGradientColor != Color.Unspecified) darkGradientColor
                           else if (isDarkBg) NeuColorDarkGradDark else NeuColorLightGradDark

    val darkPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = darkShadowColor.toArgb()
        if (blurRadiusPx > 0f) {
            maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    val hasLightShadow = lightShadowColor != Color.Transparent && lightShadowColor.alpha > 0f
    val lightPaint = if (hasLightShadow) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = lightShadowColor.toArgb()
            if (blurRadiusPx > 0f) {
                maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
            }
        }
    } else null

    val baseBgPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
    }
    val gradientShader = android.graphics.LinearGradient(
        0f, 0f,
        size.width, size.height,
        resolvedDarkGrad.toArgb(),
        resolvedLightGrad.toArgb(),
        android.graphics.Shader.TileMode.CLAMP
    )
    val bgPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        shader = gradientShader
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRoundRect(
                elevationPx,
                elevationPx,
                size.width + elevationPx,
                size.height + elevationPx,
                cornerRadiusPx,
                cornerRadiusPx,
                darkPaint
            )

            if (lightPaint != null) {
                canvas.nativeCanvas.drawRoundRect(
                    -elevationPx,
                    -elevationPx,
                    size.width - elevationPx,
                    size.height - elevationPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    lightPaint
                )
            }

            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                cornerRadiusPx,
                cornerRadiusPx,
                bgPaint
            )
        }
    }
}
