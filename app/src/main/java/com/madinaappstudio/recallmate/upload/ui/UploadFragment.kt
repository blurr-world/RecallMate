package com.madinaappstudio.recallmate.upload.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FileDataPart
import com.google.ai.client.generativeai.type.content
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.madinaappstudio.recallmate.BuildConfig
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.api.GeminiFileUploader
import com.madinaappstudio.recallmate.core.api.GeminiUploadCallback
import com.madinaappstudio.recallmate.core.utils.getFileName
import com.madinaappstudio.recallmate.core.utils.setLoading
import com.madinaappstudio.recallmate.core.utils.setLog
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentUploadBinding
import com.madinaappstudio.recallmate.upload.model.UploadConfiguration
import com.madinaappstudio.recallmate.upload.model.AiResponseModel
import com.madinaappstudio.recallmate.upload.viewmodel.UploadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private var uriMimePair: Pair<String, String>? = null
    private val uploadViewModel: UploadViewModel by activityViewModels()
    private var selectedFileName = ""
    private var isFlashcardGenerate = false
    private var uploadConfiguration = UploadConfiguration()
    private var currentUploadMode = 0
    val model = GenerativeModel(
        "gemini-2.5-flash",
        BuildConfig.GEMINI_API_KEY
    )

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                selectedFileName = getFileName(requireContext(), it) ?: "Unknown Source"
                handleFile(it)
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mtbUpload.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.cvUploadDoc.setOnClickListener {
            filePicker.launch(arrayOf("application/pdf", "application/docs"))
        }

//        binding.cgUploadSummaryLength.check(R.id.chipUploadSummaryMedium)

        binding.btnUploadCreateStack.setOnClickListener {
            if (currentUploadMode == 0) {
                uriMimePair?.let { (uri, mimeType) ->
                    handleLoading(true)
                    saveConfiguration()
                    generateContent(uri, mimeType, true)
                } ?: selectFileToast()
            } else {
                val text = binding.etUploadPasteText.text.toString()
                if (text.isNotEmpty()) {
                    handleLoading(true)
                    generateContent(text, "", false)
                } else {
                    showToast(requireContext(), "Please enter something")
                }

            }
        }

        setupAudienceDropDown()

        binding.btnUploadToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btnUploadOptionUpload -> {
                    currentUploadMode = 0
                    resetField(true)
                    changeOptionViews(true)
                }
                R.id.btnUploadOptionPaste -> {
                    currentUploadMode = 1
                    resetField(false)
                    changeOptionViews(false)
                }
            }
        }

        binding.btnUploadCustomize.setOnClickListener {
            showCustomizeView()
            val drawables = binding.btnUploadCustomize.compoundDrawablesRelative

            val endDrawable = ContextCompat.getDrawable(
                binding.btnUploadCustomize.context,
                if (binding.llUploadCustomizeMain.isVisible)
                    R.drawable.ic_arrow_top
                else
                    R.drawable.ic_arrow_down
            )

            binding.btnUploadCustomize.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawables[0],
                drawables[1],
                endDrawable,
                drawables[3]
            )

        }

        binding.msUploadFlashcardSettings.setOnCheckedChangeListener { p0, p1 ->
            binding.llUploadFlashcardMain.isVisible = p1
            isFlashcardGenerate = p1
            binding.svUploadMain.scrollToDescendant(binding.sliderUploadFlashcard)
        }
        binding.cgUploadSummaryLength.setOnCheckedStateChangeListener { group, checkedId ->
            binding.cgUploadSummaryLength.check(checkedId[0])
        }

        binding.sliderUploadFlashcard.addOnChangeListener { p0, p1, p2 ->
            binding.txtUploadFlashcardCount.text = "Flashcard: ${p1.toInt()}"
        }

    }

    private fun saveConfiguration() {
        val lengthChip = binding.cgUploadSummaryLength
            .findViewById<Chip>(binding.cgUploadSummaryLength.checkedChipId)

        uploadConfiguration = UploadConfiguration(
            summaryTitle = binding.etUploadSummaryTitle.text?.toString()?.trim(),
            summaryLength = lengthChip.text as String,
            summaryAudienceLevel = binding.aTxtUploadAudienceLevel.text.toString(),
            isFlashcard = isFlashcardGenerate,
            flashcardTitle = if (isFlashcardGenerate)
                binding.etUploadFlashcardTitle.text?.toString()?.trim()
            else null,
            flashcardCount = if (isFlashcardGenerate)
                binding.sliderUploadFlashcard.value.toInt()
            else 0
        )
    }

    private fun showCustomizeView() {
        val isVisible = binding.llUploadCustomizeMain.isVisible
        binding.llUploadCustomizeMain.isVisible = !isVisible
    }

    private fun changeOptionViews(isUploadView: Boolean) {
        binding.cvUploadDoc.isVisible = isUploadView
        binding.ilUploadPasteText.isVisible = !isUploadView
    }

    private fun setupAudienceDropDown() {
        val levels = resources.getStringArray(R.array.audience_level)
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1, levels)
        binding.aTxtUploadAudienceLevel.setAdapter(adapter)
    }


    private fun selectFileToast() {
        handleLoading(false)
        showToast(requireContext(), "Please select file")
    }

    private fun handleFile(uri: Uri) {
        GeminiFileUploader.upload(requireContext(), uri, object : GeminiUploadCallback{
            override fun onSuccess(fileUri: String, mimeType: String) {
                uriMimePair = Pair(fileUri, mimeType)
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        binding.txtUploadFileName.text = selectedFileName
                    }
                }
            }

            override fun onError(message: String) {
                showToast(requireContext(), "Failed to upload file.")
                setLog(any = message)
            }

        })
    }


    private fun generateContent(uri: String, mimeType: String, isFile: Boolean) {
        val prompt = if (isFile) content {
            text(buildPrompt(uploadConfiguration))
            part(
                FileDataPart(
                    uri,
                    mimeType
                )
            )
        } else content {
            text(buildPrompt(uploadConfiguration))
            text(uri)
        }

        setLog(any = prompt)

        lifecycleScope.launch {
            try {
                val response = model.generateContent(prompt)

                if (!response.text.isNullOrEmpty()) {
                    setLog(any = response.text, tag = "GeminiResponse")
                    val ai = Gson().fromJson(extractJson(response.text.toString()), AiResponseModel::class.java)
                    val finalResponse = AiResponseModel(
                        summary = AiResponseModel.Summary(
                            summaryTitle = ai.summary.summaryTitle,
                            summary = ai.summary.summary,
                            sourceTitle = if (currentUploadMode == 0) selectedFileName else "Generated from text",
                            summaryLength = uploadConfiguration.summaryLength,
                            summaryAudienceLevel = uploadConfiguration.summaryAudienceLevel
                        ),
                        flashcards = if (uploadConfiguration.isFlashcard) {
                            ai.flashcards
                        } else null,
                        mcq = null
                    )
                    setLog(any = finalResponse)
                    uploadViewModel.setUploadResponse(finalResponse)
                } else {
                    uploadViewModel.setUploadResponse(null)
                }
            } catch (e: Exception) {
                uploadViewModel.setUploadResponse(null)
                setLog(any = e)
            } finally {
                handleLoading(false)
                setLog(any = uploadConfiguration)
                findNavController().navigate(R.id.action_upload_to_result)
            }
        }
    }

    private fun extractJson(raw: String): String {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        require(start != -1 && end != -1 && end > start) {
            "No valid JSON found"
        }
        return raw.substring(start, end + 1)
    }


    private fun handleLoading(isLoading: Boolean) {
        binding.btnUploadCreateStack.setLoading(
            isLoading,
            "Create Study Stack",
            "Wait Creating Your Stack...",
            binding.llUploadMain
        )
    }

    private fun buildPrompt(config: UploadConfiguration): String {
        val sb = StringBuilder()

        sb.append(
            "Summarize the document for a ${config.summaryAudienceLevel.lowercase()} audience. "
        )

        when (config.summaryLength.lowercase()) {
            "short" -> sb.append("Keep the summary short and concise. ")
            "medium" -> sb.append("Provide a moderately detailed summary. ")
            "detailed" -> sb.append("Provide a detailed and thorough summary. ")
        }

        if (config.summaryTitle != null) {
            sb.append("Use the summary title \"${config.summaryTitle}\". ")
        } else {
            sb.append("Generate an appropriate summary title. ")
        }

        if (isFlashcardGenerate) {
            sb.append(
                "Then create exactly ${config.flashcardCount} flashcards. " +
                        "Each flashcard must contain a question and an answer. "
            )

            if (config.flashcardTitle != null) {
                sb.append("Use the flashcard title \"${config.flashcardTitle}\". ")
            } else {
                sb.append("Generate a suitable flashcard title. ")
            }
        }

        sb.append(
            """
        Return the result strictly in the following JSON format.
        Do not include any explanation or extra text.
        If flashcards are not requested, set "flashcards" to null.
        Return only raw JSON. Do not wrap in ```json {} ``` just pure json so I can map to data model directly.
                    {
                        "summary": {
                            "summaryTitle": "string",
                            "summary": "string"
                        },
                        "flashcards": {
                            "flashcardTitle": "string",
                            "cards": [
                                { "question": "string", "answer": "string" }
                            ]
                        }
                    }
        """.trimIndent()
        )

        return sb.toString()
    }

    private fun confirmSwitchDialog(isUpload: Boolean) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Do you really want to switch mode?")
            setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, which ->
                changeOptionViews(isUpload)
            }
        }.show()
    }

    private fun resetField(isUpload: Boolean) {
        if (isUpload) {
            uriMimePair = null
            selectedFileName = ""
            binding.txtUploadFileName.text = getString(R.string.txt_upload_choose_file)
        } else {
            binding.etUploadPasteText.text?.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}