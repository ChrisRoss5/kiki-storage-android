package dev.k1k1.kikistorage.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
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
    private lateinit var itemAdapter: ItemAdapter
    private val pathStack = Stack<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupListeners()
        setupItemAdapter()
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

    private fun setupListeners() {
        binding.currentPathEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let {
                    updateItemAdapterOptions(it)
                }
            }
        })
        binding.currentPathEditText.setOnEditorActionListener { _, _, _ ->
            KeyboardUtil.hideKeyboard(requireContext(), binding.currentPathEditText)
            true
        }
        binding.navigationButton.setOnClickListener {
            val path = pathStack.peek()
            if (rootToIdMap.keys.contains(path)) return@setOnClickListener
            updatePath(path.substringBeforeLast('/', Constants.Roots.DRIVE))
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

    private fun setupItemAdapter() {
        val userDriveCollection = Firestore.getUserDriveCollection() ?: throw IllegalStateException(
            "Firestore collection not available"
        )
        val emptyQuery = userDriveCollection.whereEqualTo("a", "b")
        itemAdapter = ItemAdapter(
            FirestoreRecyclerOptions.Builder<Item>().setQuery(emptyQuery, Item::class.java).build(),
            binding.emptyStateTextView,
            onItemClick = ::handleItemClick,
            onItemLongClick = ::showItemOptions
        )
        binding.itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.itemRecyclerView.adapter = itemAdapter
    }

    private fun updatePath(path: String) {
        if (!pathStack.empty() && pathStack.peek() == path) return
        val newPath =
            if (path.startsWith(Constants.Roots.STARRED) && path != Constants.Roots.STARRED) {
                path.replace(
                    Constants.Roots.STARRED, Constants.Roots.DRIVE
                )
            } else path
        KeyboardUtil.hideKeyboard(requireContext(), binding.currentPathEditText)
        binding.currentPathEditText.setText(newPath.replaceFirstChar { it.uppercase() })
        pathStack.push(newPath)
        StackUtil.removeRepeatingTail(pathStack)
        setStringPreference(LAST_PATH, newPath)
        updateNavigationButton(newPath)
        updateBottomNavigation(newPath)
        updateItemAdapterOptions(newPath)
    }

    private fun updateNavigationButton(path: String) {
        val icon = rootToIdMap[path]?.let {
            binding.bottomNavigation.menu.findItem(it)!!.icon
        } ?: ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_back_24, null)
        binding.navigationButton.setImageDrawable(icon)
    }

    private fun updateBottomNavigation(path: String) {
        val rootId = rootToIdMap.entries.find { path.startsWith(it.key) }?.value ?: 0
        binding.bottomNavigation.menu.findItem(rootId)!!.isChecked = true
    }

    private fun updateItemAdapterOptions(path: String) {
        val userDriveCollection = Firestore.getUserDriveCollection() ?: return
        val query: Query = userDriveCollection.whereEqualTo(
            if (path == Constants.Roots.STARRED) Item::isStarred.name else Item::path.name,
            if (path == Constants.Roots.STARRED) true else path
        )
        // .orderBy("isFolder", Query.Direction.DESCENDING).orderBy("name")
        // - impossible to index while maintaining real-time updates
        itemAdapter.updateOptions(
            FirestoreRecyclerOptions.Builder<Item>().setQuery(query, Item::class.java)
                .setLifecycleOwner(viewLifecycleOwner).build()
        )
        toggleFabVisibility(path)
    }

    private fun handleItemClick(item: Item) {
        if (item.isFolder) updatePath("${pathStack.peek()}/${item.name}")
        else showItemOptions(item)
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
