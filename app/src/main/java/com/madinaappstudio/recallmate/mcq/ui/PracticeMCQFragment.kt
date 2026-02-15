package com.madinaappstudio.recallmate.mcq.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.models.McqModel
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentPracticeMcqBinding
import androidx.core.graphics.toColorInt

class PracticeMCQFragment : Fragment() {

    private var _binding: FragmentPracticeMcqBinding? = null
    private val binding get() = _binding!!

    private val args: PracticeMCQFragmentArgs by navArgs()
    private var mcqList: List<McqModel> = emptyList()
    private var currentIndex = 0
    private lateinit var userAnswers: MutableList<String?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeMcqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mtbPracticeMCQ.setNavigationOnClickListener {
            showExitWarningDialog()
        }

        mcqList = args.mcqList.toList()
        userAnswers = MutableList(mcqList.size) { null }

        if (mcqList.isNotEmpty()) {
            renderQuestion()
        } else {
            showToast(requireContext(), "No questions found.")
            findNavController().popBackStack()
            return
        }


        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        binding.btnPracticeNext.setOnClickListener {
            if (currentIndex < mcqList.lastIndex) {
                currentIndex++
                renderQuestion()
            }
        }

        binding.btnPracticePrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                renderQuestion()
            }
        }

        binding.btnPracticeSubmit.setOnClickListener {
            showResult()
        }

        binding.btnExitPractice.setOnClickListener {
            findNavController().popBackStack()
        }

        val optionCards = listOf(binding.cvOptionA, binding.cvOptionB, binding.cvOptionC, binding.cvOptionD)
        val optionTexts = listOf(binding.txtOptionA, binding.txtOptionB, binding.txtOptionC, binding.txtOptionD)

        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                userAnswers[currentIndex] = optionTexts[index].text.toString()
                renderQuestion()
            }
        }
    }


    private fun renderQuestion() {
        val mcq = mcqList[currentIndex]
        val total = mcqList.size
        val currentNumber = currentIndex + 1
        val progress = (currentNumber.toFloat() / total * 100).toInt()

        binding.txtPracticeCounter.text = "QUESTION $currentNumber OF $total"
        binding.txtPracticePercentage.text = "$progress%"
        binding.lpiPracticeMCQ.progress = progress

        binding.txtPracticeQuestion.text = mcq.question

        if (mcq.options.size >= 4) {
            binding.txtOptionA.text = mcq.options[0]
            binding.txtOptionB.text = mcq.options[1]
            binding.txtOptionC.text = mcq.options[2]
            binding.txtOptionD.text = mcq.options[3]
        }

        updateOptionSelection()

        binding.btnPracticeSubmit.isVisible = (currentIndex == mcqList.lastIndex)
        binding.btnPracticeNext.isVisible = (currentIndex < mcqList.lastIndex)
    }

    private fun updateOptionSelection() {
        val selectedOption = userAnswers[currentIndex]
        val optionCards = listOf(binding.cvOptionA, binding.cvOptionB, binding.cvOptionC, binding.cvOptionD)
        val optionTexts = listOf(binding.txtOptionA, binding.txtOptionB, binding.txtOptionC, binding.txtOptionD)
        val optionLabels = listOf(binding.txtPracticeOptionLabelA, binding.txtPracticeOptionLabelB,
            binding.txtPracticeOptionLabelC, binding.txtPracticeOptionLabelD)

        optionCards.forEachIndexed { index, card ->
            optionLabels[index].setBackgroundResource(R.drawable.bg_settings_item_first)
            if (optionTexts[index].text.toString() == selectedOption) {

                optionLabels[index].backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.brand_primary))

                optionLabels[index].setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white))
                card.strokeWidth = 4
                card.strokeColor = ContextCompat.getColor(requireContext(), R.color.brand_primary)

            } else {
                optionLabels[index].backgroundTintList = ColorStateList.valueOf("#F5F5F5".toColorInt())

                optionLabels[index].setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_secondary))

                card.strokeWidth = 1
                card.strokeColor = ContextCompat.getColor(requireContext(), R.color.divider)
            }
        }

    }

    private fun showResult() {
        binding.practiceView.isVisible = false
        binding.resultView.isVisible = true

        val correctAnswers = mcqList.map { it.correctOption }
        val score = userAnswers.zip(correctAnswers).count { (user, correct) -> user == correct }
        val total = mcqList.size
        val percentage = (score.toFloat() / total * 100).toInt()

        binding.txtResultScore.text = "You scored $score out of $total"
        binding.txtResultPercentage.text = "$percentage%"
    }

    private fun showExitWarningDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exit Practice")
            .setMessage("Are you sure you want to exit? Your progress will be lost and you’ll have to start over.")
            .setPositiveButton("Exit") { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}