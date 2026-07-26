package com.playwell.music.playback

import android.content.Context
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.media.audiofx.LoudnessEnhancer
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEffectsManager @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dsp_settings", Context.MODE_PRIVATE)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var activeSessionId = -1

    fun attachToAudioSession(sessionId: Int) {
        if (sessionId == activeSessionId) return
        activeSessionId = sessionId

        release()

        try {
            // Apply native Android structural audio effects binding to Session ID
            equalizer = Equalizer(0, sessionId).apply {
                enabled = prefs.getBoolean("eq_enabled", false)
                restoreSavedBands(this)
            }

            bassBoost = BassBoost(0, sessionId).apply {
                enabled = prefs.getBoolean("bb_enabled", false)
                setStrength(prefs.getInt("bb_strength", 0).toShort())
            }

            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = prefs.getBoolean("v_enabled", false)
                setStrength(prefs.getInt("v_strength", 0).toShort())
            }

            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = prefs.getBoolean("le_enabled", false)
                setTargetGain(prefs.getInt("le_gain", 0))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerBandValue(band: Short, milliBels: Short) {
        equalizer?.let {
            if (band < it.numberOfBands) {
                it.setBandLevel(band, milliBels)
                prefs.edit().putInt("eq_band_$band", milliBels.toInt()).apply()
            }
        }
    }

    fun toggleEqualizer(enabled: Boolean) {
        equalizer?.enabled = enabled
        prefs.edit().putBoolean("eq_enabled", enabled).apply()
    }

    fun setBassBoost(strength: Short) {
        bassBoost?.let {
            it.enabled = strength > 0
            it.setStrength(strength)
            prefs.edit().putInt("bb_strength", strength.toInt()).putBoolean("bb_enabled", strength > 0).apply()
        }
    }

    fun setVirtualizer(strength: Short) {
        virtualizer?.let {
            it.enabled = strength > 0
            it.setStrength(strength)
            prefs.edit().putInt("v_strength", strength.toInt()).putBoolean("v_enabled", strength > 0).apply()
        }
    }

    private fun restoreSavedBands(eq: Equalizer) {
        for (i in 0 until eq.numberOfBands) {
            val level = prefs.getInt("eq_band_$i", 0).toShort()
            eq.setBandLevel(i.toShort(), level)
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
        
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
}
