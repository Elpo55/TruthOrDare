package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import kotlin.random.Random
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChallengeLevel
import com.example.data.ChallengeType
import com.example.ui.components.PlayerWheel
import com.example.viewmodel.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onOpenStats: () -> Unit,
    onBackToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players by viewModel.players.collectAsState()
    val turnCount by viewModel.turnCount.collectAsState()
    val activeIndex by viewModel.activePlayerIndex.collectAsState()
    val activePlayerName by viewModel.displayedPlayerName.collectAsState()

    val isWheelSpinning by viewModel.isWheelSpinning.collectAsState()
    val wheelTargetAngle by viewModel.wheelTargetAngle.collectAsState()

    val activeChallengeText by viewModel.activeChallengeText.collectAsState()
    val activeChallengeType by viewModel.activeChallengeType.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()

    val activeChaosEventText by viewModel.activeChaosEventText.collectAsState()

    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val isChaosMode by viewModel.isChaosModeActive.collectAsState()
    val isSuddenDeath by viewModel.isSuddenDeathActive.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToSetup,
                        modifier = Modifier
                            .background(CyberSurface, CircleShape)
                            .border(1.dp, CyberSurfaceElevated, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuration",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SESSION EN COURS • TOUR $turnCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Action ou Vérité ?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Pulsing Mode Pill
                    val levelColor = when (selectedLevel) {
                        ChallengeLevel.SOFT -> NeonTeal
                        ChallengeLevel.NORMAL -> NeonPink
                        ChallengeLevel.CHAOS -> NeonGold
                    }
                    val levelText = if (isChaosMode) "Mode Chaos" else selectedLevel.name

                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Row(
                        modifier = Modifier
                            .background(CyberSurface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .border(1.dp, CyberSurfaceElevated, RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .graphicsLayer(alpha = pulseAlpha)
                                .background(levelColor, CircleShape)
                        )
                        Text(
                            text = levelText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Leaderboard Screen Button
                    IconButton(
                        onClick = onOpenStats,
                        modifier = Modifier
                            .background(CyberSurface, CircleShape)
                            .border(1.dp, CyberSurfaceElevated, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Classements",
                            tint = NeonTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Central Ring Play Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Custom Wheel Animation
                    PlayerWheel(
                        players = players,
                        targetAngle = wheelTargetAngle,
                        isSpinning = isWheelSpinning,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isWheelSpinning) {
                        Text(
                            text = "Sélection du joueur...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal,
                            textAlign = TextAlign.Center
                        )
                    } else if (activeChallengeText == null && activeChaosEventText == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .drawBehind {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(NeonPurple.copy(alpha = 0.15f), Color.Transparent),
                                                center = this.center,
                                                radius = this.size.width / 2f
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "C'est au tour de",
                                        fontSize = 14.sp,
                                        color = TextSecondary,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = activePlayerName.uppercase(),
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp,
                                        style = androidx.compose.ui.text.TextStyle(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color.White, TextSecondary)
                                            )
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                                    )
                                    // Colored underbar
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(4.dp)
                                            .background(NeonPurple, RoundedCornerShape(2.dp))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Horizontal surrounding player slot roster matching HTML mockup!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Take up to 4 players surrounding the active index
                                players.take(4).forEach { player ->
                                    val isActive = player.name == activePlayerName
                                    Box(
                                        modifier = Modifier
                                            .size(width = 64.dp, height = 48.dp)
                                            .graphicsLayer(
                                                scaleX = if (isActive) 1.1f else 1.0f,
                                                scaleY = if (isActive) 1.1f else 1.0f
                                            )
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isActive) NeonPurple else CyberSurface)
                                            .border(
                                                width = 1.dp,
                                                color = if (isActive) NeonPurple.copy(alpha = 0.4f) else CyberSurfaceElevated,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = player.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color.White else TextMuted,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lower Gameplay Decision Board
            AnimatedContent(
                targetState = Triple(activeChallengeText, activeChaosEventText, isWheelSpinning),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                },
                label = "buttonsTransition"
            ) { (challenge, chaosEvent, wheelSpinning) ->
                if (wheelSpinning) {
                    Box(modifier = Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonPurple)
                    }
                } else if (chaosEvent != null) {
                    // CHAOS EVENT CARD INSTEAD OF NORMAL TURNS
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(3.dp, NeonGold, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Casino, contentDescription = "Chaos", tint = NeonGold, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ÉVÉNEMENT DE CHAOS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = chaosEvent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { viewModel.challengeRealized() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Événement exécuté !",
                                    color = CyberBg,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                } else if (challenge != null) {
                    // GAME COMPLETED CHALLENGE DISPLAY
                    val displayType = activeChallengeType ?: ChallengeType.ACTION
                    val cardBorderColor = if (displayType == ChallengeType.ACTION) NeonPink else NeonTeal
                    val titleText = if (displayType == ChallengeType.ACTION) "ACTION !" else "VÉRITÉ !"

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(3.dp, cardBorderColor, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = titleText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = cardBorderColor,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = challenge,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // PASS BUTTON (HIDDEN IF SUDDEN DEATH)
                                if (!isSuddenDeath) {
                                    Button(
                                        onClick = { viewModel.challengePassed() },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated),
                                        border = BorderStroke(1.dp, NeonRed),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                    ) {
                                        Text(
                                            text = "Passer",
                                            color = NeonRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                // SUCCESS COMPLETED BUTTON
                                Button(
                                    onClick = { viewModel.challengeRealized() },
                                    colors = ButtonDefaults.buttonColors(containerColor = cardBorderColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(54.dp)
                                ) {
                                    Text(
                                        text = "Défi réalisé",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // STANDBY : PLAYER SELECTION DECISION MODE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // VERITE BUTTON (TEAL OUTLINED SLATE SLEEK CARD)
                            Card(
                                onClick = { viewModel.selectChallenge(ChallengeType.VERITE) },
                                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                                shape = RoundedCornerShape(32.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(136.dp)
                                    .border(1.5.dp, CyberSurfaceElevated, RoundedCornerShape(32.dp)),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline, 
                                        contentDescription = null, 
                                        tint = NeonTeal, 
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "VÉRITÉ",
                                        color = NeonTeal,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }

                            // ACTION BUTTON (PURPLE TO INDIGO SOLID GRADIENT EMBOSS CARD)
                            Card(
                                onClick = { viewModel.selectChallenge(ChallengeType.ACTION) },
                                shape = RoundedCornerShape(32.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(136.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(NeonPurple, Color(0xFF4F46E5))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow, 
                                            contentDescription = null, 
                                            tint = Color.White, 
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "ACTION",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        )
                                    }
                                }
                            }
                        }

                        // SPIN BUTTON & AI GENERATOR BOOSTERS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // SPIN THE NAME WHEEL BUTTON (SLATE 900 SLEEK ACTION BAR)
                            Card(
                                onClick = { viewModel.spinWheelAndSelectPlayer() },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF13151B)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(54.dp)
                                    .border(1.dp, CyberSurfaceElevated, RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Casino, 
                                        contentDescription = null, 
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LANCER LA ROUE",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            // AI GENERATION BOOSTER BUTTON
                            Card(
                                onClick = {
                                    val randomType = if (Random.nextBoolean()) ChallengeType.ACTION else ChallengeType.VERITE
                                    viewModel.selectChallenge(randomType)
                                },
                                enabled = !isAILoading,
                                colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(54.dp)
                                    .border(1.5.dp, NeonGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAILoading) {
                                        CircularProgressIndicator(color = NeonGold, modifier = Modifier.size(20.dp))
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star, 
                                                contentDescription = "IA", 
                                                tint = NeonGold, 
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Gage IA",
                                                color = NeonGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
