package de.coldtea.verborum.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.ui.model.dictionaryTagByCode

/**
 * Renders a dictionary's tags as a single small line of translated labels separated by middle dots,
 * e.g. "Food & drink · A1 · IELTS/Cambridge (en)". Translatable tags follow the device language;
 * fixed names (framework/exam codes) show as-is. Unknown codes are skipped. Renders nothing when
 * there are no resolvable tags.
 */
@Composable
fun DictionaryTagsText(
    tagCodes: List<String>,
    modifier: Modifier = Modifier,
) {
    val labels = dictionaryTagLabels(tagCodes)
    if (labels.isEmpty()) return

    Text(
        text = labels.joinToString(separator = " · "),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/**
 * The display labels of [tagCodes], in order: translatable tags follow the device language, fixed
 * names (framework/exam codes) show as-is, unknown codes are skipped. Shared by [DictionaryTagsText]
 * and the forum's tag chips so both resolve tags identically.
 */
@Composable
fun dictionaryTagLabels(tagCodes: List<String>): List<String> {
    // Resolve in a plain loop: stringResource can't be called from the lambda of a map/forEach.
    val labels = mutableListOf<String>()
    for (code in tagCodes) {
        val tag = dictionaryTagByCode(code) ?: continue
        val label = tag.labelRes?.let { stringResource(it) } ?: tag.name ?: continue
        labels.add(label)
    }
    return labels
}
