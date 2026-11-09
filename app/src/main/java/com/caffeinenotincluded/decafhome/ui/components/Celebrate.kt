package com.caffeinenotincluded.decafhome.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caffeinenotincluded.decafhome.data.Celebration
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.Nunito
import kotlin.random.Random

private class Piece(
    val x: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val spin: Float,
    val color: Int,
)

/** A burst of brand-coloured confetti. Restarts whenever [key] changes. */
@Composable
fun Confetti(key: Any, modifier: Modifier = Modifier, pieces: Int = 70) {
    val progress = remember(key) { Animatable(0f) }
    val parts = remember(key) {
        val random = Random(key.hashCode())
        List(pieces) {
            Piece(
                x = random.nextFloat(),
                vx = (random.nextFloat() - 0.5f) * 0.6f,
                vy = -(0.6f + random.nextFloat() * 0.7f),
                size = 7f + random.nextFloat() * 9f,
                spin = (random.nextFloat() - 0.5f) * 900f,
                color = random.nextInt(Brand.Accents.size),
            )
        }
    }
    LaunchedEffect(key) { progress.animateTo(1f, tween(1800, easing = LinearEasing)) }
    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        parts.forEach { p ->
            val x = size.width * (p.x + p.vx * t)
            val y = size.height * (0.55f + p.vy * t + 1.5f * t * t)
            val pieceSize = p.size * density / 2.5f
            rotate(p.spin * t, Offset(x, y)) {
                drawRect(
                    color = Brand.Accents[p.color].copy(alpha = 1f - t * t),
                    topLeft = Offset(x - pieceSize / 2, y - pieceSize / 3),
                    size = Size(pieceSize, pieceSize * 0.6f),
                )
            }
        }
    }
}

/**
 * Confetti plus a little banner at the top. [celebration] is the latest one (kept
 * while the banner slides away), [visible] says whether to show it, and [serial]
 * restarts the confetti for each new celebration.
 */
@Composable
fun CelebrationLayer(celebration: Celebration?, visible: Boolean, serial: Int) {
    Box(Modifier.fillMaxSize()) {
        if (celebration != null && serial > 0) {
            Confetti(key = serial, pieces = if (celebration.newLevel != null) 120 else 60)
        }
        AnimatedVisibility(
            visible = visible && celebration != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 24.dp, end = 24.dp),
        ) {
            if (celebration != null) {
                val shape = RoundedCornerShape(16.dp)
                Column(
                    Modifier
                        .background(Brand.Sunshine, shape)
                        .border(2.dp, Brand.Ink, shape)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val level = celebration.newLevel
                    Text(
                        if (level != null) "Level $level!" else "+${celebration.points} XP",
                        fontFamily = Fredoka,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Brand.Ink,
                    )
                    Text(
                        celebration.message,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Brand.Ink,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
