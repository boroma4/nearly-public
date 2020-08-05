package com.hotukrainianboyz.nearly.utilTest

import android.content.Context
import com.hotukrainianboyz.nearly.utils.apiUtils.NetworkRequest
import com.hotukrainianboyz.nearly.utils.apiUtils.RequestStatus
import org.junit.Test
import org.junit.Assert.*
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.lang.Exception

class NetworkRequestClassTests {

    private val context = mock(Context::class.java)
    private val error = "error"

    @Test
    fun cannot_attach_error_to_non_failed_request(){
        Mockito.`when`(context.getString(anyInt())).thenReturn(error)

        var request = NetworkRequest(RequestStatus.PENDING)
        request.attachError(Exception("test"),context)
        assertEquals("",request.error)

        request = NetworkRequest(RequestStatus.SUCCESS)
        request.attachError(Exception("test"),context)
        assertEquals("",request.error)

        request = NetworkRequest(RequestStatus.UNDEFINED)
        request.attachError(Exception("test"),context)
        assertEquals("",request.error)

        request = NetworkRequest(RequestStatus.FAILED)
        request.attachError(Exception("test"),context)
        assertEquals(error,request.error)
    }
}