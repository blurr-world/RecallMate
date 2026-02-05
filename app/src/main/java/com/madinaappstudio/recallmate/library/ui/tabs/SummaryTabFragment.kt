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
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentSummaryTabBinding
import com.madinaappstudio.recallmate.library.LibraryFragmentDirections
import com.madinaappstudio.recallmate.library.ui.adapter.SummaryAdapter
import com.madinaappstudio.recallmate.summary.repository.SummaryRepository
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryUiEvent
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModel
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModelFactory
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

class SummaryTabFragment : Fragment() {

    private var _binding: FragmentSummaryTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var summaryAdapter: SummaryAdapter
    private val summaryViewModel: SummaryViewModel by activityViewModels {
        SummaryViewModelFactory(SummaryRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                summaryViewModel.uiState.collect { state ->

                    binding.pbSummaryTab.isVisible = state.isLoading

                    val hasData = state.summaryList.isNotEmpty()

                    binding.rvSummaryLibrary.isVisible =
                        hasData && !state.isLoading

                    binding.llSummaryLibEmpty.isVisible =
                        !hasData && !state.isLoading

                    if (hasData) {
                        summaryAdapter.submitList(state.summaryList)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                summaryViewModel.uiEvent.collect { event ->
                    when (event) {
                        is SummaryUiEvent.Error ->
                            showToast(requireContext(), event.message)

                        is SummaryUiEvent.Success ->
                            showToast(requireContext(), event.message)
                    }
                }
            }
        }

        summaryViewModel.loadAllSummary(firebaseAuth.currentUser!!.uid)

    }

    private fun setupRecyclerView() {
        summaryAdapter = SummaryAdapter(
            onItemClick = {
                val direction = LibraryFragmentDirections.actionLibraryToSummaryDetails(it.id)
                findNavController().navigate(direction)
            },
            onItemDelete = {
                deleteDialog(it)
            }
        )
        binding.rvSummaryLibrary.apply {
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
            adapter = summaryAdapter
        }
    }

    private fun deleteDialog(summary: SummaryModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Warning")
            .setMessage("Do you really want to delete summary?")
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, which ->
                summaryViewModel.removeSummary(
                    firebaseAuth.uid!!,
                    summary.id
                )
                dialog.dismiss()
                summaryAdapter.removeItem(summary)
            }
            .show()
    }

    private fun observeUiState() {

    }

    private fun observeUiEvent() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
