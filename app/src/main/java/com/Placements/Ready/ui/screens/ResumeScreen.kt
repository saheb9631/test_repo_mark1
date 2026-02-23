package com.Placements.Ready.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Placements.Ready.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

// ─── Data Models ────────────────────────────────────────────────────────────

data class TargetRole(val name: String, val icon: ImageVector, val keywords: List<String>)

val targetRoles = listOf(
    TargetRole("Android Developer", Icons.Filled.PhoneAndroid,
        listOf("kotlin", "java", "android", "jetpack", "compose", "mvvm", "viewmodel", "coroutines", "retrofit", "room", "hilt", "android sdk", "fragment", "lifecycle", "git")),
    TargetRole("Frontend Developer", Icons.Filled.WebAsset,
        listOf("react", "javascript", "typescript", "html", "css", "tailwind", "vue", "angular", "ui", "ux", "accessibility", "redux", "git", "responsive", "figma")),
    TargetRole("Backend Developer", Icons.Filled.Storage,
        listOf("java", "python", "node", "api", "rest", "sql", "mysql", "postgresql", "mongodb", "spring", "express", "system design", "docker", "kubernetes", "microservices")),
    TargetRole("iOS Developer", Icons.Filled.PhoneIphone,
        listOf("swift", "swiftui", "uikit", "xcode", "objective-c", "cocoapods", "mvvm", "combine", "core data", "push notifications", "testflight", "ios", "git")),
    TargetRole("Full Stack Developer", Icons.Filled.Layers,
        listOf("react", "node", "javascript", "typescript", "sql", "mongodb", "rest", "api", "docker", "git", "html", "css", "java", "python", "aws")),
    TargetRole("Other IT Roles", Icons.Filled.Code,
        listOf("git", "sql", "linux", "python", "agile", "scrum", "testing", "ci/cd", "cloud", "api", "documentation", "teamwork", "problem solving"))
)

data class AtsResult(
    val score: Int,
    val strengths: List<String>,
    val missingKeywords: List<String>,
    val suggestions: List<String>,
    val foundKeywords: List<String>
)

// ─── ATS Engine ─────────────────────────────────────────────────────────────

fun runAtsAnalysis(resumeText: String, role: TargetRole): AtsResult {
    val text = resumeText.lowercase()

    // Keyword matching
    val foundKeywords = role.keywords.filter { text.contains(it.lowercase()) }
    val missingKeywords = role.keywords.filter { !text.contains(it.lowercase()) }

    // Section detection
    val hasSkills = text.contains("skill") || text.contains("technologies") || text.contains("tools")
    val hasExperience = text.contains("experience") || text.contains("internship") || text.contains("work")
    val hasProjects = text.contains("project") || text.contains("built") || text.contains("developed")
    val hasEducation = text.contains("education") || text.contains("degree") || text.contains("university") || text.contains("college")
    val hasCertifications = text.contains("certification") || text.contains("certified") || text.contains("certificate")
    val hasGithub = text.contains("github") || text.contains("gitlab")
    val hasLinkedin = text.contains("linkedin")
    val hasMeasurable = Regex("\\d+%|\\d+ users|\\d+ million|\\d+x|\\d+ hours").containsMatchIn(text)

    // Score calculation
    val keywordScore = (foundKeywords.size.toFloat() / role.keywords.size * 40).toInt()
    var sectionScore = 0
    if (hasSkills) sectionScore += 8
    if (hasExperience) sectionScore += 10
    if (hasProjects) sectionScore += 10
    if (hasEducation) sectionScore += 7
    if (hasCertifications) sectionScore += 5
    val linksScore = (if (hasGithub) 5 else 0) + (if (hasLinkedin) 5 else 0)
    val achievementScore = if (hasMeasurable) 10 else 0
    val total = (keywordScore + sectionScore + linksScore + achievementScore).coerceAtMost(100)

    // Strengths
    val strengths = mutableListOf<String>()
    if (foundKeywords.size >= role.keywords.size * 0.6) strengths.add("Strong keyword match for ${role.name} role")
    if (hasExperience) strengths.add("Experience/Internship section detected ✅")
    if (hasProjects) strengths.add("Projects section with work samples ✅")
    if (hasEducation) strengths.add("Education section present ✅")
    if (hasGithub) strengths.add("GitHub profile linked ✅")
    if (hasLinkedin) strengths.add("LinkedIn profile linked ✅")
    if (hasMeasurable) strengths.add("Measurable achievements found (numbers/%) ✅")
    if (hasCertifications) strengths.add("Certifications/Courses listed ✅")
    if (strengths.isEmpty()) strengths.add("Resume has valid content")

    // Suggestions
    val suggestions = mutableListOf<String>()
    if (!hasSkills) suggestions.add("Add a dedicated 'Skills' section with your tech stack")
    if (!hasProjects) suggestions.add("Add project descriptions — what you built and the tech used")
    if (!hasExperience) suggestions.add("Add internship or work experience, even freelance projects count")
    if (!hasGithub) suggestions.add("Add your GitHub profile link to show real code")
    if (!hasLinkedin) suggestions.add("Add your LinkedIn profile for recruiter visibility")
    if (!hasMeasurable) suggestions.add("Quantify achievements: e.g. 'Reduced load time by 40%' or 'Served 1000+ users'")
    if (!hasCertifications) suggestions.add("Add online certifications (Coursera, Udemy, Google, AWS) to show initiative")
    if (missingKeywords.size > role.keywords.size * 0.5) suggestions.add("Include more role-specific keywords to pass ATS filters")
    if (suggestions.isEmpty()) suggestions.add("Your resume looks well-structured! Keep it updated.")

    return AtsResult(
        score = total,
        strengths = strengths,
        missingKeywords = missingKeywords.take(6),
        suggestions = suggestions,
        foundKeywords = foundKeywords
    )
}

// ─── Cloudinary Upload ───────────────────────────────────────────────────────

suspend fun uploadToCloudinary(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
    return@withContext try {
        val cloudName = "ddm6ddmkf"
        val uploadPreset = "resume_upload"
        val url = "https://api.cloudinary.com/v1_1/$cloudName/raw/upload"

        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: return@withContext Result.failure(IOException("Cannot read file"))

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "resume.pdf", bytes.toRequestBody("application/pdf".toMediaType()))
            .addFormDataPart("upload_preset", uploadPreset)
            .addFormDataPart("resource_type", "raw")
            .build()

        val request = Request.Builder().url(url).post(requestBody).build()
        val response = OkHttpClient().newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (response.isSuccessful) {
            val json = JSONObject(body)
            Result.success(json.getString("secure_url"))
        } else {
            Result.failure(IOException("Upload failed: ${response.code} $body"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var name = "resume.pdf"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && idx >= 0) name = cursor.getString(idx)
    }
    return name
}

fun getFileSizeBytes(context: Context, uri: Uri): Long {
    var size = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst() && idx >= 0) size = cursor.getLong(idx)
    }
    return size
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

enum class ResumeUiState { IDLE, UPLOADING, ANALYZING, DONE, ERROR }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResumeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf(targetRoles[0]) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var uploadedUrl by remember { mutableStateOf<String?>(null) }
    var uiState by remember { mutableStateOf(ResumeUiState.IDLE) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var atsResult by remember { mutableStateOf<AtsResult?>(null) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val mime = context.contentResolver.getType(uri) ?: ""
        if (!mime.contains("pdf", ignoreCase = true)) {
            errorMsg = "Only PDF files are allowed."
            return@rememberLauncherForActivityResult
        }
        val size = getFileSizeBytes(context, uri)
        if (size > 5 * 1024 * 1024) {
            errorMsg = "File too large. Maximum size is 5 MB."
            return@rememberLauncherForActivityResult
        }
        pickedUri = uri
        fileName = getFileName(context, uri)
        uploadedUrl = null
        atsResult = null
        errorMsg = null
        uiState = ResumeUiState.IDLE
    }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(bg, surface, bg)))
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Resume ATS Checker", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Text("Upload your resume and get a real ATS score like top tech companies use.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
        }

        // Role Selector
        item {
            Text("1. Select Your Target Role", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = roleDropdownExpanded,
                onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedRole.name,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(selectedRole.icon, contentDescription = null, tint = NeonCyan) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    targetRoles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.name) },
                            leadingIcon = { Icon(role.icon, contentDescription = null, tint = NeonCyan) },
                            onClick = {
                                selectedRole = role
                                roleDropdownExpanded = false
                                atsResult = null
                            }
                        )
                    }
                }
            }
        }

        // Upload Area
        item {
            Text("2. Upload Your Resume (PDF, max 5MB)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = uiState != ResumeUiState.UPLOADING) { filePicker.launch("application/pdf") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    1.5.dp,
                    if (pickedUri != null) NeonGreen.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (pickedUri != null)
                                Brush.linearGradient(colors = listOf(NeonGreen.copy(alpha = 0.07f), NeonCyan.copy(alpha = 0.04f)))
                            else
                                Brush.linearGradient(colors = listOf(NeonCyan.copy(alpha = 0.08f), NeonPurple.copy(alpha = 0.04f)))
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (pickedUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null,
                                tint = NeonCyan, modifier = Modifier.size(52.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Tap to choose PDF", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground)
                            Text("PDF only · Max 5 MB", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null,
                                    tint = NeonGreen, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fileName ?: "resume.pdf", fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp, maxLines = 1,
                                    color = MaterialTheme.colorScheme.onBackground)
                                Text("Ready to analyze · Tap to replace",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.CheckCircle, contentDescription = null,
                                tint = NeonGreen, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        // Error
        if (errorMsg != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonPink.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(errorMsg!!, color = NeonPink, fontSize = 13.sp)
                    }
                }
            }
        }

        // Upload progress
        if (uiState == ResumeUiState.UPLOADING) {
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Uploading resume...", fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = NeonCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        if (uiState == ResumeUiState.ANALYZING) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Running ATS analysis…", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Analyze Button
        if (pickedUri != null && uiState != ResumeUiState.UPLOADING && uiState != ResumeUiState.ANALYZING) {
            item {
                Button(
                    onClick = {
                        uiState = ResumeUiState.UPLOADING
                        errorMsg = null
                        atsResult = null
                        uploadProgress = 0f
                    scope.launch {
                        // Animate fake progress while uploading
                        val progressJob = launch {
                            while (uploadProgress < 0.9f) {
                                kotlinx.coroutines.delay(200)
                                uploadProgress = (uploadProgress + 0.05f).coerceAtMost(0.9f)
                            }
                        }
                        val uploadResult = uploadToCloudinary(context, pickedUri!!)
                        progressJob.cancel()
                        uploadProgress = 1f
                        uploadResult.fold(
                            onSuccess = { url ->
                                uploadedUrl = url
                                uiState = ResumeUiState.ANALYZING
                                kotlinx.coroutines.delay(800)
                                val rawText = withContext(Dispatchers.IO) {
                                    try {
                                        context.contentResolver.openInputStream(pickedUri!!)?.bufferedReader()?.readText() ?: ""
                                    } catch (e: Exception) { "" }
                                }
                                atsResult = runAtsAnalysis(rawText, selectedRole)
                                uiState = ResumeUiState.DONE
                            },
                            onFailure = { e ->
                                uiState = ResumeUiState.ERROR
                                errorMsg = when {
                                    e.message?.contains("network", ignoreCase = true) == true ||
                                    e.message?.contains("timeout", ignoreCase = true) == true ->
                                        "⚠️ No internet connection. Please check your network and try again."
                                    else -> "⚠️ Upload failed: ${e.message?.take(60)}"
                                }
                            }
                        )
                    }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Brush.linearGradient(colors = listOf(NeonCyan, NeonPurple)),
                                shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Analytics, contentDescription = null,
                                tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (atsResult != null) "Re-Analyze" else "Run ATS Analysis",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // ─── Results ───────────────────────────────────────────────────────
        atsResult?.let { result ->
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("ATS Results", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground)
            }

            // Score Card
            item {
                val scoreColor = when {
                    result.score >= 75 -> NeonGreen
                    result.score >= 50 -> NeonGold
                    else -> NeonPink
                }
                val scoreLabel = when {
                    result.score >= 75 -> "Excellent 🎉"
                    result.score >= 50 -> "Good — keep improving 💪"
                    else -> "Needs work — let's fix it 🔧"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(colors = listOf(scoreColor.copy(alpha = 0.08f), scoreColor.copy(alpha = 0.03f))))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ATS Score", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${result.score}", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = scoreColor)
                            Text("/100", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { result.score / 100f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                color = scoreColor,
                                trackColor = scoreColor.copy(alpha = 0.15f),
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(scoreLabel, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = scoreColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Role: ${selectedRole.name}", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Keywords found
            if (result.foundKeywords.isNotEmpty()) {
                item {
                    Text("✅ Matched Keywords", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NeonGreen.copy(alpha = 0.07f)),
                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()) {
                        FlowRow(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            result.foundKeywords.forEach { kw ->
                                Chip(label = kw, color = NeonGreen)
                            }
                        }
                    }
                }
            }

            // Missing keywords
            if (result.missingKeywords.isNotEmpty()) {
                item {
                    Text("❌ Missing Keywords", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NeonPink.copy(alpha = 0.07f)),
                        border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()) {
                        FlowRow(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            result.missingKeywords.forEach { kw ->
                                Chip(label = kw, color = NeonPink)
                            }
                        }
                    }
                }
            }

            // Strengths
            item {
                SectionHeader("💪 Strengths")
            }
            items(result.strengths) { s ->
                FeedbackCard(text = s, icon = Icons.Filled.CheckCircleOutline, color = NeonGreen)
            }

            // Suggestions
            item {
                SectionHeader("💡 Suggestions to Improve")
            }
            items(result.suggestions) { s ->
                FeedbackCard(text = s, icon = Icons.Filled.Lightbulb, color = NeonGold)
            }

            // Retry
            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        pickedUri = null
                        fileName = null
                        uploadedUrl = null
                        atsResult = null
                        uiState = ResumeUiState.IDLE
                        errorMsg = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload a Different Resume", color = NeonCyan, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ─── Helper Composables ───────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun FeedbackCard(text: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.07f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, modifier = Modifier.weight(1f), lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun Chip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
    }
}
