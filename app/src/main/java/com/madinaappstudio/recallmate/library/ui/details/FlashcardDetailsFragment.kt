package com.madinaappstudio.recallmate.library.ui.details

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.models.FlashcardModel
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentFlashcardDetailsBinding
import com.madinaappstudio.recallmate.flashcard.repository.FlashcardRepository
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardUiEvent
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardViewModel
import com.madinaappstudio.recallmate.flashcard.viewmodel.FlashcardViewModelFactory
import kotlinx.coroutines.launch

class FlashcardDetailsFragment : Fragment() {

    private var _binding: FragmentFlashcardDetailsBinding? = null
    private val binding get() = _binding!!
    private val flashcardViewModel: FlashcardViewModel by viewModels {
        FlashcardViewModelFactory(FlashcardRepository())
    }
    private val args: FlashcardDetailsFragmentArgs by navArgs()
    private lateinit var flashcards: List<FlashcardModel>
    private var currentIndex = 0
    private lateinit var flipOut: Animator
    private lateinit var flipIn: Animator
    private var isFront = true
    private var isFlipping = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mtbFlashcardDetails.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val scale = requireContext().resources.displayMetrics.density
        binding.cvFlashcardDetailsQ.cameraDistance = 12000 * scale
        binding.cvFlashcardDetailsA.cameraDistance = 12000 * scale

        flipOut = AnimatorInflater
            .loadAnimator(requireContext(), R.animator.flashcard_flip_out)
        flipIn = AnimatorInflater
            .loadAnimator(requireContext(), R.animator.flashcard_flip_in)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flashcardViewModel.uiState.collect { state ->

                    binding.pbFlashcardDetails.isVisible = state.isLoading

                    val hasData = state.flashcards.isNotEmpty()

                    binding.clFlashcardDetails.isVisible =
                        hasData && !state.isLoading

                    if (hasData) {
                        flashcards = state.flashcards
                        renderCard()
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

        binding.llFlashcardDetailsMain.setOnClickListener {
            flipCard()
        }

        binding.btnFlashcardNext.setOnClickListener {
            if (currentIndex < flashcards.lastIndex) {
                currentIndex++
                renderCard()
            } else {
                showToast(requireContext(), "You’ve reached the last card.")
            }
        }

        binding.btnFlashcardPrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                renderCard()
            } else {
                showToast(requireContext(), "This is the first card.")
            }
        }


        flashcardViewModel.loadFlashcardsBySet(
            FirebaseAuth.getInstance().uid!!,
            args.setId
        )

        binding.msFlashcardDetailsMark.setOnClickListener {
            val newValue = binding.msFlashcardDetailsMark.isChecked
            updateCompletion(newValue)
        }

    }

    private fun updateCompletion(isChecked: Boolean) {
        flashcards[currentIndex].completed = isChecked
        flashcardViewModel.updateFlashcard(
            FirebaseAuth.getInstance().uid!!,
            flashcards[currentIndex]
        )
    }

    private fun renderCard() {
        val card = flashcards[currentIndex]

        binding.txtFlashcardDetailsCounter.text = "${flashcards.indexOf(card) + 1}/${flashcards.size} Flashcard"
        binding.lpiFlashcardDetails.progress = flashcards.indexOf(card) + 1
        binding.lpiFlashcardDetails.max = flashcards.size

        binding.txtFlashcardDetailsQ.text = card.question
        binding.txtFlashcardDetailsA.text = card.answer
        binding.msFlashcardDetailsMark.isChecked = card.completed

        isFront = true
        binding.cvFlashcardDetailsQ.visibility = View.VISIBLE
        binding.cvFlashcardDetailsA.visibility = View.GONE
        binding.cvFlashcardDetailsQ.rotationY = 0f
        binding.cvFlashcardDetailsA.rotationY = 0f
    }


    private fun flipCard() {
        if (isFlipping) return
        isFlipping = true

        val visible = if (isFront) binding.cvFlashcardDetailsQ else binding.cvFlashcardDetailsA
        val hidden = if (isFront) binding.cvFlashcardDetailsA else binding.cvFlashcardDetailsQ

        flipOut.setTarget(visible)
        flipIn.setTarget(hidden)


        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visible.visibility = View.GONE
                hidden.visibility = View.VISIBLE
                flipIn.start()
                flipOut.removeListener(this)
            }
        })

        flipIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isFlipping = false
                flipIn.removeListener(this)
            }
        })

        flipOut.start()
        isFront = !isFront
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}