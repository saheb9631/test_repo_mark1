package com.Placements.Ready.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var selectedLevel by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F23),
                        Color(0xFF1A1A3E),
                        Color(0xFF7B2FF2).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF7B2FF2)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Campus Placement\nCompanion",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your one-stop app for placement preparation",
                fontSize = 14.sp,
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "What's your coding level?",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))

            val levels = listOf(
                Triple("Beginner", "Just starting out", Icons.Filled.EmojiEvents),
                Triple("Intermediate", "Know the basics", Icons.Filled.Code),
                Triple("Advanced", "Ready to crack it", Icons.Filled.Rocket)
            )

            levels.forEach { (level, desc, icon) ->
                val isSelected = selectedLevel == level
                Card(
                    onClick = { selectedLevel = level },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF7B2FF2).copy(alpha = 0.3f)
                        else Color(0xFF1A1A2E)
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder()
                            .copy(brush = Brush.linearGradient(listOf(Color(0xFF7B2FF2), Color(0xFF03DAC6))))
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFF7B2FF2) else Color(0xFFB0B0B0),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = level,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = desc,
                                color = Color(0xFFB0B0B0),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF7B2FF2)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onComplete,
                enabled = selectedLevel.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B2FF2)
                )
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
