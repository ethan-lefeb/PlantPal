package com.example.plantpal

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import androidx.compose.runtime.rememberCoroutineScope
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.AvatarConfig
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.UserProgress
import com.example.plantpal.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ProgressRepository

data class CustomizationUiState(
    val plant: PlantProfile? = null,
    val progress: UserProgress? = null,
    val currentConfig: AvatarConfig = AvatarConfig("generic", "green", "terracotta", "classic"),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showUnlockDialog: UnlockableItem? = null
)

class CustomizationViewModel(
    private val plantId: String?
) : ViewModel() {
    
    private val plantRepo = PlantRepository()
    private val progressRepo = ProgressRepository()
    
    private val _uiState = MutableStateFlow(CustomizationUiState())
    val uiState: StateFlow<CustomizationUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val progressResult = progressRepo.getUserProgress()
                val progress = progressResult.getOrNull()
                
                if (plantId != null) {
                    val plantResult = plantRepo.getPlant(plantId)
                    val plant = plantResult.getOrNull()
                    
                    _uiState.value = CustomizationUiState(
                        plant = plant,
                        progress = progress,
                        currentConfig = plant?.avatarConfig ?: AvatarConfig(
                            "generic",
                            "green",
                            "terracotta",
                            "classic"
                        ),
                        isLoading = false
                    )
                } else {
                    _uiState.value = CustomizationUiState(
                        plant = null,
                        progress = progress,
                        currentConfig = AvatarConfig("generic", "green", "terracotta", "classic"),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CustomizationUiState(isLoading = false)
            }
        }
    }
    
    fun updateConfig(newConfig: AvatarConfig) {
        _uiState.value = _uiState.value.copy(currentConfig = newConfig)
    }
    
    fun showUnlockDialog(item: UnlockableItem) {
        _uiState.value = _uiState.value.copy(showUnlockDialog = item)
    }
    
    fun hideUnlockDialog() {
        _uiState.value = _uiState.value.copy(showUnlockDialog = null)
    }
    
    suspend fun saveAvatar(): Result<Unit> {
        val plant = _uiState.value.plant ?: return Result.failure(Exception("No plant"))
        val config = _uiState.value.currentConfig
        
        _uiState.value = _uiState.value.copy(isSaving = true)
        
        return try {
            val updatedPlant = plant.copy(avatarConfig = config)
            plantRepo.updatePlant(updatedPlant)
            _uiState.value = _uiState.value.copy(isSaving = false)
            Result.success(Unit)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isSaving = false)
            Result.failure(e)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamifiedAvatarCustomizationScreen(
    plantId: String?,
    onNavigateBack: () -> Unit,
    viewModel: CustomizationViewModel = viewModel { CustomizationViewModel(plantId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showSaveSuccess by remember { mutableStateOf(false) }
    
    val unlockedBadges = uiState.progress?.unlockedBadges ?: emptyList()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Avatar 🎨") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB5E48C),
                        Color(0xFFD9ED92),
                        Color(0xFF99D98C)
                    )
                )
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                CustomizationContent(
                    plant = uiState.plant,
                    config = uiState.currentConfig,
                    unlockedBadges = unlockedBadges,
                    progress = uiState.progress,
                    onConfigChange = { viewModel.updateConfig(it) },
                    onShowUnlockDialog = { viewModel.showUnlockDialog(it) },
                    onSave = {
                        scope.launch {
                            val result = viewModel.saveAvatar()
                            if (result.isSuccess) {
                                showSaveSuccess = true
                            }
                        }
                    },
                    isSaving = uiState.isSaving,
                    modifier = Modifier.padding(padding)
                )
            }
            AnimatedVisibility(
                visible = showSaveSuccess,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF52796F)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            "Avatar Saved!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    uiState.showUnlockDialog?.let { item ->
        UnlockRequirementDialog(
            item = item,
            progress = uiState.progress,
            onDismiss = { viewModel.hideUnlockDialog() }
        )
    }
}

@Composable
private fun CustomizationContent(
    plant: PlantProfile?,
    config: AvatarConfig,
    unlockedBadges: List<String>,
    progress: UserProgress?,
    onConfigChange: (AvatarConfig) -> Unit,
    onShowUnlockDialog: (UnlockableItem) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2F5233),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                PlantAvatar(
                    avatarConfig = config,
                    size = 200.dp,
                    health = "Healthy",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        CustomizationSection(
            title = "Plant Body",
            icon = "🌿",
            unlockedCount = PlantTypeDatabase.getAllAvatarTypes().size,
            totalCount = PlantTypeDatabase.getAllAvatarTypes().size
        ) {
            PlantBodyGrid(
                currentType = config.baseType,
                onSelect = { onConfigChange(config.copy(baseType = it)) }
            )
        }
        CustomizationSection(
            title = "Plant Color",
            icon = "🌿",
            unlockedCount = CustomizationUnlocks.getUnlockedItemsByType(
                UnlockType.PLANT_COLOR,
                unlockedBadges
            ).size + 1,
            totalCount = CustomizationUnlocks.getAllPlantColors().size
        ) {
            ColorOptionsGrid(
                options = CustomizationUnlocks.getAllPlantColors(),
                selectedId = config.color,
                unlockedBadges = unlockedBadges,
                progress = progress,
                type = UnlockType.PLANT_COLOR,
                onSelect = { onConfigChange(config.copy(color = it)) },
                onShowUnlock = onShowUnlockDialog
            )
        }

        CustomizationSection(
            title = "Pot Color",
            icon = "🏺",
            unlockedCount = CustomizationUnlocks.getUnlockedItemsByType(
                UnlockType.POT_COLOR,
                unlockedBadges
            ).size + 1,
            totalCount = CustomizationUnlocks.getAllPotColors().size
        ) {
            ColorOptionsGrid(
                options = CustomizationUnlocks.getAllPotColors(),
                selectedId = config.potColor,
                unlockedBadges = unlockedBadges,
                progress = progress,
                type = UnlockType.POT_COLOR,
                onSelect = { onConfigChange(config.copy(potColor = it)) },
                onShowUnlock = onShowUnlockDialog
            )
        }

        CustomizationSection(
            title = "Pot Style",
            icon = "🪴",
            unlockedCount = CustomizationUnlocks.getUnlockedItemsByType(
                UnlockType.POT_STYLE,
                unlockedBadges
            ).size + 1,
            totalCount = CustomizationUnlocks.getAllPotStyles().size
        ) {
            StyleOptionsGrid(
                options = CustomizationUnlocks.getAllPotStyles(),
                selectedId = config.potStyle,
                unlockedBadges = unlockedBadges,
                progress = progress,
                onSelect = { onConfigChange(config.copy(potStyle = it)) },
                onShowUnlock = onShowUnlockDialog
            )
        }
        
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val allTypes = PlantTypeDatabase.getAllAvatarTypes()
                    val unlockedColors = CustomizationUnlocks.getUnlockedItemsByType(
                        UnlockType.PLANT_COLOR,
                        unlockedBadges
                    ).map { it.id } + "green"
                    val unlockedPotColors = CustomizationUnlocks.getUnlockedItemsByType(
                        UnlockType.POT_COLOR,
                        unlockedBadges
                    ).map { it.id } + "terracotta"
                    val unlockedPotStyles = CustomizationUnlocks.getUnlockedItemsByType(
                        UnlockType.POT_STYLE,
                        unlockedBadges
                    ).map { it.id } + "classic"
                    
                    onConfigChange(
                        AvatarConfig(
                            baseType = allTypes.random(),
                            color = unlockedColors.random(),
                            potColor = unlockedPotColors.random(),
                            potStyle = unlockedPotStyles.random()
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2F5233)
                )
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Randomize")
            }
            
            if (plant != null) {
                OutlinedButton(
                    onClick = {
                        val autoConfig = AvatarGenerator.generateAvatarForPlant(
                            family = plant.careInfo.family,
                            genus = plant.careInfo.genus,
                            commonName = plant.commonName,
                            scientificName = plant.scientificName
                        )
                        onConfigChange(autoConfig)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2F5233)
                    )
                ) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Auto")
                }
            }
        }

        if (plant != null) {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF52796F)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Avatar")
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun CustomizationSection(
    title: String,
    icon: String,
    unlockedCount: Int,
    totalCount: Int,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2F5233),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = Color(0xFF52796F).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "$unlockedCount/$totalCount",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2F5233),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun PlantBodyGrid(
    currentType: String,
    onSelect: (String) -> Unit
) {
    val plantTypes = listOf(
        "generic" to "Generic",
        "cactus_round" to "Round Cactus",
        "cactus_trailing" to "Trailing Cactus",
        "succulent_rosette" to "Rosette",
        "succulent_jade" to "Jade",
        "succulent_string" to "String Plant",
        "succulent_aloe" to "Aloe",
        "snake_plant" to "Snake Plant",
        "pothos" to "Pothos",
        "philodendron_heart" to "Philodendron",
        "monstera" to "Monstera",
        "fern_boston" to "Boston Fern",
        "fern_maidenhair" to "Maidenhair Fern",
        "fern_birds_nest" to "Bird's Nest Fern",
        "prayer_plant" to "Prayer Plant",
        "spider_plant" to "Spider Plant",
        "peace_lily" to "Peace Lily",
        "zz_plant" to "ZZ Plant",
        "rubber_plant" to "Rubber Plant",
        "fiddle_leaf" to "Fiddle Leaf Fig",
        "dracaena" to "Dracaena",
        "palm" to "Palm",
        "orchid" to "Orchid",
        "african_violet" to "African Violet",
        "peperomia" to "Peperomia",
        "pilea" to "Pilea",
        "herb" to "Herb"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(plantTypes) { (typeId, label) ->
            OptionCard(
                label = label,
                isSelected = currentType == typeId,
                isLocked = false,
                onClick = { onSelect(typeId) },
                progress = null
            )
        }
    }
}

@Composable
private fun ColorOptionsGrid(
    options: List<Pair<String, String>>,
    selectedId: String,
    unlockedBadges: List<String>,
    progress: UserProgress?,
    type: UnlockType,
    onSelect: (String) -> Unit,
    onShowUnlock: (UnlockableItem) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (id, label) ->
            val isUnlocked = CustomizationUnlocks.isUnlocked(id, unlockedBadges)
            val isSelected = id == selectedId
            
            OptionCard(
                label = label,
                isSelected = isSelected,
                isLocked = !isUnlocked,
                onClick = {
                    if (isUnlocked) {
                        onSelect(id)
                    } else {
                        val item = CustomizationUnlocks.allUnlockables.find { 
                            it.id == id && it.type == type 
                        }
                        if (item != null) {
                            onShowUnlock(item)
                        }
                    }
                },
                progress = if (!isUnlocked && progress != null) {
                    CustomizationUnlocks.getUnlockProgress(id, progress)
                } else null
            )
        }
    }
}

@Composable
private fun StyleOptionsGrid(
    options: List<Pair<String, String>>,
    selectedId: String,
    unlockedBadges: List<String>,
    progress: UserProgress?,
    onSelect: (String) -> Unit,
    onShowUnlock: (UnlockableItem) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (id, label) ->
            val isUnlocked = CustomizationUnlocks.isUnlocked(id, unlockedBadges)
            val isSelected = id == selectedId
            
            OptionCard(
                label = label,
                isSelected = isSelected,
                isLocked = !isUnlocked,
                onClick = {
                    if (isUnlocked) {
                        onSelect(id)
                    } else {
                        val item = CustomizationUnlocks.allUnlockables.find { 
                            it.id == id && it.type == UnlockType.POT_STYLE 
                        }
                        if (item != null) {
                            onShowUnlock(item)
                        }
                    }
                },
                progress = if (!isUnlocked && progress != null) {
                    CustomizationUnlocks.getUnlockProgress(id, progress)
                } else null
            )
        }
    }
}

@Composable
private fun OptionCard(
    label: String,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    progress: Float? = null
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected && !isLocked) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color(0xFF2F5233),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .alpha(if (isLocked) 0.6f else 1f),
        color = when {
            isSelected && !isLocked -> Color(0xFF52796F)
            isLocked -> Color(0xFFE0E0E0)
            else -> Color.White
        },
        tonalElevation = if (isSelected && !isLocked) 4.dp else 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF757575)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSelected && !isLocked -> Color.White
                        isLocked -> Color(0xFF757575)
                        else -> Color(0xFF2F5233)
                    }
                )
            }

            if (isLocked && progress != null && progress > 0f) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = Color(0xFF52796F),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

@Composable
private fun UnlockRequirementDialog(
    item: UnlockableItem,
    progress: UserProgress?,
    onDismiss: () -> Unit
) {
    val badge = CustomizationUnlocks.getRequiredBadge(item.id)
    val unlockProgress = if (progress != null) {
        CustomizationUnlocks.getUnlockProgress(item.id, progress)
    } else 0f
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF757575)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Locked: ${item.name}",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                if (badge != null) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = badge.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            badge.rarity.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = badge.color
                        )
                    }
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (badge != null) {
                    Text(
                        "Unlock by earning:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                    
                    Surface(
                        color = badge.color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                badge.icon,
                                style = MaterialTheme.typography.displaySmall
                            )
                            Column {
                                Text(
                                    badge.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    badge.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                    
                    if (unlockProgress > 0f) {
                        Column {
                            Text(
                                "Progress",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = unlockProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = badge.color,
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${(unlockProgress * 100).toInt()}% Complete",
                                style = MaterialTheme.typography.labelSmall,
                                color = badge.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!")
            }
        }
    )
}
