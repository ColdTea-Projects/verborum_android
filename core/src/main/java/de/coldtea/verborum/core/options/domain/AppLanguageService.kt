package de.coldtea.verborum.core.options.domain

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import de.coldtea.verborum.core.options.ui.model.AppLanguage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The app's UI language, as an override of the device language (per-app locales).
 *
 * The override lives in the platform, not in our own storage: on API 33+ [AppCompatDelegate] hands
 * it to the system `LocaleManager`, which persists it, shows it in Settings → Apps → Language, and
 * re-applies it at every launch; below 33 AppCompat stores and re-applies it itself (which is why
 * the launcher activity is an `AppCompatActivity` and the manifest opts into `autoStoreLocales`).
 * Keeping a second copy in prefs would only give the two a chance to disagree.
 *
 * Selecting a language recreates the running activities — that is AppCompat's doing, and it is how
 * the change reaches every already-composed screen.
 */
@Singleton
class AppLanguageService @Inject constructor() {

    /** The current override, or null when the app follows the device language. */
    fun selected(): AppLanguage? =
        AppLanguage.fromTag(AppCompatDelegate.getApplicationLocales().toLanguageTags())

    /** Applies [language], or clears the override and returns to the device language when null. */
    fun select(language: AppLanguage?) {
        val locales = language
            ?.let { LocaleListCompat.forLanguageTags(it.code) }
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
