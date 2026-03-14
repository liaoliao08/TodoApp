package com.example.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.Todo
import com.example.todoapp.data.model.TodoCategory
import com.example.todoapp.data.repository.TodoRepository
import com.example.todoapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow<TodoCategory?>(null)
    val selectedCategory: StateFlow<TodoCategory?> = _selectedCategory.asStateFlow()

    // 当前编辑的待办
    private val _currentEditTodo = MutableStateFlow<Todo?>(null)
    val currentEditTodo: StateFlow<Todo?> = _currentEditTodo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val apiService = RetrofitClient.apiService

    // 搜索结果（结合搜索词和分类）
    val filteredTodos: StateFlow<List<Todo>> = combine(
        _todos,
        _searchQuery,
        _selectedCategory
    ) { todos, query, category ->
        var result = todos

        // 按分类过滤
        if (category != null) {
            result = result.filter { it.category == category }
        }

        // 按搜索词过滤,在标题或者描述里面搜索
        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        result
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 统计信息
    val statistics: StateFlow<Map<String, Int>> = _todos
        .map { todos ->
            val total = todos.size
            val completed = todos.count { it.isCompleted }
            val uncompleted = total - completed
            val workCount = todos.count { it.category == TodoCategory.WORK }
            val lifeCount = todos.count { it.category == TodoCategory.LIFE }
            val studyCount = todos.count { it.category == TodoCategory.STUDY }
            val otherCount = todos.count { it.category == TodoCategory.OTHER }

            mapOf(
                "total" to total,
                "completed" to completed,
                "uncompleted" to uncompleted,
                "work" to workCount,
                "life" to lifeCount,
                "study" to studyCount,
                "other" to otherCount
            )
        }
        // 优化：给统计计算也添加 flowOn，避免主线程耗时
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    init {
        loadTodos()
        syncWithServer()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 更新选中分类
    fun selectCategory(category: TodoCategory?) {
        _selectedCategory.value = category
    }

    // 设置当前编辑的待办
    fun setEditTodo(todo: Todo) {
        _currentEditTodo.value = todo
    }

    // 清除编辑状态
    fun clearEditTodo() {
        _currentEditTodo.value = null
    }

    // 更新待办（用于编辑）
    fun updateTodoById(id: String, title: String, description: String, category: TodoCategory) {
        viewModelScope.launch {
            val todo = _todos.value.find { it.id == id }
            if (todo != null) {
                val updatedTodo = todo.copy(
                    title = title,
                    description = description,
                    category = category
                )
                repository.updateTodo(updatedTodo)
            }
        }
    }

    private fun loadTodos() {
        viewModelScope.launch {
            _isLoading.value = true
            // 优化：仓库层数据流切换到 IO 线程
            repository.getAllTodos()
                .flowOn(Dispatchers.IO)
                .collect { todoList ->
                    _todos.value = todoList
                    _isLoading.value = false
                }
        }
    }

    fun addTodo(title: String, description: String, category: TodoCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            val todo = Todo(
                title = title,
                description = description,
                category = category,
                isCompleted = false,
                isSynced = false,  // 新数据默认未同步
                updatedAt = System.currentTimeMillis()
            )
            repository.insertTodo(todo)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) { // 优化：数据库操作切换到 IO 线程
            repository.deleteTodo(todo)
        }
    }

    fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTodo = todo.copy(
                isCompleted = !todo.isCompleted,
                isSynced = false,  // 修改后重置同步状态
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTodo(updatedTodo)
        }
    }

    fun syncWithServer() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null

            try {
                // 1. 先测试连接
                Log.d("SyncDebug", "开始测试连接...")
                val testResponse = apiService.testConnection()
                Log.d("SyncDebug", "测试连接结果: ${testResponse.code()}")

                if (!testResponse.isSuccessful) {
                    _syncError.value = "连接服务器失败: ${testResponse.code()}"
                    _isSyncing.value = false
                    return@launch
                }

                // 2. 获取本地未同步的数据
                Log.d("SyncDebug", "获取本地未同步数据...")
                val unsyncedTodos = repository.getUnsyncedTodos()
                Log.d("SyncDebug", "未同步数据数量: ${unsyncedTodos.size}")

                // 3. 上传未同步的数据
                if (unsyncedTodos.isNotEmpty()) {
                    Log.d("SyncDebug", "开始上传 ${unsyncedTodos.size} 条数据")
                    val uploadResponse = apiService.uploadTodos(unsyncedTodos)
                    Log.d("SyncDebug", "上传结果: ${uploadResponse.code()}")

                    if (uploadResponse.isSuccessful) {
                        // 上传成功后标记为已同步
                        val ids = unsyncedTodos.map { it.id }
                        repository.markAsSynced(ids)
                        Log.d("SyncDebug", "标记 ${ids.size} 条数据为已同步")
                    } else {
                        Log.d("SyncDebug", "上传失败: ${uploadResponse.errorBody()?.string()}")
                    }
                }
                // 4. 从服务器下载最新数据
                Log.d("SyncDebug", "开始下载数据...")
                val downloadResponse = apiService.downloadTodos()
                Log.d("SyncDebug", "下载结果: ${downloadResponse.code()}")

                if (downloadResponse.isSuccessful) {
                    downloadResponse.body()?.let { serverTodos ->
                        Log.d("SyncDebug", "下载到 ${serverTodos.size} 条数据")
                        // 合并服务器数据到本地
                        mergeWithServerTodos(serverTodos)
                    }
                }

            } catch (e: Exception) {
                Log.e("SyncDebug", "同步异常", e)
                _syncError.value = "同步出错: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }
    private suspend fun mergeWithServerTodos(serverTodos: List<Todo>) {
        // 简单的合并策略：服务器数据直接覆盖本地
        for (serverTodo in serverTodos) {
            val localTodo = repository.getTodoById(serverTodo.id)
            if (localTodo == null) {
                // 本地没有，直接插入
                repository.insertTodo(serverTodo.copy(isSynced = true))
            } else {
                // 本地有，用服务器的覆盖（因为服务器是最新的）
                repository.updateTodo(serverTodo.copy(isSynced = true))
            }
        }
    }
}