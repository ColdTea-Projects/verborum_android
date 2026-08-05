package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/** The dictionary's direction as localized names, e.g. "English to German". */
@Composable
fun languagePairLabel(fromLang: String, toLang: String): String =
    stringResource(
        ResStrings.dictionaryLanguagePair,
        languageName(fromLang),
        languageName(toLang),
    )

@Composable
private fun languageName(code: String): String =
    SupportedLanguage.fromCode(code)?.let { stringResource(it.displayNameRes) } ?: code
