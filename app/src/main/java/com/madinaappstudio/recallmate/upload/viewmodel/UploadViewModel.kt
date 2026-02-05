package com.madinaappstudio.recallmate.upload.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madinaappstudio.recallmate.upload.model.AiResponseModel

class UploadViewModel: ViewModel() {

    private val _uploadResponse = MutableLiveData<AiResponseModel?>()
    val uploadResponse: LiveData<AiResponseModel?>
        get() = _uploadResponse

    fun setUploadResponse(response: AiResponseModel?) {
        _uploadResponse.value = response
    }


}