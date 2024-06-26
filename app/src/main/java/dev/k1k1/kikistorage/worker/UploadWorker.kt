package dev.k1k1.kikistorage.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.firebase.Storage
import dev.k1k1.kikistorage.util.FormatUtil
import dev.k1k1.kikistorage.util.ItemUtil
import dev.k1k1.kikistorage.util.UIUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class UploadWorker(
    private val context: Context, workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val path = inputData.getString("path") ?: return Result.failure()
        val filePath = inputData.getString("file_path")
        val fileUris = filePath?.let {
            arrayOf(Uri.fromFile(File(it)).toString())
        } ?: inputData.getStringArray("file_uris") ?: return Result.failure()

        return try {
            val uploadTasks = withContext(Dispatchers.IO) {
                fileUris.mapNotNull {
                    val uri = Uri.parse(it)
                    ItemUtil.createFile(applicationContext, uri, path)?.let {
                        async {
                            Storage.uploadItem(it, uri)?.await()
                        }
                    }
                }
            }

            val byteSize =
                FormatUtil.formatSize(uploadTasks.awaitAll().sumOf { it?.bytesTransferred ?: 0 })

            UIUtil.showToastOnMainThread(
                context,
                context.getString(R.string.upload_complete_files, uploadTasks.size, byteSize)
            )
            Result.success()
        } catch (e: Exception) {
            UIUtil.showToastOnMainThread(context, context.getString(R.string.upload_failed))
            Result.failure()
        }
    }
}
