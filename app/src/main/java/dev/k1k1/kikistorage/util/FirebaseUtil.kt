package dev.k1k1.kikistorage.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.k1k1.kikistorage.model.Item
import java.io.File

object FirebaseUtil {
    private val auth = FirebaseAuth.getInstance()
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()

    fun getUserDriveCollection(): CollectionReference? {
        val uid = auth.uid ?: return null
        return firestoreDb.collection("app/drive/$uid")
    }

    fun getUserStorageReference(): StorageReference? {
        val uid = auth.uid ?: return null
        return firebaseStorage.getReference("user/$uid")
    }

    private fun getDoc(item: Item): DocumentReference? {
        val uid = auth.uid ?: return null
        return item.id?.let { firestoreDb.collection("app/drive/$uid").document(it) }
    }

    fun createItem(item: Item, itemDoc: DocumentReference? = null) {
        itemDoc?.set(item) ?: run {
            getUserDriveCollection()?.add(item)
        }
        updateParentDateModified(item)
    }

    fun updateItem(item: Item, fields: Map<String, Any>) {
        getDoc(item)?.update(fields)
    }

    fun deleteItem(item: Item) {
        getDoc(item)?.delete()
    }

    fun starItem(item: Item, isStarred: Boolean) {
        updateItem(item, mapOf("isStarred" to isStarred))
    }

    fun moveItem(item: Item, newPath: String) {
        val updates = mapOf("path" to newPath, "dateModified" to Timestamp.now())
        updateItem(item, updates)
    }

    fun deleteItemPermanently(item: Item) {
        val storageRef = getUserStorageReference()?.child(item.path)
        storageRef?.delete()
        deleteItem(item)
    }

    private fun updateParentDateModified(item: Item) {
        val pathSegments = item.path.split("/")
        if (pathSegments.size > 1) {
            val parentPath = pathSegments.dropLast(1).joinToString("/")
            val parentItemQuery =
                getUserDriveCollection()?.whereEqualTo("path", parentPath)?.limit(1)
            parentItemQuery?.get()?.addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.firstOrNull()?.reference?.update(
                    "dateModified",
                    Timestamp.now()
                )
            }
        }
    }

    fun downloadItem(context: Context, item: Item) {
        val storageRef = FirebaseStorage.getInstance()
            .getReference("user/${FirebaseAuth.getInstance().uid}/${item.id}")
        val localFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "${item.name}.${item.type}"
        )
        storageRef.getFile(localFile).addOnSuccessListener {
            Toast.makeText(context, "Downloaded to ${localFile.absolutePath}", Toast.LENGTH_LONG)
                .show()
        }.addOnFailureListener {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }
}