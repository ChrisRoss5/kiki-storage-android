package dev.k1k1.kikistorage.fragment

import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.vertexai.type.Content
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.adapter.MessageAdapter
import dev.k1k1.kikistorage.databinding.FragmentGeminiBinding
import dev.k1k1.kikistorage.firebase.Auth
import dev.k1k1.kikistorage.firebase.Gemini
import dev.k1k1.kikistorage.fragment.dialog.AddItemDialogFragment
import dev.k1k1.kikistorage.model.Message
import dev.k1k1.kikistorage.util.FormatUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.Date

class GeminiFragment : Fragment() {
    private lateinit var binding: FragmentGeminiBinding
    private lateinit var userDrawable: Drawable
    private lateinit var geminiDrawable: Drawable

    private val items = mutableListOf<Message>()
    private val chat = Gemini.generativeModel.startChat()
    private val contentBuilder = Content.Builder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGeminiBinding.inflate(inflater, container, false)
        userDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_account_circle_24)!!
        geminiDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_auto_awesome_24)!!

        setupListeners()

        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MessageAdapter(requireContext(), items)
        }
        return binding.root
    }

    private fun setupListeners() {
        binding.addMessageImageView.setOnClickListener {
            attachToMessage()
        }
        binding.sendButton.setOnClickListener {
            sendMessageAsUser()
        }
    }

    private fun attachToMessage() {
        val addIdemDialog = AddItemDialogFragment(null)
        addIdemDialog.show(parentFragmentManager, addIdemDialog.tag)
        parentFragmentManager.setFragmentResultListener("files_ready", this) { _, bundle ->
            val fileUris = bundle.getStringArray("file_uris")
            val filePath = bundle.getString("file_path")
            fileUris?.forEach {
                val mimeType = requireContext().contentResolver.getType(it.toUri())!!
                val byteArray =
                    requireContext().contentResolver.openInputStream(it.toUri())?.readBytes()!!
                contentBuilder.blob(mimeType, byteArray)
            }.let {
                Toast.makeText(
                    requireContext(), getString(R.string.file_s_attached), Toast.LENGTH_SHORT
                ).show()
            }
            filePath?.let {
                val uri = Uri.fromFile(File(it))
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver, uri
                    )
                }
                contentBuilder.image(bitmap)
                Toast.makeText(
                    requireContext(), getString(R.string.image_attached), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendMessageAsUser() {
        binding.messageEditText.text.toString().let {
            if (it.isEmpty()) return
            createMessageDisplay(it, true)
            callGemini(it)
            binding.messageEditText.text.clear()
            binding.sendButton.visibility = View.GONE
        }
    }

    private fun createMessageDisplay(message: String, isUser: Boolean) {
        val date = FormatUtil.dateFormat.format(Date())
        val sender = (if (isUser) Auth.auth.currentUser?.displayName
            ?: getString(R.string.you) else Gemini.MODEL_NAME) + " @ $date"
        val userDrawable = if (isUser) userDrawable else geminiDrawable
        items.add(Message(message, sender, userDrawable))
        binding.rvItems.adapter?.notifyItemInserted(items.size - 1)
        binding.rvItems.scrollToPosition(items.size - 1)
    }

    private fun callGemini(message: String) {
        parentFragment?.viewLifecycleOwner?.lifecycleScope?.launch {
            contentBuilder.text(message)
            createMessageDisplay(getString(R.string.thinking), false)
            try {
                var isFirstChunk = true
                chat.sendMessageStream(contentBuilder.build()).collect { chunk ->
                    if (isFirstChunk) {
                        items.last().text = chunk.text ?: ""
                        isFirstChunk = false
                    } else items.last().text += chunk.text
                    refreshMessageDisplay()
                }
            } catch (e: Exception) {
                items.last().text =
                    getString(R.string.file_not_supported_or_is_over_20mb_supported_input_files_include_images_pdfs_video_and_audio)
            } finally {
                items.last().text = items.last().text.trim()
                binding.sendButton.visibility = View.VISIBLE
                contentBuilder.parts.clear()
                refreshMessageDisplay()
            }
        }
    }

    private fun refreshMessageDisplay() {
        binding.rvItems.adapter?.notifyItemChanged(items.size - 1)
    }
}
