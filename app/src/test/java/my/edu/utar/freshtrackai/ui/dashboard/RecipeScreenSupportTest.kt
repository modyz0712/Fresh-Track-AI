package my.edu.utar.freshtrackai.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeScreenSupportTest {

    @Test
    fun `recipeHeroPreviewLabels prioritizes available ingredients before missing ones`() {
        val recipe = sampleRecipe(
            available = listOf("Spinach", "Garlic"),
            missing = listOf("Lemon", "Garlic", "Chili")
        )

        val labels = recipeHeroPreviewLabels(recipe, maxCount = 3)

        assertEquals(listOf("Spinach", "Garlic", "Lemon"), labels)
    }

    @Test
    fun `recipeHeroPreviewLabels ignores blank ingredient names`() {
        val recipe = sampleRecipe(
            available = listOf("  ", "Tomato"),
            missing = listOf("", "Basil")
        )

        val labels = recipeHeroPreviewLabels(recipe, maxCount = 3)

        assertEquals(listOf("Tomato", "Basil"), labels)
    }

    @Test
    fun `recipeAddMissingConfirmationText uses inline count copy`() {
        assertEquals("Added 2 item(s).", recipeAddMissingConfirmationText(2))
    }

    private fun sampleRecipe(
        available: List<String>,
        missing: List<String>
    ): RecipeUi {
        return RecipeUi(
            id = "recipe-1",
            title = "Green Bowl",
            description = "A quick lunch bowl.",
            prepMinutes = 18,
            imageUrl = null,
            pantryMatchText = "Uses pantry staples",
            tag = "Quick",
            usedInventoryItemIds = emptySet(),
            ingredientsAvailable = available.map {
                RecipeIngredientUi(name = it, isAvailable = true, quantityLabel = "1 cup")
            },
            ingredientsMissing = missing.map {
                RecipeIngredientUi(name = it, isAvailable = false, quantityLabel = "1 tbsp")
            },
            steps = listOf("Mix everything together."),
            avoidanceTokens = emptySet()
        )
    }
}
