package de.coldtea.verborum.bibliotheca.common.data.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.coldtea.verborum.bibliotheca.dictionary.data.db.dao.DaoDictionary
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.imported.data.db.dao.DaoImportedDictionary
import de.coldtea.verborum.bibliotheca.imported.data.db.dao.DaoImportedWord
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedDictionaryEntity
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedWordEntity
import de.coldtea.verborum.bibliotheca.word.data.db.dao.DaoWord
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity

@SuppressLint("RestrictedApi")
@Database(
    entities = [
        WordEntity::class,
        DictionaryEntity::class,
        ImportedDictionaryEntity::class,
        ImportedWordEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class BibliothecaDatabase : RoomDatabase() {

    abstract val daoDictionary: DaoDictionary
    abstract val daoWord: DaoWord
    abstract val daoImportedDictionary: DaoImportedDictionary
    abstract val daoImportedWord: DaoImportedWord

    companion object {
        @Volatile
        private var INSTANCE: BibliothecaDatabase? = null

        /** v2: deletion tombstones — rows are flagged is_deleted until the server confirms. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dictionary ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE word ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v3: dictionary tags — a JSON array of tag codes; existing rows default to empty. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dictionary ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
            }
        }

        /**
         * v4: dictionaries downloaded from the Forum — two new tables, separate from the user's own
         * so the sync engine never sees them. Nothing existing changes, so no data is touched.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `imported_dictionary` (" +
                        "`dictionary_id` TEXT NOT NULL, `publisher_id` TEXT NOT NULL, " +
                        "`publisher_name` TEXT, `name` TEXT NOT NULL, `from_lang` TEXT NOT NULL, " +
                        "`to_lang` TEXT NOT NULL, `tags` TEXT NOT NULL, `imported_at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`dictionary_id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `imported_word` (" +
                        "`word_id` TEXT NOT NULL, `fk_dictionary_id` TEXT NOT NULL, `word` TEXT NOT NULL, " +
                        "`word_meta` TEXT NOT NULL, `translation` TEXT NOT NULL, " +
                        "`translation_meta` TEXT NOT NULL, `level` INTEGER NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`word_id`))"
                )
            }
        }

        internal fun getInstance(context: Context): BibliothecaDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BibliothecaDatabase::class.java,
                        "db_verborum_bibliotheca"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}