package io.github.woojaeheo.holobinder.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random

/** 비동기 화면 상태 */
sealed interface Async<out T> {
    /** 시작 전 */
    data object Uninitialized : Async<Nothing>

    /** 첫 로딩 */
    data object Loading : Async<Nothing>

    /** 기존 데이터를 유지하는 새로고침 */
    data class Refreshing<T>(val value: T) : Async<T>

    /** 성공 */
    data class Success<T>(val value: T) : Async<T>

    /** 실패 */
    data class Failure<T>(val error: AppError, val previous: T? = null) : Async<T>
}

/** 공통 오류 */
sealed interface AppError {
    val cause: Throwable?

    data class Network(override val cause: Throwable) : AppError
    data class Server(val code: Int?, override val cause: Throwable? = null) : AppError
    data class Storage(override val cause: Throwable) : AppError
    data class Unknown(override val cause: Throwable) : AppError
}

/** 표시 가능한 최신 값 */
fun <T> Async<T>.valueOrNull(): T? = when (this) {
    is Async.Success -> value
    is Async.Refreshing -> value
    is Async.Failure -> previous
    Async.Loading, Async.Uninitialized -> null
}

/** 로딩 여부 */
val Async<*>.isLoading: Boolean
    get() = this is Async.Loading || this is Async.Refreshing

/** 취소를 유지하는 비동기 Result */
suspend inline fun <T> runSuspendCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    Result.failure(throwable)
}

/** 지수 백오프 재시도 */
suspend fun <T> retryWithBackoff(
    attempts: Int = 3,
    initialDelayMillis: Long = 400L,
    maxDelayMillis: Long = 4_000L,
    retryWhen: (Throwable) -> Boolean = { true },
    block: suspend (attempt: Int) -> T,
): T {
    require(attempts > 0) { "attempts must be greater than zero" }
    var lastFailure: Throwable? = null
    repeat(attempts) { index ->
        try {
            return block(index + 1)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            lastFailure = throwable
            if (index == attempts - 1 || !retryWhen(throwable)) throw throwable
            val exponential = initialDelayMillis * 2.0.pow(index).toLong()
            val bounded = exponential.coerceAtMost(maxDelayMillis)
            delay(Random.nextLong((bounded * .8).toLong(), bounded + 1))
        }
    }
    throw checkNotNull(lastFailure)
}
