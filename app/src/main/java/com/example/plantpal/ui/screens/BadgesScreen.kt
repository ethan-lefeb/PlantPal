package com.example.plantpal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.Badge
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.BadgeDefinitions
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.BadgeRarity
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.UserProgress
import com.example.plantpal.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.plantpal.ui.theme.ForestGradientBalanced


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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Badges & Rewards")
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "🏆",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF52796F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(ForestGradientBalanced)

            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                ErrorMessage(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadBadges() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                BadgesContent(
                    uiState = uiState,
                    modifier = Modifier.padding(padding)
                )
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
            UnlockProgressCard(uiState.progress?.unlockedBadges ?: emptyList())
        }
        item {
            StreakCard(uiState.progress)
        }
        if (uiState.unlockedBadges.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Unlocked",
                    count = uiState.unlockedBadges.size,
                    icon = "✅"
                )
            }
            
            items(uiState.unlockedBadges) { badge ->
                EnhancedBadgeCard(
                    badge = badge,
                    isUnlocked = true,
                    progress = uiState.progress,
                    unlockedBadges = uiState.progress?.unlockedBadges ?: emptyList()
                )
            }
        }
        if (uiState.lockedBadges.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(
                    title = "Locked",
                    count = uiState.lockedBadges.size,
                    icon = "🔒"
                )
            }
            
            items(uiState.lockedBadges) { badge ->
                EnhancedBadgeCard(
                    badge = badge,
                    isUnlocked = false,
                    progress = uiState.progress,
                    unlockedBadges = uiState.progress?.unlockedBadges ?: emptyList()
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
private fun SectionHeader(
    title: String,
    count: Int,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F5233)
        )
        Surface(
            color = Color(0xFF52796F).copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF2F5233),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UnlockProgressCard(unlockedBadges: List<String>) {
    val totalUnlockables = CustomizationUnlocks.allUnlockables.size
    val unlockedCount = CustomizationUnlocks.getUnlockedItems(unlockedBadges).size
    val progress = unlockedCount.toFloat() / totalUnlockables.toFloat()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "🎨",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Customization Unlocks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F5233)
                    )
                }
                Text(
                    "$unlockedCount/$totalUnlockables",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF52796F)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF52796F),
                trackColor = Color(0xFFE0E0E0)
            )
            
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UnlockTypeChip(
                    icon = "🌿",
                    label = "Colors",
                    count = CustomizationUnlocks.getUnlockedItemsByType(
                        UnlockType.PLANT_COLOR,
                        unlockedBadges
                    ).size + 1,
                    total = CustomizationUnlocks.getAllPlantColors().size
                )
                UnlockTypeChip(
                    icon = "🏺",
                    label = "Pots",
                    count = CustomizationUnlocks.getUnlockedItemsByType(
                        UnlockType.POT_COLOR,
                        unlockedBadges
                    ).size + 1,
                    total = CustomizationUnlocks.getAllPotColors().size
                )
                UnlockTypeChip(
                    icon = "🪴",
                    label = "Styles",
                    count = CustomizationUnlocks.getUnlockedItemsByType(
                        UnlockType.POT_STYLE,
                        unlockedBadges
                    ).size + 1,
                    total = CustomizationUnlocks.getAllPotStyles().size
                )
            }
        }
    }
}

@Composable
private fun UnlockTypeChip(
    icon: String,
    label: String,
    count: Int,
    total: Int
) {
    Surface(
        color = Color(0xFF52796F).copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$count/$total",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F5233)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun EnhancedBadgeCard(
    badge: Badge,
    isUnlocked: Boolean,
    progress: UserProgress?,
    unlockedBadges: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    
    val unlocks = CustomizationUnlocks.allUnlockables.filter { 
        it.requiredBadge == badge.id 
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.7f),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                Color.White.copy(alpha = 0.95f)
            } else {
                Color(0xFFF5F5F5).copy(alpha = 0.8f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) {
                                    badge.color.copy(alpha = 0.2f)
                                } else {
                                    Color(0xFFE0E0E0)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isUnlocked) badge.icon else "🔒",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                badge.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color(0xFF2F5233) else Color(0xFF757575)
                            )
                        }
                        
                        Spacer(Modifier.height(4.dp))
                        
                        Text(
                            badge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUnlocked) Color(0xFF666666) else Color(0xFF999999)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Surface(
                            color = getRarityColor(badge.rarity).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                badge.rarity.name,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = getRarityColor(badge.rarity),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (unlocks.isNotEmpty()) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color(0xFF52796F)
                        )
                    }
                }
            }

            if (!isUnlocked && progress != null) {
                val currentValue = getBadgeProgress(badge, progress)
                val progressPercent = (currentValue.toFloat() / badge.requirement.toFloat())
                    .coerceIn(0f, 1f)
                
                if (progressPercent > 0f) {
                    Spacer(Modifier.height(12.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Progress",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF757575)
                            )
                            Text(
                                "$currentValue / ${badge.requirement}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = badge.color
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent },
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

            AnimatedVisibility(
                visible = expanded && unlocks.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color(0xFFE0E0E0))
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (isUnlocked) "🎁" else "🔒",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (isUnlocked) "Unlocks" else "Will Unlock",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) Color(0xFF52796F) else Color(0xFF757575)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unlocks) { unlock ->
                            UnlockRewardChip(
                                unlock = unlock,
                                isUnlocked = isUnlocked
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnlockRewardChip(
    unlock: UnlockableItem,
    isUnlocked: Boolean
) {
    Surface(
        color = if (isUnlocked) {
            Color(0xFF52796F).copy(alpha = 0.15f)
        } else {
            Color(0xFFE0E0E0)
        },
        shape = RoundedCornerShape(12.dp),
        border = if (isUnlocked) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF52796F).copy(alpha = 0.3f))
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (unlock.icon.isNotEmpty()) {
                Text(
                    unlock.icon,
                    style = MaterialTheme.typography.titleMedium
                )
            } else if (unlock.previewColor != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(unlock.previewColor)
                        .border(1.dp, Color(0xFF2C2C2C), CircleShape)
                )
            }
            
            Column {
                Text(
                    unlock.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFF2F5233) else Color(0xFF757575)
                )
                Text(
                    when (unlock.type) {
                        UnlockType.POT_STYLE -> "Pot Style"
                        UnlockType.POT_COLOR -> "Pot Color"
                        UnlockType.PLANT_COLOR -> "Plant Color"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnlocked) Color(0xFF666666) else Color(0xFF999999)
                )
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
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFFF9800)
            )
            Text(
                "Oops!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF666666)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF52796F)
                )
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}
