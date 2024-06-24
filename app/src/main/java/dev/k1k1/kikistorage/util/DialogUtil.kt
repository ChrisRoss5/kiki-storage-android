package dev.k1k1.kikistorage.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
                if (folderName.isNotEmpty()) {
                    onAddFolder(folderName)
                }
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
        dialog.show()
    }

    fun showBottomSheetDialog(
        context: Context,
        onFolderClick: () -> Unit,
        onUploadClick: () -> Unit,
        onScanClick: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_add, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetView.findViewById<FloatingActionButton>(R.id.fab_folder).setOnClickListener {
            bottomSheetDialog.dismiss()
            onFolderClick()
        }
        bottomSheetView.findViewById<FloatingActionButton>(R.id.fab_upload).setOnClickListener {
            bottomSheetDialog.dismiss()
            onUploadClick()
        }
        bottomSheetView.findViewById<FloatingActionButton>(R.id.fab_scan).setOnClickListener {
            bottomSheetDialog.dismiss()
            onScanClick()
        }
        bottomSheetDialog.show()
    }

    fun openFileManager(context: Context, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        (context as Activity).startActivityForResult(intent, requestCode)
    }

    fun openCamera(context: Context, requestCode: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        (context as Activity).startActivityForResult(intent, requestCode)
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
}
