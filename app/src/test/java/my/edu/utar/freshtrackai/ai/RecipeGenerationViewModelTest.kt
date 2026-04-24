package my.edu.utar.freshtrackai.ai

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeGenerationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val inventory = listOf(
        InventoryItem(
            id = "milk",
            name = "Milk",
            category = InventoryCategory.Dairy,
            quantityLabel = "1 bottle",
            addedDaysAgo = 1,
            expiresInDays = 2,
            thumbnailRef = "milk"
        )
    )

    @Test
    fun `initial state loads persisted recipes from store`() = runTest {
        val persistedRecipe = recipe("Persisted Recipe")
        val store = InMemoryRecipePersistenceStore(listOf(persistedRecipe))

        val viewModel = RecipeGenerationViewModel(
            recipeStore = store
        )

        assertEquals(listOf("Persisted Recipe"), viewModel.uiState.value.recipes.map { it.title })
    }

    @Test
    fun `generateRecipes keeps previous recipes visible while loading and replaces them on success`() = runTest {
        val gate = CompletableDeferred<List<RecipeUi>>()
        val oldRecipe = recipe("Old Recipe")
        val newRecipe = recipe("New Recipe")
        val store = InMemoryRecipePersistenceStore(listOf(oldRecipe))

        val viewModel = RecipeGenerationViewModel(
            recipeStore = store,
            recipeGenerator = { _, _ ->
                gate.await()
            }
        )

        viewModel.generateRecipes(inventory)

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(listOf("Old Recipe"), viewModel.uiState.value.recipes.map { it.title })

        gate.complete(listOf(newRecipe))
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf("New Recipe"), viewModel.uiState.value.recipes.map { it.title })
        assertEquals(listOf("New Recipe"), store.loadRecipes().map { it.title })
    }

    @Test
    fun `generateRecipes keeps previous recipes when generation fails`() = runTest {
        val oldRecipe = recipe("Stable Recipe")
        val store = InMemoryRecipePersistenceStore(listOf(oldRecipe))

        val viewModel = RecipeGenerationViewModel(
            recipeStore = store,
            recipeGenerator = { _, _ -> error("network down") }
        )

        viewModel.generateRecipes(inventory)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf("Stable Recipe"), viewModel.uiState.value.recipes.map { it.title })
        assertEquals("network down", viewModel.uiState.value.errorMessage)
        assertEquals(listOf("Stable Recipe"), store.loadRecipes().map { it.title })
    }

    @Test
    fun `generateRecipes keeps previous recipes when inventory is empty`() = runTest {
        val oldRecipe = recipe("Stable Recipe")
        val store = InMemoryRecipePersistenceStore(listOf(oldRecipe))

        val viewModel = RecipeGenerationViewModel(
            recipeStore = store,
            recipeGenerator = { _, _ -> error("should not run") }
        )

        viewModel.generateRecipes(emptyList())

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf("Stable Recipe"), viewModel.uiState.value.recipes.map { it.title })
        assertEquals("No inventory items available.", viewModel.uiState.value.errorMessage)
        assertEquals(listOf("Stable Recipe"), store.loadRecipes().map { it.title })
    }

    private fun recipe(title: String): RecipeUi {
        return RecipeUi(
            id = title.lowercase().replace(" ", "-"),
            title = title,
            description = "$title description",
            prepMinutes = 10,
            imageUrl = null,
            pantryMatchText = "Uses 1 pantry item",
            tag = "Quick",
            usedInventoryItemIds = setOf("milk"),
            ingredientsAvailable = emptyList(),
            ingredientsMissing = emptyList(),
            steps = listOf("Cook"),
            avoidanceTokens = emptySet()
        )
    }

    private class InMemoryRecipePersistenceStore(
        recipes: List<RecipeUi> = emptyList()
    ) : RecipePersistenceStore {
        private var storedRecipes: List<RecipeUi> = recipes

        override fun loadRecipes(): List<RecipeUi> = storedRecipes

        override fun saveRecipes(recipes: List<RecipeUi>) {
            storedRecipes = recipes
        }

        override fun clearRecipes() {
            storedRecipes = emptyList()
        }
    }
}
