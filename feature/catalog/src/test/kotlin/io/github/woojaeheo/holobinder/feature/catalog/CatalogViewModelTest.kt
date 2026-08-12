package io.github.woojaeheo.holobinder.feature.catalog

import io.github.woojaeheo.holobinder.core.common.SyncResult
import io.github.woojaeheo.holobinder.core.data.CardRepository
import io.github.woojaeheo.holobinder.core.data.DeckRepository
import io.github.woojaeheo.holobinder.core.domain.DeckAddResult
import io.github.woojaeheo.holobinder.core.model.Card
import io.github.woojaeheo.holobinder.core.model.CardFilter
import io.github.woojaeheo.holobinder.core.model.DeckCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `검색 입력을 debounce 전에 화면 상태에 반영한다`() = runTest(dispatcher) {
        val viewModel = CatalogViewModel(FakeCardRepository(), FakeDeckRepository())

        viewModel.onAction(CatalogAction.Search("피카츄"))
        runCurrent()

        assertEquals("피카츄", viewModel.state.value.filter.query)
    }

    @Test
    fun `연속 한글 입력에서 마지막 검색어를 유지한다`() = runTest(dispatcher) {
        val viewModel = CatalogViewModel(FakeCardRepository(), FakeDeckRepository())

        viewModel.onAction(CatalogAction.Search("피"))
        viewModel.onAction(CatalogAction.Search("피카"))
        viewModel.onAction(CatalogAction.Search("피카츄"))
        runCurrent()

        assertEquals("피카츄", viewModel.state.value.filter.query)
    }
}

private class FakeCardRepository : CardRepository {
    private val cards = MutableStateFlow<List<Card>>(emptyList())

    override fun observeCards(filter: CardFilter): Flow<List<Card>> = cards
    override fun observeCard(id: String): Flow<Card?> = MutableStateFlow(null)
    override fun observeFavorites(): Flow<List<Card>> = MutableStateFlow(emptyList())
    override suspend fun refresh(filter: CardFilter, force: Boolean) = SyncResult.Success
    override suspend fun refreshCard(id: String) = SyncResult.Success
    override suspend fun toggleFavorite(id: String) = Unit
    override suspend fun latestFavorite(): Card? = null
}

private class FakeDeckRepository : DeckRepository {
    override val deck: Flow<List<DeckCard>> = MutableStateFlow(emptyList())
    override suspend fun add(cardId: String): DeckAddResult = DeckAddResult.Added
    override suspend fun remove(cardId: String) = Unit
}
