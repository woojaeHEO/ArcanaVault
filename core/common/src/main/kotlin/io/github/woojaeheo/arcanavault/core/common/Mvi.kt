package io.github.woojaeheo.arcanavault.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** MVI 화면 계약 */
interface MviContract<Action, State, Effect> {
    val state: StateFlow<State>
    val effects: Flow<Effect>
    fun onAction(action: Action)
}

/** 데이터 갱신 결과 */
sealed interface SyncResult {
    data object Success : SyncResult
    data class Error(val message: String) : SyncResult
}
