package dev.k1k1.kikistorage.firebase

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.Constants
import dev.k1k1.kikistorage.util.Constants.ROOT_LIST
import kotlinx.coroutines.tasks.await

object Firestore {
    private const val CLOUD_DIR = "app/drive"
    private val firestoreDb: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun getUserDriveCollection(): CollectionReference? {
        return Auth.getUid()?.let {
            firestoreDb.collection("$CLOUD_DIR/$it")
        }
    }

    fun createDoc(): DocumentReference? {
        return getUserDriveCollection()?.document()
    }

    private fun getDoc(item: Item): DocumentReference? {
        return item.id?.let { getUserDriveCollection()?.document(it) }
    }

    private fun getItemsWithStartingPath(path: String): Task<QuerySnapshot>? {
        return getUserDriveCollection()?.whereGreaterThanOrEqualTo(Item::path.name, path)
            ?.whereLessThanOrEqualTo(Item::path.name, "$path\uf8ff")?.get()
    }

    fun createItem(item: Item, itemDoc: DocumentReference? = null) {
        itemDoc?.set(item) ?: run {
            getUserDriveCollection()?.add(item)
        }
        updateParentDateModified(item)
    }

    suspend fun deleteItem(item: Item): Task<MutableList<Task<*>>> {
        updateParentDateModified(item)
        return moveItem(item, Constants.Roots.BIN)
    }

    suspend fun deleteItemPermanently(item: Item): Task<List<Task<*>>> {
        val tasks = mutableListOf<Task<*>>(deleteItemPermanentlyAction(item))
        if (!item.isFolder) return Tasks.whenAllComplete(tasks)
        val querySnapshot = getItemsWithStartingPath(getFullPath(item))?.await()
        querySnapshot?.documents?.forEach { doc ->
            val nestedItem = doc.toObject(Item::class.java) ?: return@forEach
            tasks.add(deleteItemPermanentlyAction(nestedItem))
        }
        return Tasks.whenAllComplete(tasks)
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

    suspend fun renameItem(item: Item, newName: String): Task<MutableList<Task<*>>> {
        val oldFullPath = getFullPath(item)
        val newFullPath = item.path + "/" + newName
        val updates = mapOf(Item::name.name to newName)
        val tasks = mutableListOf<Task<*>?>(getDoc(item)?.update(updates))
        if (item.isFolder) {
            tasks.add(updatePaths(oldFullPath, newFullPath))
        }
        updateParentDateModified(item)
        return Tasks.whenAllComplete(tasks)
    }

    suspend fun moveItem(item: Item, newPath: String): Task<MutableList<Task<*>>> {
        val itemWithNewPath = item.copy(path = newPath)
        val parentTask = getParentItemQuerySnapshotTask(itemWithNewPath)
        if (parentTask == null && !ROOT_LIST.contains(newPath) || parentTask?.await()?.isEmpty == true) {
            throw IllegalStateException("Parent item not found")
        }
        val updates = getItemMoveOperationUpdates(item, newPath)
        val tasks = mutableListOf<Task<*>?>(getDoc(item)?.update(updates))
        if (item.isFolder) {
            val oldFullPath = getFullPath(item)
            val newFullPath = newPath + "/" + item.name
            tasks.add(updatePaths(oldFullPath, newFullPath))
        }
        updateParentDateModified(item)
        updateParentDateModified(itemWithNewPath)
        return Tasks.whenAllComplete(tasks)
    }

    private suspend fun updatePaths(oldPath: String, newPath: String): Task<List<Task<*>>> {
        val tasks = mutableListOf<Task<Void>?>()
        val querySnapshot = getItemsWithStartingPath(oldPath)?.await()
        querySnapshot?.documents?.forEach { doc ->
            val item = doc.toObject(Item::class.java) ?: return@forEach
            val itemNewPath = item.path.replaceFirst(oldPath, newPath)
            val updates = getItemMoveOperationUpdates(item, itemNewPath)
            tasks.add(doc.reference.update(updates))
        }
        return Tasks.whenAllComplete(tasks)
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
        getParentItemQuerySnapshotTask(item)?.addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.firstOrNull()?.reference?.update(
                Item::dateModified.name, Timestamp.now()
            )
        }
    }

    private fun getParentItemQuerySnapshotTask(item: Item): Task<QuerySnapshot>? {
        val pathSegments = item.path.split("/")
        if (pathSegments.size < 2) return null  // Root
        val parentName = pathSegments.last()
        val parentPath = pathSegments.dropLast(1).joinToString("/")
        val parentItemQuery = getUserDriveCollection()?.whereEqualTo(Item::path.name, parentPath)
            ?.whereEqualTo(Item::name.name, parentName)?.limit(1)
        return parentItemQuery?.get()
    }

    suspend fun getTotalSize(): Long {
        val snapshot = getItemsWithStartingPath("")?.await()
        return snapshot?.documents?.sumOf { doc ->
            doc.toObject(Item::class.java)?.size ?: 0L
        } ?: 0L
    }

    private fun getFullPath(item: Item): String {
        return item.path + "/" + item.name
    }
}


