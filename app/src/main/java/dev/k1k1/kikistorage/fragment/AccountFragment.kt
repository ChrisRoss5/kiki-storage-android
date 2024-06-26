package dev.k1k1.kikistorage.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dev.k1k1.kikistorage.databinding.FragmentAccountBinding
import dev.k1k1.kikistorage.firebase.Auth
import dev.k1k1.kikistorage.firebase.Firestore
import dev.k1k1.kikistorage.util.FormatUtil
import dev.k1k1.kikistorage.util.FormatUtil.dateFormat
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        populateFields()

        return binding.root
    }

    private fun populateFields() {
        binding.textSignedInAs.text = Auth.auth.currentUser?.let {
            "Signed in as ${it.email}"
        } ?: "Not signed in"

        Auth.auth.currentUser?.let {
            binding.textEmail.text = "Email: ${it.email}"
            binding.textSignInMethods.text =
                "Sign-in methods: " + it.providerData.joinToString(", ") { provider ->
                    provider.providerId
                }
            it.metadata?.let { metadata ->
                binding.textLastSignIn.text =
                    "Last sign-in: " + dateFormat.format(metadata.lastSignInTimestamp)
                binding.textSignUp.text =
                    "Sign-up: " + dateFormat.format(metadata.creationTimestamp)
            }
            viewLifecycleOwner.lifecycleScope.launch {
                binding.textTotalStorage.text =
                    "Total storage: ${FormatUtil.formatSize(Firestore.getTotalSize())} / ∞"
            }
        }
    }
}
