package io.levanov.flashcards.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CardStateDao {
    @Query("SELECT * FROM card_states")
    fun observeAll(): Flow<List<CardStateEntity>>

    @Query("SELECT * FROM card_states WHERE `key` IN (:keys)")
    suspend fun getByKeys(keys: List<String>): List<CardStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(states: List<CardStateEntity>)
}