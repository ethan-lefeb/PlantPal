package com.example.plantpal.ui.theme

import android.content.Context
import android.content.SharedPreferences

class ScalePreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "plantpal_scale_prefs"
        private const val KEY_SCALE = "ui_scale"
        private const val DEFAULT_SCALE = 1.0f
    }

    fun getScale(): Float {
        return prefs.getFloat(KEY_SCALE, DEFAULT_SCALE)
    }

    fun setScale(scale: Float) {
        prefs.edit().putFloat(KEY_SCALE, scale).apply()
    }

    fun getUIScale(): UIScale {
        return UIScale.fromScaleFactor(getScale())
    }

    fun setUIScale(uiScale: UIScale) {
        setScale(uiScale.scaleFactor)
    }

    fun resetScale() {
        setScale(DEFAULT_SCALE)
    }
}
