package my.edu.utar.freshtrackai.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GemmaModelDownloadManagerTest {

    @Test
    fun `downloadSpec uses direct Hugging Face file URL and expected filename`() {
        val spec = GemmaModelDownloadManager.downloadSpec()

        assertEquals(
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
            spec.url
        )
        assertEquals("gemma-4-E2B-it.litertlm", spec.fileName)
        assertTrue(spec.title.contains("Gemma 4"))
        assertTrue(spec.description.contains("Downloads"))
    }

    @Test
    fun `running download status message shows percent`() {
        val message = gemmaDownloadStatusMessage(
            GemmaModelDownloadStatus.Running(
                downloadedBytes = 50,
                totalBytes = 100
            )
        )

        assertEquals("Downloading Gemma 4… 50%", message)
    }
}
