package de.coldtea.verborum.bibliotheca.dictionary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar

@Composable
fun DictionaryListScreen(
    viewModel: DictionaryListViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onDictionaryClick: (String) -> Unit,
    onCreateDictionaryClick: () -> Unit,
) {
    val dictionaries = viewModel.dictionariesState.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    RegisterTopBar(
        title = stringResource(ResStrings.dictionaryListScreenHeader),
        subtitle = stringResource(ResStrings.dictionaryListScreenSubtitle),
        showBackButton = false,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Dictionary List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(dictionaries) { index, dictionary ->
                DictionaryCard(
                    dictionary = dictionary,
                    index = index,
                    onClick = onDictionaryClick
                )
            }
        }

        // Sticky bottom action
        Button(
            onClick = onCreateDictionaryClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(ResDrawables.ic_plus_24),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(ResStrings.dictionaryListScreenCreate),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
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
