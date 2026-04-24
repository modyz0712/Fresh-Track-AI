package my.edu.utar.freshtrackai.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import my.edu.utar.freshtrackai.data.local.dao.InventoryDao
import my.edu.utar.freshtrackai.data.local.dao.ShoppingDao
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem
import my.edu.utar.freshtrackai.data.local.entity.ShoppingItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InventoryRepositoryTest {

    @Test
    fun `addOrMergeShoppingItem inserts normalized item when name is new`() = runBlocking {
        val shoppingDao = FakeShoppingDao()
        val repository = InventoryRepository(FakeInventoryDao(), shoppingDao)

        repository.addOrMergeShoppingItem(
            name = " Milk ",
            sourceRecipeId = "recipe-1",
            sourceRecipeName = "Cream Soup"
        )

        val stored = shoppingDao.snapshot().single()
        assertEquals(" Milk ", stored.name)
        assertEquals("milk", stored.normalizedName)
        assertEquals(1, stored.quantityCount)
        assertEquals("recipe-1", stored.sourceRecipeId)
        assertEquals("Cream Soup", stored.sourceRecipeName)
    }

    @Test
    fun `addOrMergeShoppingItem increments quantity and merges recipe label for duplicates`() = runBlocking {
        val shoppingDao = FakeShoppingDao()
        val repository = InventoryRepository(FakeInventoryDao(), shoppingDao)

        repository.addOrMergeShoppingItem(
            name = "Milk",
            sourceRecipeId = "recipe-1",
            sourceRecipeName = "Cream Soup"
        )

        val existing = shoppingDao.snapshot().single()
        shoppingDao.updateItem(existing.copy(checked = true))

        repository.addOrMergeShoppingItem(
            name = " milk ",
            sourceRecipeId = "recipe-2",
            sourceRecipeName = "Pasta Bake"
        )

        val merged = shoppingDao.snapshot().single()
        assertEquals(1, shoppingDao.snapshot().size)
        assertEquals("milk", merged.normalizedName)
        assertEquals(2, merged.quantityCount)
        assertEquals("recipe-1", merged.sourceRecipeId)
        assertEquals("Multiple Recipes", merged.sourceRecipeName)
        assertFalse(merged.checked)
    }
}

private class FakeInventoryDao : InventoryDao {
    override suspend fun insertItem(item: InventoryItem): Long = item.itemId

    override suspend fun updateItem(item: InventoryItem) = Unit

    override suspend fun deleteItem(item: InventoryItem) = Unit

    override fun getAllItems(): Flow<List<InventoryItem>> = flowOf(emptyList())

    override fun getItemById(id: Long): Flow<InventoryItem?> = flowOf(null)

    override fun getItemsByExpiryStatus(status: String): Flow<List<InventoryItem>> = flowOf(emptyList())

    override fun searchItemsByName(query: String): Flow<List<InventoryItem>> = flowOf(emptyList())
}

private class FakeShoppingDao : ShoppingDao {
    private val items = mutableListOf<ShoppingItemEntity>()
    private var nextId = 1L

    override suspend fun insertItem(item: ShoppingItemEntity): Long {
        val stored = item.copy(itemId = nextId++)
        items.add(stored)
        return stored.itemId
    }

    override suspend fun updateItem(item: ShoppingItemEntity) {
        val index = items.indexOfFirst { it.itemId == item.itemId }
        if (index >= 0) {
            items[index] = item
        }
    }

    override suspend fun deleteItem(item: ShoppingItemEntity) {
        items.removeAll { it.itemId == item.itemId }
    }

    override fun getAllShoppingItems(): Flow<List<ShoppingItemEntity>> = flowOf(items.toList())

    override suspend fun getItemByNormalizedName(normalizedName: String): ShoppingItemEntity? {
        return items.firstOrNull { it.normalizedName == normalizedName }
    }

    override suspend fun clearPurchasedItems() {
        items.removeAll { it.checked }
    }

    fun snapshot(): List<ShoppingItemEntity> = items.toList()
}
