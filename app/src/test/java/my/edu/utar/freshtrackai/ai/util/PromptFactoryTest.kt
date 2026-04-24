package my.edu.utar.freshtrackai.ai.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptFactoryTest {

    @Test
    fun `recipe prompt is plain inventory based without customization sections`() {
        val prompt = PromptFactory.recipePrompt(
            inventorySummary = "- Milk\n- Spinach"
        )

        assertTrue(prompt.contains("Inventory:"))
        assertTrue(prompt.contains("- Prefer recipes that consume soon-to-expire ingredients first."))
        assertFalse(prompt.contains("Preferred ingredients"))
        assertFalse(prompt.contains("Avoid these ingredients"))
        assertFalse(prompt.contains("Inventory-only mode"))
    }

    @Test
    fun `nutrition label prompt asks for unreadable sentinel and plain text`() {
        val prompt = PromptFactory.nutritionLabelPrompt()

        assertTrue(prompt.contains("UNREADABLE"))
        assertTrue(prompt.contains("Plain text only"))
    }

    @Test
    fun `nutrition estimate prompt includes item and category context`() {
        val prompt = PromptFactory.nutritionEstimatePrompt(
            itemName = "Milk",
            categoryName = "Dairy"
        )

        assertTrue(prompt.contains("\"Milk\""))
        assertTrue(prompt.contains("category: Dairy"))
        assertTrue(prompt.contains("per 100g or per 100ml"))
    }
}
