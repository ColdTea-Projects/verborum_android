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
import de.coldtea.verborum.bibliotheca.word.data.db.dao.DaoWord
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity

@SuppressLint("RestrictedApi")
@Database(
    entities = [
        WordEntity::class,
        DictionaryEntity::class,
    ],
    version = 3,
    exportSchema = false
)
abstract class BibliothecaDatabase : RoomDatabase() {

    abstract val daoDictionary: DaoDictionary
    abstract val daoWord: DaoWord

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

        internal fun getInstance(context: Context): BibliothecaDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BibliothecaDatabase::class.java,
                        "db_verborum_bibliotheca"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}