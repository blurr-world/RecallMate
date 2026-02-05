package com.madinaappstudio.recallmate.settings.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.settings.model.HelpFaqItem

class HelpFaqAdapter(
    private val listFaq: List<HelpFaqItem>
) : RecyclerView.Adapter<HelpFaqAdapter.FaqViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FaqViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_help_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FaqViewHolder,
        position: Int
    ) {
        holder.bind(listFaq[position])
    }

    override fun getItemCount(): Int = listFaq.size

    class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtQuestion: MaterialTextView =
            itemView.findViewById(R.id.txtItemHelpFaqQues)
        private val txtAnswer: MaterialTextView =
            itemView.findViewById(R.id.txtItemHelpFaqAns)

        fun bind(faq: HelpFaqItem) {
            txtQuestion.text = faq.question
            txtAnswer.text = faq.answer

            txtAnswer.visibility = if (faq.isExpanded) View.VISIBLE else View.GONE

            txtQuestion.setOnClickListener {
                faq.isExpanded = !faq.isExpanded
                txtAnswer.visibility = if (faq.isExpanded) View.VISIBLE else View.GONE
            }
        }
    }
}