package com.madinaappstudio.recallmate.settings.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.madinaappstudio.recallmate.BuildConfig
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mtbAbout.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.txtAboutVersion.text = getVersion()

    }

    private fun getVersion() : String {
        val version = BuildConfig.VERSION_NAME
        return getString(R.string.txt_app_version, version)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}