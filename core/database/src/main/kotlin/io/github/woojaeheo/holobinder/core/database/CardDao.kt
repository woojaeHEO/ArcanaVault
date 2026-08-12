package io.github.woojaeheo.holobinder.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query(
        """SELECT * FROM cards
            WHERE imageUrl != ''
            AND (:query = '' OR INSTR(LOWER(name), LOWER(:query)) > 0 OR INSTR(LOWER(description), LOWER(:query)) > 0)
            AND (:type IS NULL OR types LIKE '%' || :type || '%')
            AND (:supertype IS NULL OR supertype = :supertype)
            ORDER BY CASE WHEN :sort = 'RecentlyAdded' THEN updatedAt END DESC,
                     name ASC""",
    )
    fun observeCards(query: String, type: String?, supertype: String?, sort: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun observeCard(id: String): Flow<CardEntity?>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun card(id: String): CardEntity?

    @Query("SELECT * FROM cards WHERE isFavorite = 1 AND imageUrl != '' ORDER BY name")
    fun observeFavorites(): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<CardEntity>)

    @Query("UPDATE cards SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("SELECT isFavorite FROM cards WHERE id = :id")
    suspend fun isFavorite(id: String): Boolean?

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun count(): Int

    @Query("SELECT * FROM cards WHERE isFavorite = 1 AND imageUrl != '' ORDER BY updatedAt DESC LIMIT 1")
    suspend fun latestFavorite(): CardEntity?

    @Query("SELECT * FROM cards WHERE imageUrl != '' ORDER BY RANDOM() LIMIT 1")
    suspend fun randomCard(): CardEntity?

    @Query("SELECT * FROM cards WHERE id != :excludedId AND imageUrl != '' ORDER BY RANDOM() LIMIT 1")
    suspend fun randomCardExcluding(excludedId: String): CardEntity?
}

@Dao
interface DeckDao {
    @Query("SELECT * FROM deck_cards")
    fun observeDeck(): Flow<List<DeckCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DeckCardEntity)

    @Query("DELETE FROM deck_cards WHERE cardId = :cardId")
    suspend fun remove(cardId: String)

    @Query("SELECT quantity FROM deck_cards WHERE cardId = :cardId")
    suspend fun quantity(cardId: String): Int?

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM deck_cards")
    suspend fun totalQuantity(): Int
}
