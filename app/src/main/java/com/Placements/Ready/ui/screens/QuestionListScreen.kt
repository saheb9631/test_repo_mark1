package com.Placements.Ready.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Placements.Ready.data.Question
import com.Placements.Ready.data.QuestionProgress
import com.Placements.Ready.viewmodel.QuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionListScreen(
    viewModel: QuestionViewModel,
    navController: NavController
) {
    val questions by viewModel.filteredQuestions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val totalSolved by viewModel.totalSolved.collectAsState()
    val totalQuestions by viewModel.totalQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val progressVersion by viewModel.progressVersion.collectAsState()

    var showTopicDropdown by remember { mutableStateOf(false) }

    val difficultyOptions = listOf("All", "Easy", "Medium", "Hard")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "DSA Questions",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "$totalSolved / $totalQuestions solved",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Dashboard
                IconButton(onClick = { navController.navigate("progress_dashboard") }) {
                    Icon(Icons.Outlined.BarChart, "Dashboard",
                        tint = MaterialTheme.colorScheme.primary)
                }
                // Daily plan
                IconButton(onClick = { navController.navigate("daily_plan") }) {
                    Icon(Icons.Outlined.CalendarToday, "Daily Plan",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search questions...") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Filled.Clear, "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Difficulty Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(difficultyOptions) { option ->
                val isSelected = selectedDifficulty == option
                val chipColor = when (option) {
                    "Easy" -> Color(0xFF00E676)
                    "Medium" -> Color(0xFFFF9800)
                    "Hard" -> Color(0xFFFF5252)
                    else -> MaterialTheme.colorScheme.primary
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setDifficultyFilter(option) },
                    label = {
                        Text(
                            option,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor.copy(alpha = 0.2f),
                        selectedLabelColor = chipColor,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) chipColor else MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = chipColor,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }

            // Topic filter
            item {
                Box {
                    FilterChip(
                        selected = selectedTopic != "All",
                        onClick = { showTopicDropdown = true },
                        label = {
                            Text(
                                if (selectedTopic == "All") "All Topics" else selectedTopic,
                                maxLines = 1
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                "Select Topic",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = showTopicDropdown,
                        onDismissRequest = { showTopicDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Topics") },
                            onClick = {
                                viewModel.setTopicFilter("All")
                                showTopicDropdown = false
                            }
                        )
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        topic,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                },
                                onClick = {
                                    viewModel.setTopicFilter(topic)
                                    showTopicDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Result count
        Text(
            "${questions.size} questions",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Question List
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.SearchOff, "No results",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No questions found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(questions, key = { it.id }) { question ->
                    QuestionCard(
                        question = question,
                        progress = viewModel.getProgress(question.id),
                        progressVersion = progressVersion,
                        onClick = {
                            navController.navigate("question_detail/${question.id}")
                        },
                        onToggleSolved = {
                            val p = viewModel.getProgress(question.id)
                            if (p.isSolved) {
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

@Composable
private fun QuestionCard(
    question: Question,
    progress: QuestionProgress,
    progressVersion: Int,
    onClick: () -> Unit,
    onToggleSolved: () -> Unit
) {
    // Re-read progress whenever progressVersion changes
    val key = progressVersion

    val diffColor = when (question.difficulty.lowercase()) {
        "easy" -> Color(0xFF00E676)
        "medium" -> Color(0xFFFF9800)
        "hard" -> Color(0xFFFF5252)
        else -> MaterialTheme.colorScheme.primary
    }

    val bgColor by animateColorAsState(
        if (progress.isSolved) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "cardBg"
    )

    // Scale animation for the solve button
    val solveInteraction = remember { MutableInteractionSource() }
    val isSolvePressed by solveInteraction.collectIsPressedAsState()
    val solveScale by animateFloatAsState(
        targetValue = if (isSolvePressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "solveScale"
    )

    // Animated check color
    val checkBgColor by animateColorAsState(
        targetValue = if (progress.isSolved) Color(0xFF00E676).copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surface,
        label = "checkBg"
    )
    val checkIconColor by animateColorAsState(
        targetValue = if (progress.isSolved) Color(0xFF00E676)
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        label = "checkIcon"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tappable solved indicator — quick action
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(solveScale)
                    .clip(CircleShape)
                    .background(checkBgColor)
                    .clickable(
                        interactionSource = solveInteraction,
                        indication = ripple(bounded = true, radius = 16.dp),
                        onClick = onToggleSolved
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (progress.isSolved) Icons.Filled.Check else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (progress.isSolved) "Mark Unsolved" else "Mark Solved",
                    modifier = Modifier.size(18.dp),
                    tint = checkIconColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Question info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    question.problemName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (progress.isSolved)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    question.subTopic,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Status icons
            if (progress.isRevision) {
                Icon(
                    Icons.Filled.Refresh, "Revision",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (progress.isFavorite) {
                Icon(
                    Icons.Filled.Star, "Favorite",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Difficulty badge
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
