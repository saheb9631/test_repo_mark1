package com.Placements.Ready.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for loading question data from questionzlist.json
 * and managing user progress via SharedPreferences.
 *
 * Does NOT interact with placement_data at all.
 */
class QuestionRepository(private val context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("question_progress", Context.MODE_PRIVATE)

    private var cachedQuestions: List<Question>? = null
    private var cachedSheet: QuestionSheet? = null

    companion object {
        private const val TAG = "QuestionRepository"
        private const val ASSET_FILE = "questionzlist.json"
        private const val PREF_STREAK_LAST_DATE = "streak_last_date"
        private const val PREF_STREAK_COUNT = "streak_count"
        private const val PREF_DAILY_PLAN_DATE = "daily_plan_date"
        private const val PREF_DAILY_PLAN_IDS = "daily_plan_ids"
    }

    // ─── Load Questions ─────────────────────────────────────────

    /**
     * Load and flatten all questions from the JSON asset.
     */
    suspend fun loadQuestions(): List<Question> = withContext(Dispatchers.IO) {
        cachedQuestions?.let { return@withContext it }

        try {
            val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
            val sheet = gson.fromJson(json, QuestionSheet::class.java)
            cachedSheet = sheet

            val questions = mutableListOf<Question>()
            sheet.steps.forEach { step ->
                step.subTopics.forEachIndexed { subIdx, subTopic ->
                    subTopic.problems.forEachIndexed { probIdx, problem ->
                        questions.add(
                            Question(
                                id = "${step.stepNumber}_${subIdx}_${probIdx}",
                                problemName = problem.problemName,
                                difficulty = problem.difficulty.replaceFirstChar { it.uppercase() },
                                topic = step.stepTitle,
                                subTopic = subTopic.subTopicTitle,
                                stepNumber = step.stepNumber,
                                solutions = problem.solutions
                            )
                        )
                    }
                }
            }

            cachedQuestions = questions
            Log.d(TAG, "Loaded ${questions.size} questions from $ASSET_FILE")
            questions
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load questions", e)
            emptyList()
        }
    }

    /**
     * Get all unique topic names (step titles).
     */
    suspend fun getTopics(): List<String> {
        val questions = loadQuestions()
        return questions.map { it.topic }.distinct()
    }

    // ─── User Progress ──────────────────────────────────────────

    fun getProgress(questionId: String): QuestionProgress {
        val solved = prefs.getBoolean("${questionId}_solved", false)
        val revision = prefs.getBoolean("${questionId}_revision", false)
        val favorite = prefs.getBoolean("${questionId}_favorite", false)
        val notes = prefs.getString("${questionId}_notes", "") ?: ""
        val solvedDate = prefs.getString("${questionId}_solvedDate", null)
        return QuestionProgress(solved, revision, favorite, notes, solvedDate)
    }

    fun toggleSolved(questionId: String) {
        val current = prefs.getBoolean("${questionId}_solved", false)
        val newVal = !current
        prefs.edit().apply {
            putBoolean("${questionId}_solved", newVal)
            if (newVal) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                putString("${questionId}_solvedDate", today)
                updateStreak(today)
            } else {
                remove("${questionId}_solvedDate")
            }
            apply()
        }
    }

    fun toggleRevision(questionId: String) {
        val current = prefs.getBoolean("${questionId}_revision", false)
        prefs.edit().putBoolean("${questionId}_revision", !current).apply()
    }

    fun toggleFavorite(questionId: String) {
        val current = prefs.getBoolean("${questionId}_favorite", false)
        prefs.edit().putBoolean("${questionId}_favorite", !current).apply()
    }

    fun markUnsolved(questionId: String) {
        prefs.edit().apply {
            putBoolean("${questionId}_solved", false)
            remove("${questionId}_solvedDate")
            apply()
        }
    }

    fun saveNotes(questionId: String, notes: String) {
        prefs.edit().putString("${questionId}_notes", notes).apply()
    }

    // ─── Progress Stats ─────────────────────────────────────────

    fun getSolvedCount(): Int {
        return prefs.all.count { it.key.endsWith("_solved") && it.value == true }
    }

    fun getSolvedCountByDifficulty(difficulty: String): Int {
        val allQuestions = cachedQuestions ?: return 0
        return allQuestions.count { q ->
            q.difficulty.equals(difficulty, ignoreCase = true) &&
                    prefs.getBoolean("${q.id}_solved", false)
        }
    }

    fun getTotalByDifficulty(difficulty: String): Int {
        val allQuestions = cachedQuestions ?: return 0
        return allQuestions.count { it.difficulty.equals(difficulty, ignoreCase = true) }
    }

    fun getSolvedCountByTopic(topic: String): Int {
        val allQuestions = cachedQuestions ?: return 0
        return allQuestions.count { q ->
            q.topic == topic && prefs.getBoolean("${q.id}_solved", false)
        }
    }

    fun getTotalByTopic(topic: String): Int {
        val allQuestions = cachedQuestions ?: return 0
        return allQuestions.count { it.topic == topic }
    }

    // ─── Streak ─────────────────────────────────────────────────

    private fun updateStreak(todayStr: String) {
        val lastDate = prefs.getString(PREF_STREAK_LAST_DATE, null)
        val currentStreak = prefs.getInt(PREF_STREAK_COUNT, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.parse(todayStr)

        val newStreak = if (lastDate == null) {
            1
        } else {
            val last = sdf.parse(lastDate)
            val diffDays = ((today!!.time - last!!.time) / (1000 * 60 * 60 * 24)).toInt()
            when {
                diffDays == 0 -> currentStreak // Same day
                diffDays == 1 -> currentStreak + 1 // Consecutive
                else -> 1 // Streak broken
            }
        }

        prefs.edit()
            .putString(PREF_STREAK_LAST_DATE, todayStr)
            .putInt(PREF_STREAK_COUNT, newStreak)
            .apply()
    }

    fun getStreak(): Int = prefs.getInt(PREF_STREAK_COUNT, 0)

    // ─── Daily Plan ─────────────────────────────────────────────

    /**
     * Generate or retrieve today's daily practice plan.
     * Returns a list of question IDs for today.
     */
    suspend fun getDailyPlan(): List<String> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val savedDate = prefs.getString(PREF_DAILY_PLAN_DATE, null)
        val savedIds = prefs.getString(PREF_DAILY_PLAN_IDS, null)

        // If plan already generated today, return it
        if (savedDate == today && savedIds != null) {
            return savedIds.split(",").filter { it.isNotBlank() }
        }

        // Generate new daily plan
        val allQuestions = loadQuestions()
        val unsolved = allQuestions.filter { q ->
            !prefs.getBoolean("${q.id}_solved", false)
        }

        // Pick a mix: 3 Easy, 3 Medium, 2 Hard (or whatever is available)
        val easyPick = unsolved.filter { it.difficulty.equals("Easy", true) }.shuffled().take(3)
        val mediumPick = unsolved.filter { it.difficulty.equals("Medium", true) }.shuffled().take(3)
        val hardPick = unsolved.filter { it.difficulty.equals("Hard", true) }.shuffled().take(2)

        val plan = (easyPick + mediumPick + hardPick).map { it.id }

        prefs.edit()
            .putString(PREF_DAILY_PLAN_DATE, today)
            .putString(PREF_DAILY_PLAN_IDS, plan.joinToString(","))
            .apply()

        return plan
    }

    /**
     * Reset progress — clears all saved data.
     */
    fun resetProgress() {
        prefs.edit().clear().apply()
        cachedQuestions = null
        cachedSheet = null
    }
}
