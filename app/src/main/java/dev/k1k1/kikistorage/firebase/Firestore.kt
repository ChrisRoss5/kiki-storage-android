package dev.k1k1.kikistorage.firebase

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.Constants

object Firestore {
    private val firestoreDb: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()
    private const val CLOUD_DIR = "app/drive"

    fun getUserDriveCollection(): CollectionReference? {
        return Auth.getUid()?.let {
            firestoreDb.collection("$CLOUD_DIR/$it")
        }
    }

    private fun getDoc(item: Item): DocumentReference? {
        return item.id?.let { getUserDriveCollection()?.document(it) }
    }

    fun getItems(path: String): Task<QuerySnapshot>? {
        return getUserDriveCollection()?.whereEqualTo(Item::path.name, path)?.get()
    }

    fun getItemsWithStartingPath(path: String): Task<QuerySnapshot>? {
        return getUserDriveCollection()
            ?.whereGreaterThanOrEqualTo(Item::path.name, path)
            ?.whereLessThanOrEqualTo(Item::path.name, "$path\uf8ff")?.get()
    }

    fun createItem(item: Item, itemDoc: DocumentReference? = null) {
        itemDoc?.set(item) ?: run {
            getUserDriveCollection()?.add(item)
        }
        updateParentDateModified(item)
    }

    fun deleteItem(item: Item): Task<MutableList<Task<*>>> {
        updateParentDateModified(item)
        return moveItem(item, Constants.Roots.BIN)
    }

    fun deleteItemPermanently(item: Item): Task<List<Task<*>>> {
        val taskCompletionSource = TaskCompletionSource<List<Task<*>>>()
        val tasks = mutableListOf<Task<*>>(deleteItemPermanentlyAction(item))
        if (item.isFolder) {
            getItemsWithStartingPath(getFullPath(item))?.addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { doc ->
                    val nestedItem = doc.toObject(Item::class.java) ?: return@forEach
                    tasks.add(deleteItemPermanentlyAction(nestedItem))
                }
                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    taskCompletionSource.setResult(it.result)
                }
            }
        }
        return taskCompletionSource.task
    }

    private fun deleteItemPermanentlyAction(item: Item): Task<MutableList<Task<*>>> {
        val tasks = mutableListOf<Task<*>?>(getDoc(item)?.delete())
        if (!item.isFolder) {
            tasks.add(Storage.deleteItem(item))
        }
        return Tasks.whenAllComplete(tasks)
    }

    fun starItem(item: Item, isStarred: Boolean): Task<Void>? {
        return getDoc(item)?.update(mapOf(Item::isStarred.name to isStarred))
    }

    private fun moveItem(item: Item, newPath: String): Task<MutableList<Task<*>>> {
        val tasks = mutableListOf<Task<*>?>()
        val updates = getItemMoveOperationUpdates(item, newPath)
        if (item.isFolder) {
            tasks.add(updatePaths(getFullPath(item), newPath + "/" + item.name))
        }
        tasks.add(getDoc(item)?.update(updates))
        updateParentDateModified(item)
        return Tasks.whenAllComplete(tasks)
    }

    private fun updatePaths(oldPath: String, newPath: String): Task<List<Task<*>>> {
        val taskCompletionSource = TaskCompletionSource<List<Task<*>>>()
        val tasks = mutableListOf<Task<Void>?>()
        getItemsWithStartingPath(oldPath)?.addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEach { doc ->
                val item = doc.toObject(Item::class.java) ?: return@forEach
                val updates = getItemMoveOperationUpdates(item, newPath)
                tasks.add(doc.reference.update(updates))
            }
            Tasks.whenAllComplete(tasks).addOnCompleteListener {
                taskCompletionSource.setResult(it.result)
            }
        }
        return taskCompletionSource.task
    }

    private fun getItemMoveOperationUpdates(item: Item, newPath: String): Map<String, Any> {
        val updates: MutableMap<String, Any> = mutableMapOf(Item::path.name to newPath)
        val dateDeleted = Timestamp.now().takeIf { newPath.startsWith(Constants.Roots.BIN) }
        if (dateDeleted != null && item.dateDeleted == null) {
            updates[Item::dateDeleted.name] = dateDeleted
        } else if (dateDeleted == null && item.dateDeleted != null) {
            updates[Item::dateDeleted.name] = FieldValue.delete()
        }
        return updates
    }

    private fun updateParentDateModified(item: Item) {
        val pathSegments = item.path.split("/")
        if (pathSegments.size < 2) return
        val parentName = pathSegments.last()
        val parentPath = pathSegments.dropLast(1).joinToString("/")
        val parentItemQuery =
            getUserDriveCollection()
                ?.whereEqualTo(Item::path.name, parentPath)
                ?.whereEqualTo(Item::name.name, parentName)?.limit(1)
        parentItemQuery?.get()?.addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.firstOrNull()?.reference?.update(
                Item::dateModified.name,
                Timestamp.now()
            )
        }
    }

    private fun getFullPath(item: Item): String {
        return item.path + "/" + item.name
    }
}