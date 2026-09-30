package de.coldtea.verborum.core.ui.components

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.core.utils.ResDrawables

/** One row of a [SelectionBottomSheet] or [MultiSelectionBottomSheet]. */
data class SelectionOption(
    val label: String,
    val isSelected: Boolean,
    val onSelect: () -> Unit,
)

/** A titled group of rows in a [MultiSelectionBottomSheet]. */
data class SelectionSection(
    val title: String,
    val options: List<SelectionOption>,
)

/**
 * A titled list of single-choice options in a modal bottom sheet — the selected row is bold with a
 * checkmark, and picking a row closes the sheet. Scrolls when the list is long (e.g. the language
 * pickers). Backs the list screens' language filters and the bibliotheca sort menu.
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
            SheetTitle(title)
            LazyColumn(
                // Caps the height so a long language list scrolls while a short one still wraps.
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
            ) {
                items(options) { option ->
                    SelectionRow(
                        option = option,
                        onClick = {
                            option.onSelect()
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

/**
 * The multi-choice sibling of [SelectionBottomSheet]: rows grouped under section headers, each
 * tap toggling one row while the sheet stays open until dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectionBottomSheet(
    title: String,
    sections: List<SelectionSection>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            SheetTitle(title)
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                sections.forEach { section ->
                    item(key = "header:${section.title}") {
                        Text(
                            text = section.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(section.options) { option ->
                        SelectionRow(option = option, onClick = option.onSelect, role = Role.Checkbox)
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
    )
}

@Composable
private fun SelectionRow(
    option: SelectionOption,
    onClick: () -> Unit,
    role: Role = Role.RadioButton,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = role, onClick = onClick)
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
