package org.spotube.spotube.core.ui.spotify

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Spotify-like dark palette. */
object Sp {
    val Green = Color(0xFF1ED760)
    val GreenDark = Color(0xFF1DB954)
    val Black = Color(0xFF000000)
    val Base = Color(0xFF121212)
    val Elevated = Color(0xFF1F1F1F)
    val Highlight = Color(0xFF2A2A2A)
    val Press = Color(0xFF333333)
    val Text = Color(0xFFFFFFFF)
    val SubText = Color(0xFFB3B3B3)
    val Faint = Color(0xFF7A7A7A)
    val Divider = Color(0x1FFFFFFF)
    val DefaultTint = Color(0xFF535353)
}

/* ---------------------------------------------------------------- Icons */

private fun icon(name: String, vararg paths: String): ImageVector {
    val b = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )
    paths.forEach { b.addPath(pathData = addPathNodes(it), fill = SolidColor(Color.Black)) }
    return b.build()
}

object SpIcons {
    val HomeFilled by lazy { icon("home_f", "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z") }
    val Home by lazy {
        icon("home", "M12,5.69l5,4.5V18h-2v-6H9v6H7v-7.81l5,-4.5M12,3L2,12h3v8h6v-6h2v6h6v-8h3L12,3z")
    }
    val Search by lazy {
        icon(
            "search",
            "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"
        )
    }
    val SearchBold by lazy {
        icon(
            "search_b",
            "M15.9,14.1h-0.6l-0.25,-0.23C16,12.7 16.6,11.2 16.6,9.6 16.6,5.73 13.47,2.6 9.6,2.6S2.6,5.73 2.6,9.6 5.73,16.6 9.6,16.6c1.6,0 3.1,-0.6 4.27,-1.55l0.23,0.25v0.6l5.3,5.3 2.1,-2.1 -5.3,-5.3zM9.6,13.9C7.22,13.9 5.3,11.98 5.3,9.6S7.22,5.3 9.6,5.3 13.9,7.22 13.9,9.6 11.98,13.9 9.6,13.9z"
        )
    }
    val Library by lazy {
        icon("library", "M3,21V3h2v18H3zM8,21V3h2v18H8zM13.3,3.7l1.86,-0.73 6.6,16.9 -1.86,0.73z")
    }
    val LibraryFilled by lazy {
        icon("library_f", "M2.5,21V3h3v18h-3zM7.5,21V3h3v18h-3zM12.8,4l2.8,-1.1 6.9,17.6 -2.8,1.1z")
    }
    val Settings by lazy {
        icon(
            "settings",
            "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"
        )
    }
    val Play by lazy {
        icon("play", "M8,5.14v13.72c0,0.79 0.87,1.27 1.54,0.84l10.78,-6.86c0.62,-0.39 0.62,-1.29 0,-1.69L9.54,4.3C8.87,3.87 8,4.35 8,5.14z")
    }
    val Pause by lazy {
        icon(
            "pause",
            "M7,19h3c0.55,0 1,-0.45 1,-1V6c0,-0.55 -0.45,-1 -1,-1H7c-0.55,0 -1,0.45 -1,1v12c0,0.55 0.45,1 1,1zM14,6v12c0,0.55 0.45,1 1,1h3c0.55,0 1,-0.45 1,-1V6c0,-0.55 -0.45,-1 -1,-1h-3c-0.55,0 -1,0.45 -1,1z"
        )
    }
    val Next by lazy { icon("next", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6h-2z") }
    val Previous by lazy { icon("prev", "M6,6h2v12H6zM9.5,12l8.5,6V6z") }
    val Shuffle by lazy {
        icon(
            "shuffle",
            "M10.59,9.17L5.41,4L4,5.41l5.17,5.17 1.42,-1.41zM14.5,4l2.04,2.04L4,18.59 5.41,20 17.96,7.46 20,9.5L20,4h-5.5zM14.83,13.41l-1.41,1.41 3.13,3.13L14.5,20L20,20v-5.5l-2.04,2.04 -3.13,-3.13z"
        )
    }
    val Repeat by lazy {
        icon("repeat", "M7,7h10v3l4,-4 -4,-4v3L5,5v6h2L7,7zM17,17L7,17v-3l-4,4 4,4v-3h12v-6h-2v4z")
    }
    val RepeatOne by lazy {
        icon(
            "repeat_one",
            "M7,7h10v3l4,-4 -4,-4v3L5,5v6h2L7,7zM17,17L7,17v-3l-4,4 4,4v-3h12v-6h-2v4zM13,15L13,9h-1l-2,1v1h1.5v4L13,15z"
        )
    }
    val AddCircle by lazy {
        icon(
            "add_circle",
            "M13,7h-2v4H7v2h4v4h2v-4h4v-2h-4V7zM12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8z"
        )
    }
    val CheckCircle by lazy {
        icon(
            "check_circle",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z"
        )
    }
    val ChevronDown by lazy {
        icon("chevron_down", "M7.41,8.59L12,13.17l4.59,-4.58L18,10l-6,6 -6,-6 1.41,-1.41z")
    }
    val ChevronLeft by lazy {
        icon("chevron_left", "M15.41,7.41L14,6l-6,6 6,6 1.41,-1.41L10.83,12z")
    }
    val More by lazy {
        icon(
            "more",
            "M6,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM18,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z"
        )
    }
    val Queue by lazy {
        icon(
            "queue",
            "M15,6H3v2h12V6zM15,10H3v2h12v-2zM3,16h8v-2H3v2zM17,6v8.18C16.69,14.07 16.35,14 16,14c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3V8h3V6h-5z"
        )
    }
    val Devices by lazy {
        icon(
            "devices",
            "M4,6h18V4H4c-1.1,0 -2,0.9 -2,2v11H0v3h14v-3H4V6zM23,8h-6c-0.55,0 -1,0.45 -1,1v10c0,0.55 0.45,1 1,1h6c0.55,0 1,-0.45 1,-1V9c0,-0.55 -0.45,-1 -1,-1zM22,17h-4v-7h4v7z"
        )
    }
    val Lyrics by lazy {
        icon(
            "lyrics",
            "M12,14c1.66,0 2.99,-1.34 2.99,-3L15,5c0,-1.66 -1.34,-3 -3,-3S9,3.34 9,5v6c0,1.66 1.34,3 3,3zM17.3,11c0,3 -2.54,5.1 -5.3,5.1S6.7,14 6.7,11H5c0,3.41 2.72,6.23 6,6.72V21h2v-3.28c3.28,-0.48 6,-3.3 6,-6.72h-1.7z"
        )
    }
    val Timer by lazy {
        icon(
            "timer",
            "M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z"
        )
    }
    val Download by lazy {
        icon(
            "download",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM13,11V7h-2v4H8l4,4 4,-4h-3z"
        )
    }
    val Album by lazy {
        icon(
            "album",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,16.5c-2.49,0 -4.5,-2.01 -4.5,-4.5S9.51,7.5 12,7.5s4.5,2.01 4.5,4.5 -2.01,4.5 -4.5,4.5zM12,11c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1z"
        )
    }
    val Swap by lazy {
        icon("swap", "M6.99,11L3,15l3.99,4v-3H14v-2H6.99v-3zM21,9l-3.99,-4v3H10v2h7.01v3L21,9z")
    }
    val Info by lazy {
        icon(
            "info",
            "M11,7h2v2h-2zM11,11h2v6h-2zM12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8z"
        )
    }
    val Speaker by lazy {
        icon(
            "speaker",
            "M17,2H7c-1.1,0 -1.99,0.9 -1.99,2L5,20c0,1.1 0.9,2 2,2h10c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2zM12,4c1.1,0 2,0.9 2,2s-0.9,2 -2,2c-1.11,0 -2,-0.9 -2,-2s0.89,-2 2,-2zM12,20c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zM12,12c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z"
        )
    }
    val Plus by lazy {
        icon("plus", "M12.9,11.1V3.5h-1.8v7.6H3.5v1.8h7.6v7.6h1.8v-7.6h7.6v-1.8z")
    }
    val Person by lazy {
        icon(
            "person",
            "M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z"
        )
    }
    val Share by lazy {
        icon("share", "M16,5l-1.42,1.42 -1.59,-1.59V16h-1.98V4.83L9.42,6.42 8,5l4,-4 4,4zM20,10v11c0,1.1 -0.9,2 -2,2H6c-1.11,0 -2,-0.9 -2,-2V10c0,-1.11 0.89,-2 2,-2h3v2H6v11h12V10h-3V8h3c1.1,0 2,0.89 2,2z")
    }
    val ArrowDown by lazy {
        icon("arrow_down", "M10.5,3h3v11.2l4.4,-4.4 2.1,2.1L12,19.9 4,11.9l2.1,-2.1 4.4,4.4z")
    }
    val Music by lazy {
        icon(
            "music",
            "M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z"
        )
    }
}

/** Tinted icon without any Millennium chrome. */
@Composable
fun SpIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Sp.Text,
    size: Dp = 24.dp
) {
    androidx.compose.foundation.Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint)
    )
}

/* -------------------------------------------------------- Interaction */

/** Spotify-style press: shrinks a little with a springy release, no ripple. */
fun Modifier.bouncyClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.94f,
    role: Role = Role.Button,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "bouncy"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
}

/** Plain icon button: transparent, bouncy, 48dp touch target. */
@Composable
fun SpIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Sp.Text,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
    touchSize: Dp = 44.dp
) {
    val animatedTint by animateColorAsState(
        if (enabled) tint else tint.copy(alpha = 0.35f), tween(180), label = "tint"
    )
    Box(
        modifier = modifier
            .size(touchSize)
            .clip(CircleShape)
            .bouncyClick(enabled = enabled, pressedScale = 0.85f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SpIcon(imageVector, contentDescription, tint = animatedTint, size = iconSize)
    }
}

/** Round play/pause button: green with a black glyph (or any colour). */
@Composable
fun SpPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    background: Color = Sp.Green,
    glyph: Color = Sp.Black,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (enabled) background else background.copy(alpha = 0.5f))
            .bouncyClick(enabled = enabled, pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SpIcon(
            imageVector = if (isPlaying) SpIcons.Pause else SpIcons.Play,
            contentDescription = contentDescription,
            tint = glyph,
            size = size * 0.5f
        )
    }
}

/* ------------------------------------------------------------- Slider */

/**
 * Thin Spotify seek bar. The thumb appears and the track thickens while it is being dragged.
 */
@Composable
fun SpSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = Sp.Text,
    inactiveColor: Color = Color.White.copy(alpha = 0.24f)
) {
    var dragging by remember { mutableStateOf(false) }
    val onChange by rememberUpdatedState(onValueChange)
    val onFinished by rememberUpdatedState(onValueChangeFinished)
    val thumb by animateFloatAsState(if (dragging) 1f else 0f, tween(140), label = "thumb")
    val activeTint by animateColorAsState(if (dragging) Sp.Green else activeColor, tween(140), label = "active")
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointer = down.id
                    dragging = true
                    onChange((down.position.x / size.width).coerceIn(0f, 1f))
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointer } ?: break
                        if (!change.pressed) break
                        onChange((change.position.x / size.width).coerceIn(0f, 1f))
                        change.consume()
                    }
                    dragging = false
                    onFinished()
                }
            }
    ) {
        val h = 4.dp.toPx() * (if (dragging) 1.25f else 1f)
        val cy = size.height / 2f
        val v = value.coerceIn(0f, 1f)
        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(0f, cy - h / 2f),
            size = Size(size.width, h),
            cornerRadius = CornerRadius(h / 2f)
        )
        drawRoundRect(
            color = activeTint,
            topLeft = Offset(0f, cy - h / 2f),
            size = Size(size.width * v, h),
            cornerRadius = CornerRadius(h / 2f)
        )
        drawCircle(
            color = Sp.Text,
            radius = 6.dp.toPx() * thumb,
            center = Offset(size.width * v, cy)
        )
    }
}

/* ------------------------------------------------------ Dominant colour */

private val dominantCache = LinkedHashMap<String, Color>()

/**
 * Picks the most characteristic colour of an artwork and darkens it so white text stays readable,
 * like Spotify's player and playlist headers. Animates between artworks.
 */
@Composable
fun rememberArtworkColor(model: Any?, fallback: Color = Sp.DefaultTint): State<Color> {
    val context = LocalPlatformContext.current
    val key = model?.toString()
    var target by remember { mutableStateOf(key?.let { dominantCache[it] } ?: fallback) }
    LaunchedEffect(key) {
        if (key == null) {
            target = fallback
            return@LaunchedEffect
        }
        dominantCache[key]?.let { target = it; return@LaunchedEffect }
        val color = runCatching {
            val loader = SingletonImageLoader.get(context)
            val request = ImageRequest.Builder(context).data(model).size(64).build()
            val result = loader.execute(request)
            val image = (result as? SuccessResult)?.image ?: return@runCatching null
            val painter = image.asPainter(context)
            withContext(Dispatchers.Default) {
                val w = 24
                val h = 24
                val bitmap = ImageBitmap(w, h)
                val canvas = GraphicsCanvas(bitmap)
                CanvasDrawScope().draw(
                    Density(1f),
                    LayoutDirection.Ltr,
                    canvas,
                    Size(w.toFloat(), h.toFloat())
                ) {
                    with(painter) { draw(Size(w.toFloat(), h.toFloat())) }
                }
                val pixels = IntArray(w * h)
                bitmap.readPixels(pixels)
                pickDominant(pixels)
            }
        }.getOrNull()
        if (color != null) {
            if (dominantCache.size > 200) dominantCache.remove(dominantCache.keys.first())
            dominantCache[key] = color
            target = color
        } else {
            target = fallback
        }
    }
    return animateColorAsState(target, tween(600), label = "artwork")
}

private fun pickDominant(pixels: IntArray): Color? {
    // 12 hue buckets + 1 grey bucket, weighted towards saturated mid-tones.
    val weight = FloatArray(13)
    val rs = FloatArray(13)
    val gs = FloatArray(13)
    val bs = FloatArray(13)
    for (p in pixels) {
        val a = (p ushr 24) and 0xFF
        if (a < 128) continue
        val r = ((p shr 16) and 0xFF) / 255f
        val g = ((p shr 8) and 0xFF) / 255f
        val b = (p and 0xFF) / 255f
        val mx = max(r, max(g, b))
        val mn = min(r, min(g, b))
        val l = (mx + mn) / 2f
        val d = mx - mn
        val s = if (d == 0f) 0f else d / (1f - abs(2f * l - 1f)).coerceAtLeast(0.0001f)
        if (l < 0.06f || l > 0.96f) continue
        val bucket: Int
        val w: Float
        if (s < 0.18f) {
            bucket = 12
            w = 0.35f
        } else {
            val hue = when (mx) {
                r -> ((g - b) / d).let { if (it < 0) it + 6f else it }
                g -> (b - r) / d + 2f
                else -> (r - g) / d + 4f
            } * 60f
            bucket = ((hue / 30f).toInt()).coerceIn(0, 11)
            w = 0.4f + s * (1f - abs(l - 0.5f) * 1.4f).coerceAtLeast(0.1f)
        }
        weight[bucket] += w
        rs[bucket] += r * w
        gs[bucket] += g * w
        bs[bucket] += b * w
    }
    var best = -1
    for (i in 0 until 13) if (weight[i] > 0f && (best == -1 || weight[i] > weight[best])) best = i
    if (best == -1) return null
    val r = rs[best] / weight[best]
    val g = gs[best] / weight[best]
    val b = bs[best] / weight[best]
    return tame(Color(r, g, b))
}

/** Keeps the hue but clamps brightness/saturation into Spotify's header range. */
private fun tame(c: Color): Color {
    val mx = max(c.red, max(c.green, c.blue))
    val mn = min(c.red, min(c.green, c.blue))
    if (mx <= 0f) return Sp.DefaultTint
    val targetMax = mx.coerceIn(0.32f, 0.62f)
    val scale = targetMax / mx
    var r = c.red * scale
    var g = c.green * scale
    var b = c.blue * scale
    // Soften neon colours a touch.
    val sat = if (mx == 0f) 0f else (mx - mn) / mx
    if (sat > 0.85f) {
        val k = 0.85f / sat
        val m = targetMax
        r = m - (m - r) * k
        g = m - (m - g) * k
        b = m - (m - b) * k
    }
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
}

/** Mixes a colour towards black. */
fun Color.darken(fraction: Float): Color = Color(
    red = red * (1f - fraction),
    green = green * (1f - fraction),
    blue = blue * (1f - fraction),
    alpha = alpha
)

/** Spotify filter pill: green when selected. */
@Composable
fun SpChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(if (selected) Sp.Green else Color.White.copy(alpha = 0.07f), tween(200), label = "chip")
    val fg by animateColorAsState(if (selected) Sp.Black else Sp.Text, tween(200), label = "chipText")
    val edge by animateColorAsState(if (selected) Sp.Green else Color.White.copy(alpha = 0.14f), tween(200), label = "chipEdge")
    Box(
        modifier = modifier
            .height(33.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(bg)
            .border(0.8.dp, edge, RoundedCornerShape(17.dp))
            .bouncyClick(pressedScale = 0.92f, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        com.composeunstyled.Text(
            text = text,
            style = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
            color = fg,
            maxLines = 1
        )
    }
}


/** Blur source behind the floating glass chrome (tab bar + mini player) on phones. */
val LocalGlassHaze = staticCompositionLocalOf<HazeState?> { null }

/**
 * iOS 26-style "liquid glass": blurs what scrolls behind, tints it, and adds a light rim and a
 * soft top sheen. Falls back to a translucent fill when no blur source is available.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color = Color.Black.copy(alpha = 0.45f),
    blur: Dp = 24.dp,
    shadow: Boolean = true
): Modifier {
    val haze = LocalGlassHaze.current
    var m = this
    if (shadow) m = m.shadow(16.dp, shape, ambientColor = Color.Black.copy(alpha = 0.6f), spotColor = Color.Black.copy(alpha = 0.6f))
    m = m.clip(shape)
    m = if (haze != null) {
        m.hazeEffect(state = haze) {
            blurEffect {
                blurRadius = blur
                colorEffects = listOf(HazeColorEffect.tint(tint))
            }
        }
    } else {
        m.background(tint.copy(alpha = (tint.alpha + 0.4f).coerceAtMost(0.95f)))
    }
    return m
        .background(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.10f),
                0.45f to Color.White.copy(alpha = 0.02f),
                1f to Color.Transparent
            )
        )
        .border(
            width = 0.8.dp,
            brush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.32f),
                0.5f to Color.White.copy(alpha = 0.08f),
                1f to Color.White.copy(alpha = 0.16f)
            ),
            shape = shape
        )
}
