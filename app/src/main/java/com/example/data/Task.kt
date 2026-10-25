package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // "High", "Medium", "Low"
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val category: String = "General", // e.g., "Work", "Personal", "Health", "Leisure"
    val dateCreated: Long = System.currentTimeMillis()
)
