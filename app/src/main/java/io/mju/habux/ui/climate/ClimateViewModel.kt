package io.mju.habux.ui.climate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClimateViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is climate Fragment"
    }
    val text: LiveData<String> = _text
}