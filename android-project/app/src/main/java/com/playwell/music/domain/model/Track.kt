package com.playwell.music.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["filePath"], unique = true),
        Index(value = ["folderPath"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isLocked"])
    ]
)
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val parentDirectory: String,
    val folderPath: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val genre: String?,
    val durationMs: Long,
    val fileSize: Long,
    val bitrate: Int,
    val sampleRate: Int,
    val trackNumber: Int?,
    val year: Int?,
    val dateModified: Long,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false, // True if stored in Scoped Private Vault
    
    // Playback telemetry tracker (completely local)
    val playCount: Int = 0,
    val skipCount: Int = 0,
    val lastPlayedTimestamp: Long? = null,
    val totalListeningTimeMs: Long = 0,
    val embeddedArtPath: String? = null
)
