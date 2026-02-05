package com.madinaappstudio.recallmate.library.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.madinaappstudio.recallmate.library.ui.tabs.FlashcardSetTabFragment
import com.madinaappstudio.recallmate.library.ui.tabs.SummaryTabFragment

class ViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SummaryTabFragment()
            1 -> FlashcardSetTabFragment()
            else -> throw IllegalStateException("Invalid tab")
        }
    }
}
