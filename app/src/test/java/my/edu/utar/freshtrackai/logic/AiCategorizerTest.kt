package my.edu.utar.freshtrackai.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class AiCategorizerTest {

    @Test
    fun `resolveApiKey uses saved settings key`() {
        assertEquals("saved-key", AiCategorizer.resolveApiKey("  saved-key  "))
    }

    @Test
    fun `resolveApiKey returns blank when settings key is blank`() {
        assertEquals("", AiCategorizer.resolveApiKey("   "))
    }
}
