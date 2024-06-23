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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.adapter.ItemAdapter
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.FirestoreUtil
import dev.k1k1.kikistorage.util.KeyboardUtil
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var currentPathEditText: EditText
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var emptyStateTextView: TextView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        setupTextWatcher()
        setupBottomNavigation()
        setupFabClickListener()
        updatePath("drive")

        return view
    }

    private fun initializeViews(view: View) {
        currentPathEditText = view.findViewById(R.id.currentPathEditText)
        itemRecyclerView = view.findViewById(R.id.itemRecyclerView)
        bottomNavigationView = view.findViewById(R.id.bottomNavigation)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        fabAdd = view.findViewById(R.id.fab_add)
    }

    private fun setupTextWatcher() {
        currentPathEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.let {
                    setupRecyclerView(it)
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        val paths = mapOf(
            R.id.drive to "drive",
            R.id.starred to "starred",
            R.id.bin to "bin"
        )

        @Suppress("DEPRECATION")
        bottomNavigationView.setOnNavigationItemSelectedListener {
            val newPath = paths[it.itemId]
            if (newPath != null) {
                updatePath(newPath)
                true
            } else {
                false
            }
        }
    }

    private fun setupFabClickListener() {
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

    private fun updatePath(newPath: String) {
        currentPathEditText.clearFocus()
        KeyboardUtil.hideKeyboard(requireContext(), currentPathEditText)
        currentPathEditText.setText(newPath)
        setupRecyclerView(newPath)
    }

    private fun setupRecyclerView(path: String) {
        val userDriveCollection = FirestoreUtil.getUserDriveCollection() ?: return
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
            onItemLongClick = ::handleItemLongClick
        )
        itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        itemRecyclerView.adapter = itemAdapter
        toggleFabVisibility(path)
    }

    private fun handleItemClick(item: Item) {
        if (item.isFolder) {
            updatePath("${currentPathEditText.text}/${item.name}")
        } else {
            // Handle file click (e.g., open the file, show details, etc.)
        }
    }

    private fun handleItemLongClick(item: Item) {
        // Handle item long click (e.g., show options menu)
    }

    private fun toggleFabVisibility(path: String) {
        val fabVisible = fabAdd.visibility == View.VISIBLE
        val shouldShowFab = path.startsWith("drive")
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_out_right)
        if (fabVisible && !shouldShowFab) {
            animation.setInterpolator {
                abs(it - 1.0f)
            }
            fabAdd.visibility = View.GONE
            fabAdd.startAnimation(animation)
        } else if (!fabVisible && shouldShowFab) {
            fabAdd.visibility = View.VISIBLE
            fabAdd.startAnimation(animation)
        }
    }

    private fun showAddFolderDialog() {
        DialogUtil.showAddFolderDialog(requireContext()) { folderName ->
            val folder = Item(
                name = folderName,
                type = "",
                dateAdded = Timestamp.now(),
                dateModified = Timestamp.now(),
                path = currentPathEditText.text.toString(),
                isFolder = true
            )
            FirestoreUtil.getUserDriveCollection()?.add(folder)
        }
    }

    companion object {
        private const val REQUEST_CODE_OPEN_DOCUMENT = 1
        private const val REQUEST_CODE_IMAGE_CAPTURE = 2
    }
}
