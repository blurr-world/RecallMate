package com.madinaappstudio.recallmate.upload.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.upload.model.UploadResultItem
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.movement.MovementMethodPlugin

class UploadResultAdapter(
    private val items: List<UploadResultItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_DIVIDER = 1
        const val TYPE_SUMMARY = 2
        const val TYPE_FLASHCARD = 3
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is UploadResultItem.SectionHeader -> TYPE_HEADER
        is UploadResultItem.SectionDivider -> TYPE_DIVIDER
        is UploadResultItem.Summary -> TYPE_SUMMARY
        is UploadResultItem.Flashcard -> TYPE_FLASHCARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderVH(
                inflater.inflate(R.layout.rv_item_result_section_header, parent, false)
            )
            TYPE_DIVIDER -> DividerVH(
                inflater.inflate(R.layout.rv_item_result_section_divider, parent, false)
            )
            TYPE_SUMMARY -> SummaryVH(
                inflater.inflate(R.layout.rv_item_result_summary, parent, false)
            )
            else -> FlashcardVH(
                inflater.inflate(R.layout.rv_item_result_flashcard, parent, false)
            )
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is UploadResultItem.SectionHeader -> (holder as HeaderVH).bind(item)
            is UploadResultItem.Summary -> (holder as SummaryVH).bind(item)
            is UploadResultItem.Flashcard -> (holder as FlashcardVH).bind(item)
            else -> Unit
        }
    }

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: UploadResultItem.SectionHeader) {
            itemView.findViewById<MaterialTextView>(R.id.txtResultSectionHeader).text = item.title
        }
    }

    class DividerVH(view: View) : RecyclerView.ViewHolder(view)

    class SummaryVH(view: View) : RecyclerView.ViewHolder(view) {
        val markwon = Markwon.builder(itemView.context)
            .usePlugin(MovementMethodPlugin.link())
            .build()
        fun bind(item: UploadResultItem.Summary) {
            markwon.setMarkdown(
                itemView.findViewById<MaterialTextView>(R.id.txtResultSummary),
                item.text
            )
        }
    }

    class FlashcardVH(view: View) : RecyclerView.ViewHolder(view) {
        private val question = view.findViewById<MaterialTextView>(R.id.txtResultFlashcardQ)
        private val answer = view.findViewById<MaterialTextView>(R.id.txtResultFlashcardA)

        fun bind(item: UploadResultItem.Flashcard) {
            question.text = item.question
            answer.text = item.answer
            answer.visibility = if (item.expanded) View.VISIBLE else View.GONE

            question.setOnClickListener {
                item.expanded = !item.expanded
                answer.visibility = if (item.expanded) View.VISIBLE else View.GONE
            }
        }
    }
}