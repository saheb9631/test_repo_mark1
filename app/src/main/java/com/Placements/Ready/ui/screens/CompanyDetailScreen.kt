package com.Placements.Ready.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.data.Company
import com.Placements.Ready.ui.components.PremiumCard
import com.Placements.Ready.ui.theme.*
import com.Placements.Ready.viewmodel.PlacementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(companyName: String, viewModel: PlacementViewModel, onBack: () -> Unit) {
    val details = remember { viewModel.getCompanyDetails(companyName) }
    val company = details.firstOrNull()
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text(companyName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, scrolledContainerColor = surface)
            )
        }
    ) { padding ->
        if (company == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("No details available", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Company Header
                item {
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))
                                    .background(Brush.linearGradient(colors = listOf(NeonPurple.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0.15f)))),
                                contentAlignment = Alignment.Center
                            ) { Text(companyName.first().toString().uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = NeonPurple) }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(companyName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                company.driveMonth?.let { Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium) }
                            }
                        }
                    }
                }

                item { Text("Drive Information", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoChip(modifier = Modifier.weight(1f), label = "Type", value = company.driveType?.replaceFirstChar { it.uppercase() } ?: "N/A", icon = Icons.Filled.Wifi, color = NeonCyan)
                        InfoChip(modifier = Modifier.weight(1f), label = "Rounds", value = "${company.totalRounds ?: "N/A"}", icon = Icons.Filled.Layers, color = NeonOrange)
                    }
                }

                company.jobLocation?.let { loc ->
                    item { PremiumCard { DetailRow(icon = Icons.Filled.LocationOn, label = "Location", value = loc) } }
                }

                if (!company.rolesOffered.isNullOrEmpty()) {
                    item { Text("Roles Offered", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(company.rolesOffered) { role ->
                                SuggestionChip(onClick = {}, label = { Text(role, color = NeonPurple) }, shape = RoundedCornerShape(10.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = NeonPurple.copy(alpha = 0.1f)), border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.2f)))
                            }
                        }
                    }
                }

                if (!company.skillsRequired.isNullOrEmpty()) {
                    item { Text("Skills Required", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(company.skillsRequired) { skill ->
                                SuggestionChip(onClick = {}, label = { Text(skill, color = NeonCyan) }, shape = RoundedCornerShape(10.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = NeonCyan.copy(alpha = 0.1f)), border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f)))
                            }
                        }
                    }
                }

                if (!company.roundDetails.isNullOrEmpty()) {
                    item { Text("Selection Process", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) }
                    itemsIndexed(company.roundDetails) { index, round ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(NeonPurple, NeonCyan))), contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                if (index < (company.roundDetails?.size ?: 0) - 1) {
                                    Box(modifier = Modifier.width(2.dp).height(50.dp).background(NeonPurple.copy(alpha = 0.3f)))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            PremiumCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Column {
                                    Text(round.roundName ?: "Round ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    round.date?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    round.selectedCount?.let { Text("$it selected", fontSize = 13.sp, color = NeonGreen, fontWeight = FontWeight.Medium) }
                                }
                            }
                        }
                    }
                }

                if (company.roundDetails.isNullOrEmpty() && !company.selectionProcessSteps.isNullOrEmpty()) {
                    item { Text("Selection Steps", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) }
                    itemsIndexed(company.selectionProcessSteps) { index, step ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(NeonPurple.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                                Text("${index + 1}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(step, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (!company.driveDates.isNullOrEmpty()) {
                    item { Text("Drive Dates", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) }
                    items(company.driveDates) { date ->
                        PremiumCard(modifier = Modifier.padding(bottom = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp), tint = NeonPurple)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(date, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                item {
                    company.noticeNumber?.let {
                        PremiumCard { DetailRow(icon = Icons.Filled.Numbers, label = "Notice", value = it) }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(NeonPurple.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = NeonPurple)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Text(value, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
