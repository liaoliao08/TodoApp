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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.data.model.TodoCategory
import com.example.todoapp.viewmodel.TodoViewModel
import com.example.todoapp.data.model.Todo
import com.example.todoapp.ui.component.SearchBar
import com.example.todoapp.ui.component.CategoryRow
import com.example.todoapp.ui.component.StatisticsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel = viewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: () -> Unit  // 新增：跳转到编辑页面
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredTodos by viewModel.filteredTodos.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(//脚手架,负责为通用区域留出固定位置，比如顶部栏，悬浮按钮，底部栏，侧边栏，底部提示框
        topBar = { //顶部导航栏
            TopAppBar(
                title = { Text("我的待办") },
                actions = { //用于设置「导航栏右侧的操作按钮组」
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
            // 搜索栏
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.padding(16.dp)
            )

            // 分类选择行
            CategoryRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 统计卡片
            StatisticsCard(
                statistics = statistics,
                selectedCategory = selectedCategory,  // 传入当前选中的分类
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 待办列表
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (filteredTodos.isEmpty()) {
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
                } else { //加载完了，且列表不为空
                    LazyColumn( //LazyColumn 是 Compose 中「垂直懒加载列表」，对应传统 Android 的 RecyclerView，核心优势是「只渲染屏幕可见的列表项」，即使列表有上万条数据也不卡顿
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTodos) { todo ->//相当于 Java 中的 for (Todo todo : filteredTodos) { ... }
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
    onEdit: () -> Unit,  // 新增：编辑回调
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(//// 左侧：复选框 + 分类 + 标题 + 描述
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(//复选框
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    // 显示分类标签
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (todo.category) {
                                    TodoCategory.WORK -> "工作"
                                    TodoCategory.LIFE -> "生活"
                                    TodoCategory.STUDY -> "学习"
                                    TodoCategory.OTHER -> "其他"
                                    else -> "其他"  // ← 添加 else 分支
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
            Row {//右侧，编辑按钮，和删除按钮
                // 编辑按钮
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                // 删除按钮
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}