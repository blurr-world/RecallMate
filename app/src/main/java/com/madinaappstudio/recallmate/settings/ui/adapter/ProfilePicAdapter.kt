package com.madinaappstudio.recallmate.settings.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.settings.model.SettingsProfileItem
import com.madinaappstudio.recallmate.core.utils.PrefManager

class ProfilePicAdapter (
    private val listPic: List<SettingsProfileItem>,
    val prefManager: PrefManager,
    val onPicSelect: (Int) -> Unit
): RecyclerView.Adapter<ProfilePicAdapter.ProfilePicViewHolder>() {
    private var selectedId = prefManager.getProfileSelection()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfilePicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_settings_profile, parent, false)
        return ProfilePicViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePicViewHolder, position: Int) {
        holder.bind(listPic[position], selectedId)

        holder.mainCard.setOnClickListener {
            val oldSelected = selectedId
            selectedId = listPic[position].id
            prefManager.setProfileSelection(selectedId)

            notifyItemChanged(listPic.indexOfFirst { it.id == oldSelected })
            notifyItemChanged(position)

            onPicSelect(listPic[position].location)
        }
    }


    override fun getItemCount() = listPic.size

    class ProfilePicViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgItemSettingsProfile)
        val imgChecked: ImageView = itemView.findViewById(R.id.imgItemSettingsSelected)
        val mainCard: MaterialCardView = itemView.findViewById(R.id.cvItemSettingsProfile)

        fun bind(image: SettingsProfileItem, selectedPic: Int) {
            imgProfile.setImageResource(image.location)

            if (image.id == selectedPic) {
                imgProfile.alpha = 0.5f
                imgChecked.visibility = View.VISIBLE
            } else {
                imgProfile.alpha = 1f
                imgChecked.visibility = View.GONE
            }
        }

    }
}