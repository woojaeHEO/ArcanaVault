package io.github.woojaeheo.arcanavault.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(entities = [CardEntity::class, DeckCardEntity::class], version = 1, exportSchema = true)
abstract class ArcanaDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun deckDao(): DeckDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArcanaDatabase =
        Room.databaseBuilder(context, ArcanaDatabase::class.java, "arcana-vault.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideCardDao(database: ArcanaDatabase): CardDao = database.cardDao()
    @Provides fun provideDeckDao(database: ArcanaDatabase): DeckDao = database.deckDao()
}
