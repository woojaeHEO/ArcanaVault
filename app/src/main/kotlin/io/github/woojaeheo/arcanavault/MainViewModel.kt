package io.github.woojaeheo.arcanavault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.woojaeheo.arcanavault.core.common.MviContract
import io.github.woojaeheo.arcanavault.core.data.CardRepository
import io.github.woojaeheo.arcanavault.core.data.DeckRepository
import io.github.woojaeheo.arcanavault.core.data.SettingsRepository
import io.github.woojaeheo.arcanavault.core.model.Card
import io.github.woojaeheo.arcanavault.core.model.DeckCard
import io.github.woojaeheo.arcanavault.core.model.ThemeMode
import io.github.woojaeheo.arcanavault.core.model.UserPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ArcanaDestination { Catalog, Deck, Favorites, Settings }

sealed interface MainAction {
    data class SelectDestination(val destination: ArcanaDestination) : MainAction
    data class RemoveFromDeck(val id: String) : MainAction
    data class Theme(val mode: ThemeMode) : MainAction
    data class DynamicColor(val enabled: Boolean) : MainAction
    data class ReducedMotion(val enabled: Boolean) : MainAction
    data class GridDensity(val columns: Int) : MainAction
}

data class MainState(
    val destination: ArcanaDestination = ArcanaDestination.Catalog,
    val preferences: UserPreferences = UserPreferences(),
    val deck: List<DeckCard> = emptyList(),
    val favorites: List<Card> = emptyList(),
)

sealed interface MainEffect

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val deckRepository: DeckRepository,
    cardRepository: CardRepository,
) : ViewModel(), MviContract<MainAction, MainState, MainEffect> {
    private val destination = kotlinx.coroutines.flow.MutableStateFlow(ArcanaDestination.Catalog)
    private val effectChannel = Channel<MainEffect>(Channel.BUFFERED)

    override val state: StateFlow<MainState> = combine(
        destination,
        settings.preferences,
        deckRepository.deck,
        cardRepository.observeFavorites(),
    ) { selected, preferences, deck, favorites ->
        MainState(selected, preferences, deck, favorites)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainState())

    override val effects: Flow<MainEffect> = effectChannel.receiveAsFlow()

    override fun onAction(action: MainAction) {
        when (action) {
            is MainAction.SelectDestination -> destination.value = action.destination
            is MainAction.RemoveFromDeck -> viewModelScope.launch { deckRepository.remove(action.id) }
            is MainAction.Theme -> viewModelScope.launch { settings.setTheme(action.mode) }
            is MainAction.DynamicColor -> viewModelScope.launch { settings.setDynamicColor(action.enabled) }
            is MainAction.ReducedMotion -> viewModelScope.launch { settings.setReducedMotion(action.enabled) }
            is MainAction.GridDensity -> viewModelScope.launch { settings.setGridDensity(action.columns) }
        }
    }
}
