package de.coldtea.verborum.bibliotheca.dictionary.ui.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.coldtea.verborum.core.theme.VerborumTheme

/**
 * Placeholder shaped like [DictionaryCard], shown while the list is loading so the screen has a
 * stable layout instead of popping in empty and then abruptly filling with content.
 */
@Composable
fun DictionaryCardSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Book icon placeholder
            SkeletonBlock(
                color = shimmerColor,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonBlock(color = shimmerColor, modifier = Modifier.height(20.dp).width(140.dp))
                SkeletonBlock(color = shimmerColor, modifier = Modifier.height(14.dp).width(90.dp))
                SkeletonBlock(color = shimmerColor, modifier = Modifier.height(14.dp).width(110.dp))
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(6.dp),
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(color)
    ) {}
}

@PreviewLightDark
@Composable
private fun DictionaryCardSkeletonPreview() {
    VerborumTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DictionaryCardSkeleton()
            DictionaryCardSkeleton()
            DictionaryCardSkeleton()
        }
    }
}
