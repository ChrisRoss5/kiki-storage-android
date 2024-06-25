package dev.k1k1.kikistorage.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.k1k1.kikistorage.databinding.BottomSheetAddBinding
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.ItemUtil.createFolder

class AddItemDialogFragment(private val path: String) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        _binding = BottomSheetAddBinding.inflate(layoutInflater, null, false)

        setupListeners()

        dialog.setContentView(binding.root)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.fabFolder.setOnClickListener {
            addFolder()
        }
        binding.fabUpload.setOnClickListener {
            uploadFile()
        }
        binding.fabScan.setOnClickListener {
            uploadFileWithCamera()
        }

    }

    private fun addFolder() {
        DialogUtil.showAddFolderDialog(requireContext()) {
            createFolder(it, path)
            dismiss()
        }
    }

    private fun uploadFile() {
        DialogUtil.openFileManager(
            requireContext(), REQUEST_CODE_OPEN_DOCUMENT
        )
    }

    private fun uploadFileWithCamera() {
        DialogUtil.openCamera(
            requireContext(), REQUEST_CODE_IMAGE_CAPTURE
        )
    }

    companion object {
        private const val REQUEST_CODE_OPEN_DOCUMENT = 1
        private const val REQUEST_CODE_IMAGE_CAPTURE = 2
    }
}
