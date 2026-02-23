package com.Placements.Ready.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Placements.Ready.ui.SubScreen
import com.Placements.Ready.ui.theme.*

@Composable
fun MoreScreen(navController: NavController) {
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("More", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(20.dp))

        val menuItems = listOf(
            Triple("Roadmap", Icons.Filled.Map, SubScreen.Roadmap.route),
            Triple("Developer", Icons.Filled.Person, SubScreen.Portfolio.route),
            Triple("Settings", Icons.Filled.Settings, SubScreen.Settings.route)
        )
        val itemColors = listOf(NeonPurple, NeonCyan, NeonOrange)

        menuItems.forEachIndexed { index, (title, icon, route) ->
            val accent = itemColors[index]
            Card(
                onClick = { navController.navigate(route) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
