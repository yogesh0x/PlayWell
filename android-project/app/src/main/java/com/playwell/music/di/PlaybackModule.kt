package com.playwell.music.di

import android.content.Context
import com.playwell.music.playback.AudioEffectsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun provideAudioEffectsManager(@ApplicationContext context: Context): AudioEffectsManager {
        return AudioEffectsManager(context)
    }
}
