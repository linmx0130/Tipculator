package me.mengxiaolin.tipculator

import org.junit.Test

import org.junit.Assert.*

class UtilityTest {
    @Test
    fun `test calculate tips without rounding`() {
        assertEquals(17, calculateTips(100, 0, 17, false))
    }
    @Test
    fun `test calculate tips with rounding`() {
        assertEquals(50, calculateTips(500, 50, 15, true))
    }
}