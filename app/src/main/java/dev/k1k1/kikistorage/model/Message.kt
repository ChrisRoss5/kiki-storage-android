package dev.k1k1.kikistorage.model

import android.graphics.drawable.Drawable

data class Message(
    var text: String,
    val sender: String,
    val image: Drawable,
)
