package de.coldtea.verborum.bibliotheca.onboarding.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OnboardingRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean =
        preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted() {
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "verborum_onboarding"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
