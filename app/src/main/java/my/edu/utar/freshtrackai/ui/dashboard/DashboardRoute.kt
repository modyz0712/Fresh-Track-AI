package my.edu.utar.freshtrackai.ui.dashboard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.ai.FoodExtractorProvider
import my.edu.utar.freshtrackai.ai.FoodReviewMapper
import my.edu.utar.freshtrackai.ai.RecipeGenerationViewModel
import my.edu.utar.freshtrackai.ai.ReceiptOcrProvider
import my.edu.utar.freshtrackai.ai.ReceiptReviewMapper
import my.edu.utar.freshtrackai.ai.ScanReviewEnricher
import my.edu.utar.freshtrackai.ai.ScanCaptureBitmapResolver
import my.edu.utar.freshtrackai.ui.inventory.InventoryViewModel
import my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme

@Composable
fun FreshTrackDashboardScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(WiseScreen.AppLauncher) }
    val appContext = LocalContext.current.applicationContext

    val inventoryVm: InventoryViewModel = viewModel(
        factory = InventoryViewModel.Factory(appContext)
    )
    val dbItems by inventoryVm.allItems.collectAsState()

    val inventory = remember(dbItems) {
        mutableStateListOf<InventoryItem>().apply { addAll(dbItems.map { it.toUiModel() }) }
    }
    val reviewItems = remember { mutableStateListOf<ReviewItemUi>() }

    val dbShoppingItems by inventoryVm.shoppingItems.collectAsState()
    val shoppingListItems = remember(dbShoppingItems) {
        mutableStateListOf<ShoppingListItemUi>().apply {
            addAll(dbShoppingItems.map { entity ->
                ShoppingListItemUi(
                    id = entity.itemId.toString(),
                    name = entity.name,
                    sourceRecipeId = entity.sourceRecipeId,
                    sourceRecipeName = entity.sourceRecipeName,
                    quantityCount = entity.quantityCount,
                    checked = entity.checked
                )
            })
        }
    }
    var expiringSearch by rememberSaveable { mutableStateOf("") }
    var expiringFilter by rememberSaveable { mutableStateOf<InventoryCategory?>(null) }
    var editingReviewItemId by remember { mutableStateOf<String?>(null) }
    var editingInventoryItemId by remember { mutableStateOf<String?>(null) }
    var addItemOrigin by remember { mutableStateOf(AddItemOrigin.ItemReview) }
    var addFormDraft by remember { mutableStateOf(AddItemFormDraft()) }
    var selectedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeAiTask by remember { mutableStateOf<DashboardAiTaskState?>(null) }
    var showScanGuide by remember { mutableStateOf(true) }
    var showRecipeGuide by remember { mutableStateOf(true) }
    var showListGuide by remember { mutableStateOf(true) }
    val recipeViewModel: RecipeGenerationViewModel = viewModel()
    val recipeUiState by recipeViewModel.uiState.collectAsState()

    fun refreshRecipes() {
        recipeViewModel.generateRecipes(
            inventory = inventory.toList()
        )
    }

    LaunchedEffect(recipeUiState.recipes) {
        if (recipeUiState.recipes.isNotEmpty()) {
            if (selectedRecipeId == null || recipeUiState.recipes.none { it.id == selectedRecipeId }) {
                selectedRecipeId = recipeUiState.recipes.first().id
            }
        }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(recipeUiState.errorMessage) {
        val message = recipeUiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        recipeViewModel.clearError()
    }

    LaunchedEffect(toastMessage) {
        val message = toastMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        toastMessage = null
    }

    val topBarController = remember(activeAiTask) {
        DashboardTopBarController(
            currentAiTask = activeAiTask,
            setAiTask = { activeAiTask = it }
        )
    }

    val toRootTab: (RootTab) -> Unit = { tab ->
        if (activeAiTask?.allowNavigationAway != false) {
            screen = when (tab) {
                RootTab.Home -> WiseScreen.MainDashboard
                RootTab.Scan -> WiseScreen.SmartScan
                RootTab.Recipe -> WiseScreen.AiRecipes
                RootTab.List -> WiseScreen.ShoppingList
            }
        }
    }

    CompositionLocalProvider(
        LocalDashboardTopBarController provides topBarController
    ) {
        Box(modifier = modifier.fillMaxSize()) {
        when (screen) {
            WiseScreen.AppLauncher -> AppLauncherScreen { screen = WiseScreen.MainDashboard }
            WiseScreen.MainDashboard -> MainDashboardScreen(
                inventory = inventory,
                onViewAllExpiring = { screen = WiseScreen.ExpiringSoonAll },
                onRecipe = { screen = WiseScreen.AiRecipes },
                onScan = { screen = WiseScreen.SmartScan },
                onAddItem = {
                    addItemOrigin = AddItemOrigin.Dashboard
                    editingInventoryItemId = null
                    editingReviewItemId = null
                    addFormDraft = AddItemFormDraft()
                    screen = WiseScreen.AddMissingItem
                },
                onEditItem = { item ->
                    addItemOrigin = AddItemOrigin.Dashboard
                    editingInventoryItemId = item.id
                    addFormDraft = AddItemFormDraft(
                        name = item.name,
                        quantity = item.quantityLabel,
                        category = item.category,
                        expiryDate = item.formattedExpiryDate,
                        nutritionNotes = item.nutritionNotes
                    )
                    screen = WiseScreen.AddMissingItem
                },
                onTabSelected = toRootTab
            )

            WiseScreen.ExpiringSoonAll -> ExpiringSoonAllScreen(
                inventory = inventory,
                searchQuery = expiringSearch,
                selectedCategory = expiringFilter,
                onSearchQueryChange = { expiringSearch = it },
                onCategoryChange = { expiringFilter = it },
                onRecipe = { screen = WiseScreen.AiRecipes },
                onMarkUsed = { inventoryId ->
                    inventoryId.toLongOrNull()?.let { inventoryVm.deleteItemById(it) }
                },
                onBack = { screen = WiseScreen.MainDashboard },
                onTabSelected = toRootTab
            )

            WiseScreen.SmartScan -> SmartScanScreen(
                onDone = { mode, capture ->
                    if (mode == ScanMode.Receipt) {
                        android.util.Log.d("RECEIPT_FLOW", "Mode = $mode")
                        scope.launch {
                            activeAiTask = DashboardAiTaskState(
                                title = "Reading receipt…",
                                detail = "Please stay on this page while AI finishes."
                            )
                            try {
                                val bitmap = ScanCaptureBitmapResolver.resolve(context, capture)
                                val parsed = ReceiptOcrProvider.get(context).parseReceipt(bitmap)
                                android.util.Log.d("RECEIPT_FLOW", "Parsed item count = ${parsed.items.size}")
                                android.util.Log.d("RECEIPT_FLOW", "Parsed items = ${parsed.items}")

                                val mapped = ScanReviewEnricher.enrich(
                                    ReceiptReviewMapper.map(parsed)
                                )
                                android.util.Log.d("RECEIPT_FLOW", "Mapped review count = ${mapped.size}")
                                android.util.Log.d("RECEIPT_FLOW", "Mapped review items = ${mapped.map { it.name }}")

                                reviewItems.clear()
                                reviewItems.addAll(mapped)

                                toastMessage = "Parsed ${mapped.size} item(s) from receipt."
                                screen = WiseScreen.ItemReview
                            } catch (e: Exception) {
                                toastMessage = e.message ?: "Failed to parse receipt."
                            } finally {
                                activeAiTask = null
                            }
                        }
                    } else {
                        android.util.Log.d("FOOD_FLOW", "Mode = $mode")
                        scope.launch {
                            activeAiTask = DashboardAiTaskState(
                                title = "Analyzing food photo…",
                                detail = "Please stay on this page while AI finishes."
                            )
                            try {
                                val bitmap = ScanCaptureBitmapResolver.resolve(context, capture)
                                val parsed = FoodExtractorProvider.get(context).detectFood(bitmap)
                                android.util.Log.d("FOOD_FLOW", "Parsed item count = ${parsed.items.size}")
                                android.util.Log.d("FOOD_FLOW", "Parsed items = ${parsed.items}")

                                val mapped = ScanReviewEnricher.enrich(
                                    FoodReviewMapper.map(parsed)
                                )
                                android.util.Log.d("FOOD_FLOW", "Mapped review count = ${mapped.size}")
                                android.util.Log.d("FOOD_FLOW", "Mapped review items = ${mapped.map { it.name }}")

                                reviewItems.clear()
                                reviewItems.addAll(mapped)

                                toastMessage = "Detected ${mapped.size} food item(s)."
                                screen = WiseScreen.ItemReview
                            } catch (e: Exception) {
                                toastMessage = e.message ?: "Failed to scan food image."
                            } finally {
                                activeAiTask = null
                            }
                        }
                    }
                },
                onTabSelected = toRootTab,
                showGuide = showScanGuide,
                onDismissGuide = { showScanGuide = false }
            )

            WiseScreen.AddMissingItem -> AddMissingItemScreen(
                draft = addFormDraft,
                isEditMode = editingReviewItemId != null || editingInventoryItemId != null,
                onDraftChange = { addFormDraft = it },
                onSubmit = {
                    coroutineScope.launch {
                        val existingItem = if (addItemOrigin == AddItemOrigin.ItemReview) {
                            reviewItems.firstOrNull { it.id == editingReviewItemId }
                        } else {
                            null
                        }

                        val needsAiCheck = requiresAiCheck(addFormDraft, existingItem)

                        try {
                            if (needsAiCheck) {
                                activeAiTask = DashboardAiTaskState(
                                    title = "Estimating item details…",
                                    detail = "Please stay on this page while AI finishes."
                                )
                            }
                            when (addItemOrigin) {
                                AddItemOrigin.ItemReview -> {
                                    val existing = reviewItems.firstOrNull { it.id == editingReviewItemId }
                                    val next = draftToReviewItem(
                                        draft = addFormDraft,
                                        existing = existing,
                                        forcedId = editingReviewItemId
                                    )
                                    val existingIndex = reviewItems.indexOfFirst { it.id == next.id }
                                    if (existingIndex >= 0) {
                                        reviewItems[existingIndex] = next
                                        toastMessage = "Scanned item updated."
                                    } else {
                                        reviewItems.add(next)
                                        toastMessage = "Missing item added."
                                    }
                                    addFormDraft = AddItemFormDraft()
                                    editingReviewItemId = null
                                    screen = WiseScreen.ItemReview
                                }

                                AddItemOrigin.Dashboard -> {
                                    if (editingInventoryItemId != null) {
                                        val numId = editingInventoryItemId!!.toLongOrNull()
                                        if (numId != null) {
                                            val updated = draftToInventoryItem(addFormDraft).copy(itemId = numId)
                                            inventoryVm.confirmAndEditItem(updated, updated.quantity, updated.expiryDate, updated.notes)
                                            toastMessage = "Food details updated."
                                        }
                                    } else {
                                        inventoryVm.insertItem(draftToInventoryItem(addFormDraft))
                                        toastMessage = "Item added to inventory."
                                    }
                                    addFormDraft = AddItemFormDraft()
                                    editingInventoryItemId = null
                                    screen = WiseScreen.MainDashboard
                                }
                            }
                        } finally {
                            if (needsAiCheck) {
                                activeAiTask = null
                            }
                        }
                    }
                },
                onDelete = {
                    coroutineScope.launch {
                        if (editingInventoryItemId != null) {
                            editingInventoryItemId!!.toLongOrNull()?.let { inventoryVm.deleteItemById(it) }
                            toastMessage = "Item deleted from inventory."
                            screen = WiseScreen.MainDashboard
                        } else if (editingReviewItemId != null) {
                            val idx = reviewItems.indexOfFirst { it.id == editingReviewItemId }
                            if (idx >= 0) {
                                reviewItems.removeAt(idx)
                            }
                            toastMessage = "Scanned item deleted."
                            screen = WiseScreen.ItemReview
                        }
                        editingInventoryItemId = null
                        editingReviewItemId = null
                        addFormDraft = AddItemFormDraft()
                    }
                },
                onBack = {
                    if (activeAiTask?.allowNavigationAway != false) {
                        screen = if (addItemOrigin == AddItemOrigin.ItemReview) {
                            WiseScreen.ItemReview
                        } else {
                            WiseScreen.MainDashboard
                        }
                    }
                },
                onTabSelected = toRootTab
            )

            WiseScreen.ItemReview -> ItemReviewScreen(
                reviewItems = reviewItems,
                onAddMissingItem = {
                    addItemOrigin = AddItemOrigin.ItemReview
                    editingReviewItemId = null
                    addFormDraft = AddItemFormDraft()
                    screen = WiseScreen.AddMissingItem
                },
                onEditItem = { reviewId ->
                    val current = reviewItems.firstOrNull { it.id == reviewId }
                    if (current != null) {
                        addItemOrigin = AddItemOrigin.ItemReview
                        editingReviewItemId = reviewId
                        addFormDraft = current.toDraft()
                        screen = WiseScreen.AddMissingItem
                    }
                },
                onDeleteItem = { reviewId ->
                    val idx = reviewItems.indexOfFirst { it.id == reviewId }
                    if (idx >= 0) {
                        reviewItems.removeAt(idx)
                        toastMessage = "Scanned item removed."
                    }
                },
                onSaveToInventory = {
                    if (reviewItems.isNotEmpty()) {
                        inventoryVm.insertBulk(reviewItems.map { it.toInventoryItem() })
                        val savedCount = reviewItems.size
                        reviewItems.clear()
                        toastMessage = "$savedCount item(s) saved to inventory."
                        screen = WiseScreen.MainDashboard
                    }
                },
                onTabSelected = toRootTab
            )

            WiseScreen.ShoppingList -> ShoppingListScreen(
                items = shoppingListItems,
                onItemCheckedChange = { itemId, checked ->
                    val dbItem = dbShoppingItems.firstOrNull { it.itemId.toString() == itemId }
                    if (dbItem != null) {
                        inventoryVm.toggleShoppingItem(dbItem, checked)
                    }
                },
                onAddGeneralItem = { itemName ->
                    inventoryVm.addShoppingItem(name = itemName)
                    toastMessage = "Added to shopping list."
                },
                onClearPurchased = {
                    val removed = dbShoppingItems.count { it.checked }
                    inventoryVm.clearPurchasedShoppingItems()
                    toastMessage = if (removed > 0) {
                        "Cleared $removed purchased item(s)."
                    } else {
                        "No purchased items to clear."
                    }
                    removed
                },
                onTabSelected = toRootTab,
                showGuide = showListGuide,
                onDismissGuide = { showListGuide = false }
            )

            WiseScreen.RecipeDetails -> {
                val recipe = recipeUiState.recipes.firstOrNull { it.id == selectedRecipeId }
                    ?: recipeUiState.recipes.firstOrNull()

                if (recipe == null) {
                    LaunchedEffect(Unit) {
                        screen = WiseScreen.AiRecipes
                    }
                    return@Box
                }
                RecipeDetailsScreen(
                    recipe = recipe,
                    onAddMissingToShoppingList = { current ->
                        var affected = 0
                        current.ingredientsMissing.forEach { ingredient ->
                            inventoryVm.addShoppingItem(
                                name = ingredient.name,
                                sourceRecipeId = current.id,
                                sourceRecipeName = current.title
                            )
                            affected++
                        }
                        affected
                    },
                    onOpenList = { screen = WiseScreen.ShoppingList },
                    onBack = {
                        if (activeAiTask?.allowNavigationAway != false) {
                            screen = WiseScreen.AiRecipes
                        }
                    },
                    onTabSelected = toRootTab
                )
            }

            WiseScreen.AiRecipes -> AiRecipesScreen(
                recipes = recipeUiState.recipes,
                loading = recipeUiState.isLoading,
                loadingMessage = recipeUiState.processingMessage,
                onGenerate = { refreshRecipes() },
                onOpenRecipe = { recipeId ->
                    selectedRecipeId = recipeId
                    screen = WiseScreen.RecipeDetails
                },
                onTabSelected = toRootTab,
                showGuide = showRecipeGuide,
                onDismissGuide = { showRecipeGuide = false }
            )
        }

        activeAiTask?.let { task ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                AiProcessingOverlay(state = task)
            }
        }
    }
    }

    val handleDeviceBack: (() -> Unit)? = when (screen) {
        WiseScreen.ExpiringSoonAll -> ({ screen = WiseScreen.MainDashboard })
        WiseScreen.ItemReview -> ({ screen = WiseScreen.SmartScan })
        WiseScreen.AddMissingItem -> ({
            screen = if (addItemOrigin == AddItemOrigin.ItemReview) {
                WiseScreen.ItemReview
            } else {
                WiseScreen.MainDashboard
            }
        })
        WiseScreen.RecipeDetails -> ({ screen = WiseScreen.AiRecipes })
        WiseScreen.SmartScan, WiseScreen.AiRecipes, WiseScreen.ShoppingList -> ({ screen = WiseScreen.MainDashboard })
        WiseScreen.MainDashboard, WiseScreen.AppLauncher -> null
    }

    BackHandler(enabled = handleDeviceBack != null && activeAiTask?.allowNavigationAway != false) {
        handleDeviceBack?.invoke()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewFreshTrackDashboard() {
    FreshTrackAITheme {
        FreshTrackDashboardScreen()
    }
}
