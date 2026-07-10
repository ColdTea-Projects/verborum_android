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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.createword.composables.LanguageInputCard
import de.coldtea.verborum.bibliotheca.word.ui.createword.composables.WordTypeChips
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.CreateWordState
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageGrammar
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun CreateWordScreen(
    viewModel: CreateWordViewModel = hiltViewModel()
) {
    val createWordState =
        viewModel.createWordState.collectAsState(initial = CreateWordState.Loading).value

    var selectedType by remember { mutableStateOf<WordType?>(null) }
    var sourceInput by remember { mutableStateOf(WordFormInput()) }
    var targetInput by remember { mutableStateOf(WordFormInput()) }

    if (createWordState is CreateWordState.Success) {
        val dictionary = createWordState.dictionaryUi

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Text(
                text = stringResource(ResStrings.createWordScreenHeader),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 0.5.sp
            )

            Text(
                text = dictionary.name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                selectedType = selectedType,
                onTypeSelected = { wordType ->
                    if (wordType != selectedType) {
                        selectedType = wordType
                        // Keep the typed words; clear only the type-specific grammatical fields.
                        sourceInput = WordFormInput(text = sourceInput.text)
                        targetInput = WordFormInput(text = targetInput.text)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            selectedType?.let { wordType ->
                Spacer(modifier = Modifier.height(24.dp))

                LanguageInputCard(
                    languageName = languageDisplayName(dictionary.fromLang),
                    languageCode = dictionary.fromLang,
                    barColor = MaterialTheme.colorScheme.primary,
                    spec = LanguageGrammar.formSpec(dictionary.fromLang, wordType),
                    input = sourceInput,
                    onInputChange = { sourceInput = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LanguageInputCard(
                    languageName = languageDisplayName(dictionary.toLang),
                    languageCode = dictionary.toLang,
                    barColor = MaterialTheme.colorScheme.secondary,
                    spec = LanguageGrammar.formSpec(dictionary.toLang, wordType),
                    input = targetInput,
                    onInputChange = { targetInput = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.saveWord(
                            wordType = wordType,
                            sourceInput = sourceInput,
                            targetInput = targetInput,
                        )
                        sourceInput = WordFormInput()
                        targetInput = WordFormInput()
                    },
                    enabled = sourceInput.text.isNotBlank() && targetInput.text.isNotBlank(),
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
                        text = stringResource(ResStrings.createWordScreenSave),
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
