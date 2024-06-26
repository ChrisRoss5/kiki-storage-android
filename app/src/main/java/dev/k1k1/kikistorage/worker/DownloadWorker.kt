package dev.k1k1.kikistorage.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.firebase.Storage
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.UIUtil
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
            UIUtil.showToastOnMainThread(
                context,
                context.getString(R.string.downloaded_to, filePath)
            )
            Result.success()
        } catch (e: Exception) {
            UIUtil.showToastOnMainThread(context, context.getString(R.string.download_failed))
            Result.failure()
        }
    }
}
