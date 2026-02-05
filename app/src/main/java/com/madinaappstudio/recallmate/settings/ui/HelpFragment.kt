package com.madinaappstudio.recallmate.settings.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.databinding.FragmentHelpBinding
import com.madinaappstudio.recallmate.settings.model.HelpFaqItem
import com.madinaappstudio.recallmate.settings.ui.adapter.HelpFaqAdapter
import androidx.core.net.toUri

class HelpFragment : Fragment() {
    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mtbHelp.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        setupFaqList()

        binding.btnHelpEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:help@example.com".toUri()
                putExtra(Intent.EXTRA_SUBJECT, "This is the email subject")
                putExtra(Intent.EXTRA_TEXT, "This is the email body")
            }

            startActivity(intent)
        }

        binding.btnHelpPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:0123456789".toUri()
            }

            startActivity(intent)
        }

    }

    fun setupFaqList() {

        val faqQuestion = resources.getStringArray(R.array.help_faq_questions)
        val faqAnswers = resources.getStringArray(R.array.help_faq_answers)

        val faqs = faqQuestion.mapIndexed { index, question ->
            HelpFaqItem(
                question = question,
                answer = faqAnswers.getOrElse(index) { "" }
            )
        }


        binding.rvHelpFaq.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHelpFaq.adapter = HelpFaqAdapter(faqs)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}