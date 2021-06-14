package io.mju.habux.ui.system

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SystemViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is system Fragment"
    }
    val text: LiveData<String> = _text
}