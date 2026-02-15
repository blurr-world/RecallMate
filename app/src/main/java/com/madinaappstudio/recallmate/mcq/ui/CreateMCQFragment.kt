package com.madinaappstudio.recallmate.mcq.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.madinaappstudio.recallmate.BuildConfig
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.models.McqModel
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.core.utils.extractJson
import com.madinaappstudio.recallmate.core.utils.setLoading
import com.madinaappstudio.recallmate.core.utils.setLog
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentCreateMcqBinding
import com.madinaappstudio.recallmate.databinding.LayoutMcqConfigBottomSheetBinding
import com.madinaappstudio.recallmate.mcq.ui.adapter.SummarySelectionAdapter
import com.madinaappstudio.recallmate.summary.repository.SummaryRepository
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryUiEvent
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModel
import com.madinaappstudio.recallmate.summary.viewmodel.SummaryViewModelFactory
import kotlinx.coroutines.launch

class CreateMCQFragment : Fragment() {

    private var _binding: FragmentCreateMcqBinding? = null
    private val binding get() = _binding!!

    private val summaryViewModel: SummaryViewModel by viewModels {
        SummaryViewModelFactory(SummaryRepository())
    }

    private lateinit var summaryAdapter: SummarySelectionAdapter
    private val auth = FirebaseAuth.getInstance()
    private var selectedDifficulty = "Intermediate"
    private var selectedQuestionCount = 5

    private val model = GenerativeModel(
        "gemini-2.5-flash",
        BuildConfig.GEMINI_API_KEY
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateMcqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUiState()
        observeUiEvents()

        auth.currentUser?.uid?.let { userId ->
            summaryViewModel.loadAllSummary(userId)
        }

        binding.mtbCreateMCQ.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnMCQSettings.setOnClickListener {
            showConfigurationBottomSheet()
        }

        binding.btnGenerateQuiz.setOnClickListener {
            val selectedSummary = summaryAdapter.getSelectedSummary()
            if (selectedSummary != null) {
                generateMCQ(selectedSummary)
            }
        }
    }

    private fun generateMCQ(summary: SummaryModel) {
        binding.btnGenerateQuiz.setLoading(
            true,
            "Generate Quiz",
            "Generating Quiz...",
            binding.llCreateMCQBottom
        )
        
        val prompt = buildMCQPrompt(summary, selectedDifficulty, selectedQuestionCount)
        
        lifecycleScope.launch {
            try {
                val response = model.generateContent(prompt)
                val jsonResponse = response.text?.let { extractJson(it, false) }
                if (!jsonResponse.isNullOrEmpty()) {

                    val type = object : TypeToken<List<McqModel>>() {}.type
                    val mcqList: List<McqModel> = Gson().fromJson(jsonResponse, type)

                    val action = CreateMCQFragmentDirections.actionCreateMCQFragmentToPracticeMCQFragment(mcqList.toTypedArray())
                    findNavController().navigate(action)
                } else {
                    showToast(requireContext(), "Failed to generate MCQs", Toast.LENGTH_SHORT)
                }
            } catch (e: Exception) {
                setLog(e, "MCQ_ERROR")
                showToast(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
            } finally {
                binding.btnGenerateQuiz.setLoading(
                    false,
                    "Generate Quiz",
                    "Generating Quiz...",
                    binding.llCreateMCQBottom
                )
            }
        }
    }

    private fun buildMCQPrompt(summary: SummaryModel, difficulty: String, count: Int): String {
        return """
            Generate exactly $count multiple-choice questions (MCQs) based on the following summary content.
            Difficulty Level: $difficulty
            
            Summary Content:
            ${summary.summary}
            
            Return the result strictly as a raw JSON array of objects. 
            Do not include any markdown formatting, backticks, or extra text.
            Each object must have these exact fields:
            - "question": The question text.
            - "options": A list of exactly 4 strings.
            - "correctOption": The text of the correct option (must be present in the options list).
            
            JSON Format Example:
            [
              {
                "question": "What is...?",
                "options": ["A", "B", "C", "D"],
                "correctOption": "A"
              }
            ]
        """.trimIndent()
    }

    private fun showConfigurationBottomSheet() {
        val bottomSheetBinding = LayoutMcqConfigBottomSheetBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(bottomSheetBinding.root)

        val levels = resources.getStringArray(R.array.audience_level)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, levels)
        bottomSheetBinding.aTxtMCQDifficultyBS.setAdapter(adapter)
        bottomSheetBinding.aTxtMCQDifficultyBS.setText(selectedDifficulty, false)
        
        bottomSheetBinding.sliderMCQCountBS.value = selectedQuestionCount.toFloat()
        bottomSheetBinding.txtMCQQuestionCountBS.text = "Number of Questions: $selectedQuestionCount"

        bottomSheetBinding.sliderMCQCountBS.addOnChangeListener { _, value, _ ->
            bottomSheetBinding.txtMCQQuestionCountBS.text = "Number of Questions: ${value.toInt()}"
        }

        bottomSheetBinding.btnApplyMCQConfig.setOnClickListener {
            selectedDifficulty = bottomSheetBinding.aTxtMCQDifficultyBS.text.toString()
            selectedQuestionCount = bottomSheetBinding.sliderMCQCountBS.value.toInt()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupRecyclerView() {
        summaryAdapter = SummarySelectionAdapter { _ ->
            binding.btnGenerateQuiz.isEnabled = true
            binding.btnGenerateQuiz.alpha = 1.0f
        }
        binding.rvCreateMCQ.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = summaryAdapter
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                summaryViewModel.uiState.collect { state ->
                    binding.pbCreateMCQ.isVisible = state.isLoading
                    binding.rvCreateMCQ.isVisible = !state.isLoading
                    binding.txtCreateMcqHeader.isVisible = !state.isLoading
                    summaryAdapter.submitList(state.summaryList)
                }
            }
        }
    }

    private fun observeUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                summaryViewModel.uiEvent.collect { event ->
                    when (event) {
                        is SummaryUiEvent.Error -> {
                            showToast(requireContext(), event.message, Toast.LENGTH_SHORT)
                        }
                        is SummaryUiEvent.Success -> {
                            showToast(requireContext(), event.message, Toast.LENGTH_SHORT)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}