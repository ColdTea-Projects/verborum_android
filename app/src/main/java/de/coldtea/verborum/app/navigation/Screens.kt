package de.coldtea.verborum.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.coldtea.verborum.app.R

sealed class ScreenGroups(val route: String, @StringRes val textResourceId: Int, @DrawableRes val iconResourceId: Int)

object GroupBibliotheca: ScreenGroups(GROUP_BIBLIOTHECA, R.string.group_bibliotheca_nav_title, R.drawable.baseline_book_24)
object GroupForum: ScreenGroups(GROUP_FORUM, R.string.group_forum_nav_title, R.drawable.baseline_account_balance_24)

val screenGroups = listOf(
    GroupBibliotheca,
    GroupForum
)

const val GROUP_BIBLIOTHECA = "groupBibliotheca"
const val SCREEN_DICTIONARIES_LIST = "dictionariesListScreen"

const val GROUP_FORUM = "groupForum"
const val SCREEN_FORUM_MAIN_SCREEN = "forumMainScreen"

