package dev.k1k1.kikistorage.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

// https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/
// JavaBean naming pattern removes "is" prefix from booleans so PropertyName annotation is used!

data class Item(
    @DocumentId var id: String? = null,
    var name: String = "",
    var type: String = "",
    var dateAdded: Timestamp? = null,
    var dateModified: Timestamp? = null,
    var dateDeleted: Timestamp? = null,
    var path: String = "",
    @get:PropertyName("isFolder") @set:PropertyName("isFolder") var isFolder: Boolean = false,
    @get:PropertyName("isStarred") @set:PropertyName("isStarred") var isStarred: Boolean = false,
    var size: Long? = null
)
