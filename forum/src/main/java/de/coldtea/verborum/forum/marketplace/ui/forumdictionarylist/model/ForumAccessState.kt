package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

/**
 * Whether the user may enter the Forum. Only members — a display name plus accepted Forum terms —
 * can browse and import (`docs/marketplace-client-guide.md` §4.1, §6.3); everyone else is shown
 * the way to join from their profile.
 */
sealed class ForumAccessState {
    data object Loading : ForumAccessState()
    data object Member : ForumAccessState()
    data object NotMember : ForumAccessState()
    data object Failed : ForumAccessState()
}
