package com.svobnick.thisorthat.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SingleQuestionDataViewModel : ViewModel() {
    val firstText: MutableLiveData<String> = MutableLiveData()
    val lastText: MutableLiveData<String> = MutableLiveData()
    val firstRate: MutableLiveData<String> = MutableLiveData()
    val lastRate: MutableLiveData<String> = MutableLiveData()
    val firstPercent: MutableLiveData<String> = MutableLiveData()
    val lastPercent: MutableLiveData<String> = MutableLiveData()
    val choice: MutableLiveData<String> = MutableLiveData()
}