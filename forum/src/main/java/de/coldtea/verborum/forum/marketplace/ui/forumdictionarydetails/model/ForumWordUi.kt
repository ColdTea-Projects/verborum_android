package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model

/**
 * A read-only word of a marketplace dictionary, ready to render ("buy/purchase"). No practice
 * level: the owner's progress is not the viewer's (guide §5.5).
 */
data class ForumWordUi(
    val wordId: String,
    val word: String,
    val translation: String,
)
