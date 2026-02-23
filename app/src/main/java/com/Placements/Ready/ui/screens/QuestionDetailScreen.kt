package com.Placements.Ready.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.viewmodel.QuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailScreen(
    questionId: String,
    viewModel: QuestionViewModel,
    onBack: () -> Unit
) {
    val question = viewModel.getQuestionById(questionId)
    val progressVersion by viewModel.progressVersion.collectAsState()

    if (question == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Question not found")
        }
        return
    }

    // Re-read progress every time progressVersion changes → buttons always reflect truth
    val progress = remember(progressVersion) { viewModel.getProgress(question.id) }

    val diffColor = when (question.difficulty.lowercase()) {
        "easy" -> Color(0xFF00E676)
        "medium" -> Color(0xFFFF9800)
        "hard" -> Color(0xFFFF5252)
        else -> MaterialTheme.colorScheme.primary
    }

    var userNotes by remember(question.id) { mutableStateOf(progress.notes) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question Detail", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Difficulty badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = diffColor.copy(alpha = 0.15f)
            ) {
                Text(
                    question.difficulty,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = diffColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Problem Name
            Text(
                question.problemName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Topic info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Topic, "Topic",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Step ${question.stepNumber}: ${question.topic}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Label, "SubTopic",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            question.subTopic,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Text(
                "Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Solved / Unsolved + Revision + Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Solved / Unsolved toggle
                if (progress.isSolved) {
                    // Show Unsolved button to reset
                    AnimatedActionButton(
                        modifier = Modifier.weight(1f),
                        text = "Unsolved",
                        icon = Icons.AutoMirrored.Filled.Undo,
                        isActive = false,
                        activeColor = Color(0xFFFF5252),
                        inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { viewModel.markUnsolved(question.id) }
                    )
                } else {
                    // Show Solved button
                    AnimatedActionButton(
                        modifier = Modifier.weight(1f),
                        text = "Solved",
                        icon = Icons.Filled.Check,
                        isActive = false,
                        activeColor = Color(0xFF00E676),
                        inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { viewModel.toggleSolved(question.id) }
                    )
                }

                // Revision
                AnimatedActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Revision",
                    icon = Icons.Filled.Refresh,
                    isActive = progress.isRevision,
                    activeColor = Color(0xFFFF9800),
                    inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { viewModel.toggleRevision(question.id) }
                )

                // Favorite
                AnimatedActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Favorite",
                    icon = if (progress.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    isActive = progress.isFavorite,
                    activeColor = Color(0xFFFFD700),
                    inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { viewModel.toggleFavorite(question.id) }
                )
            }

            // Status indicator
            if (progress.isSolved) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF00E676).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle, "Solved",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Solved" + if (progress.solvedDate != null) " on ${progress.solvedDate}" else "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Solutions Section ───────────────────────────────────
            if (question.solutions.isNotEmpty()) {
                Text(
                    "Solutions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                var selectedApproachIdx by remember { mutableIntStateOf(0) }
                var selectedLang by remember { mutableStateOf("cpp") }

                val approaches = question.solutions.map { it.approach }

                // Approach tabs (Brute / Better / Optimal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    approaches.forEachIndexed { idx, approach ->
                        val approachColor = when (approach.lowercase()) {
                            "brute" -> Color(0xFFFF5252)
                            "better" -> Color(0xFFFF9800)
                            "optimal" -> Color(0xFF00E676)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        val isSelected = idx == selectedApproachIdx

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedApproachIdx = idx },
                            label = {
                                Text(
                                    approach,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = approachColor.copy(alpha = 0.2f),
                                selectedLabelColor = approachColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = approachColor.copy(alpha = 0.3f),
                                selectedBorderColor = approachColor,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val currentSolution = question.solutions.getOrNull(selectedApproachIdx)
                if (currentSolution != null) {
                    // Explanation
                    if (currentSolution.explanation.isNotBlank()) {
                        Text(
                            currentSolution.explanation,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Complexity badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentSolution.timeComplexity.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF7C4DFF).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "⏱ ${currentSolution.timeComplexity}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF7C4DFF)
                                )
                            }
                        }
                        if (currentSolution.spaceComplexity.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF00BCD4).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "💾 ${currentSolution.spaceComplexity}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF00BCD4)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language selector
                    val languages = listOf("cpp" to "C++", "java" to "Java", "python" to "Python")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { (key, label) ->
                            val isLangSelected = selectedLang == key
                            val hasCode = currentSolution.code[key]?.isNotBlank() == true

                            FilterChip(
                                selected = isLangSelected,
                                onClick = { if (hasCode) selectedLang = key },
                                label = {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isLangSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                enabled = hasCode,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Code block
                    val codeText = currentSolution.code[selectedLang] ?: "No code available"
                    val clipboardManager = LocalClipboardManager.current

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E2E)
                        )
                    ) {
                        Column {
                            // Copy button header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedLang.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF888888)
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(codeText))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.ContentCopy,
                                        "Copy",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF888888)
                                    )
                                }
                            }

                            // Code content
                            val codeScrollState = rememberScrollState()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .horizontalScroll(rememberScrollState())
                                    .verticalScroll(codeScrollState)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = codeText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFCDD6F4)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // User Notes
            Text(
                "Your Notes",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userNotes,
                onValueChange = { userNotes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text("Add your notes, approach, or key observations...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveNotes(question.id, userNotes) },
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Save, "Save", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Notes")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AnimatedActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.22f) else inactiveColor,
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contentColor"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 8.dp),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isActive) 2.dp else 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(icon, text, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
