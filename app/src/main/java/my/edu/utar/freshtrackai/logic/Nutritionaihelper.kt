package my.edu.utar.freshtrackai.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NutritionAiHelper {
    private const val TAG = "NutritionAiHelper"

    sealed class NutritionResult {
        data class Success(val text: String) : NutritionResult()
        data class Failure(val reason: String) : NutritionResult()
    }

    suspend fun getNutritionFromLabel(bitmap: Bitmap): NutritionResult =
        withContext(Dispatchers.IO) {
            LocalNutritionService.readLabel(bitmap).toNutritionResult()
        }

    suspend fun getNutritionFromLabelUri(context: Context, uri: Uri): NutritionResult =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                getNutritionFromLabel(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "getNutritionFromLabelUri failed", e)
                NutritionResult.Failure("Could not load image: ${e.message ?: "unknown error"}")
            }
        }

    suspend fun estimateNutritionByName(
        itemName: String,
        categoryName: String = ""
    ): NutritionResult = withContext(Dispatchers.IO) {
        LocalNutritionService.estimateByName(
            itemName = itemName,
            categoryName = categoryName
        ).toNutritionResult()
    }

    fun shouldAutoFill(nutritionNotes: String): Boolean {
        val cleaned = nutritionNotes.trim().lowercase()
        return cleaned.isBlank() ||
            cleaned == "not provided" ||
            cleaned == "not available" ||
            cleaned == "ocr parsed from receipt" ||
            cleaned.startsWith("quick scan label (mock)") ||
            cleaned.startsWith("e.g.")
    }

    private fun Result<String>.toNutritionResult(): NutritionResult {
        return fold(
            onSuccess = { NutritionResult.Success(it) },
            onFailure = { throwable ->
                Log.e(TAG, "Local nutrition request failed", throwable)
                NutritionResult.Failure(
                    throwable.message ?: "Local nutrition estimate failed. Try entering it manually."
                )
            }
        )
    }
}
