package com.playwell.music.di

import android.content.Context
import androidx.room.Room
import com.playwell.music.data.database.PlayWellDatabase
import com.playwell.music.data.database.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlayWellDatabase {
        return Room.databaseBuilder(
            context,
            PlayWellDatabase::class.java,
            "playwell_music.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTrackDao(db: PlayWellDatabase): TrackDao {
        return db.trackDao()
    }
}
