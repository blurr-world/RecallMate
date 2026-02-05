package com.madinaappstudio.recallmate.settings.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.auth.repository.UserRepository
import com.madinaappstudio.recallmate.auth.viewmodel.UserUiEvent
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModel
import com.madinaappstudio.recallmate.auth.viewmodel.UserViewModelFactory
import com.madinaappstudio.recallmate.core.models.UserModel
import com.madinaappstudio.recallmate.core.utils.PrefManager
import com.madinaappstudio.recallmate.core.utils.ProfileImages
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.DialogEditProfileBinding
import com.madinaappstudio.recallmate.databinding.FragmentSettingsBinding
import com.madinaappstudio.recallmate.onboarding.ui.OnboardingActivity
import com.madinaappstudio.recallmate.settings.model.SettingsProfileItem
import com.madinaappstudio.recallmate.settings.ui.adapter.ProfilePicAdapter
import kotlinx.coroutines.launch


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PrefManager
    private lateinit var userModel: UserModel
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PrefManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.uiState.collect { state ->
                    if (state.user != null) {
                        userModel = state.user
                        bindViews(state.user)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.uiEvent.collect { event ->
                    when(event){
                        is UserUiEvent.Error -> {
                            showToast(requireContext(), event.message)
                        }
                        is UserUiEvent.Success -> {
                            showToast(requireContext(), event.message)
                        }
                    }
                }
            }
        }

        userViewModel.loadUser(prefManager.getUserId()!!)


        binding.btnSettingsNotification.root.setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
            startActivity(intent)
        }
        binding.btnSettingsLang.root.setOnClickListener {
            pendingDialog()
        }
        binding.btnSettingsPrivacy.root.setOnClickListener {
            findNavController().navigate(R.id.action_setting_to_privacy)
        }
        binding.btnSettingsHelp.root.setOnClickListener {
            findNavController().navigate(R.id.action_setting_to_help)
        }
        binding.btnSettingsAbout.root.setOnClickListener {
            findNavController().navigate(R.id.action_setting_to_about)
        }

        binding.btnSettingsLogout.root.setOnClickListener {
            showLogoutDialog()
        }

        binding.imgSettingsProfile.setImageResource(
            ProfileImages.getDrawableRes(prefManager.getProfileSelection())
        )
        binding.imgSettingsProfile.setOnClickListener {
            setupProfileList()
        }

        binding.btnSettingsEdit.setOnClickListener {
            showEditProfileDialog()
        }


    }

    private fun showEditProfileDialog() {
        val bindingDialog = DialogEditProfileBinding.inflate(layoutInflater)

        bindingDialog.etEditProfileName.setText(userModel.name)
        bindingDialog.etEditProfileEmail.setText(userModel.email)

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(bindingDialog.root)
            .setTitle("Edit profile")
            .setMessage("To change profile picture, click on it.")
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Save", null)
            .create()

        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = bindingDialog.etEditProfileName.text.toString().trim()

            if (name.isEmpty()) {
                bindingDialog.etEditProfileName.error = "Please enter name"
            } else {
                userModel.name = name
                userViewModel.updateUser(prefManager.getUserId()!!, userModel)
                userViewModel.loadUser(prefManager.getUserId()!!)
                alertDialog.dismiss()
            }
        }

    }

    private fun setupProfileList() {
        val layoutDialog = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_settings_profile, null)
        val rvProfile = layoutDialog.findViewById<RecyclerView>(R.id.rvSettingsDialogProfile)

        val picsSheet = BottomSheetDialog(requireContext()).apply {
            setContentView(layoutDialog)
        }
        rvProfile.layoutManager = GridLayoutManager(requireContext(), 4)
        rvProfile.adapter = ProfilePicAdapter(
            listOf(
                SettingsProfileItem(101, R.drawable.profile_pic_1),
                SettingsProfileItem(102, R.drawable.profile_pic_2),
                SettingsProfileItem(103, R.drawable.profile_pic_3),
                SettingsProfileItem(104, R.drawable.profile_pic_4),
                SettingsProfileItem(105, R.drawable.profile_pic_5),
                SettingsProfileItem(106, R.drawable.profile_pic_6),
                SettingsProfileItem(107, R.drawable.profile_pic_7),
                SettingsProfileItem(108, R.drawable.profile_pic_8),
            ),prefManager
        ) {
            binding.imgSettingsProfile.setImageResource(it)
        }

        picsSheet.show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Warning!")
            setMessage("Do you want to logout?")
                .setCancelable(false)
            setNegativeButton("No") { dialog, which ->
                dialog.cancel()
            }
            setPositiveButton("Yes") { dialog, which ->
                proceedLogout()
            }
            show()
        }
    }

    private fun proceedLogout() {
        prefManager.clearAllPrefs()
        FirebaseAuth.getInstance().signOut()
        requireActivity().startActivity(
            Intent(requireActivity(), OnboardingActivity::class.java)
        )
        requireActivity().finishAffinity()
    }

    private fun pendingDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage("This feature is not implemented!")
            show()
        }
    }

    private fun bindViews(user: UserModel) {
        binding.txtSettingsUserName.text = user.name
        binding.txtSettingsUserEmail.text = user.email
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}