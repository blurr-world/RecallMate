package com.madinaappstudio.recallmate.upload.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.core.models.FlashcardModel
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.core.utils.setLoading
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentUploadResultBinding
import com.madinaappstudio.recallmate.flashcard.repository.FlashcardRepository
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardUiEvent
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardViewModel
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardViewModelFactory
import com.madinaappstudio.recallmate.summary.repository.SummaryRepository
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryUiEvent
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModel
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModelFactory
import com.madinaappstudio.recallmate.upload.model.AiResponseModel
import com.madinaappstudio.recallmate.upload.model.UploadResultItem
import com.madinaappstudio.recallmate.upload.ui.adapter.UploadResultAdapter
import com.madinaappstudio.recallmate.upload.viewmodel.UploadViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class UploadResultFragment : Fragment() {

    private var _binding: FragmentUploadResultBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: UploadViewModel by activityViewModels()
    private val summaryViewModel: SummaryViewModel by activityViewModels {
        SummaryViewModelFactory(SummaryRepository())
    }
    private val flashcardViewModel: FlashcardViewModel by activityViewModels {
        FlashcardViewModelFactory(FlashcardRepository())
    }
    private lateinit var aiResponseModel: AiResponseModel
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentUploadResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.mtbUploadResultMain.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        sharedViewModel.uploadResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                aiResponseModel = it
                bindViews(it)
            } else {
                binding.llUploadResultErrorView.visibility = View.VISIBLE
                binding.rvUploadResult.visibility = View.GONE
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
                    handleLoading(false)
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
                    handleLoading(false)
                }
            }
        }

        binding.btnUploadResultSave.setOnClickListener {
            saveToLibrary()
        }

    }

    private fun handleLoading(isLoading: Boolean) {
        binding.btnUploadResultSave.setLoading(
            isLoading,
            "Save To Library",
            "Saving...",
            null
        )
    }

    private fun bindViews(model: AiResponseModel) {
        val items = mutableListOf<UploadResultItem>()

        items.add(UploadResultItem.SectionHeader("Summary"))
        items.add(UploadResultItem.Summary(model.summary.summary))

        if (aiResponseModel.flashcards != null) {
            items.add(UploadResultItem.SectionDivider)

            items.add(UploadResultItem.SectionHeader("Flashcards"))
            model.flashcards!!.cards.forEach { card ->
                items.add(
                    UploadResultItem.Flashcard(
                        question = card.question,
                        answer = card.answer
                    )
                )
            }
        }

        binding.rvUploadResult.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUploadResult.adapter = UploadResultAdapter(items)
    }

    private fun saveToLibrary() {
        handleLoading(true)
        val summaryModel = SummaryModel(
            title = aiResponseModel.summary.summaryTitle,
            summary = aiResponseModel.summary.summary,
            sourceTitle = aiResponseModel.summary.sourceTitle,
            length = aiResponseModel.summary.summaryLength,
            audienceLevel = aiResponseModel.summary.summaryAudienceLevel
        )

        summaryViewModel.saveSummary(
            firebaseAuth.uid!!,
            summaryModel
        )

        if (aiResponseModel.flashcards != null) {
            val flashcardModel = emptyList<FlashcardModel>().toMutableList()

            aiResponseModel.flashcards!!.cards.forEach { card ->
                flashcardModel.add(FlashcardModel(question = card.question, answer = card.answer))
            }

            flashcardViewModel.saveFlashcardSet(
                firebaseAuth.uid!!,
                aiResponseModel.flashcards!!.flashcardTitle,
                flashcardModel.toList()
            )
        }
    }
}