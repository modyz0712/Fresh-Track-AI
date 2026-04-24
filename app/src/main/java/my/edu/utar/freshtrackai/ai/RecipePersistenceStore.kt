package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ui.dashboard.DashboardPreferencesStore
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi

internal interface RecipePersistenceStore {
    fun loadRecipes(): List<RecipeUi>
    fun saveRecipes(recipes: List<RecipeUi>)
    fun clearRecipes()
}

internal object AppRecipePersistenceStore : RecipePersistenceStore {
    override fun loadRecipes(): List<RecipeUi> {
        val context = AppContextProvider.get() ?: return emptyList()
        return DashboardPreferencesStore.loadGeneratedRecipes(context)
    }

    override fun saveRecipes(recipes: List<RecipeUi>) {
        val context = AppContextProvider.get() ?: return
        DashboardPreferencesStore.saveGeneratedRecipes(context, recipes)
    }

    override fun clearRecipes() {
        val context = AppContextProvider.get() ?: return
        DashboardPreferencesStore.clearGeneratedRecipes(context)
    }
}
