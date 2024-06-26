package dev.k1k1.kikistorage.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object UIUtil {
    fun showToastOnMainThread(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}