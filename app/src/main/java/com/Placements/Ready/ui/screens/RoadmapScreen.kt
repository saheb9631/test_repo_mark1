package com.Placements.Ready.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.ui.theme.*

data class RoadmapItem(val title: String, val description: String, var isCompleted: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(onBack: () -> Unit) {
    val items = remember {
        mutableStateListOf(
            RoadmapItem("Learn a Programming Language", "Master C++, Java, or Python fundamentals"),
            RoadmapItem("Data Structures", "Arrays, Linked Lists, Stacks, Queues, Trees, Graphs"),
            RoadmapItem("Algorithms", "Sorting, Searching, DP, Greedy, Backtracking"),
            RoadmapItem("OOP Concepts", "Inheritance, Polymorphism, Encapsulation, Abstraction"),
            RoadmapItem("Database (SQL)", "Joins, Queries, Normalization, Indexing"),
            RoadmapItem("Operating Systems", "Process, Threads, Memory, Deadlocks"),
            RoadmapItem("Computer Networks", "OSI, TCP/IP, HTTP, DNS"),
            RoadmapItem("System Design Basics", "Scalability, Load Balancing, Caching"),
            RoadmapItem("Projects", "Build 2-3 strong projects with real impact"),
            RoadmapItem("Resume & Portfolio", "Prepare ATS-friendly resume"),
            RoadmapItem("Aptitude & Reasoning", "Practice quantitative, verbal, and logical"),
            RoadmapItem("Mock Interviews", "Practice coding and HR interviews"),
            RoadmapItem("Company Research", "Study past patterns and prepare accordingly")
        )
    }

    val completed = items.count { it.isCompleted }
    val progress = if (items.isNotEmpty()) completed.toFloat() / items.size else 0f
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Preparation Roadmap", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, scrolledContainerColor = surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(colors = listOf(NeonPurple.copy(alpha = 0.1f), NeonCyan.copy(alpha = 0.05f))))
                        .padding(16.dp)
                ) {
                    Column {
                        Row {
                            Text("Progress", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = NeonPurple)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = NeonPurple,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                itemsIndexed(items) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isCompleted) NeonGreen.copy(alpha = 0.06f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, if (item.isCompleted) NeonGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { items[index] = item.copy(isCompleted = it) },
                                colors = CheckboxDefaults.colors(checkedColor = NeonGreen, checkmarkColor = Color.Black, uncheckedColor = MaterialTheme.colorScheme.outline)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${index + 1}. ${item.title}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = if (item.isCompleted) NeonGreen else MaterialTheme.colorScheme.onSurface)
                                Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
