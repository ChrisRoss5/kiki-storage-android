package dev.k1k1.kikistorage.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService

object KeyboardUtil {
    fun hideKeyboard(context: Context, view: View) {
        view.clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = getSystemService(context, ClipboardManager::class.java)
        val clip = ClipData.newPlainText(label, text)
        clipboard?.apply {
            setPrimaryClip(clip)
            Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}
