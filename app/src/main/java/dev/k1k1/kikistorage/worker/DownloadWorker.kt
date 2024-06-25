package dev.k1k1.kikistorage.worker

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.firebase.Storage
import dev.k1k1.kikistorage.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val itemJson = inputData.getString("item") ?: return Result.failure()
        val item = Gson().fromJson(itemJson, Item::class.java)

        return try {
            val filePath = withContext(Dispatchers.IO) {
                Storage.downloadItem(item)
            }
            showToast(context.getString(R.string.downloaded_to, filePath));
            Result.success()
        } catch (e: Exception) {
            showToast(context.getString(R.string.download_failed))
            Result.failure()
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
