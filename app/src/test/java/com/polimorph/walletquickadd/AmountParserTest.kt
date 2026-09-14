package com.polimorph.walletquickadd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class AmountParserTest {
    @Test fun parsesPolishDecimal() {
        assertEquals(BigDecimal("42.50"), AmountParser.parse("42,50 zł"))
    }
    @Test fun parsesPolishThousands() {
        assertEquals(BigDecimal("1234.56"), AmountParser.parse("1 234,56 PLN"))
    }
    @Test fun parsesMixedSeparators() {
        assertEquals(BigDecimal("1234.56"), AmountParser.parse("1.234,56"))
        assertEquals(BigDecimal("1234.56"), AmountParser.parse("1,234.56"))
    }
    @Test fun rejectsTextWithoutAmount() {
        assertNull(AmountParser.parse("brak kwoty"))
    }
}
