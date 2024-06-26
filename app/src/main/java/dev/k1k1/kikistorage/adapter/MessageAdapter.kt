package dev.k1k1.kikistorage.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.k1k1.kikistorage.R
import dev.k1k1.kikistorage.model.Message

class MessageAdapter(
    private val context: Context, private val items: MutableList<Message>
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage = itemView.findViewById<TextView>(R.id.textMessage)
        private val textSender = itemView.findViewById<TextView>(R.id.textSender)
        private val messengerImageView = itemView.findViewById<ImageView>(R.id.messengerImageView)

        fun bind(item: Message) {
            textMessage.text = item.text
            textSender.text = item.sender
            messengerImageView.setImageDrawable(item.image)
        }
    }

    fun updateItem(position: Int, newMessage: Message) {
        if (position >= 0 && position < items.size) {
            items[position] = newMessage
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.gemini_message, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}