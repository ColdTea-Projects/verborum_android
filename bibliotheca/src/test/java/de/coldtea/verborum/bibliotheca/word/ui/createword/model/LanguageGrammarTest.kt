package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.core.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageGrammarTest : BaseTest() {

    // region genderOptions

    @Test
    fun `German nouns offer three genders`() {
        assertEquals(
            listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
            LanguageGrammar.genderOptions("de"),
        )
    }

    @Test
    fun `Dutch nouns offer common and neuter`() {
        assertEquals(listOf(Gender.COMMON, Gender.NEUTER), LanguageGrammar.genderOptions("nl"))
    }

    @Test
    fun `Lithuanian nouns have gender even without articles`() {
        assertEquals(listOf(Gender.MASCULINE, Gender.FEMININE), LanguageGrammar.genderOptions("lt"))
    }

    @Test
    fun `Turkish nouns have no gender`() {
        assertTrue(LanguageGrammar.genderOptions("tr").isEmpty())
    }

    @Test
    fun `genderOptions ignores casing`() {
        assertEquals(LanguageGrammar.genderOptions("de"), LanguageGrammar.genderOptions("DE"))
    }

    // endregion

    // region formSpec — nouns

    @Test
    fun `German noun spec has gender chips and a plural field`() {
        val spec = LanguageGrammar.formSpec("de", WordType.NOUN)

        assertEquals(listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER), spec.genderOptions)
        assertEquals(listOf(FieldKey.PLURAL), spec.fields.map { it.key })
    }

    @Test
    fun `Turkish noun spec is empty — regular plural, no gender`() {
        assertTrue(LanguageGrammar.formSpec("tr", WordType.NOUN).isEmpty)
    }

    @Test
    fun `Lithuanian noun spec has gender and plural`() {
        val spec = LanguageGrammar.formSpec("lt", WordType.NOUN)

        assertEquals(listOf(Gender.MASCULINE, Gender.FEMININE), spec.genderOptions)
        assertEquals(listOf(FieldKey.PLURAL), spec.fields.map { it.key })
    }

    // endregion

    // region formSpec — verbs

    @Test
    fun `German verb spec captures past participle and auxiliary`() {
        val spec = LanguageGrammar.formSpec("de", WordType.VERB)

        assertEquals(
            listOf(FieldKey.PAST, FieldKey.PARTICIPLE, FieldKey.AUXILIARY),
            spec.fields.map { it.key },
        )
        val auxiliary = spec.fields.last() as FormField.ChoiceForm
        assertEquals(listOf("haben", "sein"), auxiliary.options)
    }

    @Test
    fun `French verb spec captures participle and etre or avoir`() {
        val spec = LanguageGrammar.formSpec("fr", WordType.VERB)

        assertEquals(listOf(FieldKey.PARTICIPLE, FieldKey.AUXILIARY), spec.fields.map { it.key })
        val auxiliary = spec.fields.last() as FormField.ChoiceForm
        assertEquals(listOf("avoir", "être"), auxiliary.options)
    }

    @Test
    fun `Lithuanian verb spec captures the two extra principal parts`() {
        val spec = LanguageGrammar.formSpec("lt", WordType.VERB)

        assertEquals(listOf(FieldKey.PRESENT_3RD, FieldKey.PAST_3RD), spec.fields.map { it.key })
    }

    @Test
    fun `Turkish verb spec is empty`() {
        assertTrue(LanguageGrammar.formSpec("tr", WordType.VERB).isEmpty)
    }

    // endregion

    // region formSpec — adjectives

    @Test
    fun `Romance and Lithuanian adjectives expose feminine then comparison fields`() {
        listOf("fr", "es", "it", "pt", "lt").forEach { code ->
            assertEquals(
                "expected feminine + comparison fields for $code",
                listOf(FieldKey.FEMININE, FieldKey.COMPARATIVE, FieldKey.SUPERLATIVE),
                LanguageGrammar.formSpec(code, WordType.ADJECTIVE).fields.map { it.key },
            )
        }
    }

    @Test
    fun `German and English adjectives expose comparison fields but no feminine`() {
        listOf("de", "en", "nl").forEach { code ->
            assertEquals(
                "expected comparison fields for $code",
                listOf(FieldKey.COMPARATIVE, FieldKey.SUPERLATIVE),
                LanguageGrammar.formSpec(code, WordType.ADJECTIVE).fields.map { it.key },
            )
        }
    }

    @Test
    fun `Turkish and Azerbaijani adjectives have no fields — comparison is periphrastic`() {
        assertTrue(LanguageGrammar.formSpec("tr", WordType.ADJECTIVE).isEmpty)
        assertTrue(LanguageGrammar.formSpec("az", WordType.ADJECTIVE).isEmpty)
    }

    // endregion

    // region formSpec — adverbs

    @Test
    fun `adverbs capture only the word itself — comparison lives on the adjective card`() {
        listOf("de", "en", "nl", "lt", "fr", "tr").forEach { code ->
            assertTrue(
                "expected empty adverb spec for $code",
                LanguageGrammar.formSpec(code, WordType.ADVERB).isEmpty,
            )
        }
    }

    @Test
    fun `English comparison is hinted as only-if-irregular while German is not`() {
        val enComparative = LanguageGrammar.formSpec("en", WordType.ADJECTIVE).fields
            .filterIsInstance<FormField.TextForm>()
            .first { it.key == FieldKey.COMPARATIVE }
        val deComparative = LanguageGrammar.formSpec("de", WordType.ADJECTIVE).fields
            .filterIsInstance<FormField.TextForm>()
            .first { it.key == FieldKey.COMPARATIVE }

        assertTrue(enComparative.hintRes != null)
        assertNull(deComparative.hintRes)
    }

    // endregion

    // region formSpec — other closed-class types

    @Test
    fun `other sub-types capture only the word itself`() {
        listOf(
            WordType.FREE_TEXT,
            WordType.PREPOSITION,
            WordType.PRONOUN,
            WordType.NUMERAL,
            WordType.CONJUNCTION,
            WordType.INTERJECTION,
            WordType.ARTICLE,
        ).forEach { type ->
            assertTrue("$type should have an empty spec", LanguageGrammar.formSpec("de", type).isEmpty)
        }
    }

    // endregion

    // region gender labels

    @Test
    fun `German gender labels are the definite articles`() {
        assertEquals(GenderLabel.Article("der"), LanguageGrammar.genderLabel("de", Gender.MASCULINE))
        assertEquals(GenderLabel.Article("die"), LanguageGrammar.genderLabel("de", Gender.FEMININE))
        assertEquals(GenderLabel.Article("das"), LanguageGrammar.genderLabel("de", Gender.NEUTER))
    }

    @Test
    fun `Lithuanian gender labels are localized because it has no articles`() {
        assertTrue(LanguageGrammar.genderLabel("lt", Gender.MASCULINE) is GenderLabel.Localized)
        assertTrue(LanguageGrammar.genderLabel("lt", Gender.FEMININE) is GenderLabel.Localized)
    }

    // endregion

    // region composeSurface — article & elision

    @Test
    fun `German prepends the article with a space`() {
        assertEquals("der Apfel", LanguageGrammar.composeSurface("de", Gender.MASCULINE, "Apfel"))
    }

    @Test
    fun `French elides before a vowel and spaces otherwise`() {
        assertEquals("l'eau", LanguageGrammar.composeSurface("fr", Gender.FEMININE, "eau"))
        assertEquals("le chat", LanguageGrammar.composeSurface("fr", Gender.MASCULINE, "chat"))
    }

    @Test
    fun `Italian picks lo before s-plus-consonant, l' before a vowel, il otherwise`() {
        assertEquals("lo studente", LanguageGrammar.composeSurface("it", Gender.MASCULINE, "studente"))
        assertEquals("l'amico", LanguageGrammar.composeSurface("it", Gender.MASCULINE, "amico"))
        assertEquals("il cane", LanguageGrammar.composeSurface("it", Gender.MASCULINE, "cane"))
        assertEquals("la casa", LanguageGrammar.composeSurface("it", Gender.FEMININE, "casa"))
        assertEquals("l'isola", LanguageGrammar.composeSurface("it", Gender.FEMININE, "isola"))
    }

    @Test
    fun `composeSurface returns the bare word when there is no gender`() {
        assertEquals("Apfel", LanguageGrammar.composeSurface("de", null, "Apfel"))
    }

    @Test
    fun `composeSurface returns the bare word for article-less languages`() {
        assertEquals("knyga", LanguageGrammar.composeSurface("lt", Gender.FEMININE, "knyga"))
        assertEquals("elma", LanguageGrammar.composeSurface("tr", Gender.MASCULINE, "elma"))
    }

    @Test
    fun `composeSurface trims and tolerates empty input`() {
        assertEquals("der Apfel", LanguageGrammar.composeSurface("de", Gender.MASCULINE, "  Apfel  "))
        assertEquals("", LanguageGrammar.composeSurface("de", Gender.MASCULINE, "   "))
    }

    // endregion

    // region extractBaseWord — inverse of composeSurface

    @Test
    fun `extractBaseWord strips the space-separated article`() {
        assertEquals("Apfel", LanguageGrammar.extractBaseWord("de", "der Apfel"))
        assertEquals("Haus", LanguageGrammar.extractBaseWord("de", "das Haus"))
        assertEquals("casa", LanguageGrammar.extractBaseWord("es", "la casa"))
        assertEquals("deur", LanguageGrammar.extractBaseWord("nl", "de deur"))
    }

    @Test
    fun `extractBaseWord strips the elided French and Italian article`() {
        assertEquals("eau", LanguageGrammar.extractBaseWord("fr", "l'eau"))
        assertEquals("amico", LanguageGrammar.extractBaseWord("it", "l'amico"))
    }

    @Test
    fun `extractBaseWord strips the Italian lo article`() {
        assertEquals("studente", LanguageGrammar.extractBaseWord("it", "lo studente"))
    }

    @Test
    fun `extractBaseWord leaves article-less words untouched`() {
        assertEquals("elma", LanguageGrammar.extractBaseWord("tr", "elma"))
        assertEquals("obuolys", LanguageGrammar.extractBaseWord("lt", "obuolys"))
        assertEquals("go", LanguageGrammar.extractBaseWord("en", "go"))
    }

    // endregion

    // region noun hints

    @Test
    fun `English plural field is hinted as only-if-irregular`() {
        val plural = LanguageGrammar.formSpec("en", WordType.NOUN).fields
            .filterIsInstance<FormField.TextForm>()
            .first { it.key == FieldKey.PLURAL }

        assertTrue(plural.hintRes != null)
    }

    @Test
    fun `German plural field carries no irregular hint`() {
        val plural = LanguageGrammar.formSpec("de", WordType.NOUN).fields
            .filterIsInstance<FormField.TextForm>()
            .first { it.key == FieldKey.PLURAL }

        assertNull(plural.hintRes)
    }

    // endregion
}
