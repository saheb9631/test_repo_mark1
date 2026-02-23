package com.Placements.Ready.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Placements.Ready.data.AuthManager
import com.Placements.Ready.ui.theme.*
import com.Placements.Ready.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (verified: Boolean, email: String, password: String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState(AuthViewModel.AuthState.Idle)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // Google Sign-In Setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.Placements.Ready.R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            // User cancelled the Google sign-in picker
            isGoogleLoading = false
            return@rememberLauncherForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.signInWithGoogle(it) }
                ?: run {
                    isGoogleLoading = false
                    errorMessage = "Google Sign In Failed: No token received."
                }
        } catch (e: ApiException) {
            isGoogleLoading = false
            errorMessage = when (e.statusCode) {
                12501 -> null // User cancelled — don't show error
                7 -> "Network error. Please check your connection."
                10 -> "App configuration error. Please contact support."
                else -> "Google Sign In Failed. Please try again."
            }
        } catch (e: Exception) {
            isGoogleLoading = false
            errorMessage = "Google Sign In Failed: ${e.localizedMessage}"
        }
    }

    // Derived: overall loading state
    val isLoading = authState is AuthViewModel.AuthState.Loading || isGoogleLoading

    // Effect to handle auth state changes
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Success -> {
                isGoogleLoading = false
                onLoginSuccess(state.user.isEmailVerified, state.user.email ?: "", password)
            }
            is AuthViewModel.AuthState.Error -> {
                isGoogleLoading = false
                errorMessage = when {
                    state.message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    state.message.contains("incorrect", ignoreCase = true) -> "Incorrect email or password. Please try again."
                    state.message.contains("network", ignoreCase = true) ||
                    state.message.contains("NETWORK_ERROR", ignoreCase = true) -> "Network error. Please check your connection."
                    state.message.contains("user-not-found", ignoreCase = true) -> "No account found with this email."
                    state.message.contains("email-already-in-use", ignoreCase = true) -> "An account already exists with this email."
                    state.message.contains("weak-password", ignoreCase = true) -> "Password is too weak. Use at least 6 characters."
                    state.message.contains("invalid-email", ignoreCase = true) ||
                    state.message.contains("badly formatted", ignoreCase = true) -> "Invalid email format."
                    state.message.contains("too-many-requests", ignoreCase = true) -> "Too many attempts. Please try again later."
                    state.message.contains("user-disabled", ignoreCase = true) -> "This account has been disabled."
                    else -> state.message
                }
            }
            else -> {}
        }
    }

    // Auto-clear success messages
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(4000L)
            successMessage = null
        }
    }

    // Forgot Password State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isResetting by remember { mutableStateOf(false) }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(colors = listOf(NeonPurple, NeonCyan))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Placement Ready", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Your Career Companion", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))

        Spacer(modifier = Modifier.height(32.dp))

        EmailLoginFields(
            email = email,
            onEmailChange = { email = it; errorMessage = null; successMessage = null },
            password = password,
            onPasswordChange = { password = it; errorMessage = null },
            confirmPassword = confirmPassword,
            onConfirmPasswordChange = { confirmPassword = it; errorMessage = null },
            isSignUp = isSignUp,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible },
            focusManager = focusManager,
            enabled = !isLoading
        )

        // Success message
        AnimatedVisibility(visible = successMessage != null) {
            successMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
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

        // Error message
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(24.dp))

        // Main Action Button
        Button(
            onClick = {
                focusManager.clearFocus()
                errorMessage = null
                successMessage = null
                val trimmedEmail = email.trim()

                // Validate email
                if (trimmedEmail.isBlank()) {
                    errorMessage = "Please enter your email address."
                    return@Button
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    errorMessage = "Please enter a valid email address."
                    return@Button
                }
                if (password.isBlank()) {
                    errorMessage = "Please enter your password."
                    return@Button
                }

                if (isSignUp) {
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters."
                    } else if (password != confirmPassword) {
                        errorMessage = "Passwords do not match. Please try again."
                    } else {
                        viewModel.signUpWithEmail(trimmedEmail, password)
                    }
                } else {
                    viewModel.signInWithEmail(trimmedEmail, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = if (isLoading) listOf(NeonPurple.copy(alpha = 0.4f), NeonCyan.copy(alpha = 0.4f))
                            else listOf(NeonPurple, NeonCyan)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (authState is AuthViewModel.AuthState.Loading && !isGoogleLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch Sign In / Sign Up
        TextButton(
            onClick = { isSignUp = !isSignUp; errorMessage = null; successMessage = null },
            enabled = !isLoading
        ) {
            Text(if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up", color = NeonPurple)
        }
        if (!isSignUp) {
            TextButton(
                onClick = { showForgotPasswordDialog = true; resetEmail = email.trim() },
                enabled = !isLoading
            ) {
                Text("Forgot Password?", color = NeonCyan, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Divider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text("  OR  ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Google Sign In Button
        OutlinedButton(
            onClick = {
                if (isGoogleLoading) return@OutlinedButton
                errorMessage = null
                successMessage = null
                isGoogleLoading = true
                val signInClient = GoogleSignIn.getClient(context, gso)
                // Sign out first to force account picker every time
                signInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(signInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        color = NeonPurple,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isGoogleLoading) "Signing in..." else "Continue with Google",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }


    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!isResetting) showForgotPasswordDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text("Enter your email address to receive a password reset link.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isBlank()) return@Button
                        isResetting = true
                        scope.launch {
                            val result = viewModel.sendPasswordResetEmail(resetEmail.trim())
                            isResetting = false
                            showForgotPasswordDialog = false
                            result.fold(
                                onSuccess = {
                                    successMessage = "Password reset link sent to $resetEmail"
                                },
                                onFailure = { e ->
                                    errorMessage = when {
                                        e.message?.contains("no user record", ignoreCase = true) == true -> "No account found with this email."
                                        e.message?.contains("badly formatted", ignoreCase = true) == true -> "Invalid email format."
                                        e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection."
                                        else -> "Failed to send reset email. Please try again."
                                    }
                                }
                            )
                        }
                    },
                    enabled = !isResetting && resetEmail.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Link", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false },
                    enabled = !isResetting
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun EmailLoginFields(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    isSignUp: Boolean,
    passwordVisible: Boolean, onTogglePassword: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    enabled: Boolean = true
) {
    Column {
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, null, tint = NeonPurple) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = password, onValueChange = onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, null, tint = NeonPurple) },
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = enabled,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done),
            keyboardActions = if (!isSignUp) KeyboardActions(onDone = { focusManager.clearFocus() }) else KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        AnimatedVisibility(visible = isSignUp) {
            Column {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = confirmPassword, onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = NeonCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    enabled = enabled,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }
        }
    }
}
