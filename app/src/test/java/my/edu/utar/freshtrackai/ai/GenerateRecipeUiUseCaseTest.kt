package my.edu.utar.freshtrackai.ai

import kotlinx.coroutines.runBlocking
import my.edu.utar.freshtrackai.ai.model.RecipeDto
import my.edu.utar.freshtrackai.ai.model.RecipeIngredientDto
import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult
import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateRecipeUiUseCaseTest {

    private val inventory = listOf(
        InventoryItem(
            id = "milk",
            name = "Milk",
            category = InventoryCategory.Dairy,
            quantityLabel = "1 bottle",
            addedDaysAgo = 1,
            expiresInDays = 2,
            thumbnailRef = "milk"
        ),
        InventoryItem(
            id = "spinach",
            name = "Spinach",
            category = InventoryCategory.Vegetables,
            quantityLabel = "1 pack",
            addedDaysAgo = 1,
            expiresInDays = 2,
            thumbnailRef = "spinach"
        )
    )

    @Test
    fun `generateFromInventory maps extractor recipes without customization filtering`() = runBlocking {
        val extractor = FakeCloudFoodExtractor(
            RecipeSuggestionResult(
                recipes = listOf(
                    recipe(
                        title = "Cream Pasta",
                        available = listOf("Spinach"),
                        missing = listOf("Milk")
                    ),
                    recipe(
                        title = "Milk Soup",
                        available = listOf("Milk"),
                        missing = emptyList()
                    )
                )
            )
        )

        val useCase = GenerateRecipeUiUseCase { extractor }
        val recipes = useCase.generateFromInventory(inventory = inventory)

        assertEquals(listOf("Cream Pasta", "Milk Soup"), recipes.map { it.title })
        assertTrue(extractor.lastPrompt!!.contains("Task:"))
        assertFalse(extractor.lastPrompt!!.contains("Preferred ingredients"))
        assertFalse(extractor.lastPrompt!!.contains("Avoid these ingredients"))
        assertFalse(extractor.lastPrompt!!.contains("Inventory-only mode"))
    }

    @Test
    fun `generateFromInventory returns extractor empty list unchanged`() = runBlocking {
        val extractor = FakeCloudFoodExtractor(
            RecipeSuggestionResult(recipes = emptyList())
        )

        val useCase = GenerateRecipeUiUseCase { extractor }
        val recipes = useCase.generateFromInventory(inventory = inventory)

        assertTrue(recipes.isEmpty())
    }

    private fun recipe(
        title: String,
        available: List<String>,
        missing: List<String>
    ): RecipeDto {
        return RecipeDto(
            title = title,
            description = "$title description",
            availableIngredients = available.map { RecipeIngredientDto(name = it, quantity = "1") },
            missingIngredients = missing.map { RecipeIngredientDto(name = it, quantity = "1") },
            instructions = listOf("Cook")
        )
    }
}

private class FakeCloudFoodExtractor(
    private val result: RecipeSuggestionResult
) : CloudFoodExtractor {
    var lastPrompt: String? = null

    override suspend fun suggestRecipes(
        promptText: String,
        onStatus: ((String) -> Unit)?
    ): RecipeSuggestionResult {
        lastPrompt = promptText
        return result
    }
}
