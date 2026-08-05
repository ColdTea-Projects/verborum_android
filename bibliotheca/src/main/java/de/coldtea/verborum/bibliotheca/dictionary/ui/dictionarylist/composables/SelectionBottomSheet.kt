package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables

/** One row of a [SelectionBottomSheet]. */
data class SelectionOption(
    val label: String,
    val isSelected: Boolean,
    val onSelect: () -> Unit,
)

/**
 * A titled list of single-choice options in a modal bottom sheet — the selected row is bold with a
 * red checkmark. Scrolls when the list is long (e.g. the language pickers). Backs the From/To
 * language filters and the sort menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBottomSheet(
    title: String,
    options: List<SelectionOption>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // Open fully so the long language list is usable without a drag.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
            )
            LazyColumn(
                // Caps the height so a long language list scrolls while a short one still wraps.
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
            ) {
                items(options) { option ->
                    SelectionRow(option = option, onDismiss = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun SelectionRow(option: SelectionOption, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                option.onSelect()
                onDismiss()
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = option.label,
            fontSize = 16.sp,
            fontWeight = if (option.isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (option.isSelected) {
            Icon(
                painter = painterResource(ResDrawables.ic_check_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
