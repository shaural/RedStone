package com.cs407.team15.redstone.ui.ar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ARViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is AR Fragment"
    }
    val text: LiveData<String> = _text
}