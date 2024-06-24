package dev.k1k1.kikistorage.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.Constants
import dev.k1k1.kikistorage.util.DialogUtil
import dev.k1k1.kikistorage.util.FirebaseUtil
import dev.k1k1.kikistorage.util.FirebaseUtil.getUserDriveCollection
import dev.k1k1.kikistorage.util.ItemUtil
import java.text.DateFormat

class ItemOptionsFragment(private var item: Item) : BottomSheetDialogFragment() {

    private lateinit var itemName: TextView
    private lateinit var itemIcon: ImageView
    private lateinit var itemType: TextView
    private lateinit var itemSizeTitle: TextView
    private lateinit var itemSize: TextView
    private lateinit var itemDateAdded: TextView
    private lateinit var itemDateModified: TextView
    private lateinit var downloadButton: TextView
    private lateinit var starButton: TextView
    private lateinit var deleteButton: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomDialogStyle)
        val view = View.inflate(context, R.layout.item_options_menu, null)

        initializeViews(view)
        updateValues()
        setupListeners()

        dialog.setContentView(view)
        return dialog
    }

    private fun initializeViews(view: View) {
        itemName = view.findViewById(R.id.item_name)
        itemIcon = view.findViewById(R.id.item_icon)
        itemType = view.findViewById(R.id.item_type)
        itemSizeTitle = view.findViewById(R.id.item_size_title)
        itemSize = view.findViewById(R.id.item_size)
        itemDateAdded = view.findViewById(R.id.item_date_added)
        itemDateModified = view.findViewById(R.id.item_date_modified)
        downloadButton = view.findViewById(R.id.download_button)
        starButton = view.findViewById(R.id.star_button)
        deleteButton = view.findViewById(R.id.delete_button)
    }

    private fun updateValues() {
        itemName.text = item.name

        val icon = ItemUtil.getItemIcon(itemIcon.context, item)
        itemIcon.setImageDrawable(icon)

        itemType.text = if (item.isFolder) "FOLDER" else item.type.uppercase()

        if (item.isFolder) {
            itemSize.visibility = View.GONE
            itemSizeTitle.visibility = View.GONE
        } else {
            itemSize.text = item.size.toString()
        }

        val dateFormat: DateFormat =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        itemDateAdded.text = item.dateAdded?.toDate()?.let { dateFormat.format(it) }
        itemDateModified.text = item.dateModified?.toDate()?.let { dateFormat.format(it) }

        if (item.isFolder) {
            downloadButton.visibility = View.GONE
        }

        if (item.path.startsWith(Constants.Roots.BIN)) {
            deleteButton.text = getString(R.string.delete_permanently)
        }
    }

    private fun setupListeners() {
        downloadButton.setOnClickListener {
            FirebaseUtil.downloadItem(requireContext(), item);
        }
        starButton.setOnClickListener {
            FirebaseUtil.starItem(item, !item.isStarred)
        }
        deleteButton.setOnClickListener {
            if (item.path.startsWith(Constants.Roots.BIN)) {
                DialogUtil.showExitAppDialog(requireContext()) {
                    FirebaseUtil.deleteItemPermanently(item)
                }
            } else {
                FirebaseUtil.deleteItem(item)
            }
        }
    }
}
