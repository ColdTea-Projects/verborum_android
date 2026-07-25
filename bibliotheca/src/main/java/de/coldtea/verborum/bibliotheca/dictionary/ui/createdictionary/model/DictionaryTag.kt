package de.coldtea.verborum.bibliotheca.dictionary.ui.createdictionary.model

import androidx.annotation.StringRes
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/**
 * A selectable dictionary tag.
 *
 * [code] is the stable identifier to persist and send — it never changes with the UI language. The
 * display label is one of two kinds:
 * - [labelRes]: a translatable string (proficiency levels Basic/Intermediate/Advanced and every
 *   topic) that follows the device language.
 * - [name]: a fixed proper-noun label shown as-is in every language (framework codes like A1 / N5 /
 *   HSK 3 and exam names like "DELE (es)").
 *
 * Exactly one of the two is set.
 */
data class DictionaryTag(
    val code: String,
    @StringRes val labelRes: Int? = null,
    val name: String? = null,
)

private fun translated(code: String, @StringRes labelRes: Int) =
    DictionaryTag(code = code, labelRes = labelRes)

private fun fixed(code: String, name: String) =
    DictionaryTag(code = code, name = name)

// --- Provisional tag taxonomy (UI; codes are the eventual storage keys) ---

val LEVEL_TAGS: List<DictionaryTag> = listOf(
    // General proficiency — translatable.
    translated("basic", ResStrings.tagLevelBasic),
    translated("intermediate", ResStrings.tagLevelIntermediate),
    translated("advanced", ResStrings.tagLevelAdvanced),
    // Framework codes — fixed names. Codes are lower-cased to match the backend's tag
    // normalisation (trimmed + lower-cased on write), so they round-trip unchanged.
    fixed("a1", "A1"), fixed("a2", "A2"), fixed("b1", "B1"),
    fixed("b2", "B2"), fixed("c1", "C1"), fixed("c2", "C2"),
    fixed("n5", "N5"), fixed("n4", "N4"), fixed("n3", "N3"),
    fixed("n2", "N2"), fixed("n1", "N1"),
    fixed("hsk1", "HSK 1"), fixed("hsk2", "HSK 2"), fixed("hsk3", "HSK 3"),
    fixed("hsk4", "HSK 4"), fixed("hsk5", "HSK 5"), fixed("hsk6", "HSK 6"),
    fixed("topik1", "TOPIK 1"), fixed("topik2", "TOPIK 2"), fixed("topik3", "TOPIK 3"),
    fixed("topik4", "TOPIK 4"), fixed("topik5", "TOPIK 5"), fixed("topik6", "TOPIK 6"),
)

val TOPIC_TAGS: List<DictionaryTag> = listOf(
    translated("food_drink", ResStrings.tagTopicFoodDrink),
    translated("home_appliances", ResStrings.tagTopicHomeAppliances),
    translated("clothing", ResStrings.tagTopicClothing),
    translated("family", ResStrings.tagTopicFamily),
    translated("daily_routine", ResStrings.tagTopicDailyRoutine),
    translated("shopping", ResStrings.tagTopicShopping),
    translated("money", ResStrings.tagTopicMoney),
    translated("travel", ResStrings.tagTopicTravel),
    translated("transport", ResStrings.tagTopicTransport),
    translated("cars_parts", ResStrings.tagTopicCarsParts),
    translated("directions", ResStrings.tagTopicDirections),
    translated("city", ResStrings.tagTopicCity),
    translated("nature_weather", ResStrings.tagTopicNatureWeather),
    translated("animals", ResStrings.tagTopicAnimals),
    translated("plants", ResStrings.tagTopicPlants),
    translated("body_health", ResStrings.tagTopicBodyHealth),
    translated("medicine", ResStrings.tagTopicMedicine),
    translated("emotions", ResStrings.tagTopicEmotions),
    translated("work_office", ResStrings.tagTopicWorkOffice),
    translated("business", ResStrings.tagTopicBusiness),
    translated("education", ResStrings.tagTopicEducation),
    translated("it_technology", ResStrings.tagTopicItTechnology),
    translated("law", ResStrings.tagTopicLaw),
    translated("science", ResStrings.tagTopicScience),
    translated("sports", ResStrings.tagTopicSports),
    translated("music", ResStrings.tagTopicMusic),
    translated("art_film", ResStrings.tagTopicArtFilm),
    translated("culture_holidays", ResStrings.tagTopicCultureHolidays),
    translated("news_politics", ResStrings.tagTopicNewsPolitics),
    translated("food_service", ResStrings.tagTopicFoodService),
)

val EXAM_TAGS: List<DictionaryTag> = listOf(
    fixed("goethe_testdaf", "Goethe/TestDaF (de)"),
    fixed("dele", "DELE (es)"),
    fixed("delf_dalf", "DELF/DALF (fr)"),
    fixed("cils", "CILS (it)"),
    fixed("ielts_cambridge", "IELTS/Cambridge (en)"),
    fixed("jlpt", "JLPT (ja)"),
    fixed("hsk", "HSK (zh)"),
    fixed("topik", "TOPIK (ko)"),
    fixed("torfl", "TORFL (ru)"),
)

/** Every known tag, and a code → tag lookup for resolving stored codes back to display labels. */
val ALL_TAGS: List<DictionaryTag> = LEVEL_TAGS + TOPIC_TAGS + EXAM_TAGS

private val tagsByCode: Map<String, DictionaryTag> = ALL_TAGS.associateBy { it.code }

fun dictionaryTagByCode(code: String): DictionaryTag? = tagsByCode[code]
