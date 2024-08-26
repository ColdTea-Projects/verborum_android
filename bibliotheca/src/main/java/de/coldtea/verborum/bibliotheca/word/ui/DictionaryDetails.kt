package de.coldtea.verborum.bibliotheca.word.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.word.ui.model.Loading
import de.coldtea.verborum.bibliotheca.word.ui.model.Success

@Composable
fun DictionaryDetailsScreen(
    viewModel: DictionaryDetailsViewModel = hiltViewModel(),
) {
    val dictionary = viewModel.dictionariesState.collectAsState(Loading).value

    if (dictionary is Success){
        Column {
            Text(text = dictionary.dictionaryUi.dictionaryId)
            Text(text = dictionary.dictionaryUi.name)
            Text(text = dictionary.dictionaryUi.userId)
        }
    }
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    DictionaryDetailsScreen()
}