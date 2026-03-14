package com.example.todoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
enum class TodoCategory {
    WORK,        // 工作
    LIFE,        // 生活
    STUDY,       // 学习
    OTHER        // 其他
}

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),  // ← 改成 String
    val title: String,
    val description: String,
    val category: TodoCategory = TodoCategory.OTHER,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),

    // 新增同步相关字段
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)