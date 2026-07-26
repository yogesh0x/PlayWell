package com.playwell.music.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.playwell.music.domain.model.Track
import com.playwell.music.domain.model.Playlist
import com.playwell.music.domain.model.PlaylistTrack
import com.playwell.music.domain.model.Folder
import com.playwell.music.domain.model.FolderExclusion

@Database(
    entities = [
        Track::class,
        Playlist::class,
        PlaylistTrack::class,
        Folder::class,
        FolderExclusion::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PlayWellDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
