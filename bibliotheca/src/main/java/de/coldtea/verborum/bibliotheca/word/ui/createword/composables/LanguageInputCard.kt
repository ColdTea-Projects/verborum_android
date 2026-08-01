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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
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
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordInputFilter
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import de.coldtea.verborum.bibliotheca.word.ui.model.displayLine
import de.coldtea.verborum.core.theme.VerborumTheme

/** The number of keyboard text fields one meaning of [spec] renders: the base word plus its text forms. */
internal fun textFieldsPerMeaning(spec: LanguageFormSpec): Int =
    1 + spec.fields.count { it is FormField.TextForm }

@Composable
fun LanguageInputCard(
    modifier: Modifier = Modifier,
    languageName: String,
    languageCode: String,
    barColor: Color,
    // Drives input filtering alongside [spec]: free text accepts anything, every other type is
    // restricted to its language's letters (docs/word-input-filtering-android.md §3, §5).
    wordType: WordType,
    spec: LanguageFormSpec,
    inputs: List<WordFormInput>,
    onInputChange: (Int, WordFormInput) -> Unit,
    onAddAlternative: () -> Unit,
    onRemoveAlternative: (Int) -> Unit,
    // One requester per text field this card renders, in top-to-bottom order; the IME "Next" action
    // advances along them. [nextFieldRequester] is the field to jump to after this card's last one
    // (the next card's first field), or null when this card ends the whole form.
    fieldRequesters: List<FocusRequester> = emptyList(),
    nextFieldRequester: FocusRequester? = null,
) {
    val perMeaning = textFieldsPerMeaning(spec)
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
                        wordType = wordType,
                        spec = spec,
                        input = input,
                        onInputChange = { onInputChange(index, it) },
                        slotOffset = index * perMeaning,
                        requesters = fieldRequesters,
                        nextAfterLast = nextFieldRequester,
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
    wordType: WordType,
    spec: LanguageFormSpec,
    input: WordFormInput,
    onInputChange: (WordFormInput) -> Unit,
    slotOffset: Int,
    requesters: List<FocusRequester>,
    nextAfterLast: FocusRequester?,
) {
    // Resolves a text field's requester and the field the IME "Next" action should jump to: the next
    // slot in this card, or [nextAfterLast] when this is the card's final field.
    fun requesterAt(slot: Int): FocusRequester? = requesters.getOrNull(slot)
    fun nextAfter(slot: Int): FocusRequester? = requesters.getOrNull(slot + 1) ?: nextAfterLast

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

    // The base word occupies the first slot; the text forms follow in declaration order (choice
    // chips take no slot, as they carry no keyboard).
    val textFormKeys = spec.fields.filterIsInstance<FormField.TextForm>().map { it.key }

    ScriptTextField(
        languageCode = languageCode,
        wordType = wordType,
        // The base word has no meta key of its own.
        fieldKey = null,
        value = input.text,
        onValueChange = { onInputChange(input.copy(text = it)) },
        label = stringResource(ResStrings.createWordScreenTextLabel),
        focusRequester = requesterAt(slotOffset),
        nextFocusRequester = nextAfter(slotOffset),
    )

    spec.fields.forEach { field ->
        Spacer(modifier = Modifier.height(12.dp))
        when (field) {
            is FormField.TextForm -> {
                val slot = slotOffset + 1 + textFormKeys.indexOf(field.key)
                ScriptTextField(
                    languageCode = languageCode,
                    wordType = wordType,
                    fieldKey = field.key,
                    value = input.field(field.key),
                    onValueChange = { onInputChange(input.withField(field.key, it)) },
                    label = stringResource(field.labelRes),
                    placeholder = field.hintRes?.let { stringResource(it) },
                    focusRequester = requesterAt(slot),
                    nextFocusRequester = nextAfter(slot),
                )
            }

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
 * language's keyboard, lays itself out right-to-left for RTL scripts, filters what may be typed
 * through [WordInputFilter], and wires the IME action so "Next" advances to [nextFocusRequester].
 * When there is no next field, the action becomes "Done" and dismisses the keyboard.
 *
 * It drives a [TextFieldValue] rather than a plain String because the filter has to see the IME's
 * composition state: for Japanese/Chinese/Korean the field holds candidate text that a
 * per-keystroke filter would corrupt, so those languages are only validated once composition ends
 * (docs/word-input-filtering-android.md §6).
 */
@Composable
private fun ScriptTextField(
    languageCode: String,
    wordType: WordType,
    fieldKey: FieldKey?,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester?,
    nextFocusRequester: FocusRequester?,
    placeholder: String? = null,
) {
    val direction = if (languageCode.lowercase() in RTL_LANGUAGE_CODES) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    // Set when the last change carried something the field rejects; cleared as soon as an accepted
    // change comes through, so the hint only shows on a rejection.
    var rejection by remember { mutableStateOf<WordInputFilter.Rejection?>(null) }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    // Re-sync whenever the hoisted value moves on its own — a rejected keystroke bouncing back, a
    // word loaded for editing, the form cleared after a save — and park the cursor at the end.
    if (fieldValue.text != value) {
        fieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
    }
    val deferToCommit = WordInputFilter.defersToCommit(languageCode)
    val focusManager = LocalFocusManager.current

    // Ask the IME for this dictionary language's script (Arabic keyboard for ar/fa, Greek for el…),
    // except on the reading, which the user writes as a pronunciation note in their own language —
    // there we hint the app's current locale instead. The last field on the form uses Done, every
    // earlier one Next.
    val imeLocale = if (fieldKey == FieldKey.READING) Locale.current else Locale(languageCode)
    val keyboardOptions = KeyboardOptions(
        hintLocales = LocaleList(imeLocale),
        imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done,
    )
    val keyboardActions = KeyboardActions(
        onNext = { nextFocusRequester?.requestFocus() },
        onDone = { focusManager.clearFocus() },
    )

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = { changed ->
                if (deferToCommit && changed.composition != null) {
                    // Mid-composition: the IME owns this text until it commits. Let it through
                    // untouched — the filter runs on the committed result a keystroke later.
                    fieldValue = changed
                    onValueChange(changed.text)
                } else {
                    val filtered = WordInputFilter.apply(
                        languageCode = languageCode,
                        wordType = wordType,
                        fieldKey = fieldKey,
                        text = changed.text,
                    )
                    rejection = filtered.rejection
                    fieldValue = if (filtered.text == changed.text) {
                        changed
                    } else {
                        // Characters were dropped, so the IME's selection no longer maps onto the
                        // text; collapse the cursor to the end of what survived.
                        changed.copy(
                            text = filtered.text,
                            selection = TextRange(filtered.text.length),
                        )
                    }
                    onValueChange(filtered.text)
                }
            },
            label = { Text(text = label) },
            placeholder = placeholder?.let { { Text(text = it) } },
            singleLine = true,
            isError = rejection != null,
            supportingText = rejection?.let { reason ->
                {
                    Text(
                        text = when (reason) {
                            WordInputFilter.Rejection.FOREIGN_SCRIPT -> stringResource(
                                ResStrings.createWordScreenScriptRestriction,
                                scriptDisplayName(languageCode),
                            )

                            WordInputFilter.Rejection.NON_LETTER ->
                                stringResource(ResStrings.createWordScreenLettersOnlyRestriction)
                        },
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
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
            wordType = WordType.NOUN,
            spec = LanguageGrammar.formSpec("de", WordType.NOUN),
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
