package io.github.woojaeheo.holobinder.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentHashMap

/** 화면의 단방향 MVI 계약 */
interface MviContract<Action : Any, State : Any, Effect : Any> {
    /** 화면 상태 */
    val state: StateFlow<State>

    /** 한 번만 소비하는 화면 이벤트 */
    val effects: Flow<Effect>

    /** 사용자 입력 전달 */
    fun onAction(action: Action)
}

/** 공통 MVI ViewModel */
abstract class MviViewModel<Action : Any, State : Any, Effect : Any>(
    initialState: State,
    actionDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel(), MviContract<Action, State, Effect> {
    private val mutableState = MutableStateFlow(initialState)
    private val effectChannel = Channel<Effect>(capacity = Channel.BUFFERED)
    private val actionChannel = Channel<Action>(capacity = Channel.BUFFERED)
    private val keyedJobs = ConcurrentHashMap<Any, Job>()

    final override val state: StateFlow<State> = mutableState.asStateFlow()
    final override val effects: Flow<Effect> = effectChannel.receiveAsFlow()

    init {
        (viewModelScope + actionDispatcher).launch {
            for (action in actionChannel) handleAction(action)
        }
    }

    /** 입력 처리 */
    protected abstract suspend fun handleAction(action: Action)

    /** 상태 변경 */
    protected fun setState(reducer: State.() -> State) {
        mutableState.update(reducer)
    }

    /** 현재 상태 */
    protected fun currentState(): State = mutableState.value

    /** 화면 이벤트 전달 */
    protected suspend fun emitEffect(effect: Effect) {
        effectChannel.send(effect)
    }

    /** 비동기 작업 실행 */
    protected fun intent(
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
        onError: suspend (Throwable) -> Unit = {},
        block: suspend () -> Unit,
    ): Job = viewModelScope.launch(dispatcher) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            onError(throwable)
        }
    }

    /** 같은 키의 이전 작업을 취소하고 최신 작업 실행 */
    protected fun latestIntent(
        key: Any,
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
        onError: suspend (Throwable) -> Unit = {},
        block: suspend () -> Unit,
    ): Job {
        keyedJobs.remove(key)?.cancel()
        val job = intent(dispatcher, onError, block)
        keyedJobs[key] = job
        job.invokeOnCompletion {
            if (keyedJobs[key] === job) keyedJobs.remove(key)
        }
        return job
    }

    /** 지정한 키의 진행 중인 작업 취소 */
    protected fun cancelIntent(key: Any) {
        keyedJobs.remove(key)?.cancel()
    }

    /** 결과 콜백이 있는 비동기 작업 실행 */
    protected fun <T> execute(
        key: Any? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        onLoading: () -> Unit = {},
        onSuccess: (T) -> Unit,
        onFailure: suspend (Throwable) -> Unit,
        block: suspend () -> T,
    ): Job {
        onLoading()
        val task: suspend () -> Unit = {
            runSuspendCatching(block).fold(
                onSuccess = onSuccess,
                onFailure = { throwable -> onFailure(throwable) },
            )
        }
        return if (key == null) intent(dispatcher, block = task) else latestIntent(key, dispatcher, block = task)
    }

    final override fun onAction(action: Action) {
        if (actionChannel.trySend(action).isFailure) {
            viewModelScope.launch { actionChannel.send(action) }
        }
    }

    override fun onCleared() {
        actionChannel.close()
        effectChannel.close()
        keyedJobs.values.forEach(Job::cancel)
        keyedJobs.clear()
    }
}

/** 데이터 동기화 결과 */
sealed interface SyncResult {
    data object Success : SyncResult
    data class Error(val message: String) : SyncResult
}
