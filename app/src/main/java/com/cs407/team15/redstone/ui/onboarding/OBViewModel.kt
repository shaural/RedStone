package com.cs407.team15.redstone.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OBViewModel : ViewModel(){

    private val _text = MutableLiveData<String>().apply {
        value = "Onboarding fragment"
    }
    val text: LiveData<String> = _text
}