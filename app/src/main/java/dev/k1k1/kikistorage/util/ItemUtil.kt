package dev.k1k1.kikistorage.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import androidx.appcompat.content.res.AppCompatResources
import com.google.firebase.Timestamp
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.model.Item
import java.io.File

object ItemUtil {
    fun createFolder(name: String, path: String): Item {
        return Item(
            name = name,
            type = "",
            dateAdded = Timestamp.now(),
            dateModified = Timestamp.now(),
            path = path,
            isFolder = true
        )
    }

    fun createFile(context: Context, uri: Uri, destPath: String): Item? {
        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return File(uri.path!!).takeIf { it.exists() }?.let {
                Item(
                    name = it.nameWithoutExtension,
                    type = it.extension,
                    path = destPath,
                    size = it.length(),
                    dateAdded = Timestamp.now(),
                    dateModified = Timestamp.now()
                )
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (!it.moveToFirst()) return null
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex == -1 || sizeIndex == -1) return null
                val displayName = it.getString(nameIndex) ?: ""
                return Item(
                    name = displayName.substringBeforeLast('.', ""),
                    type = displayName.substringAfterLast('.', ""),
                    path = destPath,
                    size = it.getLong(sizeIndex),
                    dateAdded = Timestamp.now(),
                    dateModified = Timestamp.now()
                )
            }
        }
        return null
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