package io.github.woojaeheo.arcanavault.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton abstract fun bindCardRepository(impl: OfflineFirstCardRepository): CardRepository
    @Binds @Singleton abstract fun bindDeckRepository(impl: OfflineDeckRepository): DeckRepository
}
