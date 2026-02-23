package com.Placements.Ready.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Placements.Ready.data.Company
import com.Placements.Ready.ui.components.PremiumCard
import com.Placements.Ready.ui.theme.*
import com.Placements.Ready.viewmodel.PlacementViewModel

/**
 * Updated PlacementScreen — shows companies for a specific year+month,
 * grouped into Tech and Non-Tech sections.
 *
 * Navigated to from SemesterMonthsScreen with year and month parameters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementScreen(
    viewModel: PlacementViewModel,
    navController: NavController,
    year: String? = null,
    month: String? = null
) {
    val filteredCompanies by viewModel.filteredCompanies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    // If year and month are provided (navigated from semester flow), load that data
    val displayYear = year ?: ""
    val displayMonth = month ?: ""

    LaunchedEffect(year, month) {
        if (year != null && month != null) {
            viewModel.selectYear(year)
            // Small delay to let months populate, then select month
            kotlinx.coroutines.delay(100)
            viewModel.selectMonth(month)
        }
    }

    // Separate companies by role category
    val techCompanies = filteredCompanies.filter { it.roleCategory == "Tech" }
    val nonTechCompanies = filteredCompanies.filter { it.roleCategory == "Non-Tech" }
    val generalCompanies = filteredCompanies.filter { it.roleCategory == "General" }

    // Section expand states
    var techExpanded by remember { mutableStateOf(true) }
    var nonTechExpanded by remember { mutableStateOf(true) }
    var generalExpanded by remember { mutableStateOf(true) }

    // Determine if we're in the semester flow (have year/month params)
    val isSemesterFlow = year != null && month != null

    Scaffold(
        containerColor = bg,
        topBar = {
            if (isSemesterFlow) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "$displayMonth $displayYear",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "${filteredCompanies.size} companies",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
        ) {
            // Search bar
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = if (isSemesterFlow) 8.dp else 20.dp)) {
                if (!isSemesterFlow) {
                    Text(
                        "Placement History",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Search companies, roles...",
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        cursorColor = NeonPurple,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonPurple)
                }
            } else if (filteredCompanies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No companies found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ── Tech Roles Section ──
                    if (techCompanies.isNotEmpty()) {
                        item {
                            RoleSectionHeader(
                                title = "Tech Roles",
                                count = techCompanies.size,
                                color = NeonCyan,
                                icon = Icons.Filled.Code,
                                expanded = techExpanded,
                                onToggle = { techExpanded = !techExpanded }
                            )
                        }
                        if (techExpanded) {
                            items(techCompanies) { company ->
                                PlacementCompanyCard(
                                    company = company,
                                    onClick = {
                                        navController.navigate("company_detail/${company.companyName}")
                                    }
                                )
                            }
                        }
                    }

                    // ── Non-Tech Roles Section ──
                    if (nonTechCompanies.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            RoleSectionHeader(
                                title = "Non-Tech Roles",
                                count = nonTechCompanies.size,
                                color = NeonOrange,
                                icon = Icons.Filled.BusinessCenter,
                                expanded = nonTechExpanded,
                                onToggle = { nonTechExpanded = !nonTechExpanded }
                            )
                        }
                        if (nonTechExpanded) {
                            items(nonTechCompanies) { company ->
                                PlacementCompanyCard(
                                    company = company,
                                    onClick = {
                                        navController.navigate("company_detail/${company.companyName}")
                                    }
                                )
                            }
                        }
                    }

                    // ── General / Uncategorized Section ──
                    if (generalCompanies.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            RoleSectionHeader(
                                title = "General",
                                count = generalCompanies.size,
                                color = NeonPurple,
                                icon = Icons.Filled.Work,
                                expanded = generalExpanded,
                                onToggle = { generalExpanded = !generalExpanded }
                            )
                        }
                        if (generalExpanded) {
                            items(generalCompanies) { company ->
                                PlacementCompanyCard(
                                    company = company,
                                    onClick = {
                                        navController.navigate("company_detail/${company.companyName}")
                                    }
                                )
                            }
                        }
                    }

                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

/**
 * Section header for Tech / Non-Tech role groups with expand/collapse toggle.
 */
@Composable
fun RoleSectionHeader(
    title: String,
    count: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                "$count",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Company card — kept exactly as before for consistency.
 */
@Composable
fun PlacementCompanyCard(company: Company, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Column {
            if (company.roleCategory != "General") {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (company.roleCategory == "Tech") NeonCyan.copy(alpha = 0.15f) else NeonOrange.copy(alpha = 0.15f),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        company.roleCategory,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (company.roleCategory == "Tech") NeonCyan else NeonOrange
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    NeonPurple.copy(alpha = 0.2f),
                                    NeonCyan.copy(alpha = 0.15f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        company.companyName.first().toString().uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        company.companyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        company.driveType?.let {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (it.lowercase()) {
                                    "virtual" -> NeonCyan.copy(alpha = 0.15f)
                                    "in-person" -> NeonGreen.copy(alpha = 0.15f)
                                    else -> NeonPurple.copy(alpha = 0.15f)
                                },
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    it.replaceFirstChar { c -> c.uppercase() },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = when (it.lowercase()) {
                                        "virtual" -> NeonCyan; "in-person" -> NeonGreen; else -> NeonPurple
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        company.totalRounds?.let {
                            Text(
                                "$it rounds",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            if (!company.rolesOffered.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(company.rolesOffered) { role ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    role,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }
            }

            if (!company.skillsRequired.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = NeonOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        company.skillsRequired.joinToString(", "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
