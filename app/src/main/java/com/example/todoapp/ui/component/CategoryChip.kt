package com.example.todoapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todoapp.data.model.TodoCategory

@Composable
fun CategoryChip(
    category: TodoCategory,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (category) {
        TodoCategory.WORK -> MaterialTheme.colorScheme.primaryContainer
        TodoCategory.LIFE -> MaterialTheme.colorScheme.secondaryContainer
        TodoCategory.STUDY -> MaterialTheme.colorScheme.tertiaryContainer
        TodoCategory.OTHER -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (category) {
        TodoCategory.WORK -> MaterialTheme.colorScheme.primary
        TodoCategory.LIFE -> MaterialTheme.colorScheme.secondary
        TodoCategory.STUDY -> MaterialTheme.colorScheme.tertiary
        TodoCategory.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = {
            Text(
                text = when (category) {
                    TodoCategory.WORK -> "工作"
                    TodoCategory.LIFE -> "生活"
                    TodoCategory.STUDY -> "学习"
                    TodoCategory.OTHER -> "其他"
                },
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    contentColor
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = backgroundColor,
            selectedLabelColor = contentColor
        )
    )
}

@Composable
fun CategoryRow(
    selectedCategory: TodoCategory?,
    onCategorySelected: (TodoCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = when (selectedCategory) {
            null -> 0
            TodoCategory.WORK -> 1
            TodoCategory.LIFE -> 2
            TodoCategory.STUDY -> 3
            TodoCategory.OTHER -> 4
        },
        modifier = modifier,
        divider = {},
        edgePadding = 16.dp
    ) {
        // "全部" 选项
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("全部") },
            modifier = Modifier.padding(end = 8.dp)
        )

        // 各个分类
        TodoCategory.values().forEach { category ->
            CategoryChip(
                category = category,
                isSelected = selectedCategory == category,
                onSelected = { onCategorySelected(category) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}