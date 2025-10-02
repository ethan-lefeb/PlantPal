package com.example.plantpal

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PlantPhoto(val id: Long = System.currentTimeMillis(), val uri: Uri)

class AddPlantViewModel : ViewModel() {
    private val _photos = MutableStateFlow<List<PlantPhoto>>(emptyList())
    val photos: StateFlow<List<PlantPhoto>> = _photos

    fun addPhoto(uri: Uri) {
        _photos.value = _photos.value + PlantPhoto(uri = uri)
    }
}