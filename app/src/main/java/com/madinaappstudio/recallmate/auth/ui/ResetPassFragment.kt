package com.madinaappstudio.recallmate.auth.ui

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.core.utils.setLoading
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentResetPassBinding

class ResetPassFragment : Fragment() {

    private var _binding: FragmentResetPassBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.btnBackResetPass.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSendResetLink.setOnClickListener {
            initiateReset()
        }

        binding.btnBackToSignIn.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun initiateReset() {
        val email = binding.etEmailResetPass.text.toString()

        if (email.isEmpty()) {
            binding.etEmailResetPass.error = "Email is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailResetPass.error = "Enter a valid email"
            return
        }

        resetPassword(email)
    }

    private fun handleLoading(isLoading: Boolean) {
        binding.btnSendResetLink.setLoading(
            isLoading,
            "Send Reset Link",
            "Sending Link...",
            binding.cvResetPass
        )
    }
    private fun resetPassword(email: String) {
        handleLoading(true)
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                handleLoading(false)
                handleUI()
            }
            .addOnFailureListener {
                handleLoading(false)
                showToast(requireContext(), it.localizedMessage)
            }
    }

    private fun handleUI() {
        val showingReset = binding.cvResetPass.isVisible
        binding.cvResetPass.isVisible = !showingReset
        binding.cvResetPassResult.isVisible = showingReset
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}