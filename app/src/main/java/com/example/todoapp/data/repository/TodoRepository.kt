package com.example.todoapp.data.repository
import com.example.todoapp.data.dao.TodoDao
import com.example.todoapp.data.model.Todo
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()

    suspend fun insertTodo(todo: Todo) {
        todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }

    suspend fun getTodoById(id: String): Todo? = todoDao.getTodoById(id)


    // 新增
    suspend fun getUnsyncedTodos(): List<Todo> = todoDao.getUnsyncedTodos()

    // 新增
    suspend fun markAsSynced(ids: List<String>) = todoDao.markAsSynced(ids)
}