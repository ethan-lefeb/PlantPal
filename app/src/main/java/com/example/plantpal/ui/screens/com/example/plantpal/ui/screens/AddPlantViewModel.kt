package com.example.plantpal

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddPlantUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedPlantId: String? = null
)

class AddPlantViewModel : ViewModel() {
    private val _ui = MutableStateFlow(AddPlantUiState())
    val ui: StateFlow<AddPlantUiState> = _ui.asStateFlow()

    fun resetState() {
        _ui.value = AddPlantUiState()
    }

    private val plantRepo = PlantRepository()

    companion object {
        private const val TAG = "AddPlantVM"
        private const val ENABLE_LOGGING = false
    }

    fun createPlantWithoutPhoto(
        nickname: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _ui.value = AddPlantUiState(isSaving = true)
            if (ENABLE_LOGGING) Log.d(TAG, "Creating plant WITHOUT photo: $nickname")

            try {
                val profile = PlantProfile(
                    commonName = nickname,
                    scientificName = "",
                    notes = notes,
                    photoUrl = "",
                    createdAt = System.currentTimeMillis()
                )

                if (ENABLE_LOGGING) Log.d(TAG, "Calling repository.addPlant...")
                val result = plantRepo.addPlant(profile)

                if (result.isSuccess) {
                    val plantId = result.getOrNull()
                    if (ENABLE_LOGGING) Log.d(TAG, "✅ Success! Plant ID: $plantId")
                    _ui.value = AddPlantUiState(savedPlantId = plantId)
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "❌ Failed: ${error?.message}", error)  // Keep errors
                    _ui.value = AddPlantUiState(error = error?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)  // Keep errors
                _ui.value = AddPlantUiState(error = e.message ?: "Failed to save plant")
            }
        }
    }

    fun createPlantWithPhoto(
        context: Context,
        photoUri: Uri,
        nickname: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _ui.value = AddPlantUiState(isSaving = true)
            if (ENABLE_LOGGING) Log.d(TAG, "Creating plant WITH photo: $nickname")

            try {
                val appCtx = context.applicationContext
                if (ENABLE_LOGGING) Log.d(TAG, "Saving photo locally...")
                val localContentUri = LocalPhotoStore.saveCopyToAppStorage(appCtx, photoUri)
                if (ENABLE_LOGGING) Log.d(TAG, "Photo saved: $localContentUri")

                val profile = PlantProfile(
                    commonName = nickname,
                    scientificName = "",
                    notes = notes,
                    photoUrl = localContentUri.toString(),
                    createdAt = System.currentTimeMillis()
                )

                if (ENABLE_LOGGING) Log.d(TAG, "Calling repository.addPlant...")
                val result = plantRepo.addPlant(profile)

                if (result.isSuccess) {
                    val plantId = result.getOrNull()
                    if (ENABLE_LOGGING) Log.d(TAG, "✅ Success! Plant ID: $plantId")
                    _ui.value = AddPlantUiState(savedPlantId = plantId)
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "❌ Failed: ${error?.message}", error)  // Keep errors
                    _ui.value = AddPlantUiState(error = error?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)  // Keep errors
                _ui.value = AddPlantUiState(error = e.message ?: "Failed to save plant")
            }
        }
    }
}