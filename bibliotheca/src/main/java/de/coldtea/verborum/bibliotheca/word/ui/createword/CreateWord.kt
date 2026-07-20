package de.coldtea.verborum.bibliotheca.word.ui.createword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.ui.components.ScreenError
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.createword.composables.LanguageInputCard
import de.coldtea.verborum.bibliotheca.word.ui.createword.composables.OtherTypeDropdown
import de.coldtea.verborum.bibliotheca.word.ui.createword.composables.WordTypeChips
import de.coldtea.verborum.bibliotheca.word.ui.createword.composables.textFieldsPerMeaning
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.CreateWordState
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageGrammar
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordCategory
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.defaultType
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.parseWordFormInputs
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.parseWordType
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar
import de.coldtea.verborum.core.ui.ShowSnackbarMessages

@Composable
fun CreateWordScreen(
    viewModel: CreateWordViewModel = hiltViewModel(),
    onWordUpdated: () -> Unit = {},
) {
    val createWordState =
        viewModel.createWordState.collectAsState(initial = CreateWordState.Loading).value

    ShowSnackbarMessages(viewModel.snackbarMessages)

    var selectedType by remember { mutableStateOf<WordType?>(null) }
    // One entry per alternative meaning; the first is always present, the + button appends more.
    var sourceInputs by remember { mutableStateOf(listOf(WordFormInput())) }
    var targetInputs by remember { mutableStateOf(listOf(WordFormInput())) }

    // Post-save handling runs only when the save actually succeeded: edit closes the screen,
    // create clears the form for the next entry. A failed save surfaces a snackbar and stays put.
    LaunchedEffect(Unit) {
        viewModel.wordSaved.collect { wasEditing ->
            if (wasEditing) {
                onWordUpdated()
            } else {
                sourceInputs = listOf(WordFormInput())
                targetInputs = listOf(WordFormInput())
            }
        }
    }

    when (createWordState) {
        is CreateWordState.Loading -> Unit
        is CreateWordState.Failed -> {
            RegisterTopBar(title = stringResource(ResStrings.errorScreenTitle), showBackButton = true)
            ScreenError(onRetry = viewModel::retry)
        }
        is CreateWordState.Success -> {
        val dictionary = createWordState.dictionaryUi
        val editingWord = createWordState.editingWord
        val isEditing = editingWord != null

        // Switches the active word type: back on the edited word's own type restores its stored
        // grammar; any other type keeps the typed words but clears the old type's grammatical fields.
        fun applyType(newType: WordType) {
            if (newType == selectedType) return
            selectedType = newType
            if (editingWord != null && newType == parseWordType(editingWord.wordMeta)) {
                sourceInputs = parseWordFormInputs(dictionary.fromLang, editingWord.word, editingWord.wordMeta)
                    .ifEmpty { listOf(WordFormInput()) }
                targetInputs = parseWordFormInputs(dictionary.toLang, editingWord.translation, editingWord.translationMeta)
                    .ifEmpty { listOf(WordFormInput()) }
            } else {
                sourceInputs = sourceInputs.map { WordFormInput(text = it.text) }
                targetInputs = targetInputs.map { WordFormInput(text = it.text) }
            }
        }

        // Edit mode: prefill the form from the stored word once it is loaded.
        LaunchedEffect(editingWord) {
            editingWord?.let { word ->
                selectedType = parseWordType(word.wordMeta)
                sourceInputs = parseWordFormInputs(dictionary.fromLang, word.word, word.wordMeta)
                    .ifEmpty { listOf(WordFormInput()) }
                targetInputs = parseWordFormInputs(dictionary.toLang, word.translation, word.translationMeta)
                    .ifEmpty { listOf(WordFormInput()) }
            }
        }

        RegisterTopBar(
            title = stringResource(
                if (isEditing) ResStrings.createWordScreenHeaderEdit
                else ResStrings.createWordScreenHeader
            ),
            subtitle = dictionary.name,
            showBackButton = true,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Word Type
            Text(
                text = stringResource(ResStrings.createWordScreenWordType),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            WordTypeChips(
                selectedCategory = selectedType?.category,
                onCategorySelected = { category ->
                    if (category != selectedType?.category) applyType(category.defaultType)
                },
                modifier = Modifier.fillMaxWidth()
            )

            selectedType?.let { wordType ->
                // The "Other" bucket picks its concrete part of speech from a dropdown.
                if (wordType.category == WordCategory.OTHER) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OtherTypeDropdown(
                        selected = wordType,
                        onSelected = { applyType(it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // One shared, ordered list of focus requesters spanning both cards so the IME "Next"
                // action walks every text field top-to-bottom; only the very last one shows "Done".
                val sourceSpec = LanguageGrammar.formSpec(dictionary.fromLang, wordType)
                val targetSpec = LanguageGrammar.formSpec(dictionary.toLang, wordType)
                val sourceFieldCount = textFieldsPerMeaning(sourceSpec) * sourceInputs.size
                val targetFieldCount = textFieldsPerMeaning(targetSpec) * targetInputs.size
                val totalFieldCount = sourceFieldCount + targetFieldCount
                val focusRequesters = remember(totalFieldCount) {
                    List(totalFieldCount) { FocusRequester() }
                }

                LanguageInputCard(
                    languageName = languageDisplayName(dictionary.fromLang),
                    languageCode = dictionary.fromLang,
                    barColor = MaterialTheme.colorScheme.primary,
                    spec = sourceSpec,
                    inputs = sourceInputs,
                    onInputChange = { index, updated ->
                        sourceInputs = sourceInputs.toMutableList().also { it[index] = updated }
                    },
                    onAddAlternative = { sourceInputs = sourceInputs + WordFormInput() },
                    // filterIndexed instead of removeAt: a double-tap on the same x delivers two
                    // clicks against one list state; a stale index must not throw.
                    onRemoveAlternative = { index ->
                        sourceInputs = sourceInputs.filterIndexed { i, _ -> i != index }
                    },
                    fieldRequesters = focusRequesters.subList(0, sourceFieldCount),
                    // After the source card's last field, hop to the target card's first field.
                    nextFieldRequester = focusRequesters.getOrNull(sourceFieldCount),
                )

                Spacer(modifier = Modifier.height(16.dp))

                LanguageInputCard(
                    languageName = languageDisplayName(dictionary.toLang),
                    languageCode = dictionary.toLang,
                    barColor = MaterialTheme.colorScheme.secondary,
                    spec = targetSpec,
                    inputs = targetInputs,
                    onInputChange = { index, updated ->
                        targetInputs = targetInputs.toMutableList().also { it[index] = updated }
                    },
                    onAddAlternative = { targetInputs = targetInputs + WordFormInput() },
                    onRemoveAlternative = { index ->
                        targetInputs = targetInputs.filterIndexed { i, _ -> i != index }
                    },
                    fieldRequesters = focusRequesters.subList(sourceFieldCount, totalFieldCount),
                    // Target is the last card: its final field ends the form (Done).
                    nextFieldRequester = null,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    // Navigation/clearing is driven by the wordSaved event, so it happens only on
                    // a successful save; a failure keeps the form and shows a snackbar.
                    onClick = {
                        viewModel.saveWord(
                            wordType = wordType,
                            sourceInputs = sourceInputs,
                            targetInputs = targetInputs,
                        )
                    },
                    enabled = sourceInputs.any { it.text.isNotBlank() } &&
                            targetInputs.any { it.text.isNotBlank() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (isEditing) ResStrings.createWordScreenUpdate
                            else ResStrings.createWordScreenSave
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun languageDisplayName(languageCode: String): String =
    SupportedLanguage.fromCode(languageCode)
        ?.let { stringResource(it.displayNameRes) }
        ?: languageCode

@Preview
@Composable
fun CreateWordScreenPreview() {
    VerborumTheme {
        CreateWordScreen()
    }
}
