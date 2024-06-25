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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return ItemViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, model: Item) {
        holder.name.text = model.name + (if (!model.isFolder) "." + model.type else "")

        if (model.isStarred) {
            holder.name.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.outline_star_border_24,
                0
            )
        }

        val icon = ItemUtil.getItemIcon(holder.icon.context, model)
        holder.icon.setImageDrawable(icon)
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
}
