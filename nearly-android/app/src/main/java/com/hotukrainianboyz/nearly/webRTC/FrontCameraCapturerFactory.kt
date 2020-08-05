package com.hotukrainianboyz.nearly.webRTC

import android.content.Context
import android.util.Log
import org.webrtc.*

class FrontCameraCapturerFactory {
        fun create(context: Context): CameraVideoCapturer? {
            return if(Camera2Enumerator.isSupported(context)){
                createCapturer(Camera2Enumerator(context))
            }else{
                createCapturer(Camera1Enumerator(false))
            }
        }
        private fun createCapturer(enumerator: CameraEnumerator) : CameraVideoCapturer? {
            val deviceNames = enumerator.deviceNames
            for (deviceName in deviceNames) {
                if (enumerator.isFrontFacing(deviceName)) {
                    val videoCapturer = enumerator.createCapturer(deviceName, null)
                    if (videoCapturer != null) {
                        Log.d("CameraFactory", "Returned front facing device")
                        return videoCapturer
                    }
                }
            }
            Log.d("CameraFactory", "No front facing device found")
            return null
        }
}