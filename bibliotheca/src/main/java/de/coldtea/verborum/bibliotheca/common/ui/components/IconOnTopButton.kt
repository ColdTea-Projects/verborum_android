package de.coldtea.verborum.bibliotheca.common.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.coldtea.verborum.bibliotheca.common.utils.ResDrawables
import de.coldtea.verborum.core.theme.VerborumColors
import de.coldtea.verborum.core.theme.VerborumTheme

@Composable
fun IconOnTopButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    backgroundColor: Color,
    text: String? = null,
    initialPadding: Dp = 24.dp,
    iconSize: Dp = 32.dp,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable {
                isPressed = !isPressed
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(initialPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            text?.let{
                Text(
                    text = it,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun PreviewPracticeModeButton() {
    VerborumTheme {
        IconOnTopButton(
            text = "Self Practice",
            iconRes = ResDrawables.ic_chevron_right_24,
            backgroundColor = VerborumColors.LightAccent
        ) { }
    }
}