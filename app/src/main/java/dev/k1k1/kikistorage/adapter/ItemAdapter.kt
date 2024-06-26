package dev.k1k1.kikistorage.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.model.Item
import dev.k1k1.kikistorage.util.ItemUtil

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

    init {
        setHasStableIds(true)  // With getItemId() override - for animations with notifyDataSetChanged
    }

    private var sortedItems: List<Item> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return ItemViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, model: Item) {
        val item = sortedItems[position]
        holder.name.text =
            item.name + (if (!item.isFolder && item.type.isNotEmpty()) "." + item.type else "")
        holder.name.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (item.isStarred) R.drawable.outline_star_border_24 else 0,
            0
        )
        holder.icon.setImageDrawable(ItemUtil.getItemIcon(holder.icon.context, item))
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
            true
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        sortedItems =
            snapshots.sortedWith(compareByDescending<Item> { it.isFolder }.thenBy { it.name })
        notifyDataSetChanged()
        emptyStateTextView.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun getItemId(position: Int): Long {
        return sortedItems[position].id.hashCode().toLong()
    }
}
