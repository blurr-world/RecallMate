package com.madinaappstudio.recallmate.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.auth.repository.UserRepository
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModel
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModelFactory
import com.madinaappstudio.recallmate.databinding.FragmentDashboardBinding
import com.madinaappstudio.recallmate.main.HomeActivity

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cvQuickLinkUpload.setOnClickListener {
            (activity as HomeActivity).binding.bottomNavHome
                .selectedItemId = R.id.uploadFragment
        }

        binding.cvQuickLinkChat.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_chat)
        }

        binding.cvQuickLinkLibrary.setOnClickListener {
            (activity as HomeActivity).binding.bottomNavHome
                .selectedItemId = R.id.libraryFragment
        }

        binding.cvQuickLinkMCQ.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_createMCQ)
        }

        binding.txtGreetingDash.text = getGreeting(System.currentTimeMillis())

    }

    private fun getGreeting(timeMillis: Long) : String {
        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }.get(java.util.Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}