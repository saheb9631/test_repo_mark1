package com.Placements.Ready.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import com.Placements.Ready.data.Question
import com.Placements.Ready.viewmodel.QuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPlanScreen(
    viewModel: QuestionViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val dailyQuestions by viewModel.dailyPlanQuestions.collectAsState()
    val progressVersion by viewModel.progressVersion.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDailyPlan()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Practice Plan", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Progress Card
            val completedCount = remember(dailyQuestions, progressVersion) {
                dailyQuestions.count { q ->
                    viewModel.getProgress(q.id).isSolved
                }
            }
            val totalPlan = dailyQuestions.size
            val progress = if (totalPlan > 0) completedCount.toFloat() / totalPlan else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CalendarToday, "Plan",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Today's Progress", fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            "$completedCount/$totalPlan",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    if (completedCount == totalPlan && totalPlan > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "🎉 All done for today! Great job!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "3 Easy • 3 Medium • 2 Hard",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (dailyQuestions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.EmojiEvents, "Complete",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "All questions solved! 🎊",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(dailyQuestions, key = { it.id }) { question ->
                        val isSolved = remember(progressVersion) {
                            viewModel.getProgress(question.id).isSolved
                        }

                        DailyPlanCard(
                            question = question,
                            isSolved = isSolved,
                            progressVersion = progressVersion,
                            onTap = {
                                navController.navigate("question_detail/${question.id}")
                            },
                            onToggleSolved = {
                                if (isSolved) {
                                    viewModel.markUnsolved(question.id)
                                } else {
                                    viewModel.toggleSolved(question.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPlanCard(
    question: Question,
    isSolved: Boolean,
    progressVersion: Int,
    onTap: () -> Unit,
    onToggleSolved: () -> Unit
) {
    val diffColor = when (question.difficulty.lowercase()) {
        "easy" -> Color(0xFF00E676)
        "medium" -> Color(0xFFFF9800)
        "hard" -> Color(0xFFFF5252)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSolved)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSolved,
                onCheckedChange = { onToggleSolved() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF00E676),
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    question.problemName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isSolved) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    question.topic,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = diffColor.copy(alpha = 0.15f)
            ) {
                Text(
                    question.difficulty,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = diffColor
                )
            }
        }
    }
}
