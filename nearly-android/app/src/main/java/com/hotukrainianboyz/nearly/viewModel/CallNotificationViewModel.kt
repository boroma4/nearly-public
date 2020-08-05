package com.hotukrainianboyz.nearly.viewModel

import androidx.lifecycle.MutableLiveData


object CallNotificationViewModel {

    var inComingCalls: MutableLiveData<MutableSet<String>> = MutableLiveData(mutableSetOf())
    private set

    fun placeCall(callerId: String): Boolean{
        val newSet = inComingCalls.value
        if(newSet!!.contains(callerId)) return false
        newSet.add(callerId)
        inComingCalls.postValue(newSet)
        return true
    }

    fun removeCall(callerId: String){
        val newSet = inComingCalls.value
        newSet?.remove(callerId)
        inComingCalls.postValue(newSet)
    }
}