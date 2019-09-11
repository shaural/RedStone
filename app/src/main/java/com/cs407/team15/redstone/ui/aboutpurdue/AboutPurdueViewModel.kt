package com.cs407.team15.redstone.ui.aboutpurdue

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutPurdueViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is About Purdue Fragment"
    }
    val text: LiveData<String> = _text
}