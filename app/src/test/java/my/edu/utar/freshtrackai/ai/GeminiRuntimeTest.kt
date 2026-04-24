package my.edu.utar.freshtrackai.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiRuntimeTest {

    @Test
    fun `resolveConfiguredGeminiApiKey trims saved key`() {
        assertEquals("saved-key", resolveConfiguredGeminiApiKey("  saved-key  "))
    }

    @Test
    fun `resolveConfiguredGeminiApiKey returns blank when settings key is blank`() {
        assertEquals("", resolveConfiguredGeminiApiKey("   "))
    }

    @Test
    fun `validateGeminiApiKey returns not set when key is blank`() = runTest {
        assertEquals(
            GeminiApiKeyValidationResult.NotSet,
            validateGeminiApiKey("   ")
        )
    }

    @Test
    fun `validateGeminiApiKey returns valid when test call succeeds`() = runTest {
        assertEquals(
            GeminiApiKeyValidationResult.Valid,
            validateGeminiApiKey("saved-key") { _, _ -> "OK" }
        )
    }

    @Test
    fun `validateGeminiApiKey returns invalid key when auth failure occurs`() = runTest {
        assertEquals(
            GeminiApiKeyValidationResult.InvalidKey,
            validateGeminiApiKey("saved-key") { _, _ -> error("401 API key invalid") }
        )
    }

    @Test
    fun `validateGeminiApiKey returns quota exhausted for depleted credits`() = runTest {
        assertEquals(
            GeminiApiKeyValidationResult.QuotaExhausted,
            validateGeminiApiKey("saved-key") { _, _ ->
                error("429 RESOURCE_EXHAUSTED - Your prepayment credits are depleted.")
            }
        )
    }

    @Test
    fun `validateGeminiApiKey returns request failed for unknown runtime issues`() = runTest {
        assertEquals(
            GeminiApiKeyValidationResult.RequestFailed,
            validateGeminiApiKey("saved-key") { _, _ -> error("socket timeout") }
        )
    }

    @Test
    fun `classifyGeminiFailure treats depleted quota as quota exhausted`() {
        val throwable = IllegalStateException(
            "429 RESOURCE_EXHAUSTED - Your prepayment credits are depleted."
        )

        assertEquals(
            GeminiFailureKind.QuotaExhausted,
            classifyGeminiFailure(throwable)
        )
    }
}
