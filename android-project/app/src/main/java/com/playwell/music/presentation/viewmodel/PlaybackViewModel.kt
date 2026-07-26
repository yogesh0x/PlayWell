package com.playwell.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playwell.music.domain.model.Track
import com.playwell.music.data.database.TrackDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val trackDao: TrackDao
) : ViewModel() {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playMode = MutableStateFlow(PlayMode.REPEAT_ALL)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0L)
    val playbackProgress: StateFlow<Long> = _playbackProgress.asStateFlow()

    fun playTrack(track: Track) {
        viewModelScope.launch {
            _currentTrack.value = track
            _isPlaying.value = true
            // Save local listening logs strictly offline
            trackDao.incrementPlayStats(track.id, System.currentTimeMillis(), track.durationMs)
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun playNext() {
        val currList = _queue.value
        val currIdx = currList.indexOf(_currentTrack.value)
        if (currIdx != -1 && currIdx < currList.size - 1) {
            _currentTrack.value = currList[currIdx + 1]
        } else if (_playMode.value == PlayMode.REPEAT_ALL && currList.isNotEmpty()) {
            _currentTrack.value = currList[0]
        }
    }

    fun playPrevious() {
        val currList = _queue.value
        val currIdx = currList.indexOf(_currentTrack.value)
        if (currIdx > 0) {
            _currentTrack.value = currList[currIdx - 1]
        }
    }

    fun shuffleQueue() {
        _queue.value = _queue.value.shuffled()
    }

    fun toggleRepeatMode() {
        _playMode.value = when (_playMode.value) {
            PlayMode.REPEAT_NONE -> PlayMode.REPEAT_ALL
            PlayMode.REPEAT_ALL -> PlayMode.REPEAT_ONE
            PlayMode.REPEAT_ONE -> PlayMode.REPEAT_NONE
        }
    }
}

enum class PlayMode {
    REPEAT_NONE,
    REPEAT_ONE,
    REPEAT_ALL
}
