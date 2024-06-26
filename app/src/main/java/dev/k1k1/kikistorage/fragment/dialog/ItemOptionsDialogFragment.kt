package dev.k1k1.kikistorage.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.databinding.ItemOptionsDialogBinding
import dev.k1k1.kikistorage.firebase.Firestore
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.Constants
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.FormatUtil
import dev.k1k1.kikistorage.util.FormatUtil.dateFormat
import dev.k1k1.kikistorage.util.ItemUtil
import dev.k1k1.kikistorage.worker.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ItemOptionsDialogFragment(private var item: Item) : BottomSheetDialogFragment() {
    private var _binding: ItemOptionsDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomDialogStyle)
        _binding = ItemOptionsDialogBinding.inflate(layoutInflater, null, false)

        populateFields()
        setupListeners()

        dialog.setContentView(binding.root)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateFields() {
        binding.itemName.text = item.name
        val icon = ItemUtil.getItemIcon(binding.itemIcon.context, item)
        binding.itemIcon.setImageDrawable(icon)
        binding.itemType.text = if (item.isFolder) "FOLDER" else item.type.uppercase()
        if (item.isFolder) {
            binding.itemSize.visibility = View.GONE
            binding.itemSizeTitle.visibility = View.GONE
        } else {
            binding.itemSize.text = FormatUtil.formatSize(item.size!!)
        }
        binding.itemDateAdded.text = item.dateAdded?.toDate()?.let { dateFormat.format(it) }
        binding.itemDateModified.text = item.dateModified?.toDate()?.let { dateFormat.format(it) }

        if (item.isFolder) {
            binding.downloadButton.visibility = View.GONE
        }
        if (item.isStarred) {
            binding.starButton.text = getString(R.string.remove_from_starred)
        }
        if (item.path.startsWith(Constants.Roots.BIN)) {
            binding.deleteButton.text = getString(R.string.delete_permanently)
        }
    }

    private fun setupListeners() {
        binding.downloadButton.setOnClickListener {
            downloadItem()
        }
        binding.starButton.setOnClickListener {
            starItem()
        }
        binding.deleteButton.setOnClickListener {
            if (item.path.startsWith(Constants.Roots.BIN)) {
                DialogUtil.showAreYouSureDialog(requireContext()) {
                    deleteItem(true)
                }
            } else deleteItem()
        }
        binding.renameButton.setOnClickListener {
            DialogUtil.showInputTextDialog(
                requireContext(),
                getString(R.string.renaming, item.name),
                getString(R.string.enter_new_name),
                getString(R.string.confirm),
                ::renameItem
            )
        }
        binding.moveButton.setOnClickListener {
            DialogUtil.showInputTextDialog(
                requireContext(),
                getString(R.string.moving, item.name),
                getString(R.string.enter_new_path),
                getString(R.string.confirm),
                ::moveItem
            )
        }
    }

    private fun downloadItem() {
        val itemJson = Gson().toJson(item)
        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>().setInputData(
            Data.Builder().putString("item", itemJson).build()
        ).build()

        WorkManager.getInstance(requireContext()).enqueue(downloadRequest)

        Toast.makeText(requireContext(), getString(R.string.download_started), Toast.LENGTH_SHORT)
            .show()
    }

    private fun starItem() {
        parentFragment?.viewLifecycleOwner?.lifecycleScope?.launch {
            val isStarred = !item.isStarred
            val task = Firestore.starItem(item, isStarred) ?: return@launch
            task.await()
            withContext(Dispatchers.Main) {
                item.isStarred = isStarred
                binding.starButton.text =
                    getString(if (isStarred) R.string.remove_from_starred else R.string.add_to_starred)
            }
        }
    }

    private fun renameItem(newName: String) {
        parentFragment?.viewLifecycleOwner?.lifecycleScope?.launch {
            Firestore.renameItem(item, newName).await()
            withContext(Dispatchers.Main) {
                item.name = newName
                binding.itemName.text = newName
                Toast.makeText(
                    requireContext(), getString(R.string.rename_complete), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun moveItem(newPath: String) {
        parentFragment?.viewLifecycleOwner?.lifecycleScope?.launch {
            try {
                Firestore.moveItem(item, newPath).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(), getString(R.string.move_complete), Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            } catch (e: IllegalStateException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.destination_does_not_exist),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun deleteItem(permanent: Boolean = false) {
        parentFragment?.viewLifecycleOwner?.lifecycleScope?.launch {
            if (permanent) {
                Firestore.deleteItemPermanently(item).await()
            } else {
                Firestore.deleteItem(item).await()
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(), getString(R.string.delete_complete), Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }
    }
}

