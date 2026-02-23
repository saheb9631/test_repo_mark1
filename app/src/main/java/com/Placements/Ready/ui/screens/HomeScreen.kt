package com.Placements.Ready.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Placements.Ready.ui.components.PremiumCard
import com.Placements.Ready.ui.theme.*
import com.Placements.Ready.viewmodel.PlacementViewModel
import com.Placements.Ready.viewmodel.QuestionViewModel

@Composable
fun HomeScreen(
    viewModel: PlacementViewModel,
    questionViewModel: QuestionViewModel,
    navController: NavController
) {
    val totalCompanies by viewModel.totalCompanies.collectAsState()
    val years by viewModel.years.collectAsState()
    val companies by viewModel.companies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val totalSolved by questionViewModel.totalSolved.collectAsState()
    val totalQuestions by questionViewModel.totalQuestions.collectAsState()
    val streak by questionViewModel.streak.collectAsState()

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Prepare. Practice.",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Get Placed.",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlowStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Placement Years",
                    value = if (isLoading) "..." else "${years.size}",
                    icon = Icons.Filled.DateRange,
                    gradient = listOf(GradientPurpleStart, GradientPurpleEnd)
                )
                GlowStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Placements",
                    value = if (totalCompanies == 0 && isLoading) "..." else "$totalCompanies",
                    icon = Icons.Filled.Business,
                    gradient = listOf(GradientBlueStart, GradientBlueEnd)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlowStatCard(
                    modifier = Modifier.weight(1f),
                    title = "DSA Solved",
                    value = "$totalSolved/$totalQuestions",
                    icon = Icons.Filled.Code,
                    gradient = listOf(GradientCyanStart, GradientCyanEnd)
                )
                GlowStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Day Streak",
                    value = if (streak > 0) "$streak 🔥" else "0",
                    icon = Icons.Filled.LocalFireDepartment,
                    gradient = listOf(GradientPinkStart, GradientPinkEnd)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val actions = listOf(
                    Triple("Placements", Icons.Filled.Business, "placements"),
                    Triple("DSA Sheet", Icons.Filled.Code, "planner"),
                    Triple("Daily Plan", Icons.Filled.CalendarToday, "daily_plan"),
                    Triple("Dashboard", Icons.Filled.BarChart, "progress_dashboard"),
                    Triple("Resume", Icons.Filled.Description, "resume"),
                    Triple("Roadmap", Icons.Filled.Map, "roadmap"),
                    Triple("Developer", Icons.Filled.Person, "portfolio")
                )
                items(actions) { (title, icon, route) ->
                    GlassActionChip(title = title, icon = icon) {
                        navController.navigate(route)
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            Text(
                text = "Recent Companies",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonPurple)
                }
            }
        } else {
            items(companies.take(10)) { company ->
                GlassCompanyCard(
                    companyName = company.companyName,
                    driveType = company.driveType,
                    location = company.jobLocation,
                    onClick = {
                        navController.navigate("company_detail/${company.companyName}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun GlowStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>
) {
    Card(
        modifier = modifier.defaultMinSize(minHeight = 130.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = gradient.map { it.copy(alpha = 0.85f) }))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, gradient: List<Color>) =
    GlowStatCard(modifier, title, value, icon, gradient)

@Composable
fun GlassActionChip(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(108.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.2f), NeonCyan.copy(alpha = 0.12f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = NeonCyan, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                title, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionChip(title: String, icon: ImageVector, onClick: () -> Unit) =
    GlassActionChip(title, icon, onClick)

@Composable
fun GlassCompanyCard(companyName: String, driveType: String?, location: String?, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(colors = listOf(NeonCyan.copy(alpha = 0.2f), NeonPurple.copy(alpha = 0.15f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = companyName.first().toString().uppercase(),
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonCyan
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    companyName, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    driveType?.let {
                        Text(
                            it.replaceFirstChar { c -> c.uppercase() },
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    location?.let {
                        if (driveType != null) Text(" • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun CompanyQuickCard(companyName: String, driveType: String?, location: String?, onClick: () -> Unit) =
    GlassCompanyCard(companyName, driveType, location, onClick)
