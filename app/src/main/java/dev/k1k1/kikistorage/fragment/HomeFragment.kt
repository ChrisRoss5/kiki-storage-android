package dev.k1k1.kikistorage.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.adapter.ItemAdapter
import dev.k1k1.kikistorage.databinding.FragmentHomeBinding
import dev.k1k1.kikistorage.firebase.Firestore
import dev.k1k1.kikistorage.fragment.dialog.AddItemDialogFragment
import dev.k1k1.kikistorage.fragment.dialog.ItemOptionsDialogFragment
import dev.k1k1.kikistorage.framework.getStringPreference
import dev.k1k1.kikistorage.framework.setStringPreference
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.Constants
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.KeyboardUtil
import dev.k1k1.kikistorage.util.StackUtil
import java.util.Stack
import kotlin.math.abs

const val LAST_PATH = "dev.k1k1.kikistorage.last_path"

val rootToIdMap = mapOf(
    Constants.Roots.DRIVE to R.id.drive,
    Constants.Roots.STARRED to R.id.starred,
    Constants.Roots.BIN to R.id.bin
)

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var itemAdapter: ItemAdapter? = null
    private val pathStack = Stack<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupListeners()
        updatePath(getStringPreference(LAST_PATH, Constants.Roots.DRIVE))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (pathStack.size >= 2) {
                        pathStack.pop()
                        updatePath(pathStack.pop())
                    } else {
                        DialogUtil.showExitAppDialog(requireContext()) {
                            requireActivity().finish()
                        }
                    }
                }
            })
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView(pathStack.peek())
    }

    override fun onStop() {
        super.onStop()
        itemAdapter?.stopListening()
    }

    private fun setupListeners() {
        binding.currentPathEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let {
                    setupRecyclerView(it)
                }
            }
        })
        binding.currentPathEditText.setOnEditorActionListener { _, _, _ ->
            KeyboardUtil.hideKeyboard(requireContext(), binding.currentPathEditText)
            true
        }
        @Suppress("DEPRECATION") binding.bottomNavigation.setOnNavigationItemSelectedListener {
            updatePath(rootToIdMap.entries.find { v -> v.value == it.itemId }!!.key)
            true
        }
        binding.fabAdd.setOnClickListener {
            val itemOptionsBottomSheet = AddItemDialogFragment(pathStack.peek())
            itemOptionsBottomSheet.show(parentFragmentManager, itemOptionsBottomSheet.tag)
        }
    }

    private fun updatePath(path: String) {
        if (!pathStack.empty() && pathStack.peek() == path) return
        val newPath =
            if (path.startsWith(Constants.Roots.STARRED) && path != Constants.Roots.STARRED) path.replace(
                Constants.Roots.STARRED,
                Constants.Roots.DRIVE
            ) else path
        KeyboardUtil.hideKeyboard(requireContext(), binding.currentPathEditText)
        binding.currentPathEditText.setText(newPath)
        pathStack.push(newPath)
        StackUtil.removeRepeatingTail(pathStack)
        setStringPreference(LAST_PATH, newPath)
        updateBottomNavigation(newPath)
        setupRecyclerView(newPath)
    }

    private fun updateBottomNavigation(path: String) {
        val rootId = rootToIdMap.entries.find { path.startsWith(it.key) }?.value ?: 0
        binding.bottomNavigation.menu.findItem(rootId)!!.isChecked = true
    }

    private fun setupRecyclerView(path: String) {
        val userDriveCollection = Firestore.getUserDriveCollection() ?: return
        val query: Query = userDriveCollection.whereEqualTo(
                if (path == "starred") "isStarred" else "path",
                if (path == "starred") true else path
            )
        val options = FirestoreRecyclerOptions.Builder<Item>().setQuery(query, Item::class.java)
            .setLifecycleOwner(viewLifecycleOwner).build()
        itemAdapter = ItemAdapter(
            options,
            binding.emptyStateTextView,
            onItemClick = ::handleItemClick,
            onItemLongClick = ::showItemOptions
        )
        binding.itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.itemRecyclerView.adapter = itemAdapter
        toggleFabVisibility(path)
    }

    private fun handleItemClick(item: Item) {
        if (item.isFolder) {
            updatePath("${pathStack.peek()}/${item.name}")
            return
        }
        showItemOptions(item)
    }

    private fun toggleFabVisibility(path: String) {
        val fabVisible = binding.fabAdd.visibility == View.VISIBLE
        val shouldShowFab = path.startsWith("drive")
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_out_right)
        if (fabVisible && !shouldShowFab) {
            animation.setInterpolator { abs(it - 1.0f) }
            binding.fabAdd.visibility = View.GONE
            binding.fabAdd.startAnimation(animation)
        } else if (!fabVisible && shouldShowFab) {
            binding.fabAdd.visibility = View.VISIBLE
            binding.fabAdd.startAnimation(animation)
        }
    }

    private fun showItemOptions(item: Item) {
        val itemOptionsBottomSheet = ItemOptionsDialogFragment(item)
        itemOptionsBottomSheet.show(parentFragmentManager, itemOptionsBottomSheet.tag)
    }
}
