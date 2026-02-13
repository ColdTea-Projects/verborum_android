package de.coldtea.verborum.bibliotheca.word.ui.selfpractice.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.coldtea.verborum.bibliotheca.common.ui.components.IconOnTopButton
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables

@Composable
fun SwitchWordsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconOnTopButton(
        modifier = modifier.size(48.dp),
        iconRes = ResDrawables.ic_switch_48,
        backgroundColor = MaterialTheme.colorScheme.primary,
        initialPadding = 16.dp,
        iconSize = 32.dp,
        onClick = onClick,
    )
}