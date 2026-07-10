package de.coldtea.verborum.bibliotheca.word.ui.createword.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.core.theme.VerborumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDropdown(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedArticle: String?,
    onArticleSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedArticle.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(ResStrings.createWordScreenArticleLabel)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            options.forEach { article ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = article,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onArticleSelected(article)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun ArticleDropdownPreview() {
    VerborumTheme {
        ArticleDropdown(
            options = listOf("der", "die", "das"),
            selectedArticle = "der",
            onArticleSelected = {},
        )
    }
}
