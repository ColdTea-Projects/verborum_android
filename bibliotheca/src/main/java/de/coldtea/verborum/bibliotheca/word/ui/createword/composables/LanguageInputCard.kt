package de.coldtea.verborum.bibliotheca.word.ui.createword.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordFormInput
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun LanguageInputCard(
    modifier: Modifier = Modifier,
    languageName: String,
    barColor: Color,
    input: WordFormInput,
    articleOptions: List<String>,
    showPlural: Boolean,
    showFeminine: Boolean,
    onInputChange: (WordFormInput) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Accent bar
            Spacer(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
            ) {
                Text(
                    text = languageName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = input.text,
                    onValueChange = { onInputChange(input.copy(text = it)) },
                    label = { Text(text = stringResource(ResStrings.createWordScreenTextLabel)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (articleOptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    ArticleDropdown(
                        options = articleOptions,
                        selectedArticle = input.article,
                        onArticleSelected = { onInputChange(input.copy(article = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showPlural) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = input.plural,
                        onValueChange = { onInputChange(input.copy(plural = it)) },
                        label = { Text(text = stringResource(ResStrings.createWordScreenPluralLabel)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showFeminine) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = input.feminine,
                        onValueChange = { onInputChange(input.copy(feminine = it)) },
                        label = { Text(text = stringResource(ResStrings.createWordScreenFeminineLabel)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LanguageInputCardPreview() {
    VerborumTheme {
        LanguageInputCard(
            languageName = "German",
            barColor = Color(0xFFC41E3A),
            input = WordFormInput(text = "Apfel", article = "der", plural = "Äpfel"),
            articleOptions = listOf("der", "die", "das"),
            showPlural = true,
            showFeminine = false,
            onInputChange = {},
        )
    }
}
