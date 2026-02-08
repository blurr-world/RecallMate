package com.madinaappstudio.recallmate.chat.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.FileDataPart
import com.google.ai.client.generativeai.type.content
import com.madinaappstudio.recallmate.BuildConfig
import com.madinaappstudio.recallmate.core.api.GeminiFileUploader
import com.madinaappstudio.recallmate.core.api.GeminiUploadCallback
import com.madinaappstudio.recallmate.core.models.ChatModel
import com.madinaappstudio.recallmate.core.utils.NetworkLiveData
import com.madinaappstudio.recallmate.core.utils.getFileName
import com.madinaappstudio.recallmate.core.utils.showNoInternet
import com.madinaappstudio.recallmate.core.utils.showToast
import com.madinaappstudio.recallmate.databinding.FragmentChatBinding
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chat: Chat
    private var uploadedGeminiFile: Pair<String, String>? = null
    private var pendingFile: Pair<String, String>? = null
    private lateinit var fileUri: Uri
    var hasConnection = false

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null && uploadedGeminiFile == null && pendingFile == null) {
                fileUri = uri
                handleFile(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mtbMainChat.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        NetworkLiveData(requireContext()).observe(viewLifecycleOwner) {
            hasConnection = it
        }

        setupRecyclerView()

        val model = GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY)
        chat = model.startChat()

        binding.etChatMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSendButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnChatSendMessage.setOnClickListener {
            if (hasConnection) {
                if (!canSend()) return@setOnClickListener

                val text = binding.etChatMessage.text.toString()
                binding.etChatMessage.text?.clear()

                chatAdapter.add(ChatModel(text = text, isUser = true, isTyping = false))
                chatAdapter.add(ChatModel(text = "Typing...", isUser = false, isTyping = true))
                binding.rvChatMain.scrollToPosition(chatAdapter.itemCount - 1)

                sendMessage(text)
            } else {
                showNoInternet(requireContext(), binding.root)
            }

        }

        binding.ilChatMain.setStartIconOnClickListener {
            if (hasConnection) {
                if (uploadedGeminiFile == null && pendingFile == null) {
                    pickFile.launch(arrayOf("application/pdf"))
                }
            } else {
                showNoInternet(requireContext(), binding.root)
            }
        }

        binding.btnChatClearFile.setOnClickListener {
            pendingFile = null
            binding.llChatFileName.visibility = View.GONE
            updateSendButton()
        }

        updateSendButton()
    }

    private fun canSend(): Boolean {
        val hasText = binding.etChatMessage.text?.toString()?.isNotBlank() == true
        val hasPendingFile = pendingFile != null
        return hasText || hasPendingFile
    }

    private fun updateSendButton() {
        val enabled = canSend()
        lifecycleScope.launch {
            binding.btnChatSendMessage.isEnabled = enabled
            binding.btnChatSendMessage.alpha = if (enabled) 1f else 0.6f
        }
    }

    private fun handleFile(uri: Uri) {
        GeminiFileUploader.upload(requireContext(), uri, object : GeminiUploadCallback {
            override fun onSuccess(fileUri: String, mimeType: String) {
                pendingFile = Pair(fileUri, mimeType)
                binding.txtChatFileName.text = getFileName(requireContext(), uri)
                lifecycleScope.launch {
                    binding.llChatFileName.visibility = View.VISIBLE
                }
                updateSendButton()
            }

            override fun onError(message: String) {
                pendingFile = null
                showToast(requireContext(), "Failed to upload file")
                updateSendButton()
            }
        })
    }

    private fun sendMessage(text: String) {
        updateSendButton()
        val prompt: Content

        if (pendingFile != null) {
            prompt = content {
                part(FileDataPart(pendingFile!!.first, pendingFile!!.second))
                text(
                    """
                    This PDF is the reference.
                    If the answer is not found inside it, warn the user.
                    """.trimIndent()
                )
            }

            uploadedGeminiFile = pendingFile
            pendingFile = null

            chatAdapter.add(ChatModel(text = getFileName(requireContext(), fileUri), isUser = true, isTyping = false))

            binding.llChatFileName.visibility = View.GONE
            binding.ilChatMain.setStartIconOnClickListener(null)
            binding.ilChatMain.startIconDrawable?.alpha = 80

            updateSendButton()
        } else {
            prompt = content { text(text) }
        }

        lifecycleScope.launch {
            try {
                val response = chat.sendMessage(prompt)
                val reply = response.text ?: "No reply"

                chatAdapter.removeTyping()
                chatAdapter.add(ChatModel(text = reply, isUser = false, isTyping = false))
                binding.rvChatMain.scrollToPosition(chatAdapter.itemCount - 1)
            } catch (e: Exception) {
                chatAdapter.replaceLast(ChatModel(text = "Error: ${e.message}", isUser = false, isTyping = false))
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(
            mutableListOf(ChatModel(
                text = "Hi! I'm your AI Study Assistant. How can I help you today?",
                isUser = false, isTyping =  false))
        )
        binding.rvChatMain.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatMain.adapter = chatAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
