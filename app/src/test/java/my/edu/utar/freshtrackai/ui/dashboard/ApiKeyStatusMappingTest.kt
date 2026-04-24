package my.edu.utar.freshtrackai.ui.dashboard

import my.edu.utar.freshtrackai.ai.GeminiApiKeyValidationResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiKeyStatusMappingTest {

    @Test
    fun `maps quota exhausted to explicit quota message`() {
        val status = apiKeyStatusForValidationResult(GeminiApiKeyValidationResult.QuotaExhausted)

        assertEquals("API key works, but quota/credits are exhausted.", status.message)
        assertEquals(ApiKeyStatusTone.Error, status.tone)
    }

    @Test
    fun `maps invalid key to invalid message`() {
        val status = apiKeyStatusForValidationResult(GeminiApiKeyValidationResult.InvalidKey)

        assertEquals("API key is invalid.", status.message)
        assertEquals(ApiKeyStatusTone.Error, status.tone)
    }

    @Test
    fun `maps request failure to retry message`() {
        val status = apiKeyStatusForValidationResult(GeminiApiKeyValidationResult.RequestFailed)

        assertEquals("Request failed. Check connection or try again.", status.message)
        assertEquals(ApiKeyStatusTone.Error, status.tone)
    }
}
