package de.coldtea.verborum.bibliotheca.word.ui.createword.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FormField
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.Gender
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageFormSpec
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageGrammar
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun LanguageInputCard(
    modifier: Modifier = Modifier,
    languageName: String,
    languageCode: String,
    barColor: Color,
    spec: LanguageFormSpec,
    input: WordFormInput,
    onInputChange: (WordFormInput) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Accent bar
            Spacer(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
            ) {
                Text(
                    text = languageName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (spec.genderOptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FieldLabel(text = stringResource(ResStrings.createWordScreenGenderLabel))
                    Spacer(modifier = Modifier.height(8.dp))
                    GenderChips(
                        languageCode = languageCode,
                        options = spec.genderOptions,
                        selected = input.gender,
                        onSelected = { onInputChange(input.copy(gender = it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = input.text,
                    onValueChange = { onInputChange(input.copy(text = it)) },
                    label = { Text(text = stringResource(ResStrings.createWordScreenTextLabel)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                spec.fields.forEach { field ->
                    Spacer(modifier = Modifier.height(12.dp))
                    when (field) {
                        is FormField.TextForm -> OutlinedTextField(
                            value = input.field(field.key),
                            onValueChange = { onInputChange(input.withField(field.key, it)) },
                            label = { Text(text = stringResource(field.labelRes)) },
                            placeholder = field.hintRes?.let { { Text(text = stringResource(it)) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        is FormField.ChoiceForm -> {
                            FieldLabel(text = stringResource(field.labelRes))
                            Spacer(modifier = Modifier.height(8.dp))
                            ChoiceChips(
                                options = field.options,
                                selected = input.field(field.key).takeIf { it.isNotBlank() },
                                onSelected = { onInputChange(input.withField(field.key, it)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                PreviewLine(languageCode = languageCode, input = input)
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PreviewLine(languageCode: String, input: WordFormInput) {
    val surface = LanguageGrammar.composeSurface(languageCode, input.gender, input.text)
    if (surface.isBlank()) return

    val plural = input.field(FieldKey.PLURAL).trim()
    val preview = if (plural.isNotBlank()) "$surface · $plural" else surface

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = preview,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Preview
@Composable
fun LanguageInputCardPreview() {
    VerborumTheme {
        LanguageInputCard(
            languageName = "German",
            languageCode = "de",
            barColor = Color(0xFFC41E3A),
            spec = LanguageGrammar.formSpec(
                "de",
                de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType.NOUN,
            ),
            input = WordFormInput(
                text = "Apfel",
                gender = Gender.MASCULINE,
                fields = mapOf(FieldKey.PLURAL to "Äpfel"),
            ),
            onInputChange = {},
        )
    }
}
