package com.Placements.Ready.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CodingTask(val title: String, val topic: String, val difficulty: String, var isCompleted: Boolean = false)

@Composable
fun CodingPlannerScreen() {
    val tasks = remember {
        mutableStateListOf(
            CodingTask("Two Sum", "Arrays", "Easy"),
            CodingTask("Valid Parentheses", "Stack", "Easy"),
            CodingTask("Merge Two Sorted Lists", "Linked List", "Easy"),
            CodingTask("Binary Search", "Searching", "Easy"),
            CodingTask("Maximum Subarray", "Dynamic Programming", "Medium"),
            CodingTask("Longest Palindromic Substring", "Strings", "Medium"),
            CodingTask("3Sum", "Arrays", "Medium"),
            CodingTask("Binary Tree Level Order", "Trees", "Medium"),
            CodingTask("LRU Cache", "Design", "Hard"),
            CodingTask("Merge K Sorted Lists", "Heap", "Hard"),
            CodingTask("Reverse Linked List", "Linked List", "Easy"),
            CodingTask("Climbing Stairs", "DP", "Easy"),
            CodingTask("Coin Change", "DP", "Medium"),
            CodingTask("Word Break", "DP", "Medium"),
            CodingTask("Number of Islands", "Graph", "Medium")
        )
    }

    val completedCount = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Coding Planner", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Daily Practice Tasks", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progress", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("$completedCount/${tasks.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            itemsIndexed(tasks) { index, task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { tasks[index] = task.copy(isCompleted = it) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            Text(task.topic, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (task.difficulty) {
                                "Easy" -> Color(0xFF00E676).copy(alpha = 0.2f)
                                "Medium" -> Color(0xFFFF6B35).copy(alpha = 0.2f)
                                "Hard" -> Color(0xFFFF5252).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                task.difficulty,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (task.difficulty) {
                                    "Easy" -> Color(0xFF00E676)
                                    "Medium" -> Color(0xFFFF6B35)
                                    "Hard" -> Color(0xFFFF5252)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
