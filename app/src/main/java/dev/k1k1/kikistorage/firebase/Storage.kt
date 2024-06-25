package dev.k1k1.kikistorage.firebase

import android.os.Environment
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.k1k1.kikistorage.model.Item
import kotlinx.coroutines.tasks.await
import java.io.File

object Storage {
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private const val CLOUD_DIR = "user"

    private fun getUserStorageReference(): StorageReference? {
        return Auth.getUid()?.let {
            firebaseStorage.getReference("$CLOUD_DIR/$it")
        }
    }

    suspend fun downloadItem(item: Item): String {
        val storageRef = getUserStorageReference()?.child(item.id!!)
        val localFile = File(downloadsDir, "${item.name}.${item.type}")
        storageRef?.getFile(localFile)?.await() ?: throw Exception("Download failed")
        return localFile.absolutePath
    }

    fun deleteItem(item: Item): Task<Void>? {
        val storageRef = getUserStorageReference()?.child(item.id!!)
        return storageRef?.delete()
    }
}