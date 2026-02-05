package com.madinaappstudio.recallmate.library.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.utils.formatDate
import com.madinaappstudio.recallmate.flashcard.model.FlashcardSetItem

class FlashcardSetAdapter(
    private val onItemCLick: (FlashcardSetItem) -> Unit,
    private val onItemDelete: (FlashcardSetItem) -> Unit,
) : ListAdapter<FlashcardSetItem, FlashcardSetAdapter.FlashcardSetViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FlashcardSetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_flashcard_set, parent, false)
        return FlashcardSetViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FlashcardSetViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)

        holder.cvMain.setOnClickListener {
            onItemCLick(item)
        }

//        holder.imgMenu.setOnClickListener {
//            showPopupMenu(holder.imgMenu, item)
//        }

        holder.cvMain.setOnLongClickListener {
            showPopupMenu(it, item)
            true
        }
    }

    class FlashcardSetViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val cvMain: MaterialCardView =
            itemView.findViewById(R.id.cvItemFlashcardSetMain)
//        val imgMenu: ImageView =
//            itemView.findViewById(R.id.imgItemFlashcardSetMenu)
        private val fTitle: MaterialTextView =
            itemView.findViewById(R.id.txtFlashcardItemTitle)
        private val fCount: MaterialTextView =
            itemView.findViewById(R.id.txtFlashcardItemCount)
        private val fDate: MaterialTextView =
            itemView.findViewById(R.id.txtFlashcardItemDate)

        fun bind(item: FlashcardSetItem) {
            fTitle.text = item.title
            fCount.text = "Total ${item.totalCards} Flashcards"
            fDate.text = formatDate(item.createdAt)
        }
    }

    private fun showPopupMenu(view: View, item: FlashcardSetItem) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.item_menu_summary, popup.menu)

        popup.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.btnMenuSummaryDelete -> {
                    onItemDelete(item)
                    true
                }
                else -> false
            }

        }

        popup.show()
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<FlashcardSetItem>() {

                override fun areItemsTheSame(
                    oldItem: FlashcardSetItem,
                    newItem: FlashcardSetItem
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: FlashcardSetItem,
                    newItem: FlashcardSetItem
                ): Boolean {
                    return oldItem == newItem
                }

            }
    }

    fun removeItem(item: FlashcardSetItem) {
        val newList = currentList.toMutableList()
        newList.remove(item)
        submitList(newList)
    }
}