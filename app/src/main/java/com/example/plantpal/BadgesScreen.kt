package com.example.plantpal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BadgesUiState(
    val progress: UserProgress? = null,
    val plants: List<PlantProfile> = emptyList(),
    val unlockedBadges: List<Badge> = emptyList(),
    val lockedBadges: List<Badge> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class BadgesViewModel : ViewModel() {
    private val progressRepo = ProgressRepository()
    private val plantRepo = PlantRepository()
    
    private val _uiState = MutableStateFlow(BadgesUiState())
    val uiState: StateFlow<BadgesUiState> = _uiState.asStateFlow()
    
    init {
        loadBadges()
    }
    
    fun loadBadges() {
        viewModelScope.launch {
            _uiState.value = BadgesUiState(isLoading = true)
            
            try {
                val progressResult = progressRepo.getUserProgress()
                val plantsResult = plantRepo.getAllPlants()
                
                if (progressResult.isSuccess && plantsResult.isSuccess) {
                    val progress = progressResult.getOrNull()!!
                    val plants = plantsResult.getOrNull()!!
                    
                    val unlockedBadgeIds = progress.unlockedBadges.toSet()
                    val unlocked = BadgeDefinitions.allBadges.filter { it.id in unlockedBadgeIds }
                    val locked = BadgeDefinitions.allBadges.filter { it.id !in unlockedBadgeIds }
                    
                    _uiState.value = BadgesUiState(
                        progress = progress,
                        plants = plants,
                        unlockedBadges = unlocked.sortedByDescending { it.rarity.ordinal },
                        lockedBadges = locked.sortedBy { it.requirement },
                        isLoading = false
                    )
                } else {
                    _uiState.value = BadgesUiState(
                        error = "Failed to load badges",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = BadgesUiState(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    onBack: () -> Unit,
    viewModel: BadgesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFB5E48C),
                        Color(0xFFD9ED92),
                        Color(0xFF99D98C)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Badges & Achievements") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Error loading badges",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadBadges() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                else -> {
                    BadgesContent(
                        uiState = uiState,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgesContent(
    uiState: BadgesUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BadgeStatsCard(uiState.progress)
        }

        item {
            StreakCard(uiState.progress)
        }

        if (uiState.unlockedBadges.isNotEmpty()) {
            item {
                Text(
                    "Unlocked (${uiState.unlockedBadges.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F5233)
                )
            }
            
            items(uiState.unlockedBadges) { badge ->
                BadgeCard(
                    badge = badge,
                    isUnlocked = true,
                    progress = uiState.progress
                )
            }
        }

        if (uiState.lockedBadges.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Locked (${uiState.lockedBadges.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F5233)
                )
            }
            
            items(uiState.lockedBadges) { badge ->
                BadgeCard(
                    badge = badge,
                    isUnlocked = false,
                    progress = uiState.progress
                )
            }
        }
        
        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BadgeStatsCard(progress: UserProgress?) {
    if (progress == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Your Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F5233)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = "🌱",
                    value = progress.totalPlants.toString(),
                    label = "Plants"
                )
                StatItem(
                    icon = "💧",
                    value = progress.totalWaterings.toString(),
                    label = "Waterings"
                )
                StatItem(
                    icon = "🌿",
                    value = progress.totalFertilizations.toString(),
                    label = "Fertilizations"
                )
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = "🏆",
                    value = progress.unlockedBadges.size.toString(),
                    label = "Badges"
                )
                StatItem(
                    icon = "⚡",
                    value = progress.longestStreak.toString(),
                    label = "Best Streak"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F5233)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF52796F)
        )
    }
}

@Composable
private fun StreakCard(progress: UserProgress?) {
    if (progress == null) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.currentStreak > 0) {
                Color(0xFFFF9800).copy(alpha = 0.15f)
            } else {
                Color.White.copy(alpha = 0.9f)
            }
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Current Streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2F5233)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (progress.currentStreak > 0) {
                        "${progress.currentStreak} ${if (progress.currentStreak == 1) "day" else "days"} 🔥"
                    } else {
                        "Start your streak today!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (progress.currentStreak > 0) Color(0xFFFF9800) else Color(0xFF52796F)
                )
            }
            
            if (progress.currentStreak > 0) {
                Text(
                    "🔥",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.scale(flameScale)
                )
            }
        }
    }
}

@Composable
private fun BadgeCard(
    badge: Badge,
    isUnlocked: Boolean,
    progress: UserProgress?,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) {
                    badge.color.copy(alpha = 0.15f)
                } else {
                    Color.LightGray.copy(alpha = 0.3f)
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isUnlocked) 6.dp else 2.dp
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) badge.color.copy(alpha = 0.3f)
                            else Color.Gray.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(
                            badge.icon,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            badge.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) Color(0xFF2F5233) else Color.Gray,
                            modifier = Modifier.alpha(if (isUnlocked) 1f else 0.6f)
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = getRarityColor(badge.rarity).copy(alpha = 0.2f)
                        ) {
                            Text(
                                badge.rarity.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = getRarityColor(badge.rarity),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUnlocked) Color(0xFF52796F) else Color.Gray,
                        modifier = Modifier.alpha(if (isUnlocked) 1f else 0.6f)
                    )

                    if (!isUnlocked && progress != null) {
                        Spacer(Modifier.height(8.dp))
                        val currentProgress = getBadgeProgress(badge, progress)
                        val progressPercent = (currentProgress.toFloat() / badge.requirement.toFloat())
                            .coerceIn(0f, 1f)
                        
                        if (badge.requirement > 1) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "$currentProgress / ${badge.requirement}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "${(progressPercent * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = progressPercent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = badge.color,
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getBadgeProgress(badge: Badge, progress: UserProgress): Int {
    return when (badge.id) {
        "streak_3", "streak_7", "streak_30", "streak_100" -> progress.longestStreak
        "plants_5", "plants_10", "plants_25", "plants_50" -> progress.totalPlants
        "waterings_50", "waterings_200" -> progress.totalWaterings
        "fertilizations_25", "fertilizations_100" -> progress.totalFertilizations
        "perfect_week" -> progress.perfectCareWeeks
        else -> 0
    }
}

private fun getRarityColor(rarity: BadgeRarity): Color {
    return when (rarity) {
        BadgeRarity.COMMON -> Color(0xFF9E9E9E)
        BadgeRarity.RARE -> Color(0xFF2196F3)
        BadgeRarity.EPIC -> Color(0xFF9C27B0)
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700)
    }
}

@Composable
fun BadgeUnlockedNotification(
    badge: Badge,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                badge.icon,
                style = MaterialTheme.typography.displayLarge
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🎉 Badge Unlocked! 🎉",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    badge.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = badge.color,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    badge.description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getRarityColor(badge.rarity).copy(alpha = 0.2f)
                ) {
                    Text(
                        "${badge.rarity.name} BADGE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = getRarityColor(badge.rarity),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = badge.color
                )
            ) {
                Text("Awesome!")
            }
        }
    )
}
