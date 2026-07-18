package de.coldtea.verborum.bibliotheca.word.ui.createword.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FormField
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.Gender
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageFormSpec
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageGrammar
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageScript
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.bibliotheca.word.ui.model.displayLine
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun LanguageInputCard(
    modifier: Modifier = Modifier,
    languageName: String,
    languageCode: String,
    barColor: Color,
    spec: LanguageFormSpec,
    inputs: List<WordFormInput>,
    onInputChange: (Int, WordFormInput) -> Unit,
    onAddAlternative: () -> Unit,
    onRemoveAlternative: (Int) -> Unit,
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

                inputs.forEachIndexed { index, input ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(20.dp))
                        AlternativeHeader(onRemove = { onRemoveAlternative(index) })
                    }
                    MeaningFields(
                        languageCode = languageCode,
                        spec = spec,
                        input = input,
                        onInputChange = { onInputChange(index, it) },
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                // The + sits at the left of its own row, right after the last field.
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onAddAlternative,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(ResDrawables.ic_plus_24),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(ResStrings.createWordScreenAddAlternative),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                PreviewLine(languageCode = languageCode, inputs = inputs)
            }
        }
    }
}

/** The fields for one meaning: gender chips (if any), the base word, then the grammatical forms. */
@Composable
private fun MeaningFields(
    languageCode: String,
    spec: LanguageFormSpec,
    input: WordFormInput,
    onInputChange: (WordFormInput) -> Unit,
) {
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

    // Ask the IME for this dictionary language's script (Arabic keyboard for ar/fa, Greek for el…);
    // RTL scripts also lay their fields out right-to-left so entry reads naturally.
    val keyboardOptions = KeyboardOptions(hintLocales = LocaleList(Locale(languageCode)))

    ScriptTextField(
        languageCode = languageCode,
        value = input.text,
        onValueChange = { onInputChange(input.copy(text = it)) },
        label = stringResource(ResStrings.createWordScreenTextLabel),
        keyboardOptions = keyboardOptions,
    )

    spec.fields.forEach { field ->
        Spacer(modifier = Modifier.height(12.dp))
        when (field) {
            is FormField.TextForm -> ScriptTextField(
                languageCode = languageCode,
                value = input.field(field.key),
                onValueChange = { onInputChange(input.withField(field.key, it)) },
                label = stringResource(field.labelRes),
                placeholder = field.hintRes?.let { stringResource(it) },
                keyboardOptions = keyboardOptions,
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

            is FormField.LabeledChoiceForm -> {
                FieldLabel(text = stringResource(field.labelRes))
                Spacer(modifier = Modifier.height(8.dp))
                LabeledChoiceChips(
                    options = field.options,
                    selected = input.field(field.key).takeIf { it.isNotBlank() },
                    onSelected = { onInputChange(input.withField(field.key, it)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AlternativeHeader(onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FieldLabel(text = stringResource(ResStrings.createWordScreenAlternative))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                painter = painterResource(ResDrawables.ic_close_24),
                contentDescription = stringResource(ResStrings.createWordScreenRemoveAlternative),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Language codes whose script is written right-to-left. */
private val RTL_LANGUAGE_CODES = setOf("ar", "fa")

/**
 * A single-line text field bound to a dictionary language's script: it hints the IME to open that
 * language's keyboard via [keyboardOptions], and lays itself out right-to-left for RTL scripts.
 */
@Composable
private fun ScriptTextField(
    languageCode: String,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions,
    placeholder: String? = null,
) {
    val direction = if (languageCode.lowercase() in RTL_LANGUAGE_CODES) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    // Latched true when the last keystroke introduced a wrong-script letter (which we drop);
    // cleared as soon as an accepted change comes through, so the hint only shows on a rejection.
    var rejectedForeignScript by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        OutlinedTextField(
            value = value,
            onValueChange = { raw ->
                val sanitized = LanguageScript.sanitize(languageCode, raw)
                rejectedForeignScript = sanitized != raw
                onValueChange(sanitized)
            },
            label = { Text(text = label) },
            placeholder = placeholder?.let { { Text(text = it) } },
            singleLine = true,
            isError = rejectedForeignScript,
            supportingText = if (rejectedForeignScript) {
                {
                    Text(
                        text = stringResource(
                            ResStrings.createWordScreenScriptRestriction,
                            scriptDisplayName(languageCode),
                        )
                    )
                }
            } else {
                null
            },
            keyboardOptions = keyboardOptions,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** The dictionary language's own name, used to explain a blocked keystroke ("Only Greek letters…"). */
@Composable
private fun scriptDisplayName(languageCode: String): String =
    SupportedLanguage.fromCode(languageCode)?.displayNameRes
        ?.let { stringResource(it) }
        ?: languageCode.uppercase()

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
private fun PreviewLine(languageCode: String, inputs: List<WordFormInput>) {
    val surfaces = inputs.mapNotNull { input ->
        LanguageGrammar.composeSurface(languageCode, input.gender, input.text).trim().takeIf { it.isNotBlank() }
    }
    if (surfaces.isEmpty()) return

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        // Same join rules as the stored-word renderers: "/" between alternatives, " · " between slots.
        text = displayLine(surfaces, inputs.map { it.fields }, languageCode),
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
            inputs = listOf(
                WordFormInput(
                    text = "Apfel",
                    gender = Gender.MASCULINE,
                    fields = mapOf(FieldKey.PLURAL to "Äpfel"),
                ),
            ),
            onInputChange = { _, _ -> },
            onAddAlternative = {},
            onRemoveAlternative = {},
        )
    }
}
