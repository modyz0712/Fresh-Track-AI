package my.edu.utar.freshtrackai.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

internal interface DashboardPreferenceStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

internal class InMemoryDashboardPreferenceStore : DashboardPreferenceStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String): String? = values[key]

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}

private class SharedPreferencesDashboardPreferenceStore(
    private val sharedPreferences: SharedPreferences
) : DashboardPreferenceStore {
    override fun getString(key: String): String? = sharedPreferences.getString(key, null)

    override fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}

internal object DashboardPreferencesStore {
    private const val PREFS_NAME = "dashboard_prefs"
    private const val KEY_INVENTORY_SORT_MODE = "inventory_sort_mode"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_GEMMA_DOWNLOAD_ID = "gemma_download_id"
    private const val KEY_GENERATED_RECIPES = "generated_recipes"
    private val gson = Gson()

    private fun store(context: Context): DashboardPreferenceStore {
        return SharedPreferencesDashboardPreferenceStore(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
    }

    fun loadInventorySortMode(context: Context): InventorySortMode {
        return loadInventorySortMode(store(context))
    }

    internal fun loadInventorySortMode(store: DashboardPreferenceStore): InventorySortMode {
        val raw = store.getString(KEY_INVENTORY_SORT_MODE)
            ?: InventorySortMode.Category.name
        return InventorySortMode.entries.firstOrNull { it.name == raw } ?: InventorySortMode.Category
    }

    fun saveInventorySortMode(context: Context, mode: InventorySortMode) {
        saveInventorySortMode(store(context), mode)
    }

    internal fun saveInventorySortMode(store: DashboardPreferenceStore, mode: InventorySortMode) {
        store.putString(KEY_INVENTORY_SORT_MODE, mode.name)
    }

    fun loadGeminiApiKey(context: Context): String {
        return loadGeminiApiKey(store(context))
    }

    internal fun loadGeminiApiKey(store: DashboardPreferenceStore): String {
        return store.getString(KEY_GEMINI_API_KEY).orEmpty().trim()
    }

    fun saveGeminiApiKey(context: Context, apiKey: String) {
        saveGeminiApiKey(store(context), apiKey)
    }

    internal fun saveGeminiApiKey(store: DashboardPreferenceStore, apiKey: String) {
        store.putString(KEY_GEMINI_API_KEY, apiKey.trim())
    }

    fun clearGeminiApiKey(context: Context) {
        clearGeminiApiKey(store(context))
    }

    internal fun clearGeminiApiKey(store: DashboardPreferenceStore) {
        store.remove(KEY_GEMINI_API_KEY)
    }

    fun loadGemmaDownloadId(context: Context): Long? {
        return loadGemmaDownloadId(store(context))
    }

    internal fun loadGemmaDownloadId(store: DashboardPreferenceStore): Long? {
        return store.getString(KEY_GEMMA_DOWNLOAD_ID)?.toLongOrNull()
    }

    fun saveGemmaDownloadId(context: Context, downloadId: Long) {
        saveGemmaDownloadId(store(context), downloadId)
    }

    internal fun saveGemmaDownloadId(store: DashboardPreferenceStore, downloadId: Long) {
        store.putString(KEY_GEMMA_DOWNLOAD_ID, downloadId.toString())
    }

    fun clearGemmaDownloadId(context: Context) {
        clearGemmaDownloadId(store(context))
    }

    internal fun clearGemmaDownloadId(store: DashboardPreferenceStore) {
        store.remove(KEY_GEMMA_DOWNLOAD_ID)
    }

    fun loadGeneratedRecipes(context: Context): List<RecipeUi> {
        return loadGeneratedRecipes(store(context))
    }

    internal fun loadGeneratedRecipes(store: DashboardPreferenceStore): List<RecipeUi> {
        val raw = store.getString(KEY_GENERATED_RECIPES).orEmpty()
        if (raw.isBlank()) {
            return emptyList()
        }

        return runCatching {
            gson.fromJson(raw, Array<RecipeUi>::class.java)?.toList().orEmpty()
        }.getOrElse {
            emptyList()
        }
    }

    fun saveGeneratedRecipes(context: Context, recipes: List<RecipeUi>) {
        saveGeneratedRecipes(store(context), recipes)
    }

    internal fun saveGeneratedRecipes(
        store: DashboardPreferenceStore,
        recipes: List<RecipeUi>
    ) {
        store.putString(KEY_GENERATED_RECIPES, gson.toJson(recipes))
    }

    fun clearGeneratedRecipes(context: Context) {
        clearGeneratedRecipes(store(context))
    }

    internal fun clearGeneratedRecipes(store: DashboardPreferenceStore) {
        store.remove(KEY_GENERATED_RECIPES)
    }
}
