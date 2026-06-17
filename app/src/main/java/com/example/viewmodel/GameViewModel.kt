package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val playerDao = database.playerDao()
    private val historyDao = database.historyDao()
    private val customChallengeDao = database.customChallengeDao()

    // Configuration States
    private val _players = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val players: StateFlow<List<PlayerEntity>> = _players

    private val _customChallenges = MutableStateFlow<List<CustomChallengeEntity>>(emptyList())
    val customChallenges: StateFlow<List<CustomChallengeEntity>> = _customChallenges

    private val _history = MutableStateFlow<List<HistoryEntryEntity>>(emptyList())
    val history: StateFlow<List<HistoryEntryEntity>> = _history

    // Game Session Specs
    val selectedLevel = MutableStateFlow(ChallengeLevel.NORMAL)
    val isChaosModeActive = MutableStateFlow(false) // Event every 5 turns
    val isSuddenDeathActive = MutableStateFlow(false) // Passes are disabled

    // Game Running States
    val isGameStarted = MutableStateFlow(false)
    val turnCount = MutableStateFlow(1)
    val activePlayerIndex = MutableStateFlow(0)
    val displayedPlayerName = MutableStateFlow("")

    // Wheel Animation States
    val isWheelSpinning = MutableStateFlow(false)
    val wheelTargetAngle = MutableStateFlow(0f)

    // Challenge States
    val activeChallengeText = MutableStateFlow<String?>(null)
    val activeChallengeType = MutableStateFlow<ChallengeType?>(null)
    val isAILoading = MutableStateFlow(false)

    // Chaos Event States
    val activeChaosEventText = MutableStateFlow<String?>(null)

    // Seeded Challenge State Tracking (to prevent repeating challenges too quickly)
    private val usedChallengeIds = mutableSetOf<Int>()

    init {
        // Observe players from database
        viewModelScope.launch {
            playerDao.getAllPlayersFlow().collect {
                _players.value = it
            }
        }
        // Observe custom challenges
        viewModelScope.launch {
            customChallengeDao.getAllCustomChallengesFlow().collect {
                _customChallenges.value = it
            }
        }
        // Observe game history
        viewModelScope.launch {
            historyDao.getAllHistoryFlow().collect {
                _history.value = it
            }
        }
    }

    // Config Actions
    fun addPlayer(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (_players.value.size >= 10) return
        viewModelScope.launch {
            playerDao.insertPlayer(PlayerEntity(name = trimmed))
        }
    }

    fun removePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            playerDao.deletePlayer(player)
        }
    }

    fun clearPlayers() {
        viewModelScope.launch {
            playerDao.deleteAllPlayers()
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        SoundManager.setSoundEnabled(enabled)
    }

    fun isSoundEnabled(): Boolean = SoundManager.isEnabled()

    // Custom challenges helper
    fun addCustomChallenge(type: ChallengeType, level: ChallengeLevel, content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            customChallengeDao.insertCustomChallenge(
                CustomChallengeEntity(type = type.name, level = level.name, content = trimmed)
            )
        }
    }

    fun deleteCustomChallenge(challenge: CustomChallengeEntity) {
        viewModelScope.launch {
            customChallengeDao.deleteCustomChallenge(challenge)
        }
    }

    // Lifecycle Actions
    fun startGame() {
        if (_players.value.size < 2) return
        isGameStarted.value = true
        turnCount.value = 1
        activePlayerIndex.value = 0
        displayedPlayerName.value = _players.value[0].name
        usedChallengeIds.clear()
        viewModelScope.launch {
            historyDao.deleteAllHistory()
        }
    }

    fun resetGame() {
        isGameStarted.value = false
        activeChallengeText.value = null
        activeChallengeType.value = null
        activeChaosEventText.value = null
        turnCount.value = 1
        activePlayerIndex.value = 0
    }

    // Wheel Spin to designate next player
    fun spinWheelAndSelectPlayer() {
        if (_players.value.isEmpty()) return
        viewModelScope.launch {
            isWheelSpinning.value = true
            activeChallengeText.value = null
            activeChallengeType.value = null
            activeChaosEventText.value = null

            // Generate high target angle for wheel spinning effect (e.g., 5-8 full rotations + random offset)
            val baseRotations = 5 + Random.nextInt(4)
            val randomOffset = Random.nextInt(360)
            val targetAngle = (baseRotations * 360f) + randomOffset
            wheelTargetAngle.value = targetAngle

            // Synthetic spinning clicks
            val totalTicks = 20 + Random.nextInt(10)
            for (i in 0 until totalTicks) {
                if (SoundManager.isEnabled()) {
                    SoundManager.playSpinShort()
                }
                // Decelerating delay
                val stepDelay = 40 + (i * i * 0.8f).toLong()
                delay(stepDelay)
            }

            // Finish selection
            if (isChaosModeActive.value && turnCount.value > 1 && turnCount.value % 5 == 1) {
                // If it is a Chaos mode event turn, we can optionally make a sound
            }
            SoundManager.playWheelSuccess()

            // Calculate which player index is selected based on targetAngle
            val segmentSize = 360f / _players.value.size
            // Angle modulo 360 relative to pointing arrow at top (270 degrees or similar)
            val activeAngle = (targetAngle % 360f)
            // Determine player index
            val selectedIndex = ((_players.value.size - (activeAngle / segmentSize).toInt()) % _players.value.size)
                .coerceIn(0, _players.value.size - 1)

            activePlayerIndex.value = selectedIndex
            displayedPlayerName.value = _players.value[selectedIndex].name
            isWheelSpinning.value = false

            // Check if standard turn or Chaos event is triggered (every 5 turns, if configured)
            if (isChaosModeActive.value && turnCount.value % 5 == 0) {
                triggerChaosEvent()
            }
        }
    }

    // Trigger Chaotic event
    private fun triggerChaosEvent() {
        val player = _players.value[activePlayerIndex.value].name
        val other = getOtherPlayerName(player)

        val events = listOf(
            "DOUBLE DÉFI ! $player doit exécuter une Action ET une Vérité d'affilée !",
            "TOUR INVERSÉ ! La roue change de sens !",
            "DUEL ! $player doit affronter $other au Pierre-Feuille-Ciseaux. Le perdant a un gage ridicule choisi par le groupe !",
            "VÉRITÉ OBLIGATOIRE ! Pas d'Action possible pour $player ce tour-ci !",
            "TÉLÉPHONE PARTAGÉ ! $player doit donner le téléphone au joueur à sa droite pour que celui-ci choisisse son prochain choix."
        )
        val selectedEvent = events.random()
        activeChaosEventText.value = selectedEvent

        // Log to history
        viewModelScope.launch {
            historyDao.insertHistoryEntry(
                HistoryEntryEntity(
                    playerName = player,
                    type = "EVENEMENT",
                    content = "Événement Chaos: $selectedEvent",
                    result = "REALISE"
                )
            )
        }
    }

    // Get another random player in the game
    fun getOtherPlayerName(exceptPlayer: String): String {
        val candidates = _players.value.filter { it.name != exceptPlayer }
        return if (candidates.isNotEmpty()) {
            candidates.random().name
        } else {
            "quelqu'un"
        }
    }

    // Get player sitting tightly to the right
    fun getRightPlayerName(currentPlayer: String): String {
        val list = _players.value
        if (list.size < 2) return "le joueur à droite"
        val idx = list.indexOfFirst { it.name == currentPlayer }
        if (idx == -1) return "le joueur à droite"
        val rightIdx = (idx + 1) % list.size
        return list[rightIdx].name
    }

    // Prepare content with placeholders replaced
    private fun formatChallengeText(rawText: String, activePlayer: String): String {
        val other = getOtherPlayerName(activePlayer)
        val right = getRightPlayerName(activePlayer)
        return rawText
            .replace("{player}", activePlayer)
            .replace("{other}", other)
            .replace("{right}", right)
            .replace("{youngest}", "le joueur le plus jeune du groupe")
    }

    // Select content (Action or Truth)
    fun selectChallenge(type: ChallengeType) {
        val player = _players.value[activePlayerIndex.value]
        activeChallengeType.value = type

        viewModelScope.launch {
            // Check if we can/should request from Gemini first (if key is set)
            val otherName = getOtherPlayerName(player.name)
            isAILoading.value = true

            val aiChallenge = GeminiService.generateChallenge(
                type = type.name,
                level = selectedLevel.value.name,
                activePlayer = player.name,
                otherPlayer = otherName
            )

            isAILoading.value = false

            if (aiChallenge.isNotEmpty()) {
                activeChallengeText.value = aiChallenge
                SoundManager.playSuspense()
                return@launch
            }

            // Fallback to extensive offline base (combining official preseeded list + user custom templates)
            val level = selectedLevel.value
            val customMatched = _customChallenges.value
                .filter { it.type == type.name && it.level == level.name }
                .map { Challenge(id = -it.id, type = type, level = level, text = it.content) }

            val preseededMatched = DefaultChallenges.list
                .filter { it.type == type && it.level == level }

            val available = (customMatched + preseededMatched)
                .filter { !usedChallengeIds.contains(it.id) }

            val chosen = if (available.isNotEmpty()) {
                available.random()
            } else {
                // If drained, clear used set and recycle
                usedChallengeIds.clear()
                (customMatched + preseededMatched).randomOrNull() ?: Challenge(0, type, level, "Fais coucou à tout le monde.")
            }

            usedChallengeIds.add(chosen.id)
            activeChallengeText.value = formatChallengeText(chosen.text, player.name)
            SoundManager.playSuspense()
        }
    }

    // Turn resolution : Successful completion
    fun challengeRealized() {
        val player = _players.value[activePlayerIndex.value]
        val type = activeChallengeType.value ?: return
        val text = activeChallengeText.value ?: activeChaosEventText.value ?: ""

        viewModelScope.launch {
            // Sound feedback
            SoundManager.playSuccess()

            // Update stats
            val updatedPlayer = when (type) {
                ChallengeType.VERITE -> player.copy(truthsAnswered = player.truthsAnswered + 1)
                ChallengeType.ACTION -> player.copy(actionsCompleted = player.actionsCompleted + 1)
            }
            playerDao.updatePlayer(updatedPlayer)

            // Save log
            historyDao.insertHistoryEntry(
                HistoryEntryEntity(
                    playerName = player.name,
                    type = type.name,
                    content = text,
                    result = "REALISE"
                )
            )

            // Auto-advance
            advanceTurn()
        }
    }

    // Turn resolution : Fail or Pass
    fun challengePassed() {
        if (isSuddenDeathActive.value) return // Forbidden in sudden death

        val player = _players.value[activePlayerIndex.value]
        val type = activeChallengeType.value ?: return
        val text = activeChallengeText.value ?: ""

        viewModelScope.launch {
            // Sound feedback
            SoundManager.playPass()

            // Update stats
            val updatedPlayer = player.copy(passesUsed = player.passesUsed + 1)
            playerDao.updatePlayer(updatedPlayer)

            // Save log
            historyDao.insertHistoryEntry(
                HistoryEntryEntity(
                    playerName = player.name,
                    type = type.name,
                    content = text,
                    result = "PASSE"
                )
            )

            // Auto-advance
            advanceTurn()
        }
    }

    private fun advanceTurn() {
        activeChallengeText.value = null
        activeChallengeType.value = null
        activeChaosEventText.value = null
        turnCount.value = turnCount.value + 1

        // Move active index
        if (_players.value.isNotEmpty()) {
            activePlayerIndex.value = (activePlayerIndex.value + 1) % _players.value.size
            displayedPlayerName.value = _players.value[activePlayerIndex.value].name
        }
    }

    fun cleanHistory() {
        viewModelScope.launch {
            historyDao.deleteAllHistory()
        }
    }

    fun resetStatistics() {
        viewModelScope.launch {
            val resetList = _players.value.map {
                it.copy(truthsAnswered = 0, actionsCompleted = 0, passesUsed = 0)
            }
            playerDao.insertPlayers(resetList)
        }
    }
}
