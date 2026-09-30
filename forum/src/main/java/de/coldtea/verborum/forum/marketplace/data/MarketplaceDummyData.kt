package de.coldtea.verborum.forum.marketplace.data

import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceListingResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceWordResponse

/**
 * Canned marketplace content served by [MarketplaceRepository] until ms_marketplace is wired in.
 * Shaped exactly like the wire payloads (canonical UUIDs, uppercase language codes, ISO-8601
 * timestamps, JSON-array surfaces) so swapping in the real API changes no layer above the
 * repository. Delete this file once the repository calls the API.
 */
internal object MarketplaceDummyData {

    private const val EVERYDAY_GERMAN = "0e7c1a52-3f1b-4c2a-9d4e-1a2b3c4d5e01"
    private const val JAPANESE_TRAVEL = "0e7c1a52-3f1b-4c2a-9d4e-1a2b3c4d5e02"
    private const val TURKISH_KITCHEN = "0e7c1a52-3f1b-4c2a-9d4e-1a2b3c4d5e03"
    private const val FRENCH_VERBS = "0e7c1a52-3f1b-4c2a-9d4e-1a2b3c4d5e04"
    private const val ITALIAN_CAFE = "0e7c1a52-3f1b-4c2a-9d4e-1a2b3c4d5e05"
    private const val SPANISH_BUSINESS = "0e7c1a52-3f1b-4c2a-9d4e-1a2b3c4d5e06"
    private const val POLISH_UKRAINIAN = "0e7c1a52-3f1b-4c2a-9d4e-1a2b3c4d5e07"

    // Declared before [listings]: object properties initialise in order, and each listing reads
    // its word count from here.
    private val wordsByDictionary: Map<String, List<Pair<String, String>>> = mapOf(
        // Two long lists (this and ITALIAN_CAFE, 50 words each) to exercise scrolling.
        EVERYDAY_GERMAN to listOf(
            "house" to "das Haus", "to buy" to "kaufen/erwerben", "bread" to "das Brot",
            "train station" to "der Bahnhof", "tomorrow" to "morgen", "friend" to "der Freund",
            "water" to "das Wasser", "apple" to "der Apfel", "car" to "das Auto",
            "street" to "die Straße", "city" to "die Stadt", "school" to "die Schule",
            "book" to "das Buch", "table" to "der Tisch", "chair" to "der Stuhl",
            "window" to "das Fenster", "door" to "die Tür", "kitchen" to "die Küche",
            "to eat" to "essen", "to drink" to "trinken", "to sleep" to "schlafen",
            "to work" to "arbeiten", "to read" to "lesen", "to write" to "schreiben",
            "to speak/to talk" to "sprechen/reden", "to go" to "gehen", "to come" to "kommen",
            "today" to "heute", "yesterday" to "gestern", "morning" to "der Morgen",
            "evening" to "der Abend", "night" to "die Nacht", "week" to "die Woche",
            "money" to "das Geld", "shop" to "das Geschäft/der Laden", "doctor" to "der Arzt",
            "family" to "die Familie", "child" to "das Kind", "mother" to "die Mutter",
            "father" to "der Vater", "weather" to "das Wetter", "rain" to "der Regen",
            "sun" to "die Sonne", "big" to "groß", "small" to "klein",
            "good" to "gut", "beautiful" to "schön", "expensive" to "teuer",
            "cheap" to "billig/günstig", "thank you" to "danke",
        ),
        JAPANESE_TRAVEL to listOf(
            "hello" to "こんにちは", "thank you" to "ありがとう", "station" to "駅",
            "ticket" to "切符", "hotel" to "ホテル", "excuse me" to "すみません",
        ),
        TURKISH_KITCHEN to listOf(
            "ekmek" to "bread", "peynir" to "cheese", "çay" to "tea",
            "domates" to "tomato", "kaşık" to "spoon", "tencere" to "pot",
        ),
        FRENCH_VERBS to listOf(
            "to be" to "être", "to have" to "avoir", "to go" to "aller",
            "to do/to make" to "faire", "to say" to "dire", "to see" to "voir",
        ),
        ITALIAN_CAFE to listOf(
            "coffee" to "il caffè", "milk" to "il latte", "sugar" to "lo zucchero",
            "the bill" to "il conto", "a table" to "un tavolo", "please" to "per favore",
            "thank you" to "grazie", "good morning" to "buongiorno", "good evening" to "buonasera",
            "espresso" to "l'espresso", "cappuccino" to "il cappuccino", "tea" to "il tè",
            "hot chocolate" to "la cioccolata calda", "juice" to "il succo", "water" to "l'acqua",
            "sparkling water" to "l'acqua frizzante", "still water" to "l'acqua naturale",
            "ice" to "il ghiaccio", "cup" to "la tazza", "glass" to "il bicchiere",
            "spoon" to "il cucchiaino", "napkin" to "il tovagliolo", "croissant" to "il cornetto",
            "sandwich" to "il panino/il tramezzino", "cake" to "la torta", "biscuit" to "il biscotto",
            "breakfast" to "la colazione", "lunch" to "il pranzo", "waiter" to "il cameriere",
            "counter" to "il bancone", "outside" to "fuori", "inside" to "dentro",
            "to order" to "ordinare", "to pay" to "pagare", "to sit" to "sedersi",
            "to wait" to "aspettare", "to take away" to "portare via", "receipt" to "lo scontrino",
            "cash" to "i contanti", "card" to "la carta", "change" to "il resto",
            "small" to "piccolo", "large" to "grande", "hot" to "caldo",
            "cold" to "freddo", "sweet" to "dolce", "bitter" to "amaro",
            "delicious" to "buonissimo/delizioso", "excuse me" to "scusi", "how much" to "quanto costa",
        ),
        SPANISH_BUSINESS to listOf(
            "meeting" to "la reunión", "invoice" to "la factura", "contract" to "el contrato",
            "deadline" to "la fecha límite", "customer" to "el cliente",
        ),
        POLISH_UKRAINIAN to listOf(
            "dom" to "дім", "woda" to "вода", "chleb" to "хліб",
            "dziękuję" to "дякую", "szkoła" to "школа",
        ),
    )

    /** Newest first, as `GET /marketplace/dictionaries` orders them. */
    val listings: List<MarketplaceListingResponse> = listOf(
        listing(ITALIAN_CAFE, "b1d2e3f4-0000-4000-8000-000000000005", "Sofia Rossi",
            "Italian Café Talk", "EN", "IT", 2051, "2026-09-26T08:12:40.118204Z", 4.8f,
            // More than four tags, to exercise the list card's chip limit.
            listOf("basic", "a1", "food_drink", "food_service", "travel", "culture_holidays", "cils")),
        listing(EVERYDAY_GERMAN, "b1d2e3f4-0000-4000-8000-000000000001", "Anna Schmidt",
            "Everyday German", "EN", "DE", 1284, "2026-09-24T17:01:21.303971Z", 4.7f,
            listOf("a1", "daily_routine", "shopping", "family")),
        listing(JAPANESE_TRAVEL, "b1d2e3f4-0000-4000-8000-000000000002", "Kenji Watanabe",
            "Japanese Travel Phrases", "EN", "JA", 842, "2026-09-20T11:45:03.552100Z", 4.5f,
            listOf("n5", "travel", "transport")),
        listing(TURKISH_KITCHEN, "b1d2e3f4-0000-4000-8000-000000000003", "Elif Yılmaz",
            "Turkish Kitchen", "TR", "EN", 317, "2026-09-12T19:30:00.000000Z", 4.2f,
            listOf("food_drink", "home_appliances")),
        listing(POLISH_UKRAINIAN, "b1d2e3f4-0000-4000-8000-000000000007", "Marta Kowalska",
            "Polish → Ukrainian", "PL", "UK", 7, "2026-09-01T09:00:12.000000Z", 4.1f,
            // No tags: the card shows no chip row.
            emptyList()),
        listing(FRENCH_VERBS, "b1d2e3f4-0000-4000-8000-000000000004", "Lucas Martin",
            "French Verbs A1", "EN", "FR", 96, "2026-08-18T14:22:51.020000Z", 3.9f,
            listOf("a1", "education", "delf_dalf")),
        listing(SPANISH_BUSINESS, "b1d2e3f4-0000-4000-8000-000000000006", "Daniel Kim",
            "Spanish Business Basics", "EN", "ES", 58, "2026-07-30T07:05:44.700000Z", 3.6f,
            listOf("intermediate", "business", "work_office")),
    )

    fun wordsFor(dictionaryId: String): List<MarketplaceWordResponse> =
        wordsByDictionary[dictionaryId].orEmpty().mapIndexed { index, (word, translation) ->
            MarketplaceWordResponse(
                wordId = "%s%02d".format(dictionaryId.dropLast(2), index),
                dictionaryId = dictionaryId,
                // Canonical surfaces column: a JSON array, "/" separating alternatives here.
                word = word.toSurfacesJson(),
                translation = translation.toSurfacesJson(),
            )
        }

    private fun listing(
        dictionaryId: String,
        publisherId: String,
        publisherName: String,
        name: String,
        fromLang: String,
        toLang: String,
        importCount: Int,
        publishedAt: String,
        rating: Float,
        tags: List<String>,
    ) = MarketplaceListingResponse(
        dictionaryId = dictionaryId,
        publisherId = publisherId,
        name = name,
        fromLang = fromLang,
        toLang = toLang,
        importCount = importCount,
        publishedAt = publishedAt,
        publisherName = publisherName,
        rating = rating,
        wordCount = wordsByDictionary[dictionaryId]?.size,
        tags = tags,
    )

    private fun String.toSurfacesJson(): String =
        split("/").joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }
}
