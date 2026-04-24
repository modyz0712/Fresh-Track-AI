package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.model.ExpiryInfoDto
import my.edu.utar.freshtrackai.ai.model.FoodDetectionResult
import my.edu.utar.freshtrackai.ai.model.FoodItemDto
import my.edu.utar.freshtrackai.ai.model.QuantityDto
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodReviewMapperTest {

    @Test
    fun `map uses neutral nutrition label for food scan results`() {
        val reviewItem = FoodReviewMapper.map(
            FoodDetectionResult(
                items = listOf(
                    FoodItemDto(
                        name = "Pineapple",
                        expiry = ExpiryInfoDto(estimatedShelfLifeDays = 5)
                    )
                )
            )
        ).single()

        assertEquals("Not available", reviewItem.nutritionLabel)
    }

    @Test
    fun `map normalizes missing quantity to one unit`() {
        val reviewItem = FoodReviewMapper.map(
            FoodDetectionResult(
                items = listOf(
                    FoodItemDto(
                        name = "Pineapple",
                        quantity = null
                    )
                )
            )
        ).single()

        assertEquals("1 unit", reviewItem.quantityLabel)
    }

    @Test
    fun `map normalizes placeholder raw quantity text to one unit`() {
        val reviewItem = FoodReviewMapper.map(
            FoodDetectionResult(
                items = listOf(
                    FoodItemDto(
                        name = "Bread",
                        quantity = QuantityDto(raw = "Detected item")
                    )
                )
            )
        ).single()

        assertEquals("1 unit", reviewItem.quantityLabel)
    }

    @Test
    fun `map keeps numeric quantity and adds unit fallback when unit is missing`() {
        val reviewItem = FoodReviewMapper.map(
            FoodDetectionResult(
                items = listOf(
                    FoodItemDto(
                        name = "Fish",
                        quantity = QuantityDto(value = 2.0, unit = null, raw = null)
                    )
                )
            )
        ).single()

        assertEquals("2 units", reviewItem.quantityLabel)
    }
}
