package my.edu.utar.freshtrackai

import my.edu.utar.freshtrackai.ai.GemmaModelStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityTest {

    @Test
    fun `shouldRequestGemmaModel returns true for not set`() {
        assertEquals(true, shouldRequestGemmaModel(GemmaModelStatus.NotSet))
    }

    @Test
    fun `shouldRequestGemmaModel returns true for missing file`() {
        assertEquals(true, shouldRequestGemmaModel(GemmaModelStatus.MissingFile))
    }

    @Test
    fun `shouldRequestGemmaModel returns false for configured model`() {
        assertEquals(false, shouldRequestGemmaModel(GemmaModelStatus.Configured))
    }
}
