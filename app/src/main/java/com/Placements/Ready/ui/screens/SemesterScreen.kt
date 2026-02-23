package com.Placements.Ready.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Placements.Ready.ui.components.PremiumCard
import com.Placements.Ready.ui.theme.*

/**
 * Data class representing a semester's metadata for UI display.
 */
data class SemesterInfo(
    val id: String,
    val name: String,
    val year: String,
    val months: List<String>,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

/**
 * Hardcoded semester-to-year/month mapping from the spec.
 */
val semesters = listOf(
    SemesterInfo(
        id = "sem5",
        name = "Semester 5",
        year = "2024",
        months = listOf("July"),
        subtitle = "July 2024",
        icon = Icons.Filled.School,
        gradientColors = listOf(NeonPurple, NeonCyan)
    ),
    SemesterInfo(
        id = "sem6",
        name = "Semester 6",
        year = "2025",
        months = listOf("January", "February", "March", "April", "May", "June"),
        subtitle = "January – June 2025",
        icon = Icons.Filled.TrendingUp,
        gradientColors = listOf(NeonCyan, NeonGreen)
    ),
    SemesterInfo(
        id = "sem7",
        name = "Semester 7",
        year = "2025",
        months = listOf("July", "August", "September", "October", "November", "December"),
        subtitle = "July – December 2025",
        icon = Icons.Filled.WorkspacePremium,
        gradientColors = listOf(NeonOrange, NeonPink)
    ),
    SemesterInfo(
        id = "sem8",
        name = "Semester 8",
        year = "2026",
        months = listOf("January", "February"),
        subtitle = "January – February 2026",
        icon = Icons.Filled.EmojiEvents,
        gradientColors = listOf(NeonPurple, NeonPink)
    )
)

/**
 * Semester selection screen — first level of the placement hierarchy.
 * Shows 4 semester cards that navigate to the month screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterScreen(navController: NavController) {
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(
                "Placement History",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Browse placement drives by semester",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Semester Cards
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(semesters) { index, semester ->
                SemesterCard(
                    semester = semester,
                    index = index,
                    onClick = {
                        navController.navigate("semester_months/${semester.id}")
                    }
                )
            }
            // Bottom spacing
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SemesterCard(semester: SemesterInfo, index: Int, onClick: () -> Unit) {
    // Stagger animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 80L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "semCardScale"
    )

    PremiumCard(
        onClick = onClick,
        modifier = Modifier.scale(scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Gradient icon box
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = semester.gradientColors.map { it.copy(alpha = 0.2f) }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = semester.icon,
                    contentDescription = semester.name,
                    tint = semester.gradientColors.first(),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    semester.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    semester.subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = semester.gradientColors.first().copy(alpha = 0.12f)
                ) {
                    Text(
                        "${semester.months.size} month${if (semester.months.size > 1) "s" else ""}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = semester.gradientColors.first()
                    )
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "View months",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
