package io.github.woojaeheo.holobinder

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.woojaeheo.holobinder.core.common.MviViewModel
import io.github.woojaeheo.holobinder.core.data.CardRepository
import io.github.woojaeheo.holobinder.core.data.DeckRepository
import io.github.woojaeheo.holobinder.core.data.SettingsRepository
import io.github.woojaeheo.holobinder.core.model.Card
import io.github.woojaeheo.holobinder.core.model.DeckCard
import io.github.woojaeheo.holobinder.core.model.ThemeMode
import io.github.woojaeheo.holobinder.core.model.UserPreferences
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HoloBinderDestination { Catalog, Deck, Favorites, Settings }

sealed interface MainAction {
    data class SelectDestination(val destination: HoloBinderDestination) : MainAction
    data class RemoveFromDeck(val id: String) : MainAction
    data class Theme(val mode: ThemeMode) : MainAction
    data class DynamicColor(val enabled: Boolean) : MainAction
    data class ReducedMotion(val enabled: Boolean) : MainAction
    data class GridDensity(val columns: Int) : MainAction
}

data class MainState(
    val destination: HoloBinderDestination = HoloBinderDestination.Catalog,
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
) : MviViewModel<MainAction, MainState, MainEffect>(MainState()) {
    private val destination = kotlinx.coroutines.flow.MutableStateFlow(HoloBinderDestination.Catalog)

    init {
        viewModelScope.launch {
            combine(
                destination,
                settings.preferences,
                deckRepository.deck,
                cardRepository.observeFavorites(),
            ) { selected, preferences, deck, favorites ->
                MainState(selected, preferences, deck, favorites)
            }.collect { latest -> setState { latest } }
        }
    }

    /** 앱 입력 처리 */
    override suspend fun handleAction(action: MainAction) {
        when (action) {
            is MainAction.SelectDestination -> destination.value = action.destination
            is MainAction.RemoveFromDeck -> deckRepository.remove(action.id)
            is MainAction.Theme -> settings.setTheme(action.mode)
            is MainAction.DynamicColor -> settings.setDynamicColor(action.enabled)
            is MainAction.ReducedMotion -> settings.setReducedMotion(action.enabled)
            is MainAction.GridDensity -> settings.setGridDensity(action.columns)
        }
    }
}
