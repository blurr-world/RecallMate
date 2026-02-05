package com.madinaappstudio.recallmate.library.ui.tabs

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentFlashcardTabBinding
import com.madinaappstudio.recallmate.flashcard.model.FlashcardSetItem
import com.madinaappstudio.recallmate.flashcard.repository.FlashcardRepository
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardUiEvent
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardViewModel
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardViewModelFactory
import com.madinaappstudio.recallmate.library.LibraryFragmentDirections
import com.madinaappstudio.recallmate.library.ui.adapter.FlashcardSetAdapter
import kotlinx.coroutines.launch

class FlashcardSetTabFragment : Fragment() {

    private var _binding: FragmentFlashcardTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var flashcardSetAdapter: FlashcardSetAdapter
    private val flashcardViewModel: FlashcardViewModel by activityViewModels {
        FlashcardViewModelFactory(FlashcardRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flashcardViewModel.uiState.collect { state ->

                    binding.pbFlashcardTab.isVisible = state.isLoading

                    val hasData = state.flashcardSets.isNotEmpty()

                    binding.rvFlashcardLibrary.isVisible =
                        hasData && !state.isLoading

                    binding.llFlashcardLibEmpty.isVisible =
                        !hasData && !state.isLoading

                    if (hasData){
                        flashcardSetAdapter.submitList(state.flashcardSets)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flashcardViewModel.uiEvent.collect { event ->
                    when (event) {
                        is FlashcardUiEvent.Error ->
                            showToast(requireContext(), event.message)

                        is FlashcardUiEvent.Success ->
                            showToast(requireContext(), event.message)
                    }
                }
            }
        }

        flashcardViewModel.loadFlashcardSets(firebaseAuth.uid!!)

    }

    private fun setupRecyclerView() {
        flashcardSetAdapter = FlashcardSetAdapter(
            onItemCLick = {
                flashcardViewModel.flashcardSet.value = it
                val direction = LibraryFragmentDirections
                    .actionLibraryToFlashcardDetails(it.id)
                findNavController().navigate(direction)
            },
            onItemDelete = {
                deleteDialog(it)
            }
        )
        binding.rvFlashcardLibrary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(object : RecyclerView.ItemDecoration(){
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.bottom = 16
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.top = 16
                    }
                }
            })
            adapter = flashcardSetAdapter
        }
    }

    private fun deleteDialog(flashcardSet: FlashcardSetItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Warning")
            .setMessage("Do you really want to delete summary?")
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, which ->
                flashcardViewModel.removeFlashcardSet(
                    firebaseAuth.uid!!,
                    flashcardSet.id
                )
                dialog.dismiss()
                flashcardSetAdapter.removeItem(flashcardSet)
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}