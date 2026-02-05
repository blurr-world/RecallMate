package com.madinaappstudio.recallmate.library.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.core.utils.formatDate

class SummaryAdapter(
    private val onItemClick: (SummaryModel) -> Unit,
    private val onItemDelete: (SummaryModel) -> Unit
) : ListAdapter<SummaryModel, SummaryAdapter.SummaryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SummaryViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)

        holder.cvMain.setOnClickListener {
            onItemClick(item)
        }

//        holder.imgMenu.setOnClickListener {
//            showPopupMenu(holder.imgMenu, item)
//        }

        holder.cvMain.setOnLongClickListener {
            showPopupMenu(it, item)
            true
        }

    }

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvMain: MaterialCardView =
            itemView.findViewById(R.id.cvItemSummaryMain)
//        val imgMenu: ImageView =
//            itemView.findViewById(R.id.imgItemSummaryMenu)
        private val sTitle: MaterialTextView =
            itemView.findViewById(R.id.txtSummaryItemTitle)
        private val sSource: MaterialTextView =
            itemView.findViewById(R.id.txtSummaryItemSource)
        private val sDate: MaterialTextView =
            itemView.findViewById(R.id.txtSummaryItemDate)

        fun bind(item: SummaryModel) {
            sTitle.text = item.title
            sSource.text = item.sourceTitle
            sDate.text = formatDate(item.timestamp)
        }


    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<SummaryModel>() {

                override fun areItemsTheSame(
                    oldItem: SummaryModel,
                    newItem: SummaryModel
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: SummaryModel,
                    newItem: SummaryModel
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    private fun showPopupMenu(view: View, item: SummaryModel) {
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

    fun removeItem(item: SummaryModel) {
        val newList = currentList.toMutableList()
        newList.remove(item)
        submitList(newList)
    }


}