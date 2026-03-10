package com.example.todoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TodoCategory {
    WORK,        // 工作
    LIFE,        // 生活
    STUDY,       // 学习
    OTHER        // 其他
}

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: TodoCategory = TodoCategory.OTHER,  // 新增：分类
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)