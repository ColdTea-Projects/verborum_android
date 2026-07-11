package de.coldtea.verborum.bibliotheca.onboarding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.onboarding.ui.composables.IntroVisual
import de.coldtea.verborum.bibliotheca.onboarding.ui.composables.LibraryVisual
import de.coldtea.verborum.bibliotheca.onboarding.ui.composables.PracticeVisual
import de.coldtea.verborum.bibliotheca.onboarding.ui.composables.TestVisual
import de.coldtea.verborum.core.theme.VerborumTheme

private const val WELCOME_PAGE_COUNT = 4
private const val LAST_PAGE = WELCOME_PAGE_COUNT - 1

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = hiltViewModel(),
    onDone: () -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { WELCOME_PAGE_COUNT })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            WelcomePage(
                page = page,
                onDoneClick = {
                    viewModel.completeOnboarding()
                    onDone()
                },
            )
        }

        DotsIndicator(
            pageCount = WELCOME_PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 24.dp)
        )
    }
}

@Composable
private fun WelcomePage(
    page: Int,
    onDoneClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        when (page) {
            0 -> IntroVisual()
            1 -> LibraryVisual()
            2 -> TestVisual()
            else -> PracticeVisual()
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(
                when (page) {
                    0 -> ResStrings.welcomeScreenTitleIntro
                    1 -> ResStrings.welcomeScreenTitleLibrary
                    2 -> ResStrings.welcomeScreenTitleTest
                    else -> ResStrings.welcomeScreenTitlePractice
                }
            ),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(
                when (page) {
                    0 -> ResStrings.welcomeScreenDescriptionIntro
                    1 -> ResStrings.welcomeScreenDescriptionLibrary
                    2 -> ResStrings.welcomeScreenDescriptionTest
                    else -> ResStrings.welcomeScreenDescriptionPractice
                }
            ),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (page == LAST_PAGE) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDoneClick,
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
                    text = stringResource(ResStrings.welcomeScreenDone),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        repeat(pageCount) { page ->
            val isCurrent = page == currentPage

            val width by animateDpAsState(targetValue = if (isCurrent) 24.dp else 8.dp)
            val color by animateColorAsState(
                targetValue = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline
            )

            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    VerborumTheme {
        WelcomeScreen()
    }
}
