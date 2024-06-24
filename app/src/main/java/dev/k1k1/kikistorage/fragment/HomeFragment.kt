package dev.k1k1.kikistorage.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.adapter.ItemAdapter
import dev.k1k1.kikistorage.framework.getStringPreference
import dev.k1k1.kikistorage.framework.setStringPreference
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.Constants
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.FirebaseUtil
import dev.k1k1.kikistorage.util.ItemUtil.createFolder
import dev.k1k1.kikistorage.util.KeyboardUtil
import dev.k1k1.kikistorage.util.StackUtil
import java.util.Stack
import kotlin.math.abs

const val LAST_PATH = "dev.k1k1.kikistorage.last_path"

class HomeFragment : Fragment() {
    private lateinit var currentPathEditText: EditText
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var emptyStateTextView: TextView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var fabAdd: FloatingActionButton
    private val pathStack = Stack<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        setupListeners()
        updatePath(getStringPreference(LAST_PATH, Constants.Roots.DRIVE))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
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


    private fun initializeViews(view: View) {
        currentPathEditText = view.findViewById(R.id.currentPathEditText)
        itemRecyclerView = view.findViewById(R.id.itemRecyclerView)
        bottomNavigationView = view.findViewById(R.id.bottomNavigation)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        fabAdd = view.findViewById(R.id.fab_add)
    }

    private fun setupListeners() {
        currentPathEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let {
                    setupRecyclerView(it)
                }
            }
        })
        currentPathEditText.setOnEditorActionListener { _, _, _ ->
            KeyboardUtil.hideKeyboard(requireContext(), currentPathEditText)
            true
        }
        @Suppress("DEPRECATION")
        bottomNavigationView.setOnNavigationItemSelectedListener {
            updatePath(rootToIdMap.entries.find { v -> v.value == it.itemId }!!.key)
            true
        }
        fabAdd.setOnClickListener {
            DialogUtil.showBottomSheetDialog(
                requireContext(),
                onFolderClick = { showAddFolderDialog() },
                onUploadClick = {
                    DialogUtil.openFileManager(
                        requireContext(),
                        REQUEST_CODE_OPEN_DOCUMENT
                    )
                },
                onScanClick = {
                    DialogUtil.openCamera(
                        requireContext(),
                        REQUEST_CODE_IMAGE_CAPTURE
                    )
                }
            )
        }
    }

    private fun updatePath(path: String) {
        if (!pathStack.empty() && pathStack.peek() == path) return
        val newPath =
            if (path.startsWith(Constants.Roots.STARRED) && path != Constants.Roots.STARRED)
                path.replace(Constants.Roots.STARRED, Constants.Roots.DRIVE) else path
        KeyboardUtil.hideKeyboard(requireContext(), currentPathEditText)
        currentPathEditText.setText(newPath)
        pathStack.push(newPath)
        StackUtil.removeRepeatingTail(pathStack)
        setStringPreference(LAST_PATH, newPath)
        updateBottomNavigation(newPath)
        setupRecyclerView(newPath)
    }

    private fun updateBottomNavigation(path: String) {
        val rootId = rootToIdMap.entries.find { path.startsWith(it.key) }?.value ?: 0
        bottomNavigationView.menu.findItem(rootId)!!.isChecked = true
    }

    private fun setupRecyclerView(path: String) {
        val userDriveCollection = FirebaseUtil.getUserDriveCollection() ?: return
        val query: Query = userDriveCollection
            .whereEqualTo(
                if (path == "starred") "isStarred" else "path",
                if (path == "starred") true else path
            )
        val options = FirestoreRecyclerOptions.Builder<Item>()
            .setQuery(query, Item::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()
        itemAdapter = ItemAdapter(
            options, emptyStateTextView,
            onItemClick = ::handleItemClick,
            onItemLongClick = ::showItemOptions
        )
        itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        itemRecyclerView.adapter = itemAdapter
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
        val fabVisible = fabAdd.visibility == View.VISIBLE
        val shouldShowFab = path.startsWith("drive")
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_out_right)
        if (fabVisible && !shouldShowFab) {
            animation.setInterpolator { abs(it - 1.0f) }
            fabAdd.visibility = View.GONE
            fabAdd.startAnimation(animation)
        } else if (!fabVisible && shouldShowFab) {
            fabAdd.visibility = View.VISIBLE
            fabAdd.startAnimation(animation)
        }
    }

    private fun showAddFolderDialog() {
        DialogUtil.showAddFolderDialog(requireContext()) {
            createFolder(it, pathStack.peek())
        }
    }

    private fun showItemOptions(item: Item) {
        val itemOptionsBottomSheet = ItemOptionsFragment(item)
        itemOptionsBottomSheet.show(parentFragmentManager, itemOptionsBottomSheet.tag)
    }

    companion object {
        val rootToIdMap = mapOf(
            Constants.Roots.DRIVE to R.id.drive,
            Constants.Roots.STARRED to R.id.starred,
            Constants.Roots.BIN to R.id.bin
        )
        private const val REQUEST_CODE_OPEN_DOCUMENT = 1
        private const val REQUEST_CODE_IMAGE_CAPTURE = 2
    }
}
