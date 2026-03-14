package com.example.todoapp.data.dao
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.todoapp.data.model.Todo
@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_table ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>

    @Insert
    suspend fun insertTodo(todo: Todo)

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("SELECT * FROM todo_table WHERE id = :id")
    suspend fun getTodoById(id: String): Todo?

    // 新增：查询未同步的数据
    @Query("SELECT * FROM todo_table WHERE isSynced = 0")
    suspend fun getUnsyncedTodos(): List<Todo>

    // 新增：更新同步状态
    @Query("UPDATE todo_table SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}