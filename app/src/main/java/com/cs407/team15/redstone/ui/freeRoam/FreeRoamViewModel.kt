package com.cs407.team15.redstone.ui.ar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FreeRoamViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Free Roam Fragment"
    }
    val text: LiveData<String> = _text
}