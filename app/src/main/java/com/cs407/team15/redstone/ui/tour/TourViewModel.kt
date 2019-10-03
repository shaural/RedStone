package com.cs407.team15.redstone.ui.tour

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.type.LatLng

abstract class TourViewModel : ViewModel() {

   private val _text = MutableLiveData<String>().apply {
        value = "This is Tour Fragment"
    }
    val text: LiveData<String> = _text


}