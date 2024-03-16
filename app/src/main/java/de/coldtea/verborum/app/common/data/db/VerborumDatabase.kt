package de.coldtea.verborum.app.common.data.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.coldtea.verborum.app.dictionary.data.db.dao.DaoDictionary
import de.coldtea.verborum.app.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.app.word.data.db.dao.DaoWord
import de.coldtea.verborum.app.word.data.db.entity.WordEntity

@SuppressLint("RestrictedApi")
@Database(
    entities = [
        WordEntity::class,
        DictionaryEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class VerborumDatabase : RoomDatabase() {

    abstract val daoDictionary: DaoDictionary
    abstract val daoWord: DaoWord

    companion object {
        @Volatile
        private var INSTANCE: VerborumDatabase? = null
        internal fun getInstance(context: Context): VerborumDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        VerborumDatabase::class.java,
                        "db_verborum_app"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}