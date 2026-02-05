package com.madinaappstudio.recallmate.library.ui.details

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
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.core.utils.formatDate
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentSummaryDetailsBinding
import com.madinaappstudio.recallmate.summary.repository.SummaryRepository
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryUiEvent
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModel
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModelFactory
import kotlinx.coroutines.launch
import kotlin.getValue

class SummaryDetailsFragment : Fragment() {

    private var _binding: FragmentSummaryDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: SummaryDetailsFragmentArgs by navArgs()

    private val summaryViewModel: SummaryViewModel by activityViewModels {
        SummaryViewModelFactory(SummaryRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mtbSummaryDetailsMain.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                summaryViewModel.uiState.collect { state ->

                    binding.pbSummaryDetails.isVisible = state.isLoading
                    binding.clSummaryDetailsIgnore.isVisible = state.isLoading

                    val hasData = state.summary != null

                    binding.svSummaryDetails.isVisible =
                        hasData && !state.isLoading

                    if (hasData)
                        bindViews(state.summary)
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

        summaryViewModel.loadSummary(
            FirebaseAuth.getInstance().uid!!,
            args.summaryId
        )

    }

    private fun bindViews(summary: SummaryModel) {
        binding.txtSummaryDetailsTitle.text = summary.title
        binding.txtSummaryDetailsDate.text = formatDate(summary.timestamp)
        binding.txtSummaryDetailsSource.text = summary.sourceTitle
        binding.txtSummaryDetailsSummary.text = summary.summary
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}