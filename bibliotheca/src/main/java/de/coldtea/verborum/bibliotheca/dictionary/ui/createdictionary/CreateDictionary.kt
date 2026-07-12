package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.composables.LanguageDropdown
import de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model.CreateDictionaryState
import de.coldtea.verborum.core.theme.VerborumTheme
import de.coldtea.verborum.core.ui.RegisterTopBar

@Composable
fun CreateDictionaryScreen(
    viewModel: CreateDictionaryViewModel = hiltViewModel(),
    onDictionaryCreated: (String) -> Unit,
    onDictionaryUpdated: () -> Unit = {},
) {
    val createDictionaryState =
        viewModel.createDictionaryState.collectAsState(initial = CreateDictionaryState.Idle).value
    val editingDictionary = viewModel.editingDictionary.collectAsState().value
    val isEditing = editingDictionary != null

    LaunchedEffect(createDictionaryState) {
        when (createDictionaryState) {
            is CreateDictionaryState.Created -> onDictionaryCreated(createDictionaryState.dictionaryId)
            is CreateDictionaryState.Updated -> onDictionaryUpdated()
            else -> Unit
        }
    }

    var name by remember { mutableStateOf("") }
    var fromLanguage by remember { mutableStateOf<SupportedLanguage?>(null) }
    var toLanguage by remember { mutableStateOf<SupportedLanguage?>(null) }

    // Edit mode: prefill the form from the loaded dictionary once it arrives.
    LaunchedEffect(editingDictionary) {
        editingDictionary?.let { dictionary ->
            name = dictionary.name
            fromLanguage = SupportedLanguage.fromCode(dictionary.fromLang)
            toLanguage = SupportedLanguage.fromCode(dictionary.toLang)
        }
    }

    val isCreateEnabled = name.isNotBlank() &&
            fromLanguage != null &&
            toLanguage != null &&
            fromLanguage != toLanguage &&
            createDictionaryState !is CreateDictionaryState.Saving

    RegisterTopBar(
        title = stringResource(
            if (isEditing) ResStrings.createDictionaryScreenHeaderEdit
            else ResStrings.createDictionaryScreenHeader
        ),
        subtitle = stringResource(ResStrings.createDictionaryScreenSubtitle),
        showBackButton = true,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = stringResource(ResStrings.createDictionaryScreenNameLabel)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageDropdown(
            label = stringResource(ResStrings.createDictionaryScreenFromLanguage),
            selectedLanguage = fromLanguage,
            onLanguageSelected = { fromLanguage = it },
            // A dictionary's language pair is fixed once its words exist — locked in edit mode.
            enabled = !isEditing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageDropdown(
            label = stringResource(ResStrings.createDictionaryScreenToLanguage),
            selectedLanguage = toLanguage,
            onLanguageSelected = { toLanguage = it },
            enabled = !isEditing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val fromLang = fromLanguage ?: return@Button
                val toLang = toLanguage ?: return@Button
                viewModel.save(name, fromLang, toLang)
            },
            enabled = isCreateEnabled,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(
                    if (isEditing) ResStrings.createDictionaryScreenUpdate
                    else ResStrings.createDictionaryScreenCreate
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview
@Composable
fun CreateDictionaryScreenPreview() {
    VerborumTheme {
        CreateDictionaryScreen(
            onDictionaryCreated = {}
        )
    }
}
