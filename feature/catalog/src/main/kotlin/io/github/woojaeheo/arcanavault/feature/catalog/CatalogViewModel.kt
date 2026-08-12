package io.github.woojaeheo.arcanavault.feature.catalog

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.woojaeheo.arcanavault.core.common.MviViewModel
import io.github.woojaeheo.arcanavault.core.common.SyncResult
import io.github.woojaeheo.arcanavault.core.data.CardRepository
import io.github.woojaeheo.arcanavault.core.data.DeckRepository
import io.github.woojaeheo.arcanavault.core.domain.DeckAddResult
import io.github.woojaeheo.arcanavault.core.model.Card
import io.github.woojaeheo.arcanavault.core.model.CardFilter
import io.github.woojaeheo.arcanavault.core.model.CardSort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CatalogAction {
    data class Search(val query: String) : CatalogAction
    data class FilterType(val type: String?) : CatalogAction
    data class Sort(val sort: CardSort) : CatalogAction
    data class Select(val card: Card?) : CatalogAction
    data class ToggleFavorite(val id: String) : CatalogAction
    data class AddToDeck(val id: String) : CatalogAction
    data object SurpriseMe : CatalogAction
    data object Refresh : CatalogAction
}

data class CatalogState(
    val cards: List<Card> = emptyList(),
    val filter: CardFilter = CardFilter(),
    val selectedCard: Card? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

sealed interface CatalogEffect {
    data class Message(val text: String) : CatalogEffect
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
) : MviViewModel<CatalogAction, CatalogState, CatalogEffect>(CatalogState()) {
    private val filter = MutableStateFlow(CardFilter())

    private val cards = filter
        .debounce(250)
        .flatMapLatest(cardRepository::observeCards)

    init {
        refresh(force = false)
        viewModelScope.launch {
            cards.collectLatest { items ->
                setState {
                    copy(
                        cards = items,
                        filter = this@CatalogViewModel.filter.value,
                        selectedCard = selectedCard?.let { selected -> items.firstOrNull { it.id == selected.id } ?: selected },
                        isLoading = false,
                    )
                }
            }
        }
        viewModelScope.launch {
            filter.drop(1).debounce(350).distinctUntilChanged().collectLatest { activeFilter ->
                refresh(activeFilter, force = true)
            }
        }
    }

    /** 카탈로그 입력 처리 */
    override suspend fun handleAction(action: CatalogAction) {
        when (action) {
            is CatalogAction.Search -> {
                updateFilter { copy(query = action.query) }
            }
            is CatalogAction.FilterType -> {
                updateFilter { copy(type = action.type) }
            }
            is CatalogAction.Sort -> {
                updateFilter { copy(sort = action.sort) }
            }
            is CatalogAction.Select -> {
                setState { copy(selectedCard = action.card) }
                action.card?.let { card ->
                    latestIntent(DETAIL_JOB) {
                        val result = cardRepository.refreshCard(card.id)
                        if (result is SyncResult.Error) {
                            emitEffect(CatalogEffect.Message(result.message))
                        }
                    }
                }
            }
            is CatalogAction.ToggleFavorite -> cardRepository.toggleFavorite(action.id)
            is CatalogAction.AddToDeck -> {
                val message = when (deckRepository.add(action.id)) {
                    DeckAddResult.Added -> "덱에 카드를 추가했습니다."
                    DeckAddResult.CopyLimitReached -> "같은 카드는 네 장까지만 넣을 수 있습니다."
                    DeckAddResult.DeckFull -> "덱에는 카드 육십 장까지만 넣을 수 있습니다."
                    DeckAddResult.CardNotFound -> "카드 정보를 찾지 못했습니다."
                }
                emitEffect(CatalogEffect.Message(message))
            }
            CatalogAction.SurpriseMe -> {
                val card = currentState().cards.randomOrNull()
                if (card == null) {
                    emitEffect(CatalogEffect.Message("추천할 카드를 불러오는 중입니다."))
                } else {
                    setState { copy(selectedCard = card) }
                    emitEffect(CatalogEffect.Message("오늘의 행운 카드는 ${card.name}입니다."))
                }
            }
            CatalogAction.Refresh -> refresh(filter.value, force = true)
        }
    }

    private fun refresh(force: Boolean) = refresh(filter.value, force)

    /** 이전 요청을 취소하고 최신 조건만 동기화 */
    private fun refresh(activeFilter: CardFilter, force: Boolean) {
        latestIntent(REFRESH_JOB) {
            refreshFilter(activeFilter, force)
        }
    }

    private fun updateFilter(transform: CardFilter.() -> CardFilter) {
        val updated = filter.value.transform()
        filter.value = updated
        setState { copy(filter = updated) }
    }

    private suspend fun refreshFilter(activeFilter: CardFilter, force: Boolean) {
        setState { copy(isRefreshing = true) }
        when (val result = cardRepository.refresh(activeFilter, force)) {
            SyncResult.Success -> Unit
            is SyncResult.Error -> emitEffect(CatalogEffect.Message(result.message))
        }
        setState { copy(isRefreshing = false) }
    }

    private companion object {
        const val DETAIL_JOB = "card-detail"
        const val REFRESH_JOB = "catalog-refresh"
    }
}
