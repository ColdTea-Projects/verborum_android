package de.coldtea.verborum.bibliotheca.dictionary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.coldtea.verborum.bibliotheca.common.utils.convertToLocalDateTimeStamp
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis

@Composable
fun DictionariesListScreen(){
    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column {
            val now = getNowInMillis()
            Text(text = now.toString())
            Text(text = now.convertToLocalDateTimeStamp())
        }
    }
}

@Preview
@Composable
fun DictionariesListScreenPreview() {
    DictionariesListScreen()
}