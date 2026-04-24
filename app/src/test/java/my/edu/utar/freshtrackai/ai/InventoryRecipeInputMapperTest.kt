package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryRecipeInputMapperTest {

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
            expiresInDays = 3,
            thumbnailRef = "spinach"
        ),
        InventoryItem(
            id = "rice",
            name = "Rice",
            category = InventoryCategory.GrainsPasta,
            quantityLabel = "1 bag",
            addedDaysAgo = 3,
            expiresInDays = 40,
            thumbnailRef = "rice"
        )
    )

    @Test
    fun `map includes all inventory names in original order`() {
        val input = InventoryRecipeInputMapper.map(inventory)

        assertEquals(listOf("Milk", "Spinach", "Rice"), input.allItemNames)
    }

    @Test
    fun `map keeps input minimal with no recipe customization fields`() {
        val input = InventoryRecipeInputMapper.map(inventory)

        assertTrue(input.preferredItemNames.isEmpty())
        assertTrue(input.avoidanceTokens.isEmpty())
        assertEquals(false, input.inventoryOnly)
    }
}
