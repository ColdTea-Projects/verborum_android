package de.coldtea.verborum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import de.coldtea.verborum.app.navigation.NavigationCentral
import de.coldtea.verborum.core.theme.VerborumTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VerborumTheme {
                NavigationCentral()
            }
        }
    }
}