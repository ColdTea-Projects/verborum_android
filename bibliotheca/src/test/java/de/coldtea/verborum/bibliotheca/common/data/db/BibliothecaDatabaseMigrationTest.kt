package de.coldtea.verborum.bibliotheca.common.data.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Migration test for [BibliothecaDatabase]. Seeds a real on-disk database at the previous schema
 * version, then opens it through the production [BibliothecaDatabase.getInstance] (which carries the
 * migrations) so the actual migration runs — asserting existing rows survive and the new column
 * appears with its default. `exportSchema = false`, so we build the old schema by hand rather than
 * via MigrationTestHelper; v2 is v3 minus the `tags` column that MIGRATION_2_3 adds.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BibliothecaDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        resetSingleton()
        context.deleteDatabase(DB_NAME)
    }

    @After
    fun tearDown() {
        resetSingleton()
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun `MIGRATION_2_3 adds the tags column with an empty default and preserves rows`() = runTest {
        seedVersion2Database()

        // Opening through getInstance runs MIGRATION_2_3 (schema version is now 3).
        val db = BibliothecaDatabase.getInstance(context)
        try {
            val dictionary = db.daoDictionary.getDictionary("d1")
            assertEquals("German Basics", dictionary.name)
            assertEquals("guest", dictionary.userId)
            // The new column exists and defaults to the empty-tags JSON.
            assertEquals("[]", dictionary.tags)
            assertEquals(emptyList<String>(), dictionary.convertToDictionary().tags)

            // Words (untouched by this migration) survive too.
            val words = db.daoWord.getWordsByDictionary("d1")
            assertEquals(1, words.size)
            assertEquals("w1", words.single().wordId)
        } finally {
            db.close()
        }
    }

    /** Creates the on-disk DB at schema version 2 (no `tags` column) with one dictionary + word. */
    private fun seedVersion2Database() {
        val callback = object : SupportSQLiteOpenHelper.Callback(SCHEMA_VERSION_2) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `dictionary` (" +
                        "`dictionary_id` TEXT NOT NULL, `fk_user_id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, `is_public` INTEGER NOT NULL, " +
                        "`isSynced` INTEGER NOT NULL, `is_deleted` INTEGER NOT NULL, " +
                        "`from_lang` TEXT NOT NULL, `to_lang` TEXT NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`dictionary_id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `word` (" +
                        "`word_id` TEXT NOT NULL, `fk_dictionary_id` TEXT NOT NULL, " +
                        "`word` TEXT NOT NULL, `word_meta` TEXT NOT NULL, " +
                        "`translation` TEXT NOT NULL, `translation_meta` TEXT NOT NULL, " +
                        "`isSynced` INTEGER NOT NULL, `is_deleted` INTEGER NOT NULL, " +
                        "`level` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL, PRIMARY KEY(`word_id`))"
                )
                db.execSQL(
                    "INSERT INTO `dictionary` VALUES " +
                        "('d1', 'guest', 'German Basics', 0, 1, 0, 'en', 'de', 100, 200)"
                )
                db.execSQL(
                    "INSERT INTO `word` VALUES " +
                        "('w1', 'd1', 'apple', '{}', 'Apfel', '{}', 1, 0, 0, 100, 200)"
                )
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }

        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(DB_NAME)
            .callback(callback)
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        // Touching the writable database triggers onCreate at version 2, then we release it.
        helper.writableDatabase
        helper.close()
    }

    /** Clears the cached singleton so each test opens a freshly-seeded file. */
    private fun resetSingleton() {
        BibliothecaDatabase::class.java.getDeclaredField("INSTANCE").apply {
            isAccessible = true
            set(null, null)
        }
    }

    private companion object {
        const val DB_NAME = "db_verborum_bibliotheca"
        const val SCHEMA_VERSION_2 = 2
    }
}
