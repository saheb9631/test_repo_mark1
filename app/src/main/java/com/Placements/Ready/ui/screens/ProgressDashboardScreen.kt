package com.Placements.Ready.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.viewmodel.QuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboardScreen(
    viewModel: QuestionViewModel,
    onBack: () -> Unit
) {
    val totalSolved by viewModel.totalSolved.collectAsState()
    val totalQuestions by viewModel.totalQuestions.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val easySolved by viewModel.easySolved.collectAsState()
    val mediumSolved by viewModel.mediumSolved.collectAsState()
    val hardSolved by viewModel.hardSolved.collectAsState()
    val easyTotal by viewModel.easyTotal.collectAsState()
    val mediumTotal by viewModel.mediumTotal.collectAsState()
    val hardTotal by viewModel.hardTotal.collectAsState()
    val progressVersion by viewModel.progressVersion.collectAsState()

    val topicProgress = remember(progressVersion) { viewModel.getTopicProgress() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Overall Progress Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Overall Progress", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Circular-style big number
                        Text(
                            "$totalSolved",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "out of $totalQuestions solved",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val overallProgress = if (totalQuestions > 0)
                            totalSolved.toFloat() / totalQuestions else 0f
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Streak
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocalFireDepartment, "Streak",
                                tint = Color(0xFFFF6B35),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "$streak day streak",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF6B35)
                            )
                        }
                    }
                }
            }

            // Difficulty Breakdown
            item {
                Text("Difficulty Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DifficultyCard(
                        modifier = Modifier.weight(1f),
                        label = "Easy",
                        solved = easySolved,
                        total = easyTotal,
                        color = Color(0xFF00E676)
                    )
                    DifficultyCard(
                        modifier = Modifier.weight(1f),
                        label = "Medium",
                        solved = mediumSolved,
                        total = mediumTotal,
                        color = Color(0xFFFF9800)
                    )
                    DifficultyCard(
                        modifier = Modifier.weight(1f),
                        label = "Hard",
                        solved = hardSolved,
                        total = hardTotal,
                        color = Color(0xFFFF5252)
                    )
                }
            }

            // Topic-wise Progress
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Topic-wise Progress", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            items(topicProgress) { item ->
                TopicProgressRow(
                    topic = item.topic,
                    solved = item.solved,
                    total = item.total
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    modifier: Modifier = Modifier,
    label: String,
    solved: Int,
    total: Int,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "$solved/$total",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            val progress = if (total > 0) solved.toFloat() / total else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = color,
                trackColor = color.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
private fun TopicProgressRow(
    topic: String,
    solved: Int,
    total: Int
) {
    val progress = if (total > 0) solved.toFloat() / total else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    topic,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    "$solved/$total ($percentage%)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
