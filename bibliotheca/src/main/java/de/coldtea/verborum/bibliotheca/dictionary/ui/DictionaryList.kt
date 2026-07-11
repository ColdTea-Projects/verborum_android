package de.coldtea.verborum.bibliotheca.dictionary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.composables.DictionaryCard
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun DictionaryListScreen(
    viewModel: DictionaryListViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onDictionaryClick: (String) -> Unit,
    onCreateDictionaryClick: () -> Unit,
) {
    val dictionaries = viewModel.dictionariesState.collectAsState().value
    var dictionaryToDelete by remember { mutableStateOf<DictionaryUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCreateDictionaryClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            ) {
                Icon(
                    painter = painterResource(ResDrawables.ic_plus_24),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(ResStrings.dictionaryListScreenCreate))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text(
            text = stringResource(ResStrings.dictionaryListScreenHeader),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 0.5.sp
        )

        Text(
            text = stringResource(ResStrings.dictionaryListScreenSubtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Dictionary List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(dictionaries) { index, dictionary ->
                DictionaryCard(
                    dictionary = dictionary,
                    index = index,
                    onDeleteClick = { dictionaryToDelete = it },
                    onClick = onDictionaryClick
                )
            }
        }
    }

    dictionaryToDelete?.let { dictionary ->
        DeleteDictionaryDialog(
            dictionary = dictionary,
            onConfirm = {
                viewModel.deleteDictionary(dictionary.dictionaryId)
                dictionaryToDelete = null
            },
            onDismiss = { dictionaryToDelete = null },
        )
    }
}

@Composable
private fun DeleteDictionaryDialog(
    dictionary: DictionaryUi,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(ResStrings.dictionaryListScreenDeleteDialogTitle)) },
        text = {
            Text(
                text = stringResource(
                    ResStrings.dictionaryListScreenDeleteDialogMessage,
                    dictionary.name,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(ResStrings.dictionaryListScreenDeleteDialogConfirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(ResStrings.dictionaryListScreenDeleteDialogCancel))
            }
        },
    )
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    VerborumTheme {
        DictionaryListScreen(
            snackbarHostState = SnackbarHostState(),
            onDictionaryClick = { _ -> },
            onCreateDictionaryClick = {}
        )
    }
}