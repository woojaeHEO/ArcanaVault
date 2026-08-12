package io.github.woojaeheo.holobinder.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AsyncTest {
    @Test(expected = CancellationException::class)
    fun `취소 예외는 Result 실패로 변환하지 않는다`() = runTest {
        runSuspendCatching<Unit> { throw CancellationException() }
    }

    @Test
    fun `재시도 뒤 성공 값을 반환한다`() = runTest {
        var calls = 0
        val result = retryWithBackoff(
            attempts = 3,
            initialDelayMillis = 1,
            maxDelayMillis = 1,
        ) {
            calls += 1
            if (calls < 3) error("temporary")
            "ready"
        }
        assertEquals("ready", result)
        assertEquals(3, calls)
    }
}
