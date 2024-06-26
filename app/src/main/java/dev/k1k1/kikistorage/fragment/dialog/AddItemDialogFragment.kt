package dev.k1k1.kikistorage.fragment.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.databinding.AddItemDialogBinding
import dev.k1k1.kikistorage.firebase.Firestore
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.DialogUtil.showSimpleAlert
import dev.k1k1.kikistorage.util.ImageUtil
import dev.k1k1.kikistorage.util.ItemUtil
import dev.k1k1.kikistorage.util.ItemUtil.createFolder
import dev.k1k1.kikistorage.util.PermissionUtil
import dev.k1k1.kikistorage.worker.UploadWorker

const val PROVIDER_AUTHORITY = "dev.k1k1.kikistorage.fileprovider"

class AddItemDialogFragment(private val path: String?) : BottomSheetDialogFragment() {
    private var _binding: AddItemDialogBinding? = null
    private val binding get() = _binding!!

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.takeIf { it.resultCode == Activity.RESULT_OK }?.data?.let { data ->
            val uris = data.clipData?.let {
                (0 until it.itemCount).map { i ->
                    it.getItemAt(i).uri.toString()
                }
            } ?: listOf(data.data.toString())
            if (path == null) {
                setFragmentResult(
                    "files_ready",
                    bundleOf("file_uris" to uris.toTypedArray())
                )
                dismiss()
            }
            else startUploadWorker(uris)
        }
    }

    private lateinit var currentPhotoPath: String
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        if (path == null) {
            setFragmentResult(
                "files_ready",
                bundleOf("file_path" to currentPhotoPath)
            )
            dismiss()
        }
        else startUploadWorker()
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            capturePhoto()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        _binding = AddItemDialogBinding.inflate(layoutInflater, null, false)

        setupListeners()

        if (path == null) {
            binding.addFolderLayout.visibility = View.GONE
        }

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
            openFilePicker()
        }
        binding.fabPhoto.setOnClickListener {
            capturePhoto()
        }
    }

    private fun addFolder() {
        DialogUtil.showInputTextDialog(
            requireContext(),
            requireContext().getString(R.string.add_folder),
            requireContext().getString(R.string.enter_new_folder_name),
            requireContext().getString(R.string.add)
        ) { folderName ->
            ItemUtil.checkItemName(requireContext(), folderName)?.let {
                showSimpleAlert(requireContext(), folderName, ::addFolder)
            } ?: run {
                Firestore.createItem(createFolder(folderName, path!!))
                dismiss()
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        filePickerLauncher.launch(intent)
    }

    private fun capturePhoto() {
        PermissionUtil.requestCameraPermission(
            requireContext(), requestCameraPermissionLauncher
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = ImageUtil.createImageFile(requireContext())
            currentPhotoPath = photoFile.absolutePath
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(), PROVIDER_AUTHORITY, photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraLauncher.launch(intent)
        }
    }

    private fun startUploadWorker(uris: List<String>? = null) {
        val dataBuilder = Data.Builder().putString("path", path)
        uris?.let {
            dataBuilder.putStringArray("file_uris", it.toTypedArray())
        } ?: dataBuilder.putString("file_path", currentPhotoPath)

        val uploadWorkRequest =
            OneTimeWorkRequestBuilder<UploadWorker>().setInputData(dataBuilder.build()).build()

        WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)

        Toast.makeText(requireContext(), getString(R.string.upload_started), Toast.LENGTH_SHORT)
            .show()
        dismiss()
    }
}
