package dev.k1k1.kikistorage.firebase

import com.google.firebase.auth.FirebaseAuth

object Auth {
    val auth = FirebaseAuth.getInstance()

    fun getUid(): String? = auth.uid
}