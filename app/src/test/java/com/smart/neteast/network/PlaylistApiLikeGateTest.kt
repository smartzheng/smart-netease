package com.smart.neteast.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 锁住普通歌曲 `/eapi/song/like` 的业务码判定。
 */
class PlaylistApiLikeGateTest {

    @Test
    fun `only code 200 is directly accepted`() {
        assertTrue(PlaylistApi.isLikeWriteAccepted(200))
        assertFalse("非 200 由云端读回判断是否已经处于目标状态", PlaylistApi.isLikeWriteAccepted(502))
    }

    @Test
    fun `risk-control and error codes are rejected`() {
        assertFalse(PlaylistApi.isLikeWriteAccepted(-1))
        assertFalse(PlaylistApi.isLikeWriteAccepted(301))
        assertFalse(PlaylistApi.isLikeWriteAccepted(403))
        assertFalse(PlaylistApi.isLikeWriteAccepted(405))
        assertFalse("524 = 环境异常已取消喜欢，绝不是成功", PlaylistApi.isLikeWriteAccepted(524))
        assertFalse(PlaylistApi.isLikeWriteAccepted(-460))
    }
}
