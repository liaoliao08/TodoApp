package com.example.todoapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // 新增：关键导入
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.data.model.Todo
import com.example.todoapp.data.model.TodoCategory
import com.example.todoapp.ui.component.CategoryRow
import com.example.todoapp.ui.component.SearchBar
import com.example.todoapp.ui.component.StatisticsCard
import com.example.todoapp.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel = viewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    // 1. 修复：统一使用正确的 collectAsStateWithLifecycle（删除重复的 WithLifecycle）
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredTodos by viewModel.filteredTodos.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的待办") },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.padding(16.dp)
            )

            CategoryRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            StatisticsCard(
                statistics = statistics,
                selectedCategory = selectedCategory,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (filteredTodos.isEmpty()) { // 2. 修复：明确 List 类型的 isEmpty
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "没有找到匹配的待办"
                                selectedCategory != null -> "该分类下暂无待办"
                                else -> "暂无待办，点击+添加"
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3. 修复：items 遍历 Todo 列表，参数传递正确的 Todo 对象
                        items(filteredTodos) { todo: Todo ->
                            TodoItem(
                                todo = todo,
                                onToggle = { viewModel.toggleTodoCompletion(todo) },
                                onEdit = {
                                    viewModel.setEditTodo(todo)
                                    onNavigateToEdit()
                                },
                                onDelete = { viewModel.deleteTodo(todo) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (todo.category) {
                                    TodoCategory.WORK -> "工作"
                                    TodoCategory.LIFE -> "生活"
                                    TodoCategory.STUDY -> "学习"
                                    TodoCategory.OTHER -> "其他"
                                    else -> "其他"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}