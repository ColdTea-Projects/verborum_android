package de.coldtea.verborum.bibliotheca.word.ui.multiplechoice

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.coldtea.verborum.bibliotheca.common.domain.PendingUploadSyncTrigger
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.model.WordMeta
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.bibliotheca.word.ui.model.languageCodeOf
import de.coldtea.verborum.bibliotheca.word.ui.model.surfacesDisplay
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestion
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceCurrentQuestionState
import de.coldtea.verborum.bibliotheca.word.ui.multiplechoice.model.MultipleChoiceQuestion
import de.coldtea.verborum.core.ui.BaseViewModel
import de.coldtea.verborum.core.ui.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A question shows one correct answer plus three distractors. */
const val REQUIRED_WORDS_FOR_TEST = 4

@HiltViewModel
class MultipleChoiceViewModel @Inject constructor(
    private val wordService: WordService,
    private val pendingUploadSyncTrigger: PendingUploadSyncTrigger,
) : BaseViewModel() {

    // Hold back per-answer uploads while the test is open; the run of level changes is flushed to
    // the server once, when the screen closes (onCleared).
    init {
        pendingUploadSyncTrigger.pause()
    }

    override fun onCleared() {
        pendingUploadSyncTrigger.resume()
        super.onCleared()
    }

    private var currentQuestionIndex = 0
    private var score: Int = 0
    private var words: List<WordUi> = listOf()
    private var questions: List<MultipleChoiceQuestion> = listOf()

    // Wrong answers are drawn from every dictionary sharing this language pair, grouped by form so
    // a past-tense question can still be offered past-tense distractors. Questions themselves stay
    // limited to the dictionary under test.
    private var distractorsByForm: Map<FieldKey?, List<String>> = emptyMap()

    // The score counts every correct *question* (base form plus plurals, tenses, alternatives). The
    // level, by contrast, moves per *word*: the first correct answer for a word raises it once, the
    // first wrong answer lowers it once, and later answers for that word leave the level alone — so
    // a word's level shifts by at most +1 and/or -1 per test.
    private val raisedWordIds = mutableSetOf<String>()
    private val loweredWordIds = mutableSetOf<String>()

    private var _answered = MutableStateFlow(false)
    val answered = _answered.asSharedFlow()

    private var _selectedAnswer = MutableStateFlow("")
    val selectedAnswer = _selectedAnswer.asSharedFlow()
    private val _currentQuestion =
        MutableStateFlow<MultipleChoiceCurrentQuestionState>(MultipleChoiceCurrentQuestionState.Loading)
    val currentQuestion = _currentQuestion.asSharedFlow()

    private var loadJob: Job? = null
    private var dictionaryId: String = ""

    fun init(dictionaryId: String) {
        this.dictionaryId = dictionaryId
        observeWords()
    }

    /** Re-subscribes after a Failed load (the observed flow terminates on error). */
    fun retry() {
        _currentQuestion.tryEmit(MultipleChoiceCurrentQuestionState.Loading)
        observeWords()
    }

    private fun observeWords() {
        loadJob?.cancel()
        loadJob = combine(
            wordService.observeWordsByDictionary(dictionaryId),
            wordService.observeWordsInLanguagePair(dictionaryId),
        ) { dictionaryWords, languagePairWords -> dictionaryWords to languagePairWords }
            .observe(
                onSuccess = { (dictionaryWords, languagePairWords) ->
                    // A question needs three plausible wrong answers, so the whole language pair
                    // — not just this dictionary — must hold at least four distinct entries.
                    val distinctInPair =
                        languagePairWords.distinctBy { it.word + it.translation }.size

                    if (dictionaryWords.isEmpty() || distinctInPair < REQUIRED_WORDS_FOR_TEST) {
                        _currentQuestion.emit(MultipleChoiceCurrentQuestionState.NotEnoughWords)
                    } else if (questions.isEmpty()) {
                        words = dictionaryWords
                        // One question per form of every word in *this* dictionary: 11 forms in
                        // the dictionary means 11 questions.
                        questions = dictionaryWords.flatMap { it.toQuestions() }.shuffled()
                        distractorsByForm = languagePairWords
                            .flatMap { it.toQuestions() }
                            .groupBy({ it.formKey }, { it.answer })
                        resetScoring()
                        initNextQuestion()
                    }
                },
                onError = {
                    _currentQuestion.emit(MultipleChoiceCurrentQuestionState.Failed)
                }
            )
    }

    fun onAnswerReceived(answer: String) = viewModelScope.launch {
        _selectedAnswer.emit(answer)
    }

    fun onAnswerGiven() = viewModelScope.launch {
        val question = _currentQuestion.value
        if (question is MultipleChoiceCurrentQuestionState.Success) {
            val currentQuestionWordUiId = question.multipleChoiceCurrentQuestion.question.wordId
            val correctAnswer = question.multipleChoiceCurrentQuestion.question.answer
            if (correctAnswer == _selectedAnswer.value) {
                score += 1
                registerCorrect(currentQuestionWordUiId)
            } else {
                registerWrong(currentQuestionWordUiId)
            }
            // The correct/incorrect result is shown inline by the screen (derived from `answered`
            // + the selected vs. correct answer), so it never covers the action buttons the way a
            // bottom snackbar did — the user can advance immediately.
            _answered.emit(true)
        }
    }

    fun onNextQuestionRequested() = viewModelScope.launch {
        // Already past the last question (Completed is showing) — ignore further requests.
        if (currentQuestionIndex >= questions.size) return@launch

        _selectedAnswer.emit("")
        _answered.emit(false)
        currentQuestionIndex += 1
        initNextQuestion()
    }

    fun onRetryClicked() = viewModelScope.launch {
        _selectedAnswer.emit("")
        _answered.emit(false)
        resetScoring()
        currentQuestionIndex = 0
        initNextQuestion()
    }

    private fun resetScoring() {
        score = 0
        raisedWordIds.clear()
        loweredWordIds.clear()
    }

    /** First correct answer for a word raises its level once; later correct forms only add to the score. */
    private fun registerCorrect(wordId: String) {
        if (raisedWordIds.add(wordId)) applyWordLevel(wordId)
    }

    /** First wrong answer for a word lowers its level once; repeats are ignored. */
    private fun registerWrong(wordId: String) {
        if (loweredWordIds.add(wordId)) applyWordLevel(wordId)
    }

    private suspend fun initNextQuestion() {
        val nextQuestionState = if (currentQuestionIndex == questions.size) {
            // Scored per question: every correct answer counts, across all forms of every word.
            MultipleChoiceCurrentQuestionState.Completed(
                passed = score > (questions.size / 2),
                percentage = ((score.toDouble() / questions.size.toDouble()) * 100).toInt(),
                correctAnswers = score,
                totalQuestions = questions.size,
            )
        } else {
            val question = questions[currentQuestionIndex]
            val currentQuestion = MultipleChoiceCurrentQuestion(
                question = question,
                choices = prepareChoices(question)
            )

            MultipleChoiceCurrentQuestionState.Success(
                multipleChoiceCurrentQuestion = currentQuestion,
                index = currentQuestionIndex + 1,
                size = questions.size
            )
        }

        _currentQuestion.emit(nextQuestionState)
    }

    /**
     * A base question (word → translation) plus one question per grammatical form entered on
     * both language sides, e.g. go/went/gone → gehen/ging/(sein) gegangen yields three questions.
     */
    private fun WordUi.toQuestions(): List<MultipleChoiceQuestion> {
        val sourceMeta = WordMeta.parse(wordMeta)
        val targetMeta = WordMeta.parse(translationMeta)

        return buildList {
            // Surfaces are stored as JSON arrays; the question/answer show "kaufen/erwerben".
            add(
                MultipleChoiceQuestion(
                    wordId,
                    surfacesDisplay(word, languageCodeOf(wordMeta)),
                    surfacesDisplay(translation, languageCodeOf(translationMeta)),
                )
            )
            FieldKey.entries.forEach { key ->
                val questionForm = sourceMeta?.displayForm(key)
                val answerForm = targetMeta?.displayForm(key)
                if (questionForm != null && answerForm != null) {
                    add(MultipleChoiceQuestion(wordId, questionForm, answerForm, key))
                }
            }
        }
    }

    /**
     * Distractors of the same grammatical form come first (a past-tense question offers other
     * past-tense answers), padded with answers of other forms when there are not enough. Both come
     * from the language-pair pool, so wrong answers are never limited to the dictionary under test.
     */
    private fun prepareChoices(
        current: MultipleChoiceQuestion,
        count: Int = 3
    ): List<String> {
        val sameForm = distractorsByForm[current.formKey].orEmpty()
        val otherForms = distractorsByForm
            .filterKeys { it != current.formKey }
            .values
            .flatten()

        return (sameForm.distinct().shuffled() + otherForms.distinct().shuffled())
            .distinct()
            .filter { it != current.answer }
            .take(count)
            .plus(current.answer)
            .shuffled()
    }

    /**
     * Re-derives the word's level from its stored value plus the (at most) one raise and one lower
     * it has earned this test, so the two independent latches compose correctly and repeated saves
     * stay consistent regardless of the order answers arrive in.
     */
    private fun applyWordLevel(wordId: String) = viewModelScope.launch {
        try {
            val wordUi = words.first { it.wordId == wordId }
            val delta =
                (if (wordId in raisedWordIds) 1 else 0) + (if (wordId in loweredWordIds) -1 else 0)
            val newLevel = (wordUi.level + delta).coerceIn(0, 7)

            wordService.updateWordLevel(wordId = wordId, level = newLevel)
        } catch (e: Exception) {
            // The answer already counted toward the score; only persisting the level failed.
            _snackbarMessages.emit(UiText.Resource(ResStrings.errorSaveFailed))
        }
    }
}