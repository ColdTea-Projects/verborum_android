package de.coldtea.verborum.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.coldtea.verborum.app.R

sealed class ScreenGroups(val route: String, @StringRes val textResourceId: Int, @DrawableRes val iconResourceId: Int)

object GroupBibliotheca: ScreenGroups(GROUP_BIBLIOTHECA, R.string.group_bibliotheca_nav_title, R.drawable.baseline_book_24)
object GroupForum: ScreenGroups(GROUP_FORUM, R.string.group_forum_nav_title, R.drawable.baseline_account_balance_24)
object GroupOptions: ScreenGroups(GROUP_OPTIONS, R.string.group_options_nav_title, R.drawable.baseline_settings_24)

val screenGroups = listOf(
    GroupBibliotheca,
    GroupForum,
    GroupOptions
)

const val GROUP_BIBLIOTHECA = "groupBibliotheca"
const val SCREEN_WELCOME = "welcomeScreen"
const val SCREEN_DICTIONARIES_LIST = "dictionariesListScreen"
const val SCREEN_DICTIONARIES_DETAIL = "dictionariesDetailScreen"
const val SCREEN_CREATE_DICTIONARY = "createDictionaryScreen"
const val SCREEN_CREATE_WORD = "createWordScreen"
const val SCREEN_SELF_PRACTICE = "selfPracticeScreen"
const val SCREEN_MULTIPLE_CHOCIE = "multipleChoiceScreen"

const val GROUP_FORUM = "groupForum"
const val SCREEN_FORUM_MAIN_SCREEN = "forumMainScreen"

const val GROUP_OPTIONS = "groupOptions"
const val SCREEN_OPTIONS = "optionsScreen"

