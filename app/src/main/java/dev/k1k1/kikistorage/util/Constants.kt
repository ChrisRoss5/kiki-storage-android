package dev.k1k1.kikistorage.util

object Constants {
    object Roots {
        const val DRIVE = "drive"
        const val STARRED = "starred"
        const val BIN = "bin"
    }

    const val DEFAULT_ROOT = Roots.DRIVE

    val ROOT_LIST = Roots::class.java.declaredFields.mapNotNull { field ->
        field.takeIf { it.type == String::class.java }?.get(Roots) as? String
    }
}
