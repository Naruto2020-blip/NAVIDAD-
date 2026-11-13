package com.example

import com.example.model.FestiveTarget
import com.example.ui.NavidadViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CountdownCalculatorTest {

    @Test
    fun `countdown before christmas calculates exact days hours minutes seconds`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.DECEMBER, 23, 22, 30, 15)
            set(Calendar.MILLISECOND, 0)
        }
        val snapshot = NavidadViewModel.computeCountdown(now.timeInMillis, FestiveTarget.NAVIDAD)

        assertFalse(snapshot.isCelebrationToday)
        assertEquals(1L, snapshot.days)
        assertEquals(1L, snapshot.hours)
        assertEquals(29L, snapshot.minutes)
        assertEquals(45L, snapshot.seconds)
        assertEquals("25 de diciembre de 2026", snapshot.targetDateLabel)
    }

    @Test
    fun `countdown on christmas day triggers celebration state`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.DECEMBER, 25, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val snapshot = NavidadViewModel.computeCountdown(now.timeInMillis, FestiveTarget.NAVIDAD)

        assertTrue(snapshot.isCelebrationToday)
        assertEquals(0L, snapshot.days)
        assertEquals(1f, snapshot.progressFraction, 0.001f)
    }

    @Test
    fun `countdown after christmas rolls over to next year`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.DECEMBER, 26, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val snapshot = NavidadViewModel.computeCountdown(now.timeInMillis, FestiveTarget.NAVIDAD)

        assertFalse(snapshot.isCelebrationToday)
        assertEquals("25 de diciembre de 2027", snapshot.targetDateLabel)
        assertTrue(snapshot.days >= 363L)
    }
}
