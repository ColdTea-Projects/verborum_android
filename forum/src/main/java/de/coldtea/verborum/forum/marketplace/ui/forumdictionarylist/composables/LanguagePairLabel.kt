package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair

/** "German ⇄ Turkish" — the two-way arrow says the pair matches either direction. */
@Composable
fun languagePairLabel(pair: ForumLanguagePair): String =
    stringResource(
        ResStrings.forumLanguagePairChip,
        stringResource(pair.first.displayNameRes),
        stringResource(pair.second.displayNameRes),
    )
