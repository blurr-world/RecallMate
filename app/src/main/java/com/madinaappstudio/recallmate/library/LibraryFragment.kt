package com.madinaappstudio.recallmate.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.madinaappstudio.recallmate.databinding.FragmentLibraryBinding
import com.madinaappstudio.recallmate.library.ui.adapter.ViewPagerAdapter

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerAdapter(this)
        binding.viewPagerLibrary.adapter = adapter

        TabLayoutMediator(
            binding.tabLayoutLibrary,
            binding.viewPagerLibrary
        ) { tab, position ->
            when (position) {
                0 -> tab.text = "Summaries"
                1 -> tab.text = "Flashcards"
            }
        }.attach()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}