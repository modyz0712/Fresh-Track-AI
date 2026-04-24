package my.edu.utar.freshtrackai.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi

/**
 * Manages recipe generation state for the UI.
 * It handles loading, progress updates, results, and error messages.
 */
internal class RecipeGenerationViewModel(
    private val useCase: GenerateRecipeUiUseCase = GenerateRecipeUiUseCase(),
    private val recipeStore: RecipePersistenceStore = AppRecipePersistenceStore,
    private val initialRecipes: List<RecipeUi> = recipeStore.loadRecipes(),
    private val recipeGenerator: suspend (
        inventory: List<InventoryItem>,
        onStatus: ((String) -> Unit)?
    ) -> List<RecipeUi> = { inventory, onStatus ->
        useCase.generateFromInventory(
            inventory = inventory,
            onStatus = onStatus
        )
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RecipeGenerationUiState(recipes = initialRecipes)
    )
    val uiState: StateFlow<RecipeGenerationUiState> = _uiState.asStateFlow()

    // Starts recipe generation using the current inventory.
    fun generateRecipes(inventory: List<InventoryItem>) {
        if (inventory.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                recipes = _uiState.value.recipes,
                errorMessage = "No inventory items available.",
                processingMessage = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                processingMessage = "Preparing inventory summary..."
            )

            try {
                val recipes = recipeGenerator(
                    inventory,
                    { status ->
                        _uiState.value = _uiState.value.copy(processingMessage = status)
                    }
                )

                recipeStore.saveRecipes(recipes)
                _uiState.value = RecipeGenerationUiState(
                    isLoading = false,
                    recipes = recipes,
                    errorMessage = null,
                    processingMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recipes = _uiState.value.recipes,
                    errorMessage = e.message ?: "Failed to generate recipes.",
                    processingMessage = null
                )
            }
        }
    }

    // Clears the current error message after it has been shown to the user.
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    internal fun seedRecipesForTest(recipes: List<RecipeUi>) {
        recipeStore.saveRecipes(recipes)
        _uiState.value = _uiState.value.copy(recipes = recipes)
    }
}
