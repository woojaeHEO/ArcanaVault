package io.github.woojaeheo.arcanavault.core.database

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
            WHERE (:query = '' OR name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
            AND (:type IS NULL OR types LIKE '%' || :type || '%')
            AND (:supertype IS NULL OR supertype = :supertype)
            ORDER BY CASE WHEN :sort = 'Price' THEN price END DESC,
                     CASE WHEN :sort = 'Newest' THEN releaseDate END DESC,
                     name ASC""",
    )
    fun observeCards(query: String, type: String?, supertype: String?, sort: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun observeCard(id: String): Flow<CardEntity?>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun card(id: String): CardEntity?

    @Query("SELECT * FROM cards WHERE isFavorite = 1 ORDER BY name")
    fun observeFavorites(): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<CardEntity>)

    @Query("UPDATE cards SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("SELECT isFavorite FROM cards WHERE id = :id")
    suspend fun isFavorite(id: String): Boolean?

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun count(): Int

    @Query("SELECT * FROM cards WHERE isFavorite = 1 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun latestFavorite(): CardEntity?
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
}
