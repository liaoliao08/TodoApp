package com.example.todoapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todoapp.data.model.TodoCategory

@Composable
fun StatisticsCard(
    statistics: Map<String, Int>,
    selectedCategory: TodoCategory?,  // 新增：当前选中的分类
    modifier: Modifier = Modifier
) {
    // 如果选中了具体分类，不显示统计卡片
    if (selectedCategory != null) {
        return
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "待办统计",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 总体统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = statistics["total"] ?: 0,
                    label = "总数",
                    color = MaterialTheme.colorScheme.primary
                )
                StatisticItem(
                    value = statistics["completed"] ?: 0,
                    label = "已完成",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatisticItem(
                    value = statistics["uncompleted"] ?: 0,
                    label = "未完成",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // 分类统计（只显示数量 > 0 的分类）
            Text(
                text = "分类统计",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 动态生成有数据的分类统计项
            val categoryStats = listOf(
                Triple("work" to (statistics["work"] ?: 0), "工作", MaterialTheme.colorScheme.primary),
                Triple("life" to (statistics["life"] ?: 0), "生活", MaterialTheme.colorScheme.secondary),
                Triple("study" to (statistics["study"] ?: 0), "学习", MaterialTheme.colorScheme.tertiary),
                Triple("other" to (statistics["other"] ?: 0), "其他", MaterialTheme.colorScheme.surfaceVariant)
            ).filter { it.first.second > 0 }  // 只保留数量大于0的分类

            if (categoryStats.isNotEmpty()) {
                // 根据数量动态调整布局
                if (categoryStats.size <= 3) {
                    // 3个以内用Row平均分布
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        categoryStats.forEach { stat ->
                            StatisticItem(
                                value = stat.first.second,
                                label = stat.second,
                                color = stat.third
                            )
                        }
                    }
                } else {
                    // 4个以上用Grid布局
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 第一行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            categoryStats.take(2).forEach { stat ->
                                StatisticItem(
                                    value = stat.first.second,
                                    label = stat.second,
                                    color = stat.third
                                )
                            }
                        }
                        // 第二行
                        if (categoryStats.size > 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                categoryStats.drop(2).forEach { stat ->
                                    StatisticItem(
                                        value = stat.first.second,
                                        label = stat.second,
                                        color = stat.third
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 所有分类都是0
                Text(
                    text = "暂无分类数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    value: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}