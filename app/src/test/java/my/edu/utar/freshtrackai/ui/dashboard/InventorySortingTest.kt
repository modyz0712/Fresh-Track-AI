package my.edu.utar.freshtrackai.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class InventorySortingTest {

    private val inventory = listOf(
        InventoryItem(
            id = "3",
            name = "Banana",
            category = InventoryCategory.Fruits,
            quantityLabel = "1 bunch",
            addedDaysAgo = 2,
            formattedAddedDate = "Apr 22, 2026",
            expiresInDays = 5,
            thumbnailRef = "banana",
            purchaseDateMillis = 1_713_484_800_000L
        ),
        InventoryItem(
            id = "1",
            name = "Milk",
            category = InventoryCategory.Dairy,
            quantityLabel = "1 bottle",
            addedDaysAgo = 1,
            formattedAddedDate = "Apr 23, 2026",
            expiresInDays = 3,
            thumbnailRef = "milk",
            purchaseDateMillis = 1_713_571_200_000L
        ),
        InventoryItem(
            id = "2",
            name = "Eggs",
            category = InventoryCategory.Eggs,
            quantityLabel = "1 unit",
            addedDaysAgo = 0,
            formattedAddedDate = "Apr 24, 2026",
            expiresInDays = 21,
            thumbnailRef = "eggs",
            purchaseDateMillis = 1_713_657_600_000L
        )
    )

    @Test
    fun `sortInventory returns recently added first`() {
        val sorted = sortInventory(inventory, InventorySortMode.RecentlyAdded)

        assertEquals(listOf("Eggs", "Milk", "Banana"), sorted.map { it.name })
    }

    @Test
    fun `sortInventory returns expiry soonest first`() {
        val sorted = sortInventory(inventory, InventorySortMode.ExpirySoonest)

        assertEquals(listOf("Milk", "Banana", "Eggs"), sorted.map { it.name })
    }

    @Test
    fun `sortInventory returns alphabetical order`() {
        val sorted = sortInventory(inventory, InventorySortMode.NameAZ)

        assertEquals(listOf("Banana", "Eggs", "Milk"), sorted.map { it.name })
    }
}
