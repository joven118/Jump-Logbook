package com.V2Skydivejump.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncMarkersTest {
    @Test
    fun addIsIdempotentAndSorted() {
        val markers = SyncMarkers.add("5,2,5", 3)

        assertEquals("2,3,5", markers)
    }

    @Test
    fun containsIgnoresInvalidAndZeroIds() {
        assertTrue(SyncMarkers.contains("1,bad,4", 4))
        assertFalse(SyncMarkers.contains("1,bad,4", 2))
        assertFalse(SyncMarkers.contains("1,bad,4", 0))
    }
}
