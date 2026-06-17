package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChallengeLevel
import com.example.data.ChallengeType
import com.example.viewmodel.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players by viewModel.players.collectAsState()
    val customChallenges by viewModel.customChallenges.collectAsState()
    val focusManager = LocalFocusManager.current

    var newPlayerName by remember { mutableStateOf("") }
    var soundEnabled by remember { mutableStateOf(viewModel.isSoundEnabled()) }

    // Custom Challenge Inputs
    var isAddingCustom by remember { mutableStateOf(false) }
    var customType by remember { mutableStateOf(ChallengeType.ACTION) }
    var customLevel by remember { mutableStateOf(ChallengeLevel.NORMAL) }
    var customContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero / Main Title Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACTION OU VÉRITÉ ?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFC0C5D0))
                        )
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "AIDEZ SÉLECTIONNER VOS JOUEURS",
                    fontSize = 11.sp,
                    color = NeonTeal,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Global Settings & Sound Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Son",
                            tint = NeonPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Effets Sonores",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Bruitages rétro synthétisés",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            viewModel.setSoundEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonPurple,
                            uncheckedTrackColor = CyberBg
                        )
                    )
                }
            }
        }

        // Players Section (2 to 10 players)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Joueurs (${players.size}/10)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Add Player Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = { if (it.length <= 15) newPlayerName = it },
                            placeholder = { Text("Prénom du joueur", color = TextMuted, fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = CyberSurfaceElevated,
                                focusedContainerColor = Color(0xFF13151B),
                                unfocusedContainerColor = Color(0xFF13151B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (newPlayerName.trim().isNotEmpty()) {
                                    viewModel.addPlayer(newPlayerName)
                                    newPlayerName = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter", tint = CyberBg)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (players.isEmpty()) {
                        Text(
                            text = "Ajoutez entre 2 et 10 joueurs pour démarrer !",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        // Quick chips for added players
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            players.forEach { player ->
                                Row(
                                    modifier = Modifier
                                        .background(CyberSurfaceElevated, RoundedCornerShape(48.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = player.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Supprimer",
                                        tint = NeonRed,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.removePlayer(player) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Difficulty Selector
        item {
            val selectedLvl by viewModel.selectedLevel.collectAsState()
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Difficulté",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val levels = listOf(
                            Triple(ChallengeLevel.SOFT, "Soft", NeonTeal),
                            Triple(ChallengeLevel.NORMAL, "Normal", NeonPink),
                            Triple(ChallengeLevel.CHAOS, "Chaos", NeonGold)
                        )

                        levels.forEach { (lvl, title, color) ->
                            val isSelected = selectedLvl == lvl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSurfaceElevated)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) color else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectedLevel.value = lvl }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (isSelected) color else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val explanation = when (selectedLvl) {
                        ChallengeLevel.SOFT -> "Idéal pour jouer avec tout le monde ! Questions amusantes, brisent la glace, sans gêne."
                        ChallengeLevel.NORMAL -> "Des défis plus épicés et drôles. Attendez-vous à quelques rougeurs !"
                        ChallengeLevel.CHAOS -> "Complètement farfelu et absurde ! Idéal pour rigoler sans fin et improviser des bêtises."
                    }

                    Text(
                        text = explanation,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Game Options Block
        item {
            val isChaosMode by viewModel.isChaosModeActive.collectAsState()
            val isSuddenDeath by viewModel.isSuddenDeathActive.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Modes Spéciaux",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Chaos Match Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.isChaosModeActive.value = !isChaosMode }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Evenement Chaos",
                                    tint = NeonGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mode Chaos de Soirée", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(
                                "Événement imprévisible (Duel, double défi...) déclenché tous les 5 tours",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Checkbox(
                            checked = isChaosMode,
                            onCheckedChange = { viewModel.isChaosModeActive.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = NeonGold)
                        )
                    }

                    Divider(color = CyberSurfaceElevated, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                    // Sudden Death Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.isSuddenDeathActive.value = !isSuddenDeath }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Dangerous,
                                    contentDescription = "Mort subite",
                                    tint = NeonRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mort Subite", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(
                                "Désactivation absolue du bouton 'Passer'. Tout défi doit être relevé !",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Checkbox(
                            checked = isSuddenDeath,
                            onCheckedChange = { viewModel.isSuddenDeathActive.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = NeonRed)
                        )
                    }
                }
            }
        }

        // Custom Challenges additions section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isAddingCustom = !isAddingCustom },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.RateReview, contentDescription = "Défis perso", tint = NeonTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Défis Personnalisés (${customChallenges.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = if (isAddingCustom) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = Color.White
                        )
                    }

                    if (isAddingCustom) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Type Choice (Action or Verite)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { customType = ChallengeType.ACTION },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (customType == ChallengeType.ACTION) NeonPink else CyberSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Action", color = Color.White)
                            }
                            Button(
                                onClick = { customType = ChallengeType.VERITE },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (customType == ChallengeType.VERITE) NeonTeal else CyberSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Vérité", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Level Choice
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val items = listOf(
                                ChallengeLevel.SOFT to "Soft",
                                ChallengeLevel.NORMAL to "Normal",
                                ChallengeLevel.CHAOS to "Chaos"
                            )
                            items.forEach { (lvl, name) ->
                                Button(
                                    onClick = { customLevel = lvl },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (customLevel == lvl) NeonPurple else CyberSurfaceElevated
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(name, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customContent,
                            onValueChange = { customContent = it },
                            placeholder = { Text("Tapez votre défi personnalisé... Utilisez {player} ou {other} pour l'intégration automatique !", color = TextSecondary, fontSize = 13.sp) },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal,
                                unfocusedBorderColor = CyberSurfaceElevated
                            ),
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (customContent.trim().isNotEmpty()) {
                                    viewModel.addCustomChallenge(customType, customLevel, customContent)
                                    customContent = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sauvegarder ce défi", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (customChallenges.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Vos défis enregistrés :", color = TextSecondary, fontSize = 14.sp)
                            // Show custom challenges
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 150.dp)
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                customChallenges.take(15).forEach { challenge ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberSurfaceElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(if (challenge.type == "ACTION") NeonPink else NeonTeal, RoundedCornerShape(4.dp))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${challenge.type} [${challenge.level}]",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = challenge.content,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            tint = NeonRed,
                                            modifier = Modifier
                                                .clickable { viewModel.deleteCustomChallenge(challenge) }
                                                .size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Launch Game Block
        item {
            val hasEnoughPlayers = players.size >= 2
            val brush = Brush.linearGradient(listOf(NeonPurple, Color(0xFF4F46E5)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasEnoughPlayers) brush else Brush.linearGradient(listOf(CyberSurfaceElevated, CyberSurfaceElevated)))
                    .clickable(enabled = hasEnoughPlayers) {
                        onStartGame()
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Démarrer",
                        tint = if (hasEnoughPlayers) Color.White else TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LANCER LE JEU",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (hasEnoughPlayers) Color.White else TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
