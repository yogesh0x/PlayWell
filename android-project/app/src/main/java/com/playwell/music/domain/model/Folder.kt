package com.playwell.music.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "folders",
    indices = [Index(value = ["absolutePath"], unique = true)]
)
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val absolutePath: String,
    val displayName: String,
    val songCount: Int,
    val lastModified: Long,
    val isExcluded: Boolean = false,
    val isLocked: Boolean = false
)

@Entity(tableName = "folder_exclusions")
data class FolderExclusion(
    @PrimaryKey val folderPath: String,
    val dateAdded: Long = System.currentTimeMillis()
)
