package com.madinaappstudio.recallmate.mcq.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.core.utils.formatDate
import com.madinaappstudio.recallmate.databinding.ItemMcqSummarySelectionBinding

class SummarySelectionAdapter(
    private val onSummarySelected: (SummaryModel) -> Unit
) : ListAdapter<SummaryModel, SummarySelectionAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMcqSummarySelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun getSelectedSummary(): SummaryModel? {
        return if (selectedPosition != RecyclerView.NO_POSITION) getItem(selectedPosition) else null
    }

    inner class ViewHolder(private val binding: ItemMcqSummarySelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: SummaryModel, isSelected: Boolean) {
            binding.apply {
                txtSummaryTitle.text = summary.title
                txtSummaryDesc.text = summary.summary
                txtSummaryDate.text = formatDate(summary.timestamp, isOnlyDate = true)

                ivSelectedIndicator.isVisible = isSelected
                cvSummarySelection.strokeWidth = if (isSelected) 4 else 0

                root.setOnClickListener {
                    val previousSelected = selectedPosition
                    selectedPosition = bindingAdapterPosition
                    if (selectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelected)
                        notifyItemChanged(selectedPosition)
                        onSummarySelected(summary)
                    }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SummaryModel>() {
        override fun areItemsTheSame(oldItem: SummaryModel, newItem: SummaryModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SummaryModel, newItem: SummaryModel) =
            oldItem == newItem
    }
}