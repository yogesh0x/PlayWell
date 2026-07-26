package com.playwell.music.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.playwell.music.domain.model.Track

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onUnlockVault: (() -> Unit) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Songs", "Folders", "Vault")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PlayWell Offline Library") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { /* Trigger WorkManager manual sync Scan */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan Media Store")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.MusicNote, contentDescription = null)
                                1 -> Icon(Icons.Default.Folder, contentDescription = null)
                                2 -> Icon(Icons.Default.Lock, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> SongListTab()
                1 -> FolderListTab()
                2 -> VaultTab(onUnlockVault)
            }
        }
    }
}

@Composable
fun SongListTab() {
    // Simulated tracks. In production, this binds to Flow from PlaybackViewModel
    val mockTracks = remember {
        listOf(
            Track(id = 1, title = "Neon Horizon", artist = "CyberSynth", album = "Cybernetic Odyssey", durationMs = 145000, fileSize = 24500000, filePath = "/storage/emulated/0/Music/CyberSynth - Neon Horizon.flac", folderPath = "/storage/emulated/0/Music", parentDirectory = "Music", genre = "Synthwave", dateModified = 1717545600, trackNumber = 1, year = 2024, bitrate = 1411, sampleRate = 96000),
            Track(id = 2, title = "Deep Forest Whispers", artist = "Luna Sol", album = "Wilderness Tales", durationMs = 180000, fileSize = 4200000, filePath = "/storage/emulated/0/Downloads/Luna Sol - Deep Forest.mp3", folderPath = "/storage/emulated/0/Downloads", parentDirectory = "Downloads", genre = "Ambient", dateModified = 1717545601, trackNumber = 2, year = 2024, bitrate = 320, sampleRate = 44100),
            Track(id = 3, title = "Midnight Grooves", artist = "Groovy Cat", album = "Sax & Coffee", durationMs = 165000, fileSize = 28200000, filePath = "/storage/emulated/0/Music/Groovy Cat - Midnight Grooves.alac", folderPath = "/storage/emulated/0/Music", parentDirectory = "Music", genre = "Jazz", dateModified = 1717545602, trackNumber = 3, year = 2024, bitrate = 800, sampleRate = 48000)
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(mockTracks) { track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Play track */ }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(track.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${track.artist ?: "Unknown"} • ${track.album ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "${(track.durationMs / 1000) / 60}:${String.format("%02d", (track.durationMs / 1000) % 60)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun FolderListTab() {
    val folders = listOf("/storage/emulated/0/Music", "/storage/emulated/0/Downloads")
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(folders) { path ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* open directory */ }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(path.substringAfterLast("/"), style = MaterialTheme.typography.titleMedium)
                    Text(path, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun VaultTab(onUnlockVault: (() -> Unit) -> Unit) {
    var isUnlocked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isUnlocked) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Biometric Secure Private Vault",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Keep audio records nested inside excluded folders entirely encrypted under Android Keystore keys.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Button(
                onClick = {
                    onUnlockVault {
                        isUnlocked = true
                    }
                }
            ) {
                Text("Unlock with Fingerprint")
            }
        } else {
            Icon(
                Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Decrypted Content View",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                onClick = { isUnlocked = false },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Close and Lock Storage")
            }
        }
    }
}
