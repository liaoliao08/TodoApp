package com.example.todoapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.data.model.Todo
import com.example.todoapp.data.model.TodoCategory
import com.example.todoapp.ui.component.CategoryChip
import com.example.todoapp.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoScreen(
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val currentTodo by viewModel.currentEditTodo.collectAsState()

    // 如果当前没有待办要编辑，直接返回
    if (currentTodo == null) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    var title by remember { mutableStateOf(currentTodo!!.title) }
    var description by remember { mutableStateOf(currentTodo!!.description) }
    var selectedCategory by remember { mutableStateOf(currentTodo!!.category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑待办") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.updateTodoById(
                                    currentTodo!!.id,
                                    title,
                                    description,
                                    selectedCategory
                                )
                                viewModel.clearEditTodo()
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text(
                text = "分类",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoCategory.values().forEach { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        onSelected = { selectedCategory = category },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}