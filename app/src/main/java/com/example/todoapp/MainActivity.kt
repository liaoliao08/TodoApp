package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.repository.TodoRepository
import com.example.todoapp.ui.screen.AddTodoScreen
import com.example.todoapp.ui.screen.EditTodoScreen  // 新增
import com.example.todoapp.ui.screen.TodoListScreen
import com.example.todoapp.ui.theme.TodoAppTheme
import com.example.todoapp.viewmodel.TodoViewModel
import com.example.todoapp.viewmodel.TodoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TodoDatabase.getInstance(this)
        val repository = TodoRepository(database.todoDao())

        setContent {
            TodoAppTheme {
                val navController = rememberNavController()
                val viewModel: TodoViewModel = viewModel(
                    factory = TodoViewModelFactory(repository)
                )

                NavHost(
                    navController = navController,
                    startDestination = "todo_list"
                ) {
                    composable("todo_list") {
                        TodoListScreen(
                            viewModel = viewModel,
                            onNavigateToAdd = {
                                navController.navigate("add_todo")
                            },
                            onNavigateToEdit = {
                                navController.navigate("edit_todo")
                            }
                        )
                    }
                    composable("add_todo") {
                        AddTodoScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("edit_todo") {  // 新增编辑页面路由
                        EditTodoScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}