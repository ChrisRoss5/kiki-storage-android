package dev.k1k1.kikistorage.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Item(
    @DocumentId var id: String? = null,
    var name: String = "",
    var type: String = "",
    var dateAdded: Timestamp? = null,
    var dateModified: Timestamp? = null,
    var dateDeleted: Timestamp? = null,
    var path: String = "",
    var isFolder: Boolean = false,
    var isStarred: Boolean = false,
    var size: Long? = null
)
