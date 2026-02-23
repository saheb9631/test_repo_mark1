package com.Placements.Ready.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.ui.theme.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onSignOut: () -> Unit = {}) {
    val themePrefs = LocalThemePreferences.current
    val isDark = themePrefs?.isDarkMode?.value ?: false
    var notifications by remember { mutableStateOf(true) }
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, scrolledContainerColor = surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Dark Mode Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            if (isDark) "Neon dark theme" else "Light theme",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { themePrefs?.setDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonPurple,
                            checkedTrackColor = NeonPurple.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notifications Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notifications", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Push notifications", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = notifications,
                        onCheckedChange = { notifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Campus Placement Companion v1.0.0", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Helping students prepare for placements", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign Out Button
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPink.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sign Out", fontWeight = FontWeight.SemiBold, color = NeonPink)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
