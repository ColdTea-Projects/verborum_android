package de.coldtea.verborum.bibliotheca.word.ui.createword.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.Gender
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.GenderLabel
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.LanguageGrammar
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun GenderChips(
    modifier: Modifier = Modifier,
    languageCode: String,
    options: List<Gender>,
    selected: Gender?,
    onSelected: (Gender) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { gender ->
            val isSelected = gender == selected
            val shape = RoundedCornerShape(20.dp)

            Surface(
                shape = shape,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .clickable { onSelected(gender) }
            ) {
                Text(
                    text = genderLabelText(languageCode, gender),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun genderLabelText(languageCode: String, gender: Gender): String =
    when (val label = LanguageGrammar.genderLabel(languageCode, gender)) {
        is GenderLabel.Article -> label.text
        is GenderLabel.Localized -> stringResource(label.res)
    }

@Preview
@Composable
fun GenderChipsPreview() {
    VerborumTheme {
        GenderChips(
            languageCode = "de",
            options = listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
            selected = Gender.MASCULINE,
            onSelected = {},
        )
    }
}
