package com.Placements.Ready.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Placements.Ready.data.Question
import com.Placements.Ready.data.QuestionProgress
import com.Placements.Ready.data.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)

    // All questions loaded from JSON
    private val _allQuestions = MutableStateFlow<List<Question>>(emptyList())

    // Filtered questions shown in the list
    private val _filteredQuestions = MutableStateFlow<List<Question>>(emptyList())
    val filteredQuestions: StateFlow<List<Question>> = _filteredQuestions.asStateFlow()

    // Available topics
    private val _topics = MutableStateFlow<List<String>>(emptyList())
    val topics: StateFlow<List<String>> = _topics.asStateFlow()

    // Filters
    private val _selectedDifficulty = MutableStateFlow("All")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _selectedTopic = MutableStateFlow("All")
    val selectedTopic: StateFlow<String> = _selectedTopic.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Progress
    private val _totalSolved = MutableStateFlow(0)
    val totalSolved: StateFlow<Int> = _totalSolved.asStateFlow()

    private val _totalQuestions = MutableStateFlow(0)
    val totalQuestions: StateFlow<Int> = _totalQuestions.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _easySolved = MutableStateFlow(0)
    val easySolved: StateFlow<Int> = _easySolved.asStateFlow()

    private val _mediumSolved = MutableStateFlow(0)
    val mediumSolved: StateFlow<Int> = _mediumSolved.asStateFlow()

    private val _hardSolved = MutableStateFlow(0)
    val hardSolved: StateFlow<Int> = _hardSolved.asStateFlow()

    private val _easyTotal = MutableStateFlow(0)
    val easyTotal: StateFlow<Int> = _easyTotal.asStateFlow()

    private val _mediumTotal = MutableStateFlow(0)
    val mediumTotal: StateFlow<Int> = _mediumTotal.asStateFlow()

    private val _hardTotal = MutableStateFlow(0)
    val hardTotal: StateFlow<Int> = _hardTotal.asStateFlow()

    // Progress change trigger (incremented to force recomposition)
    private val _progressVersion = MutableStateFlow(0)
    val progressVersion: StateFlow<Int> = _progressVersion.asStateFlow()

    // Daily plan
    private val _dailyPlanQuestions = MutableStateFlow<List<Question>>(emptyList())
    val dailyPlanQuestions: StateFlow<List<Question>> = _dailyPlanQuestions.asStateFlow()

    // Loading
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val questions = repository.loadQuestions()
            _allQuestions.value = questions
            _totalQuestions.value = questions.size
            _topics.value = repository.getTopics()
            applyFilters()
            refreshStats()
            _isLoading.value = false
        }
    }

    // ─── Filters ────────────────────────────────────────────────

    fun setDifficultyFilter(difficulty: String) {
        _selectedDifficulty.value = difficulty
        applyFilters()
    }

    fun setTopicFilter(topic: String) {
        _selectedTopic.value = topic
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    private fun applyFilters() {
        val difficulty = _selectedDifficulty.value
        val topic = _selectedTopic.value
        val query = _searchQuery.value.lowercase().trim()

        var result = _allQuestions.value

        if (difficulty != "All") {
            result = result.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
        }
        if (topic != "All") {
            result = result.filter { it.topic == topic }
        }
        if (query.isNotBlank()) {
            result = result.filter {
                it.problemName.lowercase().contains(query) ||
                        it.topic.lowercase().contains(query) ||
                        it.subTopic.lowercase().contains(query)
            }
        }

        _filteredQuestions.value = result
    }

    // ─── Actions ────────────────────────────────────────────────

    fun toggleSolved(questionId: String) {
        repository.toggleSolved(questionId)
        refreshStats()
    }

    fun markUnsolved(questionId: String) {
        repository.markUnsolved(questionId)
        refreshStats()
    }

    fun toggleRevision(questionId: String) {
        repository.toggleRevision(questionId)
        _progressVersion.value++
    }

    fun toggleFavorite(questionId: String) {
        repository.toggleFavorite(questionId)
        _progressVersion.value++
    }

    fun saveNotes(questionId: String, notes: String) {
        repository.saveNotes(questionId, notes)
    }

    fun getProgress(questionId: String): QuestionProgress {
        return repository.getProgress(questionId)
    }

    // ─── Stats ──────────────────────────────────────────────────

    private fun refreshStats() {
        _totalSolved.value = repository.getSolvedCount()
        _streak.value = repository.getStreak()
        _easySolved.value = repository.getSolvedCountByDifficulty("Easy")
        _mediumSolved.value = repository.getSolvedCountByDifficulty("Medium")
        _hardSolved.value = repository.getSolvedCountByDifficulty("Hard")
        _easyTotal.value = repository.getTotalByDifficulty("Easy")
        _mediumTotal.value = repository.getTotalByDifficulty("Medium")
        _hardTotal.value = repository.getTotalByDifficulty("Hard")
        _progressVersion.value++
    }

    // ─── Lists ──────────────────────────────────────────────────

    fun getRevisionQuestions(): List<Question> {
        return _allQuestions.value.filter { q ->
            repository.getProgress(q.id).isRevision
        }
    }

    fun getFavoriteQuestions(): List<Question> {
        return _allQuestions.value.filter { q ->
            repository.getProgress(q.id).isFavorite
        }
    }

    // ─── Daily Plan ─────────────────────────────────────────────

    fun loadDailyPlan() {
        viewModelScope.launch {
            val planIds = repository.getDailyPlan()
            val allQ = _allQuestions.value
            _dailyPlanQuestions.value = planIds.mapNotNull { id ->
                allQ.find { it.id == id }
            }
        }
    }

    // ─── Question Lookup ────────────────────────────────────────

    fun getQuestionById(id: String): Question? {
        return _allQuestions.value.find { it.id == id }
    }

    // ─── Topic Progress ─────────────────────────────────────────

    fun getTopicProgress(): List<TopicProgressItem> {
        return _topics.value.map { topic ->
            TopicProgressItem(
                topic = topic,
                solved = repository.getSolvedCountByTopic(topic),
                total = repository.getTotalByTopic(topic)
            )
        }
    }

    fun resetProgress() {
        repository.resetProgress()
        loadData()
    }
}

data class TopicProgressItem(
    val topic: String,
    val solved: Int,
    val total: Int
)
