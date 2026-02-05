package com.madinaappstudio.recallmate.chat.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.models.ChatModel
import io.noties.markwon.Markwon
import io.noties.markwon.movement.MovementMethodPlugin

class ChatAdapter (private val chatList: MutableList<ChatModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val USER = 1
        private const val AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].isUser) USER else AI
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == USER) {
            val viewUser = LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_item_chat_user, parent, false)
            UserChatViewHolder(viewUser)
        } else {
            val viewAI = LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_item_chat_ai, parent, false)
            AiChatViewHolder(viewAI)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chatList[position]

        when (holder) {
            is UserChatViewHolder -> holder.bind(chat)
            is AiChatViewHolder -> holder.bind(chat)
        }
    }

    override fun getItemCount(): Int = chatList.size

    fun add(message: ChatModel) {
        chatList.add(message)
        notifyItemInserted(chatList.size - 1)
    }

    fun removeTyping() {
        val index = chatList.indexOfLast { it.isTyping }
        if (index != -1) {
            chatList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun replaceLast(message: ChatModel) {
        chatList[chatList.lastIndex] = message
        notifyItemChanged(chatList.lastIndex)
    }

    class UserChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: MaterialTextView = itemView.findViewById(R.id.txtItemChatUser)
        val markwon = Markwon.builder(itemView.context)
            .usePlugin(MovementMethodPlugin.link())
            .build()
        fun bind(model: ChatModel) {
            markwon.setMarkdown(text, model.text)
        }
    }

    class AiChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: MaterialTextView = itemView.findViewById(R.id.txtItemChatAi)
        val markwon = Markwon.builder(itemView.context)
            .usePlugin(MovementMethodPlugin.link())
            .build()
        fun bind(model: ChatModel) {
            markwon.setMarkdown(text, model.text)
        }
    }
}