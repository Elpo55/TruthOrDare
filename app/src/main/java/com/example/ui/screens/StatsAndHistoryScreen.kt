package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.HistoryEntryEntity
import com.example.data.database.PlayerEntity
import com.example.viewmodel.GameViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAndHistoryScreen(
    viewModel: GameViewModel,
    onBackToGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players by viewModel.players.collectAsState()
    val history by viewModel.history.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = CLASSEMENTS, 1 = HISTORIQUE

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackToGame,
                modifier = Modifier
                    .background(CyberSurface, CircleShape)
                    .border(1.dp, CyberSurfaceElevated, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "STATS & HISTORIQUE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Résultats de Soirée",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }
            // Empty spacer for balancing
            Spacer(modifier = Modifier.size(40.dp))
        }

        // Tab Row Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .background(CyberSurface, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("CLASSEMENTS", "HISTORIQUE")
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonPurple else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        // Tab Content Display Workspace
        if (selectedTab == 0) {
            // CLASSEMENTS TAB
            // Process Leaders / Rankings
            val maxActions = players.maxOfOrNull { it.actionsCompleted } ?: 0
            val maxTruths = players.maxOfOrNull { it.truthsAnswered } ?: 0
            val maxChaosPoints = players.maxOfOrNull { (it.actionsCompleted * 2) + it.truthsAnswered } ?: 0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (players.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aucun joueur enregistré actuellement.",
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Sorting players by overall chaotic points
                    val rankedPlayers = players.sortedByDescending { (it.actionsCompleted * 2) + it.truthsAnswered }

                    items(rankedPlayers) { player ->
                        // Determine Custom Rankings / Humorous Titles
                        val isRoiCourage = player.actionsCompleted > 0 && player.actionsCompleted == maxActions
                        val isMaitreMensonges = player.truthsAnswered > 0 && player.truthsAnswered == maxTruths
                        val isSurvivantChaos = ((player.actionsCompleted * 2) + player.truthsAnswered) > 0 && 
                                ((player.actionsCompleted * 2) + player.truthsAnswered) == maxChaosPoints

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSurvivantChaos) NeonGold else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = player.name,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )

                                        // Humorous Title Badge Chips
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (isSurvivantChaos) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(NeonGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("🌀 Survivant du Chaos", color = NeonGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            if (isRoiCourage) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(NeonPink.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("🏆 Roi du Courage", color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            if (isMaitreMensonges) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(NeonTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("🤥 Maître des Mensonges", color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            if (!isSurvivantChaos && !isRoiCourage && !isMaitreMensonges) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(TextMuted.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("🤫 Observateur", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    // Display Total Action / Truth Score Ring
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "SCORE",
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${(player.actionsCompleted * 2) + player.truthsAnswered} pts",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonPurple
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Breakdown Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Actions details
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(CyberSurfaceElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${player.actionsCompleted} Actions",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Truths details
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(CyberSurfaceElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${player.truthsAnswered} Vérités",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Passes details
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(CyberSurfaceElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${player.passesUsed} Passes",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reset stats button at lists bottom
                    item {
                        Button(
                            onClick = { viewModel.resetStatistics() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Réinitialiser les scores", tint = NeonRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Réinitialiser tous les scores", color = NeonRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // HISTORIQUE TAB
            val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aucun tour joué dans cette partie.",
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(history) { entry ->
                        val itemColor = when (entry.type) {
                            "ACTION" -> NeonPink
                            "VERITE" -> NeonTeal
                            else -> NeonGold
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(itemColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = entry.playerName,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${entry.type})",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Status Badge (Realize or Fail/Pass)
                                    val isRealized = entry.result == "REALISE"
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isRealized) NeonTeal.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isRealized) "Réalisé" else "Passé",
                                            color = if (isRealized) NeonTeal else NeonRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = entry.content,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = dateFormat.format(Date(entry.timestamp)),
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    // Clear logs button
                    item {
                        Button(
                            onClick = { viewModel.cleanHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Effacer l'historique", tint = NeonRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Effacer tout l'historique", color = NeonRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
