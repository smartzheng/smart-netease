package com.smart.neteast.network

import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class LikeWriteTransportTest {
    @Test
    fun `both directions use API Enhanced song like payload on EAPI`() {
        for (desired in listOf(true, false)) {
            var calls = 0
            val code = PlaylistApi.writeLikeOnce(2754388325L, 123456L, desired) { uri, pairs, antiCheat ->
                calls++
                assertEquals("/eapi/song/like", uri)
                assertFalse(antiCheat)
                assertEquals(mapOf(
                    "like" to desired.toString(), "trackId" to "2754388325", "userid" to "123456"
                ), pairs.toMap())
                "{\"code\":200}"
            }
            assertTrue(PlaylistApi.isLikeWriteAccepted(code))
            assertEquals(1, calls)
        }
    }

    @Test
    fun `risk errors malformed responses and network failures never submit another request`() {
        for (response in listOf("{\"code\":524}", "{\"code\":-460}", "broken", "network")) {
            var calls = 0
            val code = PlaylistApi.writeLikeOnce(42, 123456L, true) { uri, _, _ ->
                calls++
                assertEquals("/eapi/song/like", uri)
                if (response == "network") throw IOException("offline")
                response
            }
            assertFalse(PlaylistApi.isLikeWriteAccepted(code))
            assertEquals(1, calls)
        }
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is propagated without falling back`() {
        PlaylistApi.writeLikeOnce(42, 123456L, true) { _, _, _ -> throw CancellationException("cancel") }
    }
}
