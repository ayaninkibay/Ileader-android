package com.ileader.app

import com.ileader.app.data.models.BracketType
import org.junit.Assert.*
import org.junit.Test

class BracketTypeTest {

    @Test
    fun `BracketType fromString maps correctly`() {
        assertEquals(BracketType.UPPER, BracketType.fromString("upper"))
        assertEquals(BracketType.LOWER, BracketType.fromString("lower"))
        assertEquals(BracketType.GRAND_FINAL, BracketType.fromString("grand_final"))
        assertEquals(BracketType.THIRD_PLACE, BracketType.fromString("third_place"))
        assertEquals(BracketType.UPPER, BracketType.fromString("unknown"))
    }
}
