package dev.k1k1.kikistorage.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import dev.k1k1.kikistorage.R

object DialogUtil {
    fun showAddFolderDialog(context: Context, onAddFolder: (String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_folder, null)
        val folderNameEditText = dialogView.findViewById<EditText>(R.id.folderNameEditText)
        val dialog = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.add_folder))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.add)) { _, _ ->
                val folderName = folderNameEditText.text.toString()
                val err = ItemUtil.checkItemName(context, folderName)
                if (err == null) {
                    onAddFolder(folderName)
                } else {
                    showSimpleAlert(context, err) {
                        showAddFolderDialog(context, onAddFolder)
                    }
                }
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
        dialog.show()
    }

    fun showExitAppDialog(context: Context, onExit: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(R.string.exit)
            setMessage(context.getString(R.string.really_exit_the_application))
            setIcon(R.drawable.baseline_exit_to_app_24)
            setCancelable(true)
            setNegativeButton(context.getString(R.string.cancel), null)
            setPositiveButton("OK") { _, _ -> onExit() }
            show()
        }
    }

    fun showAreYouSureDialog(context: Context, onConfirm: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.are_you_sure))
            setMessage(context.getString(R.string.this_action_cannot_be_undone))
            setIcon(R.drawable.baseline_warning_amber_24)
            setCancelable(true)
            setNegativeButton(context.getString(R.string.cancel), null)
            setPositiveButton("OK") { _, _ -> onConfirm() }
            show()
        }
    }

    fun showSimpleAlert(context: Context, message: String, onOk: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setPositiveButton("OK") { _, _ -> onOk() }
            .create()
            .show()
    }
}
