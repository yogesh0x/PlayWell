package com.playwell.music.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import com.playwell.music.data.database.TrackDao
import com.playwell.music.domain.model.Track
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DifferentialMediaScanner @Inject constructor(
    private val context: Context,
    private val trackDao: TrackDao
) {
    private val supportedFormats = setOf(
        "mp3", "aac", "m4a", "flac", "wav", "ogg", "opus", "wma", "alac", "aiff", "mid", "midi"
    )

    suspend fun scanLocalLibrary(
        minimumSizeKb: Long,
        minimumSecs: Long,
        excludedDirs: List<String>
    ): ScanResult = withContext(Dispatchers.IO) {
        val rootMusicDir = Environment.getExternalStorageDirectory()
        val currentDbState = trackDao.getFilePathAndModifiedMap().associate { it.filePath to it.dateModified }
        
        val discoveredTracks = mutableListOf<Track>()
        val existingActiveFiles = mutableSetOf<String>()
        
        scanDirectoryRecursively(
            directory = rootMusicDir,
            dbState = currentDbState,
            minimumSizeKbs = minimumSizeKb,
            minimumSeconds = minimumSecs,
            excludedDirs = excludedDirs,
            activeFilesOut = existingActiveFiles,
            discoveredTracksOut = discoveredTracks
        )

        // Identify files that exist in the database but have been deleted locally (Delta check)
        val deletedPaths = currentDbState.keys.filterNot { existingActiveFiles.contains(it) }
        
        if (deletedPaths.isNotEmpty()) {
            trackDao.deleteTracksByPaths(deletedPaths)
        }
        
        if (discoveredTracks.isNotEmpty()) {
            trackDao.insertOrUpdateTracks(discoveredTracks)
        }

        ScanResult(
            addedCount = discoveredTracks.count { !currentDbState.containsKey(it.filePath) },
            updatedCount = discoveredTracks.count { currentDbState.containsKey(it.filePath) && currentDbState[it.filePath] != it.dateModified },
            removedCount = deletedPaths.size
        )
    }

    private fun scanDirectoryRecursively(
        directory: File,
        dbState: Map<String, Long>,
        minimumSizeKbs: Long,
        minimumSeconds: Long,
        excludedDirs: List<String>,
        activeFilesOut: MutableSet<String>,
        discoveredTracksOut: MutableList<Track>
    ) {
        if (!directory.exists() || !directory.isDirectory) return
        
        // Anti-leak protection: Skip hidden directories and directories with a .nomedia file
        if (directory.name.startsWith(".") || File(directory, ".nomedia").exists()) return
        
        // Skip user excluded dirs
        if (excludedDirs.any { directory.absolutePath.contains(it) }) return

        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                scanDirectoryRecursively(
                    directory = file,
                    dbState = dbState,
                    minimumSizeKbs = minimumSizeKbs,
                    minimumSeconds = minimumSeconds,
                    excludedDirs = excludedDirs,
                    activeFilesOut = activeFilesOut,
                    discoveredTracksOut = discoveredTracks
                )
            } else {
                val ext = file.extension.lowercase()
                if (supportedFormats.contains(ext)) {
                    activeFilesOut.add(file.absolutePath)
                    
                    val fileLengthKb = file.length() / 1024
                    if (fileLengthKb < minimumSizeKbs) continue

                    val lastModified = file.lastModified()
                    val cachedModTime = dbState[file.absolutePath]

                    // Differential scanning: Only extract from file if new or modified
                    if (cachedModTime == null || cachedModTime != lastModified) {
                        tryMetadataExtraction(file, lastModified, minimumSeconds)?.let { track ->
                            discoveredTracksOut.add(track)
                        }
                    }
                }
            }
        }
    }

    private fun tryMetadataExtraction(file: File, lastModified: Long, minDurSecs: Long): Track? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationMsStr?.toLongOrNull() ?: 0L
            
            if (durationMs < minDurSecs * 1000) return null

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.nameWithoutExtension
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: artist
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            
            Track(
                filePath = file.absolutePath,
                parentDirectory = file.parent ?: "",
                folderPath = file.parentFile?.absolutePath ?: "",
                title = title,
                artist = artist,
                album = album,
                albumArtist = albumArtist,
                genre = genre,
                durationMs = durationMs,
                fileSize = file.length(),
                bitrate = bitrate,
                sampleRate = 44100, // Standard native stereo sample PCM default
                trackNumber = trackNumber,
                year = year,
                dateModified = lastModified,
                isFavorite = false,
                isLocked = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }
}

data class ScanResult(
    val addedCount: Int,
    val updatedCount: Int,
    val removedCount: Int
)
