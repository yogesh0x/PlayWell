package com.playwell.music.data.scanner

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LibraryScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scanner: DifferentialMediaScanner
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Read optional sizes parameters
            val minSizeKb = inputData.getLong("MIN_SIZE_KB", 500L)
            val minDurationSec = inputData.getLong("MIN_DURATION_SEC", 30L)
            val excludedDirs = inputData.getStringArray("EXCLUDED_DIRS")?.toList() ?: emptyList()

            scanner.scanLocalLibrary(
                minimumSizeKb = minSizeKb,
                minimumSecs = minDurationSec,
                excludedDirs = excludedDirs
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
