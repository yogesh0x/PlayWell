package com.playwell.music.data.database

import androidx.room.*
import com.playwell.music.domain.model.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks WHERE isLocked = 0 ORDER BY title ASC")
    fun getAllTracksFlow(): Flow<List<Track>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksSnapshot(): List<Track>

    @Query("SELECT filePath, dateModified FROM tracks")
    suspend fun getFilePathAndModifiedMap(): List<FilePathAndModified>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTracks(tracks: List<Track>)

    @Query("DELETE FROM tracks WHERE filePath IN (:filePaths)")
    suspend fun deleteTracksByPaths(filePaths: List<String>)

    @Query("SELECT * FROM tracks WHERE isLocked = 0 AND title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    suspend fun searchTracksInstant(query: String): List<Track>

    @Query("SELECT * FROM tracks WHERE folderPath = :folderPath AND isLocked = 0 ORDER BY title ASC")
    fun getTracksByFolderFlow(folderPath: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 AND isLocked = 0")
    fun getFavoritesFlow(): Flow<List<Track>>

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :trackId")
    suspend fun updateFavoriteStatus(trackId: Long, isFavorite: Boolean)

    @Query("UPDATE tracks SET isLocked = :isLocked WHERE folderPath = :folderPath")
    suspend fun updateLockedStatusForFolder(folderPath: String, isLocked: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp, totalListeningTimeMs = totalListeningTimeMs + :duration WHERE id = :trackId")
    suspend fun incrementPlayStats(trackId: Long, timestamp: Long, duration: Long)

    @Query("UPDATE tracks SET skipCount = skipCount + 1 WHERE id = :trackId")
    suspend fun incrementSkipStats(trackId: Long)

    @Query("SELECT * FROM tracks WHERE isLocked = 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayedFlow(limit: Int): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isLocked = 0 ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedFlow(limit: Int): Flow<List<Track>>
}

data class FilePathAndModified(
    val filePath: String,
    val dateModified: Long
)
