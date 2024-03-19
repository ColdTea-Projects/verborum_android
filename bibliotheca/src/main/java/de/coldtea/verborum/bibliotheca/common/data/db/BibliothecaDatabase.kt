package de.coldtea.verborum.bibliotheca.common.data.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false
)
abstract class BibliothecaDatabase : RoomDatabase() {

    abstract val daoDictionary: DaoDictionary
    abstract val daoWord: DaoWord

    companion object {
        @Volatile
        private var INSTANCE: BibliothecaDatabase? = null
        internal fun getInstance(context: Context): BibliothecaDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BibliothecaDatabase::class.java,
                        "db_verborum_bibliotheca"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}