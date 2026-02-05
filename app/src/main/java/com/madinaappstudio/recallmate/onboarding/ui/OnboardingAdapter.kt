package com.madinaappstudio.recallmate.onboarding.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.onboarding.model.OnboardingItem

class OnboardingAdapter(
    private val pages: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgOnboarding)
        val title: MaterialTextView = view.findViewById(R.id.txtTitleOnboarding)
        val desc: MaterialTextView = view.findViewById(R.id.txtDesOnboarding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_onboarding, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        holder.image.setImageResource(page.image)
        holder.title.text = page.title
        holder.desc.text = page.description
    }

    override fun getItemCount(): Int = pages.size
}
