package de.coldtea.verborum.bibliotheca.word.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.word.ui.model.DictionaryState
import de.coldtea.verborum.bibliotheca.word.ui.model.WordState

@Composable
fun DictionaryDetailsScreen(
    viewModel: DictionaryDetailsViewModel = hiltViewModel(),
) {
    val dictionary =
        viewModel.dictionaryState.collectAsState(initial = DictionaryState.Loading).value
    val wordState = viewModel.wordsState.collectAsState(initial = WordState.Loading).value
    val lazyListState = rememberLazyListState()

    Column {
        if (dictionary is DictionaryState.Success) {
            Column {
                SelectionContainer { Text(text = dictionary.dictionaryUi.dictionaryId) }
                SelectionContainer { Text(text = dictionary.dictionaryUi.name) }
                SelectionContainer { Text(text = dictionary.dictionaryUi.userId) }
            }
            Row {
                Button(onClick = viewModel::addDummyDictionary) {
                    Text(text = "Add")
                }
                Button(onClick = viewModel::cleanWords) {
                    Text(text = "Delete")
                }
            }
        }
        LazyColumn(
            state = lazyListState
        ) {
            if (wordState is WordState.Success) {
                items(wordState.wordList) { word ->
                    Row(
                        Modifier
                            .background(Color(word.wordId.hashCode()))
                            .padding(all = 3.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "${word.word} -> ${word.translation}")
                    }
                }
            }
        }
    }


}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    DictionaryDetailsScreen()
}