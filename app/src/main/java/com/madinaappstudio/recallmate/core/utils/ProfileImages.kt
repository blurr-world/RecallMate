package com.madinaappstudio.recallmate.core.utils

import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.settings.model.SettingsProfileItem

object ProfileImages {

    val list = listOf(
        SettingsProfileItem(101, R.drawable.profile_pic_1),
        SettingsProfileItem(102, R.drawable.profile_pic_2),
        SettingsProfileItem(103, R.drawable.profile_pic_3),
        SettingsProfileItem(104, R.drawable.profile_pic_4),
        SettingsProfileItem(105, R.drawable.profile_pic_5),
        SettingsProfileItem(106, R.drawable.profile_pic_6),
        SettingsProfileItem(107, R.drawable.profile_pic_7),
        SettingsProfileItem(108, R.drawable.profile_pic_8),
    )

    private val map = list.associateBy { it.id }

    fun getDrawableRes(id: Int): Int {
        return map[id]!!.location
    }
}
