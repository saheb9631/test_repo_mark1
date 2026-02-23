package com.Placements.Ready.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.R
import com.Placements.Ready.ui.components.PremiumCard
import com.Placements.Ready.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var tapCount by remember { mutableIntStateOf(0) }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image with Easter Egg & Neon Glow
            Box(contentAlignment = Alignment.Center) {
                // Neon Glow (Simpler, tighter)
                Box(
                    modifier = Modifier
                        .size(140.dp) // Outer bounds slightly larger than image
                        .blur(8.dp) // Reduced blur for a sharper, simpler glow
                        .padding(10.dp) // Reduced padding
                        .background(
                            Brush.linearGradient(colors = listOf(NeonCyan, NeonPurple, NeonPink)),
                            shape = CircleShape
                        )
                )
                // Profile Image
                Image(
                    painter = painterResource(id = R.drawable.developer_image),
                    contentDescription = "Developer Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(colors = listOf(NeonCyan, NeonPurple, NeonPink)),
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            tapCount++
                            if (tapCount == 5) {
                                Toast.makeText(context, "Thanks for checking out the developer \uD83D\uDE80", Toast.LENGTH_SHORT).show()
                                tapCount = 0
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Saheb Ansari",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Android Developer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = NeonCyan,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bio Card - matching App theme (PremiumCard)
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GSOC ’24, GCRF ’22 Global Rank 17 (AIR 3), 30 Days of Google Cloud ’21 & ’22, YouTuber (230K+), Android Developer, Cloud Engineer, Open-Source Contributor (Hacktoberfest)",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Social Links - matching App theme list items
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SocialButton(
                    icon = Icons.Filled.Person,
                    text = "LinkedIn",
                    accent = NeonPurple,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://in.linkedin.com/in/saheb-ansari-"))
                        context.startActivity(intent)
                    }
                )
                SocialButton(
                    icon = Icons.Filled.Language,
                    text = "Portfolio Website",
                    accent = NeonCyan,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://saheb-portfolio-z.vercel.app/"))
                        context.startActivity(intent)
                    }
                )
                SocialButton(
                    icon = Icons.Filled.Email,
                    text = "Email Me",
                    accent = NeonPink,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:saheb963195@gmail.com")
                        }
                        context.startActivity(intent)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SocialButton(
    icon: ImageVector,
    text: String,
    accent: Color,
    onClick: () -> Unit
) {
    PremiumCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = text, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
