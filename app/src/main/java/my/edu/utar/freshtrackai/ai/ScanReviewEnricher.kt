package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.logic.NutritionAiHelper
import my.edu.utar.freshtrackai.logic.ShelfLifeRules
import my.edu.utar.freshtrackai.ui.dashboard.ReviewItemUi
import my.edu.utar.freshtrackai.ui.dashboard.toLogicCategory

internal object ScanReviewEnricher {

    suspend fun enrich(
        items: List<ReviewItemUi>,
        nutritionEstimator: suspend (String, String) -> String? = ::estimateNutrition,
        expiryEstimator: suspend (String, ShelfLifeRules.FoodCategory) -> Int = ::estimateExpiryDays
    ): List<ReviewItemUi> {
        return items.map { item ->
            var nutritionLabel = item.nutritionLabel
            if (NutritionAiHelper.shouldAutoFill(nutritionLabel)) {
                nutritionEstimator(item.name, item.category.label)
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { nutritionLabel = it }
            }

            var expiresLabel = item.expiresLabel
            var expiresInDays = item.expiresInDays
            if (shouldResolveExpiry(item.expiresLabel)) {
                val resolvedDays = expiryEstimator(item.name, item.category.toLogicCategory())
                if (resolvedDays > 0) {
                    expiresInDays = resolvedDays
                    expiresLabel = "Estimated ${resolvedDays}d"
                }
            }

            item.copy(
                nutritionLabel = nutritionLabel,
                expiresLabel = expiresLabel,
                expiresInDays = expiresInDays
            )
        }
    }

    private fun shouldResolveExpiry(label: String): Boolean {
        val cleaned = label.trim()
        return cleaned.isEmpty() ||
            cleaned.equals("Not set", ignoreCase = true) ||
            cleaned.startsWith("Estimated ", ignoreCase = true)
    }

    private suspend fun estimateNutrition(itemName: String, categoryName: String): String? {
        return when (val result = NutritionAiHelper.estimateNutritionByName(itemName, categoryName)) {
            is NutritionAiHelper.NutritionResult.Success -> result.text
            is NutritionAiHelper.NutritionResult.Failure -> null
        }
    }

    private suspend fun estimateExpiryDays(
        itemName: String,
        category: ShelfLifeRules.FoodCategory
    ): Int {
        return ShelfLifeRules.getShelfLifeByNameAI(itemName, category)
    }
}
