package com.knowledgepearls.app.ui.shell

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.AppBrand
import com.knowledgepearls.app.R
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AccentCyan = Color(0xFF2ED6FA)
private val AccentAmber = Color(0xFFFF9E38)
private val AccentViolet = Color(0xFF8C6BF2)

private const val SplashDurationMs = 2000
private const val PearlCount = 20

private val MedicalSymbols = listOf(
    Icons.Filled.MedicalServices,
    Icons.Filled.MonitorHeart,
    Icons.Filled.Favorite,
    Icons.Filled.Medication,
    Icons.Filled.Vaccines,
    Icons.Filled.Healing,
    Icons.Filled.Emergency,
    Icons.Filled.LocalHospital,
)

@Composable
fun LaunchSplashScreen(onFinished: () -> Unit) {
    val darkTheme = isPearlDarkTheme()
    var progress by remember { mutableFloatStateOf(0f) }
    var litPearls by remember { mutableIntStateOf(0) }
    var iconScale by remember { mutableFloatStateOf(0.55f) }
    var iconOpacity by remember { mutableFloatStateOf(0f) }
    var contentOpacity by remember { mutableFloatStateOf(0f) }

    val animatedIconScale by animateFloatAsState(
        targetValue = iconScale,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 280f),
        label = "iconScale",
    )
    val animatedIconOpacity by animateFloatAsState(
        targetValue = iconOpacity,
        animationSpec = tween(750),
        label = "iconOpacity",
    )
    val animatedContentOpacity by animateFloatAsState(
        targetValue = contentOpacity,
        animationSpec = tween(550, delayMillis = 200),
        label = "contentOpacity",
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(SplashDurationMs, delayMillis = 250, easing = FastOutSlowInEasing),
        label = "progress",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )

    LaunchedEffect(Unit) {
        iconScale = 1f
        iconOpacity = 1f
        delay(200)
        contentOpacity = 1f
        delay(50)
        progress = 1f

        // Match iOS: pearl beads light on a staggered schedule in parallel, not sequentially.
        val pearlInterval = SplashDurationMs.toFloat() / PearlCount
        repeat(PearlCount) { index ->
            launch {
                delay(250L + (pearlInterval * index).toLong())
                litPearls = index + 1
            }
        }

        delay(SplashDurationMs + 300L)
        onFinished()
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = TabTheme.Feed, intensity = 1.15f)

        AmbientParticles(contentOpacity = animatedContentOpacity)

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PearlIconComplex(
                iconScale = animatedIconScale,
                iconOpacity = animatedIconOpacity,
                contentOpacity = animatedContentOpacity,
                glowPulse = glowPulse,
                litPearls = litPearls,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            SplashLoadingBar(
                progress = animatedProgress,
                opacity = animatedContentOpacity,
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .alpha(animatedContentOpacity)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = PearlColors.heroSecondary(darkTheme),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PearlIconComplex(
    iconScale: Float,
    iconOpacity: Float,
    contentOpacity: Float,
    glowPulse: Float,
    litPearls: Int,
) {
    val iconSize = 167.dp
    val ringRadius = 121.dp
    val pearlSize = 18.dp
    val containerSize = iconSize + ringRadius * 2 + pearlSize

    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size((ringRadius + iconSize * 0.2f) * 2)
                .scale(glowPulse)
                .blur(8.dp)
                .alpha(iconOpacity)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentViolet.copy(alpha = 0.28f),
                            AccentCyan.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                        radius = 220f,
                    ),
                    shape = CircleShape,
                ),
        )

        PearlRing(
            litPearls = litPearls,
            ringRadius = ringRadius,
            pearlSize = pearlSize,
            opacity = contentOpacity,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppLogoMark(
                iconSize = iconSize,
                scale = iconScale,
                opacity = iconOpacity,
            )
            SplashBrandTitle(opacity = contentOpacity)
        }
    }
}

@Composable
private fun AppLogoMark(
    iconSize: androidx.compose.ui.unit.Dp,
    scale: Float,
    opacity: Float,
) {
    Image(
        painter = painterResource(R.drawable.app_logo),
        contentDescription = AppBrand.NAME,
        modifier = Modifier
            .size(iconSize)
            .scale(scale)
            .alpha(opacity),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun SplashBrandTitle(
    opacity: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = AppBrand.NAME,
        modifier = modifier.alpha(opacity),
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            brush = Brush.linearGradient(
                colors = listOf(AccentViolet, AccentCyan, AccentAmber),
            ),
        ),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PearlRing(
    litPearls: Int,
    ringRadius: androidx.compose.ui.unit.Dp,
    pearlSize: androidx.compose.ui.unit.Dp,
    opacity: Float,
) {
    Box(
        modifier = Modifier
            .size(ringRadius * 2 + pearlSize)
            .alpha(opacity),
        contentAlignment = Alignment.Center,
    ) {
        repeat(PearlCount) { index ->
            val filled = index < litPearls
            val angle = (index.toDouble() / PearlCount) * 360.0 - 90.0
            val accent = when {
                index % 3 == 0 -> AccentAmber
                index % 2 == 0 -> AccentCyan
                else -> AccentViolet
            }
            val symbol = MedicalSymbols[index % MedicalSymbols.size]
            val radians = Math.toRadians(angle)
            val x = (cos(radians) * ringRadius.value).toFloat().dp
            val y = (sin(radians) * ringRadius.value).toFloat().dp

            SplashRingPearl(
                symbol = symbol,
                filled = filled,
                accent = accent,
                size = pearlSize,
                modifier = Modifier.offset(x = x, y = y),
            )
        }
    }
}

@Composable
private fun SplashRingPearl(
    symbol: ImageVector,
    filled: Boolean,
    accent: Color,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (filled) 1f else 0.22f)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = if (filled) 0.55f else 0.22f),
                        accent.copy(alpha = if (filled) 0.28f else 0.10f),
                    ),
                ),
            )
            .border(
                width = 0.6.dp,
                color = Color.White.copy(alpha = if (filled) 0.42f else 0.10f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = symbol,
            contentDescription = null,
            tint = if (filled) Color.White else accent.copy(alpha = 0.72f),
            modifier = Modifier
                .size(size * 0.5f)
                .alpha(if (filled) 1f else 0.65f),
        )
    }
}

@Composable
private fun SplashLoadingBar(
    progress: Float,
    opacity: Float,
) {
    val darkTheme = isPearlDarkTheme()

    Box(
        modifier = Modifier
            .size(width = 245.dp, height = 6.dp)
            .alpha(opacity)
            .clip(RoundedCornerShape(999.dp))
            .background(PearlColors.mutedTileBackground(darkTheme)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0.02f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(AccentViolet, AccentCyan, AccentAmber),
                    ),
                ),
        )
    }
}

@Composable
private fun AmbientParticles(contentOpacity: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(contentOpacity),
        contentAlignment = Alignment.Center,
    ) {
        repeat(16) { index ->
            val angle = index / 16.0 * Math.PI * 2
            val radius = 150.0 + (index % 5) * 30.0
            val color = when {
                index % 3 == 0 -> AccentViolet
                index % 2 == 0 -> AccentCyan
                else -> AccentAmber
            }
            val particleSize = (2 + index % 3).dp
            val particleAlpha = (0.12f + (index % 4) * 0.055f)

            Box(
                modifier = Modifier
                    .offset(
                        x = (cos(angle) * radius).toFloat().dp,
                        y = (sin(angle) * radius).toFloat().dp,
                    )
                    .size(particleSize)
                    .alpha(particleAlpha)
                    .background(color, CircleShape),
            )
        }
    }
}
