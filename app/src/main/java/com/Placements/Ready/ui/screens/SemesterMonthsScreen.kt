package com.Placements.Ready.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Placements.Ready.ui.components.PremiumCard
import com.Placements.Ready.ui.theme.*
import com.Placements.Ready.viewmodel.PlacementViewModel

/**
 * Month selection screen for a given semester.
 * Shows available months as a grid of clickable cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterMonthsScreen(
    semesterId: String,
    viewModel: PlacementViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val semester = semesters.find { it.id == semesterId } ?: return

    // Load months from repo for this year to check which months actually have data
    val repoMonths by viewModel.months.collectAsState()

    LaunchedEffect(semester.year) {
        viewModel.selectYear(semester.year)
    }

    // Only show months that exist in both the semester mapping AND the actual data
    val availableMonths = semester.months.filter { it in repoMonths }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            semester.name,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            semester.subtitle,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    scrolledContainerColor = surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
        ) {
            if (availableMonths.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No placement data for this semester",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // Month count subtitle
                Text(
                    "${availableMonths.size} month${if (availableMonths.size > 1) "s" else ""} with placement data",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                // Month grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(availableMonths) { index, month ->
                        MonthCard(
                            month = month,
                            index = index,
                            accentColor = semester.gradientColors.first(),
                            onClick = {
                                navController.navigate(
                                    "placement_month/${semester.year}/$month"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCard(month: String, index: Int, accentColor: Color, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "monthScale"
    )

    val monthIcons = mapOf(
        "January" to Icons.Filled.AcUnit,
        "February" to Icons.Filled.Favorite,
        "March" to Icons.Filled.FilterVintage,
        "April" to Icons.Filled.WbSunny,
        "May" to Icons.Filled.Park,
        "June" to Icons.Filled.BeachAccess,
        "July" to Icons.Filled.WaterDrop,
        "August" to Icons.Filled.Cloud,
        "September" to Icons.Filled.MenuBook,
        "October" to Icons.Filled.Celebration,
        "November" to Icons.Filled.LocalFireDepartment,
        "December" to Icons.Filled.CardGiftcard
    )

    PremiumCard(
        onClick = onClick,
        modifier = Modifier.scale(scale)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = monthIcons[month] ?: Icons.Filled.CalendarMonth,
                    contentDescription = month,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                month,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                month.take(3).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
