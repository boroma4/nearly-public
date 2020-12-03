package com.ukrainianboyz.nearly.utils.apiUtils

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class NetworkRequestHelper(private val mScope: CoroutineScope, private val mTag: String) {

    private lateinit var mContext: Context

    constructor(scope: CoroutineScope,tag: String, context: Context) : this(scope, tag) {
        mContext = context
    }

    fun process(networkRequest: MutableLiveData<NetworkRequest>, action: suspend () -> Unit) {
        if(!this::mContext.isInitialized){
            Log.e("NetworkRequestHelper","Illegal NetoworkRequestHelper call")
            return
        }
        networkRequest.value = NetworkRequest(RequestStatus.PENDING)
        mScope.launch(Dispatchers.IO) {
            val newRequestValue = try {
                action()
                NetworkRequest(RequestStatus.SUCCESS)
            } catch (e: Exception) {
                Log.e(mTag, e.toString())
                val failedRequest = NetworkRequest(RequestStatus.FAILED)
                failedRequest.attachError(e, mContext)
                failedRequest
            }
            networkRequest.postValue(newRequestValue)
        }
    }

    fun process(actionName: String, action: suspend () -> Unit = {}, onSucceeded: () -> Unit = {}, onFailed: () -> Unit = {}){
        mScope.launch(Dispatchers.IO){
            try {
                action()
                onSucceeded()
            } catch (e: Exception) {
                Log.e(mTag, "$actionName failed with ${e.message}")
                onFailed()
            }
        }
    }
}