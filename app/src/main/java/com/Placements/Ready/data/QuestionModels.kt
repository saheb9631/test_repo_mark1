package com.Placements.Ready.data

/**
 * Data classes for parsing questionzlist.json (Striver's A2Z DSA Sheet).
 */

data class QuestionSheet(
    val sheetName: String = "",
    val totalProblems: Int = 0,
    val difficulty_breakdown: DifficultyBreakdown = DifficultyBreakdown(),
    val steps: List<Step> = emptyList()
)

data class DifficultyBreakdown(
    val Easy: Int = 0,
    val Medium: Int = 0,
    val Hard: Int = 0
)

data class Step(
    val stepNumber: Int = 0,
    val stepTitle: String = "",
    val totalProblems: Int = 0,
    val subTopics: List<SubTopic> = emptyList()
)

data class SubTopic(
    val subTopicTitle: String = "",
    val totalProblems: Int = 0,
    val problems: List<Problem> = emptyList()
)

data class Problem(
    val problemName: String = "",
    val difficulty: String = "Easy",
    val solutions: List<Solution> = emptyList()
)

/**
 * A single solution approach (Brute / Better / Optimal) with multi-language code.
 */
data class Solution(
    val approach: String = "",           // "Brute", "Better", "Optimal"
    val explanation: String = "",        // Brief explanation of approach
    val timeComplexity: String = "",     // e.g. "O(n^2)"
    val spaceComplexity: String = "",    // e.g. "O(1)"
    val code: Map<String, String> = emptyMap()  // "cpp", "java", "python"
)

/**
 * Flattened question representation with all context attached.
 * This is what the UI works with.
 */
data class Question(
    val id: String,              // Unique ID: "step_sub_prob" e.g. "1_0_0"
    val problemName: String,
    val difficulty: String,      // "Easy", "Medium", "Hard"
    val topic: String,           // stepTitle e.g. "Binary Search"
    val subTopic: String,        // subTopicTitle e.g. "BS on 1D Arrays"
    val stepNumber: Int,
    val solutions: List<Solution> = emptyList()
)

/**
 * User progress for a single question. Stored locally.
 */
data class QuestionProgress(
    val isSolved: Boolean = false,
    val isRevision: Boolean = false,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val solvedDate: String? = null  // ISO date string
)
