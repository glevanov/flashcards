package io.levanov.flashcards.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// exportSchema=false: personal app, no schema diffing tooling.
@Database(entities = [CardStateEntity::class], version = 1, exportSchema = false)
abstract class FlashcardsDatabase : RoomDatabase() {
    abstract fun cardStateDao(): CardStateDao

    companion object {
        @Volatile
        private var instance: FlashcardsDatabase? = null

        fun get(context: Context): FlashcardsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlashcardsDatabase::class.java,
                    "flashcards.db",
                )
                    // Pre-1.0 personal app: schema bumps may wipe SRS state.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}