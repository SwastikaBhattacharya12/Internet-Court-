package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.AppDatabase
import com.example.data.CaseEntity
import com.example.data.CaseRepository
import com.example.data.UserStatsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourtViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = CaseRepository(database.caseDao())

    // Observe cases dynamically from Room SQLite
    val casesState: StateFlow<List<CaseEntity>> = repository.allCases
        .catch { e ->
            android.util.Log.e("CourtViewModel", "Error in cases flow", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Observe active user points, levels and daily streak
    val statsState: StateFlow<UserStatsEntity?> = repository.userStats
        .catch { e ->
            android.util.Log.e("CourtViewModel", "Error in stats flow", e)
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // AI Processing States
    private val _aiJudgeLoading = MutableStateFlow<Int?>(null) // Stores caseId being evaluated
    val aiJudgeLoading: StateFlow<Int?> = _aiJudgeLoading.asStateFlow()

    private val _toxicityChecking = MutableStateFlow(false)
    val toxicityChecking: StateFlow<Boolean> = _toxicityChecking.asStateFlow()

    private val _toxicityResult = MutableStateFlow<GeminiService.ToxicityResult?>(null)
    val toxicityResult: StateFlow<GeminiService.ToxicityResult?> = _toxicityResult.asStateFlow()

    // Submitting a new custom case Flow
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        // Pre-populate cases if database is empty on first launch
        viewModelScope.launch {
            try {
                repository.checkAndPrepopulate()
            } catch (e: Exception) {
                android.util.Log.e("CourtViewModel", "Error pre-populating database", e)
            }
        }
    }

    /**
     * Casts a direct jury vote. Awards +15 Gavel Points. Increases active streak.
     */
    fun castVote(caseId: Int, option: Int) {
        viewModelScope.launch {
            repository.voteOnCase(caseId, option)
        }
    }

    /**
     * Triggers witty generative AI Courtroom analysis from Gemini 3.5 Flash or local fallback.
     */
    fun summonAiJudge(caseId: Int) {
        viewModelScope.launch {
            _aiJudgeLoading.value = caseId
            val case = repository.getCaseById(caseId)
            if (case != null) {
                // Fetch judgment from Gemini Service
                val verdict = GeminiService.summonJudge(
                    title = case.title,
                    story = case.story,
                    plaintiff = case.plaintiffLabel,
                    defendant = case.defendantLabel
                )
                // Persist inside room SQLite
                repository.updateAiVerdict(caseId, verdict)
            }
            _aiJudgeLoading.value = null
        }
    }

    /**
     * Evaluates case text for personal insults or slurs. Returns structural empathy suggestions.
     */
    fun performToxicityAnalysis(story: String) {
        viewModelScope.launch {
            _toxicityChecking.value = true
            val analysis = GeminiService.checkToxicityAndRewrite(story)
            _toxicityResult.value = analysis
            _toxicityChecking.value = false
        }
    }

    /**
     * Clears local toxicity analysis state after being read or applied.
     */
    fun clearToxicityText() {
        _toxicityResult.value = null
    }

    /**
     * Bypasses filters and posts case directly.
     */
    fun postCaseDirect(title: String, story: String, category: String, plaintiff: String, defendant: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val newCase = CaseEntity(
                title = title,
                story = story,
                category = category,
                plaintiffLabel = plaintiff,
                defendantLabel = defendant,
                votesPlaintiff = (20..200).random(),
                votesDefendant = (20..200).random(),
                votesBothWrong = (10..150).random(),
                votesNoneWrong = (5..60).random()
            )
            repository.insertCase(newCase)
            _isSubmitting.value = false
        }
    }

    /**
     * Award dev-bonus points.
     */
    fun addGavelPoints(amount: Int) {
        viewModelScope.launch {
            repository.addPoints(amount)
        }
    }

    /**
     * Reset cases to enable revoting.
     */
    fun resetAppStore() {
        viewModelScope.launch {
            repository.clearAllVotes()
        }
    }
}
