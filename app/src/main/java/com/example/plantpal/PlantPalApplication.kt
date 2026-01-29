package com.example.plantpal

import android.app.Application
import androidx.work.Configuration

// For some reason, Android needed this added for the app to compile with Firestore's notification function.
// Naming conventions recommend '[AppName]Application', which is obviously kind of confusing with PlantPalApp existing.
// For the most part, though, you don't need to pay much mind to this file. -E

class PlantPalApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
