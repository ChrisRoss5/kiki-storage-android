package dev.k1k1.kikistorage.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.model.Item

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val icon: ImageView = view.findViewById(R.id.icon)
    val name: TextView = view.findViewById(R.id.name)
}

class ItemAdapter(
    options: FirestoreRecyclerOptions<Item>,
    private val emptyStateTextView: TextView,
    private val onItemClick: (Item) -> Unit,
    private val onItemLongClick: (Item) -> Unit
) :
    FirestoreRecyclerAdapter<Item, ItemViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, model: Item) {
        holder.name.text = model.name

        val context = holder.icon.context
        val resourceName = "vivid_file_icon_" + (if (model.isFolder) "folder" else model.type)
        val resourceId = getDrawableResourceId(context, resourceName)

        holder.icon.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                if (resourceId != 0) resourceId else R.drawable.vivid_file_icon_blank
            )
        )

        // Set click listeners for item options
        holder.itemView.setOnClickListener {
            onItemClick(model)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(model)
            true
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        emptyStateTextView.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
    }

    @SuppressLint("DiscouragedApi")
    private fun getDrawableResourceId(context: Context, resourceName: String): Int {
        return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }
}
