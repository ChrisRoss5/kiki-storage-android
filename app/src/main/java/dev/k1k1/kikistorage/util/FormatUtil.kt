package dev.k1k1.kikistorage.util

import kotlin.math.ln
import kotlin.math.pow

object FormatUtil {
    fun formatSize(bytes: Long): String {
        if (bytes.toInt() == 0) return "0 B"
        val i = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
        val decimals = when {
            i > 2 -> 2
            i > 1 -> 1
            else -> 0
        }
        val size = bytes / 1024.0.pow(i.toDouble())
        val units = listOf("B", "KB", "MB", "GB", "TB")
        return String.format("%.${decimals}f %s", size, units[i])
    }
}