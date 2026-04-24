package my.edu.utar.freshtrackai.ai

import kotlinx.coroutines.test.runTest
import my.edu.utar.freshtrackai.logic.ShelfLifeRules
import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.ReviewItemUi
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanReviewEnricherTest {

    @Test
    fun `enrich replaces scan placeholders with resolved nutrition and expiry`() = runTest {
        val item = ReviewItemUi(
            id = "food-1",
            name = "Greek Yogurt",
            category = InventoryCategory.Dairy,
            quantityLabel = "1 unit",
            expiresLabel = "Estimated 5d",
            expiresInDays = 5,
            nutritionLabel = "Not available",
            thumbnailRef = "greek_yogurt"
        )

        val enriched = ScanReviewEnricher.enrich(
            items = listOf(item),
            nutritionEstimator = { name, categoryName ->
                assertEquals("Greek Yogurt", name)
                assertEquals("Dairy", categoryName)
                "61 kcal / 100g\nProtein: 10g"
            },
            expiryEstimator = { name, category ->
                assertEquals("Greek Yogurt", name)
                assertEquals(ShelfLifeRules.FoodCategory.DAIRY, category)
                14
            }
        ).single()

        assertEquals("61 kcal / 100g\nProtein: 10g", enriched.nutritionLabel)
        assertEquals("Estimated 14d", enriched.expiresLabel)
        assertEquals(14, enriched.expiresInDays)
    }

    @Test
    fun `enrich treats receipt nutrition placeholder as missing nutrition`() = runTest {
        val item = ReviewItemUi(
            id = "receipt-1",
            name = "Bread",
            category = InventoryCategory.Bakery,
            quantityLabel = "1 loaf",
            expiresLabel = "Estimated 4d",
            expiresInDays = 4,
            nutritionLabel = "OCR parsed from receipt",
            thumbnailRef = "bread"
        )

        val enriched = ScanReviewEnricher.enrich(
            items = listOf(item),
            nutritionEstimator = { _, _ -> "265 kcal / 100g" },
            expiryEstimator = { _, _ -> 6 }
        ).single()

        assertEquals("265 kcal / 100g", enriched.nutritionLabel)
        assertEquals("Estimated 6d", enriched.expiresLabel)
        assertEquals(6, enriched.expiresInDays)
    }

    @Test
    fun `enrich preserves existing nutrition and printed expiry date`() = runTest {
        val item = ReviewItemUi(
            id = "food-2",
            name = "Orange Juice",
            category = InventoryCategory.Beverages,
            quantityLabel = "1 bottle",
            expiresLabel = "Apr 30, 2026",
            expiresInDays = 5,
            nutritionLabel = "45 kcal / 100ml",
            thumbnailRef = "orange_juice"
        )

        val enriched = ScanReviewEnricher.enrich(
            items = listOf(item),
            nutritionEstimator = { _, _ -> error("nutrition estimator should not run") },
            expiryEstimator = { _, _ -> error("expiry estimator should not run") }
        ).single()

        assertEquals(item, enriched)
    }
}
