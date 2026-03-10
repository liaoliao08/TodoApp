package com.example.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.Todo
import com.example.todoapp.data.model.TodoCategory
import com.example.todoapp.data.repository.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 新增：当前选中的分类
    private val _selectedCategory = MutableStateFlow<TodoCategory?>(null)
    val selectedCategory: StateFlow<TodoCategory?> = _selectedCategory.asStateFlow()

    // 新增：当前编辑的待办
    private val _currentEditTodo = MutableStateFlow<Todo?>(null)
    val currentEditTodo: StateFlow<Todo?> = _currentEditTodo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 新增：统计信息
    val statistics: StateFlow<Map<String, Int>> = _todos.map { todos ->
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    init {
        loadTodos()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 新增：更新选中分类
    fun selectCategory(category: TodoCategory?) {
        _selectedCategory.value = category
    }

    // 新增：设置当前编辑的待办
    fun setEditTodo(todo: Todo) {
        _currentEditTodo.value = todo
    }

    // 新增：清除编辑状态
    fun clearEditTodo() {
        _currentEditTodo.value = null
    }

    // 新增：更新待办（用于编辑）
    fun updateTodoById(id: Int, title: String, description: String, category: TodoCategory) {
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
            repository.getAllTodos().collect { todoList ->
                _todos.value = todoList
                _isLoading.value = false
            }
        }
    }

    fun addTodo(title: String, description: String, category: TodoCategory) {
        viewModelScope.launch {
            val todo = Todo(
                title = title,
                description = description,
                category = category,
                isCompleted = false
            )
            repository.insertTodo(todo)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch {
            val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
            repository.updateTodo(updatedTodo)
        }
    }
}