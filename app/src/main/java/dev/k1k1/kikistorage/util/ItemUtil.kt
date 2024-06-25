package dev.k1k1.kikistorage.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.google.firebase.Timestamp
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.firebase.Firestore
import dev.k1k1.kikistorage.model.Item

object ItemUtil {
    fun createFolder(name: String, path: String) {
        val folder = Item(
            name = name,
            type = "",
            dateAdded = Timestamp.now(),
            dateModified = Timestamp.now(),
            path = path,
            isFolder = true
        )
        Firestore.createItem(folder)
    }

    fun checkItemName(context: Context, name: String): String? {
        val characters = "\\ / : * ? \" < > |"
        val hasInvalidChars = name.replace(" ", "").any { characters.contains(it) }
        val errMsg = context.getString(
            R.string.a_name_can_t_contain_any_of_the_following_characters,
            characters
        )
        return errMsg.takeIf { hasInvalidChars || name.isEmpty() }
    }

    fun getItemIcon(context: Context, item: Item): Drawable? {
        val resourceName = "vivid_file_icon_" + (if (item.isFolder) "folder" else item.type)
        val resourceId = getDrawableResourceId(context, resourceName)
        return AppCompatResources.getDrawable(
            context,
            if (resourceId != 0) resourceId else R.drawable.vivid_file_icon_blank
        )
    }

    @SuppressLint("DiscouragedApi")
    private fun getDrawableResourceId(context: Context, resourceName: String): Int {
        return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }
}