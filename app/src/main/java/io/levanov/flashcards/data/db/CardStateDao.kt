package io.levanov.flashcards.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CardStateDao {
    @Query("SELECT * FROM card_states")
    fun observeAll(): Flow<List<CardStateEntity>>

    @Query("SELECT * FROM card_states")
    suspend fun getAll(): List<CardStateEntity>

    @Query("SELECT * FROM card_states WHERE `key` IN (:keys)")
    suspend fun getByKeys(keys: List<String>): List<CardStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(states: List<CardStateEntity>)

    @Query("DELETE FROM card_states")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(states: List<CardStateEntity>) {
        deleteAll()
        upsertAll(states)
    }
}