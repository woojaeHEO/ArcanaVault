package io.github.woojaeheo.arcanavault.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.woojaeheo.arcanavault.core.common.MviContract
import io.github.woojaeheo.arcanavault.core.common.SyncResult
import io.github.woojaeheo.arcanavault.core.data.CardRepository
import io.github.woojaeheo.arcanavault.core.data.DeckRepository
import io.github.woojaeheo.arcanavault.core.model.Card
import io.github.woojaeheo.arcanavault.core.model.CardFilter
import io.github.woojaeheo.arcanavault.core.model.CardSort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CatalogAction {
    data class Search(val query: String) : CatalogAction
    data class FilterType(val type: String?) : CatalogAction
    data class Sort(val sort: CardSort) : CatalogAction
    data class Select(val card: Card?) : CatalogAction
    data class ToggleFavorite(val id: String) : CatalogAction
    data class AddToDeck(val id: String) : CatalogAction
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
) : ViewModel(), MviContract<CatalogAction, CatalogState, CatalogEffect> {
    private val filter = MutableStateFlow(CardFilter())
    private val localState = MutableStateFlow(CatalogState())
    private val effectChannel = Channel<CatalogEffect>(Channel.BUFFERED)

    private val cards = filter
        .debounce(250)
        .flatMapLatest(cardRepository::observeCards)

    override val state: StateFlow<CatalogState> = combine(localState, filter, cards) { local, activeFilter, items ->
        local.copy(
            cards = items,
            filter = activeFilter,
            selectedCard = local.selectedCard?.let { selected -> items.firstOrNull { it.id == selected.id } ?: selected },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogState())

    override val effects = effectChannel.receiveAsFlow()

    init {
        refresh(force = false)
        viewModelScope.launch {
            filter.drop(1).debounce(350).distinctUntilChanged().collectLatest { activeFilter ->
                refreshFilter(activeFilter, force = true)
            }
        }
    }

    override fun onAction(action: CatalogAction) {
        when (action) {
            is CatalogAction.Search -> {
                filter.update { it.copy(query = action.query) }
            }
            is CatalogAction.FilterType -> {
                filter.update { it.copy(type = action.type) }
            }
            is CatalogAction.Sort -> {
                filter.update { it.copy(sort = action.sort) }
            }
            is CatalogAction.Select -> {
                localState.update { it.copy(selectedCard = action.card) }
                action.card?.let { card ->
                    viewModelScope.launch {
                        val result = cardRepository.refreshCard(card.id)
                        if (result is SyncResult.Error) {
                            effectChannel.send(CatalogEffect.Message(result.message))
                        }
                    }
                }
            }
            is CatalogAction.ToggleFavorite -> viewModelScope.launch { cardRepository.toggleFavorite(action.id) }
            is CatalogAction.AddToDeck -> viewModelScope.launch {
                deckRepository.add(action.id)
                effectChannel.send(CatalogEffect.Message("덱에 카드를 추가했습니다."))
            }
            CatalogAction.Refresh -> refresh(force = true)
        }
    }

    private fun refresh(force: Boolean) = viewModelScope.launch {
        refreshFilter(filter.value, force)
    }

    private suspend fun refreshFilter(activeFilter: CardFilter, force: Boolean) {
        localState.update { it.copy(isRefreshing = true) }
        when (val result = cardRepository.refresh(activeFilter, force)) {
            SyncResult.Success -> Unit
            is SyncResult.Error -> effectChannel.send(CatalogEffect.Message(result.message))
        }
        localState.update { it.copy(isRefreshing = false) }
    }
}
