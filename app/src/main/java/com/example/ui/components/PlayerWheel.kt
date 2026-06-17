package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PlayerEntity
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PlayerWheel(
    players: List<PlayerEntity>,
    targetAngle: Float,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    // Elegant slow-down rotation animation
    val rotationAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = if (isSpinning) {
            // Rapid rotation deceleration curve
            tween(
                durationMillis = 4000,
                easing = EaseOutCubic
            )
        } else {
            snap()
        },
        label = "wheelRotation"
    )

    // Segment colors for up to 10 players
    val segmentColors = listOf(
        NeonPurple,
        NeonPink,
        NeonTeal,
        NeonGold,
        Color(0xFF84CC16), // Lime Green
        Color(0xFFEC4899), // Hot Pink
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        NeonRed,
        Color(0xFFA855F7)  // Purple
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Core Wheel Area
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(4.dp, TextPrimary.copy(alpha = 0.8f), CircleShape)
                .rotate(rotationAngle),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = canvasWidth / 2f
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                if (players.isEmpty()) {
                    // Empty state fallback visual
                    drawCircle(
                        color = CyberSurfaceElevated,
                        radius = radius,
                        center = center
                    )
                    return@Canvas
                }

                val playerCount = players.size
                val sweepAngle = 360f / playerCount

                // Draw slices
                for (i in 0 until playerCount) {
                    val startAngle = i * sweepAngle
                    val color = segmentColors[i % segmentColors.size]

                    drawArc(
                        color = color.copy(alpha = 0.85f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = Size(canvasWidth, canvasHeight)
                    )

                    // Draw separator lines
                    val angleRad = (startAngle * PI / 180f)
                    val endX = center.x + radius * cos(angleRad).toFloat()
                    val endY = center.y + radius * sin(angleRad).toFloat()
                    drawLine(
                        color = CyberBg,
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )
                }

                // Draw central cyber glowing circle
                drawCircle(
                    color = CyberBg,
                    radius = radius * 0.22f,
                    center = center
                )
                drawCircle(
                    color = TextPrimary,
                    radius = radius * 0.05f,
                    center = center
                )

                // Write Player Names along segments (using native canvas for rotated typography)
                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = radius * 0.09f
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }

                    for (i in 0 until playerCount) {
                        val player = players[i]
                        val midAngleDeg = (i * sweepAngle) + (sweepAngle / 2f)
                        val angleRad = (midAngleDeg * PI / 180f)

                        // Save state, rotate canvas to write name horizontally within the segment
                        nativeCanvas.save()
                        nativeCanvas.rotate(midAngleDeg, center.x, center.y)

                        // Shorten names to prevent clipping on small screens
                        val displayName = if (player.name.length > 7) {
                            player.name.take(6) + ".."
                        } else {
                            player.name
                        }

                        // Coordinates relative to rotated canvas
                        nativeCanvas.drawText(
                            displayName,
                            center.x + radius * 0.88f,
                            center.y + (paint.textSize / 3f),
                            paint
                        )
                        nativeCanvas.restore()
                    }
                }
            }
        }

        // Animated neon core pulsing circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(CyberBg, CircleShape)
                .border(2.dp, NeonTeal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(NeonTeal, CircleShape)
            )
        }

        // Triumphant Indicator Pointer at the top (pointing down)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Pointeur",
                tint = NeonTeal,
                modifier = Modifier
                    .size(36.dp)
                    .rotate(180f) // Point down to highlight top-most segment
                    .background(CyberBg.copy(alpha = 0.8f), CircleShape)
                    .padding(2.dp)
            )
        }
    }
}
