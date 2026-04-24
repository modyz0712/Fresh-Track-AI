package my.edu.utar.freshtrackai.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardPreferencesStoreTest {

    @Test
    fun `save load and clear gemini api key through preference store`() {
        val store = InMemoryDashboardPreferenceStore()

        DashboardPreferencesStore.saveGeminiApiKey(store, "  saved-key  ")
        assertEquals("saved-key", DashboardPreferencesStore.loadGeminiApiKey(store))

        DashboardPreferencesStore.clearGeminiApiKey(store)
        assertEquals("", DashboardPreferencesStore.loadGeminiApiKey(store))
    }

    @Test
    fun `save load and clear generated recipes through preference store`() {
        val store = InMemoryDashboardPreferenceStore()
        val recipes = listOf(
            RecipeUi(
                id = "recipe-1",
                title = "Saved Recipe",
                description = "Persisted recipe",
                prepMinutes = 15,
                imageUrl = null,
                pantryMatchText = "Uses 2 pantry items",
                tag = "Quick",
                usedInventoryItemIds = setOf("milk"),
                ingredientsAvailable = emptyList(),
                ingredientsMissing = emptyList(),
                steps = listOf("Cook"),
                avoidanceTokens = emptySet()
            )
        )

        DashboardPreferencesStore.saveGeneratedRecipes(store, recipes)
        assertEquals(
            listOf("Saved Recipe"),
            DashboardPreferencesStore.loadGeneratedRecipes(store).map { it.title }
        )

        DashboardPreferencesStore.clearGeneratedRecipes(store)
        assertEquals(emptyList<RecipeUi>(), DashboardPreferencesStore.loadGeneratedRecipes(store))
    }
}
