package com.example.todoapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.todoapp.data.model.Todo
import com.example.todoapp.data.dao.TodoDao

@Database(
    entities = [Todo::class],
    version = 2,  // 升级版本号！
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                    .fallbackToDestructiveMigration()  // 简单处理：版本升级时删除旧数据
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}