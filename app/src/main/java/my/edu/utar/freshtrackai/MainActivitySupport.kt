package my.edu.utar.freshtrackai

import my.edu.utar.freshtrackai.ai.GemmaModelStatus

internal fun shouldRequestGemmaModel(status: GemmaModelStatus): Boolean {
    return status != GemmaModelStatus.Configured
}
