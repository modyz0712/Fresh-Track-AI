package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.model.RecipeDto
import my.edu.utar.freshtrackai.ai.model.RecipeIngredientDto
import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiCloudFoodExtractorTest {

    @Test
    fun `suggestRecipes skips cloud calls when Gemini key is missing`() = runBlocking {
        var cloudCalls = 0
        val extractor = GeminiCloudFoodExtractor(
            apiKey = "",
            generateContentBlock = {
                cloudCalls += 1
                """{"recipes": []}"""
            },
            localFallbackBlock = { _, _ -> fallbackResult("Local Recipe") }
        )

        val result = extractor.suggestRecipes("prompt")

        assertEquals(0, cloudCalls)
        assertEquals("Local Recipe", result.recipes.single().title)
    }

    @Test
    fun `suggestRecipes treats quota exhaustion as non-retriable and falls back immediately`() = runBlocking {
        var cloudCalls = 0
        val statuses = mutableListOf<String>()
        val extractor = GeminiCloudFoodExtractor(
            apiKey = "configured-key",
            generateContentBlock = {
                cloudCalls += 1
                throw IllegalStateException("429 RESOURCE_EXHAUSTED - credits are depleted")
            },
            localFallbackBlock = { _, onStatus ->
                onStatus?.invoke("Generating recipes with local Gemma...")
                fallbackResult("Fallback Recipe")
            }
        )

        val result = extractor.suggestRecipes("prompt", statuses::add)

        assertEquals(1, cloudCalls)
        assertEquals("Fallback Recipe", result.recipes.single().title)
        assertTrue(
            statuses.any { it.contains("quota exhausted", ignoreCase = true) }
        )
    }

    @Test
    fun `suggestRecipes parses fenced local Gemma JSON fallback`() = runBlocking {
        val extractor = GeminiCloudFoodExtractor(
            apiKey = "configured-key",
            generateContentBlock = {
                throw IllegalStateException("429 RESOURCE_EXHAUSTED - credits are depleted")
            },
            localFallbackBlock = { _, _ ->
                RecipeSuggestionResult(
                    recipes = listOf(
                        RecipeDto(
                            title = "Ignored",
                            description = "ignored",
                            availableIngredients = emptyList(),
                            missingIngredients = emptyList(),
                            instructions = emptyList()
                        )
                    )
                )
            }
        )

        val parsed = extractor.javaClass.getDeclaredMethod("parseRecipeJson", String::class.java, String::class.java)
        parsed.isAccessible = true
        val result = parsed.invoke(
            extractor,
            "```json\n{\"recipes\":[{\"title\":\"Soup\",\"description\":\"Warm\",\"availableIngredients\":[],\"missingIngredients\":[],\"instructions\":[\"Cook\"]}]}\n```",
            "local Gemma"
        ) as RecipeSuggestionResult

        assertEquals("Soup", result.recipes.single().title)
    }

    @Test
    fun `suggestRecipes parses noisy local Gemma JSON fallback`() = runBlocking {
        val extractor = GeminiCloudFoodExtractor(apiKey = "configured-key")
        val parsed = extractor.javaClass.getDeclaredMethod("parseRecipeJson", String::class.java, String::class.java)
        parsed.isAccessible = true
        val result = parsed.invoke(
            extractor,
            "Here are your recipes:\n{\"recipes\":[{\"title\":\"Bowl\",\"description\":\"Fresh\",\"availableIngredients\":[],\"missingIngredients\":[],\"instructions\":[\"Mix\"]}]}\nEnjoy!",
            "local Gemma"
        ) as RecipeSuggestionResult

        assertEquals("Bowl", result.recipes.single().title)
    }

    @Test
    fun `suggestRecipes fails clearly on malformed local Gemma output`() = runBlocking {
        val extractor = GeminiCloudFoodExtractor(apiKey = "configured-key")
        val parsed = extractor.javaClass.getDeclaredMethod("parseRecipeJson", String::class.java, String::class.java)
        parsed.isAccessible = true

        val thrown = runCatching {
            parsed.invoke(extractor, "No JSON here", "local Gemma")
        }.exceptionOrNull()

        assertTrue(thrown != null)
        assertTrue(
            thrown!!.cause?.message?.contains("malformed recipe JSON", ignoreCase = true) == true
        )
    }

    private fun fallbackResult(title: String): RecipeSuggestionResult {
        return RecipeSuggestionResult(
            recipes = listOf(
                RecipeDto(
                    title = title,
                    description = "$title description",
                    availableIngredients = listOf(
                        RecipeIngredientDto(name = "Milk", quantity = "1 bottle")
                    ),
                    missingIngredients = emptyList(),
                    instructions = listOf("Cook")
                )
            )
        )
    }
}
