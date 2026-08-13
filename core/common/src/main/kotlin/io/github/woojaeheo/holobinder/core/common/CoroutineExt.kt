package io.github.woojaeheo.holobinder.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 교체 가능한 Coroutine Dispatcher */
interface CoroutineDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

/** 기본 Coroutine Dispatcher */
object DefaultCoroutineDispatchers : CoroutineDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main.immediate
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

/** 입출력 작업 실행 */
suspend inline fun <T> withIo(crossinline block: suspend () -> T): T =
    withContext(Dispatchers.IO) { block() }

/** Flow 오류 관찰 */
fun <T> Flow<T>.onError(action: suspend (Throwable) -> Unit): Flow<T> = catch { throwable ->
    action(throwable)
    throw throwable
}

/** 선택 값 기준 중복 제거 */
fun <T, R> Flow<T>.distinctBy(selector: (T) -> R): Flow<T> =
    map { value -> selector(value) to value }
        .distinctUntilChanged { old, new -> old.first == new.first }
        .map { it.second }

/** 구독 기반 StateFlow 변환 */
fun <T> Flow<T>.stateInWhileSubscribed(
    scope: CoroutineScope,
    initialValue: T,
    stopTimeoutMillis: Long = 5_000L,
): StateFlow<T> = stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
    initialValue = initialValue,
)

/** Result 콜백 실행 */
fun <T> CoroutineScope.launchResult(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    onSuccess: suspend (T) -> Unit = {},
    onFailure: suspend (Throwable) -> Unit = {},
    block: suspend () -> T,
): Job = launch(dispatcher) {
    runSuspendCatching { block() }.fold(
        onSuccess = { value -> onSuccess(value) },
        onFailure = { throwable -> onFailure(throwable) },
    )
}
