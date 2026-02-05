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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.auth.repository.UserRepository
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModel
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModelFactory
import com.madinaappstudio.recallmate.core.utils.PrefManager
import com.madinaappstudio.recallmate.core.utils.setLoading
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentSignInBinding
import com.madinaappstudio.recallmate.main.HomeActivity

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var prefManager: PrefManager

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        prefManager = PrefManager(requireContext())

        val spannable = SpannableString("Don't have an account? Create one")

        val clickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_signInFrag_to_signUpFrag)
            }
        }

        spannable.setSpan(
            clickable,
            23,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(Color.BLUE),
            23,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.txtSignUnLink.text = spannable
        binding.txtSignUnLink.movementMethod = LinkMovementMethod.getInstance()

        binding.txtResetPassSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signInFrag_to_resetPassFrag)
        }

        binding.btnSignIn.setOnClickListener {
            initiateLogin()
        }

    }

    private fun initiateLogin() {
        val fields = listOf(
            binding.etEmailSignIn to "Email is required",
            binding.etPassSignIn to "Password is required",
        )

        for ((field, error) in fields) {
            if (field.value().isEmpty()) {
                field.error = error
                return
            }
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmailSignIn.value()).matches()) {
            binding.etEmailSignIn.error = "Enter a valid email"
            return
        }

        if (binding.etPassSignIn.value() != binding.etPassSignIn.value()) {
            binding.etPassSignIn.error = "Passwords do not match"
            return
        }

        loginAccount(binding.etEmailSignIn.value(), binding.etPassSignIn.value())
    }

    private fun handleLoading(isLoading: Boolean) {
        binding.btnSignIn.setLoading(
            isLoading,
            "Login",
            "Logging...",
            binding.cvSignIn
        )
    }

    private fun loginAccount(email: String, pass: String) {
        handleLoading(true)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                handleLoading(false)
                prefManager.setLogin(true)
                prefManager.setUserId(it.user!!.uid)
                requireActivity().startActivity(Intent(requireContext(), HomeActivity::class.java))
                requireActivity().finish()
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