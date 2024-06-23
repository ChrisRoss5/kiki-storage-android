package dev.k1k1.kikistorage.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtil {
    fun getUserDriveCollection(): CollectionReference? {
        val uid = FirebaseAuth.getInstance().uid ?: return null
        return FirebaseFirestore.getInstance().collection("app/drive/$uid")
    }
}
