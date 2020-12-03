package com.ukrainianboyz.nearly.utils.apiUtils

import android.content.Context
import com.ukrainianboyz.nearly.R
import retrofit2.HttpException
import java.lang.Exception
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkRequest(val status: RequestStatus) {

     var error: String = ""
     private set


    fun attachError(exception: Exception, context: Context){
        if(status != RequestStatus.FAILED) return

        error = when (exception) {
            is SocketTimeoutException -> context.getString(R.string.network_error)
            is ConnectException -> context.getString(R.string.network_error)
            is UnknownHostException -> context.getString(R.string.server_unreachable)
            is HttpException -> getMessageFromHttpError(context, exception.message())
            else -> context.getString(R.string.operation_failed)
        }
    }
    //TODO: implement
    private fun getMessageFromHttpError(context: Context, exceptionText: String): String{
         if(exceptionText.contains("409")) {
            return context.getString(R.string.id_already_exists)
        }
        return context.getString(R.string.operation_failed)
    }
}