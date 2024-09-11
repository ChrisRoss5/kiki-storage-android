package dev.k1k1.kikistorage.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.databinding.FragmentAccountBinding
import dev.k1k1.kikistorage.firebase.Auth
import dev.k1k1.kikistorage.firebase.Firestore
import dev.k1k1.kikistorage.firebase.Functions
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.FormatUtil
import dev.k1k1.kikistorage.util.FormatUtil.dateFormat
import dev.k1k1.kikistorage.util.UIUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        populateFields()
        setupListeners()

        return binding.root
    }

    private fun populateFields() {
        binding.textSignedInAs.text = Auth.auth.currentUser?.let {
            getString(R.string.signed_in_as, it.email)
        } ?: getString(R.string.not_signed_in)
        Auth.auth.currentUser?.let {
            binding.textEmail.text = getString(R.string.email, it.email)
            val signInMethods = it.providerData.joinToString(", ") { provider ->
                provider.providerId
            }
            binding.textSignInMethods.text = getString(R.string.sign_in_methods, signInMethods)
            it.metadata?.let { metadata ->
                binding.textLastSignIn.text = getString(
                    R.string.last_sign_in, dateFormat.format(metadata.lastSignInTimestamp)
                )
                binding.textSignUp.text =
                    getString(R.string.sign_up, dateFormat.format(metadata.creationTimestamp))
            }
            viewLifecycleOwner.lifecycleScope.launch {
                binding.textTotalStorage.text = getString(
                    R.string.total_storage, FormatUtil.formatSize(Firestore.getTotalSize())
                )
            }
        }
    }

    private fun setupListeners() {
        binding.buttonDeleteAccount.setOnClickListener { deleteAccount() }
    }

    private fun deleteAccount() {
        DialogUtil.showAreYouSureDialog(requireContext()) {
            binding.buttonDeleteAccount.isEnabled = false
            binding.buttonDeleteAccount.text = getString(R.string.deleting_account)
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val email = Auth.auth.currentUser?.email ?: ""
                    Functions.deleteAccount().await()
                    UIUtil.showToastOnMainThread(
                        requireContext(),
                        getString(R.string.account_deleted, email)
                    )
                    Auth.signOut(requireContext())
                } catch (e: Exception) {
                    binding.buttonDeleteAccount.isEnabled = true
                    binding.buttonDeleteAccount.text = getString(R.string.delete_account)
                    DialogUtil.showSimpleAlert(
                        requireContext(),
                        getString(R.string.error),
                        getString(R.string.you_must_reauthenticate_before_deleting_your_account)
                    ) {
                        Auth.signOut(requireContext())
                    }
                }
            }
        }
    }
}
