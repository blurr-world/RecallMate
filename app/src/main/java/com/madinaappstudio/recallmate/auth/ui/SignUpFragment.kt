package com.madinaappstudio.recallmate.auth.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.auth.repository.UserRepository
import com.madinaappstudio.recallmate.auth.viewmodel.UserUiEvent
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModel
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModelFactory
import com.madinaappstudio.recallmate.core.models.UserModel
import com.madinaappstudio.recallmate.core.utils.PrefManager
import com.madinaappstudio.recallmate.core.utils.setLoading
import com.madinaappstudio.recallmate.core.utils.setLog
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentSignUpBinding
import com.madinaappstudio.recallmate.main.HomeActivity
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var prefManager: PrefManager

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        prefManager = PrefManager(requireContext())

        lifecycleScope.launch {
            userViewModel.uiEvent.collect { event ->
                when (event) {
                    is UserUiEvent.Error -> {
                        handleLoading(false)
                        showToast(requireContext(), event.message)
                    }
                    is UserUiEvent.Success -> {
                        handleLoading(false)
                        prefManager.setUserId(event.userId)
                        showToast(requireContext(), event.message)
                        startActivity(
                            Intent(requireContext(), HomeActivity::class.java)
                        )
                        requireActivity().finish()
                    }
                }
            }
        }

        val spannable = SpannableString("Already have an account? Sign in")

        val clickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_signUp_to_signIn)
            }
        }

        spannable.setSpan(
            clickable,
            25,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(Color.BLUE),
            25,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.txtSignInLink.text = spannable
        binding.txtSignInLink.movementMethod = LinkMovementMethod.getInstance()

        binding.btnSignUp.setOnClickListener {
            initiateCreation()
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.etNameSignUp
        } else {

        }
        binding.btnSignUp.setLoading(
            isLoading,
            "Create Account",
            "Creating...",
            binding.cvSignUp
        )
    }


    private fun initiateCreation() {
        val fields = listOf(
            binding.etNameSignUp to "Name is required",
            binding.etEmailSignUp to "Email is required",
            binding.etPassSignUp to "Password is required",
            binding.etCPassSignUp to "Confirm password is required"
        )

        for ((field, error) in fields) {
            if (field.value().isEmpty()) {
                field.error = error
                return
            }
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmailSignUp.value()).matches()) {
            binding.etEmailSignUp.error = "Enter a valid email"
            return
        }

        if (binding.etPassSignUp.value() != binding.etCPassSignUp.value()) {
            binding.etCPassSignUp.error = "Passwords do not match"
            return
        }

        createAccount(binding.etEmailSignUp.value(), binding.etPassSignUp.value())
    }


    private fun createAccount(email: String, pass: String) {
        handleLoading(true)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                prefManager.setLogin(true)

                val user = UserModel(
                    it.user!!.uid,
                    binding.etNameSignUp.value(),
                    email
                )

                userViewModel.saveUser(user.uid, user)
            }
            .addOnFailureListener {
                handleLoading(false)
                showToast(requireContext(), it.localizedMessage)
            }
    }



    private fun EditText.value() = text.toString().trim()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}