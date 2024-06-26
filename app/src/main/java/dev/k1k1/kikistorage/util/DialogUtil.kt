package dev.k1k1.kikistorage.util

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import dev.k1k1.kikistorage.R

object DialogUtil {
    fun showInputTextDialog(
        context: Context, title: String, hint: String, btnText: String, onConfirm: (String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.input_text_dialog, null)
        val inputEditText = dialogView.findViewById<EditText>(R.id.text)
        inputEditText.hint = hint
        val dialog = AlertDialog.Builder(context).setTitle(title).setView(dialogView)
            .setPositiveButton(btnText) { _, _ ->
                onConfirm(inputEditText.text.toString())
            }.setNegativeButton(context.getString(R.string.cancel), null).create()
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
        builder.setMessage(message).setPositiveButton("OK") { _, _ -> onOk() }.create().show()
    }
}
