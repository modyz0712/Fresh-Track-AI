package my.edu.utar.freshtrackai.ui.dashboard

import my.edu.utar.freshtrackai.data.local.entity.InventoryItem as LocalInventoryItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardDataTest {

    @Test
    fun `toUiModel exposes formatted added date from purchase date`() {
        val purchaseDate = Date(1_713_916_800_000L) // Apr 24, 2024 UTC-based epoch example
        val item = LocalInventoryItem(
            itemId = 1L,
            name = "Orange Juice",
            category = "Beverages",
            quantity = 1.0,
            unit = "bottle",
            purchaseDate = purchaseDate.time,
            expiryDate = purchaseDate.time + (14L * 24 * 60 * 60 * 1000)
        )

        val uiItem = item.toUiModel()
        val expected = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(purchaseDate)

        assertEquals(expected, uiItem.formattedAddedDate)
    }

    @Test
    fun `toUiModel normalizes placeholder unit into one unit label`() {
        val item = LocalInventoryItem(
            itemId = 1L,
            name = "Cake slice",
            category = "Bakery",
            quantity = 1.0,
            unit = "Detected item",
            purchaseDate = System.currentTimeMillis(),
            expiryDate = System.currentTimeMillis() + 86400000L
        )

        val uiItem = item.toUiModel()

        assertEquals("1 unit", uiItem.quantityLabel)
    }
}
