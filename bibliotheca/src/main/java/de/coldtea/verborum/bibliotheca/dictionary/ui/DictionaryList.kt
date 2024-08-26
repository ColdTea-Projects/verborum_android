package de.coldtea.verborum.bibliotheca.dictionary.ui

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

@Composable
fun DictionaryListScreen(
    viewModel: DictionaryListViewModel = hiltViewModel(),
    onItemClicked: (String) -> Unit
) {
    val dicList = viewModel.dictionariesState.collectAsState().value
    val lazyListState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row {
            Button(onClick = viewModel::addDummyDictionary) {
                Text(text = "Add")
            }
            Button(onClick = viewModel::cleanDictionaries) {
                Text(text = "Delete")
            }
        }
        LazyColumn(
            state = lazyListState
        ) {
            items(dicList) { dictionary ->
                Row(
                    Modifier
                        .background(Color(dictionary.dictionaryId.hashCode()))
                        .padding(all = 3.dp)
                        .fillMaxWidth()
                        .clickable { onItemClicked(dictionary.dictionaryId) }
                ) {
                    Text(text = dictionary.dictionaryId)
                }
            }
        }
    }
}

@Preview
@Composable
fun DictionaryListScreenPreview() {
    DictionaryListScreen(
        onItemClicked = {}
    )
}