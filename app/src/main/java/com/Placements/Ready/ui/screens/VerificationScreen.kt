package com.Placements.Ready.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerificationScreen(
    email: String,
    password: String,
    viewModel: com.Placements.Ready.viewmodel.AuthViewModel,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var isSigningOut by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Background-resilient timer using timestamps
    var lastResendTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    // Auto-poll every 4 seconds to detect verification automatically
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            try {
                val reloadResult = viewModel.reloadUser()
                if (reloadResult.getOrNull() == true) {
                    viewModel.signInWithEmail(email, password)
                }
            } catch (_: Exception) {
                // Silently ignore polling errors
            }
        }
    }

    // Cooldown logic that works even if app was backgrounded
    LaunchedEffect(lastResendTime) {
        if (lastResendTime > 0) {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val elapsed = (currentTime - lastResendTime) / 1000
                val remaining = (60 - elapsed).toInt()
                
                if (remaining <= 0) {
                    secondsLeft = 0
                    break
                }
                secondsLeft = remaining
                delay(1000L)
            }
        }
    }

    // Auto-clear success messages
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(5000L)
            successMessage = null
        }
    }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    // Pulse animation for the mail icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated mail icon
        Box(
            modifier = Modifier
                .size((90 * pulse).dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.15f), NeonCyan.copy(alpha = 0.1f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((64 * pulse).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(colors = listOf(NeonPurple, NeonCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MarkEmailRead,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Verify Your Email",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "We've sent a verification link to",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            email,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeonPurple,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Click the link in your inbox — or tap below once done. We'll also detect it automatically!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Didn't see it? Check your Spam or Junk folder.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = NeonPink,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonPink.copy(alpha = 0.12f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(msg, color = NeonPink, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Success message
        AnimatedVisibility(visible = successMessage != null) {
            successMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonGreen.copy(alpha = 0.12f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircleOutline, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(msg, color = NeonGreen, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Manual "I've Verified" Button
        Button(
            onClick = {
                if (isChecking) return@Button
                isChecking = true
                errorMessage = null
                successMessage = null
                scope.launch {
                    try {
                        viewModel.signOut(context)
                        viewModel.signInWithEmail(email, password)
                    } catch (e: Exception) {
                        errorMessage = "Verification check failed. Please try again."
                    } finally {
                        isChecking = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isChecking && !isResending && !isSigningOut,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = if (isChecking)
                                listOf(NeonPurple.copy(alpha = 0.4f), NeonCyan.copy(alpha = 0.4f))
                            else listOf(NeonPurple, NeonCyan)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isChecking) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I've Verified My Email", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend Button with Timer
        Button(
            onClick = {
                if (secondsLeft > 0 || isResending) return@Button
                isResending = true
                errorMessage = null
                successMessage = null
                scope.launch {
                    try {
                        val result = viewModel.sendVerificationEmail()
                        result.fold(
                            onSuccess = {
                                successMessage = "Verification email sent! Please check your inbox."
                                errorMessage = null
                                lastResendTime = System.currentTimeMillis()
                            },
                            onFailure = { e ->
                                val msg = e.message ?: ""
                                errorMessage = when {
                                    msg.contains("too-many-requests", true) ||
                                    msg.contains("recent-login-required", true) ->
                                        "Please wait a moment before trying again."
                                    msg.contains("network", true) ->
                                        "Network error. Please check your connection."
                                    else -> "Failed to send email. Please try again."
                                }
                                if (msg.contains("too-many-requests", true)) {
                                    lastResendTime = System.currentTimeMillis()
                                }
                            }
                        )
                    } catch (e: Exception) {
                        errorMessage = "Failed to send email. Please try again."
                    } finally {
                        isResending = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isResending && secondsLeft == 0 && !isChecking && !isSigningOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            if (isResending) {
                CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sending...", color = NeonCyan, fontWeight = FontWeight.Medium)
            } else if (secondsLeft > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resend in ${secondsLeft}s", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resend Verification Email", color = NeonCyan, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                if (isSigningOut) return@TextButton
                isSigningOut = true
                viewModel.signOut(context)
                onBack()
            },
            enabled = !isChecking && !isResending && !isSigningOut
        ) {
            if (isSigningOut) {
                CircularProgressIndicator(color = NeonPurple, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Use a different email", color = NeonPurple, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}
